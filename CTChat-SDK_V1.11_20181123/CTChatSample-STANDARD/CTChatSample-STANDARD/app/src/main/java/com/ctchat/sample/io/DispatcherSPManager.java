package com.ctchat.sample.io;

import android.content.Context;

import com.ctchat.sdk.basemodule.util.PrefUtil;

/**
 * 相关数据存储
 */
public class DispatcherSPManager {
    public static final String UPDATE_TIME = "dispatcher_updateTime";

    public static void setUpdateTime(Context context, String updateTime) {
        PrefUtil.put(context, UPDATE_TIME, updateTime);
    }

    public static String getUpdateTime(Context context) {
        return (String) PrefUtil.get(context, UPDATE_TIME, "");
    }

}
