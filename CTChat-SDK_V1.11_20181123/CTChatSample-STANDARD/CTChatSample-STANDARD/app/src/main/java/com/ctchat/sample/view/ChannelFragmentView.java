package com.ctchat.sample.view;


import com.ctchat.sdk.ptt.tool.entity.ChannelEntity;
import com.ctchat.sdk.ptt.tool.entity.ContactEntity;

import java.util.List;

/**
 * ChannelFragmentView
 */
public interface ChannelFragmentView {
    void showLoadProgress();

    void hideLoadProgress();

    void loadDataSuccess(List<ChannelEntity> list);

    void getChannelMembersSuccess();
    void onNotifyChannelPersonalCreate(ChannelEntity channel);

    void onNotifyChannelPersonalDelete(ChannelEntity channel);

    void onNotifyChannelMemberAppend(ChannelEntity channel, List<ContactEntity>contacts);

    void onNotifyChannelMemberDelete(ChannelEntity channel, List<ContactEntity>contacts);

    void onNotifyChannelMemberUpdate(ChannelEntity channel, List<ContactEntity>contacts);

    void onNotifyNetChanged();
}
