package com.ihs.inputmethod.ads.fullscreen;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.inputmethod.uimodules.ui.theme.iap.IAPManager;
import com.ihs.keyboardutils.ads.KCInterstitialAd;
import com.keyboard.core.session.KCKeyboardSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KeyboardFullScreenAd {
    private String placementName;

    public static final String SP_FULLSCREEN_AD_LOADED_ON_KEYBOARD_SESSIONS = "FullScreen_Ad_Loaded_On_KeyboardSession";

    public static final String PREF_KEY_PREFIX_AD_HIT_SESSION_INDEX = "AD_HIT_SESSION_INDEX_";
    public static final String PREF_KEY_PREFIX_AD_HIT_TIME = "AD_HIT_TIME_";

    private String occasion;

    private SharedPreferences prefs;

    public void preLoad() {
        // 满足加载条件
        if (isConditionSatisfied()) {
            KCInterstitialAd.load(placementName);
        }
    }

    public boolean show() {
        if (isConditionSatisfied()) {
            boolean adShown = KCInterstitialAd.show(placementName, null, true);
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
        this.prefs = PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext());
    }

    protected boolean isConditionSatisfied() {
        if (IAPManager.getManager().hasPurchaseNoAds()) {
            return false;
        }

        boolean shouldShow = HSConfig.optBoolean(false, "Application", "InterstitialAds", "KeyboardAds", "Keyboard" + occasion, "Show");
        if (!shouldShow) {
            return false;
        }

        int sessionIndex = (int)KCKeyboardSession.getCurrentSessionIndexOfDay();

        long hitTime = prefs.getLong(PREF_KEY_PREFIX_AD_HIT_TIME + occasion, 0);
        long hitIndex;
        if (DateUtils.isToday(hitTime)) {
            hitIndex = prefs.getLong(PREF_KEY_PREFIX_AD_HIT_SESSION_INDEX + occasion, -1);
        } else {
            hitIndex = -1;
        }

        List<Object> targetSessionIndexObjectList = (List<Object>) HSConfig.getList("Application", "InterstitialAds", "KeyboardAds", "Keyboard" + occasion, "SessionIndexOfDay");
        List<Integer> targetSessionIndexList = new ArrayList<>();
        for (Object targetSessionIndexObject : targetSessionIndexObjectList) {
            if (targetSessionIndexObject instanceof Integer) {
                targetSessionIndexList.add((Integer) targetSessionIndexObject);
            } else if (targetSessionIndexObject instanceof String) {
                targetSessionIndexList.add(Integer.valueOf((String) targetSessionIndexObject));
            }
        }
        Collections.sort(targetSessionIndexList);

        for (int targetSessionIndex : targetSessionIndexList) {
            if (sessionIndex >= targetSessionIndex && hitIndex < targetSessionIndex) {
                return true;
            }
        }

        return false;
    }

    private void hasFetchedAd() {
        long hitIndex = KCKeyboardSession.getCurrentSessionIndexOfDay();
        long hitTime = System.currentTimeMillis();

        prefs.edit().putLong(PREF_KEY_PREFIX_AD_HIT_SESSION_INDEX + occasion, hitIndex).apply();
        prefs.edit().putLong(PREF_KEY_PREFIX_AD_HIT_TIME + occasion, hitTime).apply();

    }
}
