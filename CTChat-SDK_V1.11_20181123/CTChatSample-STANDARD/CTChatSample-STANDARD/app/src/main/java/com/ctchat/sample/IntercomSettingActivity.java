package com.ctchat.sample;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sdk.basemodule.util.PrefUtil;
import com.ctchat.sample.widget.CustomHintDialog;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;
import com.ctchat.sdk.ptt.tool.intercom.IntercomManager;
import com.ctchat.sdk.ptt.tool.setting.SettingManager;
import com.ctchat.sample.io.Setting;
import com.ctchat.sample.widget.SingleChoiceDialog;
import com.ctchat.sample.widget.XpActivity;

public class IntercomSettingActivity extends XpActivity {
    private static final String TAG = "IntercomSettingActivity";
    private Context mContext;
    private Button toggleMicrophone;
    private Button toggleIncreaseQuality;
    private Button toggleVolumeKey;
    private Button toggleAutoAnswer;
    private Button toggleNotDisturb;
    private RelativeLayout heartBeatLayout;
    private RelativeLayout clearHistoryLayout;
    private TextView tvHeartBeatFrequency;
    private RelativeLayout backLayout;
    private String[] items;
    private int[] heartBeatItems;
    private String heartBeatFrequencyMode;
    private static final String KEY_HEART_BEAT_FREQUENCY = "keyHeartBeatFrequency";
    private SingleChoiceDialog.Builder builder;
    private SingleChoiceDialog singleDialog;
    private static final String KEY_CURRENT_ITEM = "KeyCurrentItem";
    private static final String OFF_TEXT = "已关";
    private static final String ON_TEXT = "已开";
    private static final boolean ON = true;
    private static final boolean OFF = false;

    @Override
    protected void initView() {
        mContext = IntercomSettingActivity.this;
        backLayout = (RelativeLayout) findViewById(R.id.rl_intercom_setting_back_to_main);
        backLayout.setOnClickListener(this);


        // 声音放大器
        toggleMicrophone = (Button) findViewById(R.id.tog_btn_volume_microphone);
        toggleMicrophone.setText(Setting.getAudioAmplifier(this) ? ON_TEXT : OFF_TEXT);
        toggleMicrophone.setOnClickListener(this);

        // 增强对讲音质
        toggleIncreaseQuality = (Button) findViewById(R.id.tog_btn_increase_intercom_quality);
        toggleIncreaseQuality.setText(Setting.getIncreaseIntercomQuailty(this) ? ON_TEXT : OFF_TEXT);
        toggleIncreaseQuality.setOnClickListener(this);

        // 音量按键
        toggleVolumeKey = (Button) findViewById(R.id.tog_btn_volume_key);
        toggleVolumeKey.setText(Setting.getUseVolumeKey(this) ? ON_TEXT : OFF_TEXT);
        toggleVolumeKey.setOnClickListener(this);

        // 自动应答
        toggleAutoAnswer = (Button) findViewById(R.id.tog_btn_auto_answer);
        toggleAutoAnswer.setText(Setting.getAnswerMode(mContext) ? ON_TEXT : OFF_TEXT);
        toggleAutoAnswer.setOnClickListener(this);

        // 免打扰
        toggleNotDisturb = (Button) findViewById(R.id.tog_btn_not_disturb);
        toggleNotDisturb.setText(Setting.getNotDisturbMode(mContext) ? ON_TEXT : OFF_TEXT);
        toggleNotDisturb.setOnClickListener(this);

        // 心跳频率设置
        heartBeatLayout = (RelativeLayout) findViewById(R.id.rl_heart_beat_frequency);
        heartBeatLayout.setOnClickListener(this);
        tvHeartBeatFrequency = (TextView) findViewById(R.id.tv_heart_beat_frequency);
        heartBeatFrequencyMode = (String) PrefUtil.get(mContext, KEY_HEART_BEAT_FREQUENCY, mContext.getResources().getString(R.string.normal_mode_time));
        tvHeartBeatFrequency.setText(heartBeatFrequencyMode);
        items = new String[]{mContext.getResources().getString(R.string.high_performance_time), mContext.getResources().getString(R.string.faster_time),
                mContext.getResources().getString(R.string.medium_performance_time), mContext.getResources().getString(R.string.normal_mode_time),
                mContext.getResources().getString(R.string.low_speed_time)};
        heartBeatItems = new int[]{5, 10, 15, 20, 40};
        builder = new SingleChoiceDialog.Builder(this);
        builder.setTitle(R.string.heart_beat_frequency);
        int currentPosition = (int) PrefUtil.get(mContext, KEY_CURRENT_ITEM, 3);
        builder.setItems(items, currentPosition);
        builder.setConfirmButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (Setting.setHeartBeatFrequency(mContext, heartBeatItems[position])) {
                    heartBeatFrequencyMode = items[position];
                    tvHeartBeatFrequency.setText(heartBeatFrequencyMode.toString());
                    PrefUtil.put(mContext, KEY_HEART_BEAT_FREQUENCY, heartBeatFrequencyMode);
                    PrefUtil.put(mContext, KEY_CURRENT_ITEM, position);
                } else {
                    Toast.makeText(mContext, "心跳频度超限，设置失败", Toast.LENGTH_SHORT).show();
                }
                singleDialog.dismiss();
            }
        });
        singleDialog = builder.create();

        // 清空聊天记录
        clearHistoryLayout = (RelativeLayout) findViewById(R.id.rl_clear_chat_history);
        clearHistoryLayout.setOnClickListener(this);
    }


    @Override
    protected void initData() {
    }

    private void showClearHistoryDialog() {
        CustomHintDialog.Builder builder = new CustomHintDialog.Builder(mContext);
        builder.setTitle(R.string.hint);
        builder.setMessage(R.string.confirm_clear_history);
        builder.setConfirmButton(R.string.sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // 清空聊天记录
                IntercomManager.INSTANCE.cleanSessionEntityList();
            }
        });
        builder.setBackButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_intercom_setting;
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
            case R.id.rl_intercom_setting_back_to_main:
                finish();
                break;
            case R.id.rl_heart_beat_frequency:
                if (builder != null) {
                    singleDialog.show();
                }
                break;
            case R.id.tog_btn_volume_microphone:
                if (toggleMicrophone.getText().equals(OFF_TEXT)) {
                    Setting.setAudioAmplifier(mContext, ON);
                    toggleMicrophone.setText(ON_TEXT);
                } else {
                    Setting.setAudioAmplifier(mContext, OFF);
                    toggleMicrophone.setText(OFF_TEXT);
                }
                break;
            case R.id.tog_btn_increase_intercom_quality:
                if (toggleIncreaseQuality.getText().equals(OFF_TEXT)) {
                    Setting.setIncreaseIntercomQuailty(mContext, ON, SettingManager.SESSION_AUDIO_AMR_MODE_7);
                    toggleIncreaseQuality.setText(ON_TEXT);
                } else {
                    Setting.setIncreaseIntercomQuailty(mContext, OFF, SettingManager.SESSION_AUDIO_AMR_MODE_1);
                    toggleIncreaseQuality.setText(OFF_TEXT);
                }
                break;
            case R.id.tog_btn_volume_key:
                if (toggleVolumeKey.getText().equals(OFF_TEXT)) {
                    Setting.setUseVolumeKey(mContext, ON);
                    toggleVolumeKey.setText(ON_TEXT);
                } else {
                    Setting.setUseVolumeKey(mContext, OFF);
                    toggleVolumeKey.setText(OFF_TEXT);
                }
                break;
            case R.id.tog_btn_auto_answer:
//                if (!Setting.getNotDisturbMode(mContext)) {
                if (toggleAutoAnswer.getText().equals(OFF_TEXT)) {
                    Logger.i(TAG, "answerMode 0");
                    Setting.setAnswerMode(mContext, ON, SessionEntity.SESSION_ANSWER_MODE_AUTO);
                    toggleAutoAnswer.setText(ON_TEXT);
                } else {
                    Logger.i(TAG, "answerMode 1");
                    Setting.setAnswerMode(mContext, OFF, SessionEntity.SESSION_ANSWER_MODE_MANUAL);
                    toggleAutoAnswer.setText(OFF_TEXT);
                }
                break;
            case R.id.tog_btn_not_disturb:
                if (toggleNotDisturb.getText().equals(OFF_TEXT)) {
                    Setting.setAnswerMode(mContext, ON, SessionEntity.SESSION_ANSWER_MODE_MANUAL);
                    Setting.setNotDisturbMode(mContext, ON);
                    toggleNotDisturb.setText(ON_TEXT);
                } else {
                    Setting.setAnswerMode(mContext, OFF, SessionEntity.SESSION_ANSWER_MODE_MANUAL);
                    Setting.setNotDisturbMode(mContext, OFF);
                    toggleNotDisturb.setText(OFF_TEXT);
                }
                break;
            case R.id.rl_clear_chat_history:
                showClearHistoryDialog();
                break;
        }
    }
}
