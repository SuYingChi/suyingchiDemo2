package com.ihs.commons.config;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Arthur on 18/1/17.
 */

public class HSConfig {
    public static final String HS_NOTIFICATION_CONFIG_CHANGED = "11";

    public static List getList(String application, String s, String email) {
        return new ArrayList();
    }

    public static String getString(String application, String server, String imageURLPrefix) {
        return "";
    }

    public static String getString(String application, String shareContents, String keyboard, String shareTexts, String forFonts) {
        return "";
    }

    public static List<?> getList(String rmtcfgKeyL1Application, String rmtcfgKeyL2SendStrategy) {
        return new ArrayList<>();
    }

    public static boolean optBoolean(boolean b, String application, String keyboardTheme, String downloadTheme, String downloadThemeEnable) {
        return false;
    }

    public static List<?> getList(String application, String keyboardTheme, String downloadTheme, String downloadThemePkNamePrefix) {
        return new ArrayList<>();
    }

    public static int optInteger(int i, String application, String remoteConfig, String fetchIntervalInHour) {
        return 0;
    }

    public static String getString(String libKeyboard, String dictionaryUrl) {
        return "";
    }

    public static String optString(String s, String application, String server, String stickerSuggestionBaseURL) {
        return s;
    }

    public static int optInteger(int i, String application, String server, String getRecommend, String userStatistics, String initialSession) {
        return i;
    }

    public static int optInteger(int i, String application, String server, String report, String reportIntervalInHour) {
        return i;
    }

    public static String optString(String o, String application, String server, String getRecommend, String keys) {
        return o;
    }

    public static Map getMap(String application, String keyboardTheme, String themeContents) {

        return  new HashMap();
    }

    public static boolean optBoolean(boolean b, String application, String interstitialAds, String keyboardAds, String s, String show) {
        return b;
    }

    public static List<?> getList(String application, String interstitialAds, String keyboardAds, String s, String sessionIndexOfDay) {
        return new ArrayList<>();
    }

    public static boolean optBoolean(boolean b, String application, String autoSetKeyEnable) {
        return b;
    }

    public static boolean optBoolean(boolean b, String application, String remindChangeKeyboard, String enable) {
        return b;
    }

    public static float optFloat(int i, String application, String interstitialAds, String backButton, String minIntervalByHour) {

        return i;
    }

    public static float optFloat(float i, String application, String interstitialAds, String backButton, String minIntervalByHour) {

        return i;
    }

    public static long getInteger(String application, String searchAd, String updateTimeInMin) {
        return 0;
    }

    public static int optFloat(int maxValue, String application, String remindChangeKeyboard, String remindWhenNotActive) {
        return 0;
    }

    public static String optString(String s, String update, String downloadUrl) {
        return s;
    }

    public static String optString(String string, String application, String update, String shareAlert, String message, String preferredLanguageString) {
        return string;
    }

    public static String optString(String gp, String[] strings) {
        return gp;
    }

    public static Date optDate(Date date, String application, String chargeLocker, String afterInstallDateUser) {
        return date;
    }

    public static String getString(String[] path) {
        return "";
    }

    public static boolean getBoolean(String[] path) {
        return false;
    }

    public static int getInteger(String[] path) {
        return 0;
    }

    public static float getFloat(String[] path) {
        return 0;
    }

    public static HashMap getMap(String[] path) {
        return new HashMap<>();
    }

    public static boolean exists(String nativeAds, String adPoolName) {
        return false;
    }

    public static int optInteger(int d, String[] a){
        return d;
    }
    public static List<?> getList(String... path) {
        return new ArrayList<>();
    }
}
