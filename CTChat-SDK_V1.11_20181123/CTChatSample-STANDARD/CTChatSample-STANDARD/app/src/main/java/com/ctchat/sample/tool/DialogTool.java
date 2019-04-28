package com.ctchat.sample.tool;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.ctchat.sample.R;
import com.ctchat.sample.io.Setting;
import com.ctchat.sample.util.NetUtil;
import com.ctchat.sdk.ptt.tool.entity.BroadcastEntity;

public class DialogTool {
    /**
     * Dialog （确定/取消） 点击事件回调
     */
    public interface DialogClickCallBack {
        /**
         * 积极回调
         */
        void positiveButtonClick(Object obj);

        /**
         * 取消回调
         */
        void neutralButtonClick(Object obj);
    }

    public interface HintMenuDialogClickCallBack {
        void OnMenuClickListener(int clickId);
    }

    public interface NetworkDialogClickCallBack {

        /**
         * 不弹提醒时回调
         */
        void noDialog();

        /**
         * 网络未连接提示的积极回调
         */
        void networkPositiveButtonClick();

        /**
         * 网络未连接提示的取消回调
         */
        void networkNeutralButtonClick();

        /**
         * wifi未连接提示的积极回调
         */
        void wifiPositiveButtonClick();

        /**
         * wifi未连接提示的取消回调
         */
        void wifiNeutralButtonClick();
    }

    /**
     * 网络检查
     */
    public static void networkCheck(Context context, NetworkDialogClickCallBack callBack) {
        if (NetUtil.isNetConnected(context)) { //网络已连接
            if (Setting.getNoticeUserNoWifi(context)) { // 通知开关是否打开
                if (!NetUtil.isWifiConnected(context)) { // wifi未连接
                    // 弹出wifi未连接提示
                    createWifiUnunited(context, callBack);
                    return;
                }
            }
            callBack.noDialog();
        } else {
            // 弹出网络未连接提示
            createNetWorkAlertDialog(context, callBack);
        }
    }

    /**
     * 创建网络状态提醒Dialog
     *
     * @param context
     * @param callBack
     */
    public static void createNetWorkAlertDialog(Context context, final NetworkDialogClickCallBack callBack) {
        AlertDialog.Builder networkBuilder = new AlertDialog.Builder(context);
        networkBuilder.setMessage(R.string.txt_no_valid_network);
        networkBuilder.setPositiveButton(R.string.action_settings,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        // TODO 设置网络
                        callBack.networkPositiveButtonClick();
                    }
                });
        networkBuilder.setNeutralButton(R.string.txt_cancel,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        callBack.networkNeutralButtonClick();
                    }
                });
        networkBuilder.show();
    }

    /**
     * 弹出wifi未连接提示
     */
    public static void createWifiUnunited(Context context, final NetworkDialogClickCallBack callBack) {
        AlertDialog.Builder networkBuilder = new AlertDialog.Builder(context);
        networkBuilder.setMessage(R.string.txt_no_valid_wifi_tip);
        networkBuilder.setPositiveButton(R.string.txt_ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        callBack.wifiPositiveButtonClick();
                    }
                });
        networkBuilder.setNeutralButton(R.string.txt_cancel,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        callBack.wifiNeutralButtonClick();
                    }
                });
        networkBuilder.show();
    }

    public static void createBroadcastNoticeDialog(Context context, BroadcastEntity broadcast, final DialogClickCallBack callBack) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(broadcast.getTitle());
        builder.setMessage(broadcast.getContent());
        builder.setPositiveButton(R.string.txt_ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        callBack.positiveButtonClick(null);
                    }
                });
        builder.show();
    }

}
