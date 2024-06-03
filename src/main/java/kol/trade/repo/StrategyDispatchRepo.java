package kol.trade.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import kol.trade.entity.StrategyDispatch;
@Repository
public interface StrategyDispatchRepo extends JpaRepository<StrategyDispatch, Long> ,CrudRepository<StrategyDispatch, Long>, JpaSpecificationExecutor<StrategyDispatch>{

	@Query(value =  "select * from strategy_dispatch where strategy_id=?1",nativeQuery = true)
	List<StrategyDispatch> findByStrategyId( Long strategyId);
}
