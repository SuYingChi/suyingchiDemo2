package com.ihs.inputmethod.feature.lucky;

import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.crashlytics.android.core.CrashlyticsCore;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.connection.httplib.HttpRequest;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.api.keyboard.HSKeyboardTheme;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.feature.common.ConcurrentUtils;
import com.ihs.inputmethod.feature.common.Utils;
import com.ihs.inputmethod.feature.lucky.view.ThemeView;
import com.ihs.inputmethod.uimodules.BuildConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LuckyPreloadManager {

    private static final String TAG = LuckyPreloadManager.class.getSimpleName();

    private static final String PREF_KEY_THEME_PACKAGE_NAME = "preload_theme_package_name";

    @SuppressWarnings("PointlessBooleanExpression")
    private static final boolean DEBUG_LUCKY_PRELOAD = false && BuildConfig.DEBUG;

    private volatile static LuckyPreloadManager sInstance;
    private List<Map<String, String>> themeMapList;
    private Random mRandom = new Random();

    private Map<String, String> themeItemInfo;
    private HSKeyboardTheme themeItem;


    public static LuckyPreloadManager getInstance() {
        if (sInstance == null) {
            synchronized (LuckyPreloadManager.class) {
                if (sInstance == null) {
                    sInstance = new LuckyPreloadManager();
                }
            }
        }
        return sInstance;
    }

    private Map<String, String> getRandomThemeName() {
        if (themeMapList.isEmpty()) {
            return null;
        }
        int randIndex = mRandom.nextInt(themeMapList.size());
        return themeMapList.get(randIndex);
    }

    private void downloadFileAsync(final File output, final String url) {
        ConcurrentUtils.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                final HSHttpConnection connection = new HSHttpConnection(url, HttpRequest.Method.GET);
                connection.setDownloadFile(output);
                if (DEBUG_LUCKY_PRELOAD) {
                    connection.setDataReceivedListener(new HSHttpConnection.OnDataReceivedListener() {
                        @Override
                        public void onDataReceived(HSHttpConnection hsHttpConnection, byte[] bytes, long l, long l1) {
                            HSLog.d(TAG, "bytes.length = " + bytes.length + " l = " + l + " l1 = " + l1);
                        }
                    });
                }
                connection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
                    @Override
                    public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                        if (hsHttpConnection.isSucceeded()) {
                            HSLog.d(TAG, "File download success");
                        } else {
                            HSLog.d(TAG, "File download failed");
                        }
                    }

                    @Override
                    public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                        HSLog.d(TAG, "File download failed error = " + hsError.getMessage());
                    }
                });
                connection.startSync();
            }
        });
    }

    private boolean isCompleteDownload(@NonNull File file) {
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeFile(file.getPath(), option);
        } catch (OutOfMemoryError error) {
            CrashlyticsCore.getInstance().logException(error);
            return false;
        }

        boolean downloaded = !(option.outWidth <= 0 || option.outHeight <= 0);
        HSLog.d(TAG, "download successfully = " + downloaded);

        return downloaded;
    }

    private void resetThemeInfo() {
        HSPreferenceHelper.getDefault().putString(PREF_KEY_THEME_PACKAGE_NAME, "");
    }

    @SuppressWarnings("unchecked")
    private boolean restoreThemeInfo() {
        String packageName = HSPreferenceHelper.getDefault().getString(PREF_KEY_THEME_PACKAGE_NAME, "");
        if (packageName.isEmpty()) {
            resetThemeInfo();
            return false;
        }
        return true;
    }

    /*
    * should be called once lucky activity onStart
     */
    @SuppressWarnings("unchecked")
    public void refreshConfig() {
        ConcurrentUtils.postOnSingleThreadExecutor(new Runnable() {
            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                {
                    try {
                        themeMapList = (List<Map<String, String>>) HSConfig.getList("Application", "Lucky", "PriorityThemes");
                    } catch (Exception e) {
                        HSLog.e("lucky theme map error");
                    }
                    if (themeMapList == null) {
                        themeMapList = new ArrayList<>();
                    }
                }
            }
        });
    }

    private boolean shouldRefreshTheme() {
        File themeIcon = new File(Utils.getDirectory(ThemeView.THEME_DIRECTORY), ThemeView.ICON);
        File banner = new File(Utils.getDirectory(ThemeView.THEME_DIRECTORY), ThemeView.BANNER);

        boolean iconFile = themeIcon.exists() && isCompleteDownload(themeIcon);
        boolean themeFile = banner.exists() && isCompleteDownload(banner);

        if (iconFile && themeFile) {
            if (!restoreThemeInfo()) {
                return true;
            }
        }

        return !(iconFile && themeFile);
    }

    public void refreshTheme(final boolean force) {
        ConcurrentUtils.postOnSingleThreadExecutor(new Runnable() {
            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                if (!shouldRefreshTheme() && !force) {
                    return;
                }
                File themeIcon = new File(Utils.getDirectory(ThemeView.THEME_DIRECTORY), ThemeView.ICON);
                if (themeIcon.exists()) {
                    boolean deleted = themeIcon.delete();
                    HSLog.d(TAG, "Delete file " + themeIcon + ": " + deleted);
                }

                File theme = new File(Utils.getDirectory(ThemeView.THEME_DIRECTORY), ThemeView.BANNER);
                if (theme.exists()) {
                    boolean deleted = theme.delete();
                    HSLog.d(TAG, "Delete file " + theme + ": " + deleted);
                }

                themeItemInfo = getRandomThemeName();
                if (themeItemInfo == null || themeItemInfo.isEmpty()) {
                    resetThemeInfo();
                    return;
                }

                //save theme info
                String themeName = themeItemInfo.get("Name");
                HSPreferenceHelper.getDefault().putString(PREF_KEY_THEME_PACKAGE_NAME, themeName);

                String themeIconUrl = themeItemInfo.get(ThemeView.ICON);
                downloadFileAsync(themeIcon, themeIconUrl);

                String banner = "";
                for (HSKeyboardTheme hsKeyboardTheme : HSKeyboardThemeManager.getAllKeyboardThemeList()) {
                    if (hsKeyboardTheme.mThemeName.equals(themeName)) {
                        themeItem = hsKeyboardTheme;
                        banner = hsKeyboardTheme.getThemeBannerImgUrl();
                        if (TextUtils.isEmpty(banner)) {
                            banner = hsKeyboardTheme.getLargePreivewImgUrl();
                        }
                    }
                }
                downloadFileAsync(theme, banner);
            }
        });
    }


    public HSKeyboardTheme getThemeInfo() {
        File themeIcon = new File(Utils.getDirectory(ThemeView.THEME_DIRECTORY), ThemeView.ICON);
        File theme = new File(Utils.getDirectory(ThemeView.THEME_DIRECTORY), ThemeView.BANNER);
        if (themeItemInfo == null || themeItemInfo.isEmpty() ||
                !themeIcon.exists() || !theme.exists()) {
            return null;
        }
        if (!isCompleteDownload(themeIcon) || !isCompleteDownload(theme)) {
            return null;
        }

        return themeItem;
    }

}
