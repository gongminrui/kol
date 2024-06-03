package kol.trade.repo;

import kol.trade.entity.InvestSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author kent
 */
@Repository
public interface InvestSnapshotRepo extends JpaRepository<InvestSnapshot, Long> {
}
