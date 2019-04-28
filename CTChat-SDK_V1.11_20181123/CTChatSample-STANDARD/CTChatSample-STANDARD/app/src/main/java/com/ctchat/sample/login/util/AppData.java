package com.ctchat.sample.login.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

public class AppData {
    private static final String TAG = "AppData";

    public static void clearData(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Log.d(TAG, "Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT");
            ((ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE))
                    .clearApplicationUserData(); // note: it has a return value!
        } else {
            Log.d(TAG, "Build.VERSION.SDK_INT = " + Build.VERSION.SDK_INT);
        }
    }
}
