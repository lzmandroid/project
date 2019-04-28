package com.ctchat.sample.login.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ctchat.sample.R;

/**
 * 自定义Dialog
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
        private boolean cancelable = true;
        private int gravity = Gravity.CENTER;
        private boolean showMessage = false;
        private boolean adaptiveWidth = false;
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
            showMessage = true;
            return this;
        }
        public Builder setadaptiveWidth(boolean adaptiveWidth){
            this.adaptiveWidth = adaptiveWidth;
            return this;
        }

        //使用资源设置对话框消息
        public Builder setMessage(int message) {
            this.message = (String) context.getText(message);
            return this;
        }
        public Builder setGravity(int gravity){
            this.gravity = gravity;
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

        public Builder setCancelable(boolean cancelable){
            this.cancelable = cancelable;
            return this;
        }
        /**
         * 设置back按钮的事件和文本
         */
        public Builder setBackButton(int backButtonText, OnClickListener listener) {
            this.backButtonText = (String)context.getText(backButtonText);
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
            this.confirmButtonText = (String)context.getText(confirmButtonText);
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
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // 实例化自定义的对话框主题
            final CustomHintDialog dialog = new CustomHintDialog(context, R.style.style_custom_dialog);
            View layout = inflater.inflate(R.layout.dialog_hint_dt, null);
//            dialog.addContentView(layout,new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            Window window = dialog.getWindow();
            //设置dialog在屏幕底部
            window.setGravity(gravity);
            //设置dialog弹出时的动画效果，从屏幕底部向上弹出
            window.setWindowAnimations(R.style.dialogStyle);
            window.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams lp = window.getAttributes();
            if(adaptiveWidth){
                //设置窗口宽度为充满全屏
                lp.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 260, context.getResources().getDisplayMetrics());
            }else {
                //设置窗口宽度为充满全屏
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            }
            //设置窗口高度为包裹内容
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            //将设置好的属性set回去
            window.setAttributes(lp);
            //将自定义布局加载到dialog上
            dialog.setContentView(layout);
            // 设置对话框标题
            ((TextView) layout.findViewById(R.id.title)).setText(title);
            // 设置对话框内容
            if(showMessage){
                ((LinearLayout) layout.findViewById(R.id.content_message)).setVisibility(View.VISIBLE);
            }
            if (!TextUtils.isEmpty(message)) {
                TextView dlgMsg = (TextView)layout.findViewById(R.id.message);
                dlgMsg.setText(message);
            } else if (contentView != null) {
                // if no message set
                // 如果没有设置对话框内容，添加contentView到对话框主体
                ((LinearLayout) layout.findViewById(R.id.content_message)).removeAllViews();
                ((LinearLayout) layout.findViewById(R.id.content_message)).addView(
                        contentView, new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
            }

            // 设置返回按钮事件和文本
            if (backButtonText != null) {
                Button bckButton = ((Button) layout.findViewById(R.id.positiveButton));
                bckButton.setText(backButtonText);

                if (backButtonClickListener != null) {
                    bckButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            backButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
                        }
                    });
                }
            } else {
                layout.findViewById(R.id.positiveButton).setVisibility(View.GONE);
            }

            // 设置确定按钮事件和文本
            if (confirmButtonText != null) {
                Button cfmButton = ((Button) layout.findViewById(R.id.negativeButton));
                cfmButton.setText(confirmButtonText);

                if (confirmButtonClickListener != null) {
                    cfmButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            confirmButtonClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                        }
                    });
                }
            } else {
                layout.findViewById(R.id.negativeButton).setVisibility(View.GONE);
            }

            dialog.setContentView(layout);
            dialog.setCancelable(cancelable);
            return dialog;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
