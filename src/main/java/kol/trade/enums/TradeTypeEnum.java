package kol.trade.enums;

import lombok.Getter;

/**
 * 订单交易类型
 *
 * @author kent
 */
public enum TradeTypeEnum {
    /**
     * 开多
     */
    OPEN_LONG(SideEnum.BUY, PositionSideEnum.LONG),
    /**
     * 开空
     */
    OPEN_SHORT(SideEnum.SELL, PositionSideEnum.SHORT),
    /**
     * 平多
     */
    CLOSE_LONG(SideEnum.SELL, PositionSideEnum.LONG),
    /**
     * 平空
     */
    CLOSE_SHORT(SideEnum.BUY, PositionSideEnum.SHORT);

    @Getter
    private SideEnum side;
    @Getter
    private PositionSideEnum positionSide;

    TradeTypeEnum(SideEnum side, PositionSideEnum positionSide) {
        this.side = side;
        this.positionSide = positionSide;
    }

}
