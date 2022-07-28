package com.lyentech.np;

import android.app.Application;

import com.lyentech.sdk.GreeNp;

/**
 * @author by jason-何伟杰，2022/5/25
 * des:全局默认进程Application
 */
public class NpApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //设备唯一id
        //np统计sdk初始化
//        String uniqueId = Settings.Secure.getString(getContentResolver(), "android_id");//唯一标识
//        GreeNp.init(this,"352CEF51DE68B0B7EBE1F4955A339A93",uniqueId,true);
        GreeNp.init(this, "6BD8A496699787957A8132C10020A872");
    }
}
