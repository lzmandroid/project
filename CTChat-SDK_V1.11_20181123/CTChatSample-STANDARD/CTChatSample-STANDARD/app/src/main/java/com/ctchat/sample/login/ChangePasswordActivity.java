package com.ctchat.sample.login;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ctchat.sample.MainActivity;
import com.ctchat.sample.R;
import com.ctchat.sample.tool.MyActivityManager;
import com.ctchat.sample.widget.XpActivity;
import com.ctchat.sdk.basemodule.util.ToastUtil;
import com.ctchat.sdk.password.ChangePasswordCallback;
import com.ctchat.sdk.password.PasswordError;
import com.ctchat.sdk.password.PasswordManager;

public class ChangePasswordActivity extends XpActivity {

    private static final String TAG = "ChangePasswordActivity";
    private static final String EXTRA = "justLogin";
    private RelativeLayout backLayout;
    private Button btnConfirmModify;
    private EditText etCurrentPwd;
    private EditText etNewPwd;
    private EditText etConfirmPwd;
    private TextView tvNewPwd;
    private boolean justLogin;

    public static Intent getCallingIntent(Context context, boolean justLogin) {
        Intent intent = new Intent(context, ChangePasswordActivity.class);
        intent.putExtra(EXTRA, justLogin);
        return intent;
    }

    @Override
    protected void initView() {
        backLayout = (RelativeLayout) findViewById(R.id.rl_modify_pwd_back_to_main);
        btnConfirmModify = (Button) findViewById(R.id.btn_confirm_modify_pwd);
        etCurrentPwd = (EditText) findViewById(R.id.et_current_pwd);
        etNewPwd = (EditText) findViewById(R.id.et_new_pwd);
        etConfirmPwd = (EditText) findViewById(R.id.et_confirm_pwd);
        tvNewPwd = (TextView) findViewById(R.id.tv_new_pwd);

        tvNewPwd.setText("    " + getResources().getString(R.string.new_password));
        backLayout.setOnClickListener(this);
        btnConfirmModify.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        justLogin = getIntent().getBooleanExtra(EXTRA, false);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_change_pwd;
    }

    @Override
    protected String[] broadcastActions() {
        return new String[0];
    }

    @Override
    protected void doAction(Intent intent) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_modify_pwd_back_to_main:
                finish();
                break;
            case R.id.btn_confirm_modify_pwd:
                if (etCurrentPwd.getText().toString().length() <= 0 || etNewPwd.getText().toString().length() <= 0 || etConfirmPwd.getText().length() <= 0) {
                    ToastUtil.longShow(context, R.string.fill_all);
                } else if (etNewPwd.getText().toString().length() < 6) {
                    ToastUtil.longShow(context, R.string.short_password);
                    etNewPwd.setText("");
                    etConfirmPwd.setText("");
                } else if (!etConfirmPwd.getText().toString().equals(etNewPwd.getText().toString())) {
                    ToastUtil.longShow(context, R.string.inconsistent_password);
                    etNewPwd.setText("");
                    etConfirmPwd.setText("");
                } else {
                    PasswordManager.changePassword(this, etCurrentPwd.getText().toString(), etNewPwd.getText().toString(), new ChangePasswordCallback() {
                        @Override
                        public void onChangePasswordSuccess() {
                            MyActivityManager.getInstance().finishAllActivityExceptCurrent();
                            if (justLogin) {
                                ToastUtil.shortShow(context, "修改成功！");
                                // 如果刚刚登录成功，则直接进入app主界面
                                startActivity(new Intent(ChangePasswordActivity.this, MainActivity.class));
                            } else {
                                // 不管什么情况下，修改密码成功后都必须重新登录
                                ToastUtil.shortShow(context, "修改成功！请重新登录");
                                startActivity(new Intent(ChangePasswordActivity.this, LoginActivity.class));
                            }
                            finish();
                        }

                        @Override
                        public void onChangePasswordError(int errorCode) {
                            switch (errorCode) {
                                case PasswordError.ERROR_SAME_PASSWORD:
                                    ToastUtil.shortShow(context, "新密码与原密码相同");
                                    break;
                                case PasswordError.ERROR_WRONG_ORIGINAL_PASSWORD:
                                    ToastUtil.shortShow(context, "原密码错误");
                                    break;
                                case PasswordError.ERROR_INVALID_NEW_PASSWORD:
                                    ToastUtil.shortShow(context, "新密码不符合规范");
                                    break;
                            }
                        }

                        @Override
                        public void onChangePasswordFailure() {
                            ToastUtil.shortShow(context, "修改失败");
                        }
                    });
                }
                break;
        }
    }

}
