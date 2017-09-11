package com.ihs.inputmethod.uimodules.ui.fonts.homeui;

import android.net.Uri;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacter;
import com.ihs.inputmethod.uimodules.ui.fonts.common.HSFontDownloadManager;

import java.io.File;

import static com.ihs.inputmethod.uimodules.ui.fonts.common.HSFontDownloadManager.ASSETS_FONT_FILE_PATH;
import static com.ihs.inputmethod.uimodules.ui.fonts.common.HSFontDownloadManager.JSON_SUFFIX;

/**
 * Created by guonan.lv on 17/8/14.
 */

public class FontModel {
    private HSSpecialCharacter hsSpecialCharacter;
    private String baseDownloadUrl;
    private static final String FONT_FILE_SUFFIX = ".json";

    private boolean needDownload = true;

    public FontModel(HSSpecialCharacter specialCharacter) {
        hsSpecialCharacter = specialCharacter;
        needDownload = true;
        baseDownloadUrl = HSConfig.getString("Application", "Server", "FontDownloadBaseURL");
    }

    public HSSpecialCharacter getHsSpecialCharacter() {
        return hsSpecialCharacter;
    }

    public String getFontName() {
        return hsSpecialCharacter.name;
    }

    public boolean getNeedDownload() {
        return needDownload;
    }

    public void setNeedDownload(boolean download) {
        needDownload = download;
    }

    public String getFontDownloadBaseURL() {
        return getFontName() != null ? baseDownloadUrl + Uri.encode(getFontName()) + FONT_FILE_SUFFIX : null;
    }

    public String getFontDownloadFilePath(String fontName) {
        return HSApplication.getContext().getFilesDir() + File.separator + ASSETS_FONT_FILE_PATH + "/" + fontName + JSON_SUFFIX;
    }

    public boolean isFontDownloaded() {
        if (!needDownload) { //原先内置的字体存放路径在assets中，所以通过这种方式判断是否内置字体
            return true;
        }
        String fontFilePath = HSFontDownloadManager.getInstance().getFontDownloadFilePath(getFontName());
        File file = new File(fontFilePath);
        return file.exists() && file.length() > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FontModel fontModel = (FontModel) o;
        return hsSpecialCharacter.name.equals(fontModel.hsSpecialCharacter.name);
    }

    @Override
    public int hashCode() {
        return hsSpecialCharacter.name.hashCode();
    }
}
