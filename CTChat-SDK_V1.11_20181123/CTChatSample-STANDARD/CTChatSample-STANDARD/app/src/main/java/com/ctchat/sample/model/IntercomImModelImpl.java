package com.ctchat.sample.model;

import android.content.Context;
import android.content.res.Resources;


import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sdk.ptt.tool.chatIM.IMManager;
import com.ctchat.sdk.ptt.tool.chatIM.OnMessageEventListener;
import com.ctchat.sdk.ptt.tool.entity.ChannelEntity;
import com.ctchat.sdk.ptt.tool.entity.MessageEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;
import com.ctchat.sample.application.WeApplication;
import com.ctchat.sample.presenter.IMListener;

import java.util.List;

public class IntercomImModelImpl implements IntercomImModel {
    private static final String TAG = "IntercomImModelImpl";
    private IMListener imListener;
    private Context mContext;
    private Resources resources;
    private OnMessageEventListener messageEventListener;

    public IntercomImModelImpl(IMListener listener) {
        this.imListener = listener;
        this.mContext = WeApplication.getInstance();
        this.resources = WeApplication.getInstance().getResources();
        this.messageEventListener = new OnMessageEventListener() {
            @Override
            public void onMessageListLoad(String sessionCode, List<MessageEntity> messageEntityList) {
                imListener.onMessageListLoad(sessionCode, messageEntityList);
            }

            @Override
            public void onMessageIncomingRecv(MessageEntity message) {
                imListener.onMessageIncomingRecv(message);
            }

            @Override
            public void onMessageIncomingRecv(List<MessageEntity> list) {
                imListener.onMessageIncomingRecv(list);
            }

            @Override
            public void onMessageOutgoingSent(MessageEntity message) {
                imListener.onMessageOutgoingSent(message);
            }

            @Override
            public void onMessageUpdated(MessageEntity message) {
                imListener.onMessageUpdated(message);
            }

            @Override
            public void onMessageRecordStart() {
                imListener.onMessageRecordStart();
            }

            @Override
            public void onMessageRecordStop(int seconds, String msgCode) {
                imListener.onMessageRecordStop(seconds,msgCode);
            }

            @Override
            public void onMessageRecordTransfered(String msgCode, String resId) {
                imListener.onMessageRecordTransfered(msgCode,resId);
            }

            @Override
            public void onMessageRecordPlayLoading(String msgCode, String resId) {
                imListener.onMessageRecordPlayLoading(msgCode,resId);
            }

            @Override
            public void onMessageRecordPlayLoaded(boolean isOk, String code, String resId, byte[] resBytes) {
                imListener.onMessageRecordPlayLoaded(isOk,code,resId,resBytes);
            }

            @Override
            public void onMessageRecordPlayStart(String msgCode, String resId) {
                imListener.onMessageRecordPlayStart(msgCode,resId);
            }

            @Override
            public void onMessageRecordPlayStop(String msgCode, String resId) {
                imListener.onMessageRecordPlayStop(msgCode,resId);
            }

            @Override
            public void onMessagePttRecord(SessionEntity session, MessageEntity message, String msgCode, String resId) {
                imListener.onMessagePttRecord(session, message, msgCode, resId);
            }
        };
    }

    @Override
    public void sendMessageToSession(SessionEntity session, MessageEntity message) {
        Logger.i(TAG,":send Message");
        IMManager.getInstance().sendMessageBySession(session,message);
    }

    @Override
    public void sendMessageToChannel(ChannelEntity channel, MessageEntity message) {
        IMManager.getInstance().sendMessageByChannel(channel,message);
    }

    @Override
    public boolean sendImageToSession(SessionEntity session, MessageEntity message) {
        return IMManager.getInstance().sendImageBySession(session,message);
    }

    @Override
    public boolean sendImageToChannel(ChannelEntity channel, MessageEntity message) {
        return IMManager.getInstance().sendImageByChannel(channel,message);
    }

    @Override
    public boolean sendVideoToSession(SessionEntity session, MessageEntity message) {
        return IMManager.getInstance().sendVideoBySession(session,message);
    }

    @Override
    public boolean sendVideoToChannel(ChannelEntity channel, MessageEntity message) {
        return IMManager.getInstance().sendVedioByChannel(channel,message);
    }

    @Override
    public void resendRecordMessageBySession(SessionEntity session, MessageEntity messageEntity) {
        IMManager.getInstance().resendRecordMessageBySession(session,messageEntity);
    }

    @Override
    public void sendLocationToSession(SessionEntity session, double latitude, double longitude, String address) {
        IMManager.getInstance().sendLocationBySession(session,latitude,longitude,address);
    }

    @Override
    public void sendLocationToChannel(ChannelEntity channel, double latitude, double longitude, String address) {
        IMManager.getInstance().sendLocationByChannel(channel,latitude,longitude,address);
    }

    @Override
    public void startRecordMessageToSession(SessionEntity session) {
        IMManager.getInstance().startRecordBySession(session);
    }

    @Override
    public void startRecordMessageToChannel(ChannelEntity channel) {
        IMManager.getInstance().startRecordByChannel(channel);
    }

    @Override
    public void stopRecordMessage(boolean isCancel) {
        IMManager.getInstance().stopRecord(isCancel);
    }

    @Override
    public void startPlayRecordMessage(MessageEntity message) {
        IMManager.getInstance().startPlayRecordMessage(message);
    }

    @Override
    public void stopPlayRecordMessage() {
        IMManager.getInstance().stopPlayRecordMessage();
    }

    @Override
    public void cleanRecordFile(MessageEntity message) {
        IMManager.getInstance().cleanRecordFile(message);
    }

    @Override
    public void cleanAllRecordFile() {
        IMManager.getInstance().cleanAllRecordFile();
    }

    @Override
    public void moreMessageListLoad(String sessionCode) {
        IMManager.getInstance().messageListMoreLoad(sessionCode);
    }

    @Override
    public void onPause() {
        IMManager.getInstance().unregisterEventListener();
    }

    @Override
    public void onResume() {
        IMManager.getInstance().registerEventListener(this.messageEventListener);
    }
}