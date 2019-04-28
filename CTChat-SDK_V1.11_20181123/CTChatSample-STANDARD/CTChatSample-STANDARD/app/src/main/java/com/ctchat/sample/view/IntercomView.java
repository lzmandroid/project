package com.ctchat.sample.view;

import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;

import java.util.ArrayList;
import java.util.List;

public interface IntercomView {
    /**
     * 因sessionEntity相关属性而设置的widget状态
     *
     * @param sessionEntity
     */
    void refreshViewBySessionEntity(SessionEntity sessionEntity);

    /**
     * 准备状态
     *
     * @param session
     */
    void showTalkPreparingStatus(SessionEntity session);

    /**
     * 说话
     *
     * @param session
     */
    void showTalkStatus(SessionEntity session);

    /**
     * 说话结束
     *
     * @param session
     * @param i
     */
    void showTalkEndStatus(SessionEntity session, int i);

    /**
     * 听会话
     *
     * @param contactEntity
     */
    void showListenStatus(SessionEntity session, ContactEntity contactEntity);

    /**
     * 听会话结束
     *
     * @param session
     */
    void showListenEndStatus(SessionEntity session);

    /**
     * 会话排队等候
     *
     * @param session
     * @param arrayList
     */
    void showMediaQueue(SessionEntity session, ArrayList<ContactEntity> arrayList);

    /**
     * 加入会话等待
     *
     * @param session
     */
    void mediaQueueIn(SessionEntity session);

    /**
     * 离开会话等待
     *
     * @param session
     */
    void mediaQueueOut(SessionEntity session);

    /**
     * 临时会话主叫呼叫对方：
     *
     * @param session
     */
    void sessionOutgoingRingingStatus(SessionEntity session);

    /**
     * 临时或预定义组会话呼出开始建立事件：显示会话建立中
     *
     * @param session
     */
    void sessionEstablishingStatus(SessionEntity session);

    /**
     * 临时或预定义组会话建立：显示建立成功
     *
     * @param session
     * @param ret
     */
    void sessionEstablishedStatus(SessionEntity session, int ret);

    /**
     * 临时或预定义组会话结束：显示会话结束
     *
     * @param session
     * @param reason
     */
    void sessionReleasedStatus(SessionEntity session, int reason);

    /**
     * 会话过程中,动态变化的用户参与此会话情况
     *
     * @param session
     * @param memebersALl
     * @param membersPresence
     */
    void sessionPresenceStatus(SessionEntity session, List<ContactEntity> memebersALl, List<ContactEntity> membersPresence);

    /**
     * @param session
     * @param list
     * @param b
     */
    void sessionMemberUpdateStatus(SessionEntity session, List<ContactEntity> list, boolean b);


    void sessionCenterCallRet(boolean ret, SessionEntity session);
    /**
     * 显示 声音波动 动画
     */
    void updateAudioWave(byte[] bytes);

    void onJoinCallTimeOut();
}
