package com.ctchat.sample.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;


import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sdk.basemodule.util.ToastUtil;
import com.ctchat.sample.R;
import com.ctchat.sdk.ptt.tool.chatIM.IMManager;
import com.ctchat.sdk.ptt.tool.entity.ChannelEntity;
import com.ctchat.sdk.ptt.tool.entity.MessageEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;
import com.ctchat.sdk.ptt.tool.intercom.IntercomManager;
import com.ctchat.sdk.ptt.tool.userinfo.UserInfoManager;
import com.ctchat.sample.IntercomActivity;
import com.ctchat.sample.application.WeApplication;
import com.ctchat.sample.presenter.IntercomPttPresent;
import com.ctchat.sample.presenter.IntercomPttPresentImpl;
import com.ctchat.sample.tool.EBIntercomStatus;
import com.ctchat.sample.view.IntercomPttView;
import com.ctchat.sample.widget.CircleImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 群组对讲
 */
public class IntercomPttFragment extends BaseFragment implements IntercomPttView, View.OnClickListener {
    private static final String TAG = "IntercomPttFragment";
    private Context mContext;
    private View view;
    private ImageView ivIntercomSpeech;
    private CircleImageView ivIntercomPttMic;
    private TextView tvIntercomSpeechTitle, tvIntercomTime, tvSessionWait;
    private LinearLayout llPlayLastPTT;

    private IntercomPttPresent mPresent;
    private SessionEntity sessionEntity;
    private IntercomActivity intercomActivity;
    private boolean isLongClick = false;
    private MessageEntity lastPttRecord;
    private PopupWindow popupWindowSpeechModel;
    private ImageView ivPatternSystem, ivPatternBluetooth;
    private View popupWindowView;
    private Button ivIntercomPttHangup;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "IntercomPttFragment onCreate");
        mContext = getActivity();
        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_intercom_ptt, container, false);
        initView(view);
        initData();
        initPttStatus();
        Logger.d(TAG, "IntercomPttFragment onCreateView");
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresent.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.d(TAG, "IntercomPttFragment onResume");
        mPresent.onResume();
        EBIntercomStatus ebIntercomStatus = new EBIntercomStatus(0);
        updateIntercomBtnStatus(ebIntercomStatus);

        updateLastPttRecord();
    }

    @Override
    public void initData() {
        mPresent = new IntercomPttPresentImpl(this);
        intercomActivity = (IntercomActivity) getActivity();
        sessionEntity = intercomActivity.getSessionEntity();
    }

    @Override
    public void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }

    @Override
    public void initView(View view) {
        ivIntercomPttHangup = (Button) view.findViewById(R.id.iv_intercom_ptt_hangup);
        ivIntercomPttMic = (CircleImageView) view.findViewById(R.id.iv_intercom_ptt_mic);
        ivIntercomSpeech = (ImageView) view.findViewById(R.id.iv_intercom_speech);
        tvIntercomSpeechTitle = (TextView) view.findViewById(R.id.tv_intercom_speechme_title);
        tvIntercomTime = (TextView) view.findViewById(R.id.tv_intercom_speechme_time);
        tvSessionWait = (TextView) view.findViewById(R.id.tv_session_wait);
        llPlayLastPTT = (LinearLayout) view.findViewById(R.id.ll_play_last_ptt_record);

        ivIntercomPttMic.setOnTouchListener(pttMicTouchListener);
        ivIntercomPttMic.setOnLongClickListener(pttMicLongClickListener);
        ivIntercomPttHangup.setOnClickListener(this);
        llPlayLastPTT.setOnClickListener(this);
        tvIntercomTime.setOnClickListener(this);
        tvIntercomSpeechTitle.setOnClickListener(this);
        ivIntercomSpeech.setOnClickListener(this);

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (popupWindowView != null && popupWindowView.isShown()) {
                    popupWindowSpeechModel.dismiss();
                }
                return false;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_intercom_ptt_hangup:
                IntercomManager.INSTANCE.stopSessionCall(sessionEntity);
                getActivity().finish();
                break;
            case R.id.tv_intercom_speechme_title:
            case R.id.tv_intercom_speechme_time:
            case R.id.iv_intercom_speech:
            case R.id.ll_play_last_ptt_record:
                playLastPttRecord();
                break;
        }
    }

    /**
     * 初始化最近一次的ptt语音消息
     */
    public void updateLastPttRecord() {
        lastPttRecord = sessionEntity.getMessagePlayback();
        if (lastPttRecord != null) {
            String name = lastPttRecord.getNameFrom();
            boolean isSelf = lastPttRecord.isSelf(intercomActivity);
            tvIntercomSpeechTitle.setText((isSelf ? intercomActivity.getString(R.string.name_from_me) : name) + "  " + lastPttRecord.getRecordTime() + "\"");
            tvIntercomTime.setText(lastPttRecord.getTime());
        } else {
            tvIntercomSpeechTitle.setText(R.string.no_ptt_record);
        }
    }

    /**
     * 播放最近一次ptt语音
     */
    private void playLastPttRecord() {
        if (lastPttRecord != null) {
            if (lastPttRecord.isRecordPlaying()) {
                intercomActivity.stopLatestPttRecord();
            } else {
                intercomActivity.playLatestPttRecord(lastPttRecord);
            }
        }
    }

    /**
     * 更新播放状态
     */
    public void updateLatestPttRecordStatus() {
        Logger.d(TAG, "updateLatestPttRecordStatus");
        if (lastPttRecord.isRecordPlaying()) {
            ivIntercomSpeech.setImageResource(R.drawable.intercom_speechme_stop);
        } else {
            ivIntercomSpeech.setImageResource(R.drawable.intercom_speechme_start);
        }
    }

    View.OnTouchListener pttMicTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (null != sessionEntity && !UserInfoManager.isCalling) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                            if (sessionEntity.getSessionStatus() == SessionEntity.SESSION_STATE_DIALOG) {
                                IntercomManager.INSTANCE.releaseTalk(sessionEntity);
                                isLongClick = false;
                            }
                        break;
                    case MotionEvent.ACTION_DOWN:
                            if (sessionEntity.getSessionStatus() == SessionEntity.SESSION_STATE_DIALOG) {
                                IntercomManager.INSTANCE.requestTalk(sessionEntity);
                            } else if (sessionEntity.getSessionStatus() == SessionEntity.SESSION_STATE_IDLE) {
                                switch (sessionEntity.getSessionType()) {
                                    case SessionEntity.TYPE_DIALOG:
                                        Logger.d(TAG, "TYPE_DIALOG");
                                        if (sessionEntity.getSessionId().equals(SessionEntity.CENTER_NUM)) {
                                            IntercomManager.INSTANCE.startCenterCall(getActivity());
                                        } else {
                                            IntercomManager.INSTANCE.startSessionCall(sessionEntity);
                                            Log.d(TAG, "SESSION_STATE_IDLE generateSystemMessage");
                                            IMManager.getInstance().generateSystemMessage(sessionEntity, getString(R.string.talk_call_state_outgoing_call), false);
                                            intercomActivity.imFragmentNotify();
                                        }
                                        break;
                                    case SessionEntity.TYPE_GROUP:
                                        Logger.d(TAG, "TYPE_CHANNEL");
                                        ChannelEntity channelEntity = new ChannelEntity();
                                        channelEntity.setId(sessionEntity.getSessionId());
                                        IntercomManager.INSTANCE.startSessionCall(channelEntity);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        //}
                        break;
                    case MotionEvent.ACTION_OUTSIDE:
                        if (sessionEntity.getSessionStatus() == SessionEntity.SESSION_STATE_DIALOG) {
                            IntercomManager.INSTANCE.releaseTalk(sessionEntity);
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        if (sessionEntity.getSessionStatus() == SessionEntity.SESSION_STATE_DIALOG) {
                            IntercomManager.INSTANCE.releaseTalk(sessionEntity);
                        }
                        break;
                    default:
                        break;
                }
            }
            return false;
        }
    };

    private View.OnLongClickListener pttMicLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            Logger.i(TAG, "isLongClick = true");
            isLongClick = true;
            return false;
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateIntercomBtnStatus(EBIntercomStatus ebIntercomStatus) {
        Logger.i(TAG, "updateIntercomBtnStatus");
        if (sessionEntity != null) {
            ivIntercomPttMic.clearAnimation();
            switch (sessionEntity.getMediaButtonState()) {
                case SessionEntity.MEDIA_BUTTON_STATE_IDLE:
                    Logger.i(TAG, "MEDIA_BUTTON_STATE_IDLE");
                    if (sessionEntity.getSessionStatus() == SessionEntity.SESSION_STATE_DIALOG) {
                        ivIntercomPttMic.setImageResource(R.drawable.speak_clicked_default);
                    } else {
                        ivIntercomPttMic.setImageResource(R.drawable.speak_call);
                    }
                    break;
                case SessionEntity.MEDIA_BUTTON_STATE_CONNECTING:
                    Logger.i(TAG, "MEDIA_BUTTON_STATE_CONNECTING");
                    ivIntercomPttMic.setImageResource(R.drawable.speak_call);
                    Animation connectAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_session_btn_connecting);
                    ivIntercomPttMic.startAnimation(connectAnim);
                    break;
                case SessionEntity.MEDIA_BUTTON_STATE_REQUESTING:
                    Logger.i(TAG, "MEDIA_BUTTON_STATE_REQUESTING");
                    ivIntercomPttMic.setImageResource(R.drawable.speak_clicked_speaking);
                    break;
                case SessionEntity.MEDIA_BUTTON_STATE_RELEASING:
                    Logger.i(TAG, "MEDIA_BUTTON_STATE_RELEASING");
                    ivIntercomPttMic.setImageResource(R.drawable.speak_clicked_default);
                    break;
                case SessionEntity.MEDIA_BUTTON_STATE_TALKING:
                    Logger.i(TAG, "MEDIA_BUTTON_STATE_TALKING");
                    ivIntercomPttMic.setImageResource(R.drawable.speak_clicked_speaking);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void initPttStatus() {
        Logger.i(TAG, "initPttStatus");
        if (sessionEntity != null) {
            int sessionType = sessionEntity.getSessionType();
            //判断会话类型
            if (sessionType == SessionEntity.TYPE_GROUP) {
                ivIntercomPttHangup.setVisibility(View.GONE);
            } else {
                ivIntercomPttHangup.setVisibility(View.VISIBLE);
            }
            //判断会话建立状态
            Logger.i(TAG, "sessionStatus = " + sessionEntity.getSessionStatus());
            if (sessionEntity.getSessionStatus() == SessionEntity.SESSION_STATE_DIALOG) {
                ivIntercomPttMic.setImageResource(R.drawable.speak_clicked_default);
            } else {
                ivIntercomPttMic.setImageResource(R.drawable.speak_call);
            }
        }

    }

    @Override
    public void updateSessionLockView(boolean status) {
    }

    @Override
    public void updateSessionPreemptionView(boolean status) {
    }

    @Override
    public void onBluetoothConnectChangeEvent(int state) {
        Logger.i(TAG," bt connect change receive:"+state);
    }

}
