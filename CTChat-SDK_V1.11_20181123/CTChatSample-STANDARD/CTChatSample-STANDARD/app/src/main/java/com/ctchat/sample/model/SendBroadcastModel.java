package com.ctchat.sample.model;


import com.ctchat.sdk.ptt.tool.entity.SessionEntity;

public interface SendBroadcastModel {
    void startSendBroadcast();

    void stopSendBroadcast();

    SessionEntity getBroadcastSession();

    void registerGroupBroadcastListener();

    void unregisterGroupBroadcastListener();
}
