package com.ctchat.sample.presenter;



import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;

import java.util.ArrayList;

public interface MediaListener {
    void onTalkPreparing(SessionEntity session);

    void onTalk(SessionEntity session);

    void onTalkEnd(SessionEntity session, int i);

    void onListen(SessionEntity session, ContactEntity airContact);

    void onListenEnd(SessionEntity session);

    void onListenVoice(SessionEntity session);

    void onMediaQueue(SessionEntity session, ArrayList<ContactEntity> arrayList);

    void onMediaQueueIn(SessionEntity session);

    void onMediaQueueOut(SessionEntity session);

    void updateAudioWave(byte[] bytes);
}
