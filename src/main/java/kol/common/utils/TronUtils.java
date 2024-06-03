package kol.common.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.contract.Contract;
import org.tron.trident.core.contract.Trc20Contract;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.proto.Chain.Transaction;
import org.tron.trident.proto.Response.Account;
import org.tron.trident.proto.Response.BlockListExtention;
import org.tron.trident.proto.Response.TransactionExtention;

/**
 * 波场接口工具，
 * 
 * @author admin
 *
 */
public class TronUtils {
	public static final int SUN_POWER = 6;
	/**
	 * trongrid.io 可以申请apikey，用于ApiWrapper与主节点的通信，量大需要自己搭建主节点
	 */
	public static final String API_KEY = "621ad7ff-1c89-4e22-8d9a-763f3e67af06";

	/**
	 * USDT TRC20合约地址
	 */
	public static final String TRC20_USDT_CONTRACT_ADDR = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t";
	/**
	 * 
	 */
	private ApiWrapper apiWrapper;
	/**
	 * 
	 */
	private KeyPair keyPair;

	public static final long FEE_LIMIT = 500000000;

	public static final int BLOCK_DURATION_SECONDS = 3;

	public TronUtils(String hexPrivateKey) {
		this(hexPrivateKey, API_KEY);
	}

	public TronUtils(String hexPrivateKey, String ApiKey) {
		keyPair = new KeyPair(hexPrivateKey);
		apiWrapper = ApiWrapper.ofMainnet(hexPrivateKey, API_KEY);
	}

	/**
	 * 查询trx余额
	 * 
	 * @param addr
	 * @return
	 */
	public BigDecimal getBalance(String addr) {
		Account account = apiWrapper.getAccount(addr);
		return BigDecimal.valueOf(account.getBalance()).multiply(BigDecimal.valueOf(0.1).pow(SUN_POWER));
	}

	/**
	 * 查询trc20合约余额
	 * 
	 * @param contractAddr
	 * @param address
	 * @return
	 */
	public BigDecimal getTrc20Balance(String contractAddr, String address) {
		Contract contract = apiWrapper.getContract(contractAddr);
		Trc20Contract token = new Trc20Contract(contract, keyPair.toHexAddress(), apiWrapper);
		BigInteger decimals = token.decimals();
		BigInteger balance = token.balanceOf(address);
		return BigDecimal.valueOf(balance.longValue()).divide(BigDecimal.valueOf(10).pow(decimals.intValue()));
	}

	/**
	 * 查询trc20 usdt合约余额
	 * 
	 * @param address
	 * @return
	 */
	public BigDecimal getTrc20UsdtBalance(String address) {
		return getTrc20Balance(TRC20_USDT_CONTRACT_ADDR, address);
	}

	/**
	 * trx 转账
	 * 
	 * @param toAddress
	 * @param amount
	 * @return
	 */
	public String transfer(String toAddress, long amount) {
		try {
			TransactionExtention te = apiWrapper.transfer(keyPair.toHexAddress(), toAddress, amount);
			Transaction t = apiWrapper.signTransaction(te);
			return apiWrapper.broadcastTransaction(t);
		} catch (IllegalException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * trx 转账
	 * 
	 * @param toAddress
	 * @param amount
	 * @return
	 */
	public String transfer(String toAddress, BigDecimal amount) {
		return transfer(toAddress, amount.multiply(BigDecimal.TEN.pow(SUN_POWER)).longValue());
	}

	/**
	 * trc20 合约转账
	 * 
	 * @param contractAddr
	 * @param toAddress
	 * @param amount
	 * @return
	 */
	public String transferTrc20(String contractAddr, String toAddress, long amount) {
		Contract contract = apiWrapper.getContract(contractAddr);
		Trc20Contract token = new Trc20Contract(contract, keyPair.toHexAddress(), apiWrapper);
		return token.transfer(toAddress, amount, 0, "memo", FEE_LIMIT);
	}

	/**
	 * trc20 合约转账
	 * 
	 * @param contractAddr
	 * @param toAddress
	 * @param amount
	 * @return
	 */
	public String transferTrc20(String contractAddr, String toAddress, BigDecimal amount) {
		Contract contract = apiWrapper.getContract(contractAddr);
		Trc20Contract token = new Trc20Contract(contract, keyPair.toHexAddress(), apiWrapper);
		BigInteger decimals = token.decimals();
		return token.transfer(toAddress, amount.multiply(BigDecimal.TEN.pow(decimals.intValue())).longValue(), 0,
				"memo", FEE_LIMIT);
	}

	/**
	 * trc20 USDT合约转账
	 * 
	 * @param toAddress
	 * @param amount
	 * @return
	 */
	public String transferTrc20Usdt(String toAddress, long amount) {
		return transferTrc20(TRC20_USDT_CONTRACT_ADDR, toAddress, amount);
	}

	/**
	 * trc20 USDT合约转账
	 * 
	 * @param toAddress
	 * @param amount
	 * @return
	 */
	public String transferTrc20Usdt(String toAddress, BigDecimal amount) {
		return transferTrc20(TRC20_USDT_CONTRACT_ADDR, toAddress, amount);
	}

	/**
	 * 查询一段时间内的交易列表
	 * @param duration 一段时间
	 * @return
	 */
	public List<TransactionExtention> getTranscations() {
		try {
			BlockListExtention blockList = apiWrapper
					.getBlockByLatestNum(20);
			return blockList.getBlockList().stream().flatMap(b -> b.getTransactionsList().stream()).toList();
		} catch (IllegalException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 根据交易id查询
	 * @param txid
	 * @return
	 */
	public Transaction getTranscation(String txid) {
		try {
			return apiWrapper.getTransactionById(txid);
		} catch (IllegalException e) {
			throw new RuntimeException(e);
		}
	}
	

	
}
