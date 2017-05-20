package com.ihs.inputmethod.feature.common;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.libraryconfig.HSLibrarySessionManager;
import com.ihs.commons.libraryconfig.HSLibraryconfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSMapUtils;
import com.ihs.commons.utils.HSPlistParser;
import com.ihs.inputmethod.uimodules.BuildConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomizeConfig implements HSLibraryconfig.ILibraryListener {

    private static final String WALLPAPERS_CONFIG_LOCAL_FILE_NAME = "customize.la";
    private static final int CONFIG_VERSION = 4;

    /**
     * This notification is sent when the remote fetch has a result, success or failure.
     */
    public static final String NOTIFICATION_CUSTOMIZE_CONFIG_FETCH_FINISHED = "customize_config_fetch_finished";

    /**
     * This notification is sent when a remote fetch succeeded AND data fetched is NOT identical to local data.
     */
    public static final String NOTIFICATION_CUSTOMIZE_CONFIG_CHANGED = "customize_config_changed";

    public static final String NOTIFICATION_CUSTOMIZE_DOWNLOAD_STATE_CHANGED = "customize_config_download_state_changed";

    public static Map<String, ?> getMap(String... path) {
        Map<String, ?> map = HSMapUtils.getMap(getConfigMap(), path);
        if (map == null) {
            map = new HashMap<>();
        }
        return map;
    }

    public static List<?> getList(String... path) {
        List<?> list = HSMapUtils.getList(getConfigMap(), path);
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    public static String getString(String defaultValue, String... path) {
        return HSMapUtils.optString(getConfigMap(), defaultValue, path);
    }

    public static int getInteger(int defaultValue, String... path) {
        return HSMapUtils.optInteger(getConfigMap(), defaultValue, path);
    }

    public static boolean getBoolean(boolean defaultValue, String... path) {
        return HSMapUtils.optBoolean(getConfigMap(), defaultValue, path);
    }

    public static Map<String, ?> getConfigMap() {
        return HSLibraryconfig.getInstance().getDataForLibrary(sConfigProvider);
    }

    private static CustomizeConfig sInstance;

    private static HSLibraryconfig.ILibraryProvider sConfigProvider = new HSLibraryconfig.ILibraryProvider() {
        @Override
        public int getLibraryVersionNumber() {
            return CONFIG_VERSION;
        }

        @Override
        public String getLibraryName() {
            return "launcher-customize";
        }
    };

    private CustomizeConfig() {
    }

    /**
     * Performs a synchronous local load and starts an async remote fetch.
     */
    public synchronized static void init() {
        if (sInstance == null) {
            sInstance = new CustomizeConfig();
        }
        sInstance.doInit();
    }

    private void doInit() {
        HSLibrarySessionManager.getInstance().startSessionForLibrary(sConfigProvider);
        Map<String, ?> localConfigData = parseLocalConfig();
        String remoteConfigUrl = BuildConfig.DEBUG ?
                BuildConfig.CUSTOMIZE_CONFIG_DEBUG : BuildConfig.CUSTOMIZE_CONFIG_RELEASE;

        HSLog.d("LibraryConfig", "Load wallpaper config, fire a remote fetch");
        HSLibraryconfig.getInstance().startForLibrary(remoteConfigUrl, localConfigData, sConfigProvider, this);
    }

    private Map<String, ?> parseLocalConfig() {
        Map<String, ?> data = null;
        InputStream is = null;
        try {
            is = HSApplication.getContext().getAssets().open(WALLPAPERS_CONFIG_LOCAL_FILE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (is != null) {
            data = HSPlistParser.parse(is, true);
        }
        return data;
    }

    @Override
    public void onRemoteConfigDidFinishInitialization() {
        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_CUSTOMIZE_CONFIG_FETCH_FINISHED);
    }

    @Override
    public void onRemoteConfigDataChanged() {
        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_CUSTOMIZE_CONFIG_CHANGED);
    }
}
