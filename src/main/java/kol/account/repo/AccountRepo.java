package kol.account.repo;

import java.util.List;
import java.util.Optional;

import kol.account.model.AccountStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kol.account.model.Account;
import kol.account.model.RoleEnum;

/**
 * 账户
 *
 * @author kent
 */
@Repository
public interface AccountRepo extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {

    /**
     * 根据邮箱查询账户
     *
     * @param email
     * @return
     */
    Optional<Account> findByEmail(String email);

    /**
     * 根据钱包地址查询账户
     *
     * @param walletAddr
     * @return
     */
    Optional<Account> findByTrxAddress(String trxAddress);

    /**
     * 根据TOKEN查询用户
     *
     * @param token
     * @return
     */
    Optional<Account> findByToken(String token);

    /**
     * 根据邀请码查询用户
     *
     * @param inviteCode
     * @return
     */
    Optional<Account> findByInviteCode(String inviteCode);

    @Query(nativeQuery = true, value = "select a.* from account a " +
            "left join account_relation ar on a.id = ar.account_id " +
            "where ar.p_id=:pid")
    List<Account> listNext(Long pid);

    List<Account> findByRole(RoleEnum role);

    /**
     * 根据VIP等级获取列表
     *
     * @param vipLevel
     * @return
     */
    List<Account> findByVipLevel(Integer vipLevel);

    /**
     * 获取大于等于vip等级的列表
     *
     * @param vipLevel
     * @return
     */
    List<Account> findByVipLevelGreaterThanEqual(Integer vipLevel);

    List<Account> findByStatusNot(AccountStatusEnum status);
}
