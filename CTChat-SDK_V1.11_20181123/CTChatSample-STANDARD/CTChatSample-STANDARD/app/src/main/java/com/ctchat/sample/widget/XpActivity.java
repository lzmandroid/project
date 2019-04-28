package com.ctchat.sample.widget;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View.OnClickListener;

import com.ctchat.sdk.basemodule.api.activity.XpReceiver;

public abstract class XpActivity extends AppCompatActivity implements OnClickListener {
    protected Context context;
    protected Resources resources;
    private XpReceiver receiver;
    public static final String KILL_RESTART = "kill_restart";
    public static final String ACTION_KILL_RESTART = "com.xunpin.basemodule.KILL_RESTART";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            boolean kill_restart = savedInstanceState.getBoolean(KILL_RESTART,false);
            if(kill_restart) {
                sendBroadcast(new Intent(ACTION_KILL_RESTART));
            }
        }
        context = getApplicationContext();
        resources = getResources();
        setContentView(getContentLayout());
        initView();
        initData();
        initReceiver();
    }

    private void initReceiver() {
        receiver = new XpReceiver(context, broadcastActions()) {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Auto-generated method stub
                doAction(intent);
            }
        };
    }

    protected abstract void initView();

    protected abstract void initData();

    protected abstract int getContentLayout();

    protected abstract String[] broadcastActions();

    protected abstract void doAction(Intent intent);

    /**
     * @param activity
     * @param bundle
     */
    protected void toActivity(Class<?> activity, Bundle bundle) {
        Intent intent = new Intent(context, activity);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    /**
     * @param pkgname
     * @param classname
     * @param bundle
     */
    protected void toActivty(String pkgname, String classname, Bundle bundle) {
        if (TextUtils.isEmpty(pkgname) || TextUtils.isEmpty(classname)) {
            return;
        }
        ComponentName componentName = new ComponentName(pkgname, classname);
        Intent intent = new Intent();
        intent.setComponent(componentName);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KILL_RESTART,true);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        receiver.recycle();
    }


}
