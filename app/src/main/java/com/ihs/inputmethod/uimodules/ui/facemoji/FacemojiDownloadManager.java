package com.ihs.inputmethod.uimodules.ui.facemoji;

import android.preference.PreferenceManager;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.connection.HSHttpConnection;
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
    private static class FacemojiDownloadManagerHoler {
        static FacemojiDownloadManager instance = new FacemojiDownloadManager();
    }

    private String remoteBasePath;
    private List<String> downloadedFacemojiCateogryNameList = new ArrayList<>();

    public static FacemojiDownloadManager getInstance() {
        return FacemojiDownloadManagerHoler.instance;
    }

    private FacemojiDownloadManager() {
        remoteBasePath = HSConfig.optString("", "Application", "Server", "FacemojiBasePath");
    }

    public void onConfigChange() {
        remoteBasePath = HSConfig.optString("", "Application", "Server", "FacemojiBasePath");
    }


    private String getRemoteResourcePath(String name) {
        return remoteBasePath + name + "/" + name + ".zip";
    }

    public String getRemoteTabIconPath(String name) {
        return remoteBasePath + name + "/" + "pack_" + name + ".png";
    }

    public void startDownloadFacemojiResource(FacemojiCategory facemojiCategory,IFacemojiCategoryDownloadListener facemojiCategoryDownloadListener) {
        if (!HSNetworkConnectionUtils.isNetworkConnected()) {
            return;
        }

        if (downloadedFacemojiCateogryNameList.contains(facemojiCategory.getName())){
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
        void onDownloadSuccess(FacemojiCategory facemojiCategory);
    }
}
