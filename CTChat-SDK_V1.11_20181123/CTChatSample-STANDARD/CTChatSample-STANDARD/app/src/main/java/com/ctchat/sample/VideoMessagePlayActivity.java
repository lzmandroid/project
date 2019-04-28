package com.ctchat.sample;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.ctchat.sdk.basemodule.util.ToastUtil;
import com.ctchat.sample.R;
import com.ctchat.sample.widget.XpActivity;

import java.io.File;

public class VideoMessagePlayActivity extends XpActivity {

    private VideoView videoView;
    private ImageView ivExitPlay;
    private MediaController mController;
    private String videoPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playVideo(videoPath);
    }

    @Override
    protected void initView() {
        videoView = (VideoView) findViewById(R.id.video_play_view);
        ivExitPlay = (ImageView) findViewById(R.id.iv_exit_play);
        ivExitPlay.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        videoPath = getIntent().getStringExtra("videoPath");
        mController = new MediaController(this);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_play_video_message;
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
            case R.id.iv_exit_play:
                finish();
                break;
        }
    }

    /**
     * 播放视频
     * @param videoPath 视频地址
     */
    private void playVideo(String videoPath) {
        if (!videoPath.isEmpty()) {
            File videoFile = new File(videoPath);
            if (videoFile.exists()) {
                videoView.setVideoPath(videoFile.getAbsolutePath());
                videoView.setMediaController(mController);
                videoView.start();
                videoView.requestFocus();
            }
        } else {
            ToastUtil.shortShow(context, getString(R.string.videopath_empty));
            finish();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase)
    {
        super.attachBaseContext(new ContextWrapper(newBase)
        {
            @Override
            public Object getSystemService(String name)
            {
                // 防止VideoView内部的AudioManager会持有Activity的context造成内存泄漏
                if (Context.AUDIO_SERVICE.equals(name))
                    return getApplicationContext().getSystemService(name);

                return super.getSystemService(name);
            }
        });
    }
}
