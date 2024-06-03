package kol.money.model;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;

import kol.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
/**
 * 链上资产
 * 
 * 链上充值到热钱包--->更新用户数据库余额--->链上资产归集到冷钱包
 * @author admin
 *
 */
//@Entity(name = "account")
//@Data
//@Accessors(chain = true)
//@EqualsAndHashCode(callSuper = true)
public class ChainAsset extends BaseEntity{
	/**
	 * 账户ID
	 */
	private Long  accountId;
	/**
	 * 公链名
	 */
	private String chain;
	/**
	 * 钱包地址 
	 */
	private String address;
	/**
	 * 私钥地址
	 */
	@Column(length = 1000)
	private String privateKey;
	/**
	 * token名
	 */
	private String token;
	/**
	 * 余额  balance=链上资产-balanceTemp
	 */
	private BigInteger balance;
	/**
	 * 余额，被归集后会减少 
	 */
	private BigInteger balanceTemp;
	/**
	 * 资产精度
	 */
	private Integer decimal; 
}
