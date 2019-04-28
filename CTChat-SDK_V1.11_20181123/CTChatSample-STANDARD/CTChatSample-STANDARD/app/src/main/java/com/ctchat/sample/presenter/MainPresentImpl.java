package com.ctchat.sample.presenter;

import android.content.Context;

import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sdk.ptt.tool.entity.MessageEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;
import com.ctchat.sample.model.IntercomImModel;
import com.ctchat.sample.model.IntercomImModelImpl;
import com.ctchat.sample.model.IntercomModel;
import com.ctchat.sample.model.IntercomModelImpl;
import com.ctchat.sample.model.MainModelImpl;
import com.ctchat.sample.model.MainModle;
import com.ctchat.sample.view.MainView;

import java.util.ArrayList;
import java.util.List;

public class MainPresentImpl implements IntercomSessionListener, MediaListener, MainPresent, IMListener {
    private MainView mView;
    private IntercomModel mModel;
    private IntercomImModel imModel;
    private MainModle mainModle;
    private static final String TAG = "MainPresentImpl";

    public MainPresentImpl(MainView view, Context context) {
        mView = view;
        mModel = new IntercomModelImpl(this);
        mModel.registerMediaListener(this);
        mainModle = new MainModelImpl(this, context);
        imModel = new IntercomImModelImpl(this);
    }


    /***************** MainPresent ***********************/
    @Override
    public void onPause() {
        mainModle.onPause();

        mModel.onPause();
        mModel.unregisterMediaListener();

        imModel.onPause();
    }

    @Override
    public void onResume() {
        mainModle.onResume();

        mModel.onResume();
        mModel.registerMediaListener(this);

        imModel.onResume();
    }

    @Override
    public void onPushBroadcastMsg() {
        mView.onPushBroadcastMsg();
    }

    /***************** SessionEvent ***********************/
    @Override
    public void onSessionOutgoingRinging(SessionEntity session) {
        Logger.d(TAG, "onSessionOutgoingRinging");
    }

    @Override
    public void onSessionEstablishing(SessionEntity session) {
        Logger.d(TAG, "onSessionEstablishing");
    }

    @Override
    public void onSessionEstablished(SessionEntity session, int ret) {
        Logger.d(TAG, "onSessionEstablished");
        if (mView != null){
            mView.onSessionEstablished(session,ret);
        }
    }

    @Override
    public void onSessionReleased(SessionEntity session, int reason) {
        Logger.d(TAG, "onSessionReleased");
        if (mView != null) {
            mView.onSessionReleased(session, reason);
        }
    }

    @Override
    public void onSessionPresence(SessionEntity session, List<ContactEntity> membersAll, List<ContactEntity> membersPresence) {
        Logger.d(TAG, "onSessionPresence");
        if(mView !=  null){
            mView.onSessionPresence(session,membersAll,membersPresence);
        }
    }

    @Override
    public void onSessionMemberUpdate(SessionEntity session, List<ContactEntity> list, boolean b) {
        Logger.d(TAG, "onSessionMemberUpdate");
        if(mView != null){
            mView.onSessionMemberUpdate(session,list,b);
        }
    }

    @Override
    public void onSessionCenterCallRet(boolean ret, SessionEntity sessionEntity) {

    }

    @Override
    public void onJoinCallTimeOut() {

    }

    /****************** Media Event *********************/

    @Override
    public void onTalkPreparing(SessionEntity session) {

    }

    @Override
    public void onTalk(SessionEntity session) {
        mView.refreshTalkStatus(session);
    }

    @Override
    public void onTalkEnd(SessionEntity session, int i) {
        mView.refreshTalkStatus(session);
    }

    @Override
    public void onListen(SessionEntity session, ContactEntity airContact) {
        mView.refreshTalkStatus(session);
    }

    @Override
    public void onListenEnd(SessionEntity session) {
        mView.refreshTalkStatus(session);
    }

    @Override
    public void onListenVoice(SessionEntity session) {

    }

    @Override
    public void onMediaQueue(SessionEntity session, ArrayList<ContactEntity> arrayList) {

    }

    @Override
    public void onMediaQueueIn(SessionEntity session) {

    }

    @Override
    public void onMediaQueueOut(SessionEntity session) {

    }

    @Override
    public void updateAudioWave(byte[] bytes) {

    }



    /****************** Message Event *********************/

    @Override
    public void onMessageIncomingRecv(MessageEntity message) {
        mView.undateUnreadCount();
    }

    @Override
    public void onMessageIncomingRecv(List<MessageEntity> list) {
        mView.undateUnreadCount();
    }

    @Override
    public void onMessageOutgoingSent(MessageEntity message) {

    }

    @Override
    public void onMessageUpdated(MessageEntity message) {

    }

    @Override
    public void onMessageRecordStart() {

    }

    @Override
    public void onMessageRecordStop(int seconds, String msgCode) {

    }

    @Override
    public void onMessageRecordTransfered(String msgCode, String resId) {

    }

    @Override
    public void onMessageRecordPlayLoading(String msgCode, String resId) {

    }

    @Override
    public void onMessageRecordPlayLoaded(boolean isOk, String code, String resId, byte[] resBytes) {

    }

    @Override
    public void onMessageRecordPlayStart(String msgCode, String resId) {

    }

    @Override
    public void onMessageRecordPlayStop(String msgCode, String resId) {

    }

    @Override
    public void onMessageListLoad(String s, List<MessageEntity> messageEntityList) {

    }

    @Override
    public void onMessagePttRecord(SessionEntity session, MessageEntity message, String msgCode, String resId) {

    }
}
