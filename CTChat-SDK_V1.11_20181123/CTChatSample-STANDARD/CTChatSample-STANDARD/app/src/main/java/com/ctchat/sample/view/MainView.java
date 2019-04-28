package com.ctchat.sample.view;


import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;

import java.util.List;

public interface MainView {
    void onSessionEstablished(SessionEntity session, int ret);

    void onSessionReleased(SessionEntity session, int reason);

    void onSessionPresence(SessionEntity sessionEntity, List<ContactEntity> membersAll, List<ContactEntity> membersPresence);

    void onSessionMemberUpdate(SessionEntity session, List<ContactEntity> list, boolean b);

    /**
     * 更新信息未读数
     */
    void undateUnreadCount();

    void refreshTalkStatus(SessionEntity session);

    /**
     * 推送系统广播消息
     */
    void onPushBroadcastMsg();
}
