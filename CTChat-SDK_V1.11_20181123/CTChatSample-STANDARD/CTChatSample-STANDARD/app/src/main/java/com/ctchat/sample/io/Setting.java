package com.ctchat.sample.io;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;

import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sdk.ptt.tool.setting.SettingManager;

public class Setting {

    public static String ATTACH_CHANNEL_KEY = "attachedChannel";

    public static int getStreamType(){
        int type = AudioManager.STREAM_MUSIC;
        Logger.i("AudioOutputManager", "stream type:"+type);
        return type;
    }

    /**
     * 设置附着预定义组
     * @param jstring 预定义组ID的json字符串
     */
    public static void setAttachedChannel(Context context, String jstring){
        SwitchSPManager.setAttachedChannel(context, jstring);
    }

    public static String getAttachedChannel(Context context){
        return SwitchSPManager.getAttachedChannel(context);
    }

    /**
     * 设置是否开启声音放大器
     * @param flag
     */
    public static void setAudioAmplifier(Context context, boolean flag) {
        SwitchSPManager.setMicrophone(context, flag);
        SettingManager.INSTANCE.setAudioAmplifier(flag);
    }

    public static boolean getAudioAmplifier(Context context) {
        return SwitchSPManager.getMicrophone(context);
    }

    /**
     * 设置是否开启增强对讲音质
     * @param mode
     */
    public static void setIncreaseIntercomQuailty(Context context, boolean flag, int mode) {
        SwitchSPManager.setIncreaseIntercomQuailty(context, flag);
        SettingManager.INSTANCE.setSessionAudioQuailty(mode);
    }

    public static boolean getIncreaseIntercomQuailty(Context context) {
        return SwitchSPManager.getIncreaseIntercomQuality(context);
    }

    /**
     * 设置是否开启静音模式
     * @param flag
     */
    public static void setMuteMode(Context context, boolean flag) {
        SwitchSPManager.setMuteMode(context, flag);
        SettingManager.INSTANCE.setMuteMode(flag);
    }

    public static boolean getMuteMode(Context context) {
        return SwitchSPManager.getMuteMode(context);
    }
    /**
     * 设置应答模式
     *
     * @param mode:AirSession.INCOMING_MODE_AUTO AirSession.INCOMING_MODE_MANUALLY
     */
    public static void setAnswerMode(Context context, boolean flag, int mode) {
        SwitchSPManager.setAutoAnswer(context, flag);
        SettingManager.INSTANCE.setSessionAutoAnswerMode(mode);
    }

    public static boolean getAnswerMode(Context context) {
        return SwitchSPManager.getAutoAnswer(context);
    }

    /**
     * 设置免打扰模式
     *
     * @param status
     */
    public static void setNotDisturbMode(Context context, boolean status) {
        SwitchSPManager.setNotDisturb(context, status);
        SettingManager.INSTANCE.setSessionSilentMode(status);
    }

    public static boolean getNotDisturbMode(Context context) {
        return SwitchSPManager.getNotDisturb(context);
    }

    /**
     * 设置心跳频度
     * @param seconds
     */
    public static boolean setHeartBeatFrequency(Context context, int seconds) {
        SwitchSPManager.setHeartBeatFrequency(context, seconds);
        return SettingManager.INSTANCE.setHeartBeatFrequency(seconds);
    }

    public static int getHeartBeatFrequency(Context context) {
        return SwitchSPManager.getHeartBeatFrequency(context);
    }

    /**
     * 设置是否开启新消息提醒
     * @param context
     * @param flag
     */
    public static void setReceiveNewMsg(Context context, boolean flag) {
        SwitchSPManager.setIsNewMsgNotice(context, flag);
    }

    public static boolean getReceiveNewMsg(Context context) {
        return SwitchSPManager.getIsNewMsgNotice(context);
    }

    /**
     * 设置是否开启通知
     * @param context
     * @param flag
     */
    public static void setOpenNotice(Context context, boolean flag) {
        SwitchSPManager.setIsNotification(context, flag);
    }

    public static boolean getOpenNotice(Context context) {
        return SwitchSPManager.getIsNotification(context);
    }

    /**
     * 设置是否开启震动
     * @param context
     * @param flag
     */
    public static void setUseVibrate(Context context, boolean flag) {
        SwitchSPManager.setIsVibrate(context, flag);
    }

    public static boolean getUseVibrate(Context context) {
        return SwitchSPManager.getIsVibrate(context);
    }

    /**
     * 设置是否开启音量下键作为对讲键
     * @param context
     * @param flag
     */
    public static void setUseVolumeKey(Context context, boolean flag) {
        SwitchSPManager.setVolumeKey(context, flag);
    }

    public static boolean getUseVolumeKey(Context context) {
        return SwitchSPManager.getVolumeKey(context);
    }

    /**
     * 设置是否开启屏幕ptt按键
     * @param context
     * @param flag
     */
    public static void setUseScreenPttKey(Context context, boolean flag) {
        SwitchSPManager.setScreenPttKey(context, flag);
    }

    public static boolean getUseScreenPttKey(Context context) {
        return SwitchSPManager.getScreenPttKey(context);
    }

    /**
     * 是否启用大字体
     * @param context
     * @param flag
     */
    public static void setUseLargeFont(Context context, boolean flag) {
        SwitchSPManager.setUseLargeFont(context, flag);
    }

    public static boolean getUseLargeFont(Context context) {
        return SwitchSPManager.getUseLargeFont(context);
    }

    /**
     * 是否开启卫星定位模式
     * @param context
     * @param flag
     */
    public static void setUseSatelliteLocate(Context context, boolean flag) {
        SwitchSPManager.setIsSatelliteLocate(context, flag);
    }

    public static boolean getUseSatelliteLocate(Context context) {
        return SwitchSPManager.getIsSatelliteLocate(context);
    }

    /**
     * 图片上传设置
     * @param context
     * @param size
     */
    public static void setUploadPictureSize(Context context, int size) {
        SwitchSPManager.setUploadPictureSize(context, size);
    }

    public static int getUploadPictureSize(Context context) {
        return SwitchSPManager.getUploadPictureSize(context);
    }

    /**
     * 非wifi环境，下载时提示用户
     * @param context
     * @param flag
     */
    public static void setNoticeUserNoWifi(Context context, boolean flag) {
        SwitchSPManager.setNoticeUserNoWifi(context, flag);
    }

    public static boolean getNoticeUserNoWifi(Context context) {
        return SwitchSPManager.getNoticeUserNoWifi(context);
    }

    /**
     * 是否开启自动下载
     * @param context
     * @param flag
     */
    public static void setAutoDownload(Context context, boolean flag) {
        SwitchSPManager.setAutoDownload(context, flag);
    }

    public static boolean getAutoDownload(Context context) {
        return SwitchSPManager.getAutoDownload(context);
    }


}
