package kol.trade.repo;

import kol.trade.dto.vo.PositionSumProfit;
import kol.trade.dto.vo.StrategyStatistics;
import kol.trade.entity.Position;
import kol.trade.enums.PositionSideEnum;
import kol.trade.enums.PositionStatusEnum;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface PositionRepo extends CrudRepository<Position, Long>, JpaSpecificationExecutor<Position> {


    /**
     * 查询策略持仓信息双向
     *
     * @param investmentId
     * @param strategyId
     * @return
     */
    @Query(value = """
            select p from Position p 
            where p.status = 'OPEN' 
            and p.investmentId = :investmentId 
            and p.strategyId = :strategyId 
            """)
    List<Position> findPositionList(Long investmentId, Long strategyId);

    /**
     * 查询跟投N天每天盈利
     *
     * @param investmentId
     * @param startDate
     * @param endDate
     * @return
     */
    @Query(value = """
            select sum(profit) as profit,DATE_FORMAT(created_at,'%Y-%m-%d') as createdAt from trade_position
            where investment_id = ?1 and DATE_FORMAT(created_at,'%Y-%m-%d') between ?2 and ?3
            group by DATE_FORMAT(created_at,'%Y-%m-%d')
            """, nativeQuery = true)
    List<Map<String, Object>> findCalPositionProfitByDay(Long investmentId, String startDate, String endDate);

    @Query(value = """
            select sum(profit) as profit,count(id) as total,DATE_FORMAT(created_at,'%Y-%m-%d') as createdAt from trade_position
            where account_id = ?1 and DATE_FORMAT(created_at,'%Y-%m-%d') = ?2 and status = 'CLOSE'
            group by createdAt
            """, nativeQuery = true)
    Map<String, Object> findCalTodayPosition(Long accountId, String nowDate);

    List<Position> findByAccountIdAndStatus(Long accountId, PositionStatusEnum status);


    @Query(value = """
            select  p from Position p
            where p.status = :status 
            and p.strategyId = :strategyId
            and p.positionSide = :positionSide
            and p.symbol = :symbol
            """)
    List<Position> findStrategyPosition(Long strategyId, PositionStatusEnum status, PositionSideEnum positionSide, String symbol);

    List<Position> findByExitTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Position> findByAccountIdAndExitTimeBetween(Long accountId, LocalDateTime startDate, LocalDateTime endDate);

    List<Position> findByStatus(PositionStatusEnum status);

    @Query(nativeQuery = true, value = "select * from trade_position where strategy_id = :strategyId order by updated_at desc limit 1")
    Position findLast(Long strategyId);

    @Query(nativeQuery = true, value = "SELECT a.`strategy_id` strategyId, " +
            "SUM(IF(a.`profit` >= 0, a.`profit`, 0)) AS win, " +
            "SUM(IF(a.`profit` >= 0, 1, 0)) AS winCount," +
            "SUM(IF(a.`profit` < 0, a.`profit`, 0)) AS loss, " +
            "SUM(IF(a.`profit` < 0, 1, 0)) AS lossCount" +
            " FROM `trade_position` a WHERE a.strategy_id IN (:strategyIds) GROUP BY a.`strategy_id`")
    List<StrategyStatistics> listByStrategyStatistics(List<Long> strategyIds);

    @Query(nativeQuery = true, value = "SELECT " +
            "a.`account_id` accountId, SUM(a.`profit`) profit, count(1) totalCount, " +
            "SUM(IF(a.`profit` > 0, 1, 0)) winCount " +
            "FROM `trade_position` a " +
            "WHERE a.is_real = 1 and a.is_settlement=false and a.exit_time is not null and " +
            "a.exit_time BETWEEN :startDate AND :endDate GROUP BY a.`account_id`")
    List<PositionSumProfit> listPositionSumProfit(LocalDateTime startDate, LocalDateTime endDate);

    List<Position> findByInvestmentId(Long investmentId);

    Optional<Position> findByAccountIdAndId(Long accountId, Long id);

    /**
     * 获得第一个未结算的开平仓记录
     *
     * @return
     */
    @Query(nativeQuery = true, value = "select * from trade_position where is_settlement=false " +
            "and is_real=1 and exit_time is not null order by created_at limit 1")
    Optional<Position> findFirstNotSettlement();
}
