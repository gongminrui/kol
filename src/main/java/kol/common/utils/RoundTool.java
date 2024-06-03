package kol.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author Gongminrui
 * @date 2023-06-01 21:09
 */
public class RoundTool {

    /**
     * 默认保留2位小数，向下取整
     * @param bigDecimal
     * @return
     */
    public static BigDecimal round(BigDecimal bigDecimal){
        return bigDecimal.setScale(2, RoundingMode.DOWN);
    }

}
