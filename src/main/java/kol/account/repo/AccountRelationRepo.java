package kol.account.repo;

import kol.account.dto.vo.AccountRebateVo;
import kol.account.model.Account;
import kol.account.model.AccountRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author jacky
 */
@Repository
public interface AccountRelationRepo extends JpaRepository<AccountRelation, Long> {
    Optional<AccountRelation> findByAccountId(Long accountId);

    @Query(nativeQuery = true, value = "select a2.id, a2.email, a2.head_img headImg, a2.role, ar.rebate, a2.invite_code inviteCode," +
            "wa.rebate_amount rebateAmount, wa.follow_cost_amount followCostAmount, a2.vip_level vipLevel, a2.vip_count vipCount, " +
            "(select count(1) from account_relation arr where arr.p_id = a.account_id) as inviteCount" +
            " from account_relation a " +
            "left join account a2 on a2.id = a.account_id " +
            "left join account_rebate ar on a.account_id = ar.account_id " +
            "left join wallet wa on a.account_id = wa.account_id " +
            "where a.p_id=:pid and a2.email like concat('%',:email,'%')")
    List<AccountRebateVo> listByPid(Long pid, String email);

    @Query(nativeQuery = true, value = "select count(1) from account_relation a where a.p_id=:pid")
    int countByPId(Long pid);

    @Query(nativeQuery = true, value = "select a2.* from account_relation a " +
            "left join account a2 on a2.id = a.account_id " +
            "where a.p_id=:pid")
    List<Account> listNext(Long pid);
}
