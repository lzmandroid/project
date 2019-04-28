package com.ctchat.sample.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ctchat.sample.R;


/**
 * 信息提示dialog
 */

public class CustomHintDialog extends Dialog {
    public CustomHintDialog(Context context) {
        super(context);
    }

    public CustomHintDialog(Context context, int theme) {
        super(context, theme);
    }

    public static class Builder {
        private Context context;
        private String title; // 对话框标题
        private String message; // 对话框内容
        private String backButtonText; // 对话框返回按钮文本
        private String confirmButtonText; // 对话框确定文本
        private View contentView;

        //对话框按钮监听事件
        private OnClickListener
                backButtonClickListener,
                confirmButtonClickListener;

        public Builder(Context context) {
            this.context = context;
        }

        //使用字符串设置对话框消息
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        //使用资源设置对话框消息
        public Builder setMessage(int message) {
            this.message = (String) context.getText(message);
            return this;
        }

        //使用资源设置对话框标题信息
        public Builder setTitle(int title) {
            this.title = (String) context.getText(title);
            return this;
        }

        //使用字符串设置对话框标题信息
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        //设置自定义的对话框内容
        public Builder setContentView(View v) {
            this.contentView = v;
            return this;
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

        //创建自定义的对话框
        public CustomHintDialog create() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // 实例化自定义的对话框主题
            final CustomHintDialog dialog = new CustomHintDialog(context, R.style.style_custom_dialog);
            View layout = inflater.inflate(R.layout.dialog_hint, null);
            dialog.addContentView(layout, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            // 设置对话框标题
            ((TextView) layout.findViewById(R.id.tv_hint_dialog_title)).setText(title);
            // 设置对话框内容
            if (message != null && !"".equals(message)) {
                TextView dlgMsg = (TextView) layout.findViewById(R.id.tv_hint_dialog_message);
                dlgMsg.setText(message);
            } else {
                // if no message set
                // 如果没有设置对话框内容，添加contentView到对话框主体
                ((RelativeLayout) layout.findViewById(R.id.content_message)).setVisibility(View.GONE);
            }

            // 设置返回按钮事件和文本
            if (backButtonText != null) {
                Button bckButton = ((Button) layout.findViewById(R.id.bt_hint_dialog_positive_button));
                bckButton.setText(backButtonText);

                if (backButtonClickListener != null) {
                    bckButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            backButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
                        }
                    });
                }
            } else {
                layout.findViewById(R.id.bt_hint_dialog_positive_button).setVisibility(View.GONE);
            }

            // 设置确定按钮事件和文本
            if (confirmButtonText != null) {
                Button cfmButton = ((Button) layout.findViewById(R.id.bt_hint_dialog_negative_button));
                cfmButton.setText(confirmButtonText);

                if (confirmButtonClickListener != null) {
                    cfmButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            confirmButtonClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                        }
                    });
                }
            } else {
                layout.findViewById(R.id.bt_hint_dialog_negative_button).setVisibility(View.GONE);
            }

            dialog.setContentView(layout);

            return dialog;
        }
    }
}
