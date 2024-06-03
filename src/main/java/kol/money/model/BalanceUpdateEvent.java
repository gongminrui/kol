package kol.money.model;

import java.math.BigDecimal;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import lombok.Getter;
/**
 * 余额更新事件，实现{@link ApplicationListener}可订阅此消息
 * @author guanzhenggang@gmail.com
 *
 */
@Getter
public class BalanceUpdateEvent extends ApplicationEvent{
	
	private Long accountId;
	
	private BigDecimal balance;

	public BalanceUpdateEvent(Object source, Long accountId,BigDecimal balance) {
		super(source);
		this.accountId=accountId;
		this.balance=balance;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
