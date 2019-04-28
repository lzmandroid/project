package com.ctchat.sample.presenter;

import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;
import com.ctchat.sample.model.IntercomModel;
import com.ctchat.sample.model.IntercomModelImpl;
import com.ctchat.sample.view.IntercomView;

import java.util.ArrayList;
import java.util.List;

public class IntercomPresentImpl implements IntercomPresent, IntercomSessionListener, MediaListener {
    private IntercomView view;
    private IntercomModel model;

    public IntercomPresentImpl(IntercomView view) {
        this.view = view;
        model = new IntercomModelImpl(this);
    }

    @Override
    public void onPause() {
        model.onPause();

    }

    @Override
    public void onResume() {
        model.onResume();

    }

    @Override
    public void onDestroy() {
        model.onDestroy();
    }

    @Override
    public void startJoinCallTimer(String caller, SessionEntity newSessionEntity, SessionEntity oldSessionEntity, long delayTime) {
        model.startJoinCallTimer(caller,newSessionEntity,oldSessionEntity,delayTime);
    }

    @Override
    public void releaseJoinCallTimer() {
        model.releaseJoinCallTimer();
    }


    @Override
    public void onSessionOutgoingRinging(SessionEntity session) {
        view.sessionOutgoingRingingStatus(session);
    }

    @Override
    public void onSessionEstablishing(SessionEntity session) {
        view.sessionEstablishingStatus(session);
    }

    @Override
    public void onSessionEstablished(SessionEntity session, int ret) {
        view.sessionEstablishedStatus(session, ret);
    }

    @Override
    public void onSessionReleased(SessionEntity session, int reason) {
        view.sessionReleasedStatus(session, reason);
    }

    @Override
    public void onSessionPresence(SessionEntity session, List<ContactEntity> membersAll, List<ContactEntity> membersPresence) {
        view.sessionPresenceStatus(session, membersAll, membersPresence);
    }

    @Override
    public void onSessionMemberUpdate(SessionEntity session, List<ContactEntity> list, boolean b) {
        view.sessionMemberUpdateStatus(session, list, b);
    }

    @Override
    public void onSessionCenterCallRet(boolean ret, SessionEntity sessionEntity) {
        view.sessionCenterCallRet(ret,sessionEntity);
    }

    @Override
    public void onJoinCallTimeOut() {
        view.onJoinCallTimeOut();
    }

    //Media
    @Override
    public void registerMediaListener() {
        model.registerMediaListener(this);
    }

    @Override
    public void unregisterMediaListener() {
        model.unregisterMediaListener();
    }

    @Override
    public void onTalkPreparing(SessionEntity session) {
        view.showTalkPreparingStatus(session);
    }

    @Override
    public void onTalk(SessionEntity session) {
        view.showTalkStatus(session);
    }

    @Override
    public void onTalkEnd(SessionEntity session, int i) {
        view.showTalkEndStatus(session, i);
    }

    @Override
    public void onListen(SessionEntity session, ContactEntity airContact) {
        view.showListenStatus(session, airContact);
    }

    @Override
    public void onListenEnd(SessionEntity session) {
        view.showListenEndStatus(session);
    }

    @Override
    public void onListenVoice(SessionEntity session) {
    }

    @Override
    public void onMediaQueue(SessionEntity session, ArrayList<ContactEntity> arrayList) {
        view.showMediaQueue(session, arrayList);
    }

    @Override
    public void onMediaQueueIn(SessionEntity session) {
        view.mediaQueueIn(session);
    }

    @Override
    public void onMediaQueueOut(SessionEntity session) {
        view.mediaQueueOut(session);
    }

    @Override
    public void updateAudioWave(byte[] bytes) {
        view.updateAudioWave(bytes);
    }

}
