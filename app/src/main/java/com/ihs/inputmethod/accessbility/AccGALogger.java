package com.ihs.inputmethod.accessbility;

import com.ihs.commons.utils.HSPreferenceHelper;
import com.kc.utils.KCAnalytics;

/**
 * Created by Arthur on 17/1/11.
 */

public class AccGALogger {
    public static final String app_accessibility_setkey_screen_viewed = "app_accessibility_setkey_screen_viewed";//Accessibility设置界面展示
    public static final String app_auto_setkey_clicked = "app_auto_setkey_clicked"; //自动设置按钮点击
    public static final String app_manual_setkey_clicked = "app_manual_setkey_clicked"; //手动设置按钮点击
    public static final String app_accessibility_guide_viewed = "app_accessibility_guide_viewed"; //accessibility教程展示
    public static final String app_accessibility_guide_gotit_clicked = "app_accessibility_guide_gotit_clicked"; //教程got it按钮点击
    public static final String app_permission_accessibility_allowed = "app_permission_accessibility_allowed"; //accessibility权限通过
    public static final String app_alert_auto_setkey_showed = "app_alert_manual_setkey_showed"; //自动设置键盘提示展示
    public static final String app_alert_auto_setkey_enable_clicked = "app_alert_auto_setkey_enable_clicked"; //自动设置键盘Enable点击
    public static final String app_setting_up_page_viewed = "app_setting_up_page_viewed"; //自动设置页展示
    public static final String app_accessibility_setkey_success_page_viewed = "app_accessibility_setkey_success_page_viewed"; //自动添加键盘成功


    private static final String one_tap_page_viewed = "one_tap_page_viewed";


    public static void logOneTimeGA(String key) {
        if (!HSPreferenceHelper.getDefault().getBoolean(key, false)) {
            KCAnalytics.logEvent(key);
            HSPreferenceHelper.getDefault().putBoolean(key, true);
        }
    }

    public static boolean isOneTapPageViewed() {
        if (!HSPreferenceHelper.getDefault().getBoolean(one_tap_page_viewed, false)) {
            HSPreferenceHelper.getDefault().putBoolean(one_tap_page_viewed, true);
            return false;
        }
        return true;
    }
}
