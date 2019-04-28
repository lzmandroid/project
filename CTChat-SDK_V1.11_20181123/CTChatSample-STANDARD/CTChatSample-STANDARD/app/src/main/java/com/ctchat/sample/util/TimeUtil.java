package com.ctchat.sample.util;

import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;

public class TimeUtil {

    private static final AtomicLong LAST_TIME_MS = new AtomicLong();

    public static long getCurrTimeMillis() {
        long currTime = System.currentTimeMillis();
        return currTime;
    }

    public static long uniqueCurrentTimeMS() {
        long now = System.currentTimeMillis();
        while (true) {
            long lastTime = LAST_TIME_MS.get();
            if (lastTime >= now)
                now = lastTime + 1;
            if (LAST_TIME_MS.compareAndSet(lastTime, now))
                return now;
        }
    }

    public static String getCurrTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String currTime = String.valueOf(sdf.format(new Date()));
        return currTime;
    }

    public static String getCurrTimeData() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String currTime = String.valueOf(sdf.format(new Date()));
        return currTime;
    }

    public static String getTimeFormatmmss(long time) {
        SimpleDateFormat sdf =new SimpleDateFormat("mm:ss");
        String currTime = String.valueOf(sdf.format(time));
        return currTime;
    }

    //本地时间"yyyyMMdd'T'HHmmss" 转化 "yyyy-MM-dd"格式
    public static String localTimeShowStyleChange(String localTime) {
        SimpleDateFormat utcFormater = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        utcFormater.setTimeZone(TimeZone.getDefault());
        Date gpsUTCDate = null;
        try {
            gpsUTCDate = utcFormater.parse(localTime);
            SimpleDateFormat localFormater = new SimpleDateFormat("yyyy-MM-dd");
            localFormater.setTimeZone(TimeZone.getDefault());
            String localTimeChageStyle = localFormater.format(gpsUTCDate.getTime());
            return localTimeChageStyle;
        } catch (java.text.ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }

    }

    /**
     * 把时间戳转化成对应的字符串
     *
     * @param time      时间戳
     * @param formatStr 对应的字符串"yyyy-MM-dd HH:mm:ss"
     * @return
     */
    public static String getTime(String time, String formatStr) {
        SimpleDateFormat format = new SimpleDateFormat(((TextUtils.isEmpty(formatStr)) ? "yyyy年M月d日 H:mm:ss" : formatStr));
        String str = "";
        if (!TextUtils.isEmpty(time)) {
            Date date = new Date(Long.parseLong(time));
            str = format.format(date);
        } else {
            Date date = new Date(System.currentTimeMillis());
            str = format.format(date);
        }
        return str;
    }
}
