package kol;

import cn.hutool.core.date.CalendarUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Gongminrui
 * @date 2023-04-17 23:27
 */
public class TestTime {
    public static void main(String[] args) {
        Calendar calendar = CalendarUtil.parseByPatterns("2023-04-17 23:27:35", DatePattern.NORM_DATETIME_PATTERN);

        Date time = calendar.getTime();

        System.out.println(DateUtil.parse("2023-04-17 23:31:20").toJdkDate());
    }
}
