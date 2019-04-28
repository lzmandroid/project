package com.ctchat.sample.model;

import android.util.Log;

import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;
import com.ctchat.sdk.ptt.tool.intercom.IntercomManager;
import com.ctchat.sdk.ptt.tool.intercom.OnAudioVisualizerListener;
import com.ctchat.sdk.ptt.tool.intercom.OnMediaEventListener;
import com.ctchat.sdk.ptt.tool.intercom.OnSessionEventListener;
import com.ctchat.sample.presenter.IntercomSessionListener;
import com.ctchat.sample.presenter.MediaListener;
import com.ctchat.sample.tool.JoinCallTimerHelper;

import java.util.ArrayList;
import java.util.List;

public class IntercomModelImpl implements IntercomModel ,JoinCallTimerHelper.JoinCallTimerListener{
    private static final String TAG = "IntercomModelImpl";
    private IntercomSessionListener intercomSessionListener;
    private MediaListener mediaListener;
    private OnSessionEventListener sessionEventListener;
    private OnMediaEventListener mediaEventListener;
    private OnAudioVisualizerListener audioVisualizerListener;
    private SessionEntity currentSession;
    private String currentCaller;
    public IntercomModelImpl(IntercomSessionListener sessionListener) {
        //sessionListener
        intercomSessionListener = sessionListener;
        sessionEventListener = new OnSessionEventListener() {
            @Override
            public void onSessionOutgoingRinging(SessionEntity session) {
                Logger.d(TAG,"onSessionOutgoingRinging");
                intercomSessionListener.onSessionOutgoingRinging(session);
            }

            @Override
            public void onSessionEstablishing(SessionEntity session) {
                Logger.d(TAG,"onSessionEstablishing");
                intercomSessionListener.onSessionEstablishing(session);
            }

            @Override
            public void onSessionEstablished(SessionEntity session, int result) {
                Logger.d(TAG,"onSessionEstablished");
                intercomSessionListener.onSessionEstablished(session, result);
            }

            @Override
            public void onSessionReleased(SessionEntity session, int reason) {
                Logger.d(TAG,"onSessionReleased");
                intercomSessionListener.onSessionReleased(session, reason);
            }

            @Override
            public void onSessionPresence(SessionEntity session, List<ContactEntity> memebersALl, List<ContactEntity> membersPresence) {
                Logger.d(TAG,"onSessionPresence");
                intercomSessionListener.onSessionPresence(session, memebersALl, membersPresence);
            }

            @Override
            public void onSessionMemberUpdate(SessionEntity session, List<ContactEntity> list, boolean b) {
                intercomSessionListener.onSessionMemberUpdate(session, list, b);
            }

            @Override
            public void onCenterCallRet(boolean ret, SessionEntity session) {
                intercomSessionListener.onSessionCenterCallRet(ret,session);
            }
        };

        mediaEventListener = new OnMediaEventListener() {
            @Override
            public void onMediaStateTalkPreparing(SessionEntity session) {
                if (null != mediaListener)
                    mediaListener.onTalkPreparing(session);
            }

            @Override
            public void onMediaStateTalk(SessionEntity session) {
                if (null != mediaListener)
                    mediaListener.onTalk(session);
            }

            @Override
            public void onMediaStateTalkEnd(SessionEntity session, int i) {
                if (null != mediaListener)
                    mediaListener.onTalkEnd(session, i);
            }

            @Override
            public void onMediaStateListen(SessionEntity session, ContactEntity airContact) {
                if (null != mediaListener)
                    mediaListener.onListen(session, airContact);
            }

            @Override
            public void onMediaStateListenEnd(SessionEntity session) {
                if (null != mediaListener)
                    mediaListener.onListenEnd(session);
            }

            @Override
            public void onMediaStateListenVoice(SessionEntity sessionEntity) {

            }


        };

        audioVisualizerListener = new OnAudioVisualizerListener() {
            @Override
            public void onAudioVisualizerChanged(byte[] bytes, int num) {
                if (null != mediaListener)
                    mediaListener.updateAudioWave(bytes);
            }
        };
    }

    @Override
    public void registerMediaListener(MediaListener listener) {
        mediaListener = listener;
        IntercomManager.INSTANCE.registerMediaEventListener(mediaEventListener);
        IntercomManager.INSTANCE.registerAudioVisualizerListener(audioVisualizerListener);
    }

    @Override
    public void unregisterMediaListener() {
        IntercomManager.INSTANCE.unRegisterMediaEventListener();
        IntercomManager.INSTANCE.unRegisterAudioVisualizerListener();
    }

    @Override
    public void startJoinCallTimer(String caller,SessionEntity newSessionEntity,SessionEntity oldSessiontEntity,long delayTime) {
        if(JoinCallTimerHelper.getHelper().isTiming()){//重置定时器
            JoinCallTimerHelper.getHelper().releaseTimer();
        }
        currentCaller =caller;
        currentSession = newSessionEntity;
        JoinCallTimerHelper.getHelper().setListener(this);
        JoinCallTimerHelper.getHelper().startTimer(delayTime);
    }

    @Override
    public void releaseJoinCallTimer() {
        JoinCallTimerHelper.getHelper().releaseTimer();
    }

    @Override
    public void onPause() {
        IntercomManager.INSTANCE.unRegisterSessionEventListener();
    }

    @Override
    public void onResume() {
        IntercomManager.INSTANCE.registerSessionEventListener(sessionEventListener);
    }

    @Override
    public void onDestroy() {
        JoinCallTimerHelper.getHelper().releaseTimer();
    }


    @Override
    public void onJoinCallTimeOut() {
        Log.d(TAG,"onJoinCallTimeOut");
        intercomSessionListener.onJoinCallTimeOut();
        JoinCallTimerHelper.getHelper().releaseTimer();
    }

}
