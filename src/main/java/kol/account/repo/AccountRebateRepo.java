package kol.account.repo;

import kol.account.model.AccountRebate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author jacky
 */
@Repository
public interface AccountRebateRepo extends JpaRepository<AccountRebate, Long> {
    Optional<AccountRebate> findByAccountId(Long accountId);

    List<AccountRebate> findByAccountIdIn(List<Long> accountIds);
}
