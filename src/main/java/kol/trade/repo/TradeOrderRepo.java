package kol.trade.repo;

import kol.trade.entity.TradeOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author kent
 */
@Repository
public interface TradeOrderRepo extends JpaRepository<TradeOrder, Long> {
}
