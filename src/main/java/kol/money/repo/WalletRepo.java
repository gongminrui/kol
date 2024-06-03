package kol.money.repo;

import kol.money.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 钱包
 *
 * @author kent
 */
@Repository
public interface WalletRepo extends JpaRepository<Wallet, Long> {
    /**
     * 更新充值金额及余额
     *
     * @param amount
     * @param accountId
     */
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query(value = """
            update wallet set recharge_amount = recharge_amount + ?1,balance = balance + ?1
            where account_id = ?2
            """, nativeQuery = true)
    Integer updateRechargeAmount(BigDecimal amount, Long accountId);

    /**
     * 更新赠送金额
     *
     * @param amount
     * @param accountId
     * @return
     */
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query(value = """
            update wallet set give_amount = give_amount + ?1,balance = balance + ?1
            where account_id = ?2
            """, nativeQuery = true)
    Integer updateGiveAmount(BigDecimal amount, Long accountId);

    /**
     * 更新返佣金额及余额
     *
     * @param amount
     * @param accountId
     * @return
     */
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query(value = """
            update wallet set rebate_amount = rebate_amount + ?1,balance = balance + ?1
            where account_id = ?2
            """, nativeQuery = true)
    Integer updateRebateAmount(BigDecimal amount, Long accountId);

    /**
     * 更新提现金额及余额
     *
     * @param amount
     * @param oldBalance
     * @param accountId
     * @return
     */
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query(value = """
            update wallet set cash_out_amount = cash_out_amount + ?1,balance = balance - ?1
            where account_id = ?3 and balance = ?2
            """, nativeQuery = true)
    Integer updateCashOutAmount(BigDecimal amount, BigDecimal oldBalance, Long accountId);

    /**
     * 更新跟单服务费用
     *
     * @param amount
     * @param accountId
     * @return
     */
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query(value = """
            update wallet set follow_cost_amount = follow_cost_amount + ?1,balance = balance - ?1
            where account_id = ?2
            """, nativeQuery = true)
    Integer updateFollowCostAmount(BigDecimal amount, Long accountId);

    /**
     * 查询用户钱包详情
     *
     * @param accountId
     * @return
     */
    Optional<Wallet> findByAccountId(Long accountId);

    List<Wallet> findByAccountIdIn(List<Long> accountIds);
}
