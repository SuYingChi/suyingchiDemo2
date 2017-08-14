package com.ihs.inputmethod.uimodules.ui.fonts.homeui;

import android.view.View;

import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacter;

/**
 * Created by guonan.lv on 17/8/14.
 */

public class FontModel {
    private HSSpecialCharacter hsSpecialCharacter;

    private boolean needDownload = true;

    public FontModel(HSSpecialCharacter specialCharacter) {
        hsSpecialCharacter = specialCharacter;
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

}
