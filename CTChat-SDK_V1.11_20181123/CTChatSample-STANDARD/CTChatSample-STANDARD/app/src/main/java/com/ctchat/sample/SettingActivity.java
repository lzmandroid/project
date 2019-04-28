package com.ctchat.sample;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RelativeLayout;

import com.ctchat.sample.R;
import com.ctchat.sample.login.ChangePasswordActivity;
import com.ctchat.sample.widget.XpActivity;
import com.ctchat.sdk.ptt.tool.chatIM.IMManager;
import com.ctchat.sdk.ptt.tool.entity.MessageEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;

public class SettingActivity extends XpActivity {

    private Context mContext;
    private RelativeLayout rlBack;
    private RelativeLayout intercomSettingLayout, rlChangePassword;

    @Override
    protected void initView() {
        rlBack = (RelativeLayout) findViewById(R.id.rl_setting_back_to_main);
        intercomSettingLayout = (RelativeLayout) findViewById(R.id.rl_intercom_setting);
        rlChangePassword = (RelativeLayout) findViewById(R.id.rl_change_password);

        rlBack.setOnClickListener(this);
        intercomSettingLayout.setOnClickListener(this);
        rlChangePassword.setOnClickListener(this);



    }

    @Override
    protected void initData() {
        mContext = SettingActivity.this;
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_setting;
    }

    @Override
    protected String[] broadcastActions() {
        return new String[0];
    }

    @Override
    protected void doAction(Intent intent) {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_setting_back_to_main:
                finish();
                break;
            case R.id.rl_intercom_setting:
                toActivity(IntercomSettingActivity.class, null);
                break;
            case R.id.rl_change_password:
                toActivity(ChangePasswordActivity.class, null);
                break;

        }
    }
}
