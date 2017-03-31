package com.ihs.booster.common.event;

import com.ihs.app.framework.HSNotificationConstant;

public class MBObserverEvent extends HSNotificationConstant {

    /**
     * 与 android 系统有关的
     */
    public static final String SYSTEM_HOME_KEY_PRESSED_SHORT = "SYSTEM_HOME_KEY_PRESSED_SHORT"; //参数 无
    public static final String SYSTEM_CONFIGURATION_CHANGED = "SYSTEM_CONFIGURATION_CHANGED"; //参数 Configuration
    public static final String SYSTEM_SCREEN_STATE_CHANGED = "SYSTEM_SCREEN_STATE_CHANGED"; //参数 Map<String,Object> "context":Context, "intent":Intent
    public static final String SYSTEM_PHONE_STATE_CHANGED = "SYSTEM_PHONE_STATE_CHANGED"; //参数 Map<String,Object> "context":Context, "intent":Intent
    public static final String SYSTEM_BATTERY_STATE_CHANGED = "SYSTEM_BATTERY_STATE_CHANGED";
    public static final String SYSTEM_KEYBOARD_LAYOUT_CHANGED = "SYSTEM_KEYBOARD_LAYOUT_CHANGED";
    public static final String SYSTEM_NO_SPACE_LEFT_ON_DEVICE = "SYSTEM_NO_SPACE_LEFT_ON_DEVICE"; //参数 无
    /**
     * 与 Activity有关的
     */
    public static final String ACTIVITY_STOP = "ACTIVITY_STOP";
    public static final String ACTIVITY_RESULT = "ACTIVITY_RESULT";
    public static final String ACTIVITY_RESTORE = "ACTIVITY_RESTORE";
    public static final String ACTIVITY_PREPARE_OPTIONS_MENU = "ACTIVITY_PREPARE_OPTIONS_MENU";
    public static final String ACTIVITY_OPTIONS_ITEM_SELECTED = "ACTIVITY_OPTIONS_ITEM_SELECTED";
    public static final String ACTIVITY_CONTEXT_ITEM_SELECTED = "ACTIVITY_CONTEXT_ITEM_SELECTED";

    /**
     * 与  Permission 有关的
     */
    public static final String PERMISSION_USAGE_ACCESS_GRANTED = "PERMISSION_USAGE_ACCESS_GRANTED";
    // Permission event
    public static final String PERMISSION_ACCESSIBILITY_GRANTED = "PERMISSION_ACCESSIBILITY_GRANTED";

    /**
     * APP 通知
     */
    public static final String SCAN_MEMORY_FINISHED = "SCAN_MEMORY_FINISHED";//List<AppsListItem> apps
    public static final String CLEAN_MEMORY_FINISHED = "CLEAN_MEMORY_FINISHED";//List<AppsListItem> apps

    public static final String SCAN_JUNK_FINISHED = "SCAN_JUNK_FINISHED";//List<AppsListItem> apps
    public static final String CLEAN_JUNK_FINISHED = "CLEAN_JUNK_FINISHED";//List<AppsListItem> apps

    public static final String SCAN_JUNK_FILE_FINISHED = "SCAN_JUNK_FILE_FINISHED";//List<AppsListItem> apps
    public static final String CLEAN_JUNK_FILE_FINISHED = "CLEAN_JUNK_FILE_FINISHED";//List<AppsListItem> apps

    public static final String SCAN_BATTERY_FINISHED = "SCAN_BATTERY_FINISHED";//List<AppsListItem> apps
    public static final String CLEAN_BATTERY_FINISHED = "CLEAN_BATTERY_FINISHED";//List<AppsListItem> apps

    public static final String SCAN_CPU_FINISHED = "SCAN_CPU_FINISHED";//List<AppsListItem> apps

    public static final String IGNORE_APP_LIST_CHANGED = "IGNORE_APP_LIST_CHANGED";//List<AppsListItem> ignoreApps
    public static final String IGNORE_APP_LIST_REMOVE = "IGNORE_APP_LIST_REMOVE";//String packageName
    public static final String IGNORE_APP_LIST_ADD = "IGNORE_APP_LIST_ADD";//String packageName

    public static final String BATTERY_WHITE_APP_LIST_REMOVE = "BATTERY_WHITE_APP_LIST_REMOVE";//String packageName
    public static final String BATTERY_WHITE_APP_LIST_ADD = "BATTERY_WHITE_APP_LIST_ADD";//String packageName

    public static final String BATTERY_BLACK_APP_LIST_REMOVE = "BATTERY_BLACK_APP_LIST_REMOVE";//String packageName
    public static final String BATTERY_BLACK_APP_LIST_ADD = "BATTERY_BLACK_APP_LIST_ADD";//String packageName

    /**
<<<<<<< HEAD:app/src/main/java/com/powertools/booster/common/event/MBObserverEvent.java
=======
     * ACTIVITY通知
     */

    public static final String STUBBORN_JUNK_CLEAN_DONE = "STUBBORN_JUNK_CLEAN_DONE";
    public static final String BATTERY_OPTIMIZE_DONE = "BATTERY_OPTIMIZE_DONE";
    /**
>>>>>>> stubbornJunk:app/src/main/java/com/powertools/booster/common/event/MBObserverEvent.java
     * Device info 通知
     */
    public static final String DEVICE_INFO_CPU_FREQUENCY_CHANGED = "DEVICE_INFO_CPU_FREQUENCY_CHANGED"; //参数 ArrayList<Float>


    /**
     * 充电锁屏功能
     */
    public static final String SYSTEM_BATTERY_CHARGING_STATE_CHANGED = "SYSTEM_BATTERY_CHARGING_STATE_CHANGED";
    public static final String CHARGING_MODULE_OPENED = "CHARGING_MODULE_OPENED";
    public static final String CHARGING_SHOW_PUSH = "CHARGING_SHOW_PUSH";

}
