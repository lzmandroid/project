package com.ctchat.sample.login;

import android.content.Context;

public interface LoginView {
    /**
     * 显示等待对话框，此时界面不可交互
     */
    public void showWaitingDialog();
    /**
     * 隐藏等待对话框，此时界面可交互
     */
    public void hideWaitingDialog();

    /**
     * 网络不符合要求时弹出
     */
    public void showBadNetworkDialog();

    /**
     *在设备非法时弹出
     */
    public void showInvalidDeviceDialog();


    /**
     * imsi有效，需要短信登录
     */
    public void needPinCode();

    /**
     * 无效imsi，需要手机号+密码登录
     */
    void needInputPhoneNumberAndPassword();

    /**
     * 号码未开户提示
     */
    void showInvalidPhoneNumberDialog();

    /**
     * 通过手机号+验证码登录时，获取验证码成功
     */
    void getPinCodeSuccess();

    /**
     * 清除数据对话框，无效apikey或token时调用
     */
    void showClearDataDialog();

    /**
     * 无效的imsi，禁止登录
     */
    void showForbidLoginDialog();

    /**
     * 无效imsi，允许手机号和验证码登录
     */
    void needInputPhoneAndSmsCode();

    /**
     * 密码或验证码错误，手动登录
     */
    void wrongPasswordOrSmsCode();

    /**
     * 验证码错误
     */
    void wrongSmsCode();

    /**
     * 验证码次数达到上限
     */
    void showSmsCodeLimitDialog();

    Context getContext();

    void showInvalidAppKeyDialog();

    void showExitAppDialog(Object desc);

    void needChangePassword();
}
