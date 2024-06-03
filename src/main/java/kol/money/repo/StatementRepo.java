package kol.money.repo;

import kol.money.model.Statement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 钱包
 *
 * @author kent
 */
@Repository
public interface StatementRepo extends JpaRepository<Statement, Long>, JpaSpecificationExecutor<Statement> {

    /**
     * 获得用户的待结清的结账单
     *
     * @param accountId
     * @param settle
     * @return
     */
    Optional<Statement> findByAccountIdAndSettle(Long accountId, Boolean settle);

    /**
     * 获取未结算的列表
     *
     * @param settle
     * @return
     */
    List<Statement> findBySettle(Boolean settle);
}
