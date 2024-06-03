package kol;

import kol.account.model.VipFeeConfig;
import kol.account.service.VipFeeConfigService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Gongminrui
 * @date 2023-05-26 22:17
 */
@SpringBootTest
public class VipFeeConfigServiceTest {
    @Resource
    private VipFeeConfigService vipFeeConfigService;

    @Test
    void all(){
        List<VipFeeConfig> vipFeeConfigs = vipFeeConfigService.listAll();

        System.out.println();
    }
}
