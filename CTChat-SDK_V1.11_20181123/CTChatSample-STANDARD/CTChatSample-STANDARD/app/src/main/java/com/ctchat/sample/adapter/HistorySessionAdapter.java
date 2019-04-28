package com.ctchat.sample.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ctchat.sdk.basemodule.api.adapter.XpBaseAdapter;
import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sdk.basemodule.util.ToastUtil;
import com.ctchat.sample.R;
import com.ctchat.sdk.ptt.tool.chatIM.MessageType;
import com.ctchat.sdk.ptt.tool.entity.MessageEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;
import com.ctchat.sdk.ptt.tool.intercom.IntercomManager;
import com.ctchat.sdk.ptt.util.DateTool;
import com.ctchat.sample.widget.BadgeView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HistorySessionAdapter extends XpBaseAdapter<SessionEntity> {

    public static final String TAG = "HistorySessionAdapter";
    private int unreadCount = 0;
    private Set<String> selectOptions = new HashSet<>();
    private Set<String> animSet = new HashSet<>();

    public Set<String> getSelectOptions() {
        return selectOptions;
    }

    public void clearSelectOptions() {
        this.selectOptions.clear();
    }

    public void clearAnimSet() {
        if (this.animSet != null) {
            animSet.clear();
        }
    }

    public HistorySessionAdapter(Context context, List data, ListView listView) {
        super(context, data, listView);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final SessionEntity session = getData().get(position);
        unreadCount = session.getMessageUnreadCount();
        Logger.d(TAG, "unreadCount = " + unreadCount);
        MessageEntity message = session.getLastMessage();
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = getLayoutInflater().inflate(R.layout.item_channelsession_list, null);
            holder = new ViewHolder();
            holder.ViewHolderInit(convertView);
            convertView.setTag(holder);
        }
        holder = (ViewHolder) convertView.getTag();
        if (session.getSessionType() == SessionEntity.TYPE_DIALOG) {
            buildSessionItemWithDialog(holder, session, message);
        } else if (session.getSessionType() == SessionEntity.TYPE_GROUP) {
            buildSessionItemWithChannel(holder, session, message);
        }

        holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (session.getSessionStatus() != SessionEntity.SESSION_STATE_DIALOG) {
                    listener.SessionSelectListener(selectOptions, session.getSessionId());
                } else {
                    ToastUtil.shortShow(getContext().getApplicationContext(), R.string.select_no_type);
                }
            }
        });

        return convertView;
    }

    /**
     * 显示预定义组会话
     *
     * @param holderChannel
     * @param session
     * @param message
     */
    private void buildSessionItemWithChannel(ViewHolder holderChannel, final SessionEntity session, MessageEntity message) {
        if (selectOptions.contains(session.getSessionId())) {
            if (animSet.contains(session.getSessionId())) {
                holderChannel.ivPhoto.setText("已选");
                holderChannel.ivPhoto.setTextColor(Color.BLACK);
            } else {
                Logger.d(TAG, "openRotateAnimation");
                holderChannel.ivPhoto.setText("已选");
                holderChannel.ivPhoto.setTextColor(Color.BLACK);
                animSet.add(session.getSessionId());
            }
        } else {
            if (animSet.contains(session.getSessionId())) {
                animSet.remove(session.getSessionId());
            } else {
                holderChannel.ivPhoto.setText("未选");
                holderChannel.ivPhoto.setTextColor(Color.GRAY);
            }
        }

        if (unreadCount == 0) {
            holderChannel.badgeView.setVisibility(View.GONE);
        } else {
            holderChannel.badgeView.setVisibility(View.VISIBLE);
            if (unreadCount <= 99) {
                holderChannel.badgeView.setText(unreadCount + "");
            } else {
                holderChannel.badgeView.setText("99+");
            }
            holderChannel.badgeView.show();
        }

        // 判断会话是否建立
        if (session.getSessionStatus() == SessionEntity.SESSION_STATE_DIALOG) {
            holderChannel.ivHangup.setVisibility(View.VISIBLE);
            holderChannel.tvDate.setVisibility(View.GONE);
            holderChannel.ivLock.setVisibility(View.VISIBLE);
            // 判断会话是否锁定
            if (session.isLocked()) {
                holderChannel.ivLock.setImageResource(R.drawable.history_locked);
                holderChannel.ivHangup.setImageResource(R.drawable.history_open_default);
            } else {
                holderChannel.ivLock.setImageResource(R.drawable.history_unlocked);
                if (IntercomManager.INSTANCE.hasSessionLocked()) {
                    holderChannel.ivHangup.setImageResource(R.drawable.history_close_default);
                } else {
                    holderChannel.ivHangup.setImageResource(R.drawable.history_open_default);
                }
            }
        } else {
            holderChannel.ivHangup.setVisibility(View.GONE);
            holderChannel.tvDate.setVisibility(View.VISIBLE);
            holderChannel.ivLock.setVisibility(View.GONE);
        }

        // 显示会话状态
        switch (session.getSessionStatus()) {
            case SessionEntity.SESSION_STATE_IDLE:
                if (message != null) {
                    //设置记录时间
                    holderChannel.tvDate.setText(DateTool.isToday(message.getDate()) ? message.getTime() : message.getDate());
                    boolean isSelf = message.isSelf(getContext());
                    String fromName = message.getNameFrom();
                    //设置通话内容缩略
                    switch (message.getType()) {
                        case MessageType.TYPE_TEXT:
                            holderChannel.tvState.setText((isSelf ? getContext().getString(R.string.name_from_me) : fromName) + ":" + message.getBody());
                            break;
                        case MessageType.TYPE_PICTURE:
                            holderChannel.tvState.setText((isSelf ? getContext().getString(R.string.name_from_me) : fromName) + getContext().getString(R.string.message_type_picture));
                            break;
                        case MessageType.TYPE_VIDEO:
                            holderChannel.tvState.setText((isSelf ? getContext().getString(R.string.name_from_me) : fromName) + getContext().getString(R.string.message_type_video));
                            break;
                        case MessageType.TYPE_RECORD:
                            holderChannel.tvState.setText((isSelf ? getContext().getString(R.string.name_from_me) : fromName) + getContext().getString(R.string.message_type_record));
                            break;
                        case MessageType.TYPE_LOCATION:
                            holderChannel.tvState.setText((isSelf ? getContext().getString(R.string.name_from_me) : fromName) + getContext().getString(R.string.message_type_location));
                            break;
                        case MessageType.TYPE_SYSTEM:
                            holderChannel.tvState.setText(message.getBody());
                            break;
                        default:
                            break;
                    }
                } else {
                    holderChannel.tvState.setText(R.string.no_message);
                }
                break;
            case SessionEntity.SESSION_STATE_CALLING:
                holderChannel.tvState.setText(R.string.setting_up_the_session);
                break;
            case SessionEntity.SESSION_STATE_DIALOG:
                switch (session.getMediaState()) {
                    case SessionEntity.MEDIA_STATE_LISTEN:
                        holderChannel.tvState.setText(session.getSpeaker().getDisplayName() + getContext().getString(R.string.intercom_state_talking));
                        break;
                    case SessionEntity.MEDIA_STATE_TALK:
                        holderChannel.tvState.setText(R.string.talking_by_myself);
                        break;
                    case SessionEntity.MEDIA_STATE_IDLE://闲置
                        holderChannel.tvState.setText(R.string.state_free);
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }

        // 设置会话名称
        holderChannel.tvName.setText(session.getChannelName());

        // 显示时间
        if (message != null) {
            //设置记录时间
            holderChannel.tvDate.setText(DateTool.isToday(message.getDate()) ? message.getTime() : message.getDate());
        } else {
            holderChannel.tvDate.setVisibility(View.GONE);
        }

        // 挂断按钮监听事件
        holderChannel.ivHangup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.CurrentSessionListViewCheckListener(session);
            }
        });

        holderChannel.ivLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.switchSessionLock(session);
            }
        });
    }

    /**
     * 显示临时会话
     *
     * @param holderDialog
     * @param session
     * @param message
     * @return
     */
    private void buildSessionItemWithDialog(ViewHolder holderDialog, final SessionEntity session, MessageEntity message) {
        holderDialog.ivHangup.setVisibility(View.GONE);
        holderDialog.ivLock.setVisibility(View.GONE);

        if (unreadCount == 0) {
            holderDialog.badgeView.setVisibility(View.GONE);
        } else {
            holderDialog.badgeView.setVisibility(View.VISIBLE);
            if (unreadCount <= 99) {
                holderDialog.badgeView.setText(unreadCount + "");
            } else {
                holderDialog.badgeView.setText("99+");
            }
            holderDialog.badgeView.show();
        }

        //设置头像
        if (selectOptions.contains(session.getSessionId())) {
            if (animSet.contains(session.getSessionId())) {
                holderDialog.ivPhoto.setText("已选");
                holderDialog.ivPhoto.setTextColor(Color.BLACK);
            } else {
                Logger.d(TAG, "openRotateAnimation");
                holderDialog.ivPhoto.setText("已选");
                holderDialog.ivPhoto.setTextColor(Color.BLACK);
                animSet.add(session.getSessionId());
            }
        } else {
            if (animSet.contains(session.getSessionId())) {
                animSet.remove(session.getSessionId());
            } else {
                holderDialog.ivPhoto.setText("未选");
                holderDialog.ivPhoto.setTextColor(Color.GRAY);
            }
        }
        //设置组名/用户名
        holderDialog.tvName.setText(session.getSessionName());
        if (message != null) {
            //设置记录时间
            holderDialog.tvDate.setVisibility(View.VISIBLE);
            holderDialog.tvDate.setText(DateTool.isToday(message.getDate()) ? message.getTime() : message.getDate());
            boolean isSelf = message.isSelf(getContext());
            String fromName = message.getNameFrom();
            //设置通话内容缩略
            switch (message.getType()) {
                case MessageType.TYPE_TEXT:
                    if (null == message.getBody()) {
                        holderDialog.tvState.setText((isSelf ? getContext().getString(R.string.name_from_me) : fromName) + ":" + getContext().getString(R.string.intercom_im_tip_invalid_message));
                    } else {
                        holderDialog.tvState.setText((isSelf ? getContext().getString(R.string.name_from_me) : fromName) + ":" + message.getBody());
                    }
                    break;
                case MessageType.TYPE_PICTURE:
                    holderDialog.tvState.setText((isSelf ? getContext().getString(R.string.name_from_me) : fromName) + getContext().getString(R.string.message_type_picture));
                    break;
                case MessageType.TYPE_VIDEO:
                    holderDialog.tvState.setText((isSelf ? getContext().getString(R.string.name_from_me) : fromName) + getContext().getString(R.string.message_type_video));
                    break;
                case MessageType.TYPE_RECORD:
                    holderDialog.tvState.setText((isSelf ? getContext().getString(R.string.name_from_me) : fromName) + getContext().getString(R.string.message_type_record));
                    break;
                case MessageType.TYPE_LOCATION:
                    holderDialog.tvState.setText((isSelf ? getContext().getString(R.string.name_from_me) : fromName) + getContext().getString(R.string.message_type_location));
                    break;
                case MessageType.TYPE_SYSTEM:
                    holderDialog.tvState.setText(message.getBody());
                    break;
                default:
                    break;
            }
        } else {
            holderDialog.tvState.setText(R.string.no_message);
        }
    }

    class ViewHolder {
        protected ImageView ivHangup, ivLock;
        protected TextView ivPhoto, tvName, tvDate, tvState;
        protected BadgeView badgeView;

        ViewHolder() {
        }

        protected void ViewHolderInit(View convertView) {
            ivPhoto = (TextView) convertView.findViewById(R.id.iv_groups_temporary);
            ivHangup = (ImageView) convertView.findViewById(R.id.iv_hangup);
            ivLock = (ImageView) convertView.findViewById(R.id.iv_lock);
            tvName = (TextView) convertView.findViewById(R.id.tv_group_name);
            tvState = (TextView) convertView.findViewById(R.id.tv_state);
            tvDate = (TextView) convertView.findViewById(R.id.tv_history_date);

            badgeView = new BadgeView(getContext(), ivPhoto);
            badgeView.setBadgeBackgroundColor(Color.RED);
            badgeView.setTextSize(10);
            badgeView.setBadgePosition(BadgeView.POSITION_TOP_RIGHT); //默认值
        }
    }

    protected class ViewHolderDialog extends HistorySessionAdapter.ViewHolder {
        protected ViewHolderDialog() {
            super();
        }

        @Override
        public void ViewHolderInit(View convertView) {
            super.ViewHolderInit(convertView);
        }
    }

    protected class ViewHolderChannel extends HistorySessionAdapter.ViewHolder {
        protected ViewHolderChannel() {
            super();
        }

        @Override
        public void ViewHolderInit(View convertView) {
            super.ViewHolderInit(convertView);
        }
    }

    /**
     * *****************************CurrentSessionListViewCheckListener*****************************
     * 挂断会话事件传给实现者处理
     */
    private HistorySessionAdapter.CurrentSessionListViewCheckListener listener;

    public void setInterface(HistorySessionAdapter.CurrentSessionListViewCheckListener listener) {
        this.listener = listener;
    }


    public interface CurrentSessionListViewCheckListener {
        void CurrentSessionListViewCheckListener(SessionEntity sessionEntity);

        void SessionSelectListener(Set<String> selectOptions, String sessionCode);

        void switchSessionLock(SessionEntity sessionEntity);
    }
}


