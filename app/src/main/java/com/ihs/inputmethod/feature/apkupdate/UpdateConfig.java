package com.ihs.inputmethod.feature.apkupdate;

import android.text.TextUtils;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.utils.CommonUtils;

import java.util.List;

public class UpdateConfig {

    private boolean mShowNotification;
    private boolean mShowInDownloadUI;
    private boolean mDownloadOnlyWifi;

    private String mDownLoadUrl;
    private String mLocalFileName;
    private CharSequence mTitle;
    private String mDescription;

    // If true we check version code.
    private boolean mUpdateMode;

    private UpdateConfig(){
      mShowNotification = true;
      mShowInDownloadUI = true;
      mDownloadOnlyWifi = true;
    }

    /**
     *  Url config changed after app update success, so don't config md5 key!
     *
     * @return
     */
    public static UpdateConfig getDefault() {
        UpdateConfig config = new UpdateConfig();
        config.mDownLoadUrl = HSConfig.optString("", "Update", "DownloadUrl");
        config.mLocalFileName = CommonUtils.getMD5(config.mDownLoadUrl);
        config.mTitle = HSConfig.optString(HSApplication.getContext().getString(R.string.apk_update_alert_title), "Application", "Update", "NormalAlert", "Title");
        config.mUpdateMode = true;
        config.mDescription = getDescriptionConfig();
        return config;
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public boolean isShowNotification() {
//        return mShowNotification;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    public boolean isShowInDownloadUI() {
        return mShowInDownloadUI;
    }

    public boolean isDownloadOnlyWifi() {
        return mDownloadOnlyWifi;
    }

    public boolean isUpdateMode() {
        return mUpdateMode;
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public void setDownloadOnlyWifi(boolean downloadOnlyWifi) {
//        mDownloadOnlyWifi = downloadOnlyWifi;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    public String getDownLoadUrl() {
        return mDownLoadUrl;
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public void setDownLoadUrl(String mDownLoadUrl) {
//        this.mDownLoadUrl = mDownLoadUrl;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    public String getLocalFileName() {
        return mLocalFileName;
    }

    public CharSequence getTitle() {
        return mTitle;
    }

    public CharSequence getDescription() {
        return mDescription;
    }

    @Override
    public String toString() {
        return "downloadUrl:\n" + mDownLoadUrl;
    }


    public String getMD5Key() {
        return "";
    }

    private static String getDescriptionConfig() {
        StringBuilder description = new StringBuilder();

        @SuppressWarnings("unchecked") List<String> descriptionList = (List<String>) HSConfig.getList("Application", "Update", "NormalAlert", "Description");

        if (descriptionList != null && !descriptionList.isEmpty()) {
            for (int i = 0; i < descriptionList.size(); ++i) {
                description.append(descriptionList.get(i)).append((i < (descriptionList.size() - 1)) ? "\n" : "");
            }
        }

        if (TextUtils.isEmpty(description.toString())) {
            description = new StringBuilder(HSApplication.getContext().getString(R.string.apk_update_alert_message));
        }
        return description.toString();
    }
}
