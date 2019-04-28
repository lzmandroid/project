package com.ctchat.sample.presenter;

import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sdk.ptt.tool.entity.CompanyEntity;
import com.ctchat.sample.model.ContactsModel;
import com.ctchat.sample.model.ContactsModelImpl;
import com.ctchat.sample.view.ContactsView;

import java.util.HashMap;
import java.util.List;

/**
 */
public class ContactsPresentImpl implements ContactsPresent, ContactsListener {
    private static final String TAG = "ContactsPresentImpl";
    private ContactsView contactsView;
    private ContactsModel contactsModel;

    public ContactsPresentImpl(ContactsView contactsView) {
        this.contactsView = contactsView;
        contactsModel = new ContactsModelImpl(this);
    }

    @Override
    public void loadContactsSuccess(List<ContactEntity> contactsList, List<CompanyEntity> groupsList) {
        contactsView.loadContactsSuccess(contactsList, groupsList);
    }

    @Override
    public void onContactPresence(boolean isSubscribed, HashMap<String, Integer> hashMap) {
        contactsView.onContactListPresence();
    }

    @Override
    public void onContactPresence(boolean isSubscribed, String mdn, int state) {
        contactsView.onContactPresence();
    }

    @Override
    public void onUpdateContactSuccess(ContactEntity contactEntity, String updateTime) {
        contactsView.onUpdateContactsSuccess();
    }

    @Override
    public void onUpdateContactError() {
        contactsView.onUpdateContactError();
    }

    @Override
    public void loadContacts() {
        Logger.i(TAG, "loadContacts");
        if (null == contactsModel) {
            contactsModel = new ContactsModelImpl(this);
        }
        contactsModel.loadContactsData();
    }

    @Override
    public void onPause() {
        contactsModel.onPause();
    }

    @Override
    public void onResume() {
        contactsModel.onResume();
    }
}
