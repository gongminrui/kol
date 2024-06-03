package kol.money.service;

import cn.hutool.core.date.LocalDateTimeUtil;
import kol.account.model.AccountRebate;
import kol.account.repo.AccountRebateRepo;
import kol.account.service.AccountRebateService;
import kol.account.service.AccountService;
import kol.common.config.GlobalConfig;
import kol.common.model.ErrorMsg;
import kol.common.model.MessageType;
import kol.common.model.PageResponse;
import kol.common.service.BaseService;
import kol.common.utils.RoundTool;
import kol.config.service.ConfigService;
import kol.money.model.Statement;
import kol.money.repo.StatementRepo;
import kol.trade.dto.vo.PositionSumProfit;
import kol.trade.entity.Position;
import kol.trade.service.InvestmentService;
import kol.trade.service.PositionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author Gongminrui
 * @date 2023-04-17 21:57
 */
@Service
@Slf4j
public class StatementService extends BaseService {
    @Resource
    private StatementRepo statementRepo;
    @Resource
    private PositionService positionService;
    @Resource
    private AccountService accountService;
    @Resource
    private WalletService walletService;
    @Resource
    private GlobalConfig globalConfig;
    @Resource
    private AccountRebateService accountRebateService;
    @Resource
    private ConfigService configService;
    @Resource
    private InvestmentService investmentService;
    @Resource
    private AccountRebateRepo accountRebateRepo;

    /**
     * 获得我的账单列表
     *
     * @param pageIndex
     * @param pageSize
     * @return
     */
    public PageResponse<Statement> pageMyStatement(int pageIndex, int pageSize) {
        return pageStatement(getCurrentAccountId(), null, pageIndex, pageSize);
    }

    /**
     * 获得账单翻页数据
     *
     * @param accountId
     * @param email
     * @param pageIndex
     * @param pageSize
     * @return
     */
    public PageResponse<Statement> pageStatement(Long accountId, String email, int pageIndex, int pageSize) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(pageIndex - 1, pageSize, sort);
        Specification spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicateList = new ArrayList<>();
            if (accountId != null) {
                predicateList.add(criteriaBuilder.equal(root.get("accountId"), accountId));
            }
            if (email != null) {
                predicateList.add(criteriaBuilder.like(root.get("email"), email));
            }
            return criteriaBuilder.and(predicateList.toArray(new Predicate[0]));
        };
        Page page = statementRepo.findAll(spec, pageRequest);
        return PageResponse.of(page.getContent(), page.getTotalElements(), pageIndex, pageSize);
    }

    /**
     * 结账
     *
     * @return
     */
    @Transactional
    public int settle(Long id) {
        Long currentAccountId = getCurrentAccountId();
        Optional<Statement> statementOptional = statementRepo.findById(id);
        Assert.isTrue(statementOptional.isPresent(), "没有账单");
        Statement statement = statementOptional.get();
        final Long accountId = statement.getAccountId();
        Assert.isTrue(!statement.getSettle(), "该账单已经结算了");
        if (!accountId.equals(currentAccountId)) {
            Assert.isTrue(false, "非当前用户，不能结算");
        }

        int res = balance(statement);
        if (res == -1) {
            Assert.isTrue(false, "余额不足，结算失败");
        }
        accountService.normalAccount(accountId);
        return 1;
    }

    private int balance(Statement statement) {
        Long accountId = statement.getAccountId();
        // 结算金额
        final BigDecimal amount = statement.getAmount();
        // 钱包余额不足，不够结账
        final BigDecimal balance = walletService.getBalance(accountId);
        if (amount.compareTo(balance) > 0) {
            return -1;
        }

        // 扣余额
        walletService.settle(accountId, amount);

        statement.setSettle(true);
        statementRepo.save(statement);

        // 返利
        accountRebateService.calVipRebate(accountId, amount, true);
        return 1;
    }

    /**
     * 手动结算
     *
     * @return
     */
    @Transactional
    public void manualGenStatement() {
        Assert.isTrue(isAdmin(), ErrorMsg.NOT_POWER.getMsg());
        Assert.isTrue(globalConfig.isBillMode(), "操作失败，只有账单模式才有效");

        Optional<Position> positionOptional = positionService.findFirstNotSettlement();
        if (positionOptional.isEmpty()) {
            log.info("目前还没有未结算的开平仓记录");
            return;
        }

        LocalDateTime startDate = LocalDateTimeUtil.of(positionOptional.get().getCreatedAt());
        LocalDateTime endDate = LocalDateTime.now();

        addStatement(startDate, endDate);
    }

    private BigDecimal getFollowCostRatio(Long accountId) {
        Optional<AccountRebate> rebateOptional = accountRebateRepo.findByAccountId(accountId);
        if (rebateOptional.isPresent()) {
            return rebateOptional.get().getRebate();
        }

        return configService.getFollowCostRatio();
    }

    /**
     * 添加结账单
     *
     * @param startDate
     * @param endDate
     * @return
     */
    @Transactional
    public int addStatement(LocalDateTime startDate, LocalDateTime endDate) {
        List<PositionSumProfit> positions = positionService.listPositionSumProfit(startDate, endDate);

        // 是否净值模式（账单小于最小结算金额时累计到下一次结算期）
        final boolean billNetValue = configService.isBillNetValue();
        // 最小结算金额
        final BigDecimal minSettleAmount = configService.getMinSettleAmount();

        Date startTime = Date.from(startDate.atZone(ZoneOffset.systemDefault()).toInstant());
        Date endTime = Date.from(endDate.atZone(ZoneOffset.systemDefault()).toInstant());
        List<Statement> list = new ArrayList<>();
        for (PositionSumProfit v : positions) {
            if (v.getTotalCount() == 0) {
                continue;
            }
            BigDecimal profit = v.getProfit();

            boolean isSettle = profit.compareTo(minSettleAmount) < 0;
            if (billNetValue && isSettle) {
                continue;
            }

            Statement statement = new Statement();
            Long accountId = v.getAccountId();
            statement.setAccountId(accountId);
            accountService.getById(accountId).ifPresent(account -> {
                statement.setEmail(account.getEmail());
            });
            statement.setStartDate(startTime);
            statement.setEndDate(endTime);
            statement.setProfit(profit);
            statement.setAmount(RoundTool.round(statement.getProfit().multiply(getFollowCostRatio(accountId))));
            statement.setPositionCount(v.getTotalCount());
            statement.setWinPositionCount(v.getWinCount());
            // 盈利小于等于0直接结算了
            statement.setSettle(isSettle);

            if (!statement.getSettle()) {
                sendMessage(statement.getAccountId(), MessageType.BILLING_INFO, MessageType.BILLING_INFORM);
                // 返利
                accountRebateService.calVipRebate(accountId, statement.getAmount(), false);
            }

            // 更新仓位的结算状态
            positionService.updateWaitSettlement(accountId, startDate, endDate);
            list.add(statement);
        }

        statementRepo.saveAll(list);

        return 1;
    }

    /**
     * 禁用未结算的用户
     */
    @Transactional
    public void disableNotBalanceAccount() {
        Assert.isTrue(isAdmin(), ErrorMsg.NOT_POWER.getMsg());
        Assert.isTrue(globalConfig.isBillMode(), "操作失败，只有账单模式才有效");

        List<Statement> statements = statementRepo.findBySettle(false);
        for (Statement statement : statements) {
            Long accountId = statement.getAccountId();
            investmentService.stopAccountAllInvestment(accountId, "未按时结算账单，暂停跟单");
            accountService.disableAccount(accountId);
        }
    }

    /**
     * 每月25号生成账单
     */
    @Scheduled(cron = "0 0 0 25 * *")
    // 最后一天的前两天 0点执行
//    @Scheduled(cron = "0 0 0 L-2 * *")
    @Transactional
    public void generateStatement() {
        // 只有开启了vip功能，才有账单功能
        if (!globalConfig.isBillMode()) {
            return;
        }
//        TimeRange lastMonth = TimeRangeUtil.lastMonth();
//        addStatement(lastMonth.getStartDate(), lastMonth.getEndDate());


        Optional<Position> positionOptional = positionService.findFirstNotSettlement();
        if (positionOptional.isEmpty()) {
            log.info("目前还没有未结算的开平仓记录");
            return;
        }

        LocalDateTime startDate = LocalDateTimeUtil.of(positionOptional.get().getCreatedAt());
        LocalDateTime endDate = LocalDateTime.now();
        addStatement(startDate, endDate);

    }

    /**
     * 每月1号处理未结算的账单
     */
    @Scheduled(cron = "0 0 0 1 * *")
    @Transactional
    public void handleNotSettleStatement() {
        // 只有开启了vip功能，才有账单功能
        if (!globalConfig.isBillMode()) {
            return;
        }
        List<Statement> statements = statementRepo.findBySettle(false);
        // 自动结算账单
        for (Statement statement : statements) {
            balance(statement);
        }

        // 结算失败的用户，停止跟单，禁用账户
        for (Statement statement : statements) {
            if (statement.getSettle()) {
                continue;
            }
            Long accountId = statement.getAccountId();
            investmentService.stopAccountAllInvestment(accountId, "未按时结算账单，暂停跟单");
            accountService.freezeAccount(accountId);
            sendMessage(statement.getAccountId(), MessageType.BILLING_BALANCE_FAIL_TITLE, MessageType.BILLING_BALANCE_FAIL);
        }
    }
}
