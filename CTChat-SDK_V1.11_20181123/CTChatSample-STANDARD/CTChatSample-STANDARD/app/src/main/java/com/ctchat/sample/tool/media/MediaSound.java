package com.ctchat.sample.tool.media;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;
import com.ctchat.sdk.ptt.tool.media.OnMediaSoundEventListener;
import com.ctchat.sample.io.Setting;
import com.ctchat.sample.tool.Sound;

public class MediaSound implements OnMediaSoundEventListener {
    private static final String TAG = "MediaSound";
    private Context mContext = null;

    static AudioManager audioManager;

    // 自己是否在发言
    private static boolean isTalking = false;
    // 其他发言人数
    private static int otherTalkCount = 0;

    public MediaSound(Context context) {
        mContext = context;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    static AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
        }
    };

    private static void changeAudioFocus(boolean isFocus) {
        if (isFocus) {
            try {
                audioManager.requestAudioFocus(audioFocusChangeListener, Setting.getStreamType(), AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            } catch (Exception exception) {
                Logger.e(TAG, "Request Focus ERR:" + exception.getMessage());
            }
        } else {
            audioManager.abandonAudioFocus(audioFocusChangeListener);
        }
    }

    public static void resetState(Context context) {
        toggleLight(false, 0, context);
        changeAudioFocus(false);
        isTalking = false;
        otherTalkCount = 0;
    }

    /**
     * 申请占用音频设备
     */
    public static void requestAudioState() {
        changeAudioFocus(true);
    }

    /**
     * 释放占用的音频设备
     */
    public static void releaseAudioState() {
        changeAudioFocus(false);
    }


    /********************** Media Sound **********************/
    @Override
    public void onMediaSoundTalkBegin(SessionEntity airSession) {
        Logger.i(TAG, "onMediaSoundTalkBegin：" + airSession.getSessionId(), true);
        Sound.vibrate(mContext);
        requestAudioState();
        Sound.playSound(Sound.PLAYER_MEDIA_ME_ON, mContext);

        // 自己发言开始
        isTalking = true;
        refreshLEDLight();
    }

    @Override
    public void onMediaSoundTalkEnd(SessionEntity airSession) {
        Logger.i(TAG, "onMediaSoundTalkEnd:" + airSession.getSessionId(), true);
        Sound.playSound(Sound.PLAYER_MEDIA_ME_OFF, mContext);
        releaseAudioState();
//        RequestMicTool.requestMic(mContext, false);
        // 自己发言结束
        isTalking = false;
        refreshLEDLight();
    }

    @Override
    public void onMediaSoundTalkDeny(SessionEntity airSession) {
//        RequestMicTool.requestMic(mContext, false);
    }

    @Override
    public void onMediaSoundTalkRequestBegin(SessionEntity airSession) {
    }

    @Override
    public void onMediaSoundTalkRequestEnd(SessionEntity airSession) {

    }

    @Override
    public void onMediaSoundListenBegin(SessionEntity airSession, String s) {
        Logger.i(TAG, "onMediaSoundListenBegin:" + airSession.getSessionId() + " s:" + s, true);

        requestAudioState();
        Sound.playSound(Sound.PLAYER_MEDIA_OTHER_ON, mContext);
        Sound.vibrate(30, mContext);
        // 开始收听其他人发言
        otherTalkCount++;
        Logger.d(TAG, "onMediaSoundListenBegin otherTalkCount : " + otherTalkCount, true);
        refreshLEDLight();
    }

    @Override
    public void onMediaSoundListenEnd(SessionEntity airSession) {
        Logger.i(TAG, "onMediaSoundListenEnd:" + airSession.getSessionId(), true);
        Sound.playSound(Sound.PLAYER_MEDIA_OTHER_OFF, mContext);
        releaseAudioState();

        // 对方某一人发言结束
        otherTalkCount--;
        Logger.d(TAG, "onMediaSoundListenEnd otherTalkCount : " + otherTalkCount, true);
        refreshLEDLight();
    }

    /**
     * 刷新LED灯状态
     */
    private void refreshLEDLight() {
        if (isTalking) { // 自己在发言
            // 亮红灯
            toggleLight(true, LEDConfig.LED_COLOR_RED, mContext);
//            BluetoothManager.getInstance().toggleBtLight(true, LEDConfig.LED_COLOR_RED);
        } else {
            Logger.d(TAG, "refreshLEDLight otherTalkCount : " + otherTalkCount, true);
            if (otherTalkCount == 0) { // 无其他发言者
                Logger.d(TAG, "otherTalkCount == 0", true);
                // 灭灯
                toggleLight(false, 0, mContext);
//                BluetoothManager.getInstance().toggleBtLight(false, 0);
            } else {
                // 亮绿灯
                toggleLight(true, LEDConfig.LED_COLOR_GREEN, mContext);
//                BluetoothManager.getInstance().toggleBtLight(true, LEDConfig.LED_COLOR_GREEN);
            }
        }
    }

    /****************** 话权指示灯 *******************/

    public static void toggleLight(boolean turnOn, int color, Context context) {
        Intent lightIntent = new Intent();

        if (turnOn) {
            switch (color) {
                case LEDConfig.LED_COLOR_RED:
                    Logger.d(TAG, "toggleLight RED", true);
                    lightIntent.setAction(LEDConfig.LED_ACTION_PTT_RED);
                    break;
                case LEDConfig.LED_COLOR_GREEN:
                    Logger.d(TAG, "toggleLight GREEN", true);
                    lightIntent.setAction(LEDConfig.LED_ACTION_PTT_GREEN);
                    break;
                default:
                    break;
            }
        } else {
            Logger.d(TAG, "toggleLight OFF", true);
            lightIntent.setAction(LEDConfig.LED_ACTION_PTT_TURN_OFF);
        }

        lightIntent.putExtra("on", turnOn);
        lightIntent.putExtra("color", color);

        context.sendBroadcast(lightIntent);
    }
}
