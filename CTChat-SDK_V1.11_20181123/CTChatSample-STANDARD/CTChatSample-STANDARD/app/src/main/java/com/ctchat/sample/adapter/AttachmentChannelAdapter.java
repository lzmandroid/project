package com.ctchat.sample.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.ctchat.sample.R;
import com.ctchat.sdk.ptt.tool.entity.ChannelEntity;

import java.util.List;

public class AttachmentChannelAdapter extends BaseAdapter{
    private Context context;
    private List<ChannelEntity> list;

    public AttachmentChannelAdapter(Context context, List<ChannelEntity> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ChannelEntity channel = (ChannelEntity) getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_attachment_channel, null);
            viewHolder = new ViewHolder();
            viewHolder.tvChannelName = (TextView) convertView.findViewById(R.id.tv_attachment_channel_name);
            viewHolder.cbChannelSelect = (CheckBox) convertView.findViewById(R.id.cb_attachment_channel_select);
            convertView.setTag(viewHolder);
        }
        viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.tvChannelName.setText(channel.getName());
        viewHolder.cbChannelSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!channel.isAttachedItem()) {
                        channel.setAttachItem(true);
                    }
                } else {
                    channel.setAttachItem(false);
                }
            }
        });
        if (channel.isAttachedItem()) {
            viewHolder.cbChannelSelect.setChecked(true);
        } else {
            viewHolder.cbChannelSelect.setChecked(false);
        }
        return convertView;
    }

   class ViewHolder {
       TextView tvChannelName;
       CheckBox cbChannelSelect;
   }
}
