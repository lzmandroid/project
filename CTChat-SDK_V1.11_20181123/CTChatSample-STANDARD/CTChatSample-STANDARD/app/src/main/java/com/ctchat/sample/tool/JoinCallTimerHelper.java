package com.ctchat.sample.tool;

import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArraySet;

public class JoinCallTimerHelper {
    private static JoinCallTimerHelper helper;
    private  Timer timer;
    private  Set<JoinCallTimerListener> listenerSet = new CopyOnWriteArraySet<>();
    private  JoinCallTimerTask timerTask;

    public void setListener(JoinCallTimerListener listener){
        listenerSet.add(listener);
    }
    public void removeListener(JoinCallTimerListener listener){
        listenerSet.remove(listener);
    }

    public static JoinCallTimerHelper getHelper(){
        if(helper == null){
            synchronized (JoinCallTimerHelper.class){
                if(helper == null){
                    helper = new JoinCallTimerHelper();
                }
            }
        }
        return helper;
    }
    public boolean isTiming(){
        if(timer!=null){
            return true;
        }
        return false;
    }

    /**
     * 释放定时器资源
     */
    public  void releaseTimer(){
        if(timer!=null){
            timer.cancel();
            timer = null;
        }
        if(timerTask!=null){
            timerTask.cancel();
            timerTask = null;
        }
        listenerSet.clear();
    }

    /**
     * 启动定时器
     * @param delayTime
     */
    public void startTimer(long delayTime){
        if(timerTask == null){
            timerTask = new JoinCallTimerTask();
        }
        if(timer == null){
            timer = new Timer();
            timer.schedule(timerTask,delayTime);
        }
    }

    private class  JoinCallTimerTask extends TimerTask{

        @Override
        public void run() {
            Iterator<JoinCallTimerListener> iterator = listenerSet.iterator();
            while (iterator.hasNext()){
                JoinCallTimerListener listener = iterator.next();
                listener.onJoinCallTimeOut();
            }
        }
    }

    public interface JoinCallTimerListener{
        void onJoinCallTimeOut();
    }
}
