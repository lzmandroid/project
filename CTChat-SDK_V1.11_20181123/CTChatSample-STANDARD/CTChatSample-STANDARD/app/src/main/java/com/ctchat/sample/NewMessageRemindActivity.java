package com.ctchat.sample;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RelativeLayout;

import com.ctchat.sample.R;
import com.ctchat.sample.io.Setting;
import com.ctchat.sample.widget.XpActivity;
import com.ctchat.sample.widget.togglebutton.CustomToggleButton;

public class NewMessageRemindActivity extends XpActivity {

    private Context mContext;
    private RelativeLayout backLayout;
    private CustomToggleButton toggleNewMessage;
    private CustomToggleButton toggleNotice;
    private CustomToggleButton toggleShock;

    @Override
    protected void initView() {
        mContext = NewMessageRemindActivity.this;
        backLayout = (RelativeLayout) findViewById(R.id.rl_new_message_back_to_setting);
        backLayout.setOnClickListener(this);

        // 是否开启铃声
        toggleNewMessage = (CustomToggleButton) findViewById(R.id.tog_btn_new_message);
        toggleNewMessage.setToggle(Setting.getReceiveNewMsg(mContext));
        toggleNewMessage.setOnToggleChanged(new CustomToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                Setting.setReceiveNewMsg(mContext, on);
                toggleNewMessage.setToggle(on);
            }
        });

        // 是否开启通知
        toggleNotice = (CustomToggleButton) findViewById(R.id.tog_btn_notice);
        toggleNotice.setToggle(Setting.getOpenNotice(mContext));
        toggleNotice.setOnToggleChanged(new CustomToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                Setting.setOpenNotice(mContext, on);
                toggleNotice.setToggle(on);
            }
        });

        // 是否开启振动
        toggleShock = (CustomToggleButton) findViewById(R.id.tog_btn_shock);
        toggleShock.setToggle(Setting.getUseVibrate(mContext));
        toggleShock.setOnToggleChanged(new CustomToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                toggleShock.setToggle(on);
                Setting.setUseVibrate(mContext, on);
            }
        });
    }

    @Override
    protected void initData() {

    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_new_message_remind;
    }

    @Override
    protected String[] broadcastActions() {
        return new String[0];
    }

    @Override
    protected void doAction(Intent intent) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_new_message_back_to_setting:
                finish();
                break;
        }
    }
}
