package com.acb.adcaffe.common;

import android.content.Context;

import com.ihs.app.framework.HSApplication;

/**
 * Created by Arthur on 2018/1/31.
 */

public class UserDataUtils {
    public static String getPackageName(Context context) {
        return HSApplication.getContext().getPackageName();
    }

    public static String getOsVersion() {
        return "4.0";
    }

    public static String getCountry() {
        return "en";
    }

    public static String getNetwork(Context context) {
        return "3g";
    }

    public static boolean isPackageInstalled(Object packageName) {
        return false;
    }
}
