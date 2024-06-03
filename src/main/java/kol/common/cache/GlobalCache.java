package kol.common.cache;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;

import io.contek.invoker.okx.api.common._Instrument;
import kol.account.model.Account;
import kol.account.repo.AccountRepo;
import kol.common.service.SubscribeService;
import kol.exchange.service.OkxService;
import kol.trade.entity.Investment;
import kol.trade.entity.Position;
import kol.trade.enums.PositionStatusEnum;
import kol.trade.repo.InvestmentRepo;
import kol.trade.repo.PositionRepo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

/**
 * 全局缓存类
 *
 * @author kent
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GlobalCache implements ApplicationRunner {
    /**
     * 全局缓存持仓信息
     * key为 positionNo+"_"+accountId 例如 1679298994814_1
     */
    public ConcurrentHashMap<String, Position> positionMap = new ConcurrentHashMap<>();
    /**
     * 缓存跟单信息
     */
    @Getter
    public List<Investment> investments;

    public List<Account> accounts;

    final PositionRepo positionRepo;
    final SubscribeService subscribeService;
    final InvestmentRepo investRepo;
    final OkxService exchagneUtil;
    final AccountRepo accountRepo;
    final AsyncEventBus eventBus;

    /**
     * 缓存token用户信息
     */
    private static Cache<String, Account> TOKEN_CACHE = Caffeine.newBuilder()
            .initialCapacity(5)
            .maximumSize(2000)
            .expireAfterWrite(7, TimeUnit.DAYS)
            .build();

    /**
     * 缓存用户信息
     *
     * @param token
     * @param account
     */
    public static void cacheUser(String token, Account account) {
        TOKEN_CACHE.put(token, account);
    }

    /**
     * 获取用户信息
     *
     * @param token
     * @return
     */
    public static Account getAccount(String token) {
        Account account = TOKEN_CACHE.getIfPresent(token);
        return account;
    }

    /**
     * 初始化当前持仓信息并监听持仓交易对价格
     */
    public void initData() {
        subscribeService.init();
        if (positionMap.size() == 0) {
            positionRepo.findByStatus(PositionStatusEnum.OPEN).stream().forEach(p -> {
                String cachePositionNo = p.getPositionNo() + "_" + p.getAccountId();
                positionMap.put(cachePositionNo, p);
                _Instrument instrument = exchagneUtil.getInstrumentMap().get(p.getSymbol());
                if (instrument != null && instrument.state.equals("live")) {
                    subscribeService.subscribeTicker(p.getSymbol());
                }

            });
        }
        investments = Collections.synchronizedList(investRepo.findByIsEnd(false));
        accounts = Collections.synchronizedList(accountRepo.findAll());
    }

    @Subscribe
    public void accountCacheUpdate(Account account) {
        if (accounts.contains(account)) {
            accounts.remove(account);
        }
        accounts.add(account);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initData();
    }
}
