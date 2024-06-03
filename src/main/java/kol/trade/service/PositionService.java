package kol.trade.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.criteria.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.google.common.eventbus.AsyncEventBus;

import kol.common.model.PageResponse;
import kol.common.model.Response;
import kol.trade.dto.vo.PositionSumProfit;
import kol.trade.dto.vo.PositionVo;
import kol.trade.dto.vo.StrategyStatistics;
import kol.trade.entity.Position;
import kol.trade.enums.PositionSideEnum;
import kol.trade.enums.PositionStatusEnum;
import kol.trade.repo.PositionRepo;
import lombok.AllArgsConstructor;

/**
 * 交易仓位
 *
 * @author kent
 */
@Service
@AllArgsConstructor
public class PositionService {
    final PositionRepo positionRepo;
    final AsyncEventBus eventBus;


    public List<Map<String, Object>> getCalPositionProfitByDay(Long investmentId, Long dayNum) {
        String startDate = LocalDate.now().minusDays(dayNum).toString();
        String endDate = LocalDate.now().toString();
        List<Map<String, Object>> mapList = positionRepo.findCalPositionProfitByDay(investmentId, startDate, endDate);

        return mapList;
    }

    public Map<String, Object> getCalTodayPosition() {
        String nowDate = LocalDate.now().toString();
        Long accountId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Map<String, Object> map = positionRepo.findCalTodayPosition(accountId, nowDate);
        if (CollectionUtils.isEmpty(map)) {
            map.put("profit", 0);
            map.put("total", 0);
            map.put("createdAt", LocalDate.now());
        }
        return map;
    }

    public PageResponse<Position> getPositionPage(Long investmentId, Long strategyId, String status, int pageIndex, int pageSize) {
        Sort sort = Sort.by(Sort.Direction.DESC,"status", "createdAt");
        PageRequest pageRequest = PageRequest.of(pageIndex - 1, pageSize, sort);
        Long accountId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Specification spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicateList = new ArrayList<>();
            if (accountId.longValue() != 1L) {
                predicateList.add(criteriaBuilder.equal(root.get("accountId"), accountId));
            }
            if (investmentId != null) {
                predicateList.add(criteriaBuilder.equal(root.get("investmentId"), investmentId));
            }
            if (strategyId != null) {
                predicateList.add(criteriaBuilder.equal(root.get("strategyId"), strategyId));
            }
            if (StringUtils.isNotBlank(status)) {
                PositionStatusEnum positionStatusEnum = PositionStatusEnum.valueOf(status);
                predicateList.add(criteriaBuilder.equal(root.get("status"), positionStatusEnum));
            }
            return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
        };
        Page<Position> page = positionRepo.findAll(spec, pageRequest);
        return PageResponse.of(page.getContent(), page.getTotalElements(), pageIndex, pageSize);
    }


    public Position findLast(Long strategyId) {
        return positionRepo.findLast(strategyId);
    }

    public List<StrategyStatistics> listByStrategyStatistics(List<Long> strategyIds) {
        return positionRepo.listByStrategyStatistics(strategyIds);
    }

    /**
     * 汇总玩家一段时间内的总盈利
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public List<PositionSumProfit> listPositionSumProfit(LocalDateTime startDate, LocalDateTime endDate) {
        return positionRepo.listPositionSumProfit(startDate, endDate);
    }

    /**
     * 更新待结算的跟单
     *
     * @param accountId
     * @param startDate
     * @param endDate
     * @return
     */
    public int updateWaitSettlement(Long accountId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Position> positions = positionRepo.findByAccountIdAndExitTimeBetween(accountId, startDate, endDate);
        for (Position position : positions) {
            position.setIsSettlement(true);
        }
        positionRepo.saveAll(positions);
        return 1;
    }

//    public List<Position> getNowPosition(Long investmentId, Long strategyId) {
//        Long accountId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        if (accountId != 1L && investmentId == null && strategyId == null) {
//            return GlobalCache.positionMap.values().stream().filter(p -> p.getAccountId().equals(accountId)).toList();
//        }else if(accountId != 1L){
//            return GlobalCache.positionMap.values().stream().filter(p -> p.getAccountId().equals(accountId)).toList();
//        }
//
//            return GlobalCache.positionMap.values();
//    }

    public java.util.Optional<Position> findById(Long id) {
    	return positionRepo.findById(id);
    }

    @Transactional
    public Response modifyStopLossGain(PositionVo positionVo) {

    	Optional<Position> op= positionRepo.findById(positionVo.getPositionId());
    	if(op.isEmpty()) {
    		return Response.buildFailure("PARAM_ERROR", "仓位ID不存在");
    	}
    	Position p=op.get();
    	BigDecimal stopGainPrice=positionVo.getStopGainPrice();
    	BigDecimal stopLossPrice=positionVo.getStopLossPrice();
    	if(p.getPositionSide()==PositionSideEnum.LONG&&stopGainPrice.compareTo(stopLossPrice)<0) {
    		return Response.buildFailure("PARAM_ERROR", "多头仓位止盈价小于止损价");
    	}

    	if(p.getPositionSide()==PositionSideEnum.SHORT&&stopGainPrice.compareTo(stopLossPrice)>0) {
    		return Response.buildFailure("PARAM_ERROR", "空头仓位止盈价大于止损价");
    	}
    	long investId=p.getInvestmentId();
    	positionRepo.findByInvestmentId(investId).stream().filter(item->item.getStatus()==PositionStatusEnum.OPEN&&p.getSymbol().equals(item.getSymbol())).forEach(item->{
			item.setStopGainPrice(stopGainPrice);
			item.setStopLossPrice(stopLossPrice);
			positionRepo.save(p);
			eventBus.post(item);
		});
    	return Response.buildSuccess();
    }

    public Optional<Position> findFirstNotSettlement(){
        return positionRepo.findFirstNotSettlement();
    }
}
