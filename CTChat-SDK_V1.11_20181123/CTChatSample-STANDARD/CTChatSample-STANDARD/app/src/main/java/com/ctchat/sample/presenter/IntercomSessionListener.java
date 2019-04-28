package com.ctchat.sample.presenter;



import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;

import java.util.List;

public interface IntercomSessionListener {
    /**
     * 临时会话主叫呼叫对方,当对方来电振铃时,此时主叫方同时会触发主叫 振铃,已表明呼叫正在等待接听
     * @param session 会话实例
     */
    void onSessionOutgoingRinging(SessionEntity session) ;

    /**
     * 临时或预定义组会话呼出开始建立事件:用户调用 SessionCall 后返回此事件
     * @param session 会话实例
     */
    void onSessionEstablishing(SessionEntity session) ;

    /**
     * 临时或预定义组会话建立事件:来电或去电呼叫、或预定义组会话建立成功后返回此事件
     * @param session 会话实例
     * @param ret
     */
    void onSessionEstablished(SessionEntity session, int ret) ;

    /**
     * 临时或预定义组会话结束事件:来电或去电呼叫、或预定义组会话结束后返回此事件
     * @param session 会话实例
     * @param reason
     */
    void onSessionReleased(SessionEntity session, int reason) ;

    /**
     * 会话过程中,动态变化的用户参与此会话情况,均会通过此事件通知
     * @param session 会话实例
     * @param membersAll 会话预期所有成员
     * @param membersPresence 会话实际参与成员
     */
    void onSessionPresence(SessionEntity session, List<ContactEntity> membersAll, List<ContactEntity> membersPresence) ;

    /**
     * @param session
     * @param list
     * @param b
     */
    void onSessionMemberUpdate(SessionEntity session, List<ContactEntity> list, boolean b);


    void onSessionCenterCallRet(boolean ret, SessionEntity sessionEntity);


    void onJoinCallTimeOut();
}
