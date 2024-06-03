package kol.vip.service;

import com.google.common.eventbus.AsyncEventBus;
import kol.account.model.Account;
import kol.account.model.RoleEnum;
import kol.account.model.VipFeeConfig;
import kol.account.service.AccountRelationService;
import kol.account.service.AccountService;
import kol.account.service.VipFeeConfigService;
import kol.common.cache.GlobalCache;
import kol.common.config.GlobalConfig;
import kol.common.model.MessageType;
import kol.common.service.BaseService;
import kol.common.utils.EmailTool;
import kol.common.utils.StringUtil;
import kol.common.utils.TimeRangeUtil;
import kol.config.service.ConfigService;
import kol.money.repo.WalletRepo;
import kol.money.service.MoneyService;
import kol.money.service.StatementService;
import kol.money.service.WalletService;
import kol.trade.repo.InvestmentRepo;
import kol.trade.service.InvestmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Configuration
@EnableScheduling
public class VipService extends BaseService {
    /**
     * VIP不处理的角色
     */
    private static final List<RoleEnum> NOT_HANDLE_ROLE = Arrays.asList(RoleEnum.ROLE_ADMIN, RoleEnum.ROLE_TRADER);
    @Autowired
    private AccountService accountService;
    @Autowired
    private ConfigService configService;
    @Autowired
    private MoneyService moneyService;
    @Autowired
    EmailTool emailTool;
    @Autowired
    private InvestmentRepo investmentRepo;
    @Autowired
    private InvestmentService investmentService;
    @Autowired
    private WalletRepo walletRepo;

    @Autowired
    private GlobalConfig config;
    @Autowired
    GlobalCache cache;
    @Autowired
    AsyncEventBus eventBus;
    @Autowired
    private AccountRelationService accountRelationService;
    @Autowired
    private VipFeeConfigService vipFeeConfigService;
    @Autowired
    private StatementService statementService;
    @Autowired
    private WalletService walletService;

    /**
     * 获得登录用户的会员费
     *
     * @return
     */
    public BigDecimal getCurrentLoginVipFee() {
        Long currentAccountId = getCurrentAccountId();
        return getDiscountVipFee(getVipFee(), currentAccountId);
    }

    /**
     * 开通会员
     */
    @Transactional
    public void openVip() {
        synchronized (getCurrentAccountId().toString().intern()) {
            Account account = getLoginAccount();
            Assert.isTrue(account.getVipLevel() == 0, "已经是会员了");

            BigDecimal balance = walletService.getUsableBalance(account.getId());
            BigDecimal fee = getCurrentLoginVipFee();

            if (fee.compareTo(BigDecimal.ZERO) > 0) {
                Assert.isTrue(balance.compareTo(fee) >= 0, "开通会员失败，余额不足");
            }

            vipDeduction(account, fee);
        }
    }

    private BigDecimal getVipFee() {
        Calendar calendar = Calendar.getInstance();
        int actualMaximum = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        double rate = (actualMaximum - calendar.get(Calendar.DAY_OF_MONTH) + 1) * 1.0 / actualMaximum;
        return configService.getVipFee().multiply(BigDecimal.valueOf(rate)).setScale(0, RoundingMode.DOWN);
    }

    private void vipDeduction(Account account, BigDecimal fee) {
        // vip扣费
        account.setVipLevel(1);
        account.setVipCount(account.getVipCount() + 1);
        accountService.openVip(account.getId());

        if (fee.compareTo(BigDecimal.ZERO) > 0) {
            walletService.openVip(account.getId(), fee);
        }
        log.info("会员缴费 账户ID {}", account.getId());
    }

    /**
     * 是否VIP
     *
     * @param accountId
     * @return
     */
    public boolean isVip(Long accountId) {
        if (!config.getEnableVip()) {
            return false;
        }
        Optional<Account> optionalAccount = accountService.getById(accountId);
        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();
            return account.getVipLevel() != null && account.getVipLevel() > 0;
        }
        return false;
    }

    /**
     * 每5分钟检测新会员扣费
     */
//    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void join() {
        if (!config.getEnableVip() || cache.accounts == null) {
            return;
        }
        final BigDecimal vipFee = configService.getVipFee();
        cache.accounts.forEach(account -> {
            int vipLevel = account.getVipLevel() == null ? 0 : account.getVipLevel();
            BigDecimal balance = walletService.getUsableBalance(account.getId());
            Calendar calendar = Calendar.getInstance();
            double rate = (calendar.getActualMaximum(Calendar.DAY_OF_MONTH) - calendar.get(Calendar.DAY_OF_MONTH)) * 1.0
                    / calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            BigDecimal fee = vipFee.multiply(BigDecimal.valueOf(rate)).setScale(0, RoundingMode.DOWN);
            if (vipLevel == 0 && balance.compareTo(fee) >= 0) {
                // vip扣费
                vipDeduction(account, fee);
                log.info("会员缴费 账户ID {}", account.getId());
                eventBus.post(account);
            }

        });

    }

    /**
     * 每月1号定时扣会费
     */
//    @Scheduled(cron = "0 0 0 1 * *")
    @Transactional
    public void expire() {
//        statementService.handleNotSettleStatement();

        if (!config.getEnableVip()) {
            return;
        }
        final BigDecimal vipFee = configService.getVipFee();
        accountService.findAll().stream().filter(acc -> !NOT_HANDLE_ROLE.contains(acc.getRole()) &&
                        acc.getVipLevel() != null && acc.getVipLevel() > 0)
                .forEach(acc -> {
                    BigDecimal balance = walletService.getUsableBalance(acc.getId());
                    BigDecimal accountVipFee = getDiscountVipFee(vipFee, acc.getId());
                    if (balance.compareTo(accountVipFee) < 0) {
                        accountService.stopVip(acc.getId());
                        investmentRepo.findByIsEndAndAccountId(false, acc.getId()).forEach(i -> {
                            investmentService.stopInvestment(i);
                        });
                        emailTool.sendMessage(acc.getEmail(), "会员到期通知", "您的会员资格已到期,到期后所有已跟单策略将自动停止，如需继续跟单请保持账户余额充足，每月1号将自动会员进行续费。");

                        sendMessage(acc.getId(), MessageType.VIP_EXPIRE_TITLE, MessageType.VIP_EXPIRE_CONTENT);
                        log.info("会员到期 账户ID {}", acc.getId());
                    } else {
                        vipDeduction(acc, accountVipFee);
                    }
                });
    }

    /**
     * 每月25号提醒充值
     */
//    @Scheduled(cron = "0 0 0 25 * ?")
    @Transactional
    public void notice() {
//        statementService.generateStatement();

        if (!config.getEnableVip()) {
            return;
        }
        final BigDecimal vipFee = configService.getVipFee();
        accountService.findAll().stream().filter(acc -> !NOT_HANDLE_ROLE.contains(acc.getRole()) &&
                        acc.getVipLevel() != null && acc.getVipLevel() > 0)
                .forEach(acc -> {
                    BigDecimal balance = walletService.getUsableBalance(acc.getId());
                    BigDecimal accountVipFee = getDiscountVipFee(vipFee, acc.getId());
                    if (balance.compareTo(accountVipFee) < 0) {
                        emailTool.sendMessage(acc.getEmail(), "会员即将到期提醒", "您的会员资格即将到期，请保证账户余额充足，每月1号将自动会员续费。");
                        sendMessage(acc.getId(), MessageType.VIP_RENEW_TITLE, MessageType.VIP_RENEW_CONTENT);
                        log.info("会员即将到期 账户ID {}", acc.getId());
                    }
                });
    }


    /**
     * 每天0点检查赠送会员
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void checkGiveVip() {
        if (!config.getEnableVip()) {
            return;
        }
        final BigDecimal vipFee = configService.getVipFee();
        final int vipExpireWarnDay = configService.getVipExpireWarnDay();
        List<Account> accounts = accountService.getVipList().stream()
                .filter(v -> !NOT_HANDLE_ROLE.contains(v)).collect(Collectors.toList());
        Date date = Calendar.getInstance().getTime();
        for (Account account : accounts) {
            int differentDay = TimeRangeUtil.getDifferentDay(date, account.getVipExpired());
            // 到期了
            if (differentDay <= 0) {
                BigDecimal balance = walletService.getUsableBalance(account.getId());
                BigDecimal accountVipFee = getDiscountVipFee(vipFee, account.getId());
                if (balance.compareTo(accountVipFee) < 0) {
                    accountService.stopVip(account.getId());
                    investmentService.stopAccountAllInvestment(account.getId(), "会员到期");
                    emailTool.sendMessage(account.getEmail(), "会员到期通知", "您的会员资格已到期,到期后所有已跟单策略将自动停止，如需继续跟单请保持账户余额充足，每月1号将自动会员进行续费。");

                    sendMessage(account.getId(), MessageType.VIP_EXPIRE_TITLE, MessageType.VIP_EXPIRE_CONTENT);
                    log.info("会员到期 账户ID {}", account.getId());
                } else {
                    vipDeduction(account, accountVipFee);
                }
            } else if (differentDay == vipExpireWarnDay) { // 提前3天提示用户会员快到期了
                List<Object> params = Arrays.asList(TimeRangeUtil.format(account.getVipExpired()),
                        getDiscountVipFee(vipFee, account.getId()));
                String msg = StringUtil.replace(MessageType.VIP_RENEW_CONTENT.getZh(), params);
                emailTool.sendMessage(account.getEmail(), "会员即将到期提醒", msg);
                sendMessage(account.getId(), MessageType.VIP_RENEW_TITLE, MessageType.VIP_RENEW_CONTENT, params);
            }
        }
    }

    /**
     * 获得折扣后的vip费
     *
     * @param vipFee
     * @param accountId
     * @return
     */
    public BigDecimal getDiscountVipFee(BigDecimal vipFee, Long accountId) {
        int nextVipCount = accountRelationService.getNextVipCount(accountId);
        List<VipFeeConfig> vipFeeConfigs = vipFeeConfigService.listAll();

        BigDecimal discount = BigDecimal.valueOf(1);
        for (VipFeeConfig vipFeeConfig : vipFeeConfigs) {
            if (nextVipCount >= vipFeeConfig.getNextCount()) {
                discount = vipFeeConfig.getDiscount();
                break;
            }
        }

        return vipFee.multiply(discount);
    }

}
