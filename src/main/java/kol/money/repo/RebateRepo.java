package kol.money.repo;

import kol.money.model.Rebate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 钱包
 *
 * @author kent
 */
@Repository
public interface RebateRepo extends JpaRepository<Rebate, Long> {
    Rebate findByAccountId(Long accountId);
}
