package com.ihs.inputmethod.charging;


import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.chargingscreen.HSChargingScreenManager;
import com.ihs.chargingscreen.utils.ChargingPrefsUtil;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.utils.PreferenceUtils;

import java.util.Date;

/**
 * Created by jixiang on 16/4/20.
 *
 * charging 相关配置
 */
public class ChargingConfigManager {
    private static final String TAG = ChargingConfigManager.class.getSimpleName();
    private final static String PREF_KEY_SHOULD_HIDE_CHARGING_HINT = "display_charging_hint";
    public final static String PREF_KEY_USER_SET_CHARGING_TOGGLE = "user_set_charging_toggle";
    public final static String PREF_KEY_CHARGING_NEW_USER = "charging_new_user";
    public final static String PREF_KEY_LOCK_VIEW_CLICKED = "pref_key_lock_view_clicked";
    private final static String PREF_KEY_ENABLE_ALERT_SHOW_COUNT = "pref_key_lock_enable_alert_show_count";
    private final static String PREF_KEY_ENABLE_CARD_SHOW_COUNT = "pref_key_lock_enable_card_show_count";

    private final static int MAX_SHOW_COUNT = 2;
    private final boolean CHARGING_TOGGLE_DEFAULT_VALUE = false;

    private static ChargingConfigManager instance;
    public static ChargingConfigManager getManager() {
        if (instance == null) {
            synchronized (ChargingConfigManager.class) {
                if (instance == null) {
                    instance = new ChargingConfigManager();
                }
            }
        }
        return instance;
    }

    private ChargingConfigManager() {

    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    /**
//     * 是否显示过Charging的提示框
//     * @return
//     */
//    public boolean isDisplayChargingHint() {
//        return PreferenceUtils.getBoolean(PREF_KEY_SHOULD_HIDE_CHARGING_HINT);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public void setDisplayChargingHint(boolean display) {
//        PreferenceUtils.setBoolean(PREF_KEY_SHOULD_HIDE_CHARGING_HINT, display);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)


// --Commented out by Inspection START (18/1/11 下午2:41):
//    /**
//     * 是否该开启Charging功能
//     * @return
//     */
//    public boolean shouldOpenChargingFunction(){
//        //总控制，如果关了则直接返回false
//        boolean chargingEnable =
//                HSConfig.optBoolean(false, "Application", "ChargeLocker",  "enable");
//        if(chargingEnable) {
//            HSPreferenceHelper prefs = HSPreferenceHelper.getDefault(HSApplication.getContext());
//
//            if(prefs.contains(PREF_KEY_USER_SET_CHARGING_TOGGLE)){
//                boolean userSetValue = prefs.getBoolean(HSApplication.getContext().getResources().getString(R.string.config_charge_switchpreference_key), CHARGING_TOGGLE_DEFAULT_VALUE);
//                HSLog.d("jx,用户设置过charging的开关，用户设置的结果为:"+userSetValue);
//                return userSetValue;
//            }
//
//            // 如果未发现remote config变化，则默认打开
//            if (!prefs.contains(PREF_KEY_CHARGING_NEW_USER)) {
//                HSLog.d("jx,未发现remote config变化 shouldOpenChargingFunction");
//                return true;
//            }
//            boolean newUserConfig = prefs.getBoolean(PREF_KEY_CHARGING_NEW_USER, false);
//            boolean firstLaunchShowedUserConfig =
//                    HSConfig.optBoolean(false, "Application", "ChargeLocker", "FirstLaunchShowedUser");
//            Date configDate =
//                    HSConfig.optDate(new Date(), "Application", "ChargeLocker", "AfterInstallDateUser");
//            long currentSessionId = HSSessionMgr.getCurrentSessionId();
//            if (newUserConfig && currentSessionId == 1) {
//                HSLog.d("jx,newUser为true且是第一次打开");
//                return true;
//            }
//            if (newUserConfig && currentSessionId > 1 && firstLaunchShowedUserConfig) {
//                HSLog.d("jx,newUser为true且是第2次及以上打开");
//                return true;
//            }
//            long firstSessionStartTime = HSSessionMgr.getFirstSessionStartTime();
//            HSLog.d("jx,firstSessionStartTime:"+firstSessionStartTime+",configDate:"+configDate.getTime());
//            if (firstSessionStartTime > configDate.getTime()) {
//                HSLog.d("jx,首次打开时间大于configDate");
//                return true;
//            }
//        }else {
//            HSLog.d("jx,chargingEnable 为false");
//        }
//        HSLog.d("jx,不满足开启charging要求，返回false");
//        return false;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    /**
//     * 是否应开启charging功能
//     * @return
//     */
//    public boolean enableChargingFunction(){
//        return  HSConfig.optBoolean(false, "Application", "ChargeLocker",  "enable");
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    /**
//     * 是否用户设置过charging的开关
//     * @return
//     */
//    public boolean isUserSetChargingToggle(){
//        return HSPreferenceHelper.getDefault(HSApplication.getContext()).contains(PREF_KEY_USER_SET_CHARGING_TOGGLE);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    /**
     * 用户设置过charging的开关
     */
    public void setUserChangeChargingToggle(){
        HSPreferenceHelper.getDefault(HSApplication.getContext()).putBoolean(PREF_KEY_USER_SET_CHARGING_TOGGLE,true);
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    /**
//     * 设置lockView被点击过，则不现实任何出厂动画
//     */
//    public void setLockViewClicked(){
//        boolean lockViewClicked = HSPreferenceHelper.getDefault(HSApplication.getContext()).getBoolean(PREF_KEY_LOCK_VIEW_CLICKED, false);
//        if(!lockViewClicked){
//            HSPreferenceHelper.getDefault(HSApplication.getContext()).putBoolean(PREF_KEY_LOCK_VIEW_CLICKED,true);
//        }
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    /**
//     * 判断是否lockView被点击过
//     * @return
//     */
//    public boolean isLockViewClicked(){
//        return HSPreferenceHelper.getDefault(HSApplication.getContext()).getBoolean(PREF_KEY_LOCK_VIEW_CLICKED, false);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    private boolean shouldFocusOnChargingEnableEvent() {
        // Charging not support
        if (ChargingPrefsUtil.getChargingEnableStates() == ChargingPrefsUtil.CHARGING_MUTED) {
            HSLog.i(TAG, "Charging is MUTED");
            return false;
        }

        // Charging enabled once
        if (ChargingPrefsUtil.getInstance().isChargingEnabledBefore()) {
            HSLog.i(TAG, "Charging enable once");
            return false;
        }

        // Charging function opened
        if (HSChargingScreenManager.getInstance().isChargingModuleOpened()) {
            HSLog.i(TAG, "Charging opened");
            return false;
        }

        return true;
    }

    public void increaseEnableAlertShowCount() {
        int showCount = HSPreferenceHelper.getDefault(HSApplication.getContext()).getInt(PREF_KEY_ENABLE_ALERT_SHOW_COUNT, 0);
        showCount++;
        HSPreferenceHelper.getDefault(HSApplication.getContext()).putInt(PREF_KEY_ENABLE_ALERT_SHOW_COUNT, showCount);
    }

    public boolean shouldShowEnableChargingAlert(boolean limitShowCount) {
        if (!shouldFocusOnChargingEnableEvent()) {
            return false;
        }

        if (limitShowCount) {
            if (isEnableChargingAlertShowCountAchievedMax()) {
                HSLog.i(TAG, "Charging enable alert achieved max show count");
                return false;
            }
        }

        return true;
    }

    private boolean isEnableChargingAlertShowCountAchievedMax() {
        final int showCount = HSPreferenceHelper.getDefault(HSApplication.getContext()).getInt(PREF_KEY_ENABLE_ALERT_SHOW_COUNT, 0);
        return showCount >= MAX_SHOW_COUNT;
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    private boolean shouldShowEnableChargingAlertAtThisTime() {
//        final int sessionInterval = HSConfig.optInteger(3, "Application", "FeaturePrompt", "ChargerLockerAlert", "SessionInterval");
//        HSLog.i(TAG, "Session id: " + HSSessionMgr.getCurrentSessionId() + ", SessionInterval: " + sessionInterval);
//        if (sessionInterval > 0) {
//            return HSSessionMgr.getCurrentSessionId() % (sessionInterval + 1) == 1;
//        }
//
//        return true;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    public void increaseEnableCardShowCount() {
        int showCount = HSPreferenceHelper.getDefault(HSApplication.getContext()).getInt(PREF_KEY_ENABLE_CARD_SHOW_COUNT, 0);
        showCount++;
        HSPreferenceHelper.getDefault(HSApplication.getContext()).putInt(PREF_KEY_ENABLE_CARD_SHOW_COUNT, showCount);
    }

    public boolean shouldShowEnableChargingCard(boolean limitShowCount) {
        if (!shouldFocusOnChargingEnableEvent()) {
            return false;
        }

        if (limitShowCount) {
            if (isEnableChargingCardShowCountAchievedMax()) {
                HSLog.i(TAG, "Charging enable card achieved max show count");
                return false;
            }

            if (!shouldShowEnableChargingCardAtThisTime()) {
                HSLog.i(TAG, "Charging enable card should not show at session: " + HSSessionMgr.getCurrentSessionId());
                return false;
            }
        }

        return true;
    }

    private boolean isEnableChargingCardShowCountAchievedMax() {
        final int maxCount = HSConfig.optInteger(3, "Application", "FeaturePrompt", "ThemeCard", "MaxShowCount");
        HSLog.i(TAG, "MaxShowCount: " + maxCount);
        final int showCount = HSPreferenceHelper.getDefault(HSApplication.getContext()).getInt(PREF_KEY_ENABLE_CARD_SHOW_COUNT, 0);
        return showCount >= maxCount;
    }

    private boolean shouldShowEnableChargingCardAtThisTime() {
        final int sessionInterval = HSConfig.optInteger(3, "Application", "FeaturePrompt", "ThemeCard", "SessionInterval");
        HSLog.i(TAG, "SessionInterval: " + sessionInterval);
        if (sessionInterval > 0) {
            if (HSSessionMgr.getCurrentSessionId() > 1) {// Skip first popup
                return HSSessionMgr.getCurrentSessionId() % (sessionInterval + 1) == 1;
            }

            return false;
        }

        return true;
    }
}
