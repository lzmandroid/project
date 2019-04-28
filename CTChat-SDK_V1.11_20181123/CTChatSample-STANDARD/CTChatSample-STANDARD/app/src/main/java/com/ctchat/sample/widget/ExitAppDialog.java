package com.ctchat.sample.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.ctchat.sample.R;


/**
 * 信息提示dialog
 */

public class ExitAppDialog extends Dialog {
    public ExitAppDialog(Context context) {
        super(context);
    }

    public ExitAppDialog(Context context, int theme) {
        super(context, theme);
    }

    public static class Builder {
        private Context context;
        private String backButtonText; // 对话框返回按钮文本
        private String confirmButtonText; // 对话框确定文本

        //对话框按钮监听事件
        private OnClickListener
                backButtonClickListener,
                confirmButtonClickListener;

        private CompoundButton.OnCheckedChangeListener autoLoginChangeListener;

        public Builder(Context context) {
            this.context = context;
        }

        /**
         * 设置back按钮的事件和文本
         */
        public Builder setBackButton(int backButtonText, OnClickListener listener) {
            this.backButtonText = (String) context.getText(backButtonText);
            this.backButtonClickListener = listener;
            return this;
        }

        //设置back按钮的事件和文本
        public Builder setBackButton(String backButtonText, OnClickListener listener) {
            this.backButtonText = backButtonText;
            this.backButtonClickListener = listener;
            return this;
        }

        //设置确定按钮事件和文本
        public Builder setConfirmButton(int confirmButtonText, OnClickListener listener) {
            this.confirmButtonText = (String) context.getText(confirmButtonText);
            this.confirmButtonClickListener = listener;
            return this;
        }

        //设置确定按钮事件和文本
        public Builder setConfirmButton(String confirmButtonText, OnClickListener listener) {
            this.confirmButtonText = confirmButtonText;
            this.confirmButtonClickListener = listener;
            return this;
        }

        public Builder setAutoLogin(CompoundButton.OnCheckedChangeListener checkedChangeListener) {
            this.autoLoginChangeListener = checkedChangeListener;
            return this;
        }

        //创建自定义的对话框
        public ExitAppDialog create() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // 实例化自定义的对话框主题
            final ExitAppDialog dialog = new ExitAppDialog(context, R.style.style_custom_dialog);
            View layout = inflater.inflate(R.layout.dialog_exit_app, null);
            dialog.addContentView(layout, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

            // 设置返回按钮事件和文本
            if (backButtonText != null) {
                Button bckButton = ((Button) layout.findViewById(R.id.btn_cancel_exit_app));
                bckButton.setText(backButtonText);

                if (backButtonClickListener != null) {
                    bckButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            backButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
                        }
                    });
                }
            } else {
                layout.findViewById(R.id.btn_cancel_exit_app).setVisibility(View.GONE);
            }

            // 设置确定按钮事件和文本
            if (confirmButtonText != null) {
                Button cfmButton = ((Button) layout.findViewById(R.id.btn_confirm_exit_app));
                cfmButton.setText(confirmButtonText);

                if (confirmButtonClickListener != null) {
                    cfmButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            confirmButtonClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                        }
                    });
                }
            } else {
                layout.findViewById(R.id.btn_confirm_exit_app).setVisibility(View.GONE);
            }

            CheckBox cbAutoLogin = (CheckBox) layout.findViewById(R.id.cb_auto_login);
//                cbAutoLogin.setChecked(LoginSPManager.getAutoLoginStatus(context));
            cbAutoLogin.setVisibility(View.GONE);

//            if (autoLoginChangeListener != null) {
//                cbAutoLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                        autoLoginChangeListener.onCheckedChanged(buttonView, isChecked);
//                    }
//                });
//            }

            dialog.setContentView(layout);

            return dialog;
        }
    }
}
