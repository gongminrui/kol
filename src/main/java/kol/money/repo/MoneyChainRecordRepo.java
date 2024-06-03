package kol.money.repo;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import kol.money.model.ChainEnum;
import kol.money.model.MoneyChainRecord;

/**
 * 充值订单repo
 *
 * @author admin
 */
@Repository
public interface MoneyChainRecordRepo extends JpaRepository<MoneyChainRecord, Long> {
    @Query("select r from MoneyChainRecord r where r.txid=:txid")
    Optional<MoneyChainRecord> findByTxid(String txid);

    /**
     * 最近处理的一笔交易时间
     *
     * @param chain
     * @return
     */
    @Query(value = "select r.created_at from money_chain_record r where r.chain=:chain order by created_at desc limit 1", nativeQuery = true)
    Optional<ZonedDateTime> findLastProcessedTransactionTime(ChainEnum chain);

}
