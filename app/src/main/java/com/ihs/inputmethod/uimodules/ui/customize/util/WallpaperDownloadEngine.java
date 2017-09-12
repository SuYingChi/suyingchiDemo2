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
import com.ihs.feature.common.LauncherFiles;
import com.ihs.inputmethod.uimodules.ui.customize.WallpaperInfo;

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

    public interface OnLoadWallpaperListener {
        void onLoadFinished(List<WallpaperInfo> wallpaperInfoList);

        void onLoadFailed();
    }

    private static final HSPreferenceHelper sPrefs = HSPreferenceHelper.create(HSApplication.getContext(),
            LauncherFiles.CUSTOMIZE_PREFS);

    private static final String PREF_WALLPAPER_HOT_NEXT_PAGE = "pref_wallpaper_hot_next_page";
    private static final String PREF_WALLPAPER_CATEGORY_NEXT_PAGE = "pref_wallpaper_category_next_page_";

    private static final String TAG = "WallpaperDownloadEngine";
    private static final int DEFAULT_PAGE_SIZE = 30;

    private static final String WALLPAPER_URL = HSConfig.getString("Application", "Server", "WallpaperAPIURL");
    private static final String SUFFIX_CATEGORY = "categories/";
    private static List<Map<String, ?>> sConfig = (List<Map<String, ?>>) HSConfig.getList("Application", "Wallpapers");

    public static void getNextCategoryWallpaperList(final int categoryIndex, final OnLoadWallpaperListener listener) {
        final List<WallpaperInfo> wallpaperInfoList = new ArrayList<>();
        final int nextPage = sPrefs.getInt(PREF_WALLPAPER_CATEGORY_NEXT_PAGE + categoryIndex, 1);
        String url = WALLPAPER_URL + Uri.encode(sConfig.get(categoryIndex).get("Identifier").toString());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("page", nextPage);
            jsonObject.put("page_size", DEFAULT_PAGE_SIZE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HSLog.d(TAG, url);
//        final HSHttpConnection connection = new HSHttpConnection(url);
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

//                    CategoryInfo categoryInfo = null;
//                    List<CategoryInfo> categoryInfoList = new ArrayList<>(16);
//                    if (categoryInfoList != null && categoryInfoList.size() > categoryIndex) {
//                        categoryInfo = categoryInfoList.get(categoryIndex);
//                    }
                    for (int i = 0; i < wallpaperArray.length(); i++) {
                        JSONObject wallpaper = wallpaperArray.getJSONObject(i);
                        WallpaperInfo wallpaperInfo = WallpaperInfo.newOnlineWallpaper(
                                wallpaper.getJSONObject("images").getString("hdurl"),
                                wallpaper.getJSONObject("images").getString("thumburl"), "",
                                wallpaper.getInt("downloads") + wallpaper.getInt("views"));
//                        if (categoryInfo != null) {
//                            wallpaperInfo.setCategory(categoryInfo);
//                        }

                        wallpaperInfoList.add(wallpaperInfo);
                    }

                    int nextPage = bodyJSON.getJSONObject("data").getJSONObject("page").getInt("page") + 1;
                    int totalPage = bodyJSON.getJSONObject("data").getJSONObject("page").getInt("page_count");
                    if (totalPage < nextPage) {
                        nextPage = 1;
                    }
                    sPrefs.putInt(PREF_WALLPAPER_CATEGORY_NEXT_PAGE + categoryIndex, nextPage);

                    if (listener != null) {
                        listener.onLoadFinished(wallpaperInfoList);
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
