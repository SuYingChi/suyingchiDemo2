package com.ihs.inputmethod.feature.apkupdate;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ihs.app.framework.HSApplication;
import com.ihs.app.utils.HSMarketUtils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.feature.common.Utils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.utils.RippleDrawableUtils;
import com.ihs.inputmethod.utils.CommonUtils;
import com.ihs.keyboardutils.alerts.HSAlertDialog;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class ApkUtils {

    private static final String PREF_KEY_UPDATE_APK_VERSION_CODE = "update_apk_version_code";
    private static final String PREF_KEY_UPDATE_ALERT_LAST_SHOWN_TIME = "update_alert_last_shown_time";
    private static final String PREF_KEY_RATE_ALERT_BUTTON_CLICKED_VERSION_CODE_SET = "perf_rate_alert_button_click_version_code_set";
    private static final long UPDATE_ALERT_SHOW_INTERVAL_IN_MILLIS = 24 * 60 * 60 * 1000;

    public static void startInstall(Context context, Uri uri) {
        Intent install = new Intent(Intent.ACTION_VIEW);
        install.setDataAndType(uri, "application/vnd.android.package-archive");
        install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(install);
    }

    public static PackageInfo getApkInfo(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        return pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
    }

    public static File getDefaultLocalFile(Context context) {
        try {
            File file = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            return new File(file, UpdateConfig.getDefault().getLocalFileName());
        } catch (Exception e) {
            // noting
        }
        return new File(context.getFilesDir(), UpdateConfig.getDefault().getLocalFileName());
    }

    public static boolean isLocalApkReady(String apkPath, Context context) {
        if (apkPath == null)
            return false;
        PackageInfo apkInfo = getApkInfo(context, apkPath);
        if (apkInfo == null) {
            return false;
        }
        String localPackage = context.getPackageName();
        if (apkInfo.packageName.equals(localPackage)) {
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(localPackage, 0);
                // 1 Apk file is newer than current.
                // 2 Apk file has same version code with config load from server.
                if (apkInfo.versionCode > packageInfo.versionCode && apkInfo.versionCode >= getLatestVersionCode()) {
                    return true;
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean checkAndShowUpdateAlert() {
        return checkAndShowUpdateAlert(false);
    }

    public static boolean checkAndShowUpdateAlert(final boolean force) {
        if (shouldUpdate()) {
            if (force) {
                showUpdateAlert(null);
            } else if (checkTimeout()) {
                showUpdateAlert(null);
            }

            return true;
        }

        return false;
    }

    private static boolean checkTimeout() {
        final long lastShowTime = getUpdateAlertLastShownTime();
        if (System.currentTimeMillis() - lastShowTime >= UPDATE_ALERT_SHOW_INTERVAL_IN_MILLIS) {
            return true;
        }

        return false;
    }

    /**
     * If current App not from Market, download directly.
     */
    public static void doUpdate() {
        if (HSMarketUtils.isMarketInstalled("Google")) {
            HSMarketUtils.browseAPP("Google", HSApplication.getContext().getPackageName());
        } else {
            long downloadId;

            if ((downloadId = ApkDownloadManager.getInstance().checkDownload(UpdateConfig.getDefault())) > 0) {
                HSLog.d("Start to download update apk with downloadId: " + downloadId);
            } else {
                HSLog.e("Can't to download update apk with error code: " + downloadId);
            }
        }
    }

    /**
     * Indicates whether DownloadManager is enabled.
     */
    static boolean isDownloadManagerReady(Context context) {
        try {
            int state = context.getPackageManager().getApplicationEnabledSetting("com.android.providers.downloads");

            if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                    || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Get download file status by id.
     *
     * @param downloadId an ID for the download, unique across the system.
     *                   This ID is used to make future calls related to this download.
     * @return int
     * @see DownloadManager#STATUS_PENDING
     * @see DownloadManager#STATUS_PAUSED
     * @see DownloadManager#STATUS_RUNNING
     * @see DownloadManager#STATUS_SUCCESSFUL
     * @see DownloadManager#STATUS_FAILED
     */
    public static int getDownloadStatus(DownloadManager manager, long downloadId) {
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor c = manager.query(query);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    return c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));

                }
            } finally {
                c.close();
            }
        }
        return -1;
    }

    public static boolean shouldUpdate() {
        return shouldCheckUpdateNow() && isNewVersionAvailable();
    }

    public static boolean shouldCheckUpdateNow() {
        return isUpdateEnabled() && (HSMarketUtils.isMarketInstalled("Google") /** 安装了google play则可以下载 */
                || (CommonUtils.isNetworkAvailable(-1) && !TextUtils.isEmpty(UpdateConfig.getDefault().getDownLoadUrl())) /** 未安装google 则需要联网同时下载地址不为空 */);
    }

    public static boolean isGooglePlayAvailable() {
        return (HSMarketUtils.isMarketInstalled("Google") && CommonUtils.isNetworkAvailable(-1));
    }

    public static boolean isUpdateEnabled() {
        return HSApplication.getContext().getResources().getBoolean(R.bool.apk_update_enabled);
    }

    public static void startUpdate(final Context context) {
        File file = ApkUtils.getDefaultLocalFile(context);
        if (ApkUtils.isLocalApkReady(file.getPath(), context)) {
            startInstall(context, Uri.fromFile(file));
            return;
        }
    }

    public static boolean isNewVersionAvailable() {
        String localPackage = HSApplication.getContext().getPackageName();
        int currentVersionCode = 0;
        int latestVersionCode = 0;

        try {
            PackageInfo packageInfo = HSApplication.getContext().getPackageManager().getPackageInfo(localPackage, 0);
            currentVersionCode = packageInfo.versionCode;
            latestVersionCode = getLatestVersionCode();

            if (currentVersionCode < latestVersionCode) {
                HSLog.d("Has update, current version code: " + currentVersionCode + ", latestVersionCode: " + latestVersionCode);
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        HSLog.d("No update, current version code: " + currentVersionCode + ", latestVersionCode: " + latestVersionCode);
        return false;
    }

    public static int getLatestVersionCode() {
        final int latestVersionCode = HSConfig.optInteger(0, "Application", "Update", "LatestVersionCode");
        HSLog.d("latestVersionCode: " + latestVersionCode);
        return latestVersionCode;
    }

    public static int getUpdateApkVersionCode() {
        return HSPreferenceHelper.getDefault().getInt(PREF_KEY_UPDATE_APK_VERSION_CODE, 0);
    }

    public static void saveUpdateApkVersionCode() {
        HSPreferenceHelper.getDefault().putInt(PREF_KEY_UPDATE_APK_VERSION_CODE, getLatestVersionCode());
    }

    private static long getUpdateAlertLastShownTime() {
        return HSPreferenceHelper.getDefault().getLong(PREF_KEY_UPDATE_ALERT_LAST_SHOWN_TIME, 0);
    }

    public static void saveUpdateAlertLastShownTime() {
        HSPreferenceHelper.getDefault().putLong(PREF_KEY_UPDATE_ALERT_LAST_SHOWN_TIME, System.currentTimeMillis());
    }

    private static void setRateAlertButtonClickedInCurrentAppVersion() {
        int currentVersionCode = Utils.getVersionCode();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext());
        Set<String> ratedVersionCodeSet = new HashSet<>(sharedPreferences.getStringSet(PREF_KEY_RATE_ALERT_BUTTON_CLICKED_VERSION_CODE_SET, new HashSet<String>()));
        ratedVersionCodeSet.add(String.valueOf(currentVersionCode));
        sharedPreferences.edit().putStringSet(PREF_KEY_RATE_ALERT_BUTTON_CLICKED_VERSION_CODE_SET, ratedVersionCodeSet).apply();
    }

    public static boolean isRateAlertButtonClickedInCurrentAppVersion() {
        int currentVersionCode = Utils.getVersionCode();
        Set<String> ratedVersionCodeSet = PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).getStringSet(PREF_KEY_RATE_ALERT_BUTTON_CLICKED_VERSION_CODE_SET, new HashSet<String>());
        return ratedVersionCodeSet.contains(String.valueOf(currentVersionCode));
    }

    @SuppressLint("InflateParams")
    public static void showCustomRateAlert(final View.OnClickListener rateButtonClickListener) {
        LayoutInflater inflater = (LayoutInflater) HSApplication.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.apk_custom_rate_alert, null, false);
        final android.support.v7.app.AlertDialog alertDialog = HSAlertDialog.build().setView(view).setCancelable(false).create();
        Button positiveBtn = (Button) view.findViewById(R.id.btn_rate);
        positiveBtn.setBackgroundDrawable(RippleDrawableUtils.getContainDisableStatusCompatRippleDrawable(
                HSApplication.getContext().getResources().getColor(R.color.custom_rate_alert_button_bg),
                HSApplication.getContext().getResources().getColor(R.color.guide_bg_disable_color),
                HSApplication.getContext().getResources().getDimension(R.dimen.apk_update_alert_button_radius)));
        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (HSMarketUtils.isMarketInstalled("Google")) {
                    HSMarketUtils.browseAPP("Google", HSApplication.getContext().getPackageName());
                    setRateAlertButtonClickedInCurrentAppVersion();
                } else {
                    Toast.makeText(HSApplication.getContext(), HSApplication.getContext().getString(R.string.custom_rate_alert_toast_text), Toast.LENGTH_SHORT).show();
                }
                if (rateButtonClickListener != null) {
                    rateButtonClickListener.onClick(v);
                }
                alertDialog.dismiss();
            }
        });
        ImageView closeIcon = (ImageView) view.findViewById(R.id.iv_close_image);
        closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    public static void showUpdateAlert(@Nullable final View.OnClickListener updateButtonClickListener) {
        saveUpdateAlertLastShownTime();

        // Create custom dialog object
        final AlertDialog dialog = new AlertDialog.Builder(HSApplication.getContext()).create();

        // hide to default title for Dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // inflate the layout dialog_layout.xml and set it as contentView
        LayoutInflater inflater = (LayoutInflater) HSApplication.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.apk_update_alert, null, false);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setView(view);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        // Set message
        TextView message = (TextView) view.findViewById(R.id.txt_dialog_message);
        message.setText(UpdateConfig.getDefault().getDescription());

        Button positiveBtn = (Button) view.findViewById(R.id.update_button);
        positiveBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doUpdate();
                        if (updateButtonClickListener != null) {
                            updateButtonClickListener.onClick(v);
                        }
                        dialog.dismiss();
                    }
                });

        Button negativeBtn = (Button) view.findViewById(R.id.cancel_button);
        negativeBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }
}
