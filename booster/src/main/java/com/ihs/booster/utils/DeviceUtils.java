package com.ihs.booster.utils;

import android.os.Build;

/**
 * Created by sharp on 16/4/5.
 */
public class DeviceUtils {
    public static boolean isMiui() {
        if (Build.MANUFACTURER.contains("MIUI") || Build.ID.contains("MIUI") || Build.MODEL.contains("MIUI") || Build.MODEL.contains("MI 3") || Build.MODEL.contains("MI 2S") || Build.MODEL.startsWith("MI-ONE")) {
            return true;
        }
        return isMiuiDisplay();
    }

    private static boolean isMiuiDisplay() {
        String str;
        try {
            str = Build.DISPLAY;
        } catch (NoSuchFieldError e) {
            e.printStackTrace();
            str = null;
        }
        if (str != null && str.toUpperCase().contains("MIUI")) {
            return true;
        }
        str = Build.MODEL;
        if (str != null && str.contains("MI-ONE")) {
            return true;
        }
        str = Build.DEVICE;
        if (str != null && str.contains("mione")) {
            return true;
        }
        str = Build.MANUFACTURER;
        if (str != null && str.equalsIgnoreCase("Xiaomi")) {
            return true;
        }
        str = Build.PRODUCT;
        if (str == null || !str.contains("mione")) {
            return false;
        }
        return true;
    }
}
