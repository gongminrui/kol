package kol.trade.service;

import kol.account.model.RoleEnum;
import kol.common.service.BaseService;
import kol.config.model.Config;
import kol.trade.entity.Symbol;
import kol.trade.repo.SymbolRepo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author kent
 */
@Service
public class SymbolService extends BaseService implements ApplicationRunner {
    final SymbolRepo symbolRepo;

    private ConcurrentHashMap<String, String> symbolMap = new ConcurrentHashMap<>();

    public SymbolService(SymbolRepo symbolRepo) {
        this.symbolRepo = symbolRepo;
    }

    /**
     * 检查
     *
     * @param symbol
     * @return
     */
    public Boolean checkSymbol(String symbol) {
        symbol = symbolMap.get(symbol);
        return StringUtils.isNotBlank(symbol);
    }

    public List<Symbol> getList() {
        return symbolRepo.findByStatus(Symbol.StatusEnum.ON);
    }

    public void refresh() {
        Assert.isTrue(RoleEnum.ROLE_ADMIN == getLoginAccount().getRole(), "无权限操作");
        init();
    }

    public void init() {
        List<Symbol> list = symbolRepo.findByStatus(Symbol.StatusEnum.ON);
        Map<String, String> map = list.parallelStream().collect(Collectors.toMap(Symbol::getSymbolTitle, Symbol::getSymbolTitle));
        symbolMap.clear();
        symbolMap.putAll(map);
    }

    @Override
    public void run(ApplicationArguments args) {
        init();
    }


}
