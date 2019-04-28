package com.ctchat.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sample.R;
import com.ctchat.sdk.ptt.tool.contact.ContactManager;
import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;
import com.ctchat.sdk.ptt.tool.intercom.IntercomManager;
import com.ctchat.sample.presenter.ContactDetailPresent;
import com.ctchat.sample.presenter.ContactDetailPresentImpl;
import com.ctchat.sample.tool.incoming.SessionInitiationMan;
import com.ctchat.sample.view.ContactDetailView;
import com.ctchat.sample.widget.XpActivity;

public class ContactDetailActivity extends XpActivity implements ContactDetailView {
    private static final String TAG = "ContactDetailActivity";
    private TextView number; // 手机号码
    private TextView department; //所在部门
    private TextView status; // 在线情况
    private RelativeLayout ivBack; // 返回上一级
    private RelativeLayout rlStartTalk; // 开始对讲按钮
    private RelativeLayout rlImmediateMsg; // 即时消息按钮

    private ContactEntity contact;//联系人信息
    private SessionEntity sessionEntity;
    private ContactDetailPresent mPresent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresent.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresent.onResume();
        sessionEntity = IntercomManager.INSTANCE.getSessionEntityByContact(contact);
    }

    @Override
    protected void initView() {
        number = (TextView) findViewById(R.id.tv_contact_detail_number);
        department = (TextView) findViewById(R.id.tv_contact_detail_department_name);
        status = (TextView) findViewById(R.id.tv_detail_status);
        rlStartTalk = (RelativeLayout) findViewById(R.id.rl_start_talk);
        rlImmediateMsg = (RelativeLayout) findViewById(R.id.rl_immediate_message);
        ivBack = (RelativeLayout) findViewById(R.id.rl_back_to_contacts);
        ivBack.setOnClickListener(this);
        rlStartTalk.setOnClickListener(this);
        rlImmediateMsg.setOnClickListener(this);
        contact = (ContactEntity) getIntent().getSerializableExtra("data");
        if (contact != null) {
            if (contact.getMdn() != null) {
                number.setText(contact.getMdn());
            }
            if (contact.getCompanyName() != null) {
                department.setText(contact.getCompanyName());
            }
            if (contact.getMdn() != null) {
                showUserState(contact);
            }
        }
    }

    @Override
    protected void initData() {
        mPresent = new ContactDetailPresentImpl(this);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_contact_detail;
    }

    @Override
    protected String[] broadcastActions() {
        return new String[0];
    }

    @Override
    protected void doAction(Intent intent) {

    }

    private void showUserState(ContactEntity contactEntity) {
        if (contactEntity != null) {
            int state = ContactManager.INSTANCE.getContactStateByEntity(contactEntity);
            switch (state) {
                case ContactEntity.CONTACT_STATE_NONE:
                    status.setText(R.string.offline);
                    break;
                case ContactEntity.CONTACT_STATE_ONLINE_BG:
                    status.setText(R.string.bg_online);
                    break;
                case ContactEntity.CONTACT_STATE_ONLINE:
                    status.setText(R.string.reception_online);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_back_to_contacts:
                finish();
                break;
            case R.id.rl_start_talk:
                initiationSession(sessionEntity.getSessionId(),
                        SessionInitiationMan.INITIATION_PTT
                        , SessionInitiationMan.SESSION_CONNECTION);
                break;
            case R.id.rl_immediate_message:
                initiationSession(sessionEntity.getSessionId(),
                        SessionInitiationMan.INITIATION_IM
                        , SessionInitiationMan.SESSION_UNCONNECTION);
                break;
        }
    }

    /**
     * 跳转到ptt呼叫页面 或者 im即时消息页面
     *
     * @param sessionCode
     * @param initiationMode
     * @param connectionStatus
     */
    private void initiationSession(String sessionCode, int initiationMode, int connectionStatus) {
        Intent intent = new Intent(this, IntercomActivity.class);
        SessionInitiationMan.SessionInitiation sessionInitiation = new SessionInitiationMan.SessionInitiation();
        Logger.d(TAG,"-sessionCode:" + sessionCode + "-initializationMode:" + initiationMode + "-connectionStatus+" + connectionStatus);
        sessionInitiation.sessionCode = sessionCode;
        sessionInitiation.initializationMode = initiationMode;
        sessionInitiation.connectionStatus = connectionStatus;
        intent.putExtra(SessionInitiationMan.INTENT_DATA_KEY, sessionInitiation);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onContactStateChanged() {
        Logger.d(TAG, "onContactsStateChanged");
        if (contact != null) {
            showUserState(contact);
        }
    }
}
