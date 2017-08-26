package com.ihs.inputmethod.uimodules.ui.fonts.homeui;

import com.ihs.commons.config.HSConfig;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacter;

import java.io.File;

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
        return getFontName() != null ? baseDownloadUrl + getFontName() + FONT_FILE_SUFFIX : null;
    }

    public boolean isFontDownloaded() {
        String fontFilePath = FontDownloadManager.getInstance().getFontModelDownloadFilePath(getFontName());
        File file = new File(fontFilePath);
        return file.exists() && file.length() > 0;

    }

}
