package kol.trade.repo;

import kol.trade.entity.Symbol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author kent
 */
@Repository
public interface SymbolRepo extends JpaRepository<Symbol, Long> {
    /**
     * 根据状态查询交易所
     *
     * @param status
     * @return
     */
    List<Symbol> findByStatus(Symbol.StatusEnum status);
}
