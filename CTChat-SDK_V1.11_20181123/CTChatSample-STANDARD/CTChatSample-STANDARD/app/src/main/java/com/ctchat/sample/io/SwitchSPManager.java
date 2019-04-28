package com.ctchat.sample.io;

import android.content.Context;

import com.ctchat.sdk.basemodule.util.PrefUtil;

public class SwitchSPManager {

    // 新消息提醒页
    private static final String NEW_MESSAGE_NOTICE = "newMessageNotice";
    private static final String OPEN_NOTIFICATION = "openNotification";
    private static final String OPEN_VIBRATE = "openVibrate";

    // 高级设置页
    private static final String LARGE_FONT = "largeFont";
    private static final String SATELLITE_LOCATION_MODE = "satelliteLocationMode";
    private static final String LOCATION_FREQUENCY = "locationFrequency";

    // 对讲设置页
    private static final String MUTE_MODE = "muteMode";
    private static final String MICROPHONE = "microphone";
    private static final String INCREASE_INTERCOM_QUALITY = "increaseIntercomQuality";
    private static final String SCREEN_PTT_KEY = "screen_ptt_key";
    private static final String VOLUME_KEY = "volumeKey";
    private static final String AUTO_ANSWER = "autoAnswer";
    private static final String NOT_DISTURB = "notDisturb";
    private static final String HEART_BEAT_FREQUENCY = "heartBeatFrequency";

    // 多媒体设置页
    private static final String NOTICE_USER_NO_WIFI = "noticeUserWhileNoWifi";
    private static final String AUTO_DOWNLOAD = "autoDownload";
    private static final String PICTIRE_UPLOAD_SIZE = "pictureUploadSize";
    private static final String STORAGE_LOCATION = "storageLocation";

    // 附着预定义组
    private static final String CHANNEL_ATTACHED = "channelAttached";

    //蓝牙
    private static final String BLUETOOTH_ADDR = "bluetoothAddress";

    /**
     * 是否接收新消息
     * @param context
     * @param flag
     */
    public static void setIsNewMsgNotice(Context context, boolean flag) {
        PrefUtil.put(context, NEW_MESSAGE_NOTICE, flag);
    }

    public static boolean getIsNewMsgNotice(Context context) {
       return (boolean) PrefUtil.get(context, NEW_MESSAGE_NOTICE,true);
    }

    /**
     * 是否开启通知
     * @param context
     * @param flag
     */
    public static void setIsNotification(Context context, boolean flag) {
        PrefUtil.put(context, OPEN_NOTIFICATION, flag);
    }

    public static boolean getIsNotification(Context context) {
        return (boolean) PrefUtil.get(context, OPEN_NOTIFICATION, true);
    }

    /**
     * 是否开启振动
     * @param context
     * @param flag
     */
    public static void setIsVibrate(Context context, boolean flag) {
        PrefUtil.put(context, OPEN_VIBRATE, flag);
    }

    public static boolean getIsVibrate(Context context) {
        return (boolean) PrefUtil.get(context, OPEN_VIBRATE, false);
    }

    /**
     * 是否使用大字体
     * @param context
     * @param flag
     */
    public static void setUseLargeFont(Context context, boolean flag) {
        PrefUtil.put(context, LARGE_FONT, flag);
    }

    public static boolean getUseLargeFont(Context context) {
        return (boolean) PrefUtil.get(context, LARGE_FONT, false);
    }

    /**
     * 是否开启卫星定位模式
     * @param context
     * @param flag
     */
    public static void setIsSatelliteLocate(Context context, boolean flag) {
        PrefUtil.put(context, SATELLITE_LOCATION_MODE, flag);
    }

    public static boolean getIsSatelliteLocate(Context context) {
        return (boolean) PrefUtil.get(context, SATELLITE_LOCATION_MODE, true);
    }

    /**
     * 设置定位频度
     * @param context
     * @param seconds
     */
    public static void setLocationFrequency(Context context, int seconds) {
        PrefUtil.put(context, LOCATION_FREQUENCY, seconds);
    }

    public static int getLocationFrequency(Context context) {
        return (int) PrefUtil.get(context, LOCATION_FREQUENCY, 30);
    }

    /**
     * 是否开启静音模式
     * @param context
     * @param flag
     */
    public static void setMuteMode(Context context, boolean flag) {
        PrefUtil.put(context, MUTE_MODE, flag);
    }

    public static boolean getMuteMode(Context context) {
        return (boolean) PrefUtil.get(context, MUTE_MODE,false);
    }

    /**
     * 是否开启声音放大器
     * @param context
     * @param flag
     */
    public static void setMicrophone(Context context, boolean flag) {
        PrefUtil.put(context, MICROPHONE, flag);
    }

    public static boolean getMicrophone(Context context) {
        return (boolean) PrefUtil.get(context, MICROPHONE, false);
    }

    /**
     * 是否开启增强对讲音质
     * @param context
     * @param flag
     */
    public static void setIncreaseIntercomQuailty(Context context, boolean flag) {
        PrefUtil.put(context, INCREASE_INTERCOM_QUALITY, flag);
    }

    public static boolean getIncreaseIntercomQuality(Context context) {
        return (boolean) PrefUtil.get(context, INCREASE_INTERCOM_QUALITY, false);
    }

    /**
     * 是否开启屏幕PTT按键
     * @param context
     * @param flag
     */
    public static void setScreenPttKey(Context context, boolean flag) {
        PrefUtil.put(context, SCREEN_PTT_KEY, flag);
    }

    public static boolean getScreenPttKey(Context context) {
        return (boolean) PrefUtil.get(context, SCREEN_PTT_KEY, false);
    }

    /**
     * 是否开启音量按键
     * @param context
     * @param flag
     */
    public static void setVolumeKey(Context context, boolean flag) {
        PrefUtil.put(context, VOLUME_KEY, flag);
    }

    public static boolean getVolumeKey(Context context) {
        return (boolean) PrefUtil.get(context, VOLUME_KEY, false);
    }

    /**
     * 是否开启自动应答
     * @param context
     * @param flag
     */
    public static void setAutoAnswer(Context context, boolean flag) {
        PrefUtil.put(context, AUTO_ANSWER, flag);
    }

    public static boolean getAutoAnswer(Context context) {
        return (boolean) PrefUtil.get(context, AUTO_ANSWER, false);
    }

    /**
     * 是否开启免打扰
     * @param context
     * @param flag
     */
    public static void setNotDisturb(Context context, boolean flag) {
        PrefUtil.put(context, NOT_DISTURB, flag);
    }

    public static boolean getNotDisturb(Context context) {
        return (boolean) PrefUtil.get(context, NOT_DISTURB, false);
    }

    /**
     * 记录心跳频度
     */
    public static void setHeartBeatFrequency(Context context, int seconds) {
        PrefUtil.put(context, HEART_BEAT_FREQUENCY, seconds);
    }

    public static int getHeartBeatFrequency(Context context) {
        return (int) PrefUtil.get(context, HEART_BEAT_FREQUENCY, 20);
    }

    /**
     * 非wifi环境，下载时提醒用户
     * @param context
     * @param flag
     */
    public static void setNoticeUserNoWifi(Context context, boolean flag) {
        PrefUtil.put(context, NOTICE_USER_NO_WIFI, flag);
    }

    public static boolean getNoticeUserNoWifi(Context context) {
        return (boolean) PrefUtil.get(context, NOTICE_USER_NO_WIFI, false);
    }

    /**
     * 是否开启自动下载
     * @param context
     * @param flag
     */
    public static void setAutoDownload(Context context, boolean flag) {
        PrefUtil.put(context, AUTO_DOWNLOAD, flag);
    }

    public static boolean getAutoDownload(Context context) {
        return (boolean) PrefUtil.get(context, AUTO_DOWNLOAD, false);
    }

    /**
     * 保存附着预定义组
     * @param context
     * @param channelJstring
     */
    public static void setAttachedChannel(Context context, String channelJstring){
        PrefUtil.put(context, CHANNEL_ATTACHED, channelJstring);
    }

    public static String getAttachedChannel(Context context){
        return (String) PrefUtil.get(context, CHANNEL_ATTACHED, "");
    }

    /**
     * 图片上传设置
     * @param context
     * @param size
     */
    public static void setUploadPictureSize(Context context, int size) {
        PrefUtil.put(context, PICTIRE_UPLOAD_SIZE, size);
    }

    public static int getUploadPictureSize(Context context) {
        return (int) PrefUtil.get(context, PICTIRE_UPLOAD_SIZE, 25);
    }
}
