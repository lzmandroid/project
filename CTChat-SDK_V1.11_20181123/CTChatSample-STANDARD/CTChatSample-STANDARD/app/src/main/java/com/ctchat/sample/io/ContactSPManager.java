package com.ctchat.sample.io;

import android.content.Context;

import com.ctchat.sdk.ptt.tool.entity.UserInfo;
import com.ctchat.sdk.ptt.tool.entity.ContactEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;
import com.ctchat.sdk.ptt.tool.userinfo.UserInfoManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ContactSPManager {
    /**
     * 在联系人列表里去除当前登录用户
     * @param context
     * @param list
     */
    public static void deleteIterator(Context context, List<ContactEntity> list) {
        Iterator<ContactEntity> iterator = list.iterator();
        UserInfo userInfo = UserInfoManager.getInstance().getUserInfo(context);
        while (iterator.hasNext()) {
            ContactEntity contactEntity = iterator.next();
            if (contactEntity.getMdn().equals(userInfo.getMdn())) {
                iterator.remove();
                break;
            }
        }
    }

    /**
     * 在联系人列表里去除当前已参与会话的所有用户
     * @param list
     * @param sessionEntity
     */
    public static void deleteExistIterator(List<ContactEntity> list, SessionEntity sessionEntity) {
        List<String> existList = new ArrayList<>();
        for (ContactEntity contact : sessionEntity.getMemberAll()) {
            existList.add(contact.getPID());
        }
        Iterator<ContactEntity> iterator = list.iterator();
        while (iterator.hasNext()) {
            ContactEntity contactEntity = iterator.next();
            if (existList.contains(contactEntity.getPID())) {
                iterator.remove();
            }
        }
    }
}
