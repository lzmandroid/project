package com.ctchat.sample.model;

import android.text.TextUtils;

import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sdk.ptt.tool.channel.OnChannelDataListener;
import com.ctchat.sdk.ptt.tool.channel.ChannelManager;
import com.ctchat.sdk.ptt.tool.entity.ChannelEntity;
import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sdk.ptt.tool.intercom.IntercomManager;
import com.ctchat.sample.io.Setting;
import com.ctchat.sample.application.AppInitializeManager;
import com.ctchat.sample.presenter.ChannelListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ChannelModelImpl implements ChannelModel {
    private static final String TAG = "ChannelModelImpl";
    private ChannelListener channelListener;
    private OnChannelDataListener onChannelDataListener;
    boolean isAttachedChannelLoaded = false;

    public ChannelModelImpl( final ChannelListener channelListener) {
        this.channelListener = channelListener;
        onChannelDataListener = new OnChannelDataListener() {
            @Override
            public void onGetChannelList(List<ChannelEntity> list) {
                Logger.i(TAG,"getChannelList");
                channelListener.loadDataSuccess(list);
                //启动附着预定义组
                channelAttachLoad(true);
            }

            @Override
            public void onGetChannelMemberList(List<ContactEntity> list, String channelId) {

            }

            @Override
            public void onNotifyChannelCreate(ChannelEntity channel) {
                channelListener.onNotifyChannelPersonalCreate(channel);
            }

            @Override
            public void onNotifyChannelDelete(ChannelEntity channel) {
                channelListener.onNotifyChannelPersonalDelete(channel);
            }

            @Override
            public void onNotifyChannelMemberAdd(ChannelEntity channel, List<ContactEntity> contacts) {
                channelListener.onNotifyChannelMemberAppend(channel, contacts);
            }

            @Override
            public void onNotifyChannelMemberDelete(ChannelEntity channel, List<ContactEntity> contacts) {
                channelListener.onNotifyChannelMemberDelete(channel, contacts);
            }

            @Override
            public void onNotifyChannelMemberUpdate(ChannelEntity channel, List<ContactEntity> contacts) {
                channelListener.onNotifyChannelMemberUpdate(channel, contacts);
            }
        };
        ChannelManager.INSTANCE.registerLoadChannelDataListener(onChannelDataListener);
    }

    @Override
    public void loadChannelListData() {
        Logger.i(TAG,"ChannelModelImpl loadChannelListData");
        ChannelManager.INSTANCE.getChannelList();
    }

    @Override
    public void getChannelMembers(ChannelEntity channelEntity) {
        Logger.i(TAG,"getChannelMembers");
        if(channelEntity !=null){
            ChannelManager.INSTANCE.getChannelMembers(channelEntity.getId());
        }
    }

    @Override
    public void refreshChannelListData() {
        Logger.i(TAG,"ChannelModelImpl refreshChannelListData");
    }

    @Override
    public void onPause() {
        ChannelManager.INSTANCE.unregisterLoadChannelDataListener();
    }

    @Override
    public void onResume() {
        ChannelManager.INSTANCE.registerLoadChannelDataListener(onChannelDataListener);
    }

    /**
     * 加载附着预定义组
     *
     * @param isCall 设置是否直接建立会话
     */
    public void channelAttachLoad(boolean isCall) {
        //只在第一次登陆时候attach一次
        if (isAttachedChannelLoaded) {
            Logger.i(TAG, "attach channel has loaded");
            return;
        }
        isAttachedChannelLoaded = true;

        String channelJString = Setting.getAttachedChannel(AppInitializeManager.getmApplicationContext());
        if (!TextUtils.isEmpty(channelJString)) {
            try {
                JSONArray attachedChannels = new JSONObject(channelJString).optJSONArray(Setting.ATTACH_CHANNEL_KEY);

                if (attachedChannels != null && attachedChannels.length() > 0) {
                    for (int i = 0; i < attachedChannels.length(); i++) {
                        ChannelEntity attachChannel = ChannelManager.INSTANCE.getChannelByID(attachedChannels.getString(i));
                        if (attachChannel != null) {
                            attachChannel.setAttachItem(true);
                            if (isCall) {
                                IntercomManager.INSTANCE.startSessionCall(attachChannel);
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                Logger.e(TAG, "Channel Json parse Err:" + e.getMessage());
            }
        }
    }
}
