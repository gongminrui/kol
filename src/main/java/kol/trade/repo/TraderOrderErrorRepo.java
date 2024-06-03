package kol.trade.repo;

import kol.trade.entity.TraderOrderError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * @author kent
 */
@Repository
public interface TraderOrderErrorRepo extends JpaRepository<TraderOrderError, Long>, JpaSpecificationExecutor<TraderOrderError> {
}
