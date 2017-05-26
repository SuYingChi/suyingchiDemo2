package com.ihs.inputmethod.adpanel;

import android.preference.PreferenceManager;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.inputmethod.uimodules.ui.theme.iap.IAPManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by yanxia on 2017/5/20.
 */

public class KeyboardPanelAdManager {
    private static final String SP_KEYBOARD_CURRENT_EMOJI_SESSION = "sp_keyboard_current_emoji_session";
    private static final String SP_KEYBOARD_INPUT_VIEW_START_COUNT = "sp_keyboard_input_view_start_count";
    private static final String SP_FUNCTION_BAR_AD_SHOWED_ON_KEYBOARD_SESSIONS = "sp_function_bar_ad_showed_on_keyboard_sessions";

    public static void addEmojiPanelShowCount() {
        int sessionIndex = PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).getInt(SP_KEYBOARD_CURRENT_EMOJI_SESSION, 0);
        PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).edit().putInt(SP_KEYBOARD_CURRENT_EMOJI_SESSION, sessionIndex + 1).apply();
    }

    private static int getKeyboardEmojiPanelSessionIndex() {
        return PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).getInt(SP_KEYBOARD_CURRENT_EMOJI_SESSION, -1);
    }

    public static void addInputViewStartCount() {
        int sessionIndex = PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).getInt(SP_KEYBOARD_INPUT_VIEW_START_COUNT, -1);
        PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).edit().putInt(SP_KEYBOARD_INPUT_VIEW_START_COUNT, sessionIndex + 1).apply();
    }

    private static int getInputViewStartCount() {
        return PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).getInt(SP_KEYBOARD_INPUT_VIEW_START_COUNT, -1);
    }

    public static boolean isShowEmojiAdConditionSatisfied() {
        if (IAPManager.getManager().hasPurchaseNoAds()) {
            return false;
        }

        // 1. Plist是否显示广告
        boolean shouldShow = HSConfig.optBoolean(false, "Application", "NativeAds", "KeyboardEmojiTopLeftTabAd", "Show");
        if (!shouldShow) {
            return false;
        }
        // 2. 当前session所在组的index
        int sessionIndex = getEmojiSessionGroup();
        if (sessionIndex == -1) {
            return false;
        }
        return true;
    }

    private static int getEmojiSessionGroup() {
        // 1. 获取当前session
        int currentSessionIndex = getKeyboardEmojiPanelSessionIndex();
        // 2. 获取plist允许弹出广告的session列表
        List<Integer> showAdSessionIndexs = (List<Integer>) HSConfig.getList("Application", "NativeAds", "KeyboardEmojiTopLeftTabAd", "SessionIndexOfDay");
        int sessionTemp = -1;
        if (showAdSessionIndexs != null) {
            Collections.sort(showAdSessionIndexs);
            for (int sessionIndex : showAdSessionIndexs) {
                if (currentSessionIndex == sessionIndex) {
                    sessionTemp = sessionIndex;
                }
            }
        }
        return sessionTemp;
    }

    public static void hasShowedFunctionBarAd() {
        // 1. 当前session所在组的index
        int sessionIndex = getCurrentSessionGroupIndex();
        if (sessionIndex == -1) {
            return;
        }
        // 2. 当前已经显示过广告的组的index
        Set<String> sessions = PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).getStringSet(SP_FUNCTION_BAR_AD_SHOWED_ON_KEYBOARD_SESSIONS, new HashSet<String>());

        // 3. 添加到sharepreference
        sessions.add("" + sessionIndex);
        PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).edit().putStringSet(SP_FUNCTION_BAR_AD_SHOWED_ON_KEYBOARD_SESSIONS, sessions).apply();
    }

    public static boolean isShowFunctionBarAdConditionSatisfied() {
        if (IAPManager.getManager().hasPurchaseNoAds()) {
            return false;
        }

        // 1. Plist是否显示广告
        boolean shouldShow = HSConfig.optBoolean(false, "Application", "NativeAds", "KeyboardFunctionBarGiftAd", "Show");
        if (!shouldShow) {
            return false;
        }
        // 2. 当前session所在组的index
        int sessionIndex = getCurrentSessionGroupIndex();
        if (sessionIndex == -1) {
            return false;
        }
        // 3. 如果当前session所在组的index已经加载过广告了，则return false， 反之，return true
        Set<String> sessions = PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).getStringSet(SP_FUNCTION_BAR_AD_SHOWED_ON_KEYBOARD_SESSIONS, new HashSet<String>());
        if (!sessions.contains(sessionIndex + "")) {
            return true;
        }
        return false;
    }

    private static int getCurrentSessionGroupIndex() {
        // 1. 获取当前session索引
        int currentSessionIndex = getInputViewStartCount();
        // 2. 获取plist允许弹出广告的session列表
        List<Integer> showAdSessionIndexs = (List<Integer>) HSConfig.getList("Application", "NativeAds", "KeyboardFunctionBarGiftAd", "SessionIndexOfDay");
        int sessionTemp = -1;
        // 3. 查找比当前session索引小的索引
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

    public static void resetKeyboardPanelAdCountData() {
        PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).edit().putInt(SP_KEYBOARD_CURRENT_EMOJI_SESSION, 0).apply();
        PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).edit().putStringSet(SP_FUNCTION_BAR_AD_SHOWED_ON_KEYBOARD_SESSIONS, new HashSet<String>()).apply();
    }
}
