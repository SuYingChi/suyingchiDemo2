package com.ihs.commons.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Arthur on 18/1/17.
 */

public class HSMapUtils {
    public static String getString(Map<String, ?> o, String rmtcfgKeyL4PackageName) {
        return "";
    }

    public static Integer getInteger(Map<String, ?> o, String rmtcfgKeyL4SendMode) {
        return 0;
    }

    public static Map<String, ?> getMap(Map<String, ?> configMap, String[] path) {
        return new HashMap<>();
    }

    public static List<?> getList(Map<String, ?> configMap, String[] path) {
        return new ArrayList<>();
    }

    public static List<?> getList(Map<String, ?> configMap, String path) {
        return new ArrayList<>();
    }

    public static String optString(Map<String, ?> configMap, String defaultValue, String[] path) {
        return defaultValue;
    }

    public static int optInteger(Map<String, ?> configMap, int defaultValue, String[] path) {
        return defaultValue;
    }

    public static boolean optBoolean(Map<String, ?> configMap, boolean defaultValue, String[] path) {
        return defaultValue;
    }

    public static List<?> getList(Map<String, Object> stickerMap, String keyL0Data, String keyL1Application, String keyL2Sticker) {
        return new ArrayList<>();
    }

    public static int getInteger(Map<String, ?> mStyleMap, String size, String width) {
        return 0;
    }

    public static float optFloat(Map<String, ?> l, int i, String transform, String translateX) {
        return 0;
    }
}
