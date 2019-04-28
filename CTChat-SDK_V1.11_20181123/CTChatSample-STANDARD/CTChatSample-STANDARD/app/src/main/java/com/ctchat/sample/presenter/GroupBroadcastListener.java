package com.ctchat.sample.presenter;

public interface GroupBroadcastListener {
    //是否有发起广播权限
    void notifyBroadcastState(boolean isHavePermission);
    //广播呼叫结束的原因
    void notifyBroadcastReason(int reason);
}
