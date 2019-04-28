package com.ctchat.sample.presenter;


import com.ctchat.sdk.ptt.tool.entity.ChannelEntity;
import com.ctchat.sdk.ptt.tool.entity.ContactEntity;

import java.util.List;

public interface ChannelListener {
    /**
     * 刷新预定义组成功
     */
    void refreshDataSuccess();


    void getChannelMembersSuccess();
    /**
     * 加载预定义组成功
     */
    void loadDataSuccess(List<ChannelEntity> list);

    /**
     * 加载预定义组失败
     */
    void loadDataFail();

    /**
     * 网络错误
     */
    void netError();

    void onNotifyChannelPersonalCreate(ChannelEntity channel);

    void onNotifyChannelPersonalDelete(ChannelEntity channel);

    void onNotifyChannelMemberAppend(ChannelEntity channel, List<ContactEntity>contacts);

    void onNotifyChannelMemberDelete(ChannelEntity channel, List<ContactEntity>contacts);

    void onNotifyChannelMemberUpdate(ChannelEntity channel, List<ContactEntity>contacts);
}
