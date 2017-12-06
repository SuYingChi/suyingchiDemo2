package com.ihs.inputmethod.ads.fullscreen;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.ads.KCInterstitialAd;
import com.ihs.keyboardutils.iap.RemoveAdsManager;
import com.keyboard.core.session.KCKeyboardSession;

import net.appcloudbox.ads.base.AcbInterstitialAd;
import net.appcloudbox.ads.interstitialads.AcbInterstitialAdLoader;

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
        //此处屏蔽ad加载 因为OpenKeyboardFullScreenAd一个session只出现一次。
//        if (isConditionSatisfied()) {
//            KCInterstitialAd.load(placementName);
//        }
    }

    public boolean show() {
        if (isConditionSatisfied()) {
            boolean adShown = KCInterstitialAd.show(placementName, null, null, true);
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

    private static List<Integer> toIntegerList(List<?> objectList) {
        List<Integer> integerList = new ArrayList<>();
        for (Object object : objectList) {
            if (object instanceof Integer) {
                integerList.add((Integer) object);
            } else if (object instanceof String) {
                integerList.add(Integer.valueOf((String) object));
            }
        }
        return integerList;
    }


    private boolean isConditionSatisfied() {
        if (RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            return false;
        }

        boolean shouldShow = HSConfig.optBoolean(false, "Application", "InterstitialAds", "KeyboardAds", "Keyboard" + occasion, "Show");
        if (!shouldShow) {
            return false;
        }

        int sessionIndex = (int) KCKeyboardSession.getCurrentSessionIndexOfDay();

        long hitTime = prefs.getLong(PREF_KEY_PREFIX_AD_HIT_TIME + occasion, 0);
        long hitIndex;
        if (DateUtils.isToday(hitTime)) {
            hitIndex = prefs.getLong(PREF_KEY_PREFIX_AD_HIT_SESSION_INDEX + occasion, -1);
        } else {
            hitIndex = -1;
        }

        List<Integer> targetSessionIndexList = toIntegerList(HSConfig.getList("Application", "InterstitialAds", "KeyboardAds", "Keyboard" + occasion, "SessionIndexOfDay"));
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

    private static final String ONE_SESSION_ADPLACEMENT = HSApplication.getContext().getResources().getString(R.string.placement_full_screen_open_keyboard);

    /**
     * 这个ad 一个session只出现一次 即主页进入加载 直到退出程序只出现一次。
     */
    public static void loadSessionOneTimeAd() {
        if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            KCInterstitialAd.load(ONE_SESSION_ADPLACEMENT);
        }
    }

    /**
     * 这个ad 一个session只出现一次 即主页进入加载，直到退出程序只出现一次。
     * 1.开屏前
     * 2.进入theme详情页前
     * 3.自定义主题后（原来就有）
     * 4.facemoji制作后（loading动画之后）
     * 5.theme、wallpapper和call flash应用后
     * 6.退出app后
     */
    public static void showSessionOneTimeAd(String from) {
        if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            List<AcbInterstitialAd> fetch = AcbInterstitialAdLoader.fetch(HSApplication.getContext(), ONE_SESSION_ADPLACEMENT, 1);
            if (!fetch.isEmpty()) {
                fetch.get(0).show();
                HSAnalytics.logEvent("app_springAd_show", "from", from);
            }
        }
    }
}
