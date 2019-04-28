package com.ctchat.sample.login.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 缓存登录信息
 */
public class PreferLogin {

    private final static String NAME = "pref_login";
    private static final String KEY_ICCID = "iccid";
    private static final String KEY_HAS_LOGIN = "hasLogin";

    public static void putIccid(Context context, String iccid) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ICCID, iccid);
        editor.commit();
    }

    public static String getIccid(Context context, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_ICCID, defaultValue);
    }

    public static void putHasLogin(Context context, boolean hasLogin) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_HAS_LOGIN, hasLogin);
        editor.commit();
    }

    public static boolean hasLogin(Context context, boolean defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_HAS_LOGIN, defaultValue);
    }

    public static void clearData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }
}
