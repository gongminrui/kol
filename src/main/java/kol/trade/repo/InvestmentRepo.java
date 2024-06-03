package kol.trade.repo;

import kol.trade.entity.Investment;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface InvestmentRepo extends CrudRepository<Investment, Long>, JpaSpecificationExecutor<Investment> {

    /**
     * 查询投资人当前所有跟单资金与盈利
     *
     * @param accountId
     * @return
     */
    @Query(value = """
            select sum(balance - principal) as profit,sum(principal) as principal
            from trade_investment
            where account_id = :accountId and is_end = 0 and is_real = 1
            """, nativeQuery = true)
    List<Map<String, BigDecimal>> findInvestor(Long accountId);

    /**
     * 查询用户当前投资金额
     *
     * @param accountId
     * @return
     */
    @Query("""
            select coalesce(sum(i.principal),0) from Investment i where i.isReal = true and i.accountId=:accountId and i.isEnd=false and i.apiKeyId=:apikeyId
            """)
    BigDecimal getTotalPrincipal(Long accountId, Long apikeyId);

    /**
     * 根据策略ID、isPause(是否暂停)与isEnd(是否结束)查询
     *
     * @param strategyId
     * @param isPause
     * @param isEnd
     * @return
     */
    List<Investment> findByStrategyIdAndIsPauseAndIsEnd(Long strategyId, Boolean isPause, Boolean isEnd);

    /**
     * 根据策略ID与isEnd(是否结束)查询列表
     *
     * @param strategyId
     * @param isEnd
     * @return
     */
    List<Investment> findByStrategyIdAndIsEnd(Long strategyId, Boolean isEnd);

    /**
     * 根据accountId与isEnd查询列表
     *
     * @param isEnd
     * @param accountId
     * @return
     */
    List<Investment> findByIsEndAndAccountId(Boolean isEnd, Long accountId);

    Optional<Investment> findByIsEndAndAccountIdAndStrategyId(Boolean isEnd, Long accountId, Long strategyId);

    /**
     * 用户当前跟投列表
     *
     * @param accountId
     * @return
     */
    @Query(value = """
            select invest.id as investmentId,invest.strategyId as strategyId,invest.isPause as isPause,stra.name as strategyName,'' as position
            from Investment invest
            inner join Strategy stra on stra.id = invest.strategyId
            where invest.isEnd = false and invest.accountId = ?1 and stra.status = 'ON'
            """)
    List<Map<String, Object>> findListCurrent(Long accountId);

    @Query(value = """
            select invest.id as investmentId,invest.principal,(invest.balance - invest.principal) as profit,
            invest.created_at as createdAt,invest.end_time as endTime,stra.id as strategyId,stra.name as strategyName
            from trade_investment as invest
            inner join trade_strategy as stra on stra.id = invest.strategy_id
            where invest.is_end = 1 and invest.account_id = ?1 
            order by invest.end_time desc
            """, nativeQuery = true)
    List<Map<String, Object>> findListHistory(Long accountId);

    /**
     * 查询用户跟投详情
     *
     * @param id
     * @param accountId
     * @return
     */
    Optional<Investment> findByIdAndAccountId(Long id, Long accountId);

    /**
     * 查询合伙人下跟单资金
     *
     * @param accountIds
     * @return
     */
    @Query(value = "select sum(principal) from trade_investment where is_real = 1 and account_id in (?1)", nativeQuery = true)
    BigDecimal getTotalPrincipal(List<Long> accountIds);

    @Query(value = "select count(id) from trade_investment where strategy_id = ?1 and is_real = 1 and is_end = 0",
            nativeQuery = true)
    Integer getStrategyInvestCount(Long strategyId);

    @Query(value = "select count(id) as num,strategy_id as strategyId from trade_investment where is_end = 0 and is_real =1 group by strategy_id",
            nativeQuery = true)
    List<Map<String, Object>> getAllStrategyInvestCount();

    int countByAccountIdAndIsEndAndIsPause(Long accountId, boolean isEnd, boolean isPause);

    /**
     * 停止策略跟单
     *
     * @param investId
     * @param pauseReason
     * @return
     */
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query(value = """
            update trade_investment set is_end = 1,pause_reason = ?2, end_time = now()
            where id = ?1 and is_real = 1
            """, nativeQuery = true)
    Integer endInvestment(Long investId, String pauseReason);

    /**
     * 停止用户的所有策略跟单
     *
     * @param accountId
     * @param pauseReason
     * @return
     */
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query(value = """
            update trade_investment set is_end = 1,pause_reason = ?2, end_time = now()
            where account_id = ?1 and is_real = 1 and is_end = 0
            """, nativeQuery = true)
    Integer accountEndInvestment(Long accountId, String pauseReason);

    List<Investment> findByAccountIdIn(List<Long> investIds);

    List<Investment> findByIdIn(List<Long> ids);

    List<Investment> findByApiKeyIdAndIsEnd(Long apiKeyId, Boolean isEnd);

    List<Investment> findByIsEnd(Boolean isEnd);

}
