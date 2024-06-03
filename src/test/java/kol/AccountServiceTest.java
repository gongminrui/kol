package kol;

import kol.account.model.Account;
import kol.account.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Gongminrui
 * @date 2023-05-30 14:00
 */
@SpringBootTest
public class AccountServiceTest {
    @Resource
    private AccountService accountService;

    @Test
    void getVipList(){
        List<Account> vipList = accountService.getVipList();
    }
}
