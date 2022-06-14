package com.lyentech.sdk;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

/**
 * @author by jason-何伟杰，2022/5/17
 * des:sdk管理类
 */
public class GreeNp {
    private static Application mApplication;
    private static String uniqueId;
    private static String applicationKey;
    private static String mAndroidOs = "Android";
    private static boolean isDebugLog;

    public static void init(Application application, String appKey) {
        init(application, appKey, DeviceIdUtil.getUniqueId(application.getApplicationContext()));
    }

    public static void init(Application application, String appKey, String uniqueDevId) {
        init(application, appKey, uniqueDevId, true);
    }

    public static void init(Application application, String appKey, String uniqueDevId, boolean isDebug) {
        mApplication = application;
        applicationKey = appKey;
        uniqueId = uniqueDevId;
        isDebugLog = isDebug;
        mAndroidOs = DeviceIdUtil.getPlatForm();
//        new MMKVUtil.Builder().setSavePath(application.getExternalFilesDir("np_dir").getPath()).build();
        new MMKVUtil.Builder().setSavePath("").build();//上面无法打开 np_dir文件夹
        MMKVUtil.remove(NpConfig.PUBLIC_FRONT_RUN); //app启动重置
        MMKVUtil.remove(NpConfig.PUBLIC_LAST_TRACK);
        MMKVUtil.addStr(NpConfig.PUBLIC_APP, applicationKey);
        if (mApplication == null) {
            printLog("np——sdk arg 'appContext' should not be null!");
        }
        application.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacksImpl());
    }

    public static void onStart(Activity activity) { //首页前台显示
        if (MMKVUtil.getLong(NpConfig.PUBLIC_FRONT_RUN, 0L) == 0L)
            MMKVUtil.addLong(NpConfig.PUBLIC_FRONT_RUN, System.currentTimeMillis());
    }

    public static void onStop(Activity activity) { //当前所有界面都不显示
        long curTime = System.currentTimeMillis();
        long period = curTime - MMKVUtil.getLong(NpConfig.PUBLIC_FRONT_RUN, 0L);
        if ((period > 4000) && (period != curTime)) {//短于5秒无效
            postStayLiving(period);
        }
    }

    //子线程进行网络请求-统计app前台显示
    private static void postStayLiving(long period) {
        MMKVUtil.remove(NpConfig.PUBLIC_FRONT_RUN);
        MMKVUtil.addLong(NpConfig.PUBLIC_LAST_TRACK, System.currentTimeMillis());
        JSONObject js = new JSONObject();
        try {
            js.put("_default", period / 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String urlArg = NpConfig.PUBLIC_URL + URIEncoder.encodeURI(
                    getNpArg("/1", "/launch", "ev", "_stay", js.toString()));
            printLog("url_stay=" + urlArg);
            NpHttpUtil.getInstance().getAsync(urlArg, null);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    //搜索事件统计
    private static void trackSearch(String keyWord) {
        JSONObject js = new JSONObject();
        try {
            js.put("_default", isValidStr(keyWord));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        trackEvent("/1", "", "ev", "_search", js.toString());
    }

    //自定义事件统计
    public static void trackEvent(String eventKey) {
        trackEvent("/1", "", "ev", eventKey, null);
    }

    public static void trackEvent(String eventKey, String value) {
        trackEvent("/1", "", "ev", eventKey, value);
    }

    //子线程请求-单个事件上报
    public static void trackEvent(String dsc, String src, String tp, String ev, String evv) {
        String evvArg = "{}";
        JSONObject jsonObject = null;
        try {
            if (!TextUtils.isEmpty(evv)) {
                if (evv.contains("\"") && evv.contains(":")) {//值是json结构
                    evvArg = evv;
                } else { //非标准json结构属性值，后台无法解析，重构成json
                    jsonObject = new JSONObject();
                    jsonObject.put("_default", evv);
                    evvArg = jsonObject.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String urlArg = NpConfig.PUBLIC_URL + URIEncoder.encodeURI(getNpArg(dsc, src, tp, ev, evvArg));
            printLog("urlArg=" + urlArg);
            NpHttpUtil.getInstance().getAsync(urlArg, new NpHttpUtil.NetCall() {
                @Override
                public void success(Call call, Response response) throws IOException {
                    try {
                        printLog("trackEvent_" + response.body().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void failed(Call call, IOException e) {
                    printLog("trackEvent_err_" + e);
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    //将公共参数重构成接口的链接
    public static String getNpArg(String dsc, String src, String tp, String ev, String evv) {
        JSONObject js = setNpBody(dsc, src, tp, ev, evv);
        StringBuilder sb = new StringBuilder();
        Iterator iterator = js.keys();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            try {
                if ("evv".equals(key)) { //也就这个要处理255长度
                    sb.append(key + "=" + isValidStr(js.getString(key)) + "&");
                } else {
                    sb.append(key + "=" + js.getString(key) + "&");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        printLog("NP>>" + sb);
        return sb.toString();
    }

    //设置公共参数
    public static JSONObject setNpBody(String dsc, String src, String tp, String ev, String evv) {
        JSONObject js = new JSONObject();
        try {
            String ak=applicationKey;
            if (TextUtils.isEmpty(applicationKey)) {
                ak = MMKVUtil.getStr(NpConfig.PUBLIC_APP);
            }
            js.put("ak",ak);
            js.put("u", dsc);
            js.put("pf", mAndroidOs);
            js.put("rf", src);
            js.put("sys", "Android " + Build.VERSION.RELEASE);
            js.put("br", "" + Build.BRAND);
            js.put("brv", "" + Build.MODEL);
            int w = 1080, h = 1920;
            DisplayMetrics dm = mApplication.getResources().getDisplayMetrics();
            w = dm.widthPixels;
            h = dm.heightPixels;
            js.put("sr", w + "x" + h);
            if (TextUtils.isEmpty(uniqueId)) {
                uniqueId = DeviceIdUtil.getUniqueId(mApplication);
            }
            js.put("uuid", uniqueId);
            js.put("rnd", System.currentTimeMillis());
            js.put("tp", tp);
            js.put("ev", ev);
            js.put("evv", evv);
            js.put("xy", null);
            js.put("v", "0.0.1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        printLog("js="+js);
        return js;
    }

    //当前进程在前台
    public static boolean isTopActivity(String packageName) {
        ActivityManager am = (ActivityManager) mApplication.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = am.getRunningAppProcesses();
        if (list == null || list.size() == 0) {
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo info : list) {
            if (info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                    info.processName == packageName) {
                return true;
            }
        }
        return false;
    }

    public static String getPackageName(Context context) {
        return context.getPackageName();
    }

    public static Context getNpContext() {
        return mApplication;
    }

    private static String isValidStr(String json) {
        if (json.length() > 255) {
            JSONObject jsonObject = null;
            JSONObject jsonTmp = new JSONObject();
            try {
                jsonObject = new JSONObject(json);
                Iterator iterator = jsonObject.keys();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    jsonTmp.put(key, jsonObject.getString(key));
                    if (jsonTmp.toString().length() > 255) {
                        jsonTmp.remove(key);
                        GreeNp.printLog("截断属性值长度超过255》" + json);
                        return jsonTmp.toString();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return json.substring(0, 255);//非json文本直接返回
            }
        }
        return json;

    }

    public static void printLog(String log) {
        printLog("GreeNp", log);
    }

    public static void printLog(String tag, String log) {
        if (isDebugLog)
            Log.v(tag, log + "");
    }
}
