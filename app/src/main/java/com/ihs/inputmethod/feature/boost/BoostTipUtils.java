package com.ihs.inputmethod.feature.boost;

import android.graphics.Color;
import android.support.annotation.ColorInt;

import com.honeycomb.launcher.chargingscreen.ChargingScreenSettings;
import com.honeycomb.launcher.dialog.BoostTip;
import com.honeycomb.launcher.model.LauncherFiles;
import com.honeycomb.launcher.notification.NotificationManager;
import com.honeycomb.launcher.util.PreferenceHelper;
import com.honeycomb.launcher.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.utils.HSVersionControlUtils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BoostTipUtils {

    private static final String TAG = "BoostTipUtils";

    private static final String[] SYSTEM_APPS = {
            "com.google.android.ext.services",
            "com.google.android.gms",
            "com.google.android.googlequicksearchbox",
            "com.android.stk",
            "com.android.systemui",
            "com.android.phone",
            "com.android.mms.service",
            "com.android.chrome",
            "com.google.android.youtube",
            "com.android.providers.media",
            "com.android.bluetooth",
            "com.google.android.inputmethod.pinyin",
            "com.android.server.telecom",
            "com.google.android.music",
            "com.google.android.gm",
            "com.google.android.apps.inputmethod.hindi",
            "com.android.vending",
            "com.google.android.apps.docs",
            "com.dsi.ant.server",
            "com.qualcomm.qti.services.secureui",
            "com.nxp.nfceeapi.service",
            "com.qualcomm.wfd.service",
            "com.qualcomm.location",
            "com.google.android.inputmethod.latin",
            "com.google.android.apps.plus",
            "eu.chainfire.supersu",
            "com.sonyericsson.android.camera",
            "com.sonyericsson.crashmonitor",
            "com.realvnc.android.remote",
            "com.facebook.katana",
            "com.sonymobile.phoneusage",
            "com.sonymobile.photoanalyzer",
            "com.sonymobile.cameracommon",
            "com.google.android.talk",
            "com.sonyericsson.psm.sysmonservice",
            "com.sonymobile.runtimeskinning.core",
            "com.sonyericsson.advancedwidget.clock",
            "com.sonyericsson.extras.liveware",
            "com.sonyericsson.textinput.uxp",
            "com.sonymobile.xperialink",
            "com.sonymobile.mirrorlink.system",
            "com.sonyericsson.android.camera3d",
            "com.svox.pico",
            "com.sonymobile.mx.android",
            "com.android.keychain",
            "com.sonymobile.sonyselectdata",
            "com.sonyericsson.home",
            "com.sonymobile.enterprise.service",
            "com.sonyericsson.setupwizard",
            "com.android.voicedialer",
            "com.sonyericsson.smartcard",
            "system",
            "com.android.defcontainer",
            "com.sonyericsson.album",
            "com.sonyericsson.android.addoncamera.artfilter",
            "com.android.nfc",
            "com.sonyericsson.devicemonitor",
            "com.sonyericsson.android.bootinfo",
            "com.android.musicfx",
            "com.sonyericsson.updatecenter",
            "com.sonyericsson.usbux",
            "com.google.android.partnersetup",
            "com.google.android.gsf.login",
            "com.sony.smallapp.managerservice",
            "com.sec.android.app.taskmanager",
            "com.sec.android.gallery3d",
            "com.sec.android.service.health",
            "com.sec.android.app.soundalive",
            "com.sec.android.app.samsungapps",
            "com.samsung.android.app.pinboard",
            "com.sec.android.app.shealth",
            "com.samsung.android.app.assistantmenu",
            "com.sec.android.service.sm",
            "com.osp.app.signin",
            "com.samsung.android.app.vrsetupwizardstub",
            "com.google.android.apps.maps",
            "com.samsung.android.MtpApplication",
            "com.android.contacts",
            "com.samsung.android.app.galaxyfinder",
            "com.samsung.dcm",
            "com.samsung.android.providers.context",
            "com.sec.android.app.myfiles",
            "com.sec.android.app.videoplayer",
            "com.sec.android.provider.logsprovider",
            "com.ws.dm",
            "com.smlds",
            "com.sec.android.app.music",
            "com.android.mms",
            "com.sec.android.service.cm",
            "com.samsung.android.fingerprint.service",
            "com.samsung.android.sdk.samsunglink",
            "com.samsung.android.app.filterinstaller",
            "com.samsung.android.writingbuddyservice",
            "com.samsung.android.snote",
            "com.vlingo.midas",
            "com.sec.android.widgetapp.activeapplicationwidget",
            "com.sec.android.app.popupuireceiver",
            "com.android.settings",
            "com.sec.android.app.tmserver",
            "com.sec.pcw.device",
            "com.android.incallui",
            "com.samsung.android.provider.shootingmodeprovider",
            "com.sec.android.daemonapp",
            "com.samsung.android.app.powersharing",
            "com.sec.spp.push",
            "com.sec.android.inputmethod",
            "com.sec.android.app.launcher",
            "com.sec.android.service.bezel",
            "com.sec.android.widgetapp.ap.hero.accuweather",
            "com.mediatek.batterywarning",
            "com.android.launcher",
            "com.android.quicksearchbox",
            "com.android.inputmethod.latin",
            "com.nqmobile.antivirus20",
            "com.mediatek.thermalmanager",
            "com.android.gallery3d",
            "com.evernote.skitch",
            "com.mediatek.atci.service",
            "com.mediatek.voicecommand",
            "com.android.location.fused",
            "com.redbend.dmClient",
            "com.tinno.gesture.phone",
            "com.mediatek.bluetooth",
            "com.huawei.android.ds",
            "com.taobao.taobao",
            "com.huawei.android.pushagent",
            "com.huawei.appmarket",
            "com.huawei.geofence",
            "com.huawei.bone",
            "com.android.externalstorage",
            "com.vlife.huawei.wallpaper",
            "com.huawei.powergenie",
            "com.huawei.ca",
            "com.huawei.wallet",
            "com.netease.newsreader.activity",
            "com.android.keyguard",
            "com.android.documentsui",
            "com.tencent.mtt",
            "com.huawei.gallery.photoshare",
            "org.simalliance.openmobileapi.service",
            "com.android.huawei.projectmenu",
            "com.huawei.motionservice",
            "com.snowballtech.walletservice",
            "com.nuance.swype.emui",
            "com.huawei.floatMms",
            "com.huawei.camera",
            "com.huawei.systemmanager",
            "com.huawei.android.totemweather",
            "com.huawei.android.karaokeeffect",
            "com.baidu.input_huawei",
            "com.cootek.smartdialer_oem_module",
            "com.sohu.sohuvideo",
            "com.sina.weibo",
            "com.huawei.android.powermonitor",
            "com.huawei.bd",
            "com.android.email",
            "com.huawei.android.hwouc",
            "com.amap.android.location",
            "com.huawei.remoteassistant",
            "com.huawei.hwid",
            "com.hw.sohu.newsclient.newswall",
            "com.huawei.android.airsharingcast",
            "com.huawei.android.mewidget",
            "com.android.supl",
            "com.huawei.android.launcher",
            "com.android.dolbymobileaudioeffect",
            "com.huawei.android.multiscreen",
            "com.baidu.map.location",
            "com.huawei.smartpower",
            "com.rxnetworks.pgpsdownloader",
            "com.motorola.MotGallery2",
            "com.motorola.ccc.checkin",
            "com.motorola.motocare.internal",
            "com.motorola.bach.modemstats",
            "com.motorola.motocare",
            "com.motorola.ccc.ota",
            "com.motorola.ccc.devicemanagement",
            "com.motorola.android.nativedropboxagent",
            "com.motorola.ccc.notification",
            "com.motorola.ccc.mainplm",
            "com.android.dialer",
            "com.lge.ime",
            "com.android.packageinstaller",
            "com.mount.dev",
            "com.lge.lockscreensettings",
            "com.lge.keepscreenon",
            "com.lge.ia",
            "com.google.android.setupwizard",
            "com.google.android.apps.magazines",
            "com.lge.splitwindow",
            "com.lge.mlt",
            "com.google.android.syncadapters.calendar",
            "com.google.android.configupdater",
            "com.lge.appbox.client",
            "com.lge.sizechangable.musicwidget.widget",
            "com.qualcomm.services.location",
            "com.lge.mrg.service",
            "com.google.android.apps.books",
            "com.lge.music",
            "com.lge.sizechangable.weather",
            "com.android.providers.calendar",
            "com.lge.springcleaning",
    };
    private static final List<String> SYSTEM_APPS_LIST = new ArrayList<>(202);

    private static final String PREF_KEY_BOOST_CLICK_TIMES = "boost_click_times";
    private static final String PREF_KEY_BOOST_PLUS_LAST_OPEN_TIME = "boost_plus_last_open_time";

    private static final int BOOST_PLUS_TOAST_TIMES_NEW_USER = 2;
    private static final int BOOST_PLUS_TOAST_TIMES_UPGRADE_USER = 10;
    private static final int BOOST_PLUS_TOAST_NO_SHOW_AGAIN = -1;
    private static final long BOOST_PLUS_NOTIFICATION_SECOND_TIME_LIMIT = 24 * 60 * 60;
    public static final int BOOST_PLUS_NOTIFICATION_RUNNING_APPS_LIMIT = 3;

    static final String PREF_KEY_BOOST_TIP_SHOW_COUNT = "boost_tip_show_count";
    private static final String PREF_KEY_BOOST_TIP_AD_SHOW_INTERVAL_COUNT = "boost_tip_ad_show_count";

    private static final int DEFAULT_BASIC_BOOST_AD_SHOW_RATE = 2;
    private static final int DEFAULT_BOOST_PLUS_SHOW_RATE = 3;
    private static final boolean DEFAULT_BOOST_TIP_SHOW_ENABLED = true;
    private static final boolean IS_USER_SINCE_V47_OR_ABOVE = HSApplication.getFirstLaunchInfo().appVersionCode >= 47;

    static {
        Collections.addAll(SYSTEM_APPS_LIST, SYSTEM_APPS);
    }

    // Reused data structures
    private static float[] sHsvColorA = new float[3];
    private static float[] sHsvColorB = new float[3];

    // Data used for BoostPlus judge each time.
    private static boolean sMayShowBoostPlus = false;
    private static boolean sAlreadyShownOptimalToday = false;
    private static boolean sAlreadyShownBatteryToday = false;
    private static boolean sAlreadyShownMemoryToday = false;
    private static boolean sLowBattery = false;

    // Data used for BoostAd judge each time.
    private static boolean sShouldShowBoostAd = false;

    public static
    @ColorInt
    int interpolateColorHsv(@ColorInt int colorA, @ColorInt int colorB, float proportionB) {
        Color.colorToHSV(colorA, sHsvColorA);
        Color.colorToHSV(colorB, sHsvColorB);
        for (int i = 0; i < 3; i++) {
            sHsvColorB[i] = sHsvColorA[i] + ((sHsvColorB[i] - sHsvColorA[i]) * proportionB);
        }
        return Color.HSVToColor(sHsvColorB);
    }

    public static List<String> getSystemApps() {
        return SYSTEM_APPS_LIST;
    }

    public static void doNotShowToastSecondTimes() {
        PreferenceHelper preferenceHelper = PreferenceHelper.get(LauncherFiles.BOOST_PREFS);
        int clickTimes = preferenceHelper.getInt(PREF_KEY_BOOST_CLICK_TIMES, 0);
        boolean isUpgradeUser = HSVersionControlUtils.isFirstLaunchSinceUpgrade();
        HSLog.d(TAG, "doNotShowToastSecondTimes clickTimes = " + clickTimes + " isUpgradeUser = " + isUpgradeUser);
        if ((isUpgradeUser && clickTimes == 1) || (!isUpgradeUser && clickTimes == 9)) {
            preferenceHelper.putInt(PREF_KEY_BOOST_CLICK_TIMES, BOOST_PLUS_TOAST_NO_SHOW_AGAIN);
        }
    }

    public static boolean shouldShowBoostPlusToast() {
        boolean isUpgradeUser = HSVersionControlUtils.isFirstLaunchSinceUpgrade();
        PreferenceHelper preferenceHelper = PreferenceHelper.get(LauncherFiles.BOOST_PREFS);
        int clickTimesOriginal = preferenceHelper.getInt(PREF_KEY_BOOST_CLICK_TIMES, 0);
        HSLog.d(TAG, "shouldShowBoostPlusToast clickTimesOriginal = " + clickTimesOriginal + " isUpgradeUser = " + isUpgradeUser);

        if (clickTimesOriginal != BOOST_PLUS_TOAST_NO_SHOW_AGAIN) {
            preferenceHelper.putInt(PREF_KEY_BOOST_CLICK_TIMES, ++clickTimesOriginal);
            if (isUpgradeUser) {
                if (clickTimesOriginal > BOOST_PLUS_TOAST_TIMES_NEW_USER) {
                    preferenceHelper.putInt(PREF_KEY_BOOST_CLICK_TIMES, BOOST_PLUS_TOAST_NO_SHOW_AGAIN);
                }
            } else {
                if (clickTimesOriginal > BOOST_PLUS_TOAST_TIMES_UPGRADE_USER) {
                    preferenceHelper.putInt(PREF_KEY_BOOST_CLICK_TIMES, BOOST_PLUS_TOAST_NO_SHOW_AGAIN);
                }
            }
        }

        int clickTimes = preferenceHelper.getInt(PREF_KEY_BOOST_CLICK_TIMES, BOOST_PLUS_TOAST_NO_SHOW_AGAIN);
        boolean shouldShowToast = (isUpgradeUser && clickTimes != BOOST_PLUS_TOAST_NO_SHOW_AGAIN && (clickTimes == 1 || clickTimes == 2)) ||
                (!isUpgradeUser && clickTimes != BOOST_PLUS_TOAST_NO_SHOW_AGAIN && (clickTimes == 9 || clickTimes == 10));
        HSLog.d(TAG, "shouldShowBoostPlusToast clickTimesOriginal = " + clickTimesOriginal + " clickTimes = " + clickTimes + " isUpgradeUser = " + isUpgradeUser + " shouldShowToast = " + shouldShowToast);
        return shouldShowToast;
    }

    public static void setLastOpenBoostPlusTime() {
        PreferenceHelper.get(LauncherFiles.BOOST_PREFS).putLong(PREF_KEY_BOOST_PLUS_LAST_OPEN_TIME, System.currentTimeMillis());
    }

    public static long getLastOpenBoostPlusTime() {
        return PreferenceHelper.get(LauncherFiles.BOOST_PREFS).getLong(PREF_KEY_BOOST_PLUS_LAST_OPEN_TIME, 0);
    }

    public static void setBoostPlusNotificationStartTiming() {
        long lastOpenBoostPlusTime = getLastOpenBoostPlusTime();
        HSLog.d(NotificationManager.TAG,
                "setBoostPlusNotificationStartTiming lastOpenBoostPlusTime = " + lastOpenBoostPlusTime);
        if (0 == lastOpenBoostPlusTime) {
            setLastOpenBoostPlusTime();
        }
    }

    private static boolean shouldShowBoostPlusByCount() {
        boolean slideDownViewEnabled = HSConfig.optBoolean(DEFAULT_BOOST_TIP_SHOW_ENABLED, "Application", "BoostResult", "Enabled");
        int showBoostPlusRate = HSConfig.optInteger(DEFAULT_BOOST_PLUS_SHOW_RATE, "Application", "BoostResult", "ShowBoostPlusRate");
        int boostTipShowCount = PreferenceHelper.get(LauncherFiles.BOOST_PREFS).getInt(PREF_KEY_BOOST_TIP_SHOW_COUNT, 0);

        HSLog.d(BoostTip.TAG, "shouldShowBoostPlusByCount() | slideDownViewEnabled: " + slideDownViewEnabled
                + ", boostTipShowCount: " + boostTipShowCount + ", showBoostPlusRate: " + showBoostPlusRate);
        return slideDownViewEnabled
                && boostTipShowCount % showBoostPlusRate == 0 && boostTipShowCount > 0;
    }

    public static boolean shouldShowBoostAd() {
        boolean slideDownViewEnabled = HSConfig.optBoolean(DEFAULT_BOOST_TIP_SHOW_ENABLED, "Application", "BoostResult", "Enabled");
        int showBoostAdsRate = HSConfig.optInteger(DEFAULT_BASIC_BOOST_AD_SHOW_RATE, "Application", "BoostResult", "ShowAdsRate");
        int intervalCount = PreferenceHelper.get(LauncherFiles.BOOST_PREFS).getInt(PREF_KEY_BOOST_TIP_AD_SHOW_INTERVAL_COUNT, 0);

        HSLog.d(BoostTip.TAG, "shouldShowBoostAd() | slideDownViewEnabled: " + slideDownViewEnabled
                + ", intervalCount: " + intervalCount + ", showBoostAdsRate: " + showBoostAdsRate);
        sShouldShowBoostAd = slideDownViewEnabled && intervalCount >= showBoostAdsRate && intervalCount > 0;
        return sShouldShowBoostAd;
    }

    public static void incrementShowCount() {
        PreferenceHelper.get(LauncherFiles.BOOST_PREFS).incrementAndGetInt(PREF_KEY_BOOST_TIP_SHOW_COUNT);
    }

    public static void incrementAdShowIntervalCount() {
        PreferenceHelper.get(LauncherFiles.BOOST_PREFS).incrementAndGetInt(PREF_KEY_BOOST_TIP_AD_SHOW_INTERVAL_COUNT);
    }

    public static void resetAdShowIntervalCount() {
        PreferenceHelper.get(LauncherFiles.BOOST_PREFS).putInt(PREF_KEY_BOOST_TIP_AD_SHOW_INTERVAL_COUNT, 1);
    }

    private static boolean isToday(String prefsKey) {
        long now = System.currentTimeMillis();
        long recorded = PreferenceHelper.get(LauncherFiles.BOOST_PREFS).getLong(prefsKey, 0);
        return Utils.getDayDifference(now, recorded) == 0;
    }

    public static boolean mayShowBoostPlus() {
        sAlreadyShownOptimalToday = isToday(BoostTip.PREF_KEY_BOOST_PLUS_SHOW_TIME_OPTIMAL);
        sAlreadyShownMemoryToday = isToday(BoostTip.PREF_KEY_BOOST_PLUS_SHOW_TIME_MEMORY);
        sAlreadyShownBatteryToday = isToday(BoostTip.PREF_KEY_BOOST_PLUS_SHOW_TIME_BATTERY);
        sLowBattery = DeviceManager.getInstance().getBatteryLevel() < 30;

        HSLog.d(BoostTip.TAG, "mayShowBoostPlus() | shouldShowBoostPlusByCount: " + sMayShowBoostPlus
                + ", alreadyShownOptimalToday: " + sAlreadyShownOptimalToday
                + ", lowBattery: " + sLowBattery
                + ", alreadyShownBatteryToday: " + sAlreadyShownBatteryToday
                + ", alreadyShownMemoryToday: " + sAlreadyShownMemoryToday);

        sMayShowBoostPlus = (shouldShowBoostPlusByCount()) &&
                ((!sAlreadyShownOptimalToday) || (sLowBattery && !sAlreadyShownBatteryToday) || (!sAlreadyShownMemoryToday));

        return sMayShowBoostPlus;
    }

    public static boolean mayShowChargingScreen(BoostType type) {
        return !ChargingScreenSettings.isChargingScreenEverEnabled()
                && (type == BoostType.CPU_TEMPERATURE || type == BoostType.BATTERY);
    }

    public static boolean getIfMayShowBoostPlus() {
        return sMayShowBoostPlus;
    }

    public static boolean getShouldShowBoostAd() {
        return sShouldShowBoostAd;
    }

    public static boolean getAlreadyShownOptimalToday() {
        return sAlreadyShownOptimalToday;
    }

    public static boolean getAlreadyShownBatteryToday() {
        return sAlreadyShownBatteryToday;
    }

    public static boolean getAlreadyShownMemoryToday() {
        return sAlreadyShownMemoryToday;
    }

    public static boolean getLowBattery() {
        return sLowBattery;
    }
}
