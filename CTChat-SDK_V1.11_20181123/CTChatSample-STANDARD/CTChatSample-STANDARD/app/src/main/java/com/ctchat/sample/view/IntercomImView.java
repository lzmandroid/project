package com.ctchat.sample.view;


import com.ctchat.sdk.ptt.tool.entity.MessageEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;

import java.util.List;

/**
 * 即时通讯view层接口
 */
public interface IntercomImView {
    void messageNotify(List<MessageEntity> list);
    void messageNotify(MessageEntity message);
    void onMessageOutgoingSent(MessageEntity message);
    void onMessageRecordStart();
    void onMessageRecordStop(int seconds,String msgCode);
    void onMessageUpdated(MessageEntity message);
    void onMessageRecordPlayStart(String msgCode, String resId);
    void onMessageRecordPlayStop(String msgCode, String resId);
    void onMessageListLoad(String s, List<MessageEntity> messageEntityList);
    void onMessagePttRecord(SessionEntity session, MessageEntity message, String msgCode, String resId);

}
