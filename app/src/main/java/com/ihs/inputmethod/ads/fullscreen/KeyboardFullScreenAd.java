package com.ihs.inputmethod.ads.fullscreen;

import android.preference.PreferenceManager;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.HSUIInputMethodService;
import com.ihs.inputmethod.uimodules.ui.theme.iap.IAPManager;
import com.ihs.keyboardutils.ads.KCInterstitialAd;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KeyboardFullScreenAd {
    private String placementName;

    public static final String SP_FULLSCREEN_AD_LOADED_ON_KEYBOARD_SESSIONS = "FullScreen_Ad_Loaded_On_KeyboardSession";

    private String occasion;

    public void preLoad() {
        // 满足加载条件
        if (isConditionSatisfied()) {
            KCInterstitialAd.load(placementName);
        }
    }

    public boolean show() {
        if (isConditionSatisfied()) {
            boolean adShown = KCInterstitialAd.show(placementName);
            if (adShown) {
                hasFetchedAd();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public KeyboardFullScreenAd(String placementName, final String occasion) {
        this.placementName = placementName;
        this.occasion = occasion;
    }

    protected boolean isConditionSatisfied() {
        if (IAPManager.getManager().hasPurchaseNoAds()) {
            return false;
        }

        // 1. Plist是否显示广告
        boolean shouldShow = HSConfig.optBoolean(false, "Application", "InterstitialAds", "KeyboardAds", "Keyboard" + occasion, "Show");
        if (!shouldShow) {
            return false;
        }
        // 2. 当前session所在组的index
        int sessionIndex = getCurrentSessionGroupIndex();
        if (sessionIndex == -1) {
            return false;
        }
        // 3. 如果当前session所在组的index已经加载过广告了，则return false， 反之，return true
        Set<String> sessions = PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).getStringSet(SP_FULLSCREEN_AD_LOADED_ON_KEYBOARD_SESSIONS, new HashSet<String>());
        if (!sessions.contains(sessionIndex + "")) {
            return true;
        }
        return false;
    }

    private void hasFetchedAd() {
        // 1. 当前session所在组的index
        int sessionIndex = getCurrentSessionGroupIndex();
        if (sessionIndex == -1) {
            return;
        }
        // 2. 当前已经显示过广告的组的index
        Set<String> sessions = PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).getStringSet(SP_FULLSCREEN_AD_LOADED_ON_KEYBOARD_SESSIONS, new HashSet<String>());

        // 3. 添加到sharepreference
        sessions.add("" + sessionIndex);
        PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).edit().putStringSet(SP_FULLSCREEN_AD_LOADED_ON_KEYBOARD_SESSIONS, sessions).apply();
    }

    private int getCurrentSessionGroupIndex() {
        // 1. 获取当前session索引
        int currentSessionIndex = KeyboardFullScreenAdSession.getKeyboardFullScreenAdSessionIndex();
        // 3. 获取plist允许弹出广告的session列表
        List<Integer> showAdSessionIndexs = (List<Integer>) HSConfig.getList("Application", "InterstitialAds", "KeyboardAds", "Keyboard" + occasion, "SessionIndexOfDay");
        int sessionTemp = -1;
        // 4. 查找比当前session索引小的索引
        if (showAdSessionIndexs != null) {
            Collections.sort(showAdSessionIndexs);

            for (int sessionIndex : showAdSessionIndexs) {
                if (currentSessionIndex >= sessionIndex) {
                    sessionTemp = sessionIndex;
                }
            }
        }
        return sessionTemp;
    }

    public static void resetKeyboardFullScreenAdSessions() {
        PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).edit().putStringSet(KeyboardFullScreenAd.SP_FULLSCREEN_AD_LOADED_ON_KEYBOARD_SESSIONS, new HashSet<String>()).apply();
    }
}
