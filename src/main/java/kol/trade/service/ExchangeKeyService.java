package kol.trade.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import io.contek.invoker.okx.api.common._AccountBalance;
import kol.exchange.service.OkxService;
import kol.trade.dto.cmd.ExchangeKeyCmd;
import kol.trade.dto.vo.ExchangeKeyVO;
import kol.trade.entity.ExchangeKey;
import kol.trade.entity.Investment;
import kol.trade.entity.QExchangeKey;
import kol.trade.entity.Symbol;
import kol.trade.repo.ExchangeKeyRepo;
import kol.trade.repo.InvestmentRepo;
import kol.trade.repo.SymbolRepo;

/**
 * @author kent
 */
@Service
public class ExchangeKeyService {
    final ExchangeKeyRepo exchangeKeyRepo;
    final JPAQueryFactory jpaQueryFactory;
    final OkxService exchangeUtil;
    final SymbolRepo symbolRepo;
    final InvestmentRepo investRepo;

    public ExchangeKeyService(ExchangeKeyRepo exchangeKeyRepo, JPAQueryFactory jpaQueryFactory,
                              OkxService exchangeUtil, SymbolRepo symbolRepo, InvestmentRepo investRepo) {
        this.exchangeKeyRepo = exchangeKeyRepo;
        this.jpaQueryFactory = jpaQueryFactory;
        this.exchangeUtil = exchangeUtil;
        this.symbolRepo = symbolRepo;
        this.investRepo = investRepo;
    }

    public void add(ExchangeKeyCmd cmd) {
        ExchangeKey exchangeKey = new ExchangeKey();
        BeanUtils.copyProperties(cmd, exchangeKey);

        List<Symbol> symbolList = symbolRepo.findByStatus(Symbol.StatusEnum.ON);
        exchangeUtil.postSetAccount(exchangeKey, "20", symbolList);
        Long accountId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        exchangeKey.setAccountId(accountId);
        exchangeKeyRepo.save(exchangeKey);
    }


    public void delete(Long exchangeKeyId) {
        Long accountId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<ExchangeKey> optional = exchangeKeyRepo.findByIdAndAccountId(exchangeKeyId, accountId);
        Assert.isTrue(optional.isPresent(), "交易所KEY不存在");

        List<Investment> investList = investRepo.findByApiKeyIdAndIsEnd(exchangeKeyId, Boolean.FALSE);
        boolean result = CollectionUtils.isEmpty(investList);
        Assert.isTrue(result, "当前交易所API_KEY存在跟单，请结束跟单后在删除");

        optional.get().setStatus(ExchangeKey.ExchangeKeyStatusEnum.DELETE);
        exchangeKeyRepo.save(optional.get());

    }

    public List<ExchangeKeyVO> listExchangeKey() {
        Long accountId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        QExchangeKey exchangeKey = QExchangeKey.exchangeKey;
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(exchangeKey.accountId.eq(accountId));
        booleanBuilder.and(exchangeKey.status.eq(ExchangeKey.ExchangeKeyStatusEnum.NORMAL));

        return jpaQueryFactory
                .select(Projections.bean(ExchangeKeyVO.class,
                        exchangeKey.id, exchangeKey.name, exchangeKey.accountId, exchangeKey.apiKey, exchangeKey.createdAt, exchangeKey.exchange))
                .from(exchangeKey)
                .where(booleanBuilder)
                .orderBy(exchangeKey.createdAt.desc())
                .fetch();
    }

    public BigDecimal getBalance(Long apiKeyId) {

        Optional<ExchangeKey> optional = exchangeKeyRepo.findById(apiKeyId);
        Assert.isTrue(optional.isPresent(), "交易所API_KEY不存在");

        _AccountBalance balance = exchangeUtil.getAccountBalance(optional.get());
        String accountBalance = balance.details.stream()
                .filter(f -> f.ccy.equals("USDT"))
                .map(m -> m.cashBal).findFirst().orElse("0");
        //计算 可用余额=交易所余额-跟投资金
        Long accountId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        BigDecimal totalPrincipal = investRepo.getTotalPrincipal(accountId, apiKeyId);
        BigDecimal surplusBalance = new BigDecimal(accountBalance).subtract(totalPrincipal);
        BigDecimal availableBalance = surplusBalance.compareTo(BigDecimal.ZERO) > 0 ? surplusBalance : BigDecimal.ZERO;
        return availableBalance;
    }

    /**
     * 测试key
     *
     * @param cmd
     */
    public void testKey(ExchangeKeyCmd cmd) {

    }
}