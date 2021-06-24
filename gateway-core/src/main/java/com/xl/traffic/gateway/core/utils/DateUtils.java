package com.xl.traffic.gateway.core.utils;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 将日期时间转换成字符格式
     *
     * @param date
     * @param newFormat
     * @return
     */
    public static String format(Date date, String newFormat) {
        if (date == null) {
            return null;
        }
        if (StringUtils.isBlank(newFormat)) {
            newFormat = DATE_TIME_FORMAT;
        }
        LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return localDateTime.format(DateTimeFormatter.ofPattern(newFormat));
    }

    /**
     * 获取当前时间处于第几个周期时间
     *
     * @param cycleTime 周期时长，单位秒
     * @param time      UTC到现在的毫秒数
     * @return 当前时间处于第几个周期时间
     */
    public static long getCycleTimeIndex(long cycleTime, long time) {
        return time / (cycleTime * GatewayConstants.MILLISECOND_IN_SECOND);
    }

    /**
     * 返回举例当前时间最近的上一个周期的结束时间
     *
     * @param cycleTime 周期时长，单位秒
     * @param time      UTC到现在的毫秒数
     * @return 当前时间处于第几个周期时间
     */
    public static long getLastCycleEndTime(long cycleTime, long time) {
        return time - (time % (cycleTime * GatewayConstants.MILLISECOND_IN_SECOND));
    }

    /**
     * 获取指定时间秒数
     *
     * @param time UTC到现在的毫秒数
     * @return UTC的分钟数
     */
    public static long getSecond(long time) {
        return time / GatewayConstants.MILLISECOND_IN_SECOND;
    }
}
