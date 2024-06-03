package kol.money.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.bouncycastle.util.encoders.Hex;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tron.easywork.constant.TronConstants;
import org.tron.easywork.factory.ApiWrapperFactory;
import org.tron.easywork.factory.ApiWrapperFactory.NetType;
import org.tron.easywork.model.Trc20ContractInfo;
import org.tron.easywork.model.Trc20TransferInfo;
import org.tron.easywork.util.TransactionUtil;
import org.tron.easywork.util.Trc20ContractUtil;
import org.tron.easywork.util.TronConverter;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.contract.Contract;
import org.tron.trident.core.contract.Trc20Contract;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.proto.Chain.Transaction;
import org.tron.trident.proto.Contract.TriggerSmartContract;
import org.tron.trident.proto.Response.TransactionExtention;
import org.tron.trident.proto.Response.TransactionReturn;

import com.google.common.util.concurrent.RateLimiter;

import kol.account.repo.AccountRepo;
import kol.common.cache.GlobalCache;
import kol.common.config.GlobalConfig;
import kol.common.model.PasswordConfig;
import kol.common.utils.AESUtils;
import kol.config.model.Config;
import kol.config.service.ConfigService;
import kol.money.model.ChainEnum;
import kol.money.model.MoneyChainRecord;
import kol.money.model.MoneyRecord;
import kol.money.repo.MoneyChainRecordRepo;
import kol.money.repo.MoneyRecordRepo;
import kol.money.repo.WalletRepo;
import lombok.extern.slf4j.Slf4j;

/**
 * 充值业务
 * 
 * @author guanzhenggang@gmail.com
 */
@Service
@EnableScheduling
@Slf4j
public class RechargeService {

	/**
	 * USDT合约地址
	 */
	private static final String USDT_TOKEN_ADDRESS = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t";

	/**
	 * API键
	 */
	private static final String TRON_PRO_API_KEY = "621ad7ff-1c89-4e22-8d9a-763f3e67af06";

	public static final BigDecimal TRX_FEE = BigDecimal.valueOf(20);

	public static final BigDecimal COLLECT_MIN = BigDecimal.valueOf(3);
	ConfigService configService;
	final AccountRepo accountRepo;
	final MoneyChainRecordRepo chainRecordRepo;
	final MoneyRecordRepo moneyRecordRepo;
	final WalletRepo walletRepo;
	PasswordConfig passwordConfig;
	GlobalCache cache;
	ApiWrapper platformApi;
	List<String> txidList;

	Trc20ContractInfo usdtInfo;

	String privateKey;
	GlobalConfig config;

	RateLimiter rateLimiter;

	public RechargeService(AccountRepo accountRepo, MoneyChainRecordRepo chainRecordRepo,
			MoneyRecordRepo moneyRecordRepo, WalletRepo walletRepo, GlobalConfig config, PasswordConfig passwordConfig,
			ConfigService configService, GlobalCache cache) {
		this.accountRepo = accountRepo;
		this.chainRecordRepo = chainRecordRepo;
		this.moneyRecordRepo = moneyRecordRepo;
		this.walletRepo = walletRepo;
		this.rateLimiter = RateLimiter.create(20.0 / 60);
		this.config = config;
		this.passwordConfig = passwordConfig;
		this.configService = configService;
		this.cache = cache;
		privateKey = AESUtils.decrypt(configService.getValue(Config.KeyEnum.CHAIN_TRC20_PRIVATE),
				passwordConfig.getDataPassword());
		platformApi = ApiWrapperFactory.create(NetType.Mainnet, privateKey, TRON_PRO_API_KEY);
		txidList = chainRecordRepo.findAll().stream().map(i -> i.getTxid()).collect(Collectors.toList());
		usdtInfo = Trc20ContractUtil.readTrc20ContractInfo(USDT_TOKEN_ADDRESS, platformApi);
	}

	/**
	 * 定期检查链上数据进行充值，已充值的数据记录到本地数据库
	 */
	@Scheduled(fixedRate = 30 * 1000)
	@Transactional
	public void recharge() {
		if (txidList == null) {
			return;
		}
		try {
			platformApi.getBlockByLatestNum(20).getBlockList().stream().flatMap(i -> i.getTransactionsList().stream())
					.forEach(transactionExt -> {
						Transaction.Contract c = transactionExt.getTransaction().getRawData().getContract(0);
						String txid = Hex.toHexString(transactionExt.getTxid().toByteArray());
						if (c.getType() == Transaction.Contract.ContractType.TriggerSmartContract) {
							try {
								TriggerSmartContract tsc = c.getParameter().unpack(TriggerSmartContract.class);
								Trc20TransferInfo transfer = TransactionUtil.getTransferInfo(tsc);
								String to = transfer.getTo();
								String from = transfer.getFrom();
								BigDecimal amount = TronConverter.getRealAmount(transfer.getAmount(),
										usdtInfo.getDecimals().intValue());
								boolean isUsdt = transfer.getContractAddress().equals(USDT_TOKEN_ADDRESS);
								boolean isSuccess = TransactionUtil
										.isTransactionSuccess(transactionExt.getTransaction());
								boolean isNew = !txidList.contains(txid);
								if (isSuccess && isUsdt && isNew) {
									cache.accounts.stream().filter(
											acc -> acc.getTrxAddress() != null && acc.getTrxAddress().equals(to))
											.findAny().ifPresent(acc -> {
												txidList.add(txid);
												saveRecharge(acc.getId(), txid, from, to, amount);
											});
								}
							} catch (Exception e) {
								// ignore
							}
						}
					});

		} catch (IllegalException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 根据txid手动充值
	 * 
	 * @param txid
	 */
	@Transactional
	public void recharge(String txid) {
		try {
			Transaction t = platformApi.getTransactionById(txid);
			Transaction.Contract c = t.getRawData().getContract(0);
			TriggerSmartContract tsc = c.getParameter().unpack(TriggerSmartContract.class);
			Trc20TransferInfo transfer = TransactionUtil.getTransferInfo(tsc);
			String to = transfer.getTo();
			String from = transfer.getFrom();
			BigDecimal amount = TronConverter.getRealAmount(transfer.getAmount(), usdtInfo.getDecimals().intValue());
			boolean isUsdt = transfer.getContractAddress().equals(USDT_TOKEN_ADDRESS);
			boolean isSuccess = TransactionUtil.isTransactionSuccess(t);
			boolean isNew = !txidList.contains(txid);
			if (isSuccess && isUsdt && isNew) {
				cache.accounts.stream().filter(acc -> acc.getTrxAddress() != null && acc.getTrxAddress().equals(to))
						.findAny().ifPresent(acc -> {
							txidList.add(txid);
							saveRecharge(acc.getId(), txid, from, to, amount);
						});
			}
		} catch (Exception e) {
			// ignore
		}

	}

	/**
	 * 检查指定周期内的充值
	 * 
	 * @param duration
	 */
	public void recharge(Duration duration) {
		long seconds = duration.toSeconds();
		long totalPage = seconds / 3 / 20;// 每3秒一个块，每20个块一页
		try {
			long nowBlockNum = platformApi.getNowBlock().getBlockHeader().getRawData().getNumber();
			for (int page = 0; page <= totalPage; page++) {
				platformApi.getBlockByLimitNext(nowBlockNum - 20 * (page + 1), nowBlockNum - 20 * page).getBlockList()
						.stream().flatMap(i -> i.getTransactionsList().stream()).forEach(transactionExt -> {
							Transaction.Contract c = transactionExt.getTransaction().getRawData().getContract(0);
							String txid = Hex.toHexString(transactionExt.getTxid().toByteArray());
							if (c.getType() == Transaction.Contract.ContractType.TriggerSmartContract) {
								try {
									TriggerSmartContract tsc = c.getParameter().unpack(TriggerSmartContract.class);
									Trc20TransferInfo transfer = TransactionUtil.getTransferInfo(tsc);
									String to = transfer.getTo();
									String from = transfer.getFrom();
									BigDecimal amount = TronConverter.getRealAmount(transfer.getAmount(),
											usdtInfo.getDecimals().intValue());
									boolean isUsdt = transfer.getContractAddress().equals(USDT_TOKEN_ADDRESS);
									boolean isSuccess = TransactionUtil
											.isTransactionSuccess(transactionExt.getTransaction());
									boolean isNew = !txidList.contains(txid);
									if (isSuccess && isUsdt && isNew) {
										cache.accounts.stream().filter(
												acc -> acc.getTrxAddress() != null && acc.getTrxAddress().equals(to))
												.findAny().ifPresent(acc -> {
													txidList.add(txid);
													saveRecharge(acc.getId(), txid, from, to, amount);
												});
									}
								} catch (Exception e) {
									// ignore
								}
							}
						});
			}

		} catch (Exception e) {
			// ignore
		}
	}

	/**
	 * 定期归集资金到冷钱包
	 */
	@Scheduled(cron = "0 0 8 * * *")
	public void collect() {
		if (cache.accounts == null) {
			return;
		}
		cache.accounts.forEach(account -> {
			rateLimiter.acquire();
			String userTrxPrivate = account.getTrxPrivate();
			if (userTrxPrivate == null || userTrxPrivate.isBlank()) {
				return;
			}
			String coldAddress = configService.getValue(Config.KeyEnum.CHAIN_TRC20_ADDR);
			if (coldAddress == null || coldAddress.isBlank()) {
				return;
			}

			try {
				userTrxPrivate = AESUtils.decrypt(account.getTrxPrivate(), passwordConfig.getDataPassword());
				ApiWrapper userApi = ApiWrapperFactory.create(NetType.Mainnet, userTrxPrivate, TRON_PRO_API_KEY);
				String userAddress = userApi.keyPair.toBase58CheckAddress();
				BigDecimal balance = TronConverter.getRealAmount(
						Trc20ContractUtil.trc20BalanceOf(USDT_TOKEN_ADDRESS, userAddress, userApi),
						usdtInfo.getDecimals().intValue());
				BigDecimal trxBalance = TronConverter.getRealAmount(
						BigDecimal.valueOf(userApi.getAccountBalance(userAddress)), TronConstants.TRX_DECIMAL);

				if (balance.compareTo(COLLECT_MIN) < 0) {
					return;
				}
				log.info("归集扫描 钱包 {} USDT余额 {} TRX余额 {}", userAddress, balance, trxBalance);
				// 从热钱包发送手续费到客户钱包
				TransactionExtention te = platformApi.transfer(platformApi.keyPair.toBase58CheckAddress(),
						userApi.keyPair.toBase58CheckAddress(),
						TronConverter.getTransferAmount(TRX_FEE, TronConstants.TRX_DECIMAL).longValue());
				Transaction t = platformApi.signTransaction(te);
				TransactionReturn ret = platformApi.blockingStub.broadcastTransaction(t);
				if (!ret.getResult()) {
					log.info("手续费转账失败 {}", userApi.keyPair.toBase58CheckAddress());
					return;
				}
				// 把USDT转到冷钱包
				Contract contract = platformApi.getContract(USDT_TOKEN_ADDRESS);
				Trc20Contract token = new Trc20Contract(contract, userAddress, userApi);
				token.transfer(coldAddress,
						TronConverter.getTransferAmount(balance, usdtInfo.getDecimals().intValue()).longValue(), 0,
						"memo", 50 * TronConstants.TRX_SUN_RATE.longValue());
				log.info("归集完成 {} {}", userAddress, balance);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		});

	}

	/**
	 * 保存充值
	 */
	@Transactional(rollbackFor = Exception.class)
	public void saveRecharge(Long account, String transactionId, String from, String to, BigDecimal amount) {

		log.info("账户ID ：{}，金额：{}，交易ID：{}，to地址：{}，from地址：{}", account, amount, transactionId, to, from);
		// 记录到资金流水表
		MoneyRecord moneyRecord = new MoneyRecord();
		moneyRecord.setAccountId(account).setComment("TRC20充值").setType(MoneyRecord.Type.CHAIN_RECHARGE)
				.setWalletAddress(from).setAmount(amount);
		moneyRecord = moneyRecordRepo.save(moneyRecord);
		// 记录到链上转账业务表
		MoneyChainRecord moneyChainRecord = new MoneyChainRecord();
		moneyChainRecord.setAccountId(account).setAmount(amount).setChain(ChainEnum.TRC20).setTxid(transactionId)
				.setFromAddr(from).setToAddr(to).setMoneyRecordId(moneyRecord.getId());
		chainRecordRepo.save(moneyChainRecord);
		walletRepo.updateRechargeAmount(amount, account);
		log.info("充值成功 {} {}", account, amount);
	}

}
