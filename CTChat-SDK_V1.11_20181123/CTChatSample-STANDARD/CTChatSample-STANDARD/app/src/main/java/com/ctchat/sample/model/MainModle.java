package com.ctchat.sample.model;

public interface MainModle {
    /**
     * 推送系统广播消息
     */
    void onPushBroadcastMsg();

    void onPause();
    void onResume();
}
