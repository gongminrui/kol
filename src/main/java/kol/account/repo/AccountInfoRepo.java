package kol.account.repo;

import kol.account.model.AccountInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author kent
 */
@Repository
public interface AccountInfoRepo extends JpaRepository<AccountInfo, Long> {

    /**
     * 查询用户层级关系
     *
     * @param accountId
     * @return
     */
    Optional<AccountInfo> findByAccountId(Long accountId);

    /**
     * 代理合伙人查询邀请人数
     *
     * @param partnerId
     * @return
     */
    @Query(value = "select account_id from account_info where partner_id = ?1", nativeQuery = true)
    List<Long> findByPartnerId(Long partnerId);

    /**
     * 代理助力人查询邀请人数
     *
     * @param helperId
     * @return
     */
    @Query(value = "select account_id from account_info where helper_id = ?1", nativeQuery = true)
    List<Long> findByHelperId(Long helperId);

    /**
     * 查询账户下级成员
     *
     * @param accountId
     * @return
     */
    @Query(value = "select acc from AccountInfo acc where acc.partnerId = ?1 or acc.helperId = ?1 or acc.inviterId = ?1")
    List<AccountInfo> findInviteAccount(Long accountId);

//    /**
//     * 查询多个账号详情信息
//     *
//     * @param accountList
//     * @return
//     */
//    List<AccountInfo> findByAccountIdIn(List<Long> accountList);
}
