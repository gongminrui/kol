package kol.money.repo;

import kol.money.model.MoneyRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * @author kent
 */
@Repository
public interface MoneyRecordRepo extends CrudRepository<MoneyRecord, Long>, JpaSpecificationExecutor<MoneyRecord> {
    /**
     * 查询用户余额
     *
     * @param accountId
     * @return
     */
    @Query(value = "select coalesce(sum(a.amount),0) from money_record a where a.account_id=:accountId", nativeQuery = true)
    BigDecimal getAccountBalance(Long accountId);

    /**
     * 分页查询钱包交易记录
     *
     * @param type
     * @param pageable
     * @return
     */
    Page<MoneyRecord> findByType(String type, Pageable pageable);

    @Query(value = "select sum(amount) from money_record where account_id = ?1 and type = ?2", nativeQuery = true)
    BigDecimal findByAccountIdAndType(Long accountId, String type);
}
