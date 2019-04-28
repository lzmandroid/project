package com.ctchat.sample.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ctchat.sample.R;


/**
 * 自定义加载DiaLog
 */
public class CustomLoadingDialog {
    public static int REQUEST_LOADING_TIME = 15 * 1000;

    private Context context;
    private String msg;

    public CustomLoadingDialog(Context context, String msg) {
        this.context = context;
        this.msg = msg;
    }

    public CustomLoadingDialog(Context context, int msg) {
        this.context = context;
        this.msg = (String) context.getText(msg);
    }

    public Dialog createLoadingDialog() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.dialog_loading, null);
        // 得到加载view
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);
        // 加载布局
        // main.xml中的ImageView
        ImageView spaceshipImage = (ImageView) v.findViewById(R.id.iv_dialog_loading);
        TextView tipTextView = (TextView) v.findViewById(R.id.tv_dialog_loading_message);
        // 提示文字
        // 加载动画
        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
                context, R.anim.loading_animation);
        // 使用ImageView显示动画
        spaceshipImage.startAnimation(hyperspaceJumpAnimation);
        tipTextView.setText(msg);
        // 设置加载信息
        Dialog loadingDialog = new Dialog(context, R.style.style_loading_dialog);
        // 创建自定义样式dialog
        loadingDialog.setCancelable(false);
        // 不可以用“返回键”取消
        loadingDialog.setContentView(
                layout,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));// 设置布局
        return loadingDialog;
    }
}
