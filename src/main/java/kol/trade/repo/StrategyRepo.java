package kol.trade.repo;

import kol.trade.entity.Strategy;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author kent
 */
@Repository
public interface StrategyRepo extends CrudRepository<Strategy, Long>, JpaSpecificationExecutor<Strategy> {
    /**
     * 根据用户与策略Id查询策略详情
     *
     * @param accountId
     * @param id
     * @return
     */
    Optional<Strategy> findByAccountIdAndId(Long accountId, Long id);

    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query(value = """
            update trade_strategy set already_follow_count = already_follow_count + ?1
            where id = ?2 and already_follow_count + ?1 <= big_follow_count and already_follow_count + ?1 >= 0
            """, nativeQuery = true)
    Integer updateAlreadyFollowCount(Integer num, Long strategyId);

    List<Strategy> findByIdIn(List<Long> ids);
}
