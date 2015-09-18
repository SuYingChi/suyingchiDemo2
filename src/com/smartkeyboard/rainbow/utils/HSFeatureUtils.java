package com.smartkeyboard.rainbow.utils;

import android.graphics.drawable.Drawable;

import com.ihs.inputmethod.extended.api.HSKeyboard;
import com.ihs.inputmethod.extended.theme.HSKeyboardThemeManager;

public class HSFeatureUtils {

    public static final int FEATURE_ALPHABET = 0;
    public static final int FEATURE_SUPERMOJI = 1;
    public static final int FEATURE_STICKER = 2;

    private static HSFeatureUtils mInstance = new HSFeatureUtils();

    private HSFeatureUtils() {
    }

    public void enableFeature(final int feature) {
        switch (feature) {
            case FEATURE_ALPHABET:
                enableAlphabetKeyboard();
                break;

            case FEATURE_SUPERMOJI:
                enableSupermoji();
                break;

            case FEATURE_STICKER:
                enableSticker();
                break;
        }
    }

    private void enableAlphabetKeyboard() {
        Drawable iconShowKeyboard = HSKeyboardThemeManager.getStyledAssetDrawable(null, HSKeyboardThemeManager.TABBAR_SHOW_KEYBOARD_ICON);
        Drawable iconShowKeyboardPressed = HSKeyboardThemeManager.getStyledAssetDrawable(null, HSKeyboardThemeManager.TABBAR_SHOW_KEYBOARD_ICON_CHOSED);
        HSKeyboard.getInstance().setShowKeyboardBtnImage(iconShowKeyboard, iconShowKeyboardPressed);

    }

    private void enableSupermoji() {
        Drawable iconShowKeyboard = HSKeyboardThemeManager.getStyledAssetDrawable(null, HSKeyboardThemeManager.TABBAR_SUPERMOJI_ICON);
        Drawable iconShowKeyboardPressed = HSKeyboardThemeManager.getStyledAssetDrawable(null, HSKeyboardThemeManager.TABBAR_SUPERMOJI_ICON_CHOSED);
        HSKeyboard.getInstance().setShowSupermojiBtnImage(iconShowKeyboard, iconShowKeyboardPressed);
    }

    private void enableSticker() {
        Drawable iconShowKeyboard = HSKeyboardThemeManager.getStyledAssetDrawable(null, HSKeyboardThemeManager.TABBAR_STICKER_ICON);
        Drawable iconShowKeyboardPressed = HSKeyboardThemeManager.getStyledAssetDrawable(null, HSKeyboardThemeManager.TABBAR_STICKER_ICON_CHOSED);
        HSKeyboard.getInstance().setShowStickerBtnImage(iconShowKeyboard, iconShowKeyboardPressed);
    }

    public static HSFeatureUtils getInstance() {
        return mInstance;
    }
}
