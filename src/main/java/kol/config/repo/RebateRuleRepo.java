package kol.config.repo;

import kol.config.model.RebateRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * 抽佣与返佣规则
 *
 * @author kent
 */
@Repository
public interface RebateRuleRepo extends JpaRepository<RebateRule, Long> {
    /**
     * 根据类型查询同类中的最大值
     *
     * @param type
     * @return
     */
    @Query(value = "select max(rebate) from rebate_rule where type = ?1"
            , nativeQuery = true)
    BigDecimal findMaxByType(String type);

    /**
     * 根据类型查询同类中的最小值
     *
     * @param type
     * @return
     */
    @Query(value = "select min(rebate) from rebate_rule where type = ?1"
            , nativeQuery = true)
    BigDecimal findMinByType(String type);
}
