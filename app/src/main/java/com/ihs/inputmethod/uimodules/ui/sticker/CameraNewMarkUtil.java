package com.ihs.inputmethod.uimodules.ui.sticker;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ihs.app.framework.HSApplication;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Arthur on 2018/2/9.
 */

class CameraNewMarkUtil {
    private static final String PREF_KET_VISITED_FEATURE_SET_PREFIX = "sp_key_visited_set_";
    public static final String TAG_STICKER = "sticker";

    public static void setElementVisited(String featureTag, String elementName) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> visitedElementSet = new HashSet<>(sharedPreferences.getStringSet(PREF_KET_VISITED_FEATURE_SET_PREFIX + featureTag, new HashSet<>()));
        visitedElementSet.add(elementName);
        editor.putStringSet(PREF_KET_VISITED_FEATURE_SET_PREFIX + featureTag, visitedElementSet);
        editor.apply();
    }
}
