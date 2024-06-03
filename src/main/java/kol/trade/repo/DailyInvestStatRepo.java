package kol.trade.repo;

import kol.trade.entity.DailyInvestStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author kent
 */
@Repository
public interface DailyInvestStatRepo extends JpaRepository<DailyInvestStat, Long> {

}
