package com.ctchat.sample.view;

public interface IntercomPttView {
    void initPttStatus();

    void updateSessionLockView(boolean status);

    void updateSessionPreemptionView(boolean status);

    void onBluetoothConnectChangeEvent(int state);

}
