package kol.money.service;

import kol.common.service.BaseService;
import kol.money.model.MoneyRecord;
import kol.money.model.Wallet;
import kol.money.repo.WalletRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * @author kent
 */
@Service
@Slf4j
public class WalletService extends BaseService {
    @Resource
    private WalletRepo walletRepo;
    @Resource
    private MoneyService moneyService;

    /**
     * 查询用户钱包详情
     *
     * @param accountId
     * @return
     */
    public Optional<Wallet> findByAccountId(Long accountId) {
        return walletRepo.findByAccountId(accountId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer updateRebateAmount(BigDecimal amount, Long accountId) {
        // 避免多线程修改，加上锁
        synchronized (accountId.toString().intern()) {
            Integer res = walletRepo.updateRebateAmount(amount, accountId);
            if (res > 0) {
                BigDecimal balance = getBalance(accountId);
                log.info("{} 账号累加返佣金额 {}, 当前余额 {}", accountId, amount, balance);
                moneyService.createMoneyRecord(accountId, MoneyRecord.Type.INTRODUCE_REWORD, amount, "更新返利");
            } else {
                log.error("{} 账号累加返佣金额 {} 失败", accountId, amount);
            }

            return res;
        }
    }

    /**
     * 提现
     */
    @Transactional(rollbackFor = Exception.class)
    public void cashOut(Long accountId, BigDecimal cashOutAmount) {
        synchronized (accountId.toString().intern()) {
            BigDecimal balance = getBalance(accountId);
            Integer res = walletRepo.updateCashOutAmount(cashOutAmount, balance, accountId);
            if (res > 0) {
                log.info("{} 账号提现金额 {}， 当前余额", accountId, cashOutAmount);
                moneyService.createMoneyRecord(accountId, MoneyRecord.Type.CHAIN_CASH_OUT, cashOutAmount.multiply(BigDecimal.valueOf(-1)), "提现");
            } else {
                log.error("{} 账号提现金额 {} 失败", accountId, cashOutAmount);
            }

        }
    }

    /**
     * 修改跟单服务费
     *
     * @param amount
     * @param accountId
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateFollowCostAmount(BigDecimal amount, Long accountId) {
        synchronized (accountId.toString().intern()) {
            Optional<Wallet> optional = walletRepo.findByAccountId(accountId);
            if (optional.isEmpty()) {
                log.error("账号：{} 钱包数据异常", accountId);
                return;
            }
            Wallet wallet = optional.get();
            BigDecimal balance = wallet.getBalance();
            BigDecimal giveAmount = wallet.getGiveAmount();
            BigDecimal followCostAmount = wallet.getFollowCostAmount();

            wallet.setFollowCostAmount(followCostAmount.add(amount));
            wallet.setBalance(balance.subtract(amount));
            if (amount.compareTo(BigDecimal.ZERO) == 1) {
                wallet.setGiveAmount(giveAmount.subtract(amount));
            }

            if (wallet.getGiveAmount().compareTo(BigDecimal.ZERO) < 0) {
                wallet.setGiveAmount(BigDecimal.ZERO);
            }

            walletRepo.save(wallet);
        }
    }

    /**
     * 开通会员扣费
     *
     * @param accountId
     * @param amount
     */
    @Transactional
    public int openVip(Long accountId, BigDecimal amount) {
        synchronized (accountId.toString().intern()) {
            Optional<Wallet> walletOptional = walletRepo.findByAccountId(accountId);
            if (walletOptional.isEmpty()) {
                return -1;
            }
            moneyService.createMoneyRecord(accountId, MoneyRecord.Type.VIP_FEE, amount.multiply(BigDecimal.valueOf(-1)), "续费会员");
            Wallet wallet = walletOptional.get();
            wallet.setBalance(wallet.getBalance().subtract(amount));
            walletRepo.save(wallet);
            return 1;
        }
    }

    /**
     * 结算
     *
     * @param accountId
     * @param amount
     */
    @Transactional(rollbackFor = Exception.class)
    public void settle(Long accountId, BigDecimal amount) {
        synchronized (accountId.toString().intern()) {
            Optional<Wallet> optional = walletRepo.findByAccountId(accountId);
            if (optional.isEmpty()) {
                return;
            }
            Wallet wallet = optional.get();
            BigDecimal balance = wallet.getBalance();
            // 减掉余额
            wallet.setBalance(balance.subtract(amount));
            walletRepo.save(wallet);

            moneyService.createMoneyRecord(accountId, MoneyRecord.Type.STATEMENT_SETTLE, amount.multiply(BigDecimal.valueOf(-1)), "账单结算扣费");
        }
    }

    /**
     * 获得当前登录用户可提现金额
     *
     * @return
     */
    public BigDecimal getWithdrawAmount() {
        Long currentAccountId = getCurrentAccountId();

        Optional<Wallet> optional = walletRepo.findByAccountId(currentAccountId);
        if (optional.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Wallet wallet = optional.get();
        return wallet.getBalance().subtract(wallet.getGiveAmount());
    }


    /**
     * 获得当前登陆人的可用余额（不包含赠送金额）
     *
     * @return
     */
    public BigDecimal getLoginUsableBalance() {
        return getUsableBalance(getCurrentAccountId());
    }

    /**
     * 获得可提现的返佣
     *
     * @return
     */
    public BigDecimal getCanWithdrawRebate() {
        Long accountId = getCurrentAccountId();
        Optional<Wallet> optional = walletRepo.findByAccountId(accountId);
        if (optional.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Wallet wallet = optional.get();
        return wallet.getBalance().subtract(wallet.getGiveAmount()).subtract(wallet.getRechargeAmount());
    }

    /**
     * 获得可用的余额（不包含赠送金额）
     *
     * @param accountId
     * @return
     */
    public BigDecimal getUsableBalance(Long accountId) {
        Optional<Wallet> optional = walletRepo.findByAccountId(accountId);
        if (optional.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Wallet wallet = optional.get();
        return wallet.getBalance().subtract(wallet.getGiveAmount());
    }

    public BigDecimal getBalance(Long accountId) {
        Optional<Wallet> optional = walletRepo.findByAccountId(accountId);
        if (optional.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return optional.get().getBalance();
    }
}
