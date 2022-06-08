package com.lyentech.sdk;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author by jason-何伟杰，2022/5/26
 * des:监听Activity生命变化，只有一个页面进程前台判断不够，这种方式较好
 */
public class ActivityLifecycleCallbacksImpl implements Application.ActivityLifecycleCallbacks {

    private int activityTopCount = 0;

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        GreeNp.printLog("onActivityCreated>>" + activity);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        activityTopCount++;
        GreeNp.printLog("onActivityStarted>>" + activity+" /"+activityTopCount+" $"+(System.currentTimeMillis() - MMKVUtil.getLong(NpConfig.PUBLIC_LAST_TRACK, 0L)));
        //数值从0变到1说明是从后台切到前台
        if (activityTopCount == 1) {
            //需要记录页面开始时间
            MMKVUtil.addLong(NpConfig.PUBLIC_FRONT_RUN, System.currentTimeMillis());
            ////每次启动定义：首次启动、后台运行30s后再打开
            if (System.currentTimeMillis() - MMKVUtil.getLong(NpConfig.PUBLIC_LAST_TRACK, 0L) > 30 * 1000) {
                //存在不停创建新页面的情景  数值从0变到1说明是从后台切到前台
                GreeNp.trackEvent("/1", "/launch", "pv", null, null);
                GreeNp.trackEvent("/1","/launch","lv",null,null);
            } else {
                GreeNp.trackEvent("/1", activity.getClass().toString(), "pv", null, null);
                GreeNp.onStart(activity);
            }
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        GreeNp.printLog("onActivityResumed>>" + activity);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        GreeNp.printLog("onActivityPaused>>" + activity);
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        activityTopCount--;
        //数值从1到0说明是从前台切到后台
        if (activityTopCount == 0) {
            GreeNp.onStop(activity);
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        GreeNp.printLog("onActivityDestroyed>>" + activity);
    }
}
