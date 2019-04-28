package com.ctchat.sample.presenter;

public interface MainPresent {
    void onPause();
    void onResume();
    /**
     * 推送系统广播消息
     */
    void onPushBroadcastMsg();
}
