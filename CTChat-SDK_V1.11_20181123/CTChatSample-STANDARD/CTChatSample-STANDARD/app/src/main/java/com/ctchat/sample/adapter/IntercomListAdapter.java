
package com.ctchat.sample.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ctchat.sample.R;
import com.ctchat.sample.tool.AnimationTool;
import com.ctchat.sdk.ptt.tool.contact.ContactManager;
import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;
import com.ctchat.sdk.ptt.tool.entity.UserInfo;
import com.ctchat.sdk.ptt.tool.userinfo.UserInfoManager;

import java.util.HashSet;
import java.util.Set;

public class IntercomListAdapter extends BaseAdapter {

    private static final String TAG = "IntercomListAdapter";
    private Context context;
    private LayoutInflater mInflater;
    private SessionEntity currentSession;
    private Set<String> selectOptions = new HashSet<>();
    private Set<String> animSet = new HashSet<>();

    public IntercomListAdapter(Context context) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
    }

    public void setCurrentSession(SessionEntity currentSession) {
        this.currentSession = currentSession;
    }

    @Override
    public int getCount() {
        int i = 0;
        if (this.currentSession != null) {
            if (currentSession.getSessionType() == SessionEntity.TYPE_DIALOG) {
                i = this.currentSession.getMemberAll().size();
            } else if (currentSession.getSessionType() == SessionEntity.TYPE_GROUP) {
                i = this.currentSession.getChannel().getMemberAll().size();
            }
        }
        return i;
    }

    @Override
    public Object getItem(int position) {
        if (this.currentSession != null) {
            if (currentSession.getSessionType() == SessionEntity.TYPE_DIALOG) {
                return currentSession.getMemberAll().get(position);
            } else if (currentSession.getSessionType() == SessionEntity.TYPE_GROUP) {
                return currentSession.getChannel().getMemberAll().get(position);
            }
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ContactEntity contactEntity = (ContactEntity) getItem(position);
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_intercom_list, null);
            holder.ivContantPhoto = (ImageView) convertView.findViewById(R.id.iv_contact_photo);
            holder.ivAdmin = (ImageView) convertView.findViewById(R.id.iv_intercom_admin);
            holder.tvContantName = (TextView) convertView.findViewById(R.id.tv_contact_name);
            holder.tvContantNumber = (TextView) convertView.findViewById(R.id.tv_contact_number);
            holder.tvContantState = (TextView) convertView.findViewById(R.id.tv_contact_state);
            holder.ivInvite = (ImageView) convertView.findViewById(R.id.iv_invite_background_contact);
            convertView.setTag(holder);
        }

        holder = (ViewHolder) convertView.getTag();

        holder.ivInvite.setVisibility(View.GONE);
        //设置头像
        if (selectOptions.contains(contactEntity.getMdn())) {
            if (animSet.contains(contactEntity.getMdn())) {
                holder.ivContantPhoto.setImageResource(R.drawable.contact_select);
            } else {
                AnimationTool.INSTANCE.startSelectAnimation(holder.ivContantPhoto, R.drawable.contact_select);
                animSet.add(contactEntity.getMdn());
            }

        } else {
            if (animSet.contains(contactEntity.getMdn())) {//未播放过结束动画
                AnimationTool.INSTANCE.startCancleAnimation(holder.ivContantPhoto, R.drawable.groups_user);
                animSet.remove(contactEntity.getMdn());
            } else {//播放过结束动画
                holder.ivContantPhoto.setImageResource(R.drawable.groups_user);
            }
        }

        if (contactEntity != null) {
            holder.tvContantName.setText(contactEntity.getDisplayName());
            holder.tvContantNumber.setText("(" + contactEntity.getMdn() + ")");
        }else {
            holder.tvContantName.setText(contactEntity.getDisplayName());
        }

        boolean isBackgroundOnline = false;
        UserInfo userInfo = UserInfoManager.getInstance().getUserInfo(context);

        int state = contactEntity.getStateInChat();

        switch (state){
            case ContactEntity.IN_CHAT_STATE_ONLINE:
                holder.tvContantState.setText(R.string.reception_online);
                holder.tvContantState.setTextColor(Color.BLACK);
                isBackgroundOnline = false;
                break;
            case ContactEntity.IN_CHAT_STATE_OFFLINE:
                if (ContactManager.INSTANCE.getContactStateByEntity(contactEntity) == ContactEntity.CONTACT_STATE_NONE &&
                        !(contactEntity.getMdn().equals(userInfo.getMdn()))){
                    holder.tvContantState.setText(R.string.offline);
                    holder.tvContantState.setTextColor(Color.GRAY);
                    isBackgroundOnline = false;
                }else {
                    holder.tvContantState.setText(R.string.bg_online);
                    holder.tvContantState.setTextColor(context.getResources().getColor(R.color.base_color));
                    isBackgroundOnline = true;
                }
                break;
        }
        // 显示管理员标志
        if (currentSession.getSessionType() == SessionEntity.TYPE_GROUP) {
            switch (contactEntity.getCusertype()) {
                case ContactEntity.CURSETYPE_MANAGER:
                    holder.ivAdmin.setVisibility(View.VISIBLE);
                    break;
                case ContactEntity.CURSETYPE_USER:
                case ContactEntity.CURSETYPE_LISTEN_ONLY:
                case ContactEntity.CURSETYPE_NONE:
                case ContactEntity.CURSETYPE_LOW_PRIORITY:
                    holder.ivAdmin.setVisibility(View.GONE);
                    break;

            }
        } else if (currentSession.getSessionType() == SessionEntity.TYPE_DIALOG && currentSession.getSessionStatus() == SessionEntity.SESSION_STATE_DIALOG) {
            if (isBackgroundOnline) {
                holder.ivInvite.setVisibility(View.VISIBLE);
            } else {
                holder.ivInvite.setVisibility(View.GONE);
            }
        }

        holder.ivInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inviteBackgroundListener != null) {
                    inviteBackgroundListener.onInviteBackground(contactEntity);
                }
            }
        });
        return convertView;
    }

    class ViewHolder {
        ImageView ivContantPhoto, ivAdmin, ivInvite;
        TextView tvContantName, tvContantNumber, tvContantState;
    }

    public Set<String> getSelectOptions() {
        return selectOptions;
    }

    public void clearSelectionOptions() {
        this.selectOptions.clear();
    }

    public void clearAnimSet() {
        this.animSet.clear();
    }

    private InviteBackgroundListener inviteBackgroundListener;

    public interface InviteBackgroundListener {
        void onInviteBackground(ContactEntity contactEntity);
    }

    public void setInviteBackgroundListener(InviteBackgroundListener inviteBackgroundListener) {
        this.inviteBackgroundListener = inviteBackgroundListener;
    }

    public void removeInviteBackgroundListener() {
        this.inviteBackgroundListener = null;
    }
}
