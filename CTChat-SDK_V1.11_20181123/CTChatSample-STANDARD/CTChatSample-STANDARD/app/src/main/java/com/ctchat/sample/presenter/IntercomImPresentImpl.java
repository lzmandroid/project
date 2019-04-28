package com.ctchat.sample.presenter;

import com.ctchat.sdk.ptt.tool.entity.ChannelEntity;
import com.ctchat.sdk.ptt.tool.entity.MessageEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;
import com.ctchat.sample.model.IntercomImModel;
import com.ctchat.sample.model.IntercomImModelImpl;
import com.ctchat.sample.view.IntercomImView;

import java.util.List;

public class IntercomImPresentImpl implements IntercomImPresent, IMListener {
    private IntercomImView view;
    private IntercomImModel model;

    public IntercomImPresentImpl(IntercomImView view) {
        this.view = view;
        model = new IntercomImModelImpl(this);
    }

    @Override
    public void onPause() {
        this.model.onPause();
    }

    @Override
    public void onResume() {
        this.model.onResume();
    }


    @Override
    public void sendMessageToSession(SessionEntity session, MessageEntity message) {
        this.model.sendMessageToSession(session,message);
    }

    @Override
    public void sendMessionToChannel(ChannelEntity channel, MessageEntity message) {
        this.model.sendMessageToChannel(channel,message);
    }

    @Override
    public boolean sendImageToSession(SessionEntity session, MessageEntity message) {
        return this.model.sendImageToSession(session,message);
    }

    @Override
    public boolean sendImageToChannel(ChannelEntity channel, MessageEntity message) {
        return this.model.sendImageToChannel(channel, message);
    }

    @Override
    public boolean sendVideoToSession(SessionEntity session, MessageEntity message) {
        return this.model.sendVideoToSession(session,message);
    }

    @Override
    public void resendRecordMessageBySession(MessageEntity messageEntity) {
        this.model.resendRecordMessageBySession(messageEntity.getSession(),messageEntity);
    }

    @Override
    public void sendLocationToSession(SessionEntity session, double latitude, double longitude, String address) {
        this.model.sendLocationToSession(session,latitude,longitude,address);
    }

    @Override
    public void startRecordMessageToSession(SessionEntity session) {
        this.model.startRecordMessageToSession(session);
    }

    @Override
    public void startPlayRecordMessage(MessageEntity message) {
        this.model.startPlayRecordMessage(message);
    }

    @Override
    public void stopPlayRecordMessage() {
        this.model.stopPlayRecordMessage();
    }

    @Override
    public void stopRecordMessage(boolean isCancel) {
        this.model.stopRecordMessage(isCancel);
    }

    @Override
    public void moreMessageListLoad(String SessionCode) {
        this.model.moreMessageListLoad(SessionCode);
    }

    @Override
    public void onMessageIncomingRecv(MessageEntity message) {
        this.view.messageNotify(message);
    }

    @Override
    public void onMessageIncomingRecv(List<MessageEntity> list) {
        this.view.messageNotify(list);
    }

    @Override
    public void onMessageOutgoingSent(MessageEntity message) {
        this.view.onMessageOutgoingSent(message);
    }

    @Override
    public void onMessageUpdated(MessageEntity message) {
        this.view.onMessageUpdated(message);
    }

    @Override
    public void onMessageRecordStart() {
        this.view.onMessageRecordStart();

    }

    @Override
    public void onMessageRecordStop(int seconds, String msgCode) {
        this.view.onMessageRecordStop(seconds,msgCode);
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
        view.onMessageRecordPlayStart(msgCode, resId);
    }

    @Override
    public void onMessageRecordPlayStop(String msgCode, String resId) {
        view.onMessageRecordPlayStop(msgCode, resId);
    }

    @Override
    public void onMessageListLoad(String s, List<MessageEntity> messageEntityList) {
        view.onMessageListLoad(s, messageEntityList);
    }

    @Override
    public void onMessagePttRecord(SessionEntity session, MessageEntity message, String msgCode, String resId) {
        view.onMessagePttRecord(session, message, msgCode, resId);
    }
}