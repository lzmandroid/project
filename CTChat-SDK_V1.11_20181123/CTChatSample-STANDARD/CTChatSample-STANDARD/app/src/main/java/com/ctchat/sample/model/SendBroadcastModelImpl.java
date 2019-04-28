package com.ctchat.sample.model;

import android.text.TextUtils;

import com.ctchat.sample.presenter.GroupBroadcastListener;
import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;
import com.ctchat.sdk.ptt.tool.intercom.IntercomManager;
import com.ctchat.sdk.ptt.tool.intercom.OnMediaEventListener;

import java.util.ArrayList;


public class SendBroadcastModelImpl implements SendBroadcastModel, OnMediaEventListener {
    private static final String TAG = "SendBroadcastModelImpl";

    private SessionEntity sessionEntity;
    private GroupBroadcastListener groupBroadcastListener;

    public SendBroadcastModelImpl(GroupBroadcastListener listener){
        groupBroadcastListener = listener;
    }

    /**
     * 注册话权事件监听
     */
    public void registerGroupBroadcastListener(){
        IntercomManager.INSTANCE.registerMediaEventListener(this);
    }

    public void unregisterGroupBroadcastListener(){
        IntercomManager.INSTANCE.unRegisterMediaEventListener();
    }

    @Override
    public void startSendBroadcast() {
        if (sessionEntity == null) {
            getBroadcastSession();
        }
        IntercomManager.INSTANCE.requestTalk(sessionEntity);
    }

    @Override
    public void stopSendBroadcast() {
        if (sessionEntity == null) {
            getBroadcastSession();
        }
        IntercomManager.INSTANCE.releaseTalk(sessionEntity);
    }

    @Override
    public SessionEntity getBroadcastSession() {
        this.sessionEntity = IntercomManager.INSTANCE.getGroupBroadcastSession();
        Logger.i(TAG,"BroadCast Session Code:"+sessionEntity.getSessionId());
        return this.sessionEntity;
    }

    @Override
    public void onMediaStateTalkPreparing(SessionEntity session) {

    }

    @Override
    public void onMediaStateTalk(SessionEntity session) {
        if (TextUtils.equals(sessionEntity.getSessionId(), session.getSessionId())){
            Logger.i(TAG," Group Session Call Talk begin");
            if (groupBroadcastListener != null){
                groupBroadcastListener.notifyBroadcastState(true);
            }
        }
    }

    @Override
    public void onMediaStateTalkEnd(SessionEntity session, int reason) {
        if (TextUtils.equals(sessionEntity.getSessionId(), session.getSessionId())) {
            Logger.i(TAG," Group Session Call Talk End:"+reason);
            if (groupBroadcastListener != null){
                groupBroadcastListener.notifyBroadcastReason(reason);
            }
        }
    }

    @Override
    public void onMediaStateListen(SessionEntity session, ContactEntity airContact) {

    }

    @Override
    public void onMediaStateListenEnd(SessionEntity session) {

    }

    @Override
    public void onMediaStateListenVoice(SessionEntity sessionEntity) {

    }

}
