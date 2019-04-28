package com.ctchat.sample.receiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.ctchat.sample.login.util.SimUtil;

public class SimStateReceiver extends BroadcastReceiver {
    private final static String TAG = "SimStateReceiver";
    private final static String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    private final static int SIM_VALID = 0;
    private final static int SIM_INVALID = 1;
    private int simState = SIM_INVALID;
    private OnFinishSimDetectListener listener;

    public int getSimState() {
        return simState;
    }

    public void setOnFinishSimDetectListener(OnFinishSimDetectListener listener) {
        this.listener = listener;
    }
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "onReceive: sim state changed");
        Log.d(TAG, "onReceive: intent.getAction() = " + intent.getAction());
        if (intent.getAction().equals(ACTION_SIM_STATE_CHANGED)) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
            int state = tm.getSimState();
            Log.d(TAG, "onReceive: state = " + state);
            switch (state) {
                case TelephonyManager.SIM_STATE_READY: // 有卡，已经准备好
                    simState = SIM_VALID;
                    if (listener != null && !TextUtils.isEmpty(SimUtil.getIccid(context))) {
                        listener.onFinishSimDetect(true);
                    }
                    break;
                case TelephonyManager.SIM_STATE_NOT_READY: // 有卡，但是没有准备好
                case TelephonyManager.SIM_STATE_UNKNOWN:

                    break;
                case TelephonyManager.SIM_STATE_ABSENT:
                case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                default:
                    simState = SIM_INVALID;
                    if (listener != null) {
                        listener.onFinishSimDetect(false);
                    }
                    break;
            }
        }
    }

    public interface OnFinishSimDetectListener {
        void onFinishSimDetect(boolean simInvalid);
    }
}