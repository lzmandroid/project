package com.ctchat.sample;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.ctchat.sample.adapter.HomePagerAdapter;
import com.ctchat.sample.application.AppInitializeManager;
import com.ctchat.sample.entity.TabBean;
import com.ctchat.sample.fragment.ChannelFragment;
import com.ctchat.sample.fragment.ContactsFragment;
import com.ctchat.sample.fragment.HistorySessionFragment;
import com.ctchat.sample.io.DispatcherSPManager;
import com.ctchat.sample.presenter.MainPresent;
import com.ctchat.sample.presenter.MainPresentImpl;
import com.ctchat.sample.view.MainView;
import com.ctchat.sample.widget.ExitAppDialog;
import com.ctchat.sample.widget.XpActivity;
import com.ctchat.sdk.AccountManager;
import com.ctchat.sdk.LogoutCallback;
import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sdk.basemodule.util.ToastUtil;
import com.ctchat.sdk.ptt.tool.channel.ChannelManager;
import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;
import com.ctchat.sdk.ptt.tool.entity.ThemeInfo;
import com.ctchat.sdk.ptt.tool.entity.UserInfo;
import com.ctchat.sdk.ptt.tool.report.ReportManager;
import com.ctchat.sdk.ptt.tool.userinfo.UserInfoManager;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends XpActivity implements MainView, LogoutCallback {
    private static final String TAG = "MainActivity";
    private ContactsFragment contactsFragment;
    private ChannelFragment channelFragment;
    private HistorySessionFragment historyFragment;

    private MainPresent mainPresent;

    private ExitAppDialog.Builder exitDialog;
    private PopupWindow popupWindowMore;
    private RelativeLayout rlBroadcast;
    private RelativeLayout rlSetting;

    private long mExitTime = 0;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.i(TAG, "Main Activity on Create：" + this, true);
        super.onCreate(savedInstanceState);
        initViewPager();
        initPopupWindowMore();

        UserInfoManager.getInstance().getUserInfo(MainActivity.this).getMdn();

        com.ctchat.sdk.ptt.tool.entity.ThemeInfo.class.getName();



    }

    @Override
    protected void onResume() {
        Logger.d(TAG, "onResume");
        super.onResume();
        mainPresent.onResume();

        // 显示广播更新标志
        disPlayBroadcastUpdate();
    }
    public void refreshChannelFragmentList(){
        if (channelFragment != null){
            channelFragment.loadDataSuccess(ChannelManager.INSTANCE.getLoadedChannels());
        }
    }
    /**
     * 显示广播更新标志
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void disPlayBroadcastUpdate() {
    }

    @Override
    protected void onPause() {
        super.onPause();
        mainPresent.onPause();
        hidePopupWindowMore();
    }

    @Override
    public void initView() {

        viewPager = findViewById(R.id.homeViewPager);
        tabLayout = findViewById(R.id.tabLayout);
        findViewById(R.id.rl_main_side).setOnClickListener(this);
        findViewById(R.id.btn_setting).setOnClickListener(this);
        findViewById(R.id.btn_main_more).setOnClickListener(this);
    }

    private void initPopupWindowMore() {
        View view;
        view = LayoutInflater.from(this).inflate(R.layout.popup_window_more, null);
        popupWindowMore = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindowMore.setFocusable(true);//这里设置显示PopWindow之后在外面点击是否有效。如果为false的话，那么点击PopWindow外面并不会关闭PopWindow。
        popupWindowMore.setOutsideTouchable(true);//不能在没有焦点的时候使用
        popupWindowMore.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.bg_more)));
        rlBroadcast = (RelativeLayout) view.findViewById(R.id.rl_broadcast);
        rlSetting = (RelativeLayout) view.findViewById(R.id.rl_setting);
        rlSetting.setOnClickListener(this);
        rlBroadcast.setOnClickListener(this);
    }

    private void hidePopupWindowMore() {
        if (popupWindowMore != null && popupWindowMore.isShowing()) {
            popupWindowMore.dismiss();
        }
    }

    @Override
    public void initData() {
        mainPresent = new MainPresentImpl(this, this);

    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_main;
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
            case R.id.rl_main_side:
                showExitDialog();
                if (exitDialog != null) {
                    exitDialog.create().show();
                }
                break;
            case R.id.btn_main_more:
                toActivity(BroadcastNotificationActivity.class, null);
                break;
            case R.id.btn_setting:
                toActivity(SettingActivity.class, null);
                break;
            case R.id.rl_more_window_all:
                if (popupWindowMore != null && popupWindowMore.isShowing()) {
                    popupWindowMore.dismiss();
                }
                break;
            default:
                break;

        }
    }

    /**
     * *****************************ViewPager*****************************
     */
    private void initViewPager() {
        channelFragment = new ChannelFragment();
        contactsFragment = new ContactsFragment();
        historyFragment = new HistorySessionFragment();

        List<TabBean> dataList = new ArrayList<>();
        dataList.add(new TabBean("预定义组", channelFragment));
        dataList.add(new TabBean("联系人", contactsFragment));
        dataList.add(new TabBean("记录", historyFragment));
        viewPager.setAdapter(new HomePagerAdapter(getSupportFragmentManager(), dataList));
        tabLayout.setupWithViewPager(viewPager, true);
    }

    // 登出成功
    @Override
    public void onLogout() {
        ToastUtil.shortShow(this, R.string.logout_success);
        AppInitializeManager.exitApplication();
    }

    /**
     * 通知更新fragment界面
     */
    public void refreshHistoryFragment() {
        if (historyFragment != null) {
            historyFragment.notifyHistoryDataChanged();
        }
    }

    public void refreshChannelFragment() {
        if (channelFragment != null) {
            channelFragment.refreshView();
        }
    }


    /**
     * *****************************返回按钮*****************************
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "keyCode: " + keyCode + " action : " + event.getAction());
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            contactsFragment.hidePopupWindow();
//            if (dragLayout.getStatus() == DragLayout.Status.Open) {
//                dragLayout.close();
//            } else {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                // 如果两次按键时间间隔大于2000毫秒，则不退出
                ToastUtil.longShow(context, R.string.exit_application_tip);
                mExitTime = System.currentTimeMillis();// 更新mExitTime
            } else {// 否则退出程序
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            }
            return true;
//            }
        }

        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
//        if (Setting.getUseVolumeKey(this)) {
//            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && action == KeyEvent.ACTION_UP) {
//                if (pttSession != null) {
//                    IntercomManager.INSTANCE.releaseTalk(pttSession);
//                }
//            }
//        }
        return super.dispatchKeyEvent(event);
    }

    public void showExitDialog() {
        exitDialog = new ExitAppDialog.Builder(MainActivity.this);
        exitDialog.setBackButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        })
                .setConfirmButton(R.string.sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        AccountManager.logout(MainActivity.this);
                        DispatcherSPManager.setUpdateTime(context, "");
                    }
                })
                .setAutoLogin(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            // LoginSPManager.setAutoLoginStatus(MainActivity.this, true);
                        } else {
                            //LoginSPManager.setAutoLoginStatus(MainActivity.this, false);
                        }
                    }
                });
    }

    /*********** session Event **************/

    @Override
    public void onSessionEstablished(SessionEntity session, int ret) {
        Logger.d(TAG, "onSessionEstablished");
        refreshHistoryFragment();
        refreshChannelFragment();

    }

    @Override
    public void onSessionReleased(SessionEntity session, int reason) {
        Logger.d(TAG, "onSessionReleased");
        refreshChannelFragment();
        refreshHistoryFragment();
    }

    @Override
    public void onSessionPresence(SessionEntity sessionEntity, List<ContactEntity> membersAll, List<ContactEntity> membersPresence) {
        Logger.d(TAG, "onSessionPresence");
        refreshChannelFragment();
        refreshHistoryFragment();
    }

    @Override
    public void onSessionMemberUpdate(SessionEntity session, List<ContactEntity> list, boolean b) {
        Logger.d(TAG, "onSessionMemberUpdate");
        refreshChannelFragment();
        refreshHistoryFragment();
    }

    /**
     * 更新消息未读数显示
     */
    @Override
    public void undateUnreadCount() {
        Logger.d(TAG, "收到新消息 + 1");
        if (channelFragment != null && historyFragment != null) {
            // 刷新预定义组列表界面各个预定义组未读消息数
            channelFragment.undateUnreadCount();
            historyFragment.undateUnreadCount();
        }
    }

    /**
     * 刷新预定义组会话状态
     *
     * @param session
     */
    @Override
    public void refreshTalkStatus(SessionEntity session) {
        if (historyFragment != null) {
            historyFragment.notifyHistoryDataChanged();
        }
        if (channelFragment != null) {
            channelFragment.refreshGroupState();
        }
    }

    /**
     * 推送系统广播消息
     */
    @Override
    public void onPushBroadcastMsg() {
        disPlayBroadcastUpdate();
    }

    /**
     * 显示未读消息提醒
     */
    public void displayUnread(boolean isDisplay) {
//        if (isDisplay) {
//            rbHistory.setTipOn(true);
//        } else {
//            rbHistory.setTipOn(false);
//        }
    }

}

