package com.ctchat.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.ctchat.sample.R;
import com.ctchat.sdk.ptt.tool.MessagePush.PushManager;
import com.ctchat.sdk.ptt.tool.chatIM.IMManager;
import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;
import com.ctchat.sdk.ptt.tool.intercom.IntercomManager;
import com.ctchat.sample.application.WeApplication;
import com.ctchat.sample.tool.Sound;
import com.ctchat.sample.tool.incoming.SessionInitiationMan;
import com.ctchat.sample.widget.XpActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class IntercomingAlertActivity  extends XpActivity  {

    private TextView tvAlertTitle, tvAlertContent;
    private SessionInitiationMan.SessionInitiation sessionInitiation = new SessionInitiationMan.SessionInitiation();
    private SessionEntity sessionEntity = new SessionEntity();
    private ContactEntity airContact = new ContactEntity();
    private String TAG = "IntercomingAlertActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES;
//                | WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER;

        getWindow().addFlags(flags);

        sessionInitiation = getIntent().getParcelableExtra(SessionInitiationMan.INTENT_DATA_KEY);
        sessionEntity = IntercomManager.INSTANCE.getSessionEntityBySessionId(sessionInitiation.getSessionCode());
        airContact = (ContactEntity) getIntent().getSerializableExtra("airContact");

        if (airContact != null) {
            tvAlertTitle.setText(R.string.phone_calls_remind);
        }
        tvAlertContent.setText(airContact.getDisplayName() + getString(R.string.call_from));
        EventBus.getDefault().register(this);

    }


    public static class EBAlertEnd {

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void incomingAlertEnd(EBAlertEnd end) {
        finishAlertActivity();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        sessionInitiation = getIntent().getParcelableExtra(SessionInitiationMan.INTENT_DATA_KEY);
        sessionEntity = IntercomManager.INSTANCE.getSessionEntityBySessionId(sessionInitiation.getSessionCode());
        airContact = (ContactEntity) getIntent().getSerializableExtra("airContact");
        if (airContact != null) {
            tvAlertTitle.setText(R.string.phone_calls_remind);
        }
        tvAlertContent.setText(airContact.getDisplayName() + getString(R.string.call_from));
    }

    @Override
    protected void initView() {
        tvAlertTitle = (TextView) findViewById(R.id.tv_incoming_alert_title);
        tvAlertContent = (TextView) findViewById(R.id.tv_incoming_alert_message);
        findViewById(R.id.bt_positive_button).setOnClickListener(this);
        findViewById(R.id.bt_negative_button).setOnClickListener(this);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_intercoming_alret;
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
            case R.id.bt_positive_button:
                agreeToAnswer();
                break;
            case R.id.bt_negative_button:
                refuseToAnswer();
                break;
        }
     }

    /**
     * 拒接
     */
    private void refuseToAnswer() {
        Sound.stopSound(Sound.PLAYER_INCOMING_RING);
        WeApplication.setIncomingFlag(false);
        IMManager.getInstance().generateSystemMessage(sessionEntity, new ContactEntity(sessionEntity.getAirSession().getCaller()),
                context.getString(R.string.talk_call_state_rejected_call), true);
        IntercomManager.INSTANCE.rejectSessionCall(sessionEntity,false);
        finishAlertActivity();
        //AirServices.getInstance().callingSession = null;
    }

    /**
     * 接听
     */
    private void agreeToAnswer() {
        Sound.stopSound(Sound.PLAYER_INCOMING_RING);
        IntercomManager.INSTANCE.acceptSessionCall(sessionEntity);
        IMManager.getInstance().generateSystemMessage(sessionEntity, new ContactEntity(sessionEntity.getAirSession().getCaller()),
                context.getString(R.string.talk_call_state_incoming_call), false);
        WeApplication.setIncomingFlag(false);
        Intent i = new Intent(IntercomingAlertActivity.this, IntercomActivity.class);
        i.putExtra(SessionInitiationMan.INTENT_DATA_KEY, sessionInitiation);
        startActivity(i);
        finishAlertActivity();
        //AirServices.getInstance().callingSession = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            refuseToAnswer();
            return true;
        }
        return false;
    }

    private void finishAlertActivity() {
        EventBus.getDefault().unregister(this);
        finish();
    }
}
