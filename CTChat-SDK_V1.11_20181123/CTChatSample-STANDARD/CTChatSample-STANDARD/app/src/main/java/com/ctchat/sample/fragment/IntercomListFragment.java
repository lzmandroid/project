package com.ctchat.sample.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;


import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sdk.basemodule.util.ToastUtil;
import com.ctchat.sample.widget.CustomHintDialog;
import com.ctchat.sample.R;
import com.ctchat.sdk.ptt.tool.channel.ChannelManager;
import com.ctchat.sdk.ptt.tool.entity.UserInfo;
import com.ctchat.sdk.ptt.tool.entity.ChannelEntity;
import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;
import com.ctchat.sdk.ptt.tool.intercom.IntercomManager;
import com.ctchat.sdk.ptt.tool.userinfo.UserInfoManager;
import com.ctchat.sample.IntercomActivity;
import com.ctchat.sample.adapter.IntercomListAdapter;
import com.ctchat.sample.application.WeApplication;
import com.ctchat.sample.presenter.IntercomListPresent;
import com.ctchat.sample.presenter.IntercomListPresentImpl;
import com.ctchat.sample.tool.incoming.SessionInitiationMan;
import com.ctchat.sample.util.Util;
import com.ctchat.sample.view.IntercomListView;
import com.ctchat.sample.widget.MemberCountView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 群组对讲成员列表
 */
public class IntercomListFragment extends BaseFragment implements IntercomListView, View.OnClickListener, IntercomListAdapter.InviteBackgroundListener {
    private static final String TAG = "IntercomListFragment";
    private View view;
    private Context mContext;

    private ListView lvGroupList;
    private IntercomListPresent mPresent;

    private IntercomListAdapter mAdapter;
    private SessionEntity sessionEntity;
    private SessionEntity choiceSession;
    private IntercomActivity intercomActivity;
    private PopupWindow popupSelect;
    private LinearLayout startIntercomLayout;
    private LinearLayout immediateMsgLayout;
    private LinearLayout cancelSelectLayout;
    private LinearLayout llMemberList;
    private MemberCountView member_count;
    private Button alert_online;


    private long clickTime = 0;
    private static final long INTERVAL_TIME = 1 * 60 * 1000;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        initSelectContact();
        Logger.d(TAG, "IntercomListFragment onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_intercom_list, container, false);
        mPresent = new IntercomListPresentImpl(this);
        intercomActivity = (IntercomActivity) getActivity();
        sessionEntity = intercomActivity.getSessionEntity();
        initView(view);
        initData();
        Logger.d(TAG, "IntercomListFragment onCreateView");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.d(TAG, "IntercomListFragment onResume");
        mPresent.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresent.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.removeInviteBackgroundListener();
        }
    }

    @Override
    public void initData() {
        if (sessionEntity.getSessionType() == SessionEntity.TYPE_GROUP) {
            mPresent.getChannelMember(intercomActivity.getSessionEntity().getChannel());
        }
        updateMemberList();
    }

    private void updateMemberList() {
        SessionEntity.SessionStateCount sessionStateCount = IntercomManager.INSTANCE.getSessionStateCount(sessionEntity);
        Logger.d(TAG, "Chanel State Count :" + sessionStateCount.getChatOnlineCount() + "  " + sessionStateCount.getChatOfflineCount() + " " + sessionStateCount.getAllmemberCount());
        member_count.getTvLeftMemberCount().setText(sessionStateCount.getChatOnlineCount() + "");
        member_count.getTvMiddleMemberCount().setText(sessionStateCount.getChatOfflineCount() + "");
        member_count.getTvRightMemberCount().setText(sessionStateCount.getAllmemberCount() + "");
    }

    @Override
    public void initView(View view) {
        lvGroupList = (ListView) view.findViewById(R.id.lv_intercom_group_list);
        llMemberList = (LinearLayout) view.findViewById(R.id.ll_member_count_container);
        member_count = (MemberCountView) view.findViewById(R.id.member_count);
        alert_online = (Button) view.findViewById(R.id.alert_online);

//        if (sessionEntity.getSessionType() == SessionEntity.TYPE_GROUP){
//            alert_online.setVisibility(View.VISIBLE);
//        }else {
            alert_online.setVisibility(View.GONE);
//        }

        alert_online.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupCallOnline();
            }
        });

        View footerView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_view_popup_window, null, true);
        lvGroupList.addFooterView(footerView, null, false);
        lvGroupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                UserInfo userInfo = UserInfoManager.getInstance().getUserInfo(intercomActivity);
                if (sessionEntity.getSessionType() == SessionEntity.TYPE_GROUP && !sessionEntity.getChannel().getMemberAll().get(position).getMdn().equals(userInfo.getMdn())) {
                    String mdn = sessionEntity.getChannel().getMemberAll().get(position).getMdn();
                    Set<String> selectOptions = mAdapter.getSelectOptions();
                    //判断邀请人数是否达到上限
                    if (selectOptions.size() < Util.DIALOG_SESSION_MENBER_UPPER) {
                        if (selectOptions.contains(mdn)) {
                            selectOptions.remove(mdn);
                            if (selectOptions.size() == 0) {
                                hidePopupWindow();
                            }
                            mAdapter.notifyDataSetChanged();
                        } else {
                            popupSelect.showAtLocation(view, Gravity.BOTTOM, 0, 0);
                            selectOptions.add(mdn);
                            mAdapter.notifyDataSetChanged();
                        }
                    } else {
                        if (selectOptions.contains(mdn)) {
                            selectOptions.remove(mdn);
                            mAdapter.notifyDataSetChanged();
                        } else {
                            ToastUtil.shortShow(WeApplication.getInstance(), R.string.invite_contact_number_upper);
                        }
                    }
                }
            }
        });

        mAdapter = new IntercomListAdapter(intercomActivity);
        mAdapter.setInviteBackgroundListener(IntercomListFragment.this);
        mAdapter.setCurrentSession(sessionEntity);
        lvGroupList.setAdapter(mAdapter);
    }


    // 选中联系人头像
    private void initSelectContact() {
        View view;
        view = LayoutInflater.from(mContext).inflate(R.layout.popwindow_contact_bottom_bar, null);
        popupSelect = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupSelect.setOutsideTouchable(true);
        popupSelect.setAnimationStyle(R.style.popupwindow_contact_list_anim_style);
        popupSelect.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        startIntercomLayout = (LinearLayout) view.findViewById(R.id.ll_contact_bottom_start_talk);
        immediateMsgLayout = (LinearLayout) view.findViewById(R.id.ll_contact_bottom_immediate_message);
        cancelSelectLayout = (LinearLayout) view.findViewById(R.id.ll_contact_bottom_cancel_select);
        startIntercomLayout.setOnClickListener(this);
        immediateMsgLayout.setOnClickListener(this);
        cancelSelectLayout.setOnClickListener(this);

        popupSelect.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_contact_bottom_start_talk:
                choiceSession = MatchSessionByContacts();
                Intent ptt_intent = new Intent(getActivity(), IntercomActivity.class);
                SessionInitiationMan.SessionInitiation sessionInitiation = new SessionInitiationMan.SessionInitiation();
                sessionInitiation.sessionCode = choiceSession.getSessionId();
                sessionInitiation.initializationMode = SessionInitiationMan.INITIATION_PTT;
                sessionInitiation.connectionStatus = SessionInitiationMan.SESSION_CONNECTION;
                ptt_intent.putExtra(SessionInitiationMan.INTENT_DATA_KEY, sessionInitiation);
                getActivity().finish();
                startActivity(ptt_intent);
                hidePopupWindow();
                break;
            case R.id.ll_contact_bottom_immediate_message:
                choiceSession = MatchSessionByContacts();
                Intent im_intent = new Intent(getActivity(), IntercomActivity.class);
                SessionInitiationMan.SessionInitiation initiation = new SessionInitiationMan.SessionInitiation();
                initiation.sessionCode = choiceSession.getSessionId();
                initiation.initializationMode = SessionInitiationMan.INITIATION_IM;
                initiation.connectionStatus = SessionInitiationMan.SESSION_UNCONNECTION;
                im_intent.putExtra(SessionInitiationMan.INTENT_DATA_KEY, initiation);
                getActivity().finish();
                startActivity(im_intent);
                hidePopupWindow();
                break;
            case R.id.ll_contact_bottom_cancel_select:
                hidePopupWindow();
                break;
            default:break;
        }
    }

    /**
     * 群呼上线功能
     */
    public void groupCallOnline() {
        Logger.i(TAG,"Group Alert Online");
        if (sessionEntity.getSessionStatus() == SessionEntity.SESSION_STATE_DIALOG) {
            long dividerTime = System.currentTimeMillis() - clickTime;
            if (dividerTime > INTERVAL_TIME) {
                if (sessionEntity != null) {
                    ChannelManager.INSTANCE.channelAlertOnline(sessionEntity.getChannel().getId());
                    clickTime = System.currentTimeMillis();
                }
            } else {
                long waitSeconds = (INTERVAL_TIME - dividerTime) / 1000;
                String waitTime = getResources().getString(R.string.group_call_already_sent) + waitSeconds + getResources().getString(R.string.wait_repeat);
                ToastUtil.longShow(mContext, waitTime);
            }
        } else {
            ToastUtil.shortShow(mContext, R.string.not_in_session_channel);
        }
    }

    /**
     * 获取 SessionEntity
     */
    private SessionEntity MatchSessionByContacts() {
        List<ContactEntity> list = new ArrayList<>();
        for (String mdn : mAdapter.getSelectOptions()) {
            for (ContactEntity contact : sessionEntity.getChannel().getMemberAll()) {
                if (contact.getMdn().equals(mdn)) {
                    list.add(contact);
                    break;
                }
            }
        }
        return IntercomManager.INSTANCE.getSessionEntityByContacts(list);
    }

    public void notifyDataSetChanged() {
        updateMemberList();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void onGetChannelMemberList(List<ContactEntity> list) {
        Logger.d(TAG, "=====onGetChannelMemberList=====");
        notifyDataSetChanged();
    }

    @Override
    public void onContactPresence() {
        Logger.d(TAG, "======onContactPresence======");
        notifyDataSetChanged();
    }

    @Override
    public void onContactListPresence() {
        Logger.d(TAG, "======onContactListPresence======");
        notifyDataSetChanged();
    }

    @Override
    public void onNotifyChannelMemberAdd(ChannelEntity channel, List<ContactEntity> contacts) {
        notifyDataSetChanged();
    }

    @Override
    public void onNotifyChannelMemberDelete(ChannelEntity channel, List<ContactEntity> contacts) {
        notifyDataSetChanged();
    }

    @Override
    public void onNotifyChannelMemberUpdate(ChannelEntity channel, List<ContactEntity> contacts) {
        notifyDataSetChanged();
    }

    /**
     * 自己被移除预定义组时通知
     *
     * @param channel
     */
    @Override
    public void onNotifyChannelPersonalDelete(ChannelEntity channel) {
        IntercomActivity mActivity = (IntercomActivity) getActivity();
        if (mActivity != null && sessionEntity.getSessionId().equals(channel.getId())) {
            mActivity.finish();
        }
    }

    public void hidePopupWindow() {
        if (popupSelect != null && popupSelect.isShowing()) {
            mAdapter.clearSelectionOptions();
            mAdapter.clearAnimSet();
            popupSelect.dismiss();
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onInviteBackground(ContactEntity contactEntity) {
        Logger.d(TAG, "onInviteBackground");
        showDispatcherCallDialog(contactEntity);
    }

    private void showDispatcherCallDialog(final ContactEntity contactEntity) {
        CustomHintDialog.Builder dispatcherDialog = new CustomHintDialog.Builder(mContext);
        dispatcherDialog.setTitle(R.string.hint);
        dispatcherDialog.setMessage(R.string.confirm_invite_session);
        dispatcherDialog.setBackButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dispatcherDialog.setConfirmButton(R.string.sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (sessionEntity != null) {
                    List<ContactEntity> list = new ArrayList<>();
                    list.add(contactEntity);
                    IntercomManager.INSTANCE.inviteSessionCall(sessionEntity.getSessionId(), list);
                    Logger.d(TAG, "showDispatcherCallDialog jionSessionCall sessionCode = " + sessionEntity.getSessionId());
                }
            }
        });
        dispatcherDialog.create().show();
    }

}
