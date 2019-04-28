package com.ctchat.sample.login;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.ctchat.sample.R;
import com.ctchat.sample.login.util.PhoneNumberUtils;
import com.ctchat.sample.login.util.PreferLogin;
import com.ctchat.sample.login.util.SimUtil;
import com.ctchat.sample.login.util.TimerReacquire;
import com.ctchat.sample.login.widget.dialog.CustomHintDialog;
import com.ctchat.sample.receiver.SimStateReceiver;
import com.ctchat.sample.util.NetUtil;
import com.ctchat.sample.widget.CustomLoadingDialog;
import com.ctchat.sdk.AccountManager;
import com.ctchat.sdk.auth.tools.DeviceInfoTools;
import com.ctchat.sdk.auth.util.SmsManager;
import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sdk.basemodule.util.ToastUtil;

public class LoginActivity extends AppCompatActivity implements LoginView, View.OnClickListener, SmsManager.SmsCodeListener, TimerReacquire.TimerFinishListener, SimStateReceiver.OnFinishSimDetectListener {
    private static final String TAG = "LoginActivity";

    private LoginPresent loginPresenter;
    private String phoneNumber, password;
    private static final long TOTAL_TIME = 120000;
    private static final long INTERVAL_TIME = 1000;

    Button confirmBtn;
    EditText phoneNumberEdit, pinEdit, pwdEdit;
    public Dialog loadingDialog;
    public Dialog detectingDialog;
    private PhoneNumberWatcher phoneNumberWatcher;
    private Context context;
    // 短信验证码输入框
    private LinearLayout linearSmsCode;
    private EditText editSmsCode;
    private Button btnSmsCode;

    private TimerReacquire timer;
    private SmsManager smsManager;
    private boolean needMdnSmsCode = false;

    private SimStateReceiver simStateReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();

        if (!isTaskRoot()) {
            finish();
            return;
        }

        Logger.d(TAG, "onCreate");
        setContentView(R.layout.activity_login_hdpi);
        confirmBtn = (Button) findViewById(R.id.confirm_btn);
        confirmBtn.setOnClickListener(this);
        phoneNumberWatcher = new PhoneNumberWatcher();
        phoneNumberEdit = (EditText) findViewById(R.id.phone_number_edittxt);
        phoneNumberEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
        phoneNumberEdit.addTextChangedListener(phoneNumberWatcher);
        pwdEdit = (EditText) findViewById(R.id.pwd_edittxt);
        pwdEdit.setVisibility(View.GONE);

        linearSmsCode = (LinearLayout) findViewById(R.id.linear_sms_code);
        editSmsCode = (EditText) findViewById(R.id.edit_sms_code);
        btnSmsCode = (Button) findViewById(R.id.btn_get_sms_code);
        linearSmsCode.setVisibility(View.GONE);
        disableGetSmsCodeButton();

        loginPresenter = new LoginPresentImpl(this);
        registerReceiver();
        showWaitingDialog();
    }

    private void registerReceiver() {
        simStateReceiver = new SimStateReceiver();
        simStateReceiver.setOnFinishSimDetectListener(this);
        registerReceiver(simStateReceiver, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
    }

    private boolean checkWhetherSwapSimCard() {
        String curIccid = SimUtil.getIccid(this);
        curIccid = curIccid == null ? "" : curIccid;
        Log.d(TAG, "checkWhetherSwapSimCard: curIccid = " + curIccid);
        // 登录成功过，并且更换了sim卡
        if (PreferLogin.hasLogin(this, false) &&
                !PreferLogin.getIccid(this, "").equals(curIccid)) {
            showSimSwapDialog();
            return true;
        }
        return false;
    }

    public void showSimSwapDialog() {
        hideWaitingDialog();
        final CustomHintDialog.Builder builder = new CustomHintDialog.Builder(this);
        builder.setTitle(R.string.sim_swap_dialog_hint).setConfirmButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearData();
                dialog.dismiss();
                finish();
            }
        }).setCancelable(false).create().show();
    }

    private void clearData() {
        PreferLogin.clearData(this);
        AccountManager.clearLoginData(this);
    }
    private void checkValidWithoutPhoneNumber() {
        loginPresenter.login(this);
    }

    /**
     * 显示手动登录UI
     */
    private void showManualLoginUI() {
        showMdnPasswordEditText();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideWaitingDialog();
        hideDetectSmsCodeDialog();
        if (phoneNumberEdit != null) {
            phoneNumberEdit.removeTextChangedListener(phoneNumberWatcher);
        }
        phoneNumberWatcher = null;
        unregisterReceiver();
        unregisterSmsManager();
    }

    private void unregisterReceiver() {
        if (simStateReceiver != null) {
            unregisterReceiver(simStateReceiver);
        }
    }

    @Override
    public void showWaitingDialog() {
        Logger.d(TAG, "show waiting dialog");
        CustomLoadingDialog customLoadingDialog = new CustomLoadingDialog(this, getString(R.string.be_logging_in));
        loadingDialog = customLoadingDialog.createLoadingDialog();
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    @Override
    public void hideWaitingDialog() {
        Logger.d(TAG, "hideWaitingDialog");
        if (loadingDialog != null && loadingDialog.isShowing()) {
            Logger.d(TAG, "dismiss,cancel waiting dialog");
            loadingDialog.setCancelable(true);
            loadingDialog.dismiss();
            loadingDialog.cancel();
            loadingDialog = null;
        }
    }

    public void showBadNetworkDialog() {
        showErrorDescDialog(R.string.bad_network);
    }

    // 非法终端，无法使用
    @Override
    public void showInvalidDeviceDialog() {
        showErrorDescDialog(R.string.invalid_device);
    }

    private void setNeedMdnSmsCode(boolean needMdnSmsCode) {
        this.needMdnSmsCode = needMdnSmsCode;
    }

    private void showErrorDescDialog(Object desc) {
        Logger.i(TAG, "showErrorDescDialog");

        final CustomHintDialog.Builder builder = new CustomHintDialog.Builder(LoginActivity.this);

        if (desc instanceof String) {
            builder.setTitle((String) desc);
        } else {
            builder.setTitle((int) desc);
        }

        builder.setConfirmButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                hideWaitingDialog();
                finish();
            }
        }).setCancelable(false).create().show();
    }

    private void enableRegisterButton() {
        confirmBtn.setEnabled(true);
    }

    private void disableRegisterButton() {
        confirmBtn.setEnabled(false);
    }

    private void enableGetSmsCodeButton() {
        btnSmsCode.setEnabled(true);
    }

    private void disableGetSmsCodeButton() {
        btnSmsCode.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: ");
        switch (v.getId()) {
            case R.id.confirm_btn:
                if (needSmsCode()) {
                    String smsCode = editSmsCode.getText().toString();
                    if (TextUtils.isEmpty(smsCode)) {
                        ToastUtil.shortShow(this, R.string.hint_pin_code);
                        return;
                    }
                    showWaitingDialog();
                    phoneNumber = phoneNumberEdit.getText().toString();
                    if (needMdnSmsCode) {
                        loginPresenter.loginBySmsCode(this, phoneNumber, editSmsCode.getText().toString());
                    } else {
                        loginPresenter.loginBySmsCode(this, editSmsCode.getText().toString());
                    }
                    return;
                }
                phoneNumber = phoneNumberEdit.getText().toString();
                password = pwdEdit.getText().toString();
                if (TextUtils.isEmpty(password)) {
                    ToastUtil.shortShow(this, R.string.password_hint);
                } else {
                    showWaitingDialog();
                    loginPresenter.login(this, phoneNumber, password);
                }
                break;
            default:
                break;
        }
    }

    // sim卡加载完毕
    @Override
    public void onFinishSimDetect(boolean simInvalid) {
        Log.d(TAG, "onFinishSimDetect: simInvalid = " + simInvalid);
        if (!NetUtil.isNetConnected(this)) {
            showBadNetworkDialog();
        } else if (checkWhetherSwapSimCard()) {
            // 如果更换了sim卡
            return;
        }
        checkValidWithoutPhoneNumber();
    }

    class PhoneNumberWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (PhoneNumberUtils.isValidePhoneNumber(phoneNumberEdit.getText().toString())) {
                enableRegisterButton();
                enableGetSmsCodeButton();
            } else {
                disableRegisterButton();
                disableGetSmsCodeButton();
            }
        }
    }

    public void showDetectSmsCodeDialog() {
        Logger.d(TAG, "showDetectSmsCodeDialog");
        CustomLoadingDialog customLoadingDialog = new CustomLoadingDialog(this, getString(R.string.be_detecting_sms_code));
        detectingDialog = customLoadingDialog.createLoadingDialog();
        if (!detectingDialog.isShowing()) {
            Logger.d(TAG, "[showDetectSmsCodeDialog]");
            detectingDialog.show();
        }
    }

    public void hideDetectSmsCodeDialog() {
        Logger.d(TAG, "hideDetectSmsCodeDialog");
        if (detectingDialog != null && detectingDialog.isShowing()) {
            Logger.d(TAG, "dismiss,cancel waiting dialog");
            detectingDialog.setCancelable(true);
            detectingDialog.dismiss();
            detectingDialog.cancel();
            detectingDialog = null;
        }
    }

    private void startDetectSmsCode() {
        Log.d(TAG, "startDetectSmsCode: ");
        hideWaitingDialog();
        showDetectSmsCodeDialog();
        detectSmsCode();
        timerCountDown();
    }

    @Override
    public void needPinCode() {
        Log.d(TAG, "needPinCode: ");
        setNeedMdnSmsCode(false);
        enableGetSmsCodeButton();
        showSmsCodeUi();
        startDetectSmsCode();
    }

    @Override
    public void getPinCodeSuccess() {
        Log.d(TAG, "getPinCodeSuccess: ");
        startDetectSmsCode();
    }

    // 禁止登陆  区分有无SIM卡  点击确定强制退出  错误的IMSI号；无SIM卡
    public void showForbidLoginDialog() {
        Log.d(TAG, "showForbidLoginDialog: ");
        String[] imsi = DeviceInfoTools.getImsiArray(this);
        if (TextUtils.isEmpty(imsi[0]) && TextUtils.isEmpty(imsi[1])) { // 无SIM卡
            showExitAppDialog(R.string.forbid_login_no_sim);
        } else {
            showExitAppDialog(R.string.forbid_login_invalid_imsi);
        }
    }

    // 显示输入手机框，点击登录则弹出解析验证码的加载框，超时2分钟则弹出输入验证码框
    public void needInputPhoneAndSmsCode() {
        Log.d(TAG, "needInputPhoneAndSmsCode: ");
        setNeedMdnSmsCode(true);
        hideWaitingDialog();
        hideDetectSmsCodeDialog();
        showMdnSmsCodeEditText();
    }

    @Override
    public void wrongPasswordOrSmsCode() {
        if (needMdnSmsCode) {
            ToastUtil.shortShow(this, R.string.login_error_sms_wrong);
        } else {
            ToastUtil.shortShow(this, R.string.login_error_password_wrong);
        }
    }

    @Override
    public void wrongSmsCode() {
        hideDetectSmsCodeDialog();
        ToastUtil.shortShow(this, R.string.login_error_sms_wrong);
    }

    // 验证码次数达到上限，点确定退出
    public void showSmsCodeLimitDialog() {
        Log.d(TAG, "showSmsCodeLimitDialog: ");
        hideWaitingDialog();
        hideDetectSmsCodeDialog();
        showExitAppDialog(R.string.forbid_login_sms_limit);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void showInvalidAppKeyDialog() {
        showErrorDescDialog(R.string.login_error_invalid_app_key);
    }

    public void showExitAppDialog(Object desc) {
        Logger.i(TAG, "showExitAppDialog desc = " + desc);

        final CustomHintDialog.Builder builder = new CustomHintDialog.Builder(this);

        if (desc instanceof String) {
            builder.setTitle((String) desc);
        } else {
            builder.setTitle((int) desc);
        }

        builder.setConfirmButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                hideWaitingDialog();
                finish();
            }
        }).setCancelable(false).create().show();
    }

    @Override
    public void needChangePassword() {
        startActivity(ChangePasswordActivity.getCallingIntent(this, true));
    }

    private void timerCountDown() {
        timer = new TimerReacquire(TOTAL_TIME, INTERVAL_TIME);
        timer.setOnTimerFinishListener(this);
        timer.start();
    }

    private void timerCancel() {
        timer.cancel();
    }

    // 倒计时结束
    @Override
    public void onTimerFinish() {
        hideDetectSmsCodeDialog();
        ToastUtil.shortShow(this, R.string.detect_sms_failed);
    }

    private void detectSmsCode() {
        Log.d(TAG, "detectSmsCode: smsManager.registerSmsCodeListener");
        if (smsManager == null) {
            smsManager = new SmsManager(this);
            smsManager.registerSmsCodeListener("", this);
        }
    }

    private void showMdnPasswordEditText() {
        phoneNumberEdit.setVisibility(View.VISIBLE);
        pwdEdit.setVisibility(View.VISIBLE);
        linearSmsCode.setVisibility(View.GONE);
    }

    private void showMdnSmsCodeEditText() {
        phoneNumberEdit.setVisibility(View.VISIBLE);
        pwdEdit.setVisibility(View.GONE);
        linearSmsCode.setVisibility(View.VISIBLE);
        btnSmsCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = phoneNumberEdit.getText().toString();
                showWaitingDialog();
                loginPresenter.getSmsCode(LoginActivity.this, phoneNumber);
//                }
            }
        });
    }

    // 只输入验证码情况
    private void showSmsCodeLinearLayout() {
        phoneNumberEdit.setVisibility(View.GONE);
        pwdEdit.setVisibility(View.GONE);
        linearSmsCode.setVisibility(View.VISIBLE);
        enableRegisterButton();
    }

    // 只输入验证码情况
    private void showSmsCodeUi() {
        Log.d(TAG, "showSmsCodeUi: btnSmsCode.isClickable() = " + btnSmsCode.isClickable());
        // 仅在第一次展示的时候初始化
        if (linearSmsCode.getVisibility() == View.VISIBLE)
            return;
        showSmsCodeLinearLayout();
        if (smsManager == null) {
            smsManager = new SmsManager(this);
            smsManager.registerSmsCodeListener("", this);
        }
        btnSmsCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: btnSmsCode");
                showWaitingDialog();
                loginPresenter.login(LoginActivity.this); // 普通登录发送验证码
            }
        });
    }

    @Override
    public void onReceiveSmsCode(String smsCode) {
        Log.d(TAG, "onReceiveSmsCode: smsCode = " + smsCode);
        if (TextUtils.isEmpty(smsCode)) {
            return;
        }

        timerCancel();
//        hideDetectSmsCodeDialog(); // 不能调用此方法 有异常
//        showWaitingDialog();// 不能调用此方法 有异常
        editSmsCode.setText(smsCode);
        if (needMdnSmsCode) {
            loginPresenter.loginBySmsCode(this, phoneNumber, smsCode);
        } else {
            loginPresenter.loginBySmsCode(this, smsCode);
        }
    }

    public void unregisterSmsManager() {
        if (smsManager != null) {
            smsManager.unregisterSmsCodeListener();
        }
    }

    private boolean needSmsCode() {
        return linearSmsCode.getVisibility() == View.VISIBLE && pwdEdit.getVisibility() == View.GONE;
    }

    @Override
    public void needInputPhoneNumberAndPassword() {
        hideWaitingDialog();
        showManualLoginUI();
    }

    @Override
    public void showInvalidPhoneNumberDialog() {
        hideWaitingDialog();
        // 改号码未开户，请联系管理员
        final CustomHintDialog.Builder builder = new CustomHintDialog.Builder(this);
        builder.setTitle(R.string.phone_number_not_register).setConfirmButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
                System.exit(0);
            }
        }).setCancelable(false).create().show();
    }

    @Override
    public void showClearDataDialog() {
        final CustomHintDialog.Builder builder = new CustomHintDialog.Builder(LoginActivity.this);

        builder.setTitle(R.string.login_error_data_invalid);

        builder.setConfirmButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AccountManager.clearLoginData(LoginActivity.this);
                dialog.dismiss();
                hideWaitingDialog();
                finish();
            }
        }).setCancelable(false).create().show();
    }
}
