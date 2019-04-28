package com.ctchat.sample.model;

import com.ctchat.sdk.ptt.tool.channel.ChannelManager;
import com.ctchat.sdk.ptt.tool.channel.OnChannelDataListener;
import com.ctchat.sdk.ptt.tool.contact.ContactManager;
import com.ctchat.sdk.ptt.tool.contact.ContactPresenceListener;
import com.ctchat.sdk.ptt.tool.entity.ChannelEntity;
import com.ctchat.sample.application.WeApplication;
import com.ctchat.sample.presenter.IntercomListPresentImpl;

public class IntercomListModelImpl implements IntercomListModel {

    private OnChannelDataListener onChannelDataListener;
    private ContactPresenceListener contactPresenceListener;

    public IntercomListModelImpl(IntercomListPresentImpl listPresent) {
        this.onChannelDataListener = (OnChannelDataListener)listPresent;
        this.contactPresenceListener = (ContactPresenceListener)listPresent;

        ChannelManager.INSTANCE.registerLoadChannelDataListener(this.onChannelDataListener);
        ContactManager.INSTANCE.registerContactPresenceListener(this.contactPresenceListener);
    }

    @Override
    public void getChannelMember(ChannelEntity channel) {
        ChannelManager.INSTANCE.getChannelMembers(channel.getId());
    }

    @Override
    public void onPause() {
        ContactManager.INSTANCE.unSubscribeContactPresence();
        ChannelManager.INSTANCE.unregisterLoadChannelDataListener();
        ContactManager.INSTANCE.unregisterContactPresenceListener();
    }

    @Override
    public void onResume() {
        ContactManager.INSTANCE.subscribeContactPresence(WeApplication.getInstance());
        ChannelManager.INSTANCE.registerLoadChannelDataListener(this.onChannelDataListener);
        ContactManager.INSTANCE.registerContactPresenceListener(this.contactPresenceListener);
    }
}
