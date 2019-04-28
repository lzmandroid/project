package com.ctchat.sample.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * BaseFragment
 */
public abstract class BaseFragment extends Fragment {
    //初始化数据
    public abstract void initData();
    //初始化控件
    public abstract void initView(View view);

    /**
     * 跳转Activity
     * @param context
     * @param activity
     * @param bundle
     */
    protected void toActivity(Context context, Class<?> activity, Bundle bundle) {
        Intent intent = new Intent(context, activity);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }
}
