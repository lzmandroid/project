package com.ctchat.sample.application;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import com.ctchat.sample.tool.Sound;
import com.github.promeg.pinyinhelper.Pinyin;
import com.github.promeg.tinypinyin.lexicons.android.cncity.CnCityDict;
import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sdk.ptt.tool.media.MediaSoundManager;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.ctchat.sample.tool.media.MediaSound;

public class AppInitializeManager {
    private static final String TAG = "AppInitializeManager";

    private static Context mApplicationContext;
    private static OnAppInitListener mAppInitListener;

    public static Context getmApplicationContext() {
        return mApplicationContext;
    }

    public static void appInit(Context application, OnAppInitListener listener) {
        mApplicationContext = application;
        mAppInitListener = listener;

        Sound.soundInit(application);
        MediaSoundManager.getInstance().registerMediaSoundEventListener(new MediaSound(application));

        Logger.i(TAG, "APP ON CREATE", true);

        //初始化地图模块yInstance().initialize(application);
        //初始化ImageLoader
        initImageLoader(application);
        initPinyinCity(application);
    }

    private static void initImageLoader(Context context) {
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.memoryCacheSize(5 * 1024 * 1024);
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        config.writeDebugLogs();

        ImageLoader.getInstance().init(config.build());
    }

    private static void initPinyinCity(Context context) {
        // 添加中文城市词典
        Pinyin.init(Pinyin.newConfig().with(CnCityDict.getInstance(context)));
    }

    public static void exitApplication() {
        //退出应用时关闭话权指示灯

        if (mAppInitListener != null) {
            mAppInitListener.exitApplication();
        }
    }
}
