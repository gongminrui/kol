package kol;

import kol.money.repo.WalletRepo;
import kol.vip.service.VipService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @author Gongminrui
 * @date 2023-05-28 21:03
 */
@SpringBootTest
public class VipServiceTest extends BaseTest {
    @Resource
    private VipService vipService;
    @Resource
    private WalletRepo walletRepo;

    @Test
    void notice() {
        vipService.notice();
    }

    @Test
    void openVip() {
        setLogin(45l);
        vipService.openVip();
    }

    @Test
    void expire() {
        vipService.expire();
    }

    @Test
    void checkGiveVip() {
        vipService.checkGiveVip();
    }

    @Test
    void updateRechargeAmount() {
        walletRepo.updateRechargeAmount(BigDecimal.valueOf(100), 45l);
    }

    @Test
    void getDiscountVipFee(){
        vipService.getDiscountVipFee(BigDecimal.valueOf(100), 25l);
    }

    @Test
    void getCurrentLoginVipFee(){
        setLogin(25l);
        vipService.getCurrentLoginVipFee();
    }
}
