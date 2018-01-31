package com.ihs.commons.utils;

import android.content.Context;

/**
 * Created by Arthur on 18/1/17.
 */

public class HSPreferenceHelper {
    private static HSPreferenceHelper aDefault;

    public static HSPreferenceHelper getDefault() {
        return aDefault = new HSPreferenceHelper();
    }

    public void putLong(String name, long a) {

    }

    public long getLong(String s, long now) {
        return 0;
    }

    public void remove(String prefKey) {

    }

    public void putInt(String prefKey, int versionCode) {
    }

    public int getInt(String prefKey, int i) {
        return 0;
    }

    public String getString(String prefKeyLastDisplayedClipboardContent, String s) {
        return "";
    }

    public void putString(String prefKeyLastDisplayedClipboardContent, String content) {
    }

    public static HSPreferenceHelper create(Context context, String prefsCustomThemeFilename) {
        return new HSPreferenceHelper();
    }

    public void putBoolean(String hasSaveCustomThemeCommonToLocal, boolean b) {
    }

    public boolean getBoolean(String hasSaveCustomThemeCommonToLocal, boolean b) {
    return false;
    }

    public static HSPreferenceHelper getDefault(Context context) {
        return new HSPreferenceHelper();
    }

    public boolean contains(String prefKeyUserSetChargingToggle) {
        return false;
    }

    public long getLongInterProcess(String key, long defaultValue) {
        return defaultValue;
    }
    public void putLongInterProcess(String key, long value) {
    }
}
