package com.ihs.inputmethod.uimodules.ui.fonts.homeui;

import com.ihs.commons.config.HSConfig;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacter;

/**
 * Created by guonan.lv on 17/8/14.
 */

public class FontModel {
    private HSSpecialCharacter hsSpecialCharacter;
    private String baseDownloadUrl;
    private static final String FONT_FILE_SUFFIX = ".json";

    private boolean needDownload = true;
    private String fontName;

    public FontModel(HSSpecialCharacter specialCharacter) {
        hsSpecialCharacter = specialCharacter;
        needDownload = true;
    }

    public FontModel(String fontName) {
        this.fontName = fontName;
        baseDownloadUrl = HSConfig.getString("Application", "Server", "FontDownloadBaseURL");
        needDownload = true;
    }

    public HSSpecialCharacter getHsSpecialCharacter() {
        return hsSpecialCharacter;
    }

    public boolean getNeedDownload() {
        return needDownload;
    }

    public void setNeedDownload(boolean download) {
        needDownload = download;
    }

    public String getFontDownloadBaseURL() {
        return fontName == null ? baseDownloadUrl+fontName+FONT_FILE_SUFFIX : null;
    }

}
