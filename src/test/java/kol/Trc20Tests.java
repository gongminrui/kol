package kol;

import org.tron.easywork.factory.ApiWrapperFactory;
import org.tron.easywork.factory.ApiWrapperFactory.NetType;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.proto.Response.BlockListExtention;

import kol.common.utils.AESUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Trc20Tests {
	public static void main(String[] args) throws Exception {
		String privateKey = "07180b4f1d316e6ce2530ca260fdb1fbc7b49ec93b84c2a0b032ae8e8c6c9b795d1cb09f9c114f6e426cb4a87fad472921fbf0d5472d3e73f0aa0a9df59973509b0142507161b4145843a543547ec29d";
		String toHexAddress = "";
		String txid = "";
		String API_KEY = "621ad7ff-1c89-4e22-8d9a-763f3e67af06";
		String USDT_TOKEN_ADDRESS = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t";

//		log.info("TRX {}",utils.getBalance("TFJ9iAWaurXYTYkCeM8AsYKWAjmkjmHvEP"));
//		
//		log.info("USDT {}",utils.getTrc20UsdtBalance("TFJ9iAWaurXYTYkCeM8AsYKWAjmkjmHvEP"));
//		
//		log.info("{}",utils.transfer(toHexAddress, BigDecimal.ONE));

//		log.info("{}",utils.transferTrc20Usdt(toHexAddress, BigDecimal.ONE));
		privateKey=AESUtils.decrypt(privateKey, "a154c52565e9e7f94bfc08a1fe702624");
		ApiWrapper api = ApiWrapperFactory.create(NetType.Mainnet, privateKey, API_KEY);
//		Trc20ContractInfo usdtInfo = Trc20ContractUtil.readTrc20ContractInfo(USDT_TOKEN_ADDRESS, api);
//		BigDecimal balance= Trc20ContractUtil.trc20BalanceOf(USDT_TOKEN_ADDRESS, api.keyPair.toBase58CheckAddress(), api);
//		log.info("{}",balance);
		// 从热钱包发送手续费到客户钱包
//		long amount = TronConverter.getTransferAmount(BigDecimal.valueOf(20), TronConstants.TRX_DECIMAL)
//				.longValue();
//		System.out.println(amount);
//		TransactionExtention te = api.transfer("TAh1gLyLN8gphDTtUHcKUMp1ezgjfnSGmy",
//				"TNgqR7WnKsz3uM5tavr8EdX1CHDA31GuB2", amount);
//		Transaction t = api.signTransaction(te);
//		api.broadcastTransaction(t);
		// 把USDT转到冷钱包
//		String userTrxPrivate = AESUtils.decrypt(
//				"",
//				"a154c52565e9e7f94bfc08a1fe702624");
//		
//		ApiWrapper userApi = ApiWrapperFactory.create(NetType.Mainnet, userTrxPrivate, API_KEY);
//		Contract contract = userApi.getContract(USDT_TOKEN_ADDRESS);
//		Trc20Contract token = new Trc20Contract(contract, userApi.keyPair.toBase58CheckAddress(), userApi);
//		token.transfer("TAh1gLyLN8gphDTtUHcKUMp1ezgjfnSGmy",
//				TronConverter.getTransferAmount(BigDecimal.ONE, usdtInfo.getDecimals().intValue()).longValue(), 0,
//				"memo", 50 * TronConstants.TRX_SUN_RATE.longValue());
		
		long blockid=api.getNowBlock().getBlockHeader().getRawData().getNumber();
		log.info("blockid {}",blockid);
		BlockListExtention ext=api.getBlockByLimitNext(blockid-40, blockid-20);
		log.info("block count {}",ext.getBlockCount());
	}
}
