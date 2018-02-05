package com.ihs.inputmethod.feature.common;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.inputmethod.uimodules.BuildConfig;

import java.util.Map;

/**
 * A wrapper over {@link HSConfig} to provide multilingual and multi-APK support.
 */
@SuppressWarnings("unchecked")
public class LauncherConfig {

// --Commented out by Inspection START (18/1/11 下午2:41):
//    /**
//     * Wraps {@link com.ihs.commons.config.HSConfig#getString(String...)} and added multilingual support.
//     *
//     * @param path Config path
//     */
//    public static String getMultilingualString(String... path) {
//        String configString;
//        try {
//            configString = HSConfig.getString(path);
//            if (configString.isEmpty()) {
//                Map<String, String> stringMap = (Map<String, String>) HSConfig.getMap(path);
//                configString = getStringForCurrentLanguage(stringMap);
//            }
//        } catch (Exception expected) {
//            Map<String, String> stringMap = (Map<String, String>) HSConfig.getMap(path);
//            configString = getStringForCurrentLanguage(stringMap);
//        }
//        return configString;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    public static String getMultilingualString(Map<String, ?> map, String key) {
        String configString;
        try {
            configString = (String) map.get(key);
        } catch (ClassCastException expected) {
            Map<String, String> stringMap = (Map<String, String>) map.get(key);
            configString = getStringForCurrentLanguage(stringMap);
        }
        return configString;
    }

    public static String getStringForCurrentLanguage(Map<String, String> stringMap) {
        String language = CommonUtils.getLocale(HSApplication.getContext()).getLanguage();
        String localeString = stringMap.get(language);
        if (localeString == null) {
            localeString = stringMap.get("Default");
        }
        return localeString;
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public static boolean getVariantBoolean(String... path) {
//        boolean configBool;
//        try {
//            configBool = HSConfig.getBoolean(path);
//        } catch (Exception expected) {
//            Map<String, Boolean> boolMap = (Map<String, Boolean>) HSConfig.getMap(path);
//            configBool = Boolean.TRUE.equals(boolMap.get(BuildConfig.FLAVOR));
//        }
//        return configBool;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public static int getVariantInt(String... path) {
//        int configInt;
//        try {
//            configInt = HSConfig.getInteger(path);
//        } catch (Exception expected) {
//            Map<String, Integer> intMap = (Map<String, Integer>) HSConfig.getMap(path);
//            configInt = intMap.get(BuildConfig.FLAVOR);
//        }
//        return configInt;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public static float getVariantFloat(String... path) {
//        float configFloat;
//        try {
//            configFloat = HSConfig.getFloat(path);
//        } catch (Exception expected) {
//            //noinspection unchecked
//            Map<String, Integer> intMap = (Map<String, Integer>) HSConfig.getMap(path);
//            configFloat = intMap.get(BuildConfig.FLAVOR);
//        }
//        return configFloat;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)
}
