package kol.money.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import kol.common.model.PageResponse;
import kol.common.model.SingleResponse;
import kol.money.model.MoneyRecord;
import kol.money.repo.MoneyRecordRepo;
import kol.money.service.WalletService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kent
 */
@Api(tags = "钱包交易记录")
@RestController
@RequestMapping("/api/money")
public class MoneyRecordController {

    final MoneyRecordRepo moneyRecordRepo;
    @Resource
    private WalletService walletService;

    public MoneyRecordController(MoneyRecordRepo moneyRecordRepo) {
        this.moneyRecordRepo = moneyRecordRepo;
    }

    @ApiOperation("获得可提现金额")
    @GetMapping("getWithdrawAmount")
    public SingleResponse<BigDecimal> getWithdrawAmount() {
        return SingleResponse.of(walletService.getWithdrawAmount());
    }

    @ApiOperation("分页获取钱包交易记录")
    @GetMapping("/page")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "recordType", value = "交易类型", required = false, dataTypeClass = MoneyRecord.Type.class),
            @ApiImplicitParam(name = "pageIndex", value = "当前页数", required = true, dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", required = true, dataTypeClass = Integer.class)
    })
    public PageResponse<MoneyRecord> getMoneyPage(@RequestParam(value = "recordType", required = false) MoneyRecord.Type recordType,
                                                  @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,
                                                  @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(pageIndex - 1, pageSize, sort);
        Long accountId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Specification spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicateList = new ArrayList<>();
            predicateList.add(criteriaBuilder.equal(root.get("accountId"), accountId));
            if (recordType != null) {
                predicateList.add(criteriaBuilder.equal(root.get("type"), recordType));
            }
            return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
        };
        Page<MoneyRecord> page = moneyRecordRepo.findAll(spec, pageRequest);
        return PageResponse.of(page.getContent(), page.getTotalElements(), pageIndex, pageSize);
    }

}
