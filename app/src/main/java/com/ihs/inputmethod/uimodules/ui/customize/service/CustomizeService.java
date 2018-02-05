package com.ihs.inputmethod.uimodules.ui.customize.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.feature.common.CustomizeConfig;

import java.util.List;
import java.util.Map;

/**
 * Service provided by main process for operations related to wallpaper & themes.
 * This service is used by ":customize" remote process and independent theme packages.
 */
public class CustomizeService extends Service {

    // --Commented out by Inspection (18/1/11 下午2:41):private static final String TAG = CustomizeService.class.getSimpleName();
    private Handler mMainHandler= new Handler(Looper.getMainLooper());

    private final ICustomizeService.Stub mBinder = new ICustomizeService.Stub() {


        @Override
        public String getCurrentTheme() throws RemoteException {
            return null;
        }

        @Override
        public void setCurrentTheme(final String themePackage) throws RemoteException {

        }

        @Override
        public long browseMarketApp(String packageName) throws RemoteException {
            return 0;
        }

        @Override
        public String getDefaultSharedPreferenceString(String key, String defaultValue) {
            return HSPreferenceHelper.getDefault().getString(key, defaultValue);
        }

        @Override
        public void preChangeWallpaperFromLauncher() throws RemoteException {

        }

        @Override
        public void putDefaultSharedPreferenceString(String key, String value) {
            HSPreferenceHelper.getDefault().putString(key, value);
        }

        @Override
        public void notifyWallpaperFeatureUsed() throws RemoteException {

        }

        @Override
        public void notifyWallpaperSetEvent() throws RemoteException {

        }

        @Override
        @Deprecated
        public List getOnlineWallpaperConfig() {
            return CustomizeConfig.getList("Wallpapers");
        }

        @Override
        public Map getOnlineThemeConfig() throws RemoteException {
            return null;
        }

        @Override
        @Deprecated
        public void logWallpaperEvent(String action, String label) {
            throw new UnsupportedOperationException("logWallpaperEvent() is deprecated. Use HSAnalytics#logEvent() instead.");
        }

        @Override
        @Deprecated
        public void killWallpaperProcess() {
            throw new UnsupportedOperationException("killWallpaperProcess() is deprecated. Kill yourself with System.exit().");
        }

        @Override
        public void notifyWallpaperOrThemeExit() throws RemoteException {

        }

        @Override
        public void notifyWallpaperPackageClicked() throws RemoteException {

        }

    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        mMainHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
