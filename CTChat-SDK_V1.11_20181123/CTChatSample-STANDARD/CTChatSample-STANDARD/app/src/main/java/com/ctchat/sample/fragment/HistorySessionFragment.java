package com.ctchat.sample.fragment;


import android.content.Context;
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
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;


import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sdk.basemodule.util.ToastUtil;
import com.ctchat.sample.R;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;
import com.ctchat.sdk.ptt.tool.intercom.IntercomManager;
import com.ctchat.sample.IntercomActivity;
import com.ctchat.sample.MainActivity;

import com.ctchat.sample.adapter.HistorySessionAdapter;
import com.ctchat.sample.application.WeApplication;
import com.ctchat.sample.tool.incoming.SessionInitiationMan;
import com.ctchat.sample.util.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 历史会话
 */
public class HistorySessionFragment extends BaseFragment implements HistorySessionAdapter.CurrentSessionListViewCheckListener {
    private static final String TAG = "HistorySessionFragment";
    private View view;
    private Context mContext;
    private MainActivity mainActivity;
    private ListView lvHistorySession, lvHistorySubtag;
    private CheckBox cbSubTag;
    private TextView tvSubTitle;

    private PopupWindow popupSelect;

    private List<SessionEntity> sessionRecords = new ArrayList<>();
    private List<SessionEntity> sessionRecordsSub = new ArrayList<>();//全部记录

    private String[] subTag = new String[4];

    private int subRecordsType = Util.SUB_RECORDS_ALL;

    private HistorySessionAdapter mHistoryAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mainActivity = (MainActivity) getActivity();
        Logger.d(TAG,"HistorySessionFragment onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_history, container, false);
        initView(view);
        initData();
        Logger.d(TAG,"HistorySessionFragment onCreateView");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.d(TAG,"HistorySessionFragment onResume");

        notifyHistoryDataChanged();

    }

    @Override
    public void initData() {
        subTag[0] = getString(R.string.history_subtag_all);
        subTag[1] = getString(R.string.history_subtag_calling);
        subTag[2] = getString(R.string.history_subtag_unanswer);
        subTag[3] = getString(R.string.history_subtag_over);
        uploadSessionRecords();
        setAdapter();
    }

    private void setAdapter() {
        Logger.d(TAG, "mHistoryAdapter create");
        mHistoryAdapter = new HistorySessionAdapter(mContext, sessionRecordsSub, lvHistorySession);
        mHistoryAdapter.setInterface(this);
        lvHistorySession.setAdapter(mHistoryAdapter);
        ArrayAdapter subtagAdapter = new ArrayAdapter(getActivity(), R.layout.item_history_subtag, R.id.item_history_subtag_name, subTag);
        lvHistorySubtag.setAdapter(subtagAdapter);
    }

    @Override
    public void initView(View view) {
        cbSubTag = (CheckBox) view.findViewById(R.id.cb_history_switch);
        tvSubTitle = (TextView) view.findViewById(R.id.tv_history_tag);
        lvHistorySubtag = (ListView) view.findViewById(R.id.lv_history_subtag);
        lvHistorySession = (ListView) view.findViewById(R.id.lv_history_session);
        lvHistorySession.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SessionEntity sessionEntity = sessionRecordsSub.get(position);
                if (popupSelect.isShowing()) {
                    if (sessionEntity.getSessionStatus() != SessionEntity.SESSION_STATE_DIALOG) {
                        Set<String> selectOptions = mHistoryAdapter.getSelectOptions();
                        multiSelect(selectOptions, sessionEntity.getSessionId());
                    } else {
                        ToastUtil.shortShow(WeApplication.getInstance(), R.string.select_no_type);
                    }
                } else {
                    Intent intent = new Intent(mContext, IntercomActivity.class);
                    SessionInitiationMan.SessionInitiation sessionInitiation = new SessionInitiationMan.SessionInitiation();
                    sessionInitiation.sessionCode = sessionEntity.getSessionId();
                    sessionInitiation.connectionStatus = SessionInitiationMan.SESSION_UNCONNECTION;
                    sessionInitiation.initializationMode = SessionInitiationMan.INITIATION_IM;
                    intent.putExtra(SessionInitiationMan.INTENT_DATA_KEY, sessionInitiation);
                    startActivity(intent);
                }
            }
        });

        cbSubTag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                lvHistorySubtag.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

        lvHistorySubtag.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                subRecordsType = position;
                tvSubTitle.setText(subTag[subRecordsType]);
                cbSubTag.setChecked(false);
                notifyHistoryDataChanged();
                lvHistorySubtag.setVisibility(View.GONE);
            }
        });

        initPopupWindow();
    }

    /**
     * 更新sessionRecords
     */
    private void uploadSessionRecords() {
        sessionRecords.clear();
        sessionRecords = IntercomManager.INSTANCE.getSessionEntityList();

        // 控制主页小红点显示
        displayUnread(sessionRecords);

        sessionRecordsSub.clear();
        sessionRecordsSub.addAll(sessionRecords);
    }

    /**
     * 主页显示未读消息提醒
     */
    private void displayUnread(List<SessionEntity> sessionRecords) {
        if (mainActivity != null) {
            for (SessionEntity sessionEntity : sessionRecords) {
                if (sessionEntity.getMessageUnreadCount() > 0) {
                    mainActivity.displayUnread(true);
                    return;
                }
            }
            mainActivity.displayUnread(false);
        }
    }

    /**
     * 筛选sessionRecords子集
     */
    private void selectSubSessionRecords() {
        switch (subRecordsType) {
            case Util.SUB_RECORDS_ALL:
                sessionRecordsSub.clear();
                sessionRecordsSub.addAll(sessionRecords);
                break;
            case Util.SUB_RECORDS_CALLING:
                selectCallingSubRecords();
                break;
            case Util.SUB_RECORDS_UNANSWER:
                selectUnanswerSubRecords();
                break;
            case Util.SUB_RECORDS_OVER:
                selectOverSubRecords();
                break;
        }
        if (mHistoryAdapter != null) {
            mHistoryAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 提取已结束的会话记录
     */
    private void selectOverSubRecords() {
        sessionRecordsSub.clear();
        Iterator<SessionEntity> iterator = sessionRecords.iterator();
        while (iterator.hasNext()) {
            SessionEntity session = iterator.next();
            if (session.getSessionStatus() != SessionEntity.SESSION_STATE_DIALOG) {
                sessionRecordsSub.add(session);
            }
        }
    }

    /**
     * 提取未接听的会话记录
     */
    private void selectUnanswerSubRecords() {
        sessionRecordsSub.clear();
        Iterator<SessionEntity> iterator = sessionRecords.iterator();
        while (iterator.hasNext()) {
            SessionEntity session = iterator.next();
            if (session.getLastMessage() != null) {
                if (session.getSessionType() == SessionEntity.TYPE_DIALOG && session.getLastMessage().getBody().equals(getString(R.string.talk_call_state_missed_call))) {
                    sessionRecordsSub.add(session);
                }
            }
        }
    }

    /**
     * 提取正在通话的会话记录
     * <p/>
     * 对有序集合sessionRecords中正在
     * 通话的会话进行提取，当取到一个
     * 不是正在通话的项时就可以确定循
     * 环终止；
     * （注意：sessionRecords必须是正在通话子项置顶的有序集合）
     */
    private void selectCallingSubRecords() {
        sessionRecordsSub.clear();
        Iterator<SessionEntity> iterator = sessionRecords.iterator();
        while (iterator.hasNext()) {
            SessionEntity session = iterator.next();
            if (session.getSessionStatus() == SessionEntity.SESSION_STATE_DIALOG) {
                sessionRecordsSub.add(session);
            } else {
                break;
            }
        }
    }

    /**
     * 挂断CurrentSession事件监听
     */
    @Override
    public void CurrentSessionListViewCheckListener(SessionEntity sessionEntity) {
        IntercomManager.INSTANCE.stopSessionCall(sessionEntity);
        notifyHistoryDataChanged();
    }

    /**
     * 会话选择监听
     * @param selectOptions
     * @param sessionCode
     */
    @Override
    public void SessionSelectListener(Set<String> selectOptions, String sessionCode) {
        if (!popupSelect.isShowing()) {
            popupSelect.showAtLocation(view, Gravity.BOTTOM, 0, 0);
        }
        multiSelect(selectOptions, sessionCode);
    }

    /**
     * 切换预定义组锁定状态
     * @param sessionEntity
     */
    @Override
    public void switchSessionLock(SessionEntity sessionEntity) {
        IntercomManager.INSTANCE.lockGroupCall(sessionEntity, !sessionEntity.isLocked());
        String lockInfo = sessionEntity.getChannelName() + " ";
        //显示锁定状态
        if (sessionEntity.isLocked()) {
            lockInfo += mContext.getString(R.string.intercom_channel_manager_lock_enable);
        } else {
            lockInfo += mContext.getString(R.string.intercom_channel_manager_lock_disable);
        }
        ToastUtil.shortShow(WeApplication.getInstance(), lockInfo);
        notifyHistoryDataChanged();
        MainActivity activity = (MainActivity) getActivity();
        activity.refreshChannelFragment();
    }

    public void notifyHistoryDataChanged(){
        uploadSessionRecords();
        selectSubSessionRecords();
    }

    /**
     * 更新消息未读数显示
     */
    public void undateUnreadCount() {
        Logger.d(TAG, "更新数量+1");
        // TODO: 2016/12/14  更新历史会话消息未读数显示

        notifyHistoryDataChanged();
    }

    /**
     * 删除会话记录
     */
    private void deleteSession() {
        for (String sessionId : mHistoryAdapter.getSelectOptions()) {
            SessionEntity session = IntercomManager.INSTANCE.getSessionEntityBySessionId(sessionId);
            IntercomManager.INSTANCE.removeSessionEntity(session);
        }
        hidePopupWindow();
    }

    /**
     * 多选模式
     * @param selectOptions
     * @param sessionCode
     */
    private void multiSelect(Set<String> selectOptions, String sessionCode) {
        if (selectOptions.contains(sessionCode)) {
            selectOptions.remove(sessionCode);
            if (selectOptions.size() == 0) {
                hidePopupWindow();
            }
        } else {
            if (sessionCode != null) {
                selectOptions.add(sessionCode);
            }
        }

        notifyHistoryDataChanged();
    }

    /**
     * 初始化popupwindow
     */
    private void initPopupWindow() {
        View view;
        final LinearLayout deleteNoticeLayout,cancelSelectLayout;
        view = LayoutInflater.from(getActivity()).inflate(R.layout.popwindow_broadcast_bottom_bar, null);
        popupSelect = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupSelect.setOutsideTouchable(true);
        popupSelect.setAnimationStyle(R.style.popupwindow_contact_list_anim_style);
        popupSelect.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        deleteNoticeLayout = (LinearLayout) view.findViewById(R.id.rl_broadcast_bottom_delete);
        cancelSelectLayout = (LinearLayout) view.findViewById(R.id.rl_broadcast_bottom_cancel_select);
        deleteNoticeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSession();
            }
        });
        cancelSelectLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePopupWindow();
            }
        });

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

    public void hidePopupWindow() {
        if (popupSelect != null && popupSelect.isShowing()) {
            mHistoryAdapter.clearSelectOptions();
            mHistoryAdapter.clearAnimSet();
            popupSelect.dismiss();
            notifyHistoryDataChanged();
        }
    }
}
