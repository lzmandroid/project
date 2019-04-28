package com.ctchat.sample.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.ctchat.sample.MainActivity;
import com.ctchat.sample.R;
import com.ctchat.sample.login.util.PreferLogin;
import com.ctchat.sample.login.util.SimUtil;
import com.ctchat.sdk.AccountManager;
import com.ctchat.sdk.LoginCallback;
import com.ctchat.sdk.auth.api.ErrorCode;
import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sdk.basemodule.util.ToastUtil;
import com.ctchat.sdk.ptt.tool.entity.UserInfo;
import com.ctchat.sdk.ptt.tool.intercom.IntercomManager;
import com.ctchat.sdk.ptt.tool.userinfo.UserInfoManager;

/**
 *
 */
public class LoginPresentImpl implements LoginPresent, LoginCallback {
    private static final String TAG = "LoginPresentImpl";
    private LoginView loginView;

    public LoginPresentImpl(LoginView loginView) {
        this.loginView = loginView;
    }

    @Override
    public void login(Context context) {
        AccountManager.login(context, this);
    }

    @Override
    public void login(Context context, String mdn, String pwd) {
        AccountManager.login(context, mdn, pwd, this);
    }

    @Override
    public void loginBySmsCode(Context context, String mdn, String smsCode) {
        AccountManager.loginBySmsCode(context, mdn, smsCode, this);
    }

    @Override
    public void loginBySmsCode(Context context, String smsCode) {
        AccountManager.loginBySmsCode(context, smsCode, this);
    }

    @Override
    public void getSmsCode(Context context, String mdn) {
        AccountManager.getSmsCode(context, mdn, this);
    }

    @Override
    public void onLoginSuccess(boolean needChangePassword) {
        Logger.d(TAG, "needChangePassword = " + needChangePassword);
        ToastUtil.shortShow(loginView.getContext(), R.string.login_success);
        saveData();
        if (needChangePassword) {
            loginView.needChangePassword();
        } else {
            UserInfo userInfo = UserInfoManager.getInstance().getUserInfo(loginView.getContext());
            //开启广播呼叫
            if (userInfo.isEnableChatBroadcast()) {
                IntercomManager.INSTANCE.runGroupBroadcast();
            }
            if (loginView != null) {
                loginView.hideWaitingDialog();
            }
            Intent intent = new Intent(loginView.getContext(), MainActivity.class);
            loginView.getContext().startActivity(intent);
            ((Activity)loginView.getContext()).finish();
        }
    }

    private void saveData() {
        PreferLogin.putHasLogin(loginView.getContext(), true);
        PreferLogin.putIccid(loginView.getContext(), SimUtil.getIccid(loginView.getContext()));
    }

    @Override
    public void onLoginError(int errCode) {
        Logger.e(TAG, errCode);
        loginView.hideWaitingDialog();
        switch (errCode) {
            case ErrorCode.LOGIN_ERROR_INVALID_APP_KEY:
                loginView.showInvalidAppKeyDialog();
                break;
            case ErrorCode.LOGIN_ERROR_INVALID_IMSI_FORBID:
                loginView.showForbidLoginDialog();
                break;
            case ErrorCode.LOGIN_ERROR_INVALID_IMSI_PHONE_SMS:
                loginView.needInputPhoneAndSmsCode();
                break;
            case ErrorCode.LOGIN_ERROR_INVALID_IMSI_PHONE_PASSWORD:
                loginView.needInputPhoneNumberAndPassword();
                break;
            case ErrorCode.LOGIN_ERROR_INVALID_ACCOUNT:
                loginView.showInvalidPhoneNumberDialog();
                break;
            case ErrorCode.LOGIN_ERROR_NEED_SMS_VERIFICATION:
                loginView.needPinCode();
                break;
            case ErrorCode.LOGIN_ERROR_WRONG_PASSWORD_OR_SMS:
                loginView.wrongPasswordOrSmsCode();
                break;
            case ErrorCode.LOGIN_ERROR_REQUEST_TOO_MUCH_PIN_CODE:
                loginView.showSmsCodeLimitDialog();
                break;
            case ErrorCode.LOGIN_ERROR_INVALID_PIN_CODE:
                loginView.wrongSmsCode();
                break;
            case ErrorCode.LOGIN_ERROR_INVALID_API_KEY:
            case ErrorCode.LOGIN_ERROR_INVALID_TOKEN:
                loginView.showClearDataDialog();
                break;
            case ErrorCode.LOGIN_ERROR_RECEIVE_SMS_CODE:
                loginView.getPinCodeSuccess();
                break;
            case ErrorCode.LOGIN_ERROR_SERVER_UNAVAILABLE:
                loginView.needInputPhoneNumberAndPassword();
                ToastUtil.shortShow(loginView.getContext(), R.string.ptt_account_result_err_server_unavailable);
                break;
            case ErrorCode.LOGIN_ERROR_NETWORK:
                loginView.showExitAppDialog(R.string.net_error);
                break;
            case ErrorCode.LOGIN_ERROR_REMOTE_FORBID:
                loginView.showExitAppDialog(R.string.remote_forbid);
                break;
            case ErrorCode.LOGIN_ERROR_MUCH_PASSWORD_REQUEST:
                loginView.showExitAppDialog(R.string.much_password_request);
                break;
            default:
                loginView.showExitAppDialog(R.string.login_fail);
                break;
        }
    }

    @Override
    public void onLoginFailure() {
        Logger.e(TAG, "onLoginFailure");
        ToastUtil.shortShow(loginView.getContext(), R.string.login_time_out);
        AccountManager.clearLoginData(loginView.getContext());
        loginView.needInputPhoneNumberAndPassword();
    }

}
