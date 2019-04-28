package com.ctchat.sample.tool;

import android.app.Service;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Vibrator;

import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sample.R;
import com.ctchat.sdk.ptt.tool.userinfo.UserInfoManager;
import com.ctchat.sample.io.Setting;

import java.util.HashMap;
import java.util.Map;

/**
 * Sound handler
 */
public class Sound {

    private static final String TAG = "Sound";
    public static final int PLAYER_IDS_MAX = 18;

    //使用SoundPool 播放的音效文件
    private static final int[] PLAYER_EFFECT = {
            R.raw.sound_media_me_on, // PLAYER_MEDIA_ME_ON
            R.raw.sound_media_me_off, // PLAYER_MEDIA_ME_OFF
            R.raw.sound_media_othe_on, // PLAYER_MEDIA_OTHER_ON
            R.raw.sound_media_othe_off // PLAYER_MEDIA_OTHER_OFF
    };

    //使用MediaPlayer 播放的音效文件
    private static final int[] PLAYER_RING = {0,
            R.raw.sound_media_knock, // PLAYER_MEDIA_KNOCK
            R.raw.sound_media_error, // PLAYER_MEDIA_ERROR
            R.raw.sound_callbegin, // PALYER_CALL_BEGIN
            R.raw.sound_callend, // PALYER_CALL_END
            R.raw.sound_callerror, // PALYER_CALL_ERROR
            R.raw.sound_call_dial // PLAYER_CALL_DIAL
    };

    private static AudioManager audioManager;
    private static SoundPool pool;

    public static final int PLAYER_MEDIA_ME_ON = 0;
    public static final int PLAYER_MEDIA_ME_OFF = 1;
    public static final int PLAYER_MEDIA_OTHER_ON = 2;
    public static final int PLAYER_MEDIA_OTHER_OFF = 3;

    private static final int EFFECT_MAX = 100;  //使用SoundPool播放的音效文件最大个数

    public static final int PLAYER_NEWINFO = 100;
    public static final int PLAYER_MEDIA_KNOCK = 101;
    public static final int PLAYER_MEDIA_ERROR = 102;
    public static final int PLAYER_CALL_BEGIN = 103;
    public static final int PLAYER_CALL_END = 104;
    public static final int PLAYER_CALL_ERROR = 105;
    public static final int PLAYER_CALL_DIAL = 106;
    public static final int PLAYER_INCOMING_RING = 107;

    public static MediaPlayer[] mediaPlayer = null;
    private static boolean alert = true;
    private static Vibrator vibrator = null;
    private static String currentSystemMusic = "";

    private static Map<Integer, SoundInfo> soundIds = new HashMap();

    private static class SoundInfo {
        int soundID = -1;
        int streamID = 0;
        int streamVolume = 0;
    }

    public static void soundInit(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);


        pool = new SoundPool(PLAYER_EFFECT.length, Setting.getStreamType(), 100);

        for (int i = 0; i < PLAYER_EFFECT.length; i++) {
            SoundInfo info = new SoundInfo();
            info.soundID = pool.load(context, PLAYER_EFFECT[i], 1);
            soundIds.put(i, info);
        }
    }

    public static void soundRelease() {
        if (pool != null) {
            for (int i = 0; i < PLAYER_EFFECT.length; i++) {
                SoundInfo info = soundIds.get(i);
                pool.unload(info.soundID);
            }
            pool.release();
        }

        try {
            if (mediaPlayer == null) {
                return;
            }
            for (MediaPlayer mp : mediaPlayer) {
                if (mp != null) {
                    mp.stop();
                    mp.release();
                }
            }
            mediaPlayer = null;
        } catch (Exception e) {
            Logger.e(TAG, " Exception clearSound Error =" + e.toString());
        }
    }

    public static void setSoundAlert(boolean silent) {
        alert = silent;
    }

    public static boolean isAlert() {
        return alert;
    }

    public static void vibrate(Context context) {
        if (context != null) {
            vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(50);
            }
        }
    }

    public static void vibrate(int msecond, Context context) {
        if (context != null) {
            vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(msecond);
            }
        }
    }

    public static void playSound(int playerId, Context context) {
        playSound(playerId, false, context);
    }

    public static void playSound(int playerId, boolean isLooping, Context context) {
        if (UserInfoManager.isCalling) {
            return;
        }

        Logger.d(TAG, "startSound-playerId" + playerId);

        if (playerId < EFFECT_MAX) {
            playEffect(playerId, isLooping, context);
        } else {
            playRing(playerId, isLooping, context);
        }

        // soundPlaying = false;
    }

    public static void stopSound(int playerId) {
        if (playerId < EFFECT_MAX) {
            stopEffect(playerId);
        } else {
            stopRing(playerId);
        }
    }

    /****************************** 音效相关（添加SoundPool缓存池 防止破音）****************************/
    /**
     * 播放音效相关（声音较短的文件）
     *
     * @param playerId
     * @param isLooping
     * @param context
     */
    private static void playEffect(int playerId, boolean isLooping, Context context) {
        if (UserInfoManager.isCalling) {
            return;
        }

        SoundInfo info = soundIds.get(playerId);

        if (info != null) {
            info.streamVolume = audioManager.getStreamVolume(Setting.getStreamType());
            int soundId = info.soundID;
            int isLoop = 0;

            if (isLooping) {
                isLoop = -1;
            }

            info.streamID = pool.play(soundId, 1.0F, 1.0F, 1, isLoop, 1.0F);
            Logger.i(TAG, "soundPlay playerId=" + playerId + "  streamID=" + info.streamID + " soundID=" + info.soundID + " streamVolume=" + info.streamVolume);

        }
    }

    /**
     * 停止播放音效相关文件
     *
     * @param playerId
     */
    private static void stopEffect(int playerId) {
        SoundInfo info = soundIds.get(playerId);
        if (info != null) {
            pool.stop(info.streamID);
        }
    }


    /****************************** 铃声相关（添加SoundPool缓存池 防止破音）*****************************/
    private static void playRing(int playerId, boolean isLooping, Context context) {
        int ringID = playerId - EFFECT_MAX;

        try {
            Logger.d(TAG, "startSound-begin");
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer[PLAYER_IDS_MAX];
            }
            Logger.d(TAG, "startSound");
            switch (playerId) {
                case PLAYER_NEWINFO:
                case PLAYER_INCOMING_RING: {
                    if (context != null) {
                        int name = (playerId == PLAYER_INCOMING_RING) ? RingtoneManager.TYPE_RINGTONE : RingtoneManager.TYPE_NOTIFICATION;

                        Uri systemMucUri = RingtoneManager.getActualDefaultRingtoneUri(context, name);
                        String systemMuc = systemMucUri.toString();
                        if (mediaPlayer[ringID] == null || !systemMuc.equals(currentSystemMusic)) {
                            currentSystemMusic = systemMuc;
                            mediaPlayer[ringID] = new MediaPlayer();
                            mediaPlayer[ringID].setDataSource(context, systemMucUri);
                            mediaPlayer[ringID].prepare();
                        }
                        if (mediaPlayer[ringID] != null) {
                            mediaPlayer[ringID].start();
                            mediaPlayer[ringID].setLooping(isLooping);
                        }
                    }
                    break;
                }
                default: {
                    if (mediaPlayer[ringID] == null) {
                        mediaPlayer[ringID] = MediaPlayer.create(context, PLAYER_RING[ringID]);
                    }
                    Logger.d(TAG, "startSound() playerId=" + playerId);
                    if (mediaPlayer[ringID] != null) {
                        if (!mediaPlayer[ringID].isPlaying()) {
                            mediaPlayer[ringID].start();
                            mediaPlayer[ringID].setLooping(isLooping);
                        }
                    } else {
                        Logger.w(TAG, "startSound() playerId=" + playerId + "create failed!");
                    }
                    break;
                }
            }

        } catch (Exception e) {
            Logger.e(TAG, " Exception startSound Error =" + e.toString());
        }
    }

    private static void stopRing(int playerId) {
        int ringId = playerId - EFFECT_MAX;
        try {
            if (mediaPlayer != null && mediaPlayer[ringId] != null) {
                mediaPlayer[ringId].stop();
                mediaPlayer[ringId] = null;
            }
        } catch (Exception e) {
            Logger.e(TAG, " Exception stopSound Error =" + e.toString());
        }
    }
}