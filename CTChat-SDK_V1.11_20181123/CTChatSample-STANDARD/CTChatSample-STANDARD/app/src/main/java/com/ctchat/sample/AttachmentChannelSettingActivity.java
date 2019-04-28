package com.ctchat.sample;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sample.R;
import com.ctchat.sdk.ptt.tool.channel.ChannelManager;
import com.ctchat.sdk.ptt.tool.entity.ChannelEntity;
import com.ctchat.sample.io.Setting;
import com.ctchat.sample.adapter.AttachmentChannelAdapter;
import com.ctchat.sample.widget.XpActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AttachmentChannelSettingActivity extends XpActivity {
    private static final String TAG = "AttachmentChannelSettin";
    private ListView lvAtachmentList;
    private AttachmentChannelAdapter adapter;
    private List<ChannelEntity> channelList;
    private RelativeLayout backLayout;


    @Override
    protected void initView() {
        lvAtachmentList = (ListView) findViewById(R.id.lv_attachment_channel);
        backLayout = (RelativeLayout) findViewById(R.id.rl_attachment_channel_setting_back_to_main);
        backLayout.setOnClickListener(this);
        channelList = new ArrayList<ChannelEntity>();
        channelList = ChannelManager.INSTANCE.getLoadedChannels();
        adapter = new AttachmentChannelAdapter(AttachmentChannelSettingActivity.this, channelList);
        lvAtachmentList.setAdapter(adapter);
        lvAtachmentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!channelList.get(position).isAttachedItem()) {
                    channelList.get(position).setAttachItem(true);
                } else {
                    channelList.get(position).setAttachItem(false);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onPause() {
        super.onPause();
        channelAttachSave();
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_attachment_channel_setting;
    }

    @Override
    protected String[] broadcastActions() {
        return new String[0];
    }

    @Override
    protected void doAction(Intent intent) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_attachment_channel_setting_back_to_main:
                finish();
                break;
        }
    }

    /**
     * **********************附着预定义组相关的操作**********************
     */
    private void channelJsonBuild(List<ChannelEntity> channels) {
        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            for (int i = 0; i < channels.size(); i++) {
                jsonArray.put(i, channels.get(i).getId());
            }
            jsonObject.put(Setting.ATTACH_CHANNEL_KEY, jsonArray);

            Setting.setAttachedChannel(context, jsonObject.toString());

        } catch (JSONException e) {
            Logger.e(TAG, "Channel Json save Err:" + e.getMessage());
            Setting.setAttachedChannel(context, "");
        }
    }

    /**
     * 保存附着预定义组到本地
     */
    public void channelAttachSave() {
        List<ChannelEntity> attachedChannelList = new ArrayList<>();

        List<ChannelEntity> list = ChannelManager.INSTANCE.getLoadedChannels();

        for (ChannelEntity channel : list) {
            if (channel.isAttachedItem()) {
                attachedChannelList.add(channel);
            }
        }

        channelJsonBuild(attachedChannelList);
    }


    private void channelJsonParse(List<ChannelEntity> channels) {

    }
}
