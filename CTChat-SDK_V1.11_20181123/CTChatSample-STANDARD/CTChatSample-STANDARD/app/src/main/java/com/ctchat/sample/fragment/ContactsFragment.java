package com.ctchat.sample.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ctchat.sample.ContactDetailActivity;
import com.ctchat.sample.IntercomActivity;
import com.ctchat.sample.MainActivity;
import com.ctchat.sample.R;
import com.ctchat.sample.adapter.AllContactsAdapter;
import com.ctchat.sample.adapter.SimpleTreeAdapter;
import com.ctchat.sample.application.WeApplication;
import com.ctchat.sample.presenter.ContactsPresent;
import com.ctchat.sample.presenter.ContactsPresentImpl;
import com.ctchat.sample.tool.incoming.SessionInitiationMan;
import com.ctchat.sample.tool.treelist.FileBean;
import com.ctchat.sample.tool.treelist.Node;
import com.ctchat.sample.tool.treelist.TreeListViewAdapter;
import com.ctchat.sample.util.NetUtil;
import com.ctchat.sample.util.Util;
import com.ctchat.sample.view.ContactsView;
import com.ctchat.sample.widget.PullToRefreshView;
import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sdk.basemodule.util.ToastUtil;
import com.ctchat.sdk.ptt.tool.contact.ContactManager;
import com.ctchat.sdk.ptt.tool.entity.CompanyEntity;
import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;
import com.ctchat.sdk.ptt.tool.entity.UserInfo;
import com.ctchat.sdk.ptt.tool.intercom.IntercomManager;
import com.ctchat.sdk.ptt.tool.userinfo.UserInfoManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class ContactsFragment extends BaseFragment implements ContactsView, AllContactsAdapter.ContactCheckListener, View.OnClickListener, SimpleTreeAdapter.TreeViewCheckListener {
    private static final String TAG = "ContactsFragment";
    private Context mContext;
    private View view;
    private List<FileBean> treeContactsList;  // 树形列表的list
    private List<ContactEntity> loadContactList;
    private ListView lvAllContacts; // 普通列表的listView
    private ListView lvTreeContacts; // 树形列表的listView
    private AllContactsAdapter allContactsAdapter; // 普通列表的adapter
    private SimpleTreeAdapter treeAdapter; // 树形列表的adapter
    private TextView toggleButton; // 切换列表开关
    private PullToRefreshView allContactsPull; // 普通列表的刷新
    private PullToRefreshView treeListContactsPull; // 树形列表的刷新
    private ContactsPresent present;
    private int TYPE_OF_USER = 2;
    private int TYPE_OF_GROUP = 1;
    private PopupWindow popupSelect;
    private LinearLayout startIntercomLayout;
    private LinearLayout immediateMsgLayout;
    private LinearLayout cancelSelectLayout;
    private SessionEntity sessionEntity;
    private List<ContactEntity> selectContacts;
    private List<CompanyEntity> loadCompanyList;

    // 标志位，确定是所有联系人列表还是树形列表
    private int viewFlag;
    private static final int NO_SELECTOR = 0;
    private static final int SELECTOR_FROM_NORMAL_LIST = 1;
    private static final int SELECTOR_FROM_TREE_LIST = 2;
    private static final int STOP_REFRESH = 0xa1;
    private static final int UPDATE_LISTVIEW = 0xa2;
    private static final int REFRESH_DATA_COMPLETE = 0xa3;
    private static final int REFRESH_LISTVIEW = 0xa4;
    private static final int TIME_OF_REFRESH = 12 * 1000;
    private TextView tvLoading;
    private boolean isChecked = false;
    private MainActivity mainActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        initData();
        initSelectContact();
        Logger.d(TAG, "ContactsFragment onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_contacts, container, false);
        initView(view);
        Logger.d(TAG, "ContactsFragment onCreateView");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.d(TAG, "ContactsFragment onResume");
        present.onResume();
        refreshListView();
    }

    public void refreshListView() {
        mHandler.sendEmptyMessage(UPDATE_LISTVIEW);
    }

    private void updateListView() {
        mHandler.sendEmptyMessage(UPDATE_LISTVIEW);
    }

    private void handleUpdate() {
        List<ContactEntity> updateContacts = ContactManager.INSTANCE.getLocalContactList();
        List<CompanyEntity> updateCompanys = ContactManager.INSTANCE.getLocalCompanyList();
        loadContactList.clear();
        loadCompanyList.clear();
        loadContactList.addAll(updateContacts);
        loadCompanyList.addAll(updateCompanys);
    }


    ContactHandler mHandler = new ContactHandler(this);

    static class ContactHandler extends Handler {
        private WeakReference<ContactsFragment> contactsFragmentWeakReference;

        ContactHandler(ContactsFragment contactsFragment) {
            contactsFragmentWeakReference = new WeakReference<ContactsFragment>(contactsFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STOP_REFRESH:
                    if (contactsFragmentWeakReference.get() != null) {
                        Logger.d(TAG, "handleMessage finishPullToRefresh");
                        contactsFragmentWeakReference.get().finishPullToRefresh();
                    }
                    break;
                case REFRESH_DATA_COMPLETE:
                    if (contactsFragmentWeakReference.get() != null) {
                        contactsFragmentWeakReference.get().handleUpdate();

                        if (contactsFragmentWeakReference.get().loadContactList.size() > 0) {
                            contactsFragmentWeakReference.get().tvLoading.setVisibility(View.GONE);
                        }
                        Logger.d(TAG, "handleMessage REFRESH_DATA_COMPLETE");
                        contactsFragmentWeakReference.get().finishPullToRefresh();
                        contactsFragmentWeakReference.get().listAllContactsData();
                        contactsFragmentWeakReference.get().listTreeContactsData();
                    }
                    break;
                case UPDATE_LISTVIEW:
                    if (contactsFragmentWeakReference.get() != null) {
                        Logger.d(TAG, "handleMessage UPDATE_LISTVIEW");
                        contactsFragmentWeakReference.get().handleUpdate();
                        if (contactsFragmentWeakReference.get().allContactsAdapter != null) {
                            contactsFragmentWeakReference.get().allContactsAdapter.notifyDataSetChanged();
                        }
                        contactsFragmentWeakReference.get().listTreeContactsData();
                    }
                    break;

                case REFRESH_LISTVIEW:
                    if (contactsFragmentWeakReference.get() != null) {

                        Logger.e(TAG, "###### refresh tree contacts");

                        contactsFragmentWeakReference.get().lvTreeContacts.setAdapter(contactsFragmentWeakReference.get().treeAdapter);
                    }
                    break;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Logger.d(TAG, "ContactsFragment onPause");
        present.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Logger.d(TAG, "ContactsFragment onDestroyView");
        view = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d(TAG, "ContactsFragment onDestroy");
    }

    @Override
    public void initData() {

        loadContactList = new ArrayList<>();
        loadCompanyList = new ArrayList<>();
        treeContactsList = new ArrayList<>();
        selectContacts = new ArrayList<>();
        viewFlag = NO_SELECTOR;
        mContext = getActivity();
        present = new ContactsPresentImpl(this);
    }

    @Override
    public void initView(View view) {
        Logger.i(TAG, "initView");
        tvLoading = (TextView) view.findViewById(R.id.tv_contact_loading);
        tvLoading.setVisibility(View.VISIBLE);
        // 普通列表
        lvAllContacts = (ListView) view.findViewById(R.id.lv_all_contacts_list);
        allContactsPull = (PullToRefreshView) view.findViewById(R.id.pv_all_contacts_list);
        allContactsPull.setOnHeaderRefreshListener(new PullToRefreshView.OnHeaderRefreshListener() {
            @Override
            public void onHeaderRefresh(PullToRefreshView view) {
                if (popupSelect != null) {
                    popupSelect.dismiss();
                }
                if (!NetUtil.isNetConnected(mContext)) {
                    allContactsPull.onHeaderRefreshComplete();
                    ToastUtil.longShow(WeApplication.getInstance(), R.string.net_error);
                } else {
                    present.loadContacts();
                    mHandler.sendEmptyMessageDelayed(STOP_REFRESH, TIME_OF_REFRESH);
                }
            }
        });

        // 树形列表
        lvTreeContacts = (ListView) view.findViewById(R.id.lv_tree_contacts_list);
        treeListContactsPull = (PullToRefreshView) view.findViewById(R.id.pv_tree_contacts_list);
        treeListContactsPull.setOnHeaderRefreshListener(new PullToRefreshView.OnHeaderRefreshListener() {
            @Override
            public void onHeaderRefresh(PullToRefreshView view) {
                if (popupSelect != null) {
                    popupSelect.dismiss();
                }
                if (!NetUtil.isNetConnected(mContext)) {
                    treeListContactsPull.onHeaderRefreshComplete();
                    ToastUtil.longShow(WeApplication.getInstance(), R.string.net_error);
                } else {
                    present.loadContacts();
                    mHandler.sendEmptyMessageDelayed(STOP_REFRESH, TIME_OF_REFRESH);
                }
            }
        });

        present.loadContacts();
        // 列表切换按钮
        toggleButton = (TextView) view.findViewById(R.id.tb_right_contacts_icon);
        checkView(isChecked);
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isChecked = !isChecked;
                checkView(isChecked);
            }
        });

    }

    // 展示普通列表
    private void listAllContactsData() {
        Logger.d(TAG, "listAllContactsData");
        allContactsAdapter = new AllContactsAdapter(mContext, loadContactList);
        allContactsAdapter.setInterface(ContactsFragment.this);
        lvAllContacts.setAdapter(allContactsAdapter);
        lvAllContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserInfo userInfo = UserInfoManager.getInstance().getUserInfo(mContext);
                if (!loadContactList.get(position).getMdn().equals(userInfo.getMdn())) {
                    if (popupSelect.isShowing()) {
                        Set<String> selectOptions = allContactsAdapter.getSelectOptions();
                        viewFlag = SELECTOR_FROM_NORMAL_LIST;
                        //进入多选模式
                        multiSelect(selectOptions, loadContactList.get(position).getMdn());
                        updateAddTempGroupEntrance();
                    } else {
                        Intent intent = new Intent(mContext, ContactDetailActivity.class);
                        ContactEntity contact = loadContactList.get(position);
                        intent.putExtra("data", contact);
                        startActivity(intent);
                    }
                }
            }
        });
    }

    private AtomicBoolean isRefreshing = new AtomicBoolean(false);

    // 展示树形列表
    private void listTreeContactsData() {

        if (isRefreshing.get()) {
            Logger.e(TAG, "REfreshing!!!!");
            return;
        }

        isRefreshing.set(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<ContactEntity> updateContacts = ContactManager.INSTANCE.getLocalContactList();
                List<CompanyEntity> updateCompanys = ContactManager.INSTANCE.getLocalCompanyList();
                treeContactsList.clear();
                treeContactsList.addAll(getFileBeanList(updateCompanys, updateContacts));

                try {
                    treeAdapter = new SimpleTreeAdapter(lvTreeContacts, mContext, treeContactsList, 10);

                    treeAdapter.setInterface(ContactsFragment.this);
                    treeAdapter.setOnTreeNodeClickListener(new TreeListViewAdapter.OnTreeNodeClickListener() {
                        @Override
                        public void onClick(Node node, View view, int position) {
                            if (!TextUtils.equals(node.getId(), UserInfoManager.getInstance().getUserInfo(mContext).getMdn())) {
                                if (popupSelect.isShowing()) {
                                    Set<String> selectOptions = treeAdapter.getSelectOptions();
                                    viewFlag = SELECTOR_FROM_TREE_LIST;
                                    //进入多选模式ContactMa
                                    if (node.isLeaf()) {
                                        multiSelect(selectOptions, node.getId());
                                        updateAddTempGroupEntrance();
                                    }
                                } else if (node.isLeaf()) {
                                    ContactEntity contact = node.getContactEntity();
                                    if (contact != null) {
                                        Intent intent = new Intent(mContext, ContactDetailActivity.class);
                                        intent.putExtra("data", contact);
                                        startActivity(intent);
                                    }
                                }
                            }
                        }
                    });

                    mHandler.sendEmptyMessage(REFRESH_LISTVIEW);

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                isRefreshing.set(false);
            }
        }).start();
    }

    /**
     * 多选模式
     *
     * @param selectOptions
     * @param mdn
     */
    public void multiSelect(Set<String> selectOptions, String mdn) {
        //判断是否达到邀请上限
        if (selectOptions.size() < Util.DIALOG_SESSION_MENBER_UPPER) {
            if (selectOptions.contains(mdn)) {
                selectOptions.remove(mdn);
                if (selectOptions.size() == 0) {
                    hidePopupWindow();
                }
            } else {
                if (mdn != null) {
                    selectOptions.add(mdn);
                }
            }
        } else {
            if (selectOptions.contains(mdn)) {
                selectOptions.remove(mdn);
            } else {
                ToastUtil.shortShow(WeApplication.getInstance(), R.string.invite_contact_number_upper);
            }
        }

        switch (viewFlag) {
            case SELECTOR_FROM_NORMAL_LIST:
                allContactsAdapter.notifyDataSetChanged();
                break;
            case SELECTOR_FROM_TREE_LIST:
                treeAdapter.notifyDataSetChanged();
                break;
        }
    }

    // 切换展示列表
    private void checkView(boolean status) {
        if (status) {
            if (isRefreshing.get()) {
                //加载中
                ToastUtil.shortShow(WeApplication.getInstance(), R.string.tree_contacts_loading);
            }
            allContactsPull.setVisibility(View.GONE);
            treeListContactsPull.setVisibility(View.VISIBLE);
            toggleButton.setText("树形");
        } else {
            treeListContactsPull.setVisibility(View.GONE);
            allContactsPull.setVisibility(View.VISIBLE);
            toggleButton.setText("普通");
        }
        hidePopupWindow();
    }

    @Override
    public void netError() {
        if (allContactsPull.isShown()) {
            allContactsPull.onHeaderRefreshComplete();
        } else if (treeListContactsPull.isShown()) {
            treeListContactsPull.onHeaderRefreshComplete();
        }
    }

    @Override
    public void onContactPresence() {
        Logger.i(TAG, "onContactPresence");
        allContactsAdapter.notifyDataSetChanged();
        treeAdapter.notifyDataSetChanged();
        mainActivity.refreshChannelFragment();
    }

    @Override
    public void onContactListPresence() {
        allContactsAdapter.notifyDataSetChanged();
        treeAdapter.notifyDataSetChanged();
        mainActivity.refreshChannelFragment();
    }

    @Override
    public void loadContactsSuccess(List<ContactEntity> contactsList, List<CompanyEntity> companyList) {
        Logger.i(TAG, "loadContactsSuccess");
        mHandler.removeMessages(STOP_REFRESH);
        loadData(contactsList, companyList);
    }

    private void loadData(final List<ContactEntity> contactsList, final List<CompanyEntity> companyList) {
        mHandler.sendEmptyMessage(REFRESH_DATA_COMPLETE);
    }


    @Override
    public void onUpdateContactsSuccess() {
        Logger.i(TAG, "onUpdateContactsSuccess");
        updateListView();
    }

    @Override
    public void onUpdateContactError() {
        Logger.i(TAG, "onUpdateContactError");
        updateListView();
    }

    public List<FileBean> getFileBeanList(List<CompanyEntity> companyList, List<ContactEntity> contactsList) {
        List<FileBean> fileBeans = new ArrayList<>();
        for (ContactEntity contact : contactsList) {
            String name = contact.getDisplayName();
            String sId = contact.getMdn();
            String cId = contact.getCompanyId();
            String mdn = contact.getMdn();
            String pID = contact.getMdn();
            fileBeans.add(new FileBean(sId, cId, name, mdn, TYPE_OF_USER, pID, contact));
        }
        for (CompanyEntity company : companyList) {
            String name = company.getCompanyName();
            String sId = company.getCompanyId();
            String pId = null;
            if (company.getParentCompanyId() != null) {
                pId = company.getParentCompanyId();
            } else {
                pId = "0";
            }
            fileBeans.add(new FileBean(sId, pId, name, "", TYPE_OF_GROUP, "", null));
        }
        return fileBeans;
    }

    private void finishPullToRefresh() {
        if (allContactsPull != null) {
            allContactsPull.onHeaderRefreshComplete();
        }
        if (treeListContactsPull != null) {
            treeListContactsPull.onHeaderRefreshComplete();
        }
    }


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
    public void ContactCheckListener(Set<String> selectOptions, String mdn) {
        viewFlag = SELECTOR_FROM_NORMAL_LIST;

        if (!TextUtils.equals(mdn, UserInfoManager.getInstance().getUserInfo(mContext).getMdn())) {
            if (!popupSelect.isShowing()) {
                popupSelect.showAtLocation(view, Gravity.BOTTOM, 0, 0);
            }
            multiSelect(selectOptions, mdn);
            updateAddTempGroupEntrance();
        }

    }

    /**
     * 更新新建临时组功能入口
     */
    private void updateAddTempGroupEntrance() {
        if (viewFlag == SELECTOR_FROM_NORMAL_LIST) { //所有联系人列表时点击
            selectContacts = getSelectedContacts();
        } else {
            selectContacts = getSelectTreeContacts();
        }
    }

    @Override
    public void treeViewCheckClickEvent(Set<String> selectOptions, String mdn) {
        viewFlag = SELECTOR_FROM_TREE_LIST;

        if (!TextUtils.equals(mdn, UserInfoManager.getInstance().getUserInfo(mContext).getMdn())) {
            if (!popupSelect.isShowing()) {
                popupSelect.showAtLocation(view, Gravity.BOTTOM, 0, 0);
            }
            multiSelect(selectOptions, mdn);
            updateAddTempGroupEntrance();
        }
    }

    // 根据联系人mdn查找联系人列表
    private List<ContactEntity> getSelectTreeContacts() {
        List<ContactEntity> list = new ArrayList<ContactEntity>();
        if (treeAdapter.getSelectOptions().size() > 0) {
            Iterator<String> it = treeAdapter.getSelectOptions().iterator();
            while (it.hasNext()) {
                String mdm = it.next();
                ContactEntity contact = ContactManager.INSTANCE.getContactByMdn(mdm);
                if (contact != null) {
                    list.add(contact);
                }
            }
        }
        return list;
    }

    public void hidePopupWindow() {
        if (popupSelect != null && popupSelect.isShowing()) {
            allContactsAdapter.clearSelectOptions();
            allContactsAdapter.clearAnimSet();
            treeAdapter.clearSelectOptions();
            treeAdapter.clearAnimSet();
            popupSelect.dismiss();
            allContactsAdapter.notifyDataSetChanged();
            treeAdapter.notifyDataSetChanged();
        }
    }

    private List<ContactEntity> getSelectedContacts() {
        List<ContactEntity> list = new ArrayList<>();
        if (allContactsAdapter.getSelectOptions() != null) {
            for (String mdm : allContactsAdapter.getSelectOptions()) {
                ContactEntity contact = ContactManager.INSTANCE.getContactByMdn(mdm);
                if (contact != null) {
                    list.add(contact);
                }
            }
        }
        return list;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            hidePopupWindow();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_contact_bottom_start_talk: // 发起对讲
                initiationSession(SessionInitiationMan.INITIATION_PTT
                        , SessionInitiationMan.SESSION_CONNECTION);
                hidePopupWindow();
                break;
            case R.id.ll_contact_bottom_immediate_message: // 即时消息
                initiationSession(SessionInitiationMan.INITIATION_IM
                        , SessionInitiationMan.SESSION_UNCONNECTION);
                hidePopupWindow();
                break;
            case R.id.ll_contact_bottom_cancel_select: // 取消选择
                hidePopupWindow();
                break;
        }
    }

    /**
     * 创建临时组
     */
    private void createTempGroup() {
        if (viewFlag == SELECTOR_FROM_NORMAL_LIST) { //所有联系人列表时点击
            selectContacts = getSelectedContacts();
        } else { //树形列表时点击
            selectContacts = getSelectTreeContacts();
        }
        sessionEntity = IntercomManager.INSTANCE.getSessionEntityByContacts(selectContacts);
    }


    /**
     * 跳转到ptt呼叫页面 或者 im即时消息页面
     *
     * @param initiationMode
     * @param connectionStatus
     */
    private void initiationSession(int initiationMode, int connectionStatus) {
        if (viewFlag == SELECTOR_FROM_NORMAL_LIST) { //所有联系人列表时点击
            selectContacts = getSelectedContacts();
        } else { //树形列表时点击
            selectContacts = getSelectTreeContacts();
        }
        sessionEntity = IntercomManager.INSTANCE.getSessionEntityByContacts(selectContacts);
        if (sessionEntity != null) {
            Intent intent = new Intent(mContext, IntercomActivity.class);
            SessionInitiationMan.SessionInitiation sessionInitiation = new SessionInitiationMan.SessionInitiation();
            sessionInitiation.sessionCode = sessionEntity.getSessionId();
            sessionInitiation.initializationMode = initiationMode;
            sessionInitiation.connectionStatus = connectionStatus;
            intent.putExtra(SessionInitiationMan.INTENT_DATA_KEY, sessionInitiation);
            mContext.startActivity(intent);
        }
    }
}
