package com.ctchat.sample.presenter;


import com.ctchat.sdk.ptt.tool.entity.ChannelEntity;

public interface ChannelPresent {
    /**
     * 加载预定义组数据
     */
    void loadData();

    void getChannelMembers(ChannelEntity channelEntity);
    void onDestroy();

    void onResume();

    void onPause();
}
