package com.ctchat.sample.presenter;


import com.ctchat.sample.model.IntercomPttModel;
import com.ctchat.sample.model.IntercomPttModelImpl;
import com.ctchat.sample.view.IntercomPttView;

public class IntercomPttPresentImpl implements IntercomPttPresent, BluetoothConnectListener {
    private IntercomPttView view;
    private IntercomPttModel model;

    public IntercomPttPresentImpl(IntercomPttView view) {
        this.view = view;
        model = new IntercomPttModelImpl(this);
    }


    @Override
    public void onBluetoothConnectStateChangeEvent(int state) {
        if (view != null){
            view.onBluetoothConnectChangeEvent(state);
        }
    }


    @Override
    public void onPause() {
        model.onPause();
    }

    @Override
    public void onResume() {
        model.onResume();
    }
}
