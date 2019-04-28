package com.ctchat.sample.presenter;

import com.ctchat.sdk.ptt.tool.entity.MessageEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;

import java.util.List;

public interface IMListener {
    void onMessageIncomingRecv(MessageEntity message);

    void onMessageIncomingRecv(List<MessageEntity> list);

    void onMessageOutgoingSent(MessageEntity message);

    void onMessageUpdated(MessageEntity message);

    void onMessageRecordStart();

    void onMessageRecordStop(int seconds, String msgCode);

    void onMessageRecordTransfered(String msgCode, String resId);

    void onMessageRecordPlayLoading(String msgCode, String resId);

    void onMessageRecordPlayLoaded(boolean isOk, String code, String resId, byte[] resBytes);

    void onMessageRecordPlayStart(String msgCode, String resId);

    void onMessageRecordPlayStop(String msgCode, String resId);

    void onMessageListLoad(String s, List<MessageEntity> messageEntityList);

    void onMessagePttRecord(SessionEntity session, MessageEntity message, String msgCode, String resId);
}
