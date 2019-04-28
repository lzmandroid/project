package com.ctchat.sample.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;


import com.ctchat.sample.R;
import com.ctchat.sdk.ptt.tool.contact.ContactManager;
import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sdk.ptt.tool.userinfo.UserInfoManager;
import com.ctchat.sample.tool.treelist.Node;
import com.ctchat.sample.tool.treelist.TreeListViewAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.GRAY;

public class SimpleTreeAdapter<T> extends TreeListViewAdapter<T> {
    private Context context;
    private static final String TAG = "SimpleTreeAdapter";
    private Set<String> selectOptions = new HashSet<>();
    private Set<String> animSet = new HashSet<>();

    public SimpleTreeAdapter(ListView mTree, Context context, List<T> datas,
                             int defaultExpandLevel) throws IllegalArgumentException,
            IllegalAccessException {
        super(mTree, context, datas, defaultExpandLevel);
        this.context = context;
    }

    private TreeViewCheckListener listener;

    @Override
    public View getConvertView(final Node node, final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.tree_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.icon = (TextView) convertView.findViewById(R.id.id_treenode_icon);
            viewHolder.head_icon = (TextView) convertView.findViewById(R.id.cb_treenode_head_view);
            viewHolder.label = (TextView) convertView.findViewById(R.id.id_treenode_label);
            viewHolder.number = (TextView) convertView.findViewById(R.id.id_treenode_number);
            viewHolder.tvState = (TextView) convertView.findViewById(R.id.tv_treenode_state);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.head_icon.clearAnimation();
        //显示名称
        viewHolder.label.setText(node.getName());

        //判断显示“箭头”图标or头像
        if (node.getIcon() == -1) {
            viewHolder.icon.setVisibility(View.INVISIBLE);
            viewHolder.head_icon.setVisibility(View.VISIBLE);
            viewHolder.number.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(node.getId())) {
                viewHolder.number.setText("(" + node.getId() + ")");
            }
            viewHolder.tvState.setVisibility(View.VISIBLE);
            ContactEntity contactEntity = node.getContactEntity();
            int state = ContactEntity.CONTACT_STATE_NONE;
            if(contactEntity !=null){
                state = ContactManager.INSTANCE.getContactStateByEntity(contactEntity);
            }

            if (TextUtils.equals(node.getId(), UserInfoManager.getInstance().getUserInfo(mContext).getMdn())){
                viewHolder.tvState.setText(R.string.online);
                viewHolder.label.setText(R.string.name_from_me);
                viewHolder.tvState.setTextColor(BLACK);
            } else {
                if (state == ContactEntity.CONTACT_STATE_NONE) {
                    viewHolder.tvState.setText(R.string.offline);
                    viewHolder.tvState.setTextColor(GRAY);
                } else {
                    viewHolder.tvState.setText(R.string.online);
                    viewHolder.tvState.setTextColor(BLACK);
                }
            }

        } else { // 显示箭头
            viewHolder.icon.setVisibility(View.VISIBLE);
            viewHolder.number.setVisibility(View.GONE);
            viewHolder.head_icon.setVisibility(View.GONE);
            if (node.getIcon() == R.drawable.tree_ec) {
                viewHolder.icon.setText("展开");
                viewHolder.icon.setTextColor(GRAY);
            } else {
                viewHolder.icon.setText("收起");
                viewHolder.icon.setTextColor(BLACK);
            }
            viewHolder.tvState.setVisibility(View.INVISIBLE);
        }

        //头像
        //判断是否被选中
        if (node.getType() == Node.USER_TYPE) {
            if (selectOptions.contains(node.getId())) {
                if (animSet.contains(node.getId())) {
                    viewHolder.head_icon.setText("已选");
                    viewHolder.head_icon.setTextColor(BLACK);
                } else {
                    viewHolder.head_icon.setText("已选");
                    viewHolder.head_icon.setTextColor(BLACK);
                    animSet.add(node.getId());
                }

            } else {
                if (animSet.contains(node.getId())) {//未播放过结束动画
                    viewHolder.head_icon.setText("未选");
                    viewHolder.head_icon.setTextColor(Color.GRAY);
                    animSet.remove(node.getId());
                } else {//播放过结束动画
                    viewHolder.head_icon.setText("未选");
                    viewHolder.head_icon.setTextColor(Color.GRAY);
                }
            }
        }

        viewHolder.head_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.treeViewCheckClickEvent(selectOptions,node.getId());
            }
        });

        return convertView;
    }

    private final class ViewHolder {
        TextView icon;
        TextView head_icon;
        TextView label;
        TextView number;
        TextView tvState;
    }


    public void setInterface(TreeViewCheckListener listener) {
        this.listener = listener;
    }

    public interface TreeViewCheckListener {
        void treeViewCheckClickEvent(Set<String> set, String mdn);
    }

    public Set<String> getSelectOptions() {
        return selectOptions;
    }

    public void clearSelectOptions() {
        if (this.selectOptions != null) {
            this.selectOptions.clear();
        }
    }

    public void clearAnimSet() {
        if (this.animSet != null) {
            animSet.clear();
        }
    }
}
