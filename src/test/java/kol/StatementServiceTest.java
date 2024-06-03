package kol;

import kol.auth.model.TokenAuthentication;
import kol.common.model.PageResponse;
import kol.money.model.Statement;
import kol.money.service.StatementService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Resource;
import java.util.ArrayList;

/**
 * @author Gongminrui
 * @date 2023-05-09 15:58
 */
@SpringBootTest
public class StatementServiceTest {
    @Resource
    private StatementService statementService;

    private void setLogin(Long accountId) {
        Authentication authentication = new TokenAuthentication(accountId, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void generateStatement() {
        statementService.generateStatement();
    }

    @Test
    void handleNotSettleStatement() {
        statementService.handleNotSettleStatement();
    }

    @Test
    void settle() {
        setLogin(45l);
        statementService.settle(19l);
    }

    @Test
    void pageStatement() {
        PageResponse<Statement> statementPageResponse = statementService.pageStatement(null, null, 1, 10);
    }

    @Test
    void manualGenStatement() {
        setLogin(1l);
        statementService.manualGenStatement();
    }
}
