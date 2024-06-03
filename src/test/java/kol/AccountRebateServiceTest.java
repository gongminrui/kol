package kol;

import kol.account.dto.cmd.AgentCmd;
import kol.account.model.RoleEnum;
import kol.account.service.AccountRebateService;
import kol.auth.model.TokenAuthentication;
import kol.common.model.TimeRange;
import kol.common.utils.TimeRangeUtil;
import kol.trade.entity.Position;
import kol.trade.repo.PositionRepo;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gongminrui
 * @date 2023-03-05 18:21
 */
@SpringBootTest
public class AccountRebateServiceTest {
    @Resource
    private AccountRebateService accountRebateService;
    @Resource
    private PositionRepo positionRepo;

    private void setLogin(Long accountId) {
        Authentication authentication = new TokenAuthentication(accountId, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    public void save() {
        AgentCmd agentCmd = new AgentCmd();

//        agentCmd.setAccountId(6l);
//        agentCmd.setInviteRebate(BigDecimal.valueOf(0.4));
//        setLogin(1l);
//        accountRebateService.saveOrUpdate(agentCmd);

        agentCmd.setAccountId(7l);
        agentCmd.setInviteRebate(BigDecimal.valueOf(0.2));
        setLogin(6l);
        accountRebateService.saveOrUpdate(agentCmd);
    }

    @Test
    public void calRebate() {
//        accountRebateService.calRebate(33l, BigDecimal.valueOf(100));

        TimeRange today = TimeRangeUtil.yestertoday();
        List<Position> byCreatedAtBetween = positionRepo.findByExitTimeBetween(today.getStartDate(), today.getEndDate());

        System.out.println();
    }

    @Test
    public void timeRebate(){
        TimeRange timeRange = TimeRangeUtil.yestertoday();
        accountRebateService.timeRebate(timeRange);
    }

    @Test
    public void calVipRebate(){
        accountRebateService.calVipRebate(44l, new BigDecimal("100"), true);
    }
}
