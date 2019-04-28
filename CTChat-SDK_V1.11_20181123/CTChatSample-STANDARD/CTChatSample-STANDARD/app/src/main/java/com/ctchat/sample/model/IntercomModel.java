package com.ctchat.sample.model;

import com.ctchat.sdk.ptt.tool.entity.SessionEntity;
import com.ctchat.sample.presenter.MediaListener;

public interface IntercomModel {
    void onPause();
    void onResume();
    void onDestroy();

    void registerMediaListener(MediaListener mediaListener);

    void unregisterMediaListener();

    void startJoinCallTimer(String caller, SessionEntity newSessionEntity, SessionEntity oldSessionEntity, long delayTime);

    void releaseJoinCallTimer();
}
