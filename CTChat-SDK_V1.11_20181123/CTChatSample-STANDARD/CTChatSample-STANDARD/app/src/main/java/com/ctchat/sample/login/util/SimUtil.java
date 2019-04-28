package com.ctchat.sample.login.util;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.ctchat.sdk.basemodule.logger.Logger;

public class SimUtil {

    private static final String TAG = "SimUtil";

    public static String getIccid(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String iccid = telephonyManager.getSimSerialNumber();
        Logger.d(TAG, "getIccid: iccid:" + iccid);
        return iccid;
    }
}
