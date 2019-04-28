package com.ctchat.sample.model;

/**
 */
public interface ContactsModel {

    void loadContactsData();

    void refreshContactsData();

    void onPause();

    void onResume();
}
