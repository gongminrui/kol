package kol;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import kol.trade.dto.vo.PositionSumProfit;
import kol.trade.service.PositionService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Gongminrui
 * @date 2023-04-17 22:10
 */
@SpringBootTest
public class PositionServiceTest {
    @Resource
    private PositionService positionService;

    @Test
    void test() {
        List<PositionSumProfit> positions = positionService.listPositionSumProfit(
                LocalDateTimeUtil.parse("2023-04-01 00:00:00", DatePattern.NORM_DATETIME_PATTERN),
                LocalDateTimeUtil.parse("2023-04-11 00:00:00", DatePattern.NORM_DATETIME_PATTERN));

        System.out.println(positions);
    }
}
