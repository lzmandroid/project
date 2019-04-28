package com.ctchat.sample.login.util;

import android.os.CountDownTimer;

/**
 * 时间单位均为毫秒
 */
public class TimerReacquire extends CountDownTimer{

    private long rest;
    private TimerFinishListener listener;
    /**
     *
     * @param totalTime 计时总时间
     * @param interval  计时一次的时间间隔
     */
    public TimerReacquire(long totalTime, long interval) {
        super(totalTime, interval);
    }

    /**
     * 每隔interval时间，调用一次此方法
     * @param rest 剩余多久计时结束
     */
    @Override
    public void onTick(long rest) { // 每记时一次调用此方法
        this.rest = rest / 1000;
    }

    public long getRest() {
        return rest;
    }

    @Override
    public void onFinish() {
        if (listener != null) {
            listener.onTimerFinish();
        }
        rest = -1;
    }

    public void setOnTimerFinishListener(TimerFinishListener listener) {
        this.listener = listener;
    }

    public interface TimerFinishListener {
        void onTimerFinish();
    }
}
