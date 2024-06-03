package kol.trade.service;

import kol.account.model.Account;
import kol.account.model.RoleEnum;
import kol.account.repo.AccountRepo;
import kol.trade.dto.cmd.StrategyCmd;
import kol.trade.dto.vo.StrategyStatistics;
import kol.trade.dto.vo.StrategyVo;
import kol.trade.entity.Investment;
import kol.trade.entity.Position;
import kol.trade.entity.Strategy;
import kol.trade.entity.StrategySnapshot;
import kol.trade.repo.InvestmentRepo;
import kol.trade.repo.StrategyRepo;
import kol.trade.repo.StrategySnapshotRepo;
import org.apache.commons.lang3.RandomStringUtils;
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

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @author kent
 */
@Service
public class StrategyService {

    private static final BigDecimal PRINCIPAL = new BigDecimal("10000");
    final StrategyRepo strategyRepo;
    final StrategySnapshotRepo strategySnapshotRepo;
    final InvestmentRepo investRepo;
    final AccountRepo accountRepo;
    final InvestmentService investService;

    @Resource
    private PositionService positionService;

    public StrategyService(StrategyRepo strategyRepo, StrategySnapshotRepo strategySnapshotRepo,
                           InvestmentRepo investRepo, AccountRepo accountRepo, InvestmentService investService) {
        this.strategyRepo = strategyRepo;
        this.strategySnapshotRepo = strategySnapshotRepo;
        this.investRepo = investRepo;
        this.accountRepo = accountRepo;
        this.investService = investService;
    }

    /**
     * 添加策略
     *
     * @param cmd
     */
    @Transactional(rollbackFor = Exception.class)
    public String add(StrategyCmd cmd) {
        boolean result = cmd.getMinAmount().compareTo(cmd.getMaxAmount()) < 0;
        Assert.isTrue(result, "minAmount须小于maxAmount的值");
        Long accountId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(accountId);
        result = RoleEnum.ROLE_TRADER.equals(accountOptional.get().getRole());
        Assert.isTrue(result, "当前账号不是交易员，无法创建策略");

        Strategy strategy = new Strategy();
        BeanUtils.copyProperties(cmd, strategy);
        strategy.setAccountId(accountId);
        strategy.setStatus(Strategy.StatusEnum.ON);
        strategy.setPrincipal(PRINCIPAL);
        strategy.setBalance(PRINCIPAL);
        strategy.setMaxBalance(PRINCIPAL);
        strategy.setMaximumDrawdown(BigDecimal.ZERO);
        String secretKey = RandomStringUtils.randomAlphanumeric(128);
        strategy.setSecretKey(secretKey);
        strategy.setVipLevel(cmd.getVipLevel());


        strategy = strategyRepo.save(strategy);
        addSnapshot(strategy);
        addInvestment(accountId, strategy.getId());
        return secretKey;
    }

    /**
     * 添加策略快照
     *
     * @param strategy
     */
    public void addSnapshot(Strategy strategy) {
        StrategySnapshot snapshot = new StrategySnapshot();
        BeanUtils.copyProperties(strategy, snapshot);
        snapshot.setStrategyId(strategy.getId());
        snapshot.setId(null);
        strategySnapshotRepo.save(snapshot);
    }

    /**
     * 添加策略模拟投资
     *
     * @param accountId
     * @param strategyId
     */
    private void addInvestment(Long accountId, Long strategyId) {
        Investment invest = new Investment();
        invest.setApiKeyId(-1L);
        invest.setAccountId(accountId);
        invest.setStrategyId(strategyId);
        invest.setPrincipal(PRINCIPAL);
        invest.setBalance(PRINCIPAL);
        invest.setUseBalance(BigDecimal.ZERO);
        invest.setIsReal(false);
        invest.setIsPause(false);
        invest.setIsEnd(false);
        invest.setApiKey("模拟投资没有交易所KEY");
        investService.save(invest);
    }

    /**
     * 修改策略
     *
     * @param cmd
     */
    @Transactional(rollbackFor = Exception.class)
    public void modify(StrategyCmd cmd) {
        Optional<Strategy> optional = strategyRepo.findById(cmd.getId());
        Assert.isTrue(optional.isPresent(), "策略不存在");
        Strategy strategy = optional.get();
        BeanUtils.copyProperties(cmd, strategy);
        strategyRepo.save(strategy);

        if (Strategy.StatusEnum.OFF.equals(strategy.getStatus())) {
            investRepo.findByStrategyIdAndIsEnd(strategy.getId(), false)
                    .parallelStream().forEach(invest -> {
                        investService.stopInvestment(invest);
                    });
        }
    }

    /**
     * 查询策略列表
     *
     * @param strategyName
     * @param tags
     * @param pageIndex
     * @param pageSize
     * @return
     */
    public List<StrategyVo> list(String strategyName, Long accountId, String tags, String status, Integer type, int pageIndex, int pageSize) {
        Sort sort = Sort.by(Sort.Direction.DESC, "profitRate");
        PageRequest pageRequest = PageRequest.of(pageIndex - 1, pageSize, sort);

        if (accountId != null) {
            Optional<Account> accountOptional = accountRepo.findById(accountId);
            accountId = RoleEnum.ROLE_ADMIN.equals(accountOptional.get().getRole()) ? null : accountId;
        }
        Long finalAccountId = accountId;

        Specification spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicateList = new ArrayList<>();
            if (StringUtils.isNotBlank(status)) {
                Strategy.StatusEnum statusEnum = Strategy.StatusEnum.valueOf(status);
                predicateList.add(criteriaBuilder.equal(root.get("status"), statusEnum));
            }
            if (StringUtils.isNotBlank(strategyName)) {
                predicateList.add(criteriaBuilder.equal(root.get("name"), strategyName));
            }
            if (Objects.nonNull(finalAccountId)) {
                predicateList.add(criteriaBuilder.equal(root.get("accountId"), finalAccountId));
            }
            if (type != null) {
                predicateList.add(criteriaBuilder.equal(root.get("type"), type));
            }
            if (StringUtils.isNotBlank(tags)) {
                predicateList.add(criteriaBuilder.like(root.get("tags"), "%" + tags + "%"));
            }
            return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
        };
        Page<Strategy> page = strategyRepo.findAll(spec, pageRequest);
        List<Strategy> strategyList = page.getContent();


        List<Long> strategyIds = strategyList.stream().map(Strategy::getId).toList();
        List<StrategyStatistics> strategyStatisticsList = positionService.listByStrategyStatistics(strategyIds);

        List<StrategyVo> strategyVos = strategyList.stream().map(strategy -> {
            return buildStrategyVo(strategy, strategyStatisticsList);
        }).toList();

        return strategyVos;
    }

    /**
     * 查询策略详情
     *
     * @param id
     * @param num
     * @return
     */
    public StrategyVo getById(Long id, Integer num) {
        Optional<Strategy> optional = strategyRepo.findById(id);
        Assert.isTrue(optional.isPresent(), "策略不存在！");
        Strategy strategy = optional.get();
        List<BigDecimal> profitRateList = getProfitRateList(strategy.getId(), num);
        strategy.setProfitRateHistory(profitRateList);

        Position position = positionService.findLast(id);
        if (position != null) {
            strategy.setLastUpdateDate(position.getUpdatedAt());
            strategy.setLastPrice(position.getEntryPrice());
            strategy.setLastPositionSide(position.getPositionSide());
        }

        List<StrategyStatistics> strategyStatisticsList = positionService.listByStrategyStatistics(Arrays.asList(id));
        return buildStrategyVo(strategy, strategyStatisticsList);
    }

    private StrategyVo buildStrategyVo(Strategy strategy, List<StrategyStatistics> strategyStatisticsList){
        StrategyVo strategyVo = new StrategyVo();
        BeanUtils.copyProperties(strategy, strategyVo);
        strategyVo.setWinLossRate(BigDecimal.ZERO);

        // 计算盈亏比
        Optional<StrategyStatistics> first = strategyStatisticsList.stream()
                .filter(v -> v.getStrategyId().equals(strategyVo.getId())).findFirst();
        if (first.isPresent()) {
            StrategyStatistics strategyStatistics = first.get();
            Integer winCount = Optional.ofNullable(strategyStatistics.getWinCount()).orElse(0);
            Integer lossCount = Optional.ofNullable(strategyStatistics.getLossCount()).orElse(0);
            if (winCount > 0 && lossCount > 0) {
                BigDecimal winAvg = strategyStatistics.getWin().divide(BigDecimal.valueOf(winCount), 2, RoundingMode.HALF_UP);
                BigDecimal lossAvg = strategyStatistics.getLoss().divide(BigDecimal.valueOf(lossCount), 2, RoundingMode.HALF_UP).abs();
                if (lossAvg.compareTo(BigDecimal.ZERO) != 0) {
                    strategyVo.setWinLossRate(winAvg.divide(lossAvg, 2, RoundingMode.HALF_UP));
                }
            }
        }

        return strategyVo;
    }

    /**
     * 查询策略盈利快照
     *
     * @param strategyId
     * @param num
     * @return
     */
    private List<StrategySnapshot> getSnapshotList(Long strategyId, Integer num) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(0, num, sort);

        Specification spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicateList = new ArrayList<>();
            if (strategyId != null) {
                predicateList.add(criteriaBuilder.equal(root.get("strategyId"), strategyId));
            }
            return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
        };
        Page<StrategySnapshot> page = strategySnapshotRepo.findAll(spec, pageRequest);
        return page.getContent();
    }

    /**
     * 查询策略盈利趋势数据
     *
     * @param strategyId
     * @param limitNum
     * @return
     */
    public List<BigDecimal> getProfitRateList(Long strategyId, Integer limitNum) {
        return strategySnapshotRepo.findByStrategyId(strategyId, limitNum);
    }
}
