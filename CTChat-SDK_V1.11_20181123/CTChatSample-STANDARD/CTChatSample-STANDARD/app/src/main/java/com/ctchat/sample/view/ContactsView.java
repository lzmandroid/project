package com.ctchat.sample.view;


import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sdk.ptt.tool.entity.CompanyEntity;

import java.util.List;

/**
 */
public interface ContactsView {
    void netError();
    void onContactPresence();
    void onContactListPresence();
    void loadContactsSuccess(List<ContactEntity> contactsList, List<CompanyEntity> groupsList);
    void onUpdateContactsSuccess();
    void onUpdateContactError();
}
