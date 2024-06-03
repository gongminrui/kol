package kol.account.service;

import java.math.BigDecimal;
import java.util.*;

import javax.annotation.Resource;

import kol.account.model.*;
import kol.common.utils.TimeRangeUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.tron.trident.core.key.KeyPair;

import com.google.common.eventbus.AsyncEventBus;

import kol.account.dto.cmd.AddAccountCmd;
import kol.account.dto.cmd.RegisterCmd;
import kol.account.repo.AccountInfoRepo;
import kol.account.repo.AccountRebateRelationRepo;
import kol.account.repo.AccountRepo;
import kol.common.config.GlobalConfig;
import kol.common.model.ErrorMsg;
import kol.common.model.PasswordConfig;
import kol.common.utils.AESUtils;
import kol.config.model.Config;
import kol.config.service.ConfigService;
import kol.money.model.MoneyRecord;
import kol.money.model.Wallet;
import kol.money.repo.MoneyRecordRepo;
import kol.money.repo.WalletRepo;
import kol.vcode.model.VcodeService;

/**
 * 注册服务
 *
 * @author guanzhenggang@gmail.com
 */
@Service
public class RegisterService {

    final AccountRepo accountRepo;
    final VcodeService vcodeService;
    final PasswordEncoder passwordEncoder;
    final MoneyRecordRepo moneyRecordRepo;
    final AccountInfoRepo accountInfoRepo;
    final WalletRepo walletRepo;
    final AccountRebateRelationRepo accRelationRepo;

    @Resource
    private AccountRelationService accountRelationService;
    @Resource
    private AccountRebateService accountRebateService;
    @Resource
    private ConfigService configService;
    @Autowired
    PasswordConfig passwordConfig;
    @Autowired
    private GlobalConfig globarConfig;
    @Autowired
    private AsyncEventBus eventBus;

    public RegisterService(AccountRepo accountRepo, VcodeService vcodeService, PasswordEncoder passwordEncoder,
                           MoneyRecordRepo moneyRecordRepo, AccountInfoRepo accLevelRepo, WalletRepo walletRepo,
                           AccountRebateRelationRepo accRelationRepo) {
        this.accountRepo = accountRepo;
        this.vcodeService = vcodeService;
        this.passwordEncoder = passwordEncoder;
        this.moneyRecordRepo = moneyRecordRepo;
        this.accountInfoRepo = accLevelRepo;
        this.walletRepo = walletRepo;
        this.accRelationRepo = accRelationRepo;
    }

    /**
     * 新用户注册
     *
     * @param cmd
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public void register(RegisterCmd cmd) {
//        boolean result = vcodeService.verify(cmd.getEmail(), cmd.getVcode());
        Assert.isTrue("0yMGnV8B".equals(cmd.getInviteCode()), "邀请码错误");
        Assert.isTrue("131415".equals(cmd.getVcode()), "验证码错误");
        AddAccountCmd addCmd = new AddAccountCmd();
        BeanUtils.copyProperties(cmd, addCmd);
        this.save(addCmd, false);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public void add(AddAccountCmd cmd) {
        Long loginAccId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Account> optional = accountRepo.findById(loginAccId);
        boolean result = RoleEnum.ROLE_ADMIN.equals(optional.get().getRole());
        Assert.isTrue(result, "没有添加账户权限");
        this.save(cmd, true);
    }

    /**
     * 添加账号
     *
     * @param cmd
     * @param isManager 是否管理后台
     */
    // 事务不生效
    // 注意：类内部访问，即并非直接访问带@Transactional注解的方法，而是通过内部普通方法来调用带事务注解的方法；
//    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public void save(AddAccountCmd cmd, boolean isManager) {
        boolean result = accountRepo.findByEmail(cmd.getEmail()).isEmpty();
        Assert.isTrue(result, ErrorMsg.EMAIL_EXIST.getMsg());

        Long pid = null;
        Account inviter = null;
//        if (StringUtils.isNotBlank(cmd.getInviteCode())) {
//            Optional<Account> inviterOptional = accountRepo.findByInviteCode(cmd.getInviteCode());
//            Assert.isTrue(inviterOptional.isPresent(), ErrorMsg.INVITE_CODE_ERROR.getMsg());
//            inviter = inviterOptional.get();
//            pid = inviter.getId();
//        }

        Account account = new Account();
        BeanUtils.copyProperties(cmd, account);
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        account.setInviteCode(getInviteCode());
        account.setStatus(AccountStatusEnum.NORMAL);

        RoleEnum roleEnum = account.getRole() == null ? RoleEnum.ROLE_USER : cmd.getRole();
        account.setRole(roleEnum);
        // 生成trx地址
        KeyPair keyPair = KeyPair.generate();
        account.setTrxAddress(keyPair.toBase58CheckAddress());
        account.setVipCount(0);
        account.setVipLevel(0);

        account.setTrxPrivate(AESUtils.encrypt(keyPair.toPrivateKey(), passwordConfig.dataPassword));

        if (!isManager) {
            // 赠送vip
            giveVip(account);
        }

        account = accountRepo.save(account);
        saveWallet(account.getId());
        saveAccountInfo(account.getId(), inviter);

//        saveAccountRebateRelation(account.getId(), inviter);

        // 建立关系
        accountRelationService.createRelation(account.getId(), pid);
        // 保存邀请人返佣
        accountRebateService.saveDefaultRebate(account.getId());

        eventBus.post(account);
    }

    /**
     * 赠送vip
     *
     * @param account
     */
    private void giveVip(Account account) {
        int giveDay = configService.getRegisterGiveVipDay();
        if (giveDay == 0) {
            return;
        }
        // 赠送的vip不累加vipcount次数
        account.setVipLevel(1);
        Date date = TimeRangeUtil.addDay(new Date(), giveDay);
        date = TimeRangeUtil.setStartTime(date);
        account.setVipExpired(date);
    }

    /**
     * 保存用户详情
     *
     * @param accountId
     * @param inviter
     */
    protected void saveAccountInfo(Long accountId, Account inviter) {
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setAccountId(accountId);

        if (Objects.nonNull(inviter)) {
            accountInfo.setInviteCode(inviter.getInviteCode());
            accountInfo.setInviterId(inviter.getId());
            Optional<AccountInfo> accInfoOptional = accountInfoRepo.findByAccountId(inviter.getId());

            if (RoleEnum.ROLE_PARTNER.equals(inviter.getRole())) {
                accountInfo.setPartnerId(inviter.getId());
            } else if (RoleEnum.ROLE_HELPER.equals(inviter.getRole())) {
                Assert.isTrue(accInfoOptional.isPresent(), "代理账号数据有问题！");
                accountInfo.setPartnerId(accInfoOptional.get().getPartnerId());
                accountInfo.setHelperId(inviter.getId());
            }
        }

        accountInfo.setFollowCost(configService.getFollowCostRatio());
        BigDecimal inviteRebate = new BigDecimal(configService.getValue(Config.KeyEnum.INVITE_REBATE));
        accountInfo.setInviteRebate(inviteRebate);
        accountInfoRepo.save(accountInfo);
    }

    /**
     * 保存用户跟单手续费返佣关系
     *
     * @param accountId
     */
    protected void saveAccountRebateRelation(Long accountId, Account inviter) {
        if (Objects.nonNull(inviter)) {
            List<AccountRebateRelation> relationList = new ArrayList<>();
            if (RoleEnum.ROLE_HELPER.equals(inviter.getRole())) {
                relationList = accRelationRepo.findByAccountId(inviter.getId());
                relationList.parallelStream().forEach(f -> {
                    f.setAccountId(accountId);
                    f.setId(null);
                    f.setCreatedAt(null);
                    f.setUpdatedAt(null);
                });
            }

            Optional<AccountInfo> inviteOptional = accountInfoRepo.findByAccountId(inviter.getId());
            AccountRebateRelation rebateRelation = new AccountRebateRelation();
            rebateRelation.setAccountId(accountId);
            rebateRelation.setRebateId(inviter.getId());
            rebateRelation.setRebate(inviteOptional.get().getInviteRebate());
            rebateRelation.setSerialNum(System.currentTimeMillis());
            relationList.add(rebateRelation);
            accRelationRepo.saveAll(relationList);
        }
    }

    /**
     * 更新钱包与记录信息
     *
     * @param accountId
     */
    protected void saveWallet(Long accountId) {
        BigDecimal giveAmount = new BigDecimal(configService.getValue(Config.KeyEnum.NEW_ACCOUNT_DONATE));
        if (giveAmount.compareTo(BigDecimal.ZERO) == 1) {
            // 赠送金额纪录流水
            MoneyRecord moneyRecord = new MoneyRecord().setAccountId(accountId).setAmount(giveAmount)
                    .setComment("新用户注册赠送资金").setType(MoneyRecord.Type.NEW_USER_DONATE);
            moneyRecordRepo.save(moneyRecord);
        }

        Wallet wallet = new Wallet();
        wallet.setAccountId(accountId);
        wallet.setBalance(giveAmount);
        wallet.setGiveAmount(giveAmount);
        walletRepo.save(wallet);
    }

    /**
     * 获取邀请码
     *
     * @return
     */
    protected String getInviteCode() {
        String inviteCode = RandomStringUtils.randomAlphanumeric(8);
        Optional<Account> optional = accountRepo.findByInviteCode(inviteCode);
        if (optional.isPresent()) {
            inviteCode = getInviteCode();
        }
        return inviteCode;
    }
}
