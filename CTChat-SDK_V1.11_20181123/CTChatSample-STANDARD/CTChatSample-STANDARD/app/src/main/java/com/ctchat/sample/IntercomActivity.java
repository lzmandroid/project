package com.ctchat.sample;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ctchat.sample.adapter.HomePagerAdapter;
import com.ctchat.sample.adapter.InviteContactAdapter;
import com.ctchat.sample.entity.TabBean;
import com.ctchat.sample.fragment.IntercomImFragment;
import com.ctchat.sample.fragment.IntercomListFragment;
import com.ctchat.sample.fragment.IntercomPttFragment;
import com.ctchat.sample.io.ContactSPManager;
import com.ctchat.sample.io.Setting;
import com.ctchat.sample.presenter.IntercomPresent;
import com.ctchat.sample.presenter.IntercomPresentImpl;
import com.ctchat.sample.tool.EBIntercomStatus;
import com.ctchat.sample.tool.EBSessionWait;
import com.ctchat.sample.tool.incoming.SessionInitiationMan;
import com.ctchat.sample.util.ApiConst;
import com.ctchat.sample.util.TimeUtil;
import com.ctchat.sample.util.Util;
import com.ctchat.sample.view.IntercomView;
import com.ctchat.sample.widget.AudioVirtualizerView;
import com.ctchat.sample.widget.BadgeView;
import com.ctchat.sample.widget.CustomHintDialog;
import com.ctchat.sample.widget.XpActivity;
import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sdk.basemodule.util.ToastUtil;
import com.ctchat.sdk.ptt.tool.chatIM.IMManager;
import com.ctchat.sdk.ptt.tool.contact.ContactManager;
import com.ctchat.sdk.ptt.tool.entity.ChannelEntity;
import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sdk.ptt.tool.entity.MessageEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;
import com.ctchat.sdk.ptt.tool.intercom.IntercomManager;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static android.view.KeyEvent.KEYCODE_BACK;

public class IntercomActivity extends XpActivity implements IntercomView {
    private static final String TAG = "IntercomActivity";

    private IntercomPresent presenter;
    private TextView tvTitleName, tvChatTitle, ivInvitation, ivLock, ivSetting;
    private ImageView ivChatPoint;

    private AudioVirtualizerView audioVirtualizerView;

    private IntercomListFragment listFragment;//会话成员列表页面Fragment
    private IntercomPttFragment pttFragment;//呼叫页面Fragment
    private IntercomImFragment imFragment;//即时消息页面Fragment

    private ImageView ivBack;

    private SessionInitiationMan.SessionInitiation sessionInitiation;
    private int initiationMode = SessionInitiationMan.INITIATION_PTT;//初始化Fragment的显示页码
    private int connectionStatus = SessionInitiationMan.SESSION_UNCONNECTION;//初始化Fragment的显示页码
    private SessionEntity sessionEntity;

    public static final byte OWN_SPEAKING = 0x3;

    private int sessionType = -1;//会话类型（预定义组||临时会话）

    private long startSpeakTime;//记录开始说话的时间

    private PopupWindow popupWindow;
    private ListView lvPopInvite;
    private RelativeLayout rlBack;
    private TextView tvFinish, tvBack;
    private InviteContactAdapter adapter;
    private List<ContactEntity> contactList, currentContactList;
    private int pageSelect = Util.FRAGMENT_INTERCOM_IM;//默认为即时消息界面
    private BadgeView badgeView;
    private TextView tvJoinSession, tvNotJoinSession, tvJoinSessionContent;
    private PopupWindow popupJoinSession;
    private long mExitTime = 0;
    private SessionEntity oldJoinCallSesionEntity;
    private SessionEntity newJoinCallSessionEntity;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TextView tvIntercomCallOnline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "IntercomActivity onCreate");
        int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES;

        getWindow().addFlags(flags);

        initViewPage();
        refreshViewBySessionEntity(sessionEntity);


        //直接建立会话链接
        if (connectionStatus == SessionInitiationMan.SESSION_CONNECTION) {
            directEstablishConnection(sessionEntity);
        }

        //根据会话类型以及状态判断是否要更新PTT按键会话
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Logger.d(TAG, "onNewIntent");
        super.onNewIntent(intent);
        //当在预定义组会话中有临时会话呼入
        //注意：只能使用intent 不能使用getIntent();
        sessionInitiation = intent.getParcelableExtra(SessionInitiationMan.INTENT_DATA_KEY);
        String sessionId = sessionInitiation.getSessionCode();
        connectionStatus = sessionInitiation.getConnectionStatus();
        initiationMode = sessionInitiation.getInitializationMode();

        Logger.d(TAG, "sessionCode:" + sessionId);
        if (!sessionId.isEmpty()) {
            sessionEntity = IntercomManager.INSTANCE.getSessionEntityBySessionId(sessionId);
        }

        /*当OnNewIntent被调用时，是从task中找出已经存在的IntercomActivity实例进行使用，initViewPage里面会
         *  重设fragment对象，因此相关fragment内控件的隐藏需放在initViewPage之前
         */
        if (popupJoinSession != null && popupJoinSession.isShowing()) {
            popupJoinSession.dismiss();
        }

        if (null != popupWindow && popupWindow.isShowing()) {
            hidePopwindow();
        }

        listFragment.hidePopupWindow();

        initViewPage();
        refreshViewBySessionEntity(sessionEntity);

        //直接建立会话链接
        if (connectionStatus == SessionInitiationMan.SESSION_CONNECTION) {
            directEstablishConnection(sessionEntity);
            ivChatPoint.setImageResource(R.drawable.intercom_state_busy);
            tvChatTitle.setText(R.string.setting_up_the_session);
            notifyPttBtnUpdateStatus(EBIntercomStatus.SESSION_CALLING_STATUS);
        }

        //根据会话类型以及状态判断是否要更新PTT按键会话
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            listFragment.onActivityResult(requestCode, resultCode, data);
            pttFragment.onActivityResult(requestCode, resultCode, data);
            imFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initPopupJoinSession() {
        View view = LayoutInflater.from(this).inflate(R.layout.popup_window_group_call, null);
        popupJoinSession = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        popupJoinSession.setOutsideTouchable(false);
        tvJoinSession = (TextView) view.findViewById(R.id.tv_join_session);
        tvNotJoinSession = (TextView) view.findViewById(R.id.tv_not_join_session);
        tvJoinSessionContent = (TextView) view.findViewById(R.id.tv_intercom_group_call_msg);
    }

    private void showJoinSessionMsg(TextView tvContent, String name, String channelName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(name);
        stringBuilder.append(getResources().getString(R.string.in));
        stringBuilder.append(channelName);
        stringBuilder.append(getResources().getString(R.string.start_group_call));
        SpannableString spannableString = new SpannableString(stringBuilder.toString());
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.not_join_group_call)), name.length() + 1, name.length() + 1 + channelName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvContent.setText(spannableString);
    }

    public void showPopupJoinSession(final SessionEntity groupCallSession, String channelName, String initiator, String pId) {
        if (newJoinCallSessionEntity != null) {
            oldJoinCallSesionEntity = newJoinCallSessionEntity;
        }
        newJoinCallSessionEntity = groupCallSession;
        presenter.startJoinCallTimer(pId, newJoinCallSessionEntity, oldJoinCallSesionEntity, ApiConst.JOIN_CALL_TIME_OUT);//定时30s
        if (popupJoinSession != null && !popupJoinSession.isShowing()) {
            View parent = LayoutInflater.from(this).inflate(R.layout.activity_main, null);
            showJoinSessionMsg(tvJoinSessionContent, initiator, channelName);
            popupJoinSession.showAtLocation(parent, Gravity.TOP, 0, 0);
        } else {
            showJoinSessionMsg(tvJoinSessionContent, initiator, channelName);
        }
        tvJoinSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupJoinSession.dismiss();
                presenter.releaseJoinCallTimer();
                if (sessionEntity.getSessionType() == SessionEntity.TYPE_DIALOG && sessionEntity.getSessionStatus() == SessionEntity.SESSION_STATE_DIALOG) {
                    showConfirmJoinDialog(groupCallSession);
                } else {
                    joinIntercom(groupCallSession);
                }
            }
        });
        tvNotJoinSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupJoinSession.dismiss();
                presenter.releaseJoinCallTimer();
            }
        });
    }


    @Override
    protected void onResume() {
        Logger.d(TAG, "onResume");
        super.onResume();

        undateUnreadCount();

        presenter.registerMediaListener();
        presenter.onResume();

        refreshMediaState(sessionEntity);
    }


    @Override
    protected void onPause() {
        Logger.d(TAG, "onPause");
        super.onPause();

        IMManager.getInstance().stopPlayRecordMessage();
        presenter.unregisterMediaListener();
        presenter.onPause();
    }

    @Override
    protected void onStop() {
        Logger.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Logger.d(TAG, "onDestroy");
        super.onDestroy();
        presenter.onDestroy();
        //临时会话，当界面finish()，挂断电话
        if (sessionType == SessionEntity.TYPE_DIALOG
                && sessionEntity.getSessionStatus() != SessionEntity.SESSION_STATE_IDLE) {
            IntercomManager.INSTANCE.stopSessionCall(sessionEntity);
        }
        if (handler != null) {
            //取消handler对象在Message中排队，避免内存泄漏
            handler.removeMessages(OWN_SPEAKING);
            handler = null;
        }
    }

    @Override
    protected void initView() {
        Logger.d(TAG, "initView");
        viewPager = findViewById(R.id.homeViewPager);
        tabLayout = findViewById(R.id.tabLayout);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        tvBack = (TextView) findViewById(R.id.chat_txtview_back_text);
        tvChatTitle = (TextView) findViewById(R.id.tv_chat_title);
        ivChatPoint = (ImageView) findViewById(R.id.chat_instant_message_head_point);
        tvTitleName = (TextView) findViewById(R.id.tv_intercom_group_title_name);
        tvIntercomCallOnline = findViewById(R.id.tv_intercom_call_online);
        ivInvitation = findViewById(R.id.iv_invitation);
        ivSetting = findViewById(R.id.iv_setting);
        ivLock = (TextView) findViewById(R.id.iv_lock);
        audioVirtualizerView = (AudioVirtualizerView) findViewById(R.id.audio_virtualizer_view);
        audioVirtualizerView.setSpectrumNum(Util.AUDIO_VIRTUALLIZE_SPECTRUM_NUM);
        ivInvitation.setOnClickListener(this);
        ivSetting.setOnClickListener(this);
        ivLock.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        tvBack.setOnClickListener(this);
        tvIntercomCallOnline.setOnClickListener(this);

        initPopupJoinSession();
        badgeView = new BadgeView(context, ivBack);
        badgeView.setBadgeBackgroundColor(Color.RED);
        badgeView.setTextSize(6);
        badgeView.setBadgePosition(BadgeView.POSITION_TOP_LEFT); //默认值

    }

    /**
     * 清空信息未读提醒
     */
    public void cleanMessageUnreadCount() {
        Logger.d(TAG, "清空数量");
        IMManager.getInstance().cleanMessageUnreadCount(sessionEntity);
    }

    @Override
    protected void initData() {
        Logger.d(TAG, "initData");
        presenter = new IntercomPresentImpl(this);
        sessionInitiation = getIntent().getParcelableExtra(SessionInitiationMan.INTENT_DATA_KEY);
        String sessionId = sessionInitiation.getSessionCode();
        initiationMode = sessionInitiation.getInitializationMode();
        connectionStatus = sessionInitiation.getConnectionStatus();
        Logger.d(TAG, "sessionCode:" + sessionId);
        Logger.d(TAG, "initiationMode:" + initiationMode);
        Logger.d(TAG, "connectionStatus:" + connectionStatus);
        if (!sessionId.isEmpty()) {
            sessionEntity = IntercomManager.INSTANCE.getSessionEntityBySessionId(sessionId);
        } else {
            Logger.e(TAG, "Input SessionCode Null!!!!");
        }
    }


    @Override
    public void refreshViewBySessionEntity(SessionEntity sessionEntity) {
        Logger.d(TAG, "refreshViewBySessionEntity");
        //获取会话类型
        if (null != sessionEntity) {
            //根据会话类型显示或隐藏返回按钮
            if (sessionEntity.getSessionType() == SessionEntity.TYPE_GROUP) {
                ivInvitation.setVisibility(View.GONE);
                //设置预定义组名称
                tvTitleName.setText(sessionEntity.getChannelName());
            } else {
                ivLock.setVisibility(View.GONE);

                //设置会话名称：一般为主叫人的名字或预定义组名称
                tvTitleName.setText(sessionEntity.getSessionName());
            }
        }
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_intercom;
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
            case R.id.chat_txtview_back_text:
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_invitation://添加成员
                InviteContacts(v);
                break;
            case R.id.iv_setting:
                Intent setting_intent = new Intent(this, IntercomSettingActivity.class);
                startActivity(setting_intent);
                break;
            case R.id.iv_lock://锁定||群呼上线
                //判断是否为预定义组
                Log.d(TAG, "onClick: sessionEntity = " + sessionEntity);
                Log.d(TAG, "onClick: sessionEntity.getSessionType() = " + sessionEntity.getSessionType());
                if (sessionEntity != null && sessionEntity.getSessionType() == SessionEntity.TYPE_GROUP) {
                    //判断会话是否建立
                    Log.d(TAG, "onClick: sessionEntity.getSessionStatus() = " + sessionEntity.getSessionStatus());
                    if (sessionEntity.getSessionStatus() == SessionEntity.SESSION_STATE_DIALOG) {
                        IntercomManager.INSTANCE.lockGroupCall(sessionEntity, !sessionEntity.isLocked());
                        String lockInfo = "";
                        //显示锁定状态
                        if (sessionEntity.isLocked()) {
                            lockInfo = context.getString(R.string.intercom_channel_manager_lock_enable);
                            ivLock.setText("已锁定");
                        } else {
                            lockInfo = context.getString(R.string.intercom_channel_manager_lock_disable);
                            ivLock.setText("锁定");
                        }
                        ToastUtil.shortShow(context, lockInfo);
                    } else {
                        ToastUtil.shortShow(context, getString(R.string.talk_unconnected));
                    }
                }
                break;
            case R.id.rl_select_back_pop:
                hidePopwindow();
                break;
            case R.id.tv_finish_select:
                if (currentContactList.size() > 0) {
                    IntercomManager.INSTANCE.inviteSessionCall(sessionEntity.getSessionId(), currentContactList);
                    ToastUtil.shortShow(context, resources.getString(R.string.invite_call_has_send));
                    hidePopwindow();
                } else {
                    ToastUtil.shortShow(getApplicationContext(), R.string.invite_contact_no_empty);
                }
                break;
            case R.id.tv_intercom_call_online:
                listFragment.groupCallOnline();
                break;
        }
    }

    private void showConfirmJoinDialog(final SessionEntity groupCallSession) {
        CustomHintDialog.Builder confirmJoinDialog = new CustomHintDialog.Builder(this);
        confirmJoinDialog.setTitle(R.string.hint);
        confirmJoinDialog.setMessage(R.string.confirm_join_warning);
        confirmJoinDialog.setConfirmButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                IntercomManager.INSTANCE.stopSessionCall(sessionEntity);
                joinIntercom(groupCallSession);
            }
        });
        confirmJoinDialog.setBackButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        confirmJoinDialog.create().show();
    }


    private void joinIntercom(SessionEntity groupCallSession) {
        Intent intent = new Intent(this, IntercomActivity.class);
        SessionInitiationMan.SessionInitiation sessionInitiation = new SessionInitiationMan.SessionInitiation();
        sessionInitiation.sessionCode = groupCallSession.getSessionId();
        sessionInitiation.initializationMode = SessionInitiationMan.INITIATION_PTT;
        sessionInitiation.connectionStatus = SessionInitiationMan.SESSION_CONNECTION;
        intent.putExtra(SessionInitiationMan.INTENT_DATA_KEY, sessionInitiation);
        startActivity(intent);
    }

    /**
     * 隐藏popwindow
     */
    private void hidePopwindow() {
        Set<String> selectOptions = adapter.getSelectOptions();
        if (selectOptions.size() > 0) {
            selectOptions.clear();
            adapter.clearAnimSet();
            adapter.notifyDataSetChanged();
            currentContactList.clear();
        }
        popupWindow.dismiss();
    }

    /**
     * 添加成员
     */

    private void InviteContacts(View view) {
        Logger.d(TAG, "InviteContacts");
        getPopupWindow();
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }


    /**
     * ***************************** ViewPager *****************************
     */
    void initViewPage() {
        Logger.d(TAG, "initViewPage");

        sessionType = sessionEntity.getSessionType();
        if (listFragment == null)
            listFragment = new IntercomListFragment();
        if (pttFragment == null)
            pttFragment = new IntercomPttFragment();
        if (imFragment == null)
            imFragment = new IntercomImFragment();
        tvIntercomCallOnline.setVisibility(View.GONE);
        dataList = new ArrayList<>();
        dataList.add(new TabBean("成员列表", listFragment));
        dataList.add(new TabBean("语音对讲", pttFragment));
        dataList.add(new TabBean("即时消息", imFragment));
        viewPager.setAdapter(new HomePagerAdapter(getSupportFragmentManager(), dataList));
        viewPager.setCurrentItem(initiationMode, true);
        viewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(viewPager, true);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setCurrentPage(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        updateUI(initiationMode);
    }

    private List<TabBean> dataList;

    public void setCurrentPage(int num) {
        Logger.d(TAG, "setCurrentPage");
        if (num == Util.FRAGMENT_INTERCOM_PTT) {
            imFragment.keyboardManage(false);
        }
//        vpIntercom.setCurrentItem(num, true);
        updateUI(num);
    }

    public void updateUI(int page) {
        Logger.d(TAG, "updateUI");
        switch (page) {
            case Util.FRAGMENT_INTERCOM_LIST:
                if (sessionType == SessionEntity.TYPE_DIALOG) {
                    ivInvitation.setVisibility(View.VISIBLE);
                    ivLock.setVisibility(View.GONE);
                }
                badgeView.setVisibility(View.GONE);
                changeCallOnlineState(true);
                break;
            case Util.FRAGMENT_INTERCOM_PTT:
                if (sessionType == SessionEntity.TYPE_DIALOG) {
                    ivInvitation.setVisibility(View.VISIBLE);
                    ivLock.setVisibility(View.GONE);
                }
                changeCallOnlineState(false);
                break;
            case Util.FRAGMENT_INTERCOM_IM:
                if (sessionType == SessionEntity.TYPE_DIALOG) {
                    ivInvitation.setVisibility(View.VISIBLE);
                    ivLock.setVisibility(View.GONE);
                }
                badgeView.setVisibility(View.GONE);
                changeCallOnlineState(false);
                break;
        }
    }

    /**
     * 更改群呼上线功能按钮显示状态
     *
     * @param isDisplay
     */
    private void changeCallOnlineState(boolean isDisplay) {
        if (sessionEntity.getSessionType() == SessionEntity.TYPE_GROUP) {
            if (isDisplay) {
                tvIntercomCallOnline.setVisibility(View.VISIBLE);
            } else {
                tvIntercomCallOnline.setVisibility(View.GONE);
            }
        } else {
            tvIntercomCallOnline.setVisibility(View.GONE);
        }
    }

    @Override
    public void showTalkPreparingStatus(SessionEntity session) {
        if (session.getSessionId().equals(sessionEntity.getSessionId())) {
            Logger.d(TAG, "showTalkPreparingStatus + MediaState = " + sessionEntity.getMediaState());
            notifyRequestTalk();
            notifyPttBtnUpdateStatus(EBIntercomStatus.MEDIA_PREPARE_STATUS);
        }
    }

    /**
     * 开始发言
     *
     * @param session
     */
    @Override
    public void showTalkStatus(SessionEntity session) {
        if (sessionEntity != null && sessionEntity.getSessionId().equals(session.getSessionId())) {
            Logger.d(TAG, "showTalkStatus + MediaState = " + sessionEntity.getMediaState());
            refreshMediaState(session);
            notifyPttBtnUpdateStatus(EBIntercomStatus.MEDIA_TALK_STATUS);
        }

    }

    /**
     * 启动计时
     */
    private void startTime() {
        Message msg = new Message();
        long currentTime = System.currentTimeMillis();
        msg.obj = currentTime - startSpeakTime + 1000;
        msg.what = OWN_SPEAKING;
        handler.sendMessageDelayed(msg, 1000);
    }

    /**
     * 发言结束
     *
     * @param session
     * @param reason
     */
    @Override
    public void showTalkEndStatus(SessionEntity session, int reason) {
        if (sessionEntity != null && sessionEntity.getSessionId().equals(session.getSessionId())) {
            Logger.d(TAG, "showTalkEndStatus + MediaState = " + sessionEntity.getMediaState() + "end reason:" + reason);
            handler.removeMessages(OWN_SPEAKING);
            refreshMediaState(session);
            notifyPttBtnUpdateStatus(EBIntercomStatus.MEDIA_IDLE_STATUS);

            switch (reason) {
                case SessionEntity.TALK_END_REASON_GRABED:
                    ToastUtil.shortShow(context, getString(R.string.intercom_channel_tip_media_interruptted));
                    break;
                case SessionEntity.TALK_END_REASON_TIMEOUT:
                    ToastUtil.shortShow(context, getString(R.string.intercom_channel_tip_media_timeout));
                    break;
                case SessionEntity.TALK_END_REASON_TIMEUP:
                    ToastUtil.shortShow(context, getString(R.string.intercom_channel_tip_media_timeup));
                    break;
                case SessionEntity.TALK_END_REASON_EXCEPTION:
                    ToastUtil.shortShow(context, getString(R.string.intercom_channel_tip_media_exception));
                    break;
                case SessionEntity.TALK_END_REASON_LISTEN_ONLY:
                    ToastUtil.shortShow(context, getString(R.string.intercom_channel_tip_media_listen_only));
                    break;
                case SessionEntity.TALK_END_REASON_SPEAKING_FULL:
                    ToastUtil.shortShow(context, getString(R.string.intercom_channel_tip_media_speak_full));
                    break;
            }
        }
    }

    /**
     * 对方发言开始
     *
     * @param session
     * @param contactEntity
     */
    @Override
    public void showListenStatus(SessionEntity session, ContactEntity contactEntity) {
        Logger.d(TAG, "showListenStatus  + MediaState = " + sessionEntity.getMediaState() + "  SessionEntity: MediaState = " + session.getMediaState());
        refreshMediaState(session);
        notifyPttBtnUpdateStatus(EBIntercomStatus.MEDIA_LISTEN_STATUS);
    }

    /**
     * 对方发言结束
     *
     * @param session
     */
    @Override
    public void showListenEndStatus(SessionEntity session) {
//        Logger.d(TAG, "showListenEndStatus  + MediaState = " + sessionEntity.getMediaState() + " SessionEntity: MediaState = " + session.getMediaState() + "   " + session.getChannelName());
        refreshMediaState(session);
    }

    @Override
    public void showMediaQueue(SessionEntity session, ArrayList<ContactEntity> arrayList) {
        if (sessionEntity != null && sessionEntity.getSessionId().equals(session.getSessionId())) {
            Logger.d(TAG, "showMediaQueue");
            notifySessionWaitStatus(arrayList.size());
            Logger.d(TAG, "MediaState = " + session.getMediaState());
        }
    }

    @Override
    public void mediaQueueIn(SessionEntity session) {
        if (sessionEntity != null && sessionEntity.getSessionId().equals(session.getSessionId())) {
            Logger.d(TAG, "mediaQueueIn");
            ToastUtil.shortShow(context, getString(R.string.intercom_channel_tip_media_queue_in));
            refreshMediaState(session);
            notifyPttBtnUpdateStatus(0);
        }

    }

    @Override
    public void mediaQueueOut(SessionEntity session) {
        if (sessionEntity != null && sessionEntity.getSessionId().equals(session.getSessionId())) {
            refreshMediaState(session);
            Logger.d(TAG, "mediaQueueOut");
            ToastUtil.shortShow(context, getString(R.string.intercom_channel_tip_media_queue_out));
            refreshMediaState(session);
            notifyPttBtnUpdateStatus(0);
        }

    }

    @Override
    public void sessionOutgoingRingingStatus(SessionEntity session) {
        if (sessionEntity != null && sessionEntity.getSessionId().equals(session.getSessionId())) {
            Logger.d(TAG, "sessionOutgoingRingingStatus");

        }

    }

    @Override
    public void sessionEstablishingStatus(SessionEntity session) {
        if (sessionEntity != null && sessionEntity.getSessionId().equals(session.getSessionId())) {
            Logger.d(TAG, "sessionEstablishingStatus + SessionState = " + sessionEntity.getSessionStatus());
            refreshMediaState(session);
            notifyPttBtnUpdateStatus(EBIntercomStatus.SESSION_CALLING_STATUS);
        }
    }

    @Override
    public void sessionEstablishedStatus(SessionEntity session, int ret) {
        if (sessionEntity != null && sessionEntity.getSessionId().equals(session.getSessionId())) {
            Logger.d(TAG, "sessionEstablishedStatus + SessionState = " + sessionEntity.getSessionStatus());
            refreshMediaState(sessionEntity);
            notifyPttBtnUpdateStatus(EBIntercomStatus.SESSION_DIALOG_STATUS);
        }
    }

    @Override
    public void sessionReleasedStatus(SessionEntity session, int reason) {
        if (sessionEntity != null && sessionEntity.getSessionId().equals(session.getSessionId())) {
            Logger.d(TAG, "sessionReleasedStatus:" + reason + " SessionState = " + sessionEntity.getSessionStatus());
            String prompt = "";
            if (session.getSessionType() == SessionEntity.TYPE_DIALOG)
                switch (reason) {
                    case SessionEntity.SESSION_RELEASE_REASON_GENERAL:
                        prompt = context.getString(R.string.talk_had_idel);
                        break;
                    case SessionEntity.SESSION_RELEASE_REASON_ERROR:
                        prompt = context.getString(R.string.talk_calling_fail);
                        break;
                    case SessionEntity.SESSION_RELEASE_REASON_NOTREACH:
                        prompt = context.getString(R.string.talk_calling_offline);
                        break;
                    case SessionEntity.SESSION_RELEASE_REASON_BUSY:
                        prompt = context.getString(R.string.talk_line_busy);
                        break;
                    case SessionEntity.SESSION_RELEASE_REASON_FORBIDDEN:
                        prompt = context.getString(R.string.talk_calling_server_deny);
                        break;
                    case SessionEntity.SESSION_RELEASE_REASON_FREQUENTLY:
                        prompt = context.getString(R.string.talk_calling_frequently);
                        break;
                    case SessionEntity.SESSION_RELEASE_REASON_REJECTED:
                        prompt = context.getString(R.string.talk_line_reject);
                        break;
                    case SessionEntity.SESSION_RELEASE_REASON_NOANSWER:
                        prompt = context.getString(R.string.talk_calling_noanswer);
                        break;
                    default:
                        prompt = context.getString(R.string.talk_had_idel);
                        break;
                }
            ToastUtil.shortShow(context, prompt);
            refreshMediaState(session);
            notifyPttBtnUpdateStatus(EBIntercomStatus.SESSION_IDLE_STATUS);

            imFragmentNotify();
        }
    }

    /**
     * 通知消息界面数据变动
     */
    public void imFragmentNotify() {
        imFragment.notifyDataSetChanged(true);
    }

    /**
     * 会话过程中,动态变化的用户参与此会话情况,均会通过此事件通知
     *
     * @param session         会话实例
     * @param membersAll      会话预期所有成员
     * @param membersPresence 会话实际参与成员
     */
    @Override
    public void sessionPresenceStatus(SessionEntity session, List<ContactEntity> membersAll, List<ContactEntity> membersPresence) {
        if (sessionEntity != null && sessionEntity.getSessionId().equals(session.getSessionId())) {
            Logger.d(TAG, "sessionPresenceStatus");
            if (session.getSessionType() == SessionEntity.TYPE_DIALOG) {
                tvTitleName.setText(sessionEntity.getSessionName());
            }
            // 通知会话成员列表刷新
            imFragmentNotify();
            listFragment.notifyDataSetChanged();
        }
    }

    @Override
    public void sessionMemberUpdateStatus(SessionEntity session, List<ContactEntity> list, boolean b) {
        if (sessionEntity != null && sessionEntity.getSessionId().equals(session.getSessionId())) {
            Logger.d(TAG, "sessionMemberUpdateStatus");
            if (session.getSessionType() == SessionEntity.TYPE_DIALOG) {
                tvTitleName.setText(sessionEntity.getSessionName());
            }
            // 通知会话成员列表刷新
            listFragment.notifyDataSetChanged();
            refreshViewBySessionEntity(sessionEntity);
        }
    }

    @Override
    public void sessionCenterCallRet(boolean ret, SessionEntity session) {
        Logger.i(TAG, "sessionCenterCallRet：" + ret);

        if (ret) {
            Intent intent = new Intent(this, IntercomActivity.class);
            SessionInitiationMan.SessionInitiation sessionInitiation = new SessionInitiationMan.SessionInitiation();
            sessionInitiation.sessionCode = session.getSessionId();
            sessionInitiation.initializationMode = SessionInitiationMan.INITIATION_PTT;
            sessionInitiation.connectionStatus = SessionInitiationMan.SESSION_CONNECTION;
            intent.putExtra(SessionInitiationMan.INTENT_DATA_KEY, sessionInitiation);

            startActivity(intent);
        } else {
            ToastUtil.longShow(context, getString(R.string.call_center_err));
            finish();
        }
    }

    @Override
    public void updateAudioWave(final byte[] bytes) {
//        Logger.d(TAG,"updateAudioWave");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                audioVirtualizerView.updateVisualizer(bytes);
            }
        });
    }

    @Override
    public void onJoinCallTimeOut() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (popupJoinSession != null && popupJoinSession.isShowing()) {
                    popupJoinSession.dismiss();
                }
            }
        });
    }

    /**
     * 更新消息未读数显示
     */
    public void undateUnreadCount() {
        Logger.d(TAG, "收到新消息 + 1");
        if (pageSelect != Util.FRAGMENT_INTERCOM_IM) {//当前界面不为即时聊天界面
            Logger.d(TAG, "更新数量+1");
            // 刷新界面未读消息数
            int unreadCount = sessionEntity.getMessageUnreadCount();
            if (unreadCount == 0) {
                badgeView.setVisibility(View.GONE);
            } else {
                if (pageSelect == Util.FRAGMENT_INTERCOM_PTT) {
                    badgeView.setVisibility(View.VISIBLE);
                    badgeView.show();
                }
            }
        }
    }

    /**
     * SessionEntity
     *
     * @return
     */
    public SessionEntity getSessionEntity() {
        return sessionEntity;
    }


    /**
     * EventBus
     * 通知ptt界面，会话按钮更新状态
     */
    public void notifyPttBtnUpdateStatus(int status) {
//        Logger.d(TAG,"notifyPttBtnUpdateStatus");
        EBIntercomStatus ebIntercomStatus = new EBIntercomStatus(status);
        EventBus.getDefault().post(ebIntercomStatus);
    }


    public void notifyRequestTalk() {
        ivChatPoint.setImageResource(R.drawable.intercom_state_busy);
        tvChatTitle.setText(getString(R.string.talk_request));
    }

    /**
     * 通知pttFragment显示
     *
     * @param waitSize
     */
    public void notifySessionWaitStatus(int waitSize) {
//        Logger.d(TAG,"notifySessionWaitStatus");
        EBSessionWait ebSessionWait = new EBSessionWait(waitSize);
        EventBus.getDefault().post(ebSessionWait);
    }


    /**
     * 刷新媒体状态
     */
    public void refreshMediaState(SessionEntity session) {
        if (sessionEntity != null && session != null) {
            if (session.getSessionStatus() == SessionEntity.SESSION_STATE_IDLE) {//会话未建立
                ivChatPoint.setImageResource(R.drawable.intercom_state_free);
                if (session.getSessionId().equals(SessionEntity.CENTER_NUM)) {
                    tvChatTitle.setText(getString(R.string.intercom_state_center_wait));
                } else {
                    tvChatTitle.setText(getString(R.string.intercom_state_free));
                }

                //停止定时器
                handler.removeMessages(OWN_SPEAKING);

                //频谱清空
                updateAudioWave(new byte[Util.AUDIO_VIRTUALLIZE_SPECTRUM_NUM]);
            } else if (session.getSessionStatus() == SessionEntity.SESSION_STATE_CALLING) { //会话建立中
                ivChatPoint.setImageResource(R.drawable.intercom_state_busy);
                tvChatTitle.setText(resources.getText(R.string.setting_up_the_session));
            } else if (session.getSessionStatus() == SessionEntity.SESSION_STATE_DIALOG) {
                switch (session.getMediaState()) {
                    case SessionEntity.MEDIA_STATE_IDLE://闲置
                        if (!session.getSessionId().equals(sessionEntity.getSessionId())) {
                            if (sessionType == SessionEntity.TYPE_GROUP) {
                                tvTitleName.setText(sessionEntity.getChannelName());
                            } else {
                                tvTitleName.setText(sessionEntity.getSessionName());
                            }
                        } else {
                            //停止定时器
                            handler.removeMessages(OWN_SPEAKING);

                            //频谱清空
                            updateAudioWave(new byte[Util.AUDIO_VIRTUALLIZE_SPECTRUM_NUM]);
                            ivChatPoint.setImageResource(R.drawable.intercom_state_free);
                            tvChatTitle.setText(getString(R.string.intercom_state_free));
                        }
                        break;
                    case SessionEntity.MEDIA_STATE_LISTEN://接听
                        if (sessionEntity.getSessionId().equals(session.getSessionId())) {
                            ivChatPoint.setImageResource(R.drawable.intercom_state_busy);
                            tvChatTitle.setText(sessionEntity.getSpeaker().getDisplayName() + getString(R.string.intercom_state_talking));
                        } else {
                            String otherChannelSpeaker;
                            if (session.getSessionType() == SessionEntity.TYPE_GROUP) {
                                otherChannelSpeaker = session.getChannelName() + ":" + session.getSpeaker().getDisplayName() + getString(R.string.intercom_state_talking);
                            } else {
                                otherChannelSpeaker = session.getSessionName() + ":" + session.getSpeaker().getDisplayName() + getString(R.string.intercom_state_talking);
                            }
                            tvTitleName.setText(otherChannelSpeaker);
                        }
                        break;
                    case SessionEntity.MEDIA_STATE_TALK://发言
                        if (!handler.hasMessages(OWN_SPEAKING)) {
                            startSpeakTime = System.currentTimeMillis();
                            startTime();
                            ivChatPoint.setImageResource(R.drawable.intercom_state_busy);
                            tvChatTitle.setText("00:00");
                        }
                        break;
                }
            }
        }
    }

    /**
     * 消息处理
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case OWN_SPEAKING:
                    String time = TimeUtil.getTimeFormatmmss((Long) msg.obj);
                    ivChatPoint.setImageResource(R.drawable.intercom_state_busy);
                    tvChatTitle.setText(time);
                    startTime();
                    break;
            }
        }
    };
    //预定义组会话被禁止
    public void onChannelTurnOff(String channelId){
        if (TextUtils.equals(sessionEntity.getSessionId(),channelId)){
            finish();
        }
    }

    /**
     * *****************************返回按钮*****************************
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KEYCODE_BACK) {
            if (sessionEntity.getSessionType() == SessionEntity.TYPE_GROUP) {
                finish();
            } else if (sessionEntity.getSessionType() == SessionEntity.TYPE_DIALOG) {
                //会话没有建立时直接退出界面，当建立过程时或建立成功了需要按2次返回键
                if (sessionEntity.getSessionStatus() == SessionEntity.SESSION_STATE_IDLE) {
                    finish();
                } else {
                    if ((System.currentTimeMillis() - mExitTime) > 2000) {
                        // 如果两次按键时间间隔大于2000毫秒，则不退出
                        ToastUtil.longShow(context, R.string.exit_session_tip);
                        mExitTime = System.currentTimeMillis();// 更新mExitTime
                    } else {// 否则结束会话
                        IntercomManager.INSTANCE.stopSessionCall(sessionEntity);
                        finish();
                    }
                }
            } else {
                hidePopwindow();
            }
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (Setting.getUseVolumeKey(IntercomActivity.this)) {
                if (sessionEntity != null && (event.getRepeatCount() == 0)) {
                    IntercomManager.INSTANCE.requestTalk(sessionEntity);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        if (action == KeyEvent.ACTION_DOWN) {
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && action == KeyEvent.ACTION_UP) {
            if (Setting.getUseVolumeKey(IntercomActivity.this)) {
                if (sessionEntity != null) {
                    IntercomManager.INSTANCE.releaseTalk(sessionEntity);
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * 直接建立链接
     **/
    public void directEstablishConnection(SessionEntity sessionEntity) {
        if (sessionEntity.getSessionStatus() == SessionEntity.SESSION_STATE_IDLE) {
            switch (sessionEntity.getSessionType()) {
                case SessionEntity.TYPE_DIALOG:
                    Logger.d(TAG, "TYPE_DIALOG");

                    if (sessionEntity.getSessionId().equals(SessionEntity.CENTER_NUM)) {
                        IntercomManager.INSTANCE.startCenterCall(context);
                        refreshMediaState(sessionEntity);
                    } else {
                        IntercomManager.INSTANCE.startSessionCall(sessionEntity);
                        IMManager.getInstance().generateSystemMessage(sessionEntity, getString(R.string.talk_call_state_outgoing_call), false);
                    }

                    break;
                case SessionEntity.TYPE_GROUP:
                    Logger.d(TAG, "TYPE_CHANNEL");
                    ChannelEntity channelEntity = new ChannelEntity();
                    channelEntity.setId(sessionEntity.getSessionId());
                    IntercomManager.INSTANCE.startSessionCall(channelEntity);
                    break;
            }
        }
    }

    /**
     * 播放 最近的一条ptt语音记录
     *
     * @param pttMessage
     */
    public void playLatestPttRecord(MessageEntity pttMessage) {
        imFragment.playLatestPttRecord(pttMessage);
    }

    /**
     * t停止 最近的一条ptt语音记录
     */
    public void stopLatestPttRecord() {
        imFragment.stopLatestPttRecord();
    }

    /**
     * 更新播放状态
     */
    public void updateLatestPttRecordStatus() {
        pttFragment.updateLatestPttRecordStatus();
    }

    /**
     * 通知更新最近的一条ptt语音
     */
    public void updateLastPttRecord() {
        pttFragment.updateLastPttRecord();
    }

    /**
     * 是否在IM界面
     *
     * @return
     */
    public boolean isImFragment() {
        return (pageSelect == Util.FRAGMENT_INTERCOM_IM);
    }

    /**
     * 创建PopupWindow
     */
    protected void initPopupWindow() {
        //获取自定义布局文件activity_pop_left.xml 布局文件
        final View popipWindow_view = getLayoutInflater().inflate(R.layout.popupwindow_invite_contact, null, false);
        //创建Popupwindow 实例，LayoutParams.MATCH_PARENT，LayoutParams.MATCH_PARENT 分别是宽高
        popupWindow = new PopupWindow(popipWindow_view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindow.setAnimationStyle(R.style.popwin_anim_style);
        lvPopInvite = (ListView) popipWindow_view.findViewById(R.id.lv_pop_invite_contact);
        rlBack = (RelativeLayout) popipWindow_view.findViewById(R.id.rl_select_back_pop);
        tvFinish = (TextView) popipWindow_view.findViewById(R.id.tv_finish_select);
        rlBack.setOnClickListener(this);
        tvFinish.setOnClickListener(this);
        popipWindow_view.setFocusable(true);
        popipWindow_view.setFocusableInTouchMode(true);
        popipWindow_view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (keyCode) {
                    case KEYCODE_BACK:
                        if (popupWindow != null) {
                            hidePopwindow();
                        }
                        return true;
                }
                return false;
            }
        });
//        initializePopwin();
    }

    /**
     * 初始化popupwindow
     */
    private void initializePopwin() {
        currentContactList = new ArrayList<>();
        contactList = new ArrayList<>();
        contactList = ContactManager.INSTANCE.getLocalContactList();
        ContactSPManager.deleteIterator(this, contactList);
        ContactSPManager.deleteExistIterator(contactList, sessionEntity);
        adapter = new InviteContactAdapter(this, contactList);
        lvPopInvite.setAdapter(adapter);
        lvPopInvite.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Set<String> selectOptions = adapter.getSelectOptions();
                String mdn = contactList.get(position).getMdn();
                //判断邀请人数是否达到上限
                if ((sessionEntity.getMemberSize() + currentContactList.size()) < Util.DIALOG_SESSION_MENBER_UPPER) {
                    if (selectOptions.contains(mdn)) {
                        selectOptions.remove(mdn);
                        currentContactList.remove(contactList.get(position));
                        adapter.setSelectOptions(selectOptions);
                        adapter.notifyDataSetChanged();
                    } else {
                        selectOptions.add(mdn);
                        currentContactList.add(contactList.get(position));
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    if (selectOptions.contains(mdn)) {
                        selectOptions.remove(mdn);
                        currentContactList.remove(contactList.get(position));
                        adapter.notifyDataSetChanged();
                    } else {
                        ToastUtil.shortShow(context, R.string.invite_contact_number_upper);
                    }
                }
            }
        });
    }

    /**
     * 获取PopipWinsow实例
     */
    private void getPopupWindow() {
        if (popupWindow == null) {
            initPopupWindow();
        }
        initializePopwin();
    }
}
