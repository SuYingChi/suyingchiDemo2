package com.ihs.inputmethod.uimodules.ui.fonts.homeui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacter;
import com.ihs.inputmethod.api.utils.HSFileUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.adbuffer.AdLoadingView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by guonan.lv on 17/8/21.
 */

public class FontDownloadManager {
    public static final String FONT_DOWNLOAD_JSON_SUFFIX = ".json";
    public static final String ASSETS_FONT_FILE_PATH = "Fonts";
    private static FontDownloadManager instance;

    private List<FontModel> downloadedFonts = new ArrayList<>();
    private List<FontModel> remoteFonts = new ArrayList<>();

    private FontDownloadManager() {
        List<Map<String, Object>> fontList = (List<Map<String, Object>>) HSConfig.getList("Application", "FontList");

    }

    public static FontDownloadManager getInstance() {
        if (instance == null) {
            synchronized (FontDownloadManager.class) {
                if (instance == null) {
                    instance = new FontDownloadManager();
                }
            }
        }
        return instance;
    }

    public String getFontModelDownloadFilePath(String fontName) {
        return HSApplication.getContext().getFilesDir() + File.separator + ASSETS_FONT_FILE_PATH + "/" + fontName + FONT_DOWNLOAD_JSON_SUFFIX;
    }

    public List<FontModel> getDownloadedFonts() {
        return downloadedFonts;
    }

    public List<FontModel> getRemoteFonts() {
        return remoteFonts;
    }

    public void updateFontList(FontModel fontModel, FontModel newFontModel) {
        downloadedFonts.add(newFontModel);
        remoteFonts.remove(fontModel);
    }

    public void setRemoteFonts(List<FontModel> fonts) {
        if (remoteFonts.isEmpty()) {
            remoteFonts = fonts;
        }
    }

    public void startForegroundDownloading(Context context, final FontModel fontModel,
                                           final Drawable thumbnailDrawable, final AdLoadingView.OnAdBufferingListener onAdBufferingListener) {
        final String fontModelJsonFilePath = getFontModelDownloadFilePath(fontModel.getFontName());
        final AdLoadingView adLoadingView = new AdLoadingView(context);
        final Resources resources = HSApplication.getContext().getResources();
        adLoadingView.configParams(null, thumbnailDrawable != null ? thumbnailDrawable : resources.getDrawable(R.drawable.ic_sticker_loading_image),
                resources.getString(R.string.sticker_downloading_label),
                resources.getString(R.string.sticker_downloading_successful),
                resources.getString(R.string.ad_placement_lucky),
                new AdLoadingView.OnAdBufferingListener() {
                    @Override
                    public void onDismiss(boolean downloadSuccess) {
                        if (downloadSuccess) {
                            if (fontModel.isFontDownloaded()) {
                                HSLog.d("sticker " + fontModel.getFontName() + " download succeed");
                            } else {
                                HSLog.e("sticker " + fontModel.getFontName() + " download error!");
                            }
                        } else {
                            // 没下载成功
                            HSHttpConnection connection = (HSHttpConnection) adLoadingView.getTag();
                            if (connection != null) {
                                connection.cancel();
                                HSFileUtils.delete(new File(fontModelJsonFilePath));
                            }
                        }
                        if (onAdBufferingListener != null) {
                            onAdBufferingListener.onDismiss(downloadSuccess);
                        }
                    }
                }, 2000, false);
        adLoadingView.showInDialog();

        HSHttpConnection connection = new HSHttpConnection(fontModel.getFontDownloadBaseURL());
        connection.setDownloadFile(HSFileUtils.createNewFile(fontModelJsonFilePath));
        connection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @Override
            public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
            }

            @Override
            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                HSLog.e("startForegroundDownloading onConnectionFailed hsError" + hsError.getMessage());
                adLoadingView.setConnectionStateText(resources.getString(R.string.foreground_download_failed));
                adLoadingView.setConnectionProgressVisibility(View.INVISIBLE);
            }
        });
        connection.setDataReceivedListener(new HSHttpConnection.OnDataReceivedListener() {
            @Override
            public void onDataReceived(HSHttpConnection hsHttpConnection, byte[] bytes, long received, long totalSize) {
                if (totalSize > 0) {
                    final float percent = (float) received * 100 / totalSize;
                    if (received >= totalSize) {
                        updateFontModel(fontModel);
                        HSGoogleAnalyticsUtils.getInstance().logAppEvent("font_download_succeed", fontModel.getFontName());
                    }
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            adLoadingView.updateProgressPercent((int) percent);
                        }
                    });
                }
            }
        });
        connection.startAsync();
        adLoadingView.setTag(connection);
    }

    public void updateFontModel(FontModel fontModel) {
        HSSpecialCharacter hsNewSpecialCharacter = new HSSpecialCharacter();
        JSONObject supportFont = new JSONObject();
        String fontFileName = this.getFontModelDownloadFilePath(fontModel.getFontName());
        if(fontFileName != null) {
            InputStream e = null;

            try {
                e = HSApplication.getContext().getAssets().open(fontFileName);
                byte[] font = new byte[e.available()];
                e.read(font);

                try {
                    supportFont = (new JSONObject(new String(font)));
                } catch (JSONException eJson) {
                    eJson.printStackTrace();
                }
            } catch (IOException eIO) {
                eIO.printStackTrace();
            } finally {
                if(e != null) {
                    try {
                        e.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
        try {
            hsNewSpecialCharacter.name = fontModel.getFontName();
            hsNewSpecialCharacter.example = supportFont.getString("FontExample");
            hsNewSpecialCharacter.mapNormalToFont = supportFont.getJSONObject("Content");
            FontModel newFontModel = new FontModel(hsNewSpecialCharacter);
            updateFontList(fontModel, newFontModel);
        } catch (JSONException eJson) {
            eJson.printStackTrace();
        }
    }
}
