package com.ihs.inputmethod.uimodules.ui.fonts.homeui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacter;
import com.ihs.inputmethod.api.utils.HSFileUtils;
import com.ihs.inputmethod.specialcharacter.SpecialCharacterManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.adbuffer.AdLoadingView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeActivity.FONT_DOWNLOAD_SUCCESS_NOTIFICATION;

/**
 * Created by guonan.lv on 17/8/21.
 */

public class FontDownloadManager {
    public static final String FONT_DOWNLOAD_JSON_SUFFIX = ".json";
    public static final String ASSETS_FONT_FILE_PATH = "Fonts";
    private static final String DOWNLOADED_FONT_NAME_JOIN = "download_font_name_join";
    private static FontDownloadManager instance;

    private List<FontModel> downloadFonts = new ArrayList<>();

    private FontDownloadManager() {
        String downloadedFontNameJoin = HSPreferenceHelper.getDefault().getString(DOWNLOADED_FONT_NAME_JOIN, "");
        if (!downloadedFontNameJoin.isEmpty()) {
            List<String> downloadFontNameList = Arrays.asList(downloadedFontNameJoin.split("\t"));
            for (String downloadFontName : downloadFontNameList) {
                HSLog.e("eeee", downloadFontName);
                HSSpecialCharacter downloadSpecialCharacter = readSpecialCharacterFromFile(downloadFontName);
                if (downloadSpecialCharacter != null) {
                    SpecialCharacterManager.getInstance().addSpecialFont(downloadSpecialCharacter);
                }
            }
        }
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

    public void updateSpecialCharacterList(FontModel newFontModel) {
        HSGlobalNotificationCenter.sendNotificationOnMainThread(FONT_DOWNLOAD_SUCCESS_NOTIFICATION);
        SpecialCharacterManager.getInstance().addSpecialFont(newFontModel.getHsSpecialCharacter());

        String downloadedFontNameJoin = HSPreferenceHelper.getDefault().getString(DOWNLOADED_FONT_NAME_JOIN, "");
        String downloadedFontName = newFontModel.getFontName();
        downloadedFontNameJoin += downloadedFontNameJoin.isEmpty() ? downloadedFontName : "\t" + downloadedFontName;
        HSPreferenceHelper.getDefault().putString(DOWNLOADED_FONT_NAME_JOIN, downloadedFontNameJoin);

    }

    public List<FontModel> getDownloadFonts() {
        return downloadFonts;
    }

    public void setDownloadFonts(List<FontModel> fonts) {
        if (!fonts.isEmpty()) {
            if (downloadFonts.isEmpty()) {
                downloadFonts = fonts;
                for (FontModel fontModel : downloadFonts) {
                    SpecialCharacterManager.getInstance().addSpecialFont(fontModel.getHsSpecialCharacter());
                }
            }
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

        HSHttpConnection connection = new HSHttpConnection("http://testauto.s3.amazonaws.com/test.json");
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
        HSSpecialCharacter hsSpecialCharacter = readSpecialCharacterFromFile(fontModel.getFontName());
        if(hsSpecialCharacter == null) {
            return;
        }
        FontModel newFontModel = new FontModel(hsSpecialCharacter);
        updateSpecialCharacterList(newFontModel);
    }

    private HSSpecialCharacter readSpecialCharacterFromFile(String fontName) {
        HSSpecialCharacter hsNewSpecialCharacter = new HSSpecialCharacter();
        JSONObject supportFont = new JSONObject();
        String fontFileName = this.getFontModelDownloadFilePath(fontName);
        if(fontFileName != null) {
            BufferedInputStream bufferedInputStream = null;
            File jsonFile = new File(fontFileName);
            int size = (int) jsonFile.length();
            byte[] font = new byte[size];
            try {
                bufferedInputStream = new BufferedInputStream(new FileInputStream(jsonFile));
                bufferedInputStream.read(font, 0, font.length);
                try {
                    supportFont = (new JSONObject(new String(font)));
                } catch (JSONException eJson) {
                    eJson.printStackTrace();
                }
            } catch (IOException eIO) {
                eIO.printStackTrace();
            } finally {
                if(bufferedInputStream != null) {
                    try {
                        bufferedInputStream.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
        try {
            hsNewSpecialCharacter.name = fontName;
            hsNewSpecialCharacter.example = supportFont.getString("FontExample");
            hsNewSpecialCharacter.mapNormalToFont = supportFont.getJSONObject("Content");
            return hsNewSpecialCharacter;
        } catch (JSONException eJson) {
            eJson.printStackTrace();
            return null;
        } finally {

        }
    }
}
