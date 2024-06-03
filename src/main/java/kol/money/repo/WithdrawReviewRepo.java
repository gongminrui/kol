package kol.money.repo;

import kol.money.model.WithdrawReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 提现审核
 *
 * @author kent
 */
@Repository
public interface WithdrawReviewRepo extends JpaRepository<WithdrawReview, Long>, JpaSpecificationExecutor<WithdrawReview> {
    int countByAccountIdAndReview(Long accountId, boolean review);
}
