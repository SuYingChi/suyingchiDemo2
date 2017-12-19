package com.ihs.inputmethod.uimodules.ui.facemoji;

import android.preference.PreferenceManager;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSError;
import com.ihs.inputmethod.api.utils.HSNetworkConnectionUtils;
import com.ihs.inputmethod.api.utils.HSZipUtils;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiCategory;
import com.ihs.inputmethod.uimodules.ui.gif.common.control.UIController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipException;

public class FacemojiDownloadManager {
    public final static String FACEMOJI_CATEGORY_DOWNLOADED = "FACEMOJI_CATEGORY_DOWNLOADED";
    public final static String FACEMOJI_CATEGORY_BUNDLE_KEY = "facemojiCategory";

    private static class FacemojiDownloadManagerHoler {
        static FacemojiDownloadManager instance = new FacemojiDownloadManager();
    }

    private List<String> downloadedFacemojiCateogryNameList = new ArrayList<>();

    public static FacemojiDownloadManager getInstance() {
        return FacemojiDownloadManagerHoler.instance;
    }

    private FacemojiDownloadManager() {
    }


    private String getRemoteResourcePath(String name) {
        return HSConfig.optString("", "Application", "Server", "FacemojiBasePath") + name + "/" + name + ".zip";
    }

    public String getRemoteTabIconPath(String name) {
        return HSConfig.optString("", "Application", "Server", "FacemojiBasePath") + name + "/" + "pack_" + name + ".png";
    }

    public void startDownloadFacemojiResource(FacemojiCategory facemojiCategory,IFacemojiCategoryDownloadListener facemojiCategoryDownloadListener) {
        if (!HSNetworkConnectionUtils.isNetworkConnected()) {
            if (facemojiCategoryDownloadListener != null) {
                facemojiCategoryDownloadListener.onDownloadFailed(facemojiCategory,IFacemojiCategoryDownloadListener.NET_WORK_UNAVALIABLE_FAILED);
            }
            return;
        }

        if (downloadedFacemojiCateogryNameList.contains(facemojiCategory.getName())){
            if (facemojiCategoryDownloadListener != null) {
                facemojiCategoryDownloadListener.onDownloadFailed(facemojiCategory,IFacemojiCategoryDownloadListener.DOWNLOADING);
            }
            return;
        }

        downloadedFacemojiCateogryNameList.add(facemojiCategory.getName());

        HSHttpConnection connection = new HSHttpConnection(getRemoteResourcePath(facemojiCategory.getName()));
        File downloadFile = FacemojiManager.getFacemojiZipFile(facemojiCategory.getName());
        if (downloadFile.exists() && downloadFile.length() > 0){
            downloadFile.delete();
        }
        connection.setDownloadFile(downloadFile);
        connection.setConnectTimeout(1000 * 60);
        connection.setReadTimeout(1000 * 60 * 10);
        connection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @Override
            public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                if (hsHttpConnection.isSucceeded() && downloadFile.exists() && downloadFile.length() > 0) {
                    try {
                        HSZipUtils.unzip(downloadFile,FacemojiManager.getFacemojiCategoryDir(facemojiCategory.getName()));
                        setFacemojiCategoryDownloadedSuccess(facemojiCategory.getName());
                        List<FacemojiCategory> categories = FacemojiManager.getInstance().getCategories();
                        for (int i = 0 ; i < categories.size() ; i++ ){
                            FacemojiCategory category = categories.get(i);
                            if (category.getName().equals(facemojiCategory.getName())){
                                category.parseYaml();
                                HSBundle bundle = new HSBundle();
                                bundle.putObject(FACEMOJI_CATEGORY_BUNDLE_KEY, category);
                                HSGlobalNotificationCenter.sendNotification(FACEMOJI_CATEGORY_DOWNLOADED, bundle);
                                break;
                            }
                        }

                        if (facemojiCategoryDownloadListener != null) {
                            facemojiCategoryDownloadListener.onDownloadSuccess(facemojiCategory);
                        }
                    } catch (ZipException e) {
                        e.printStackTrace();
                    }
                }
                downloadFile.delete();
                downloadedFacemojiCateogryNameList.remove(facemojiCategory.getName());
            }

            @Override
            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                downloadedFacemojiCateogryNameList.remove(facemojiCategory.getName());
                if (facemojiCategoryDownloadListener != null) {
                    facemojiCategoryDownloadListener.onDownloadFailed(facemojiCategory,IFacemojiCategoryDownloadListener.CONNECTION_FAILED);
                }
            }
        });
        UIController.getInstance().getUIHandler().post(new Runnable() {
            @Override
            public void run() {
                connection.startAsync();
            }
        });
    }

    /**
     * 设置下载失败，让其下次再重新下载并解压
     * @param facemojiCategoryName
     */
    public static void setFacemojiCategoryDownloadedFailed(String facemojiCategoryName) {
        PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).edit().putBoolean("FacemojiCategory_" + facemojiCategoryName + "_DownloadedSuccess", false).apply();
    }

    public static void setFacemojiCategoryDownloadedSuccess(String facemojiCategoryName) {
        PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).edit().putBoolean("FacemojiCategory_" + facemojiCategoryName + "_DownloadedSuccess", true).apply();
    }

    public static boolean isFacemojiCategoryDownloadedSuccess(String facemojiCategoryName) {
        return PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).getBoolean("FacemojiCategory_" + facemojiCategoryName + "_DownloadedSuccess", false);
    }

    public interface IFacemojiCategoryDownloadListener {
        int NET_WORK_UNAVALIABLE_FAILED = 1;
        int DOWNLOADING = 2;
        int CONNECTION_FAILED = 3;
        void onDownloadSuccess(FacemojiCategory facemojiCategory);
        void onDownloadFailed(FacemojiCategory facemojiCategory,int failedCode);
    }
}
