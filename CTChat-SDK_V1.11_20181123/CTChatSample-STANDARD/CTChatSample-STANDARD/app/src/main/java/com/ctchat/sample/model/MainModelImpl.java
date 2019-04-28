package com.ctchat.sample.model;

import android.content.Context;

import com.ctchat.sdk.ptt.tool.broadcast.BroadcastManager;
import com.ctchat.sdk.ptt.tool.broadcast.OnNewBroadCastSubscribe;
import com.ctchat.sample.presenter.MainPresent;

public class MainModelImpl implements MainModle,OnNewBroadCastSubscribe{
    private MainPresent mainPresent;
    private Context mContext;

    public MainModelImpl(MainPresent mainPresent, Context context) {
        this.mainPresent = mainPresent;
        this.mContext = context;
    }

    @Override
    public void onPushBroadcastMsg() {
        mainPresent.onPushBroadcastMsg();
    }

    @Override
    public void onPause() {
        BroadcastManager.getInstance().unregisterNewBroadcastListener(this);
    }

    @Override
    public void onResume() {
        BroadcastManager.getInstance().registerNewBroadcastListener(this);
    }

    @Override
    public void onNewBroadCast(String content) {
        onPushBroadcastMsg();
    }
}
