package com.ctchat.sample.presenter;


import com.ctchat.sdk.ptt.tool.entity.SessionEntity;

public interface IntercomPresent extends MediaPresent {
    @Override
    void registerMediaListener();

    @Override
    void unregisterMediaListener();

    void onPause();

    void onResume();

    void onDestroy();

    void startJoinCallTimer(String caller, SessionEntity newSessionEntity, SessionEntity oldSessionEntity, long delayTime);

    void releaseJoinCallTimer();
}
