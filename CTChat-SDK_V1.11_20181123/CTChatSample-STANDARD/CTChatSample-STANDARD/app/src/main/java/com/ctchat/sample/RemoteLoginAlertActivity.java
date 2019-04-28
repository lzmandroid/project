package com.ctchat.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sample.R;
import com.ctchat.sample.application.AppInitializeManager;
import com.ctchat.sample.widget.XpActivity;

public class RemoteLoginAlertActivity extends XpActivity {
    private static final String TAG = "RemoteLoginAlertActivit";
    private Button btnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES
                | WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER;

        getWindow().addFlags(flags);
    }

    @Override
    protected void initView() {
        btnExit = (Button) findViewById(R.id.btn_exit_remote_login);
        btnExit.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        //LoginSPManager.setAutoLoginStatus(this, false);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_remote_login_alert;
    }

    @Override
    protected String[] broadcastActions() {
        return new String[0];
    }

    @Override
    protected void doAction(Intent intent) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.i(TAG,"remote Login onStop");
        finish();
        AppInitializeManager.exitApplication();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_exit_remote_login:
//                SocketManager.MANAGER.SendStopMessage();
                finish();
                AppInitializeManager.exitApplication();
                break;
            default:
                break;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            AppInitializeManager.exitApplication();
        }
        return true;
    }
}