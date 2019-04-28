package com.ctchat.sample.view;

import com.ctchat.sdk.ptt.tool.entity.ChannelEntity;
import com.ctchat.sdk.ptt.tool.entity.ContactEntity;

import java.util.List;

public interface IntercomListView {

    void onGetChannelMemberList(List<ContactEntity> list);
    void onContactPresence();
    void onContactListPresence();

    /**
     * 预定义组成员添加回调事件
     * @param channel
     * @param contacts
     */
    void onNotifyChannelMemberAdd(ChannelEntity channel, List<ContactEntity> contacts);

    /**
     * 预定义组成员删除回调事件
     * @param channel
     * @param contacts
     */
    void onNotifyChannelMemberDelete(ChannelEntity channel, List<ContactEntity> contacts);

    /**
     * 预定义组成员更新回调事件
     * @param channel
     * @param contacts
     */
    void onNotifyChannelMemberUpdate(ChannelEntity channel, List<ContactEntity> contacts);

    void onNotifyChannelPersonalDelete(ChannelEntity channel);
}
