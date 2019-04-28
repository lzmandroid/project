package com.ctchat.sample.model;


import com.ctchat.sdk.ptt.tool.entity.ChannelEntity;
import com.ctchat.sdk.ptt.tool.entity.MessageEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;

/**
 * 即时通讯model层接口
 */
public interface IntercomImModel {

    void sendMessageToSession(SessionEntity session, MessageEntity message);
    void sendMessageToChannel(ChannelEntity channel, MessageEntity message);

    boolean sendImageToSession(SessionEntity session,MessageEntity message);
    boolean sendImageToChannel(ChannelEntity channel, MessageEntity message);
    boolean sendVideoToSession(SessionEntity session,MessageEntity message);
    boolean sendVideoToChannel(ChannelEntity channel, MessageEntity message);

    void resendRecordMessageBySession(SessionEntity session, MessageEntity messageEntity);

    void sendLocationToSession(SessionEntity session,double latitude,double longitude,String address);
    void sendLocationToChannel(ChannelEntity channel, double latitude, double longitude, String address);

    void startRecordMessageToSession(SessionEntity session);
    void startRecordMessageToChannel(ChannelEntity channel);

    void stopRecordMessage(boolean isCancel);

    void startPlayRecordMessage(MessageEntity message);
    void stopPlayRecordMessage();
    void cleanRecordFile(MessageEntity message);
    void cleanAllRecordFile();

    void moreMessageListLoad(String SessionCode);

    void onPause();
    void onResume();
}
