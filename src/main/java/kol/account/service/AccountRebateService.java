package kol.account.service;

import kol.account.dto.cmd.AgentCmd;
import kol.account.dto.vo.AccountRebateVo;
import kol.account.dto.vo.AgentVO;
import kol.account.model.Account;
import kol.account.model.AccountRebate;
import kol.account.model.AccountRelation;
import kol.account.model.RoleEnum;
import kol.account.repo.AccountRebateRepo;
import kol.account.repo.AccountRepo;
import kol.common.config.GlobalConfig;
import kol.common.model.PageResponse;
import kol.common.model.TimeRange;
import kol.common.service.BaseService;
import kol.common.utils.RoundTool;
import kol.config.model.Config;
import kol.config.service.ConfigService;
import kol.money.model.Wallet;
import kol.money.service.RebateService;
import kol.money.service.WalletService;
import kol.trade.entity.Position;
import kol.trade.entity.Strategy;
import kol.trade.repo.PositionRepo;
import kol.trade.repo.StrategyRepo;
import kol.vip.service.VipService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Gongminrui
 * @date 2023-03-04 19:06
 */
@Service
@Slf4j
public class AccountRebateService extends BaseService {
    @Resource
    private AccountRebateRepo accountRebateRepo;
    @Resource
    private AccountRelationService accountRelationService;
    @Resource
    private AccountRepo accountRepo;
    @Resource
    private WalletService walletService;
    @Resource
    private ConfigService configService;
    @Resource
    private PositionRepo positionRepo;
    @Resource
    private StrategyRepo strategyRepo;
    @Resource
    private GlobalConfig globalConfig;
    @Resource
    @Lazy
    private VipService vipService;
    @Resource
    private RebateService rebateService;

    public PageResponse<AgentVO> getNext(int pageIndex, int pageSize, String email) {
        email = Optional.ofNullable(email).orElse("");
        Account loginAccount = getLoginAccount();
        List<AccountRebateVo> accountRebateVos = accountRelationService.listNext(loginAccount.getId(), email);
        int size = accountRebateVos.size();

        int start = (pageIndex - 1) * pageIndex;
        int end = Math.min(size, start + pageSize);
        accountRebateVos = accountRebateVos.subList(start, end);

        List<AgentVO> list = accountRebateVos.stream().map(v -> {
            AgentVO agentVO = new AgentVO();
            agentVO.setAccountId(v.getId());
            agentVO.setEmail(v.getEmail());
            agentVO.setInviteCode(v.getInviteCode());
            agentVO.setRebate(Optional.ofNullable(v.getRebate()).orElse(BigDecimal.ZERO));
            agentVO.setRebateAmount(Optional.ofNullable(v.getRebateAmount()).orElse(BigDecimal.ZERO));
            agentVO.setFollowAmount(Optional.ofNullable(v.getFollowCostAmount()).orElse(BigDecimal.ZERO));
            agentVO.setInviteCount(Optional.ofNullable(v.getInviteCount()).orElse(0));
            agentVO.setRole(v.getRole());
            return agentVO;
        }).collect(Collectors.toList());

        return PageResponse.of(list, size, pageIndex, pageSize);
    }

    /**
     * 根据email获取用户信息
     *
     * @param email
     * @return
     */
    public AgentVO getAgentVOByEmail(String email) {
        Optional<Account> accountOptional = accountRepo.findByEmail(email);
        Assert.isTrue(accountOptional.isPresent(), "用户不存在");
        Account account = accountOptional.get();
        Optional<AccountRelation> relationOptional = accountRelationService.findByAccountId(account.getId());
        Assert.isTrue(relationOptional.isPresent(), "用户不存在");
        Assert.isTrue(relationOptional.get().getPId().longValue() == getCurrentAccountId().longValue(), "email不存在");

        return getAgentVO(account);
    }

    /**
     * 获得代理商信息
     *
     * @return
     */
    public AgentVO getAgentVO() {
        Account loginAccount = getLoginAccount();
        return getAgentVO(loginAccount);
    }

    private AgentVO getAgentVO(Account account) {
        Long accountId = account.getId();
        Optional<Wallet> walletOptional = walletService.findByAccountId(accountId);
        BigDecimal rebateAmount = BigDecimal.ZERO;
        BigDecimal followAmount = BigDecimal.ZERO;

        if (walletOptional.isPresent()) {
            Wallet wallet = walletOptional.get();
            rebateAmount = wallet.getRebateAmount();
            followAmount = wallet.getFollowCostAmount();
        }

        Optional<AccountRebate> rebateOptional = accountRebateRepo.findByAccountId(accountId);
        BigDecimal rebate = rebateOptional.isEmpty() ? BigDecimal.ZERO : rebateOptional.get().getRebate();

        AgentVO agentVO = new AgentVO();
        agentVO.setAccountId(accountId);
        agentVO.setInviteCode(account.getInviteCode());
        agentVO.setRebate(rebate);
        agentVO.setRebateAmount(rebateAmount);
        agentVO.setInviteCount(accountRelationService.getNextCount(accountId));
        agentVO.setFollowAmount(followAmount);
        agentVO.setRole(account.getRole());
        agentVO.setEmail(account.getEmail());
        return agentVO;
    }

    @Transactional
    public void saveOrUpdate(AgentCmd agentCmd) {
        Account loginAccount = getLoginAccount();
        Assert.isTrue(loginAccount != null, "未登录不能操作");

        if (RoleEnum.ROLE_ADMIN == loginAccount.getRole()) {
            saveOrUpdatePartner(loginAccount, agentCmd);
        } else if (RoleEnum.ROLE_PARTNER == loginAccount.getRole()) {
            saveOrUpdateHelper(loginAccount, agentCmd);
        } else {
            Assert.isTrue(false, "无权限操作");
        }
    }

    /**
     * 保存或者更新合伙人比例
     *
     * @param agentCmd
     */
    private void saveOrUpdatePartner(Account loginAccount, AgentCmd agentCmd) {
        Long accountId = agentCmd.getAccountId();
        Optional<Account> accountOptional = accountRepo.findById(accountId);
        Assert.isTrue(accountOptional.isPresent(), "不存在的用户");
        Account account = accountOptional.get();
        Assert.isTrue(RoleEnum.ROLE_HELPER != account.getRole(), "不能给助力人设置合伙人比例");

        Account parentAccount = getParentAccount(accountId);
        Assert.isTrue(parentAccount == null || parentAccount.getRole() == RoleEnum.ROLE_USER, "上级用户是代理商，不能设置该用户为合伙人");

        BigDecimal rebate = agentCmd.getInviteRebate();
        BigDecimal defaultRebate = getMaxDefaultRebate();
        Assert.isTrue(rebate.compareTo(defaultRebate) <= 0, "返佣不能高于" + defaultRebate.multiply(BigDecimal.valueOf(100)) + "%");

        account.setRole(RoleEnum.ROLE_PARTNER);
        accountRepo.save(account);

        saveData(accountId, rebate);
    }

    /**
     * 保存或更新助力人比例
     *
     * @param agentCmd
     */
    private void saveOrUpdateHelper(Account loginAccount, AgentCmd agentCmd) {
        BigDecimal rebate = agentCmd.getInviteRebate();
        Long accountId = agentCmd.getAccountId();
        Optional<Account> accountOptional = accountRepo.findById(accountId);
        Assert.isTrue(accountOptional.isPresent(), "不存在的用户");

        Optional<AccountRelation> relationOptional = accountRelationService.findByAccountId(accountId);
        Assert.isTrue(relationOptional.isPresent(), "没有上级不能设置为助力人");

        AccountRelation accountRelation = relationOptional.get();
        Long pId = accountRelation.getPId();
        Assert.isTrue(pId.longValue() == loginAccount.getId(), "无权限操作");

        Optional<AccountRebate> accountRebateOptional = accountRebateRepo.findByAccountId(pId);
        Assert.isTrue(accountRebateOptional.isPresent(), "上级还没有返佣比例");
        Assert.isTrue(rebate.compareTo(accountRebateOptional.get().getRebate()) <= 0, "助力人的返佣比例不能超过合伙人的返佣比例");

        Account account = accountOptional.get();
        account.setRole(RoleEnum.ROLE_HELPER);
        accountRepo.save(account);

        saveData(accountId, rebate);
    }

    /**
     * 保存默认返佣
     *
     * @param accountId
     */
    public void saveDefaultRebate(Long accountId) {
        BigDecimal minDefaultRebate = getMinDefaultRebate();
        saveData(accountId, minDefaultRebate);
    }

    /**
     * 更新用户为普通用户
     *
     * @param accountId
     */
    @Transactional
    public void updateToUser(Long accountId) {
        Optional<Account> accountOptional = accountRepo.findById(accountId);
        if (accountOptional.isEmpty()) {
            return;
        }
        Account account = accountOptional.get();
        if (RoleEnum.ROLE_PARTNER != account.getRole() && RoleEnum.ROLE_HELPER != account.getRole()) {
            return;
        }

        BigDecimal defaultRebate = getMinDefaultRebate();
        updateToUserAccount(account, defaultRebate);

        List<Account> nextAccountList = accountRepo.listNext(accountId);
        if (!CollectionUtils.isEmpty(nextAccountList)) {
            for (Account nextAccount : nextAccountList) {
                updateToUserAccount(nextAccount, defaultRebate);
            }
        }
    }

    private void updateToUserAccount(Account account, BigDecimal defaultRebate) {
        if (RoleEnum.ROLE_PARTNER != account.getRole() && RoleEnum.ROLE_HELPER != account.getRole()) {
            return;
        }

        account.setRole(RoleEnum.ROLE_USER);
        accountRepo.save(account);
        Optional<AccountRebate> accountRebateOptional = accountRebateRepo.findByAccountId(account.getId());
        accountRebateOptional.ifPresent(v -> {
            v.setRebate(defaultRebate);
            accountRebateRepo.save(v);
        });
    }


    private void saveData(Long accountId, BigDecimal rebate) {
        Optional<AccountRebate> optional = accountRebateRepo.findByAccountId(accountId);
        AccountRebate accountRebate = optional.isPresent() ? optional.get() : new AccountRebate();
        accountRebate.setAccountId(accountId);
        accountRebate.setRebate(rebate);
        accountRebate.setValid(true);
        accountRebateRepo.save(accountRebate);
    }

    /**
     * 获得上级信息
     *
     * @param accountId
     * @return
     */
    private Account getParentAccount(Long accountId) {
        Optional<AccountRelation> relationOptional = accountRelationService.findByAccountId(accountId);
        if (relationOptional.isEmpty()) {
            accountRelationService.createRelation(accountId, null);
            return null;
        }
        AccountRelation accountRelation = relationOptional.get();
        if (accountRelation.getPId() == null) {
            return null;
        }
        Optional<Account> account = accountRepo.findById(accountRelation.getPId());
        return account.get();
    }

    /**
     * 邀请人默认返佣
     *
     * @return
     */
    private BigDecimal getMinDefaultRebate() {
        return new BigDecimal(configService.getValue(Config.KeyEnum.INVITE_REBATE));
    }

    /**
     * 合伙人最大返佣
     *
     * @return
     */
    private BigDecimal getMaxDefaultRebate() {
        return new BigDecimal(configService.getValue(Config.KeyEnum.PARTNER_MAX_REBATE));
    }

    /**
     * 获得vip关系一级返利的比例
     *
     * @return
     */
    private BigDecimal getVipOneLevelRebate() {
        String value = configService.getValue(Config.KeyEnum.VIP_ONE_LEVEL_REBATE);
        if (StringUtils.isNotBlank(value)) {
            return new BigDecimal(value);
        }
        return BigDecimal.valueOf(0.15);
    }

    /**
     * 获得vip关系二级返利比例
     *
     * @return
     */
    private BigDecimal getVipTwoLevelRebate() {
        String value = configService.getValue(Config.KeyEnum.VIP_TWO_LEVEL_REBATE);
        if (StringUtils.isNotBlank(value)) {
            return new BigDecimal(value);
        }
        return BigDecimal.valueOf(0.05);
    }

    /**
     * 计算返佣
     *
     * @param accountId 本次平仓的账号
     * @param fee       本次收取的返佣费用
     * @return 剩余的跟单服务费
     */
    @Deprecated
    public BigDecimal calRebate(Long accountId, BigDecimal fee) {
        Optional<AccountRelation> optional = accountRelationService.findByAccountId(accountId);
        if (optional.isEmpty()) {
            return fee;
        }

        // 系统默认返佣
        final BigDecimal defaultRebate = getMinDefaultRebate();
        final List<AccountRebate> accountRebates = getAccountRebates(optional.get().getRelationTree());
        // 当前用户没有上级，那么在关系中没有人能分到返佣
        int level = accountRebates.size();
        if (level == 0) {
            return fee;
        }
        BigDecimal totalAmount = BigDecimal.ZERO;
        // 固定取第一个为合伙人
        AccountRebate partnetAccount = accountRebates.get(0);

        RebateInfo partnerRebateInfo = partnerRebate(partnetAccount);
        // 合伙人的返佣比例
        BigDecimal partnetRebate = partnerRebateInfo.getRebate();
        // 判断合伙人是否是普通人，普通人就拿默认的比例
        final boolean isPartnetToUser = isUser(partnerRebateInfo);
        if (isPartnetToUser) {
            partnetRebate = defaultRebate;
        }
        // 合伙人要收取的服务费
        BigDecimal amount = fee.multiply(partnetRebate);

        // 如果只有一级关系，只能是合伙人或者普通邀请人
        if (level == 1) {
            updateRebateAmount(partnetAccount.getAccountId(), amount);
            totalAmount = totalAmount.add(amount);
        } else if (level == 2) { //有两级的情况下
            AccountRebate helperAaccout = accountRebates.get(1);
            // 如果合伙人变为了普通人，那么助力人直接就是默认奖励，合伙人拿到不奖励
            if (isPartnetToUser) {
                // 收取的服务费
                updateRebateAmount(helperAaccout.getAccountId(), amount);
                totalAmount = totalAmount.add(amount);
            } else {
                RebateInfo helperRebateInfo = helperRebate(helperAaccout);
                BigDecimal helperRebate = helperRebateInfo.getRebate();
                // 更新助力人的跟单奖励
                BigDecimal helperAmount = BigDecimal.ZERO;
                // 助力人是普通人，只能拿到默认比例
                if (isUser(helperRebateInfo)) {
                    helperRebate = defaultRebate;
                }
                helperAmount = fee.multiply(helperRebate);
                // 合伙人拿剩下的一部分
                amount = amount.subtract(helperAmount);

                updateRebateAmount(helperAaccout.getAccountId(), helperAmount);
                totalAmount = totalAmount.add(helperAmount);

                if (amount.compareTo(BigDecimal.ZERO) != 0) {
                    updateRebateAmount(partnetAccount.getAccountId(), amount);
                    totalAmount = totalAmount.add(amount);
                }
            }
        }
        return fee.subtract(totalAmount);
    }

    /**
     * 按照时间返佣
     *
     * @param timeRange
     */
    @Transactional
    public void timeRebate(TimeRange timeRange) {
        if (!globalConfig.isRealTimeMode()) {
            return;
        }

        List<Position> list = positionRepo.findByExitTimeBetween(timeRange.getStartDate(), timeRange.getEndDate());
        List<FollowCostInfo> accounts = new ArrayList<>();
        List<FollowCostInfo> strategys = new ArrayList<>();
        for (Position position : list) {
            if (!position.getIsReal()) {
                continue;
            }

            Long strategyId = position.getStrategyId();
            FollowCostInfo strategyFollow = strategys.stream().filter(v -> v.getStrategyId().equals(strategyId)).findFirst().orElse(new FollowCostInfo());
            if (strategyFollow.getStrategyId() == null) {
                strategyFollow.setStrategyId(strategyId);
                strategys.add(strategyFollow);
            }
            strategyFollow.setFollowCost(strategyFollow.getFollowCost().add(position.getFollowCost()));


            Long accountId = position.getAccountId();
            FollowCostInfo followCostInfo = accounts.stream().filter(v -> v.getAccountId().equals(accountId)).findFirst().orElse(new FollowCostInfo());

            if (followCostInfo.getAccountId() == null) {
                followCostInfo.setAccountId(accountId);
                accounts.add(followCostInfo);
                // 在返佣的时候如果余额为负数，就要把负数不分减掉，不能返给用户
                BigDecimal balance = walletService.getBalance(accountId);
                if (balance.compareTo(BigDecimal.ZERO) < 0) {
                    followCostInfo.setFollowCost(followCostInfo.getFollowCost().add(balance));
                    strategyFollow.setFollowCost(strategyFollow.getFollowCost().add(balance));
                }

            }
            followCostInfo.setFollowCost(followCostInfo.getFollowCost().add(position.getFollowCost()));
        }

        for (FollowCostInfo followCostInfo : accounts) {
            if (BigDecimal.ZERO.compareTo(followCostInfo.getFollowCost()) == 0) {
                continue;
            }
            calRebateV2(followCostInfo.getAccountId(), followCostInfo.getFollowCost());
        }

        for (FollowCostInfo followCostInfo : strategys) {
            calStrategyRebate(followCostInfo);
        }
    }

    /**
     * 策略返佣
     *
     * @param followCostInfo
     */
    private void calStrategyRebate(FollowCostInfo followCostInfo) {
        Optional<Strategy> strategyOptional = strategyRepo.findById(followCostInfo.getStrategyId());
        if (strategyOptional.isEmpty()) {
            return;
        }
        Strategy strategy = strategyOptional.get();
        BigDecimal rebate = strategy.getRebate();
        if (BigDecimal.ZERO.compareTo(rebate) == 0) {
            return;
        }
        BigDecimal amount = rebate.multiply(followCostInfo.getFollowCost());
        updateRebateAmount(strategy.getAccountId(), amount);
    }

    private BigDecimal calRebateV2(Long accountId, BigDecimal fee) {
        Optional<AccountRelation> optional = accountRelationService.findByAccountId(accountId);
        if (optional.isEmpty()) {
            return fee;
        }

        final List<AccountRebate> accountRebates = getAccountRebates(optional.get().getRelationTree());
        // 当前用户没有上级，那么在关系中没有人能分到返佣
        int level = accountRebates.size();
        if (level == 0) {
            return fee;
        }
        BigDecimal totalAmount = BigDecimal.ZERO;
        // 固定取第一个为合伙人
        AccountRebate partnetAccount = accountRebates.get(0);

        RebateInfo partnerRebateInfo = partnerRebate(partnetAccount);
        // 合伙人的返佣比例
        BigDecimal partnetRebate = partnetAccount.getRebate();
        // 判断合伙人是否是普通人，普通人就拿默认的比例
        final boolean isPartnetToUser = isUser(partnerRebateInfo);
        // 合伙人要收取的服务费
        BigDecimal amount = fee.multiply(partnetRebate);

        // 如果只有一级关系，只能是合伙人或者普通邀请人
        if (level == 1) {
            updateRebateAmount(partnetAccount.getAccountId(), amount);
            totalAmount = totalAmount.add(amount);
        } else if (level == 2) { //有两级的情况下
            AccountRebate helperAaccout = accountRebates.get(1);
            // 如果合伙人成为了普通人，那么助力人直接就是默认奖励，合伙人拿到不奖励
            if (isPartnetToUser) {
                // 使用
                amount = fee.multiply(helperAaccout.getRebate());
                // 收取的服务费
                updateRebateAmount(helperAaccout.getAccountId(), amount);
                totalAmount = totalAmount.add(amount);
            } else {
                RebateInfo helperRebateInfo = helperRebate(helperAaccout);
                BigDecimal helperRebate = helperRebateInfo.getRebate();
                // 更新助力人的跟单奖励
                BigDecimal helperAmount = fee.multiply(helperRebate);
                // 合伙人拿剩下的一部分
                amount = amount.subtract(helperAmount);

                updateRebateAmount(helperAaccout.getAccountId(), helperAmount);
                totalAmount = totalAmount.add(helperAmount);

                if (amount.compareTo(BigDecimal.ZERO) != 0) {
                    updateRebateAmount(partnetAccount.getAccountId(), amount);
                    totalAmount = totalAmount.add(amount);
                }
            }
        }
        return fee.subtract(totalAmount);
    }

    /**
     * 计算会员返利
     *
     * @param accountId
     * @param fee
     * @param isBalance 是否结算
     * @return
     */
    public BigDecimal calVipRebate(Long accountId, BigDecimal fee, boolean isBalance) {
        Optional<AccountRelation> optional = accountRelationService.findByAccountId(accountId);
        if (optional.isEmpty()) {
            return fee;
        }

        List<Long> relationAccount = getRelationAccount(optional.get().getRelationTree());
        // 当前用户没有上级，那么在关系中没有人能分到返佣
        int level = relationAccount.size();
        if (level == 0) {
            return fee;
        }
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (level >= 1) {
            final Long father = level == 1 ? relationAccount.get(0) : relationAccount.get(1);
            // 父亲返利
            totalAmount = totalAmount.add(vipRebate(father, fee, getVipOneLevelRebate(), isBalance));
            if (level >= 2) {
                final Long grandpa = relationAccount.get(0);
                // 爷爷返利
                totalAmount = totalAmount.add(vipRebate(grandpa, fee, getVipTwoLevelRebate(), isBalance));
            }
        }

        return fee.subtract(totalAmount);
    }

    /**
     * 会员返利
     *
     * @param accountId  返利账号
     * @param fee        服务费
     * @param rebateRate 返利比例
     * @return
     */
    private BigDecimal vipRebate(Long accountId, BigDecimal fee, BigDecimal rebateRate, boolean isBalance) {
        // 是否vip
        final boolean isVip = vipService.isVip(accountId);
        if (!isVip) {
            return BigDecimal.ZERO;
        }
        // 返利金额
        BigDecimal amount = fee.multiply(rebateRate);
        if (isBalance) {
            updateRebateAmount(accountId, RoundTool.round(amount));
            rebateService.balanceRebate(accountId, amount);
        } else {
            rebateService.addRebate(accountId, amount);
        }
        return amount;
    }

    private boolean isUser(RebateInfo rebateInfo) {
        return rebateInfo.getRoleEnum() == RoleEnum.ROLE_USER;
    }

    private void updateRebateAmount(Long accountId, BigDecimal amount) {
        walletService.updateRebateAmount(amount, accountId);
    }

    /**
     * 合伙人的返佣
     *
     * @param accountRebate
     * @return
     */
    private RebateInfo partnerRebate(AccountRebate accountRebate) {
        return getRebate(accountRebate, RoleEnum.ROLE_PARTNER);
    }

    /**
     * 助力人的返佣
     *
     * @param accountRebate
     * @return
     */
    private RebateInfo helperRebate(AccountRebate accountRebate) {
        return getRebate(accountRebate, RoleEnum.ROLE_HELPER);
    }

    private RebateInfo getRebate(AccountRebate accountRebate, RoleEnum roleEnum) {
        RebateInfo rebateInfo = new RebateInfo();
        Optional<Account> optional = accountRepo.findById(accountRebate.getAccountId());
        if (optional.isEmpty()) {
            return rebateInfo;
        }
        // 不是参数传入的角色，返回0
        RoleEnum role = optional.get().getRole();
        if (roleEnum != role) {
            return rebateInfo;
        }
        rebateInfo.setRoleEnum(roleEnum);
        rebateInfo.setRebate(accountRebate.getRebate());
        return rebateInfo;
    }

    /**
     * 获得关系树，最高两级
     *
     * @param relationTree
     * @return 返回不可变的聚合
     */
    private List<AccountRebate> getAccountRebates(String relationTree) {
        List<Long> accountIds = getRelationAccount(relationTree);
        if (accountIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<AccountRebate> list = accountRebateRepo.findByAccountIdIn(accountIds);
//        for (Long accountId : accountIds) {
//            Optional<AccountRebate> first = list.stream().filter(v -> v.getAccountId().longValue() == accountId.longValue()).findFirst();
//            if (first.isEmpty()) {
//                AccountRebate accountRebate = new AccountRebate();
//                accountRebate.setAccountId(accountId);
//                accountRebate.setRebate(BigDecimal.ZERO);
//                accountRebate.setValid(true);
//                list.add(accountRebate);
//            }
//        }
        // 排序，保证按照 合伙人/助力人的顺序来排列的
        list.sort((v1, v2) -> accountIds.indexOf(v1.getAccountId().longValue()) - accountIds.indexOf(v2.getAccountId().longValue()));
        return Collections.unmodifiableList(list);
    }

    /**
     * 获得关系账号
     *
     * @param relationTree
     * @return
     */
    private List<Long> getRelationAccount(String relationTree) {
        if ("/".equals(relationTree)) {
            return Collections.emptyList();
        }
        String[] split = relationTree.replaceFirst("/", "").split("/");
        int length = split.length;
        List<Long> accountIds = Arrays.stream(split).map(Long::valueOf).collect(Collectors.toList()).subList(Integer.max(0, length - 2), length);
        return accountIds;
    }

    public List<AccountRebate> findByAccountIdIn(List<Long> accountIds) {
        return accountRebateRepo.findByAccountIdIn(accountIds);
    }

    @Data
    private static class RebateInfo {
        private RoleEnum roleEnum = RoleEnum.ROLE_USER;
        private BigDecimal rebate = BigDecimal.ZERO;
    }

    @Data
    private static class FollowCostInfo {
        private Long accountId;
        private Long strategyId;
        private BigDecimal followCost = BigDecimal.ZERO;
    }

}
