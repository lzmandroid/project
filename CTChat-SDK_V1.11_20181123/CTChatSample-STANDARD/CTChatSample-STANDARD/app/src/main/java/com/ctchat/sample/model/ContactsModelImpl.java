package com.ctchat.sample.model;

import android.content.Context;


import com.ctchat.sdk.ptt.tool.contact.ContactDataListener;
import com.ctchat.sdk.ptt.tool.contact.ContactManager;
import com.ctchat.sdk.ptt.tool.contact.ContactPresenceListener;
import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sdk.ptt.tool.entity.CompanyEntity;
import com.ctchat.sample.application.WeApplication;
import com.ctchat.sample.io.DispatcherSPManager;
import com.ctchat.sample.presenter.ContactsListener;

import java.util.HashMap;
import java.util.List;

/**
 *
 */
public class ContactsModelImpl implements ContactsModel {

    private ContactsListener contactListener;
    private ContactDataListener contactDataListener;
    private ContactPresenceListener contactPresenceListener;
    private Context mContext;

    public ContactsModelImpl(ContactsListener listener) {
        this.contactListener = listener;
        this.mContext = WeApplication.getInstance().getApplicationContext();
        contactDataListener = new ContactDataListener() {
            @Override
            public void onLoadContactSuccess(List<ContactEntity> contactsList, List<CompanyEntity> companyList,String updateTime) {
                contactListener.loadContactsSuccess(contactsList, companyList);
                DispatcherSPManager.setUpdateTime(mContext,updateTime);
            }

            @Override
            public void onContactUpdate(ContactEntity contactEntity, String updateTime) {
                contactListener.onUpdateContactSuccess(contactEntity, updateTime);
                DispatcherSPManager.setUpdateTime(mContext,updateTime);
            }

            @Override
            public void onLoadContactListError() {
                contactListener.onUpdateContactError();
            }
        };

        contactPresenceListener = new ContactPresenceListener() {
            @Override
            public void onContactPresence(boolean isSubscribed, HashMap<String, Integer> hashMap) {
                contactListener.onContactPresence(isSubscribed,hashMap);
            }

            @Override
            public void onContactPresence(boolean isSubscribed, String mdn, int state) {
                contactListener.onContactPresence(isSubscribed,mdn,state);
            }
        };

        ContactManager.INSTANCE.registerLoadContactDataListener(contactDataListener);
        ContactManager.INSTANCE.registerContactPresenceListener(contactPresenceListener);
    }

    @Override
    public void loadContactsData() {
        ContactManager.INSTANCE.getOrgnizationInfo(mContext, DispatcherSPManager.getUpdateTime(mContext));
    }

    @Override
    public void refreshContactsData() {

    }

    @Override
    public void onPause() {
        ContactManager.INSTANCE.unSubscribeContactPresence();
        ContactManager.INSTANCE.unregisterLoadContactDataListener();
        ContactManager.INSTANCE.unregisterContactPresenceListener();
    }

    @Override
    public void onResume() {
        ContactManager.INSTANCE.subscribeContactPresence(mContext);
        ContactManager.INSTANCE.registerLoadContactDataListener(contactDataListener);
        ContactManager.INSTANCE.registerContactPresenceListener(contactPresenceListener);
    }
}
