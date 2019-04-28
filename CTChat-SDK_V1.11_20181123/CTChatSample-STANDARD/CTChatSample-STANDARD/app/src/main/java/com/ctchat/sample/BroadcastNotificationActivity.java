package com.ctchat.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.ctchat.sample.adapter.BroadcastNotificationAdapter;
import com.ctchat.sample.tool.DialogTool;
import com.ctchat.sample.widget.PullToRefreshView;
import com.ctchat.sample.widget.XpActivity;
import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sdk.basemodule.util.ToastUtil;
import com.ctchat.sdk.ptt.tool.broadcast.BroadcastManager;
import com.ctchat.sdk.ptt.tool.broadcast.OnBroadcastGetListener;
import com.ctchat.sdk.ptt.tool.broadcast.OnNewBroadCastSubscribe;
import com.ctchat.sdk.ptt.tool.entity.BroadcastEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BroadcastNotificationActivity extends XpActivity implements OnBroadcastGetListener, BroadcastNotificationAdapter.BroadcastCheckListener,OnNewBroadCastSubscribe {

    private View contentView;
    private RelativeLayout backLayout;
    private List<BroadcastEntity> broadcastEntityList = new ArrayList<>();
    private BroadcastNotificationAdapter adapter;
    private ListView lvBroadcast;
    private PullToRefreshView listPullToRefreshView;
    private PopupWindow popupSelect;
    private static final String TAG = "BroadcastNotificationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.d(TAG, "onCreate");
        BroadcastManager.getInstance().registerBroadcastGetListener(this);
        BroadcastManager.getInstance().registerNewBroadcastListener(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initView() {
        contentView = LayoutInflater.from(this).inflate(getContentLayout(), null);
        backLayout = (RelativeLayout) findViewById(R.id.rl_broadcast_back_to_main);
        lvBroadcast = (ListView) findViewById(R.id.lv_broadcast_list);
        View footerView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_view_popup_window, null, true);
        lvBroadcast.addFooterView(footerView, null, false);
        backLayout.setOnClickListener(this);
        lvBroadcast.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BroadcastEntity broadcast = broadcastEntityList.get(position);
                if (popupSelect.isShowing()) {
                    Set<String> selectOptions = adapter.getSelectOptions();
                    multiSelect(selectOptions, broadcast.getId());
                } else {
                    displayBroadcast(broadcast);
                }
            }
        });

        listPullToRefreshView = (PullToRefreshView) findViewById(R.id.pv_broadcast_notice);
        listPullToRefreshView.setOnHeaderRefreshListener(
                new PullToRefreshView.OnHeaderRefreshListener() {
                    @Override
                    public void onHeaderRefresh(PullToRefreshView view) {
                        //刷新广播信息
                        BroadcastManager.getInstance().refreshBroadcastDatas();
                    }
                });

        initPopupWindow();
        adapter = new BroadcastNotificationAdapter(this, broadcastEntityList, lvBroadcast);
        adapter.setInterface(this);
        lvBroadcast.setAdapter(adapter);
    }

    /**
     * 展示广播
     * @param broadcast
     */
    private void displayBroadcast(final BroadcastEntity broadcast) {
        DialogTool.createBroadcastNoticeDialog(this, broadcast, new DialogTool.DialogClickCallBack() {
            @Override
            public void positiveButtonClick(Object obj) {
                // TODO: 2017/1/16 修改已读状态，刷新数据
                broadcast.setReadState(BroadcastEntity.STATE_READ);
                BroadcastManager.getInstance().updateBroadcastReadState(broadcast);
            }

            @Override
            public void neutralButtonClick(Object obj) {

            }
        });
    }

    @Override
    protected void initData() {
        Logger.d(TAG, "initData");
        BroadcastManager.getInstance().broadcastLocalLoad();
        BroadcastManager.getInstance().refreshBroadcastDatas();
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_broadcast_notification;
    }

    @Override
    protected String[] broadcastActions() {
        return new String[0];
    }

    @Override
    protected void doAction(Intent intent) {

    }

    @Override
    protected void onResume() {
        Logger.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Logger.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BroadcastManager.getInstance().unregisterBroadcastGetListener();
        BroadcastManager.getInstance().unregisterNewBroadcastListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_broadcast_back_to_main:
                finish();
                break;
        }
    }

    /**
     * 广播消息排序
     * @param list
     */
    private void sortNotice(List<BroadcastEntity> list) {
        List<BroadcastEntity> listUnread = new ArrayList<>();
        List<BroadcastEntity> listRead = new ArrayList<>();
        for (BroadcastEntity notice : list) {
            if (notice.getReadState() == BroadcastEntity.STATE_UNREAD) {
                listUnread.add(notice);
            } else {
                listRead.add(notice);
            }
        }
        Collections.sort(listUnread);
        Collections.sort(listRead);
        //合并两个list
        broadcastEntityList.clear();
        broadcastEntityList.addAll(listUnread);
        broadcastEntityList.addAll(listRead);
    }

    /**
     * 删除广播消息
     */
    private void deleteNotice() {
        Set<String> options = adapter.getSelectOptions();
        if (options.size() == broadcastEntityList.size()) {
            BroadcastManager.getInstance().broadcastClean();
        } else {
            for (String id : options) {
                BroadcastManager.getInstance().broadcastRemove(id);
            }
//            List<BroadcastEntity> afterList = BroadcastManager.getInstance().getBroadcastList();
//            onBroadcastGetSuccess(afterList);
        }
        ToastUtil.shortShow(context, options.size() + context.getString(R.string.broadcast_delete));
        hidePopupWindow();
    }

    /**
     * 多选模式
     * @param selectOptions
     * @param id
     */
    private void multiSelect(Set<String> selectOptions, String id) {
        if (selectOptions.contains(id)) {
            selectOptions.remove(id);
            if (selectOptions.size() == 0) {
                hidePopupWindow();
            }
        } else {
            if (id != null) {
                selectOptions.add(id);
            }
        }

        adapter.notifyDataSetChanged();
    }

    /**
     * 初始化popupwindow
     */
    private void initPopupWindow() {
        View view;
        final LinearLayout deleteNoticeLayout,cancelSelectLayout;
        view = LayoutInflater.from(this).inflate(R.layout.popwindow_broadcast_bottom_bar, null);
        popupSelect = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupSelect.setOutsideTouchable(true);
        popupSelect.setAnimationStyle(R.style.popupwindow_contact_list_anim_style);
        popupSelect.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        deleteNoticeLayout = (LinearLayout) view.findViewById(R.id.rl_broadcast_bottom_delete);
        cancelSelectLayout = (LinearLayout) view.findViewById(R.id.rl_broadcast_bottom_cancel_select);
        deleteNoticeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteNotice();
            }
        });
        cancelSelectLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePopupWindow();
            }
        });
    }

    public void hidePopupWindow() {
        if (popupSelect != null && popupSelect.isShowing()) {
            adapter.clearSelectOptions();
            adapter.clearAnimSet();
            popupSelect.dismiss();
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBroadcastGetSuccess(List<BroadcastEntity> list) {
        listPullToRefreshView.onHeaderRefreshComplete();
        sortNotice(list);
        adapter.notifyDataSetChanged();

        // 设置取消广播更新标志位
//        BroadcastManager.getInstance().setBroadcastUpdate(false);
    }

    @Override
    public void onBroadCastUpdateComplete(List<BroadcastEntity> list) {
        sortNotice(list);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onBroadcastGetError(String errorMsg) {
        listPullToRefreshView.onHeaderRefreshComplete();
        ToastUtil.shortShow(context,errorMsg);
        broadcastEntityList.clear();
        broadcastEntityList.addAll(BroadcastManager.getInstance().getBroadcastList());
        sortNotice(broadcastEntityList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void BroadcastCheckListener(Set<String> selectOptions, String id) {
        if (!popupSelect.isShowing()) {
            popupSelect.showAtLocation(contentView, Gravity.BOTTOM, 0, 0);
        }
        multiSelect(selectOptions, id);
    }

    @Override
    public void onNewBroadCast(String content) {
        BroadcastManager.getInstance().refreshBroadcastDatas();
    }
}
