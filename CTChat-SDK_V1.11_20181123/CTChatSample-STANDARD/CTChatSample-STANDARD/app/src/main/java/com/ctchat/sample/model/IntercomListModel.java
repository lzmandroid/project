package com.ctchat.sample.model;


import com.ctchat.sdk.ptt.tool.entity.ChannelEntity;

public interface IntercomListModel {
    void getChannelMember(ChannelEntity channel);
    void onPause();
    void onResume();
}
