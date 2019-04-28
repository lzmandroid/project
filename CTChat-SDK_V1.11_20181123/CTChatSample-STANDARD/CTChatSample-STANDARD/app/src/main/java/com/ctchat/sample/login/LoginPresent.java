package com.ctchat.sample.login;

import android.content.Context;

public interface LoginPresent {

    void login(final Context context);

    void login(final Context context, final String mdn, final String pwd);

    void loginBySmsCode(Context context, String mdn, String smsCode);

    void loginBySmsCode(Context context, String smsCode);

    void getSmsCode(Context context, String mdn);

}
