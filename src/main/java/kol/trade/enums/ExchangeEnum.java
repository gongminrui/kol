package kol.trade.enums;

import lombok.Getter;

/**
 * 交易所
 *
 * @author guanzhenggang@gmail.com
 */
public enum ExchangeEnum {
    OKX("OKX", "OK交易所"),
    BINANCE("BINANCE", "币安交易所"),
	BYBIT("BYBIT","BYBIT");
    @Getter
    private String zh;
    @Getter
    private String en;

    ExchangeEnum(String en, String zh) {
        this.zh = zh;
        this.en = en;
    }
}
