package kol.money.service;

import kol.common.model.PageResponse;
import kol.money.model.MoneyRecord;
import kol.money.repo.MoneyRecordRepo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 系统资金服务，核心是记账
 *
 * @author guanzhenggang@gmail.com
 */
@Service
public class MoneyService {
    final MoneyRecordRepo moneyRecordRepo;

    public MoneyService(MoneyRecordRepo moneyRecordRepo) {
        this.moneyRecordRepo = moneyRecordRepo;
    }

    /**
     * 记录资金流水账单
     *
     * @param accountId
     * @param type
     * @param amount
     * @return
     */
    public Long createMoneyRecord(Long accountId, MoneyRecord.Type type, BigDecimal amount, String comment) {
        MoneyRecord moneyRecord = new MoneyRecord();
        moneyRecord.setAccountId(accountId)
                .setType(type)
                .setAmount(amount)
                .setComment(comment);
        return moneyRecordRepo.save(moneyRecord).getId();
    }


}
