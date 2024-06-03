package kol.account.repo;

import kol.account.model.AccountRebateRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author kent
 */
@Repository
public interface AccountRebateRelationRepo extends JpaRepository<AccountRebateRelation, Long> {
    /**
     * 查询账户返佣关系
     *
     * @param accountId
     * @return
     */
    List<AccountRebateRelation> findByAccountId(Long accountId);

//    /**
//     * 查询多个客服
//     * @param accountList
//     * @return
//     */
//    @Query(value = "",nativeQuery = true)
//    List<AccountRebateRelation> findByAccountIdIn(Long accountList);

//    /**
//     * 更新用户返佣比例
//     *
//     * @param accountId
//     * @param rebate
//     * @return
//     */
//    Integer updateRebateByAccount(Long accountId, BigDecimal rebate);

}
