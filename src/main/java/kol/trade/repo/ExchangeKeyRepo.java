package kol.trade.repo;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import kol.trade.entity.ExchangeKey;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author guanzhenggang@gmail.com
 */
@Repository
public interface ExchangeKeyRepo extends CrudRepository<ExchangeKey, Long> {
    /**
     * 根据账号ID查询
     *
     * @param accountId
     * @return
     */
    List<ExchangeKey> findByAccountId(Long accountId);

    /**
     * 查询用户单个交易所KEY
     *
     * @param id
     * @param accountId
     * @return
     */
    Optional<ExchangeKey> findByIdAndAccountId(Long id, Long accountId);

    /**
     * 删除交易所KEY
     *
     * @param accountId
     * @param id
     */
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query(value = "delete from trade_key where id = :id and account_id = :accountId", nativeQuery = true)
    void deleteByAccountIdAndId(Long accountId, Long id);
}
