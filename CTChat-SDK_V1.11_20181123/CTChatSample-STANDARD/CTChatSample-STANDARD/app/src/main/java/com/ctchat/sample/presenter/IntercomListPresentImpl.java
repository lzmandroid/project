package com.ctchat.sample.presenter;


import com.ctchat.sdk.ptt.tool.channel.OnChannelDataListener;
import com.ctchat.sdk.ptt.tool.contact.ContactPresenceListener;
import com.ctchat.sdk.ptt.tool.entity.ChannelEntity;
import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sample.model.IntercomListModel;
import com.ctchat.sample.model.IntercomListModelImpl;
import com.ctchat.sample.view.IntercomListView;

import java.util.HashMap;
import java.util.List;

public class IntercomListPresentImpl implements IntercomListPresent, OnChannelDataListener,ContactPresenceListener {

    private IntercomListView view;
    private IntercomListModel model;

    public IntercomListPresentImpl(IntercomListView view) {
        this.view = view;
        model = new IntercomListModelImpl(this);
    }

    @Override
    public void getChannelMember(ChannelEntity channel) {
        model.getChannelMember(channel);
    }

    @Override
    public void onPause() {
        model.onPause();
    }

    @Override
    public void onResume() {
        model.onResume();
    }

    @Override
    public void onGetChannelList(List<ChannelEntity> list) {

    }

    @Override
    public void onGetChannelMemberList(List<ContactEntity> list, String channelId) {
        view.onGetChannelMemberList(list);
    }

    @Override
    public void onNotifyChannelCreate(ChannelEntity channel) {

    }

    @Override
    public void onNotifyChannelDelete(ChannelEntity channel) {
        view.onNotifyChannelPersonalDelete(channel);
    }

    @Override
    public void onNotifyChannelMemberAdd(ChannelEntity channel, List<ContactEntity> contacts) {
        view.onNotifyChannelMemberAdd(channel,contacts);
    }

    @Override
    public void onNotifyChannelMemberDelete(ChannelEntity channel, List<ContactEntity> contacts) {
        view.onNotifyChannelMemberDelete(channel,contacts);
    }

    @Override
    public void onNotifyChannelMemberUpdate(ChannelEntity channel, List<ContactEntity> contacts) {
        view.onNotifyChannelMemberUpdate(channel,contacts);
    }

    @Override
    public void onContactPresence(boolean isSubscribed, HashMap<String, Integer> hashMap) {
        this.view.onContactListPresence();
    }

    @Override
    public void onContactPresence(boolean isSubscribed, String mdn, int state) {
        this.view.onContactPresence();
    }
}
