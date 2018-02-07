package com.ihs.inputmethod.uimodules.ui.customize.util;

import android.net.Uri;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.connection.HSServerAPIConnection;
import com.ihs.commons.connection.httplib.HttpRequest;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.api.utils.HSYamlUtils;
import com.ihs.inputmethod.uimodules.ui.customize.WallpaperInfo;
import com.ihs.inputmethod.uimodules.ui.customize.view.CategoryInfo;
import com.ihs.inputmethod.uimodules.ui.gif.common.control.UIController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by guonan.lv on 17/9/4.
 */

public class WallpaperDownloadEngine {
    private static final String CUSTOMIZE_PREFS = "com.honeycomb.launcher.customize.prefs"; // Process ":customize"


    public interface OnLoadWallpaperListener {
        void onLoadFinished(List<WallpaperInfo> wallpaperInfoList, int totalSize);

        void onLoadFailed();
    }

    private static final HSPreferenceHelper sPrefs = HSPreferenceHelper.create(HSApplication.getContext(),
            CUSTOMIZE_PREFS);

    private static final String PREF_WALLPAPER_HOT_NEXT_PAGE = "pref_wallpaper_hot_next_page";
    private static final String PREF_WALLPAPER_CATEGORY_NEXT_PAGE = "pref_wallpaper_category_next_page_";

    private static final String TAG = "WallpaperDownloadEngine";
    private static final int DEFAULT_PAGE_SIZE = 30;

    private static final String WALLPAPER_URL = HSConfig.getString("Application", "WallpaperAPIURL");
    private static final String SUFFIX_CATEGORY = "categories/";
    private static List<Map<String, ?>> sConfig = (List<Map<String, ?>>) HSConfig.getList("Application", "Wallpapers");

    public static void getNextCategoryWallpaperList(int categoryIndex, OnLoadWallpaperListener listener) {
        if (HSYamlUtils.convertObjectToBool(sConfig.get(categoryIndex).get("FromConfig"))) {
            getNextCategoryWallpaperListFromConfig(categoryIndex, listener);
        } else {
            getNextCategoryWallpaperListFromNetwork(categoryIndex, listener);
        }
    }

    private static void getNextCategoryWallpaperListFromConfig(int categoryIndex, OnLoadWallpaperListener listener) {
        try {

            int nextPage = sPrefs.getInt(PREF_WALLPAPER_CATEGORY_NEXT_PAGE + categoryIndex, 1);
            final int startIndex = (nextPage - 1) * DEFAULT_PAGE_SIZE;
            final int endIndex = startIndex + DEFAULT_PAGE_SIZE;

            List<WallpaperInfo> wallpaperInfoList = new ArrayList<>();
            List<Map<String, String>> wallpaperList = (List<Map<String, String>>) sConfig.get(categoryIndex).get("WallpaperList");
            int configTotalSize = wallpaperList.size();
            int lastIndex = configTotalSize <= endIndex ? configTotalSize : endIndex;
            for (int i = startIndex; i < lastIndex; i++) {
                Map<String, String> map = wallpaperList.get(i);
                WallpaperInfo wallpaperInfo = WallpaperInfo.newConfigWallpaper(map.get("HDUrl"), map.get("ThumbUrl"));
                CategoryInfo category = CategoryInfo.ofConfig(sConfig.get(categoryIndex));
                if (category != null) {
                    wallpaperInfo.setCategory(category);
                }
                wallpaperInfoList.add(wallpaperInfo);
            }

            nextPage = lastIndex >= configTotalSize ? 1 : (nextPage + 1);
            sPrefs.putInt(PREF_WALLPAPER_CATEGORY_NEXT_PAGE + categoryIndex, nextPage);

            // 延迟500s 模拟下载
            UIController.getInstance().getUIHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.onLoadFinished(wallpaperInfoList,configTotalSize);
                    }
                }
            },500);
        } catch (Exception e) {
            if (listener != null) {
                listener.onLoadFailed();
            }
        }
    }

    private static void getNextCategoryWallpaperListFromNetwork(final int categoryIndex, final OnLoadWallpaperListener listener) {
        final List<WallpaperInfo> wallpaperInfoList = new ArrayList<>();
        final int nextPage = sPrefs.getInt(PREF_WALLPAPER_CATEGORY_NEXT_PAGE + categoryIndex, 1);
        String url = WALLPAPER_URL + SUFFIX_CATEGORY + Uri.encode(sConfig.get(categoryIndex).get("Identifier").toString());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("page", nextPage);
            jsonObject.put("page_size", DEFAULT_PAGE_SIZE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HSLog.d(TAG, url);
        final HSServerAPIConnection connection = new HSServerAPIConnection(url, HttpRequest.Method.GET, jsonObject);

        connection.setConnectTimeout(30 * 1000);
        connection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                HSLog.d(TAG, "load success : " + hsHttpConnection.getURL());
                try {
                    JSONObject bodyJSON = hsHttpConnection.getBodyJSON();
                    JSONArray wallpaperArray = bodyJSON.getJSONObject("data").getJSONArray("medias");

                    for (int i = 0; i < wallpaperArray.length(); i++) {
                        JSONObject wallpaper = wallpaperArray.getJSONObject(i);
                        WallpaperInfo wallpaperInfo = WallpaperInfo.newOnlineWallpaper(
                                wallpaper.getJSONObject("images").getString("hdurl"),
                                wallpaper.getJSONObject("images").getString("thumburl"), "",
                                wallpaper.getInt("downloads") + wallpaper.getInt("views"));
                        CategoryInfo category = CategoryInfo.ofConfig(sConfig.get(categoryIndex));
                        if (category != null) {
                            wallpaperInfo.setCategory(category);
                        }

                        wallpaperInfoList.add(wallpaperInfo);
                    }

                    int nextPage = bodyJSON.getJSONObject("data").getJSONObject("page").getInt("page") + 1;
                    int totalPage = bodyJSON.getJSONObject("data").getJSONObject("page").getInt("page_count");
                    int totalSize = bodyJSON.getJSONObject("data").getJSONObject("page").getInt("total");
                    if (totalPage < nextPage) {
                        nextPage = 1;
                    }
                    sPrefs.putInt(PREF_WALLPAPER_CATEGORY_NEXT_PAGE + categoryIndex, nextPage);

                    if (listener != null) {
                        listener.onLoadFinished(wallpaperInfoList, totalSize);
                    }
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();

                    if (listener != null) {
                        listener.onLoadFailed();
                    }
                }
            }

            @Override
            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                HSLog.e(TAG, "load failed : " + hsHttpConnection.getURL() + " : " + hsError.getMessage());
                if (listener != null) {
                    listener.onLoadFailed();
                }
            }
        });
        connection.startAsync();
    }
}
