package com.ihs.inputmethod.adpanel;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.keyboardutils.iap.RemoveAdsManager;
import com.keyboard.core.session.KCKeyboardSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by yanxia on 2017/5/20.
 */

public class KeyboardPanelAdManager {
    private String occasion;
    private SharedPreferences prefs;

    public static final String PREF_KEY_PREFIX_AD_HIT_SESSION_INDEX = "AD_HIT_SESSION_INDEX_";
    public static final String PREF_KEY_PREFIX_AD_HIT_TIME = "AD_HIT_TIME_";

    public KeyboardPanelAdManager(final String occasion) {
        this.occasion = occasion;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext());
    }

    public void hasShowedAd() {
        long hitIndex = KCKeyboardSession.getCurrentSessionIndexOfDay();
        long hitTime = System.currentTimeMillis();

        prefs.edit().putLong(PREF_KEY_PREFIX_AD_HIT_SESSION_INDEX + occasion, hitIndex).apply();
        prefs.edit().putLong(PREF_KEY_PREFIX_AD_HIT_TIME + occasion, hitTime).apply();
    }

    public boolean isShowAdConditionSatisfied() {
        if (RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            return false;
        }

        // 1. Plist是否显示广告
        boolean shouldShow = HSConfig.optBoolean(false, "Application", "NativeAds", "Keyboard" + occasion, "Show");
        if (!shouldShow) {
            return false;
        }
        // 2. 当前session所在组的index
        int sessionIndex = (int) KCKeyboardSession.getCurrentSessionIndexOfDay();

        long hitTime = prefs.getLong(PREF_KEY_PREFIX_AD_HIT_TIME + occasion, 0);
        long hitIndex;
        if (DateUtils.isToday(hitTime)) {
            hitIndex = prefs.getLong(PREF_KEY_PREFIX_AD_HIT_SESSION_INDEX + occasion, -1);
        } else {
            hitIndex = -1;
        }

        List<Object> targetSessionIndexObjectList = (List<Object>) HSConfig.getList("Application", "NativeAds", "Keyboard" + occasion, "SessionIndexOfDay");
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
}
