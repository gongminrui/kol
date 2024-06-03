package kol.trade.repo;

import kol.trade.entity.StrategySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author kent
 */
@Repository
public interface StrategySnapshotRepo extends JpaRepository<StrategySnapshot, Long>, JpaSpecificationExecutor<StrategySnapshot> {

    @Query(value = """
            select profit_rate from (
                select profit_rate,created_at from strategy_snapshot 
                where strategy_id = ?1
                order by created_at desc limit ?2) as temp
            order by created_at asc 
            """, nativeQuery = true)
    List<BigDecimal> findByStrategyId(Long strategyId, Integer limitNum);
}
