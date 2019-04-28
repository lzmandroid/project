package com.ctchat.sample.presenter;


import com.ctchat.sdk.ptt.tool.entity.ChannelEntity;

public interface IntercomListPresent {
    void getChannelMember(ChannelEntity channel);
    void onPause();
    void onResume();
}
