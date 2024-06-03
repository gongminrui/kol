package kol.trade.entity;

import javax.persistence.Entity;

import kol.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@EqualsAndHashCode(callSuper = true)
@Data
public class StrategyDispatch extends BaseEntity{
	private Long strategyId;
	private String url;
	private String apiSecret;
	private Long dispatchStrategyId;
}
