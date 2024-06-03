package kol.money.service;

import kol.common.service.BaseService;
import kol.money.model.Rebate;
import kol.money.repo.RebateRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Optional;

@Service
@Slf4j
public class RebateService extends BaseService {
    @Resource
    private RebateRepo rebateRepo;

    /**
     * 获得总的返佣
     *
     * @return
     */
    public BigDecimal getTotalRebate() {
        Rebate rebate = rebateRepo.findByAccountId(getCurrentAccountId());
        if (Optional.ofNullable(rebate).isPresent()) {
            return rebate.getTotalRebate();
        }
        return BigDecimal.ZERO;
    }

    /**
     * 累加返佣
     *
     * @param accountId
     * @param value
     * @return
     */
    public int addRebate(Long accountId, BigDecimal value) {
        Rebate rebate = rebateRepo.findByAccountId(accountId);
        if (rebate == null) {
            rebate = new Rebate();
            rebate.setAccountId(accountId);
            rebate.setBalanceRebate(BigDecimal.ZERO);
            rebate.setTotalRebate(BigDecimal.ZERO);
        }
        rebate.setTotalRebate(rebate.getTotalRebate().add(value));

        rebateRepo.save(rebate);
        return 1;
    }

    /**
     * 结算返佣
     *
     * @param accountId
     * @param value
     * @return
     */
    public int balanceRebate(Long accountId, BigDecimal value) {
        Rebate rebate = rebateRepo.findByAccountId(accountId);
        if (rebate == null) {
            rebate = new Rebate();
            rebate.setAccountId(accountId);
            rebate.setBalanceRebate(BigDecimal.ZERO);
            rebate.setTotalRebate(BigDecimal.ZERO);
        }
        rebate.setBalanceRebate(rebate.getBalanceRebate().add(value));
        rebateRepo.save(rebate);
        return 1;
    }
}
