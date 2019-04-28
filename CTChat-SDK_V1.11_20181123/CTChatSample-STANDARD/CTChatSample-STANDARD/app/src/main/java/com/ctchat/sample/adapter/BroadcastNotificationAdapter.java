package com.ctchat.sample.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ctchat.sample.R;
import com.ctchat.sample.tool.AnimationTool;
import com.ctchat.sdk.basemodule.api.adapter.XpBaseAdapter;
import com.ctchat.sdk.ptt.tool.entity.BroadcastEntity;
import com.ctchat.sdk.ptt.util.DateTool;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BroadcastNotificationAdapter extends XpBaseAdapter<BroadcastEntity> {

    private int num_unread;
    private int num_read;
    private Set<String> selectOptions = new HashSet<>();
    private Set<String> animSet = new HashSet<>();

    public BroadcastNotificationAdapter(Context context, List<BroadcastEntity> data, ListView listView) {
        super(context, data, listView);
    }

    public Set<String> getSelectOptions() {
        return selectOptions;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        getNum();
        final BroadcastEntity broadcastEntity = getData().get(position);
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = getLayoutInflater().inflate(R.layout.item_broadcast_nitice, null);
            viewHolder = new ViewHolder();
            viewHolder.ivPhoto = (ImageView) convertView.findViewById(R.id.iv_notice_photo);
            viewHolder.ivDetails = (ImageView) convertView.findViewById(R.id.iv_notice_details);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_notice_title);
            viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tv_notice_content);
            viewHolder.tvDate = (TextView) convertView.findViewById(R.id.tv_notice_date);
            viewHolder.tvType = (TextView) convertView.findViewById(R.id.tv_notice_type);
            convertView.setTag(viewHolder);
        }

        viewHolder = (ViewHolder) convertView.getTag();

        // 判断是否需要显示消息类型头
        if (position == 0) {
            if (broadcastEntity.getReadState() == BroadcastEntity.STATE_UNREAD) {
                viewHolder.tvType.setVisibility(View.VISIBLE);
                viewHolder.tvType.setText(getContext().getString(R.string.unread_message) + "("+num_unread+")");
            } else {
                viewHolder.tvType.setVisibility(View.VISIBLE);
                viewHolder.tvType.setText(getContext().getString(R.string.have_read_message) + "("+num_read+")");
            }
        } else {
            int currentType = broadcastEntity.getReadState();
            int lastType = getData().get(position - 1).getReadState();
            if ((currentType == BroadcastEntity.STATE_READ) && (lastType == BroadcastEntity.STATE_UNREAD)) {
                viewHolder.tvType.setVisibility(View.VISIBLE);
                viewHolder.tvType.setText(getContext().getString(R.string.have_read_message) + "("+num_read+")");
            } else {
                viewHolder.tvType.setVisibility(View.GONE);
            }
        }

        // 头像
        // 判断是否被选中
        if (selectOptions.contains(broadcastEntity.getId())) {
            if (animSet.contains(broadcastEntity.getId())) {
                if (broadcastEntity.getReadState() == BroadcastEntity.STATE_UNREAD) {
                    viewHolder.ivPhoto.setImageResource(R.drawable.broadcast_msg_unread_select);
                } else {
                    viewHolder.ivPhoto.setImageResource(R.drawable.broadcast_msg_read_select);
                }
            } else {
                if (broadcastEntity.getReadState() == BroadcastEntity.STATE_UNREAD) {
                    AnimationTool.INSTANCE.startSelectAnimation(viewHolder.ivPhoto, R.drawable.broadcast_msg_unread_select);
                } else {
                    AnimationTool.INSTANCE.startSelectAnimation(viewHolder.ivPhoto, R.drawable.broadcast_msg_read_select);
                }
                animSet.add(broadcastEntity.getId());
            }
        } else {
            if (animSet.contains(broadcastEntity.getId())) {
                if (broadcastEntity.getReadState() == BroadcastEntity.STATE_UNREAD) {
                    AnimationTool.INSTANCE.startCancleAnimation(viewHolder.ivPhoto, R.drawable.broadcast_msg_unread);
                } else {
                    AnimationTool.INSTANCE.startCancleAnimation(viewHolder.ivPhoto, R.drawable.broadcast_msg_read);
                }
                animSet.remove(broadcastEntity.getId());
            } else {
                if (broadcastEntity.getReadState() == BroadcastEntity.STATE_UNREAD) {
                    viewHolder.ivPhoto.setImageResource(R.drawable.broadcast_msg_unread);
                } else {
                    viewHolder.ivPhoto.setImageResource(R.drawable.broadcast_msg_read);
                }
            }
        }

        viewHolder.ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                broadcastCheckListener.BroadcastCheckListener(selectOptions, broadcastEntity.getId());
            }
        });

        viewHolder.tvTitle.setText(broadcastEntity.getTitle());

        viewHolder.tvDate.setText(getBroadcastDate(broadcastEntity.getDate()));
        viewHolder.tvContent.setText(broadcastEntity.getContent());

        return convertView;
    }

    private String getBroadcastDate(String fulltime){
        String date = fulltime.substring(0,10);
        String time = fulltime.substring(11);

        return DateTool.isToday(date) ? time : date;
    }

    class ViewHolder {
        private ImageView ivPhoto;
        private ImageView ivDetails;
        private TextView tvTitle;
        private TextView tvDate;
        private TextView tvContent;
        private TextView tvType;
    }

    private void getNum(){
        num_read = num_unread = 0;
        for (BroadcastEntity notice : getData()) {
            if (notice.getReadState() == BroadcastEntity.STATE_UNREAD) {
                num_unread++;
            } else {
                num_read++;
            }
        }
    }

    public void clearSelectOptions() {
        this.selectOptions.clear();
    }

    public void clearAnimSet() {
        if (this.animSet != null) {
            animSet.clear();
        }
    }

    private BroadcastCheckListener broadcastCheckListener;

    public interface BroadcastCheckListener {
        void BroadcastCheckListener(Set<String> selectOptions, String id);
    }

    public void setInterface(BroadcastCheckListener broadcastCheckListener) {
        this.broadcastCheckListener = broadcastCheckListener;
    }
}
