package com.ihs.app.framework;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.ihs.inputmethod.uimodules.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Arthur on 18/1/17.
 */

public class HSApplication extends Application {
    private static Context context;
    public static boolean isDebugging = BuildConfig.DEBUG;
    private static AcbLaunchInfo currentLaunchInfo = new AcbLaunchInfo();

    public static HSLaunchInfo getFirstLaunchInfo() {
        return new HSLaunchInfo();
    }

    public static String getProcessName() {
        return HSApplication.context.getPackageName();
    }

    public static AcbLaunchInfo getCurrentLaunchInfo() {
        return currentLaunchInfo;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }

    public static class AcbLaunchInfo {
        public int appVersionCode = 10000;
    }

    public static class HSLaunchInfo {
        public int launchId;
        public int appVersionCode;
        public String appVersionName;
        public String osVersion;
        private static String KEY_LAUNCH_ID = "launchId";
        private static String KEY_APP_VERSION_CODE = "appVersionCode";
        private static String KEY_APP_VERSION_NAME = "appVersion";
        private static String KEY_OS_VERSION = "osVersion";

        public HSLaunchInfo() {
        }

        static HSApplication.HSLaunchInfo parseFromString(String str) {
            HSApplication.HSLaunchInfo info = null;
            if (!TextUtils.isEmpty(str)) {
                try {
                    JSONObject json = new JSONObject(str);
                    info = new HSApplication.HSLaunchInfo();
                    info.launchId = json.getInt(KEY_LAUNCH_ID);
                    info.appVersionCode = json.optInt(KEY_APP_VERSION_CODE, -1);
                    info.appVersionName = json.getString(KEY_APP_VERSION_NAME);
                    info.osVersion = json.getString(KEY_OS_VERSION);
                } catch (JSONException var3) {
                    info = null;
                }
            }

            return info;
        }

        public String toString() {
            String str = "";

            try {
                JSONObject json = new JSONObject();
                json.put(KEY_LAUNCH_ID, this.launchId);
                json.put(KEY_APP_VERSION_CODE, this.appVersionCode);
                json.put(KEY_APP_VERSION_NAME, this.appVersionName);
                json.put(KEY_OS_VERSION, this.osVersion);
                str = json.toString();
            } catch (JSONException var3) {
                str = "";
            }

            return str;
        }
    }
}
