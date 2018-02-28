package com.ihs.inputmethod.home.model;

import com.ihs.inputmethod.uimodules.R;

/**
 * Created by jixiang on 18/1/20.
 */

public enum HomeMenu {
    KeyboardThemes(R.string.home_menu_keyboard_themes, R.drawable.home_menu_keyboard_theme_bg, R.drawable.home_menu_keyboard_theme_icon),
    AdultStickers(R.string.home_menu_adult_stickers, R.drawable.home_menu_adult_stikers_bg, R.drawable.home_menu_adult_stickers_icon),
    SexyWallpaper(R.string.home_menu_sexy_wallpaper, R.drawable.home_menu_sexy_wallpaper_bg, 0),
    CallFlash(R.string.home_menu_call_flash, R.drawable.home_menu_call_flash_bg, R.drawable.home_menu_call_flash_icon);

    private int textResId;
    private int menuBgResId;
    private int menuIconResId;

    HomeMenu(int textResId, int menuBgResId, int menuIconResId) {
        this.textResId = textResId;
        this.menuBgResId = menuBgResId;
        this.menuIconResId = menuIconResId;
    }

    public int getTextResId() {
        return textResId;
    }

    public int getMenuBgResId() {
        return menuBgResId;
    }

    public int getMenuIconResId() {
        return menuIconResId;
    }
}
