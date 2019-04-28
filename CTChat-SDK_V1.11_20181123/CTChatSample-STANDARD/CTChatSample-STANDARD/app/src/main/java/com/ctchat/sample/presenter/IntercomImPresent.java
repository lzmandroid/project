package com.ctchat.sample.presenter;


import com.ctchat.sdk.ptt.tool.entity.ChannelEntity;
import com.ctchat.sdk.ptt.tool.entity.MessageEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;

/**
 * 即时通讯presenter层接口
 */
public interface IntercomImPresent {
    void onPause();
    void onResume();

    void sendMessageToSession(SessionEntity session, MessageEntity message);
    void sendMessionToChannel(ChannelEntity channel, MessageEntity message);

    boolean sendImageToSession(SessionEntity session,MessageEntity message);
    boolean sendImageToChannel(ChannelEntity channel, MessageEntity message);

    boolean sendVideoToSession(SessionEntity session,MessageEntity message);

    void resendRecordMessageBySession(MessageEntity messageEntity);

    void sendLocationToSession(SessionEntity session,double latitude,double longitude,String address);

    void startRecordMessageToSession(SessionEntity session);

    void startPlayRecordMessage(MessageEntity message);
    void stopPlayRecordMessage();

    void stopRecordMessage(boolean isCancel);

    void moreMessageListLoad(String SessionCode);
}