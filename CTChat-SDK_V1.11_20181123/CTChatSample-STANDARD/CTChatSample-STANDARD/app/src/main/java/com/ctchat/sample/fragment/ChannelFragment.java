package com.ctchat.sample.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ctchat.sample.tool.MyActivityManager;
import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sample.R;
import com.ctchat.sdk.basemodule.util.ToastUtil;
import com.ctchat.sdk.ptt.tool.MessagePush.PushManager;
import com.ctchat.sdk.ptt.tool.channel.ChannelManager;
import com.ctchat.sdk.ptt.tool.entity.ChannelEntity;
import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;
import com.ctchat.sdk.ptt.tool.intercom.IntercomManager;
import com.ctchat.sample.IntercomActivity;
import com.ctchat.sample.MainActivity;
import com.ctchat.sample.adapter.ChannelAdapter;
import com.ctchat.sample.presenter.ChannelPresent;
import com.ctchat.sample.presenter.ChannelPresentImpl;
import com.ctchat.sample.tool.incoming.SessionInitiationMan;
import com.ctchat.sample.util.NetUtil;
import com.ctchat.sample.view.ChannelFragmentView;
import com.ctchat.sample.widget.ListViewForScroll;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 */
public class ChannelFragment extends BaseFragment implements ChannelFragmentView, ChannelAdapter.ChannelStateListener {
    private static final String TAG = "ChannelFragment";
    private View view;
    private MainActivity mContext;
    private ListViewForScroll groupLv;
    private ChannelPresent channelPresent;
    private ChannelAdapter channelAdapter;
    private List<ChannelEntity> channelEntityList = new CopyOnWriteArrayList<>();
    private RelativeLayout netNoticeLayout;
    private TextView tvchannelOnlineCount;
    private TextView tvChannelAllCount;
    private ScrollView svChannel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG,"ChannelFragment onCreate");
        initData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.d(TAG, "ChannelFragment onCreateView");
        view = inflater.inflate(R.layout.fragment_groups, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        Logger.d(TAG, "ChannelFragment onPause");
        channelPresent.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.d(TAG, "ChannelFragment onResume");
        channelPresent.onResume();
        refreshChannelEntityList();
        getOnlineChannelCount();
        onNotifyNetChanged();
        channelAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        Logger.i(TAG,"ChannelFragment onDestroy");
        super.onDestroy();
        channelPresent.onDestroy();
    }

    @Override
    public void initData() {
        mContext = (MainActivity) getActivity();
        channelPresent = new ChannelPresentImpl(this);
        channelPresent.loadData();

    }

    @Override
    public void initView(View view) {
        svChannel = (ScrollView) view.findViewById(R.id.sv_channel);
        svChannel.smoothScrollTo(0, 0);
        tvChannelAllCount = (TextView) view.findViewById(R.id.tv_all_channel_count);
        tvchannelOnlineCount = (TextView) view.findViewById(R.id.tv_channel_online_count);
        netNoticeLayout = (RelativeLayout) view.findViewById(R.id.net_disconnected_prompt);
        groupLv = (ListViewForScroll) view.findViewById(R.id.lv_group);
        groupLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChannelEntity channelEntity = channelEntityList.get(position);
                SessionEntity sessionEntity = IntercomManager.INSTANCE.getSessionEntityByChannel(channelEntity);
                Intent intent = new Intent(mContext, IntercomActivity.class);
                SessionInitiationMan.SessionInitiation sessionInitiation = new SessionInitiationMan.SessionInitiation();
                sessionInitiation.sessionCode = sessionEntity.getSessionId();
                sessionInitiation.initializationMode = SessionInitiationMan.INITIATION_PTT;
                sessionInitiation.connectionStatus = SessionInitiationMan.SESSION_CONNECTION;
                intent.putExtra(SessionInitiationMan.INTENT_DATA_KEY, sessionInitiation);

                startActivity(intent);
            }
        });

        channelAdapter = new ChannelAdapter(mContext, channelEntityList, groupLv);
        channelAdapter.setInterface(this);
        groupLv.setAdapter(channelAdapter);

    }

    @Override
    public void showLoadProgress() {

    }

    @Override
    public void hideLoadProgress() {

    }

    public void refreshChannelEntityList(){
        channelEntityList.clear();
        channelEntityList.addAll(ChannelManager.INSTANCE.getLoadedChannels());
        tvChannelAllCount.setText("" + channelEntityList.size());
    }

    private void getOnlineChannelCount() {
        int onlineCount = 0;
        for (int i = 0; i< channelEntityList.size(); i++) {
            ChannelEntity channel = channelEntityList.get(i);
            SessionEntity session = channel.getSessionEntity();
            if (session.getSessionStatus() == SessionEntity.SESSION_STATE_DIALOG) {
                channelPresent.getChannelMembers(channel);
                onlineCount ++;
            }
        }
          tvchannelOnlineCount.setText(onlineCount + "");
    }

    @Override
    public void loadDataSuccess(List<ChannelEntity> list) {
        Logger.d(TAG, "ChannelFragment loadDataSuccess size = " + list.size());
        tvChannelAllCount.setText(list.size() + "");
        refreshChannelEntityList();
        channelAdapter.notifyDataSetChanged();

        mContext.refreshHistoryFragment();
    }

    @Override
    public void getChannelMembersSuccess() {
        Logger.i(TAG, "getChannelMembersSuccess ");
        refreshChannelEntityList();
        channelAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNotifyChannelPersonalCreate(ChannelEntity channel) {
        Logger.i(TAG, "[channel] channel create");
        refreshChannelEntityList();
        channelAdapter.notifyDataSetChanged();

        mContext.refreshHistoryFragment();
    }

    @Override
    public void onNotifyChannelPersonalDelete(ChannelEntity channel) {
        Logger.i(TAG,"[channel] channel delete");
        refreshChannelEntityList();
        channelAdapter.notifyDataSetChanged();

        mContext.refreshHistoryFragment();
    }

    @Override
    public void onNotifyChannelMemberAppend(ChannelEntity channel, List<ContactEntity> contacts) {
        Logger.i(TAG, "[channel] channel memeber Append");
        channelAdapter.notifyDataSetChanged();

        mContext.refreshHistoryFragment();
    }

    @Override
    public void onNotifyChannelMemberDelete(ChannelEntity channel, List<ContactEntity> contacts) {
        Logger.i(TAG,"[channel] channel memeber delete");
        channelAdapter.notifyDataSetChanged();

        mContext.refreshHistoryFragment();
    }

    @Override
    public void onNotifyChannelMemberUpdate(ChannelEntity channel, List<ContactEntity> contacts) {
        Logger.i(TAG, "[channel] channel member update");
        channelAdapter.notifyDataSetChanged();

        mContext.refreshHistoryFragment();
    }

    @Override
    public void onNotifyNetChanged() {
        Logger.i(TAG, "onNotifyNetChanged");
        if (!NetUtil.isNetConnected(mContext)) {
            netNoticeLayout.setVisibility(View.VISIBLE);
        } else {
            netNoticeLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 更新消息未读数显示
     */
    public void undateUnreadCount() {
        Logger.d(TAG, "更新数量+1");
    }

    @Override
    public void channelStateChanged(SessionEntity session) {
        Logger.d(TAG, "channelStateChanged");
        Logger.d(TAG, "session status = "+session.getSessionStatus()+"sessionCode = "+session.getSessionId());
        if (session.getSessionStatus() == SessionEntity.SESSION_STATE_DIALOG) {
            IntercomManager.INSTANCE.stopSessionCall(session);
        } else {
            if (session.getChannel() != null) {
                IntercomManager.INSTANCE.startSessionCall(session.getChannel());
            }
        }
    }

    public void refreshView() {
        if (channelAdapter != null) {
            getOnlineChannelCount();
            channelAdapter.notifyDataSetChanged();
        }
    }

    public void refreshGroupState() {
        channelAdapter.notifyDataSetChanged();
    }

}
