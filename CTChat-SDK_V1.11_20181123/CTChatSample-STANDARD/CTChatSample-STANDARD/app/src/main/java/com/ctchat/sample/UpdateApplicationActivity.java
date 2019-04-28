package com.ctchat.sample;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;

import com.ctchat.sdk.basemodule.util.ToastUtil;
import com.ctchat.sample.widget.CustomHintDialog;
import com.ctchat.sdk.ptt.tool.versionUpdate.OnFileDownloadListener;
import com.ctchat.sdk.ptt.tool.versionUpdate.VersionUpdateManager;
import com.ctchat.sample.application.AppInitializeManager;
import com.ctchat.sample.application.WeApplication;
import com.ctchat.sample.widget.XpActivity;

import java.io.File;

public class UpdateApplicationActivity extends XpActivity implements OnFileDownloadListener {
    private static final String APK_SAVE_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + "hwptt";
    private static final String APK_NAME = "hwptt.apk";
    private static final String APK_SAVE_FULL_PATH = APK_SAVE_PATH + File.separator + APK_NAME;

    private ProgressBar processBar;
    private AlertDialog processDialog;
    private CustomHintDialog.Builder chooseBuilder;
    private AlertDialog.Builder forceBuilder;
    private int type;

    private static final String TAG = "UpdateApplicationActivity";


    @Override
    protected void initView() {
        VersionUpdateManager.getInstance().registerFileDownloadListener(this);
    }

    @Override
    protected void initData() {
        VersionUpdateManager.VersionEntity versionEntity = (VersionUpdateManager.VersionEntity) getIntent().getSerializableExtra(WeApplication.UPDATE_MESSAGE);
        type = versionEntity.grade;
        switch (type) {
            case VersionUpdateManager.GRADE_HIGH:
                showForceUpdateDialog(UpdateApplicationActivity.this, versionEntity);
                break;
            case VersionUpdateManager.GRADE_MEDIUM:
                showForceUpdateDialog(UpdateApplicationActivity.this, versionEntity);
                break;
            case VersionUpdateManager.GRADE_LOW:
                showChooseUpdateDialog(UpdateApplicationActivity.this, versionEntity);
                break;
            default:
                break;
        }
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_update_application;
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

    }

    /**
     * 强制升级对话框
     *
     * @param context
     */
    public void showForceUpdateDialog(final Context context, final VersionUpdateManager.VersionEntity versionEntity) {
    }

    /**
     * 选择升级对话框
     *
     * @param context
     */
    public void showChooseUpdateDialog(final Context context, final VersionUpdateManager.VersionEntity versionEntity) {
        chooseBuilder = new CustomHintDialog.Builder(context);

        CustomHintDialog dialog = chooseBuilder.create();
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                    finish();
                }
                return false;
            }
        });
        dialog.show();
    }

    private void showProcessDialog(Context context, VersionUpdateManager.VersionEntity versionEntity) {
        processDialog = new AlertDialog.Builder(context).create();
        processDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                }
                return false;
            }
        });
        processDialog.setCancelable(false);
        processDialog.show();
        //processDialog.setContentView(view);
        VersionUpdateManager.getInstance().downloadApk(versionEntity.verUrl,APK_SAVE_PATH,APK_NAME);
    }

    @Override
    public void onDownloadProgress(int progress) {
        processBar.setProgress(progress);
    }

    @Override
    public void onDownloadRet(int ret) {
        if (ret == VersionUpdateManager.DOWN_RET_OK) {
            VersionUpdateManager.getInstance().installApk(UpdateApplicationActivity.this,APK_SAVE_FULL_PATH);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.shortShow(UpdateApplicationActivity.this, R.string.download_failure);
                    if (processDialog != null) {
                        processDialog.dismiss();
                    }
                    finish();
                    if (type == VersionUpdateManager.GRADE_HIGH) {
                        AppInitializeManager.exitApplication();
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VersionUpdateManager.getInstance().unregisterFileDownloadListener();
    }
}
