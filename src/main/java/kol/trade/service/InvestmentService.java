package kol.trade.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.criteria.Predicate;

import kol.account.model.AccountStatusEnum;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.google.common.eventbus.AsyncEventBus;

import io.contek.invoker.okx.api.common._AccountBalance;
import kol.account.model.Account;
import kol.account.model.RoleEnum;
import kol.account.repo.AccountRepo;
import kol.common.config.GlobalConfig;
import kol.common.model.PageResponse;
import kol.common.model.VipAccessException;
import kol.common.service.BaseService;
import kol.common.utils.JsonUtils;
import kol.exchange.service.OkxService;
import kol.trade.dto.cmd.InvestmentCmd;
import kol.trade.dto.cmd.PageInvestmentCmd;
import kol.trade.entity.ExchangeKey;
import kol.trade.entity.InvestSnapshot;
import kol.trade.entity.Investment;
import kol.trade.entity.Position;
import kol.trade.entity.Strategy;
import kol.trade.enums.PositionSideEnum;
import kol.trade.enums.PositionStatusEnum;
import kol.trade.repo.ExchangeKeyRepo;
import kol.trade.repo.InvestSnapshotRepo;
import kol.trade.repo.InvestmentRepo;
import kol.trade.repo.PositionRepo;
import kol.trade.repo.StrategyRepo;
import kol.trade.service.trading.ClosePositionService;
import lombok.AllArgsConstructor;

/**
 * @author kent
 */
@Service
@AllArgsConstructor
public class InvestmentService extends BaseService {
    final InvestmentRepo investRepo;
    final StrategyRepo strategyRepo;
    final PositionRepo positionRepo;
    final ExchangeKeyRepo exchangeKeyRepo;
    final InvestSnapshotRepo snapshotRepo;
    final OkxService exchangeUtil;
    final ClosePositionService closePositionService;
    final private AccountRepo accountRepo;
    final private AsyncEventBus eventBus;
    final GlobalConfig config;


    @Transactional(rollbackFor = Exception.class)
    public void add(InvestmentCmd cmd) {
        Optional<Strategy> optionalStrategy = strategyRepo.findById(cmd.getStrategyId());
        Assert.isTrue(optionalStrategy.isPresent(), "策略不存在");
        Strategy strategy = optionalStrategy.get();
        Long accountId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Account> account = accountRepo.findById(accountId);
        boolean result = AccountStatusEnum.NORMAL.equals(account.get().getStatus());
        Assert.isTrue(result, "账号有未支付的结算单，无法跟单");

        if (config.getEnableVip()) {
            int userVip = account.get().getVipLevel();
            int strategyVip = strategy.getVipLevel();
            if (strategyVip > userVip) {
                throw new VipAccessException();
            }
        }

        result = cmd.getPrincipal().compareTo(strategy.getMaxAmount()) < 1
                || cmd.getPrincipal().compareTo(strategy.getMinAmount()) > -1;
        Assert.isTrue(result, "跟单资金不符合策略资金要求");
        Integer strategyInvestCount = investRepo.getStrategyInvestCount(strategy.getId());

        result = strategyInvestCount < strategy.getBigFollowCount();
        Assert.isTrue(result, "策略跟单人数已满");


        String exchangeKey = "模拟投资没有交易所KEY";
        Optional<ExchangeKey> optionalExchangeKey = exchangeKeyRepo.findByIdAndAccountId(cmd.getApiKeyId(), accountId);
        Assert.isTrue(optionalExchangeKey.isPresent(), "交易所KEY不存在");

        Optional<Investment> investOptional =
                investRepo.findByIsEndAndAccountIdAndStrategyId(false, accountId, cmd.getStrategyId());
        Assert.isTrue(!investOptional.isPresent(), "已跟单此策略不能重复跟单");

        //判断当前跟投资金小于交易所资金-已投资金
        BigDecimal totalPrincipal = investRepo.getTotalPrincipal(accountId, cmd.getApiKeyId());
        _AccountBalance balance = exchangeUtil.getAccountBalance(optionalExchangeKey.get());
        String accountBalance = balance.details.stream()
                .filter(f -> f.ccy.equals("USDT"))
                .map(m -> m.cashBal).findFirst().orElse("0");
        BigDecimal accountPrincipal = new BigDecimal(accountBalance);
        result = cmd.getPrincipal().compareTo(accountPrincipal.subtract(totalPrincipal)) < 1;
        Assert.isTrue(result, "账户资金不足");
        exchangeKey = JsonUtils.objectToJson(optionalExchangeKey.get());
        cmd.setIsReal(Boolean.TRUE);

        Investment investment = new Investment();
        investment.setStrategyId(cmd.getStrategyId());
        investment.setApiKeyId(cmd.getApiKeyId());
        investment.setAccountId(accountId);
        investment.setPrincipal(cmd.getPrincipal());
        investment.setBalance(cmd.getPrincipal());
        investment.setUseBalance(BigDecimal.ZERO);
        investment.setApiKey(exchangeKey);
        investment.setIsReal(cmd.getIsReal());
        investment.setStrategyFusing(cmd.getStrategyFusing());


        investment.setEmail(account.get().getEmail());
        investment = investRepo.save(investment);
        strategyRepo.updateAlreadyFollowCount(1, investment.getStrategyId());
        addSnapshot(investment);
        eventBus.post(investment);
    }

    /**
     * 用于开平仓保存投资信息
     *
     * @param invest
     */
    public void save(Investment invest) {
        invest = investRepo.save(invest);
        addSnapshot(invest);
    }

    /**
     * 添加投资快照
     *
     * @param investment
     */
    public void addSnapshot(Investment investment) {
        InvestSnapshot snapshot = new InvestSnapshot();
        BeanUtils.copyProperties(investment, snapshot);
        snapshot.setInvestId(investment.getId());
        snapshot.setId(null);
        BigDecimal profitRate = investment.getBalance().subtract(investment.getPrincipal())
                .divide(investment.getPrincipal(), 5, RoundingMode.DOWN);
        snapshot.setProfitRate(profitRate);
        snapshot = snapshotRepo.save(snapshot);
    }

    /**
     * 停止结束跟单
     *
     * @param investmentId
     */
    @Transactional(rollbackFor = Exception.class)
    public void stopInvestment(Long investmentId) {
        Long accountId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Investment> optional = investRepo.findByIdAndAccountId(investmentId, accountId);
        Assert.isTrue(optional.isPresent(), "跟投不成在，无法停止");

        Investment investment = optional.get();
        this.stopInvestment(investment);
    }

    /**
     * 停止策略跟单
     *
     * @param invest
     */
    public void stopInvestment(Investment invest) {
        invest.setIsEnd(true);
        invest.setEndTime(new Date());
        invest = investRepo.save(invest);
        closePositionService.closePosition(invest);
        strategyRepo.updateAlreadyFollowCount(-1, invest.getStrategyId());
        eventBus.post(invest);
    }

    /**
     * 停止用户所有跟单
     *
     * @param accountId
     */
    @Transactional
    public void stopAccountAllInvestment(Long accountId, String pauseReason) {
        investRepo.findByIsEndAndAccountId(false, accountId).forEach(i -> {
            i.setPauseReason(pauseReason);
            stopInvestment(i);
        });
    }

    public Investment getInfo(Long investmentId) {
        Long accountId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Investment> optional = investRepo.findByIdAndAccountId(investmentId, accountId);
        Assert.isTrue(optional.isPresent(), "跟投不存在");
        return optional.get();
    }

    public List<Map<String, Object>> listHistory() {
        Long accountId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return investRepo.findListHistory(accountId);
    }

    public List<Map<String, Object>> listOpenInvestments(long accountId) {
        return investRepo.findListCurrent(accountId);
    }

    public List<Map<String, Object>> listCurrent() {
        Long accountId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Position> positionList = positionRepo.findByAccountIdAndStatus(accountId, PositionStatusEnum.OPEN);
        List<Map<String, Object>> mapList = investRepo.findListCurrent(accountId);
        boolean result = CollectionUtils.isEmpty(positionList);
        if (result) {
            return mapList;
        }

        List<Map<String, Object>> currentList = new ArrayList<>();
        mapList.stream().forEach(f -> {
            Long strategyId = Long.parseLong(f.get("strategyId").toString());
            HashMap<String, Object> map = new HashMap(f.size());
            map.putAll(f);
            positionList.parallelStream().filter(p -> p.getStrategyId().equals(strategyId))
                    .forEach(p -> {
                        if (ObjectUtils.isNotEmpty(p)) {
                            BigDecimal currentPrice = exchangeUtil.getPrice(p.getSymbol());
                            p.setExitPrice(currentPrice);
                            calPositionProfit(p);
                        }
                        map.put("position", p);
                    });
            currentList.add(map);
        });
        return currentList;
    }

    /**
     * 计算收益和收益率
     *
     * @param position
     */
    private void calPositionProfit(Position position) {
        //价格波动率
        BigDecimal priceVolatility;
        if (PositionSideEnum.LONG == position.getPositionSide()) {
            priceVolatility = position.getExitPrice()
                    .subtract(position.getEntryPrice())
                    .divide(position.getEntryPrice(), 10, RoundingMode.DOWN);
        } else {
            priceVolatility = position.getEntryPrice()
                    .subtract(position.getExitPrice())
                    .divide(position.getEntryPrice(), 10, RoundingMode.DOWN);
        }
        //收益=价格波动率*杠杆倍数
        BigDecimal profitRate = priceVolatility.multiply(position.getLeverage());
        position.setProfitRate(profitRate);

        //收益=持仓量*开仓均价*价格波动率
        BigDecimal profit = position.getEntryVol().multiply(position.getEntryPrice()).multiply(priceVolatility);
        position.setProfit(position.getProfit().add(profit));
    }

    public PageResponse<Investment> pageInvestment(PageInvestmentCmd pageInvestmentCmd) {
        Long strategyId = pageInvestmentCmd.getStrategyId();
        Account loginAccount = getLoginAccount();

        if (RoleEnum.ROLE_TRADER == loginAccount.getRole()) {
            Optional<Strategy> optionalStrategy = strategyRepo.findById(strategyId);
            if (optionalStrategy.isEmpty()) {
                return PageResponse.buildSuccess();
            }
            Strategy strategy = optionalStrategy.get();
            if (strategy.getAccountId().longValue() != loginAccount.getId().longValue()) {
                return PageResponse.buildSuccess();
            }
        } else if (RoleEnum.ROLE_ADMIN != loginAccount.getRole()) {
            return PageResponse.buildSuccess();
        }

        int pageIndex = pageInvestmentCmd.getPageIndex();
        int pageSize = pageInvestmentCmd.getPageSize();
        String email = pageInvestmentCmd.getEmail();

        Boolean isEnd = false;
        Boolean isPause = false;
        Boolean isReal = true;
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(pageIndex - 1, pageSize, sort);
        Specification spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicateList = new ArrayList<>();
            if (strategyId != null) {
                predicateList.add(criteriaBuilder.equal(root.get("strategyId"), strategyId));
            }
            if (StringUtils.isNotBlank(email)) {
                predicateList.add(criteriaBuilder.like(root.get("email"), "%" + email + "%"));
            }
            if (isReal != null) {
                predicateList.add(criteriaBuilder.equal(root.get("isReal"), isReal));
            }
            if (isPause != null) {
                predicateList.add(criteriaBuilder.equal(root.get("isPause"), isPause));
            }
            if (isEnd != null) {
                predicateList.add(criteriaBuilder.equal(root.get("isEnd"), isEnd));
            }

            return criteriaBuilder.and(predicateList.toArray(new Predicate[0]));
        };
        Page<Investment> page = investRepo.findAll(spec, pageRequest);
        return PageResponse.of(page.getContent(), page.getTotalElements(), pageSize, pageIndex);
    }

    /**
     * 是否有跟单
     *
     * @param accountId
     * @return
     */
    public boolean hasInvertment(Long accountId) {
        return investRepo.countByAccountIdAndIsEndAndIsPause(accountId, false, false) > 0;
    }

    /**
     * 停止账号所有策略跟单
     *
     * @param accountId
     * @param pauseReason
     * @return
     */
    public Integer accountEndInvestment(Long accountId, String pauseReason) {
        return investRepo.accountEndInvestment(accountId, pauseReason);
    }
}
