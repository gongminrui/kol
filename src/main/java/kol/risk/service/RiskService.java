package kol.risk.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;

import io.contek.invoker.okx.api.common._Ticker;
import kol.common.annotation.NotLogRecord;
import kol.common.cache.GlobalCache;
import kol.config.model.Config;
import kol.config.service.ConfigService;
import kol.trade.dto.cmd.OrderCmd;
import kol.trade.entity.Investment;
import kol.trade.entity.Position;
import kol.trade.enums.CapitalModeEnum;
import kol.trade.enums.MarketEnum;
import kol.trade.enums.PositionSideEnum;
import kol.trade.enums.PositionStatusEnum;
import kol.trade.enums.TradeTypeEnum;
import kol.trade.service.InvestmentService;
import kol.trade.service.trading.ClosePositionService;
import kol.trade.service.trading.TradingService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kent
 */
@Slf4j
@Service
@AllArgsConstructor()
public class RiskService implements CommandLineRunner {
	final AsyncEventBus asyncEventBus;
	final ConfigService configService;
	final GlobalCache cache;
	final InvestmentService investmentService;
	final TradingService tradingService;
	static final BigDecimal INVESTMENT_STOP_LOSS=BigDecimal.valueOf(-0.2);
	ClosePositionService closePositionService;

	@Override
	public void run(String... args) throws Exception {
		registerEvent();
	}

	private void registerEvent() {
		asyncEventBus.register(this);
	}

	/**
	 * 订阅交易对价格信息
	 *
	 * @param ticker
	 */
	@Subscribe
	public void subscribeTicker(_Ticker ticker) {
		cache.positionMap.values().parallelStream().forEach(p -> {
			if (p.getSymbol().equals(ticker.instId)) {
				BigDecimal price = new BigDecimal(ticker.last);
				calFollowCost(p, price);
			}
		});
	}

	/**
	 * 订阅仓位信息
	 *
	 * @param position
	 */
	@Subscribe
	public void subscribePosition(Position position) {
		String cachePositionNo = position.getPositionNo() + "_" + position.getAccountId();
		if (PositionStatusEnum.OPEN.equals(position.getStatus())) {
			cache.positionMap.put(cachePositionNo, position);
		} else {
			cache.positionMap.remove(cachePositionNo);
		}
	}

	/**
	 * 处理投资缓存
	 * 
	 * @param investment
	 */
	@Subscribe
	public void subscribeInvestment(Investment investment) {
		if (investment.getIsEnd()) {
			cache.getInvestments().remove(investment);
		} else {
			cache.getInvestments().add(investment);
		}
	}

	private void calFollowCost(Position position, BigDecimal price) {
		BigDecimal profit;
		if (PositionSideEnum.LONG == position.getPositionSide()) {
			// 收益=（平仓价格-开仓价）*平仓量+上次收益-平仓手续费
			profit = price.subtract(position.getEntryPrice())
					.multiply(position.getEntryVol().subtract(position.getExitVol())).add(position.getProfit());
		} else {
			// 收益=（开仓价-平仓价格）*平仓量+上次收益-平仓手续费
			profit = position.getEntryPrice().subtract(price)
					.multiply(position.getEntryVol().subtract(position.getExitVol())).add(position.getProfit());
		}

		if (profit.compareTo(BigDecimal.ZERO) > 0) {
			position.setFollowCost(profit.multiply(configService.getFollowCostRatio()));
		}
	}

	/**
	 * 仓位止盈止损
	 * @param ticker
	 */
	@Subscribe
	@Transactional
	public void positionStopLoss(_Ticker ticker) {
		if(cache.positionMap==null) {
			return;
		}
		cache.positionMap.values().parallelStream().forEach(p -> {
			if (p.getSymbol()!=null&&!p.getIsReal()&&p.getSymbol().equals(ticker.instId)&&p.getStatus()==PositionStatusEnum.OPEN) {
				p.updateFloatingGrossProfit(new BigDecimal(ticker.last));
				BigDecimal price=new BigDecimal(ticker.last);
				BigDecimal stopGainPrice=p.getStopGainPrice();
				BigDecimal stopLossPrice=p.getStopLossPrice();
				if(p.getPositionSide()==PositionSideEnum.LONG) {
					//多头止盈止损
					boolean stopGain=(stopGainPrice!=null&&stopGainPrice.compareTo(BigDecimal.ZERO)>0&&price.compareTo(stopGainPrice)>0);
					boolean stopLoss=(stopLossPrice!=null&&stopLossPrice.compareTo(BigDecimal.ZERO)>0&&price.compareTo(stopLossPrice)<0);
					if(stopGain||stopLoss) {
						OrderCmd cmd=new OrderCmd();
						cmd.setSymbol(p.getSymbol());
						cmd.setExchange(p.getExchange());
						cmd.setLeverage(p.getLeverage());
						cmd.setMarket(MarketEnum.PERP);
						cmd.setPositionNo(p.getPositionNo());
						cmd.setCapitalMode(CapitalModeEnum.REMAIND);
						cmd.setStrategyId(p.getStrategyId());
						cmd.setPositionRatio(BigDecimal.ONE);
						cmd.setTradeType(TradeTypeEnum.CLOSE_LONG);
						closePositionService.closePosition(cmd);
						log.info("策略止盈止损平仓 策略id {} 仓位id {}",cmd.getStrategyId(),p.getId());
					}
				}else {
					//空头止盈止损
					boolean stopGain=(stopGainPrice!=null&&stopGainPrice.compareTo(BigDecimal.ZERO)>0&&price.compareTo(stopGainPrice)<0);
					boolean stopLoss=(stopLossPrice!=null&&stopLossPrice.compareTo(BigDecimal.ZERO)>0&&price.compareTo(stopLossPrice)>0);
					if(stopGain||stopLoss) {
						OrderCmd cmd=new OrderCmd();
						cmd.setSymbol(p.getSymbol());
						cmd.setExchange(p.getExchange());
						cmd.setLeverage(p.getLeverage());
						cmd.setMarket(MarketEnum.PERP);
						cmd.setPositionNo(p.getPositionNo());
						cmd.setCapitalMode(CapitalModeEnum.REMAIND);
						cmd.setStrategyId(p.getStrategyId());
						cmd.setPositionRatio(BigDecimal.ONE);
						cmd.setTradeType(TradeTypeEnum.CLOSE_SHORT);
						closePositionService.closePosition(cmd);
						log.info("策略止盈止损平仓 策略id {} 仓位id {}",cmd.getStrategyId(),p.getId());
					}
				}
			}
		});
	}
	
	/**
	 * 策略清盘止损
	 */
	@Scheduled(cron = "* * * * * *")
	@NotLogRecord
	public void investmentStopLoss() {
		if(cache.getInvestments()==null) {
			return;
		}
		cache.getInvestments().parallelStream().filter(investment->investment.getIsReal()).forEach(investment -> {
			BigDecimal principal = investment.getPrincipal();// 本金
			List<Position> positions=cache.positionMap.values().parallelStream().filter(p->p.getInvestmentId()==investment.getId()).toList();
			if(positions.isEmpty()) {
				return;
			}
			//浮动利润
			BigDecimal floatingProfit= positions.parallelStream().map(p -> p.getFloatingGrossProfit())
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			//固定利润
			BigDecimal fixedProfit=investment.getBalance().subtract(principal);
			
			//跟单浮动利润
			BigDecimal profitRate =fixedProfit.add(floatingProfit).divide(principal,4,RoundingMode.DOWN);

			if(investment.getBalance().add(investment.getUseBalance()).compareTo(principal)<0&&profitRate.compareTo(INVESTMENT_STOP_LOSS)<0) {
				log.info("跟单ID {} 盈亏率 {} 已达到本金回撤清盘线",investment.getId(),profitRate);
				investmentService.stopInvestment(investment);
			}
			
			
		});
	}
	
}
