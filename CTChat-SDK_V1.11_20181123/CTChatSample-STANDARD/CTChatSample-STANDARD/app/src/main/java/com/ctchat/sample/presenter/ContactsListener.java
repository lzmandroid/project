package com.ctchat.sample.presenter;


import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sdk.ptt.tool.entity.CompanyEntity;

import java.util.HashMap;
import java.util.List;

/**
 */
public interface ContactsListener {

    void loadContactsSuccess(List<ContactEntity> contactsList, List<CompanyEntity> companyList);

    void onContactPresence(boolean isSubscribed, HashMap<String, Integer> hashMap);

    void onContactPresence(boolean isSubscribed, String mdn, int state);

    void onUpdateContactSuccess(ContactEntity contactEntity, String updateTime);

    void onUpdateContactError();
}
