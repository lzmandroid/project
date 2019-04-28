package com.ctchat.sample.model;


import com.ctchat.sdk.ptt.tool.entity.ChannelEntity;

public interface ChannelModel {

    void loadChannelListData();

    void getChannelMembers(ChannelEntity channelEntity);
    void refreshChannelListData();

    void onPause();

    void onResume();

}
