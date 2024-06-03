package kol;

import kol.account.dto.vo.AccountRebateVo;
import kol.account.model.Account;
import kol.account.repo.AccountRepo;
import kol.account.service.AccountRelationService;
import kol.common.utils.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Gongminrui
 * @date 2023-03-05 15:16
 */
@SpringBootTest
public class AccountRelationServiceTest {
    @Resource
    private AccountRelationService accountRelationService;

    @Test
    public void listNext() {
//        List<AccountRebateVo> accounts = accountRelationService.listNext(6l, "");
//
//        System.out.println(JsonUtils.objectToJson(accounts));
    }

    @Test
    public void createRelation() {
        accountRelationService.createRelation(6l, 1l);
    }

    @Test
    void getNextVipCount(){
        int nextVipCount = accountRelationService.getNextVipCount(25l);
    }
}
