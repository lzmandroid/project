package com.ctchat.sample.presenter;


import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sdk.ptt.tool.entity.ChannelEntity;
import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sample.model.ChannelModel;
import com.ctchat.sample.model.ChannelModelImpl;
import com.ctchat.sample.view.ChannelFragmentView;

import java.util.List;

public class ChannelPresentImpl implements ChannelPresent, ChannelListener {
    private static final String TAG = "ChannelPresentImpl";
    private ChannelFragmentView channelFragmentView;
    private ChannelModel channelModel;

    public ChannelPresentImpl(ChannelFragmentView channelFragmentView) {
        this.channelFragmentView = channelFragmentView;
        channelModel = new ChannelModelImpl(this);
    }


    @Override
    public void loadData() {
        Logger.i(TAG,"ChannelPresentImpl loadData");
        if (null == channelModel) {
            channelModel = new ChannelModelImpl(this);
        }

        channelModel.loadChannelListData();
    }

    @Override
    public void getChannelMembers(ChannelEntity channelEntity) {
        Logger.i(TAG,"refreshChannel");
        channelModel.getChannelMembers(channelEntity);

    }

    @Override
    public void onDestroy() {
        Logger.i(TAG,"ChannelPresentImpl onDestroy");
        channelFragmentView = null;
        channelModel = null;
    }

    @Override
    public void onResume() {
        channelModel.onResume();
    }

    @Override
    public void onPause() {
        channelModel.onPause();
    }

    @Override
    public void refreshDataSuccess() {
        Logger.i(TAG,"ChannelPresentImpl refreshDataSuccess");
    }

    @Override
    public void getChannelMembersSuccess() {
        Logger.i(TAG,"refreshChannelsSuccess");
        if(channelFragmentView !=null){
            channelFragmentView.getChannelMembersSuccess();
        }
    }

    @Override
    public void loadDataSuccess(List<ChannelEntity> list) {
        Logger.i(TAG,"ChannelPresentImpl loadDataSuccess:"+list.size());
        if (channelFragmentView != null) {
            channelFragmentView.loadDataSuccess(list);
        }
    }

    @Override
    public void loadDataFail() {
        Logger.i(TAG,"ChannelPresentImpl loadDataFail");
    }

    @Override
    public void netError() {
        if (channelFragmentView != null) {
            channelFragmentView.onNotifyNetChanged();
        }
    }

    @Override
    public void onNotifyChannelPersonalCreate(ChannelEntity channel) {
        if (channelFragmentView != null) {
            channelFragmentView.onNotifyChannelPersonalCreate(channel);
        }
    }

    @Override
    public void onNotifyChannelPersonalDelete(ChannelEntity channel) {
        if (channelFragmentView != null) {
            channelFragmentView.onNotifyChannelPersonalDelete(channel);
        }
    }

    @Override
    public void onNotifyChannelMemberAppend(ChannelEntity channel, List<ContactEntity> contacts) {
        if (channelFragmentView != null) {
            channelFragmentView.onNotifyChannelMemberAppend(channel,contacts);
        }
    }

    @Override
    public void onNotifyChannelMemberDelete(ChannelEntity channel, List<ContactEntity> contacts) {
        if (channelFragmentView != null) {
            channelFragmentView.onNotifyChannelMemberDelete(channel,contacts);
        }
    }

    @Override
    public void onNotifyChannelMemberUpdate(ChannelEntity channel, List<ContactEntity> contacts) {
        if (channelFragmentView != null) {
            channelFragmentView.onNotifyChannelMemberUpdate(channel,contacts);
        }
    }


}
