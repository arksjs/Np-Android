package com.lyentech.sdk;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.github.gzuliyujiang.oaid.DeviceIdentifier;

/**
 * @author by jason-何伟杰，2022/5/26
 * des:获取唯一标识
 */
public class DeviceIdUtil {

    public static String getUniqueId(Context context) {
        String uniqueId = getAndroidId(context);
        if (checkId(uniqueId)) {
            uniqueId = getOAID(context);
            if (checkId(uniqueId)) {
                uniqueId = getGUID(context);
                if (checkId(uniqueId)) {
                    uniqueId = getWidevineId(context);
                    if (checkId(uniqueId)) {
                        uniqueId = getImei(context);
                        if (checkId(uniqueId)) {
                            uniqueId = getPseudoId();
                        }
                    }
                }
            }
        }
        return uniqueId;
    }

    public static boolean checkId(String uniqueId) {
        if (TextUtils.isEmpty(uniqueId) || "null".equals(uniqueId)) {
            return true;
        }
        return false;
    }

    public static String getPlatForm() {
        String os = "Android";
        if (TextUtils.isEmpty(os)) {
            return "Android";
        } else {
            if (isHarmonyOs()) {
                os = "HarmonyOS";
            } else {
                os = "Android";
            }
            GreeNp.printLog("os=" + os);
            return os;
        }
    }

    public static boolean isHarmonyOs() {
        try {
            Class<?> buildExClass = Class.forName("com.huawei.system.BuildEx");
            Object osBrand = buildExClass.getMethod("getOsBrand").invoke(buildExClass);
            return "Harmony".equalsIgnoreCase(osBrand.toString());
        } catch (Throwable x) {
            return false;
        }
    }

    public static String getImei(Context context) {
        return DeviceIdentifier.getIMEI(context);
    }

    public static String getAndroidId(Context context) {
        return DeviceIdentifier.getAndroidID(context);
    }

    public static String getWidevineId(Context context) {
        return DeviceIdentifier.getWidevineID();
    }

    public static String getPseudoId() {
        return DeviceIdentifier.getPseudoID();
    }

    public static String getGUID(Context context) {
        return DeviceIdentifier.getGUID(context);
    }

    public static String getOAID(Context context) {
        return DeviceIdentifier.getOAID(context);
    }
}
