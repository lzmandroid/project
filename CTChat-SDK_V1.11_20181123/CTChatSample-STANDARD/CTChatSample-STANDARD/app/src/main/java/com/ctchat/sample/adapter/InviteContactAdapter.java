package com.ctchat.sample.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.ctchat.sample.R;
import com.ctchat.sdk.ptt.tool.contact.ContactManager;
import com.ctchat.sdk.ptt.tool.entity.UserInfo;
import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sdk.ptt.tool.userinfo.UserInfoManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.GRAY;

public class InviteContactAdapter extends BaseAdapter{
    private Context mContext;
    private List<ContactEntity> contactList;
    private Set<String> selectOptions = new HashSet<>();
    private Set<String> animSet = new HashSet<>();

    public InviteContactAdapter(Context context, List<ContactEntity> contactList) {
        this.mContext = context;
        this.contactList = contactList;
    }

    @Override
    public int getCount() {
        return contactList.size() > 0 ? contactList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return contactList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        final ContactEntity contact = contactList.get(position);
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_select_contact,null);
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_select_contacts_name);
            viewHolder.tvNumber = (TextView) convertView.findViewById(R.id.iv_select_contacts_number);
            viewHolder.tvState = (TextView) convertView.findViewById(R.id.tv_contact_online_state);
            viewHolder.ivPhoto = (TextView) convertView.findViewById(R.id.iv_select_contacts_head_view);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvName.setText(contact.getDisplayName());
        viewHolder.tvNumber.setText(contact.getMdn());
        UserInfo userInfo = UserInfoManager.getInstance().getUserInfo(mContext);
        if (ContactManager.INSTANCE.getContactStateByEntity(contact) == ContactEntity.CONTACT_STATE_NONE &&
                !(contact.getMdn().equals(userInfo.getMdn()))){
            viewHolder.tvState.setText(R.string.offline);
            viewHolder.tvState.setTextColor(Color.GRAY);
        }else {
            int state = contact.getStateInChat();

            switch (state){
                case ContactEntity.IN_CHAT_STATE_ONLINE:
                    viewHolder.tvState.setText(R.string.reception_online);
                    viewHolder.tvState.setTextColor(BLACK);
                    break;
                case ContactEntity.IN_CHAT_STATE_OFFLINE:
                    viewHolder.tvState.setText(R.string.bg_online);
                    viewHolder.tvState.setTextColor(BLACK);
                    break;
            }
        }

        if (selectOptions.contains(contact.getMdn())) {
            if (animSet.contains(contact.getMdn())) {
                viewHolder.ivPhoto.setText("已选");
                viewHolder.ivPhoto.setTextColor(BLACK);
            } else {
                viewHolder.ivPhoto.setText("已选");
                viewHolder.ivPhoto.setTextColor(BLACK);
                animSet.add(contact.getMdn());
            }

        } else {
            if (animSet.contains(contact.getMdn())) {//未播放过结束动画
                viewHolder.ivPhoto.setText("未选");
                viewHolder.ivPhoto.setTextColor(GRAY);
                animSet.remove(contact.getMdn());
            } else {//播放过结束动画
                viewHolder.ivPhoto.setText("未选");
                viewHolder.ivPhoto.setTextColor(GRAY);
            }
        }
        return convertView;
    }

    class ViewHolder{
        TextView tvName, tvNumber, tvState;
        TextView ivPhoto;
    }

    public Set<String> getSelectOptions() {
        return selectOptions;
    }

    public void setSelectOptions(Set<String> selectOptions) {
        this.selectOptions = selectOptions;
    }

    public void clearAnimSet() {
        this.animSet.clear();
    }
}
