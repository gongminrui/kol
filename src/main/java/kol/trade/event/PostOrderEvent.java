package kol.trade.event;

import kol.trade.dto.cmd.OrderCmd;
import kol.trade.enums.ExchangeEnum;
import kol.trade.enums.MarketEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 开平仓事件
 *
 * @author kent
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class PostOrderEvent extends OrderCmd {
    /**
     * 策略仓位ID
     */
    private Long positionNo;
    /**
     * 交易所
     */
    private ExchangeEnum exchange;
    /**
     * 交易市场
     */
    private MarketEnum market;
}
