package com.ctchat.sample.presenter;

import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sdk.ptt.tool.entity.CompanyEntity;
import com.ctchat.sample.model.ContactsModel;
import com.ctchat.sample.model.ContactsModelImpl;
import com.ctchat.sample.view.ContactDetailView;

import java.util.HashMap;
import java.util.List;

public class ContactDetailPresentImpl implements ContactDetailPresent, ContactsListener {

    private static final String TAG = "ContactDetailPresentImpl";

    private ContactDetailView contactDetailView;
    private ContactsModel contactsModel;

    public ContactDetailPresentImpl(ContactDetailView contactDetailView) {
        this.contactDetailView = contactDetailView;
        contactsModel = new ContactsModelImpl(this);
    }


    @Override
    public void onPause() {
        Logger.d(TAG, "onPause");
        contactsModel.onPause();
    }

    @Override
    public void onResume() {
        Logger.d(TAG, "onResume");
        contactsModel.onResume();
    }

    @Override
    public void loadContactsSuccess(List<ContactEntity> contactsList, List<CompanyEntity> companyList) {

    }

    @Override
    public void onContactPresence(boolean isSubscribed, HashMap<String, Integer> hashMap) {
        Logger.d(TAG, "onContactPresence hashMap");
        contactDetailView.onContactStateChanged();
    }

    @Override
    public void onContactPresence(boolean isSubscribed, String mdn, int state) {
        Logger.d(TAG, "onContactPresence state");
        contactDetailView.onContactStateChanged();
    }

    @Override
    public void onUpdateContactSuccess(ContactEntity contactEntity, String updateTime) {

    }

    @Override
    public void onUpdateContactError() {

    }
}
