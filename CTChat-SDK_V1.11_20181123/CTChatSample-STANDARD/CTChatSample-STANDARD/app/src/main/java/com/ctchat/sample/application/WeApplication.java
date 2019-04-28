package com.ctchat.sample.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.ctchat.sample.BuildConfig;
import com.ctchat.sample.DisFuncChangeAlertActivity;
import com.ctchat.sample.IntercomActivity;
import com.ctchat.sample.IntercomingAlertActivity;
import com.ctchat.sample.MainActivity;
import com.ctchat.sample.R;
import com.ctchat.sample.RemoteLoginAlertActivity;
import com.ctchat.sample.UpdateApplicationActivity;
import com.ctchat.sample.io.Setting;
import com.ctchat.sample.login.util.PreferLogin;
import com.ctchat.sample.tool.MyActivityManager;
import com.ctchat.sample.tool.Sound;
import com.ctchat.sample.tool.incoming.SessionInitiationMan;
import com.ctchat.sdk.AccountManager;
import com.ctchat.sdk.DispatcherManager;
import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sdk.basemodule.util.ToastUtil;
import com.ctchat.sdk.ptt.tool.MessagePush.OnPushEventListener;
import com.ctchat.sdk.ptt.tool.MessagePush.PushManager;
import com.ctchat.sdk.ptt.tool.chatIM.IMManager;
import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;
import com.ctchat.sdk.ptt.tool.incoming.OnSessionIncomingListener;
import com.ctchat.sdk.ptt.tool.intercom.IntercomManager;
import com.ctchat.sdk.ptt.tool.versionUpdate.OnVersionCheckUpdateListener;
import com.ctchat.sdk.ptt.tool.versionUpdate.VersionUpdateManager;

import org.greenrobot.eventbus.EventBus;

/**
 * BaseApplication
 */
public class WeApplication extends Application implements OnPushEventListener, OnVersionCheckUpdateListener, OnAppInitListener, OnSessionIncomingListener {
    private static final String TAG = "WeApplication";
    private static final String TAG_ACCOUNT_CTRL = "AccountControl";

    private static final String SERVER_ADDRESS = BuildConfig.server;

    private static WeApplication mApplication;
    private int appCount = 0;
    public static final String UPDATE_MESSAGE = "updateMessage";
    private static boolean incomingFlag = false;

    @Override
    public void onCreate() {
        super.onCreate();
        if (mApplication == null) {
            mApplication = this;
        }
        AccountManager.setServerAddr(SERVER_ADDRESS);
        AccountManager.setAppKey("d9021393d1bc4275bd5a169812348ca4");
        AccountManager.setAudioPlayVisualize(true);
        AccountManager.setAudioRecordVisualize(true);
        AccountManager.setLogPrintMode(AccountManager.TRACE_MODE_LOGCAT);
        AccountManager.setEnableLog(true);
        AccountManager.setImsi(null, "null");
//        AccountManager.setAudioRecordSource("123");
//        AccountManager.setStreamType("123");
//        AccountManager.setAudioVisualizeNum("123");
        //注册消息推送事件监听
        PushManager.INSTANCE.registerPushEventListener(this);
        VersionUpdateManager.getInstance().registerVersionUpdateListener(this);

        AppInitializeManager.appInit(mApplication, this);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                MyActivityManager.getInstance().addActivity(activity);
                activity.setVolumeControlStream(Setting.getStreamType());
            }

            @Override
            public void onActivityStarted(Activity activity) {
                appCount++;
            }

            @Override
            public void onActivityResumed(Activity activity) {
                MyActivityManager.getInstance().setCurrentActivity(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
                appCount--;
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                MyActivityManager.getInstance().removeActivity(activity);
            }
        });

        IntercomManager.INSTANCE.registerSessionIncomingListener(this);

    }

    public int getAppCount() {
        return appCount;
    }

    public static Context getInstance() {

        if (mApplication == null) {
            return AppInitializeManager.getmApplicationContext();
        }

        return mApplication;
    }

    @Override
    public void onTerminate() {
        Logger.e(TAG, "TAT!!!! APP TERMINATED!!!!!!!", true);
        super.onTerminate();
        PushManager.INSTANCE.unRegisterPushEventListener();
        VersionUpdateManager.getInstance().unregisterVersionUpdateListener();
    }

    /**
     * 即时聊天消息推送
     *
     * @param sessionCode
     */
    @Override
    public void onPushIMMsg(String sessionCode) {
    }

    /**
     * 系统广播消息推送
     *
     * @param content
     */
    @Override
    public void onPushBroadcastMsg(String content) {
    }

    @Override
    public void onPushGisEvent() {
    }


    /**
     * 异地登入,弹框提醒
     */
    @Override
    public void onPushRemoteLogin() {
        Logger.i(TAG, "LoginModuleTool remoteLogin");
        Intent intent = new Intent(mApplication, RemoteLoginAlertActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onHeartbeat(int i) {
        Logger.i(TAG, "onHeartbeat "+i);
    }

    @Override
    public void onPushSystemUserCtrl(int cmd) {
        Logger.i(TAG, "Push System User Control " + cmd);
        Intent intent = new Intent();
        intent.setClassName(getPackageName(), RemoteLoginAlertActivity.class.getName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(TAG_ACCOUNT_CTRL, PushManager.AccountCtrlCmdEnum.toAccountCtrlCmd(cmd));
        startActivity(intent);
    }

    @Override
    public void onPushSystemCallLimit(int i, String s) {
        int message = 0;
        switch (i) {
            case PushManager.TEMPORARY_CALL_RESTRICTION_IN:
                message = R.string.talk_system_limit_call_in;
                break;
            case PushManager.TEMPORARY_CALL_RESTRICTION_OUT:
                message = R.string.talk_system_limit_call_out;
                break;
            case PushManager.CHANNEL_CALL_RESTRICTION_IN:
                message = R.string.talk_system_limit_channel;
                break;
            case PushManager.RELIEVE_TEMPORARY_CALL_RESTRICTION_IN:
                message = R.string.relieve_talk_system_limit_call_in;
                break;
            case PushManager.RELIEVE_TEMPORARY_CALL_RESTRICTION_OUT:
                message = R.string.relieve_talk_system_limit_call_out;
                break;
            case PushManager.RELIEVE_CHANNEL_CALL_RESTRICTION_IN:
                message = R.string.relieve_talk_system_limit_channel;
                break;
        }
        ToastUtil.shortShow(getApplicationContext(), message);
    }
    /**
     * 指定预定义组被限制/恢复
     *
     * @param channelID
     * @param channelName
     * @param operation
     */
    @Override
    public void onPushChannelLimit(String channelID, String channelName, int operation) {
        Logger.i(TAG, "on PUsh channel id [" + channelID + "] name:" + channelName + "  operation:" + operation);
        int message = 0;
        switch (operation) {
            case PushManager.CHANNEL_TRUN_ON:
                message = R.string.talk_channel_tip_turn_on;
                break;
            case PushManager.CHANNEL_TURN_OFF:
                message = R.string.talk_channel_tip_turn_off;
                break;
            default:
                break;
        }

        Activity activity = MyActivityManager.getInstance().getCurrentActivity();
        boolean isMainActivity = MainActivity.class.isInstance(activity);
        //(缓存数据变更，需要刷新界面)
        if (isMainActivity){
            ((MainActivity) activity).refreshChannelFragmentList();
        }else {
            boolean isIntercomActivity = IntercomActivity.class.isInstance(activity);
            if (isIntercomActivity){
                ((IntercomActivity)activity).onChannelTurnOff(channelID);
            }
        }

        String msgStr = getApplicationContext().getString(message);
        ToastUtil.longShow(getApplicationContext(), String.format(msgStr, channelName));
    }

    @Override
    public void onPushLiveStreamStart(int liveId, String liveServer, String liveName, boolean forceLiveStream,String cameraType,String resolution) {
    }

    @Override
    public void onPushLiveStreamEnd() {
    }

    @Override
    public void onPushChangeLiveCamera(String liveName, String camera) {
    }

    @Override
    public void onPushChannelAlert(String channelID, String channelName, String initiator) {
        Toast.makeText(this,"预定义组["+ channelName +"]接收到群呼上线通知",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPushDispatcherFunctionChange() {
        Logger.i(TAG, "onPushDispatcherFunctionChange" + DispatcherManager.getInstance().getDispatcherFunctions(this));
        Intent intent = new Intent(mApplication, DisFuncChangeAlertActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * 获取程序版本号
     */
    public String getAppVersion() {
        return "V";
    }

    /**
     * 退出应用程序(主动点击的触发事件)
     */
    @Override
    public void exitApplication() {
        Logger.i(TAG, "Exit App Begin");
        MyActivityManager.getInstance().finishAllActivity();
        PreferLogin.clearData(this);
        AccountManager.clearLoginData(this);
        System.exit(0);
    }

    @Override
    public void onForceUpdate(VersionUpdateManager.VersionEntity versionEntity) {
        Intent intent = new Intent(mApplication, UpdateApplicationActivity.class);
        intent.putExtra(UPDATE_MESSAGE, versionEntity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onNoticeUpdate(VersionUpdateManager.VersionEntity versionEntity) {
        Intent intent = new Intent(mApplication, UpdateApplicationActivity.class);
        intent.putExtra(UPDATE_MESSAGE, versionEntity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    /**
     * 判断当前是否已有电话呼入
     *
     * @return
     */
    public static boolean getIncomingFlag() {
        return incomingFlag;
    }

    public static void setIncomingFlag(boolean flag) {
        incomingFlag = flag;
    }

    @Override
    public void onSessionIncomingAlertStart(SessionEntity session, ContactEntity caller, boolean isAccepted) {

        if (getIncomingFlag() || IntercomManager.INSTANCE.hasSessionDialoging()) {
            IntercomManager.INSTANCE.rejectSessionCall(session, true);
        } else {
            Logger.i(TAG, "OnSessionIncomingListener onSessionIncomingAlertStart");

            //停止播放录音文件
            IMManager.getInstance().stopPlayRecordMessage();

            if (Setting.getAnswerMode(getApplicationContext())) {
                Logger.i(TAG, "force incoming");
                //未被接听，改为接听并跳转
                if (!isAccepted) {
                    IntercomManager.INSTANCE.acceptSessionCall(session);
                }
                autoAnswerIncomingCall(getApplicationContext(), session);
            } else {
                Sound.playSound(Sound.PLAYER_INCOMING_RING, true, getApplicationContext());
                showSessionIncomingAlert(getApplicationContext(), session, caller);
            }
        }
    }

    /**
     * 接听当前来电弹窗
     *
     * @param context
     * @param session
     */
    public void showSessionIncomingAlert(
            final Context context, final SessionEntity session, final ContactEntity airContact) {

        incomingFlag = true;

        Intent intent = new Intent(getApplicationContext(), IntercomingAlertActivity.class);
        //intent.setClassName(context.getPackageName(), Setting.intercomingAlretActivity);
        SessionInitiationMan.SessionInitiation sessionInitiation = new SessionInitiationMan.SessionInitiation();
        sessionInitiation.sessionCode = session.getSessionId();
        sessionInitiation.connectionStatus = SessionInitiationMan.SESSION_UNCONNECTION;
        sessionInitiation.initializationMode = SessionInitiationMan.INITIATION_PTT;
        intent.putExtra(SessionInitiationMan.INTENT_DATA_KEY, sessionInitiation);
        intent.putExtra("airContact", airContact);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 设置自动应答时 自动接听跳转activity
     *
     * @param context
     * @param session
     */
    public void autoAnswerIncomingCall(final Context context, final SessionEntity session) {
        incomingFlag = false;
        Intent intent = new Intent(getApplicationContext(), IntercomActivity.class);
        //intent.setClassName(context.getPackageName(), Setting.interComActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        SessionInitiationMan.SessionInitiation sessionInitiation = new SessionInitiationMan.SessionInitiation();
        sessionInitiation.sessionCode = session.getSessionId();
        sessionInitiation.connectionStatus = SessionInitiationMan.SESSION_UNCONNECTION;
        sessionInitiation.initializationMode = SessionInitiationMan.INITIATION_PTT;
        intent.putExtra(SessionInitiationMan.INTENT_DATA_KEY, sessionInitiation);

        context.startActivity(intent);
    }

    @Override
    public void onSessionIncomingAlertStop(SessionEntity session) {
        incomingFlag = false;
        Sound.stopSound(Sound.PLAYER_INCOMING_RING);
        EventBus.getDefault().post(new IntercomingAlertActivity.EBAlertEnd());
    }
}
