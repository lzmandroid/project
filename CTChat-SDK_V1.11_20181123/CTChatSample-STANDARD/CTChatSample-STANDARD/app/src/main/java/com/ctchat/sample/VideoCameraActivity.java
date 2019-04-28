package com.ctchat.sample;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ctchat.sdk.basemodule.util.ToastUtil;
import com.ctchat.sample.R;
import com.ctchat.sdk.ptt.tool.chatIM.VideoRecording;
import com.ctchat.sample.util.TimeUtil;
import com.ctchat.sample.util.Util;
import com.ctchat.sample.widget.XpActivity;

import java.util.Timer;
import java.util.TimerTask;

public class VideoCameraActivity extends XpActivity implements View.OnClickListener, VideoRecording.VideoRecordingCallback {

    public static final String ISTIMING = "isTiming";
    private SurfaceView mSurfaceView;
    private ImageView ivRecordingSwitch, ivFlash, ivCameraSwitch, ivBack;
    private TextView tvClose, tvComplete, tvCameraTimer;
    private ProgressBar pgOne, pgTwo;

    private VideoRecording videoSession;

    private Timer timer;
    private TimerTask timerTask;
    private int mTimerNum = 0;
    private boolean isTiming = false;
    private long mStopTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoSession.setCallback(this);
//        fitScreen();
        videoSession.setSurfaceView(mSurfaceView);
        videoSession.setPreviewOrientation(getPreviewDegree(this));
        videoSession.startPreview();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 消息处理
     */
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Util.UPDATE_TIMER:
                    mTimerNum += 1;
                    String time = TimeUtil.getTimeFormatmmss((Long) msg.obj);
                    tvCameraTimer.setVisibility(View.VISIBLE);
                    tvCameraTimer.setText(time);
                    int status = mTimerNum * 100 / 30;
                    setProgressStatus(status);
                    if (mTimerNum >= Util.VIDEO_SECTION_TIME_MAX) {
                        videoSession.stopRecord();
                        cancleTimer();//取消定时器
                    }
                    break;
            }
        }
    };

    @Override
    protected void initView() {
        ivBack = (ImageView) findViewById(R.id.video_camera_back);
        mSurfaceView = (SurfaceView) findViewById(R.id.sv_view);
        tvCameraTimer = (TextView) findViewById(R.id.video_camera_timer);
        ivRecordingSwitch = (ImageView) findViewById(R.id.iv_movieRecorder);
        ivFlash = (ImageView) findViewById(R.id.iv_flash);
        ivCameraSwitch = (ImageView) findViewById(R.id.iv_switch_camera);
        tvClose = (TextView) findViewById(R.id.tv_close_movieRecorger);
        tvComplete = (TextView) findViewById(R.id.tv_complete_movieRecorger);
        pgOne = (ProgressBar) findViewById(R.id.pg_progressOne);
        pgTwo = (ProgressBar) findViewById(R.id.pg_progressTwo);

        ivBack.setOnClickListener(this);
        mSurfaceView.setOnClickListener(this);
        ivRecordingSwitch.setOnClickListener(this);
        ivFlash.setOnClickListener(this);
        ivCameraSwitch.setOnClickListener(this);
        tvClose.setOnClickListener(this);
        tvComplete.setOnClickListener(this);
        tvComplete.setVisibility(View.GONE);
        tvClose.setVisibility(View.GONE);
    }

    @Override
    protected void initData() {
        videoSession = VideoRecording.newInstance(VideoRecording.QUALITY_480P);
        Bundle bundle = this.getIntent().getExtras();
        isTiming = bundle.getBoolean(ISTIMING);

        showSwitchCameraButton(true);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_video_camera;
    }

    @Override
    protected String[] broadcastActions() {
        return new String[0];
    }

    @Override
    protected void doAction(Intent intent) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoSession.setCallback(null);
        videoSession.release();
        videoSession = null;
    }

    @Override
    public void finish() {
        super.finish();
        if (timer != null) {
            cancleTimer();//取消定时器
        }
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.video_camera_back:
                    cameraBack();
                    break;
                case R.id.iv_movieRecorder://录制开始和结束
                    if ((System.currentTimeMillis() - mStopTime) > 3000) {
                        //录制时间大于3S方可停止
                        mStopTime = System.currentTimeMillis();// 更新mStopTime
                        toggleStream();
                    } else {
                        ToastUtil.longShow(context, R.string.stop_movie_recorder);
                    }
                    break;
                case R.id.iv_flash://开关闪光灯
                    toggleFlash();
                    break;
                case R.id.iv_switch_camera://切换前后摄像头
                    videoSession.switchCamera();
                    break;
                case R.id.tv_close_movieRecorger://关闭按钮
                    if (videoSession.getState() == VideoRecording.STATE_STARTED || videoSession.getState() == VideoRecording.STATE_STARTING) {
                        videoSession.stopRecord();
                    }
                    Intent intent = new Intent();
                    intent.putExtra("videoPath", "");
                    setResult(RESULT_OK, intent);
                    finish();
                    break;
                case R.id.tv_complete_movieRecorger://完成按钮
                    String videoPath = videoSession.getVideoFilePath();
                    Intent data = new Intent();
                    data.putExtra("videoPath", videoPath);
                    setResult(RESULT_OK, data);
                    finish();
                    break;
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.camera_error), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 退出界面
     */
    private void cameraBack() {
        if (videoSession.getState() == VideoRecording.STATE_STARTED || videoSession.getState() == VideoRecording.STATE_STARTING) {
            videoSession.stopRecord();
            cancleTimer();
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        Intent back = new Intent();
        back.putExtra("videoPath", "");
        setResult(RESULT_OK, back);
        finish();
    }

    @Override
    public void onSessionError(int i, int i1, Exception e) {

    }

    @Override
    public void onPreviewStarted() {

    }

    @Override
    public void onCameraSwitched(int i) {
        if (i != Configuration.ORIENTATION_UNDEFINED) {
            videoSession.setOrientationHint(90);
            if (i == Configuration.ORIENTATION_PORTRAIT) {
                videoSession.setOrientationHint(270);
            }
        }
    }

    @Override
    public void onSessionStarted() {
        refreshStartButton();
    }

    @Override
    public void onSessionStopped() {
        refreshStartButton();
    }

    @Override
    public void onFlashToggle() {
        refreshFlashState();
    }

    /**
     * 设置进度条进度
     * @param status
     */
    private void setProgressStatus(int status) {
        pgOne.setProgress(status);
        pgTwo.setProgress(status);
    }

    /**
     * 录制/停止录制
     */
    public void toggleStream() {
        if (videoSession.getState() == VideoRecording.STATE_STOPPED) {
            videoSession.startRecord();
            if (isTiming) {
                setTimer(TimeUtil.getCurrTimeMillis());//设置定时器
            }
        } else {
            videoSession.stopRecord();
            cancleTimer();//取消定时器
        }
    }

    /**
     * 切换闪光灯开关
     */
    public void toggleFlash() {
        videoSession.toggleFlash();
    }

    /**
     * 刷新闪光灯图标
     */
    private void refreshFlashState() {
        if (videoSession.getFlashState()) {
            ivFlash.setImageResource(R.drawable.flash_close);
        } else {
            ivFlash.setImageResource(R.drawable.flash_open);
        }
    }

    /**
     * 控制摄像头转换按钮显示
     * @param show
     */
    private void showSwitchCameraButton(boolean show){
//        if (show && (videoSession.getNumberOfCameras() > 1)) {
//            ivCameraSwitch.setVisibility(View.VISIBLE);
//        }else {
//            ivCameraSwitch.setVisibility(View.GONE);
//        }
    }
    /**
     * 刷新录制按钮图标
     */
    public void refreshStartButton() {
        switch (videoSession.getState()) {
            case VideoRecording.STATE_STARTED:
                ivRecordingSwitch.setBackgroundResource(R.drawable.report_live_stop);
                ivFlash.setVisibility(View.GONE);
                showSwitchCameraButton(false);
                break;
            case VideoRecording.STATE_STARTING:
                break;
            case VideoRecording.STATE_STOPPED:
                ivRecordingSwitch.setVisibility(View.GONE);
                tvComplete.setVisibility(View.VISIBLE);
                tvClose.setVisibility(View.VISIBLE);
                ivFlash.setVisibility(View.VISIBLE);
                showSwitchCameraButton(true);
                tvCameraTimer.setVisibility(View.GONE);
                findViewById(R.id.ll_progress).setVisibility(View.GONE);
                break;
            case VideoRecording.STATE_STOPPING:
                break;
        }
    }

    /**
     * 根据手机方向获得相机预览画面旋转的角度
     *
     * @param activity
     * @return
     */
    public static int getPreviewDegree(Activity activity) {

        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degree = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 90;
                break;
            case Surface.ROTATION_90:
                degree = 0;
                break;
            case Surface.ROTATION_180:
                degree = 270;
                break;
            case Surface.ROTATION_270:
                degree = 180;
            default:
                break;
        }
        return degree;
    }

    /**
     * 适配画面尺寸
     */
    private void fitScreen() {
        mSurfaceView.post(new Runnable() {
            @Override
            public void run() {
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                int heightPixels = (displayMetrics).heightPixels;
                int widthPixels = (displayMetrics).widthPixels;
                int height = getVideoResolutionHeight();
                int width = getVideoResolutionWidth();
                ViewGroup.LayoutParams layoutParams = mSurfaceView.getLayoutParams();
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    if (heightPixels * height == width * widthPixels) {
                        layoutParams.width = -1;
                        layoutParams.height = -1;
                    } else if ((heightPixels == width) || (widthPixels == height)) {
                        layoutParams.width = widthPixels;
                        layoutParams.height = heightPixels;
                    } else {
                        layoutParams.width = widthPixels;
                        layoutParams.height = (width * widthPixels / height);
                    }
                } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    if (heightPixels * width == height * widthPixels) {
                        layoutParams.width = -1;
                        layoutParams.height = -1;
                    } else if ((heightPixels == height) || (widthPixels == width)) {
                        layoutParams.width = widthPixels;
                        layoutParams.height = heightPixels;
                    } else {
                        layoutParams.width = heightPixels;
                        layoutParams.height = (heightPixels * width / height);
                    }
                }
                mSurfaceView.setLayoutParams(layoutParams);
            }
        });
    }

    public static int getVideoResolutionHeight() {
        return  720;
    }

    public static int getVideoResolutionWidth() {
        return  1280;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                cameraBack();
                break;
        }
        return super.onKeyDown(keyCode, event);

    }

    /**
     * 设置定时器
     *
     * @param startTime
     */
    private void setTimer(final long startTime) {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Message msg = new Message();
                long currentTime = TimeUtil.getCurrTimeMillis();
                msg.obj = currentTime - startTime;
                msg.what = Util.UPDATE_TIMER;
                handler.sendMessage(msg);
            }
        };

        timer = new Timer();
        timer.schedule(timerTask, 1000, 1000);
    }

    private void cancleTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    protected void onPause() {
//        cameraBack();
        super.onPause();
        videoSession.stopRecord();
        cancleTimer();//取消定时器
    }
}
