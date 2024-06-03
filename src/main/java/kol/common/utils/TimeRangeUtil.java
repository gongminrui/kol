package kol.common.utils;


import kol.common.model.TimeRange;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.bridge.MessageUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@Slf4j
public abstract class TimeRangeUtil {
    public static final String PATTERN_1 = "yyyy-MM-dd HH:mm:ss";
    public static final String PATTERN_2 = "yyyy-MM-dd";
    public static final String PATTERN_3 = "yyyyMMdd-HHmmss";
    public static final String PATTERN_4 = "yyyyMMddHHmmss";

    public static final int YEAR = Calendar.YEAR;
    public static final int MONTH = Calendar.MONTH;
    public static final int DATE = Calendar.DATE;
    public static final int HOUR = Calendar.HOUR_OF_DAY;
    public static final int MINUTE = Calendar.MINUTE;
    public static final int SECOND = Calendar.SECOND;
    public static final int MILLISECOND = Calendar.MILLISECOND;
    public static final int AM = Calendar.AM;
    public static final int AM_PM = Calendar.AM_PM;
    public static final int PM = Calendar.PM;

    public static TimeRange today() {
        return new TimeRange(setStartTime(new Date()), setEndTime(new Date()));
    }

    public static TimeRange getTimeRange(long start, long end) {
        return new TimeRange(new Date(start), new Date(end));
    }

    /**
     * 获得时间返回
     *
     * @param day 天数，根据当前时间加或者减去 day 参数值
     * @return endTime 结束时间就是当前最新时间
     */
    public static TimeRange getTimeRange(int day) {
        Calendar startTime = Calendar.getInstance();
        startTime.add(Calendar.DATE, day);
        Calendar endTime = Calendar.getInstance();
        // 如果是加时间，那么当前时间就作为开始时间
        if (day > 0) {
            return new TimeRange(setStartTime(endTime.getTime()), setEndTime(startTime.getTime()));
        } else {
            return new TimeRange(setStartTime(startTime.getTime()), setEndTime(endTime.getTime()));
        }
    }

    public static TimeRange yestertoday() {
        Calendar startTime = Calendar.getInstance();
        startTime.add(Calendar.DATE, -1);
        Calendar endTime = Calendar.getInstance();
        endTime.add(Calendar.DATE, -1);
        return new TimeRange(setStartTime(startTime.getTime()), setEndTime(endTime.getTime()));
    }

    public static TimeRange thisWeek() {
        Calendar calendar = Calendar.getInstance();

        // 当前星期几
        int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (day == 0) {
            day = 7;
        }

        // 需要向前推的天数
        final int dayNeedAdvanced = day - 1;
        calendar.add(Calendar.DAY_OF_MONTH, -dayNeedAdvanced);
        final Date startTime = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 6);
        final Date endTime = calendar.getTime();

        return new TimeRange(setStartTime(startTime), setEndTime(endTime));
    }

    public static TimeRange thisMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        Date startTime = calendar.getTime();

        calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DATE));
        Date endTime = calendar.getTime();

        setStartTime(startTime);
        setEndTime(endTime);
        return new TimeRange(startTime, endTime);
    }

    /**
     * 上月
     *
     * @return
     */
    public static TimeRange lastMonth() {
        return addTimeRangeByMonth(thisMonth(), -1);
    }

    /**
     * 增加年范围
     *
     * @param timeRange
     * @param value
     * @return
     */
    public static TimeRange addTimeRangeByYear(TimeRange timeRange, int value) {
        Date startTime = timeRange.getStartTime();
        Date endTime = timeRange.getEndTime();
        return new TimeRange(add(startTime, YEAR, value), add(endTime, YEAR, value));
    }

    /**
     * 月范围
     *
     * @param timeRange
     * @param value
     * @return
     */
    public static TimeRange addTimeRangeByMonth(TimeRange timeRange, int value) {
        Date startTime = timeRange.getStartTime();
        Date endTime = timeRange.getEndTime();
        return new TimeRange(add(startTime, MONTH, value), add(endTime, MONTH, value));
    }

    public static Date getNextMonthFirst() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(MONTH, 1);
        calendar.set(DATE, 1);
        calendar.set(HOUR, 0);
        calendar.set(MINUTE, 0);
        calendar.set(SECOND, 0);
        calendar.set(MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date setStartTime(Date date) {
        // 设置开始时间
        set(date, HOUR, 0);
        set(date, MINUTE, 0);
        set(date, SECOND, 0);
        set(date, MILLISECOND, 0);
        return date;
    }

    public static Date setEndTime(Date date) {
        // 设置开始时间
        set(date, HOUR, 23);
        set(date, MINUTE, 59);
        set(date, SECOND, 59);
        set(date, MILLISECOND, 999);
        return date;
    }

    public static void set(Date date, int field, int value) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTime(date);
        calendar.set(field, value);
        date.setTime(calendar.getTimeInMillis());
    }

    public static Date addDay(Date date, int value) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTime(date);
        calendar.add(DATE, value);
        return calendar.getTime();
    }

    public static Date add(Date date, int field, int value) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTime(date);
        calendar.add(field, value);
        return calendar.getTime();
    }

    public static TimeRange getTimeRange(String startTime, String endTime) {
        startTime = addTime(startTime, true);
        endTime = addTime(endTime, false);

        final Date startDate = parse(startTime);
        final Date endDate = parse(endTime);
        return new TimeRange(startDate, endDate);
    }

    /**
     * 添加时间
     *
     * @param datetime 时间
     * @param isStart  是否开始时间，添加 00:00:00 否则添加 23:59:59
     * @return
     */
    private static String addTime(String datetime, boolean isStart) {
        // 通过空格分割字符串，如果有2个以上，表示有时分秒部分
        int length = datetime.split(" ").length;
        if (length >= 2) {
            return datetime;
        }
        return datetime + (isStart ? " 00:00:00" : " 23:59:59");
    }

    public static Date parse(String dateString) {
        return parse(dateString, PATTERN_1);
    }


    public static Date parse(String dateString, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            return sdf.parse(dateString);
        } catch (Exception e) {
            log.error("转换日期异常: ", e);
        }
        return null;
    }

    public static String format(Date date){
        SimpleDateFormat fmt = new SimpleDateFormat(PATTERN_1);
        fmt.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        try {
            return fmt.format(date);
        } catch (Exception e) {
            log.error("转换日期错误", e);
        }
        return null;
    }

    /**
     * 计算两个日期相差天数
     *
     * @param date1
     * @param date2
     * @return
     */
    public static final int getDifferentDay(Date date1, Date date2) {
        if (date1 == null || date2 == null)
            return 0;
        date1 = setStartTime(date1);
        date2 = setStartTime(date2);
        long l1 = date1.getTime();
        long l2 = date2.getTime();
        long diff = l2 - l1;
        return (int) (diff / (1000 * 3600 * 24));
    }

    public static void main(String[] args) {
        System.out.println(format(new Date()));
    }
}
