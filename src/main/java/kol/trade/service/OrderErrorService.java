package kol.trade.service;

import kol.common.model.PageResponse;
import kol.trade.entity.TraderOrderError;
import kol.trade.repo.TraderOrderErrorRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kent
 */
@Service
public class OrderErrorService {
    final TraderOrderErrorRepo orderErrorRepo;

    public OrderErrorService(TraderOrderErrorRepo orderErrorRepo) {
        this.orderErrorRepo = orderErrorRepo;
    }

    /**
     * 查询开平单错误记录
     *
     * @param accountId
     * @param strategyId
     * @param pageSize
     * @param pageIndex
     * @return
     */
    public PageResponse<TraderOrderError> listOrderErrorToPage(Long accountId, Long strategyId, Integer pageSize, Integer pageIndex) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(pageIndex - 1, pageSize, sort);
        Specification spec = (Specification<TraderOrderError>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicateList = new ArrayList<>();
            if (accountId != null) {
                predicateList.add(criteriaBuilder.equal(root.get("accountId"), accountId));
            }
            if (strategyId != null) {
                predicateList.add(criteriaBuilder.equal(root.get("strategyId"), strategyId));
            }
            return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
        };
        Page<TraderOrderError> page = orderErrorRepo.findAll(spec, pageRequest);
        int totalCount = Integer.valueOf(String.valueOf(page.getTotalElements()));
        return PageResponse.of(page.getContent(), totalCount, pageSize, pageIndex);
    }
}
