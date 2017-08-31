package com.ihs.inputmethod.uimodules.ui.fonts.homeui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacter;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacterManager;
import com.ihs.inputmethod.api.utils.HSFileUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.adbuffer.AdLoadingView;
import com.kc.commons.configfile.KCList;
import com.kc.commons.configfile.KCParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by guonan.lv on 17/8/21.
 */

public class FontDownloadManager {
    public static final String JSON_SUFFIX = ".json";
    public static final String ASSETS_FONT_FILE_PATH = "Fonts";
    private static final String DOWNLOADED_FONT_NAME_JOIN = "download_font_name_join";
    private static FontDownloadManager instance;

    private FontDownloadManager() {
        loadDownloadedFont();
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

    private void loadDownloadedFont() {
        File file = new File(getDownloadedFontNameList());
        KCList kcList = KCParser.parseList(file);
        if (kcList == null) {
            return;
        }
        for (int i = kcList.size()-1; i >= 0; i--) { //download order
            String downloadFontName = kcList.getString(i);
            HSSpecialCharacter downloadSpecialCharacter = readSpecialCharacterFromFile(downloadFontName);
            if (downloadSpecialCharacter != null && !HSSpecialCharacterManager.getSpecialCharacterList().isEmpty()) {
                HSSpecialCharacterManager.addSpecilCharacter(1, downloadSpecialCharacter, 14);
            }
        }
    }

    public String getFontDownloadFilePath(String fontName) {
        return HSApplication.getContext().getFilesDir() + File.separator + ASSETS_FONT_FILE_PATH + "/" + fontName + JSON_SUFFIX;
    }

    public String getDownloadedFontNameList() {
        return HSApplication.getContext().getFilesDir() + File.separator + ASSETS_FONT_FILE_PATH + "/" + DOWNLOADED_FONT_NAME_JOIN + JSON_SUFFIX;
    }

    public void updateSpecialCharacterList(FontModel newFontModel) {
        HSSpecialCharacterManager.addSpecilCharacter(1, newFontModel.getHsSpecialCharacter(), 14);

        JSONArray jsonArray = new JSONArray();
        File file = new File(getDownloadedFontNameList());
        KCList kcList = KCParser.parseList(file);
        if (kcList == null) {
            jsonArray.put(newFontModel.getFontName());
        } else {
            for (int i = 0; i < kcList.size(); i++) {
                jsonArray.put(kcList.getString(i));
            }
            jsonArray.put(newFontModel.getFontName());
        }
        writeJsonToFile(jsonArray, getDownloadedFontNameList());

    }

    public void startForegroundDownloading(Context context, final FontModel fontModel,
                                           final Drawable thumbnailDrawable, final AdLoadingView.OnAdBufferingListener onAdBufferingListener) {
        final String fontModelJsonFilePath = getFontDownloadFilePath(fontModel.getFontName());
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
        HSSpecialCharacter hsSpecialCharacter = readSpecialCharacterFromFile(fontModel.getFontName());
        if (hsSpecialCharacter == null) {
            return;
        }
        FontModel newFontModel = new FontModel(hsSpecialCharacter);
        updateSpecialCharacterList(newFontModel);
    }

    private HSSpecialCharacter readSpecialCharacterFromFile(String fontName) {
        HSSpecialCharacter hsNewSpecialCharacter = new HSSpecialCharacter();
        JSONObject supportFont = new JSONObject();
        String fontFileName = getFontDownloadFilePath(fontName);
        if (fontFileName != null) {
            supportFont = readJsonFromFile(fontFileName);
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

    private void writeJsonToFile(JSONArray jsonObject, String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            DataOutputStream out = new DataOutputStream(new FileOutputStream(
                    file));
            out.writeBytes(jsonObject.toString());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JSONObject readJsonFromFile(String fileName) {
        JSONObject jsonObject = new JSONObject();
        BufferedInputStream bufferedInputStream = null;
        File jsonFile = new File(fileName);
        int size = (int) jsonFile.length();
        byte[] jsonByte = new byte[size];
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(jsonFile));
            bufferedInputStream.read(jsonByte, 0, jsonByte.length);
            try {
                jsonObject = (new JSONObject(new String(jsonByte)));

            } catch (JSONException eJson) {
                eJson.printStackTrace();
            }
        } catch (IOException eIO) {
            eIO.printStackTrace();
        } finally {
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            return jsonObject;
        }
    }
}
