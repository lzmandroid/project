package com.ctchat.sample.model;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;

import com.ctchat.sdk.ptt.tool.bluetooth.BluetoothListener;
import com.ctchat.sample.presenter.BluetoothConnectListener;

public class IntercomPttModelImpl implements IntercomPttModel {

    private BluetoothListener bluetoothListener;
    private BluetoothConnectListener connectListener;

    public IntercomPttModelImpl(BluetoothConnectListener listener) {
        this.connectListener = listener;
        bluetoothListener = new BluetoothListener() {
            @Override
            public void onActionDiscoveryStateChanged(String state) {

            }

            @Override
            public void onActionDeviceFound(BluetoothDevice device, short rssi) {

            }

            @Override
            public void onActionDeviceConnectStateChanged(int state) {
                if (connectListener != null){
                    connectListener.onBluetoothConnectStateChangeEvent(state);
                }
            }

            @Override
            public void onActionDeviceStateChanged(int state) {

            }

            @Override
            public void onHeadsetServiceConnected(BluetoothProfile bluetoothProfile) {

            }

            @Override
            public void onHeadsetServiceDisconnected() {

            }
        };
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onResume() {
    }
}
