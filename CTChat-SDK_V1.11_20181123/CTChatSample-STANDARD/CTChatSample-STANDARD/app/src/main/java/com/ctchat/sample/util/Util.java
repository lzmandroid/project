package com.ctchat.sample.util;


import android.os.Environment;

import java.io.File;

public class Util {
    public static final int WC_MSG = 1000;
    public static final String APP_BUGLY_ID = "ab4383e19c"; //如果配置了上传符号表需要同步设置build.gradle中的appid

    public static final int UPLOAD_PICTURE = 1;
    public static final int UPLOAD_VIDEO = 2;
    public static final int UPLOAD_MDM = 91;
    public static final String KEY_UPLOAD_THEME_LIST = "upload_themelist";
    public final static int MSG_UPDATE_THEME_SUCCEEDED = WC_MSG + 38;
    public final static int MSG_UPDATE_THEME_ERROR = WC_MSG + 39;
    public static final int AUDIO_VIRTUALLIZE_SPECTRUM_NUM = 18;  //音频频谱图个数

    public static final int MESSAGE_UPDATE_NUM = 5;

    public static final String SYS_PATH_IMG = Environment.getExternalStorageDirectory() + File.separator + "com.ptt/image/";
    public static final String SYS_PATH_VIDEO = Environment.getExternalStorageDirectory() + File.separator + "com.ptt/videocache/";
    public static final String ACTION_NOTIFICATION_CLICK = "com.ctchat.sample.notification.click";
    public static final String ACTION_BROADCAST_UPDATE = "com.ctchat.sample.broacast.update";
    public static final int UPDATE_TIMER = 1;


    public final static int BROADCAST_CMD_INCOMEMSG = 1;
    public final static int BROADCAST_CMD_SYSTEMNOTICE = 2;
    public final static int BROADCAST_CMD_GROUP_CALL = 3;

    public static final int FRAGMENT_INTERCOM_LIST = 0;//会话列表Fragment
    public static final int FRAGMENT_INTERCOM_PTT = 1;//对讲界面Fragment
    public static final int FRAGMENT_INTERCOM_IM = 2;//即时消息Fragment

    public static final int PHOTO_REQUEST_GALLERY = 1011;// 从相册中选择
    public static final int PHOTO_REQUEST_SHOOTING = 1012;// 拍照返回
    public static final int PHOTO_REQUEST_VIDEO = 1013;// 录像返回
    public static final int TAKE_LOCATION = 1014;//位置返回

    public static final int UPDATE_HISTORY_MESSAGES = 3001;// 更新历史消息

    public static final int VIDEO_SECTION_TIME_MAX = 30;

    public static final int SUB_RECORDS_ALL = 0;//全部记录
    public static final int SUB_RECORDS_CALLING = 1;//正在通话
    public static final int SUB_RECORDS_UNANSWER = 2;//未接记录
    public static final int SUB_RECORDS_OVER = 3;//已结束记录

    public static final String REPORT_TYPE = "reportType";
    public static final String REPORT_NORMAL = "isNormalReport";

    public static final int DIALOG_SESSION_MENBER_UPPER = 29;

    //位置相关
    public static final String LATITUDE = "latitude";
    public static final String LONGTITUDE = "longtitude";
    public static final String ADDRESS = "address";

}
