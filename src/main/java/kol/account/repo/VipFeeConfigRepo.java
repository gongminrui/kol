package kol.account.repo;

import kol.account.model.VipFeeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author jacky
 */
@Repository
public interface VipFeeConfigRepo extends JpaRepository<VipFeeConfig, Long> {
}
