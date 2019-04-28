
package com.ctchat.sample.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.ctchat.sample.tool.media.MediaSound;
import com.ctchat.sdk.basemodule.logger.Logger;
import com.ptt.sdk.PttAccount;

/**
 * 网络状态变化广播
 */
public class ConnectionChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "ConnectionChangeReceive";
    public static final String ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    public static NetworkInfo activeNetInfo;
    private ConnectivityManager connectivityManager = null;
    private int ConnectionType = -1;
    private PttAccount handleAccount = PttAccount.getInstance();

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.e(TAG ,"ReceiverConnectionChange onReceive Action = " + intent.getAction());
        if (intent.getAction().equals(ACTION)) {
            if (connectivityManager == null) {
                connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            }
            activeNetInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetInfo != null) {
                if (activeNetInfo.getState() != NetworkInfo.State.CONNECTED) {
                    Logger.d(TAG ,"ConnectionChangeReceiver DISCONNECTED");
                    closeNetwork(context);
                } else {
                    Logger.i(TAG ,"ConnectionChangeReceiver  CONNECTED");
                    if (ConnectionType != -1 && ConnectionType != activeNetInfo.getType()) {
                        Log.i(ConnectionChangeReceiver.class.getName(), "ConnectionType changed!!!!");
//                        closeNetwork(context);
                    }
                    handleAccount.NetworkOpen();
                    ConnectionType = activeNetInfo.getType();
                }
            } else {
                Logger.i(TAG ,"ConnectionChangeReceiver  activeNetInfo == null !!");
                closeNetwork(context);
            }
        }
    }

    private void closeNetwork(Context context) {
        handleAccount.NetworkClose();
        MediaSound.resetState(context);
    }

}