package com.ctchat.sample.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ctchat.sample.R;
import com.ctchat.sdk.ptt.tool.contact.ContactManager;
import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sdk.ptt.tool.userinfo.UserInfoManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AllContactsAdapter extends BaseAdapter {

    private Context mContext;
    private List<ContactEntity> contactsList;
    private static final String TAG = "AllContactsAdapter";
    private Set<String> selectOptions = new HashSet<>();
    private Set<String> animSet = new HashSet<>();

    public AllContactsAdapter(Context context, List<ContactEntity> contactsList) {
        this.mContext = context;
        this.contactsList = contactsList;
    }

    @Override
    public int getCount() {
        return contactsList.size() > 0 ? contactsList.size() : 0;
    }

    @Override
    public Object getItem(int i) {
        return contactsList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        final ContactEntity contactBean = contactsList.get(position);
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_all_contacts,null);
            new ViewHolder(view);
        }
          ViewHolder viewHolder = (ViewHolder) view.getTag();

        if (selectOptions.contains(contactBean.getMdn())) {
            if (animSet.contains(contactBean.getMdn())) {
                viewHolder.ivAvatar.setText("已选");
                viewHolder.ivAvatar.setTextColor(Color.BLACK);
            } else {
                viewHolder.ivAvatar.setText("已选");
                viewHolder.ivAvatar.setTextColor(Color.BLACK);
                animSet.add(contactBean.getMdn());
            }

        } else {
            if (animSet.contains(contactBean.getMdn())) {//未播放过结束动画
                viewHolder.ivAvatar.setText("未选");
                viewHolder.ivAvatar.setTextColor(Color.GRAY);
                animSet.remove(contactBean.getMdn());
            } else {//播放过结束动画
                viewHolder.ivAvatar.setText("未选");
                viewHolder.ivAvatar.setTextColor(Color.GRAY);
            }
        }
        viewHolder.tvName.setText(contactBean.getDisplayName());
        viewHolder.tvNumber.setText(contactBean.getMdn());
        int state = ContactManager.INSTANCE.getContactStateByMdn(contactBean.getMdn());

        if (TextUtils.equals(contactBean.getMdn(), UserInfoManager.getInstance().getUserInfo(mContext).getMdn())){
            viewHolder.tvStatus.setText(R.string.online);
            viewHolder.tvName.setText(R.string.name_from_me);
            viewHolder.tvStatus.setTextColor(mContext.getResources().getColor(R.color.black));
        } else {
            if (state == ContactEntity.CONTACT_STATE_NONE) {
                viewHolder.tvStatus.setText(R.string.offline);
                viewHolder.tvStatus.setTextColor(mContext.getResources().getColor(R.color.contact_offline_color));
            } else {
                viewHolder.tvStatus.setText(R.string.online);
                viewHolder.tvStatus.setTextColor(mContext.getResources().getColor(R.color.black));
            }
        }


        viewHolder.ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactCheckListener.ContactCheckListener(selectOptions, contactBean.getMdn());
            }
        });

        return view;
    }

    class ViewHolder {
        TextView tvName;
        TextView tvNumber;
        TextView tvStatus;
        TextView ivAvatar;

        public ViewHolder(View view) {
            tvName = (TextView) view.findViewById(R.id.tv_contacts_name);
            tvNumber = (TextView) view.findViewById(R.id.iv_contacts_number);
            tvStatus = (TextView) view.findViewById(R.id.iv_contacts_version);
            ivAvatar = (TextView) view.findViewById(R.id.iv_contacts_icon_default);
            view.setTag(this);
        }
    }

    public Set<String> getSelectOptions() {
        return selectOptions;
    }

    public void clearSelectOptions() {
        this.selectOptions.clear();
    }

    public void clearAnimSet() {
        this.animSet.clear();
    }

    private ContactCheckListener contactCheckListener;

    public interface ContactCheckListener {
        void ContactCheckListener(Set<String> selectOptions, String mdn);
    }

    public void setInterface(ContactCheckListener contactCheckListener) {
        this.contactCheckListener = contactCheckListener;
    }
}
