package com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by xiayan on 2017/9/17.
 */

public class CustomThemeUnlockManager {
    private static final CustomThemeUnlockManager ourInstance = new CustomThemeUnlockManager();
    private static final String PREF_KEY_RATE_TO_UNLOCK = "pref_rate_to_unlock_set";

    private Set<String> needNewVersionToUnlockItemSet;

    public static CustomThemeUnlockManager getInstance() {
        return ourInstance;
    }

    private CustomThemeUnlockManager() {
        needNewVersionToUnlockItemSet = new HashSet();
        loadNeedNerVersionToUnlockSet();
    }

    public void onConfigChange() {
        loadNeedNerVersionToUnlockSet();
    }

    private synchronized void loadNeedNerVersionToUnlockSet() {
        List<String> needNewVersionList = (List<String>) HSConfig.getList("Application", "ThemeContents", "custom_theme_needNewVersionToUnlock");
        needNewVersionToUnlockItemSet.clear();
        needNewVersionToUnlockItemSet.addAll(needNewVersionList);
    }

    private synchronized void loadRateToUnlockSet() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext());
        List<String> needRateToUnlockList = (List<String>) HSConfig.getList("Application", "ThemeContents", "custom_theme_needRateToUnlock");
        Set<String> allNeedRateToUnlockSet = new HashSet<>(sharedPreferences.getStringSet(PREF_KEY_RATE_TO_UNLOCK, new HashSet<String>()));
    }

    public boolean isElementNeedNewAppVersionToUnlock(String elementName) {
        return needNewVersionToUnlockItemSet.contains(elementName);
    }

    public boolean isElementNeedRateToUnlock(String elementName) {
        Set<String> needRateToUnlockSet = PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).getStringSet(PREF_KEY_RATE_TO_UNLOCK, new HashSet<String>());
        return needRateToUnlockSet.contains(elementName);
    }
}
