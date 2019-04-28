package com.ctchat.sample.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import com.ctchat.sdk.basemodule.api.adapter.XpBaseAdapter;
import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sample.R;
import com.ctchat.sdk.ptt.tool.entity.ChannelEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;
import com.ctchat.sdk.ptt.tool.intercom.IntercomManager;
import com.ctchat.sample.widget.MemberCountView;

import java.util.List;

public class ChannelAdapter extends XpBaseAdapter<ChannelEntity> {

    private static final String TAG = "ChannelAdapter";
    public ChannelStateListener listener;

    public ChannelAdapter(Context context, List data, ListView listView) {
        super(context, data, listView);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChannelEntity channel = getData().get(position);
        final SessionEntity session = channel.getSessionEntity();
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.predefine_group_item, null);
            viewHolder.tvGroupName = (TextView) convertView.findViewById(R.id.tv_predefine_group_name);
            viewHolder.ivchannelState = (TextView) convertView.findViewById(R.id.iv_channel_state);
            viewHolder.llCallState = (LinearLayout) convertView.findViewById(R.id.ll_call_status);
            viewHolder.tvState = (TextView) convertView.findViewById(R.id.tv_state);
            viewHolder.memberCountView = (MemberCountView) convertView.findViewById(R.id.member_count_view);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvGroupName.setText(channel.getName());
        SessionEntity.SessionStateCount sessionStateCount = IntercomManager.INSTANCE.getSessionStateCount(session);
        Logger.d(TAG, "Chanel State Count :" + sessionStateCount.getChatOnlineCount() + "  " + sessionStateCount.getChatOfflineCount() + " " + sessionStateCount.getAllmemberCount());
        Logger.i(TAG, "channel status:" + session.getSessionStatus());

        if (session.getSessionStatus() == SessionEntity.SESSION_STATE_DIALOG) {
            viewHolder.memberCountView.setVisibility(View.VISIBLE);
            viewHolder.memberCountView.getTvLeftMemberCount().setText(sessionStateCount.getChatOnlineCount() + "");
            viewHolder.memberCountView.getTvMiddleMemberCount().setText(sessionStateCount.getChatOfflineCount() + "");
            viewHolder.memberCountView.getTvRightMemberCount().setText(sessionStateCount.getAllmemberCount() + "");
            if (session.isLocked()) {
                viewHolder.ivchannelState.setText("正常");
            } else {
                if (IntercomManager.INSTANCE.hasSessionLocked()) {
                    viewHolder.ivchannelState.setText("屏蔽");
                } else {
                    viewHolder.ivchannelState.setText("正常");
                }
            }
        } else {
            viewHolder.memberCountView.setVisibility(View.GONE);
            viewHolder.ivchannelState.setText("屏蔽");
        }
        viewHolder.ivchannelState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.channelStateChanged(session);
            }
        });

        viewHolder.llCallState.setVisibility(View.GONE);

        // 显示会话状态
        switch (session.getSessionStatus()) {
            case SessionEntity.SESSION_STATE_IDLE:
                viewHolder.llCallState.setVisibility(View.GONE);
                break;
            case SessionEntity.SESSION_STATE_CALLING:
                viewHolder.llCallState.setVisibility(View.VISIBLE);
                viewHolder.tvState.setText(R.string.setting_up_the_session);
                break;
            case SessionEntity.SESSION_STATE_DIALOG:
                viewHolder.llCallState.setVisibility(View.VISIBLE);
                switch (session.getMediaState()) {
                    case SessionEntity.MEDIA_STATE_LISTEN:
                        viewHolder.tvState.setText(session.getSpeaker().getDisplayName() + getContext().getString(R.string.intercom_state_talking));
                        break;
                    case SessionEntity.MEDIA_STATE_TALK:
                        viewHolder.tvState.setText(R.string.talking_by_myself);
                        break;
                    case SessionEntity.MEDIA_STATE_IDLE://闲置
                        viewHolder.tvState.setText(R.string.state_free);
                        break;
                }
                break;
        }

        return convertView;
    }

    class ViewHolder {
        TextView ivchannelState, tvGroupName, tvState;
        LinearLayout llCallState;
        MemberCountView memberCountView;
    }

    public interface ChannelStateListener {
        void channelStateChanged(SessionEntity session);
    }

    public void setInterface(ChannelStateListener listener) {
        this.listener = listener;
    }
}
