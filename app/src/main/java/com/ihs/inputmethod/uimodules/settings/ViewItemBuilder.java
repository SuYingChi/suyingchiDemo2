package com.ihs.inputmethod.uimodules.settings;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.framework.HSInputMethodSettings;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSDrawableUtils;
import com.ihs.inputmethod.uimodules.R;


/**
 * Created by chenyuanming on 22/09/2016.
 */

final class ViewItemBuilder {

    private final static String SETTINGS_KEY_THEME = "ic_menu_cloth";

    private final static String SETTINGS_KEY_MY_THEME = "ic_settings_key_create_theme";

    private final static String SETTINGS_KEY_FONTS = "ic_settings_key_fonts";

    private final static String SETTINGS_KEY_SOUND_OFF = "ic_settings_key_sound_off";
    private final static String SETTINGS_KEY_SOUND_ON = "ic_settings_key_sound_on";

    private final static String SETTINGS_KEY_AUTO_CORRECTION = "ic_settings_key_auto_correction";

    private final static String SETTINGS_KEY_CAP_OFF = "settings_key_capitalization_off.png";
    private final static String SETTINGS_KEY_CAP_ON = "settings_key_capitalization_on.png";


    private final static String SETTINGS_KEY_PREDICT_OFF = "settings_key_predictive_off.png";
    private final static String SETTINGS_KEY_PREDICT_ON = "settings_key_predictive_on.png";


    private final static String SETTINGS_KEY_SWIPE_OFF = "settings_key_swipe_off.png";
    private final static String SETTINGS_KEY_SWIPE_ON = "settings_key_swipe_on.png";

    private final static String SETTINGS_KEY_ADD_LANGUAGE = "nav_language_icon";

    private final static String SETTINGS_KEY_MORE_SETTING = "ic_settings_key_more_setting";


    private static final String SETTING_ON = "on";
    private static final String SETTING_OFF = "off";


    private static ViewItem adsItem;


    static ViewItem getFontsItem(ViewItem.ViewItemListener viewItemListener) {
        return new ViewItem(HSApplication.getContext().getResources().getString(R.string.setting_item_fonts),
                getStateListDrawable(SETTINGS_KEY_FONTS, SETTINGS_KEY_FONTS)
                , viewItemListener, false);
    }

    static ViewItem getThemesItem(ViewItem.ViewItemListener viewItemListener) {
        return new ViewItem(HSApplication.getContext().getResources().getString(R.string.setting_item_themes),
                getStateListDrawable(SETTINGS_KEY_THEME, SETTINGS_KEY_THEME)
                , viewItemListener, false);
    }

    static ViewItem getMyThemeItem(ViewItem.ViewItemListener viewItemListener) {
        return new ViewItem(HSApplication.getContext().getResources().getString(R.string.setting_item_my_theme),
                getStateListDrawable(SETTINGS_KEY_MY_THEME, SETTINGS_KEY_MY_THEME)
                , viewItemListener, false);
    }

    static ViewItem getSoundsPositionItem() {
        return new ViewItem(HSApplication.getContext().getString(R.string.setting_item_sounds),
                getStateListDrawable(SETTINGS_KEY_SOUND_ON, SETTINGS_KEY_SOUND_OFF)
                , new ViewItem.ViewItemListener() {
            @Override
            public void onItemClick(ViewItem item) {
                HSInputMethodSettings.setKeySoundEnabled(!HSInputMethodSettings.getKeySoundEnabled());
                updateSoundsSettings(item);

            }

            @Override
            void onItemViewInvalidate(ViewItem item) {
                item.updateSelectedStatus(!HSInputMethodSettings.getKeySoundEnabled());
            }
        }, !HSInputMethodSettings.getKeySoundEnabled());
    }

    private static void updateSoundsSettings(ViewItem item) {
        item.setSelected(!HSInputMethodSettings.getKeySoundEnabled());
        if (!item.isSelected) {
            HSAnalytics.logEvent("keyboard_setting_sounds_clicked", "item_sound", SETTING_ON);
        } else {
            HSAnalytics.logEvent("keyboard_setting_sounds_clicked", "item_sound", SETTING_OFF);
        }
    }


    static ViewItem getAutoCorrectionItem() {
        return new ViewItem(HSApplication.getContext().getString(R.string.setting_item_correction),
                getStateListDrawable(SETTINGS_KEY_AUTO_CORRECTION, SETTINGS_KEY_AUTO_CORRECTION)
                , new ViewItem.ViewItemListener() {
            @Override
            public void onItemClick(ViewItem item) {
                HSInputMethodSettings.setAutoCorrectionEnabled(!HSInputMethodSettings.getAutoCorrectionEnabled());
                updateAutoCorrectionSettings(item);
            }

            @Override
            void onItemViewInvalidate(ViewItem item) {
                item.updateSelectedStatus(!HSInputMethodSettings.getAutoCorrectionEnabled());
            }
        }, !HSInputMethodSettings.getAutoCorrectionEnabled());
    }

    private static void updateAutoCorrectionSettings(ViewItem item) {
        item.setSelected(!HSInputMethodSettings.getAutoCorrectionEnabled());
        if (!item.isSelected) {
            HSAnalytics.logEvent("keyboard_setting_auto_correction_clicked", "auto_correction", SETTING_ON);
        } else {
            HSAnalytics.logEvent("keyboard_setting_auto_correction_clicked", "auto_correction", SETTING_OFF);
        }
    }


    static ViewItem getAutoCapitalizationItem() {
        return new ViewItem(HSApplication.getContext().getString(R.string.setting_item_capitalization),
                getStateListDrawable(SETTINGS_KEY_CAP_OFF, SETTINGS_KEY_CAP_ON)
                , new ViewItem.ViewItemListener() {
            @Override
            public void onItemClick(ViewItem item) {
                HSInputMethodSettings.setAutoCapitalizationEnabled(!HSInputMethodSettings.getAutoCapitalizationEnabled());
                updateAutoCapitalizationSettings(item);
            }


            @Override
            void onItemViewInvalidate(ViewItem item) {
                item.updateSelectedStatus(HSInputMethodSettings.getAutoCapitalizationEnabled());
            }
        }, HSInputMethodSettings.getAutoCapitalizationEnabled());
    }

    private static void updateAutoCapitalizationSettings(ViewItem item) {
        item.setSelected(HSInputMethodSettings.getAutoCapitalizationEnabled());
        if (item.isSelected) {
            HSAnalytics.logEvent("keyboard_setting_auto_capitalization_clicked", "auto_capitalization", SETTING_ON);
        } else {
            HSAnalytics.logEvent("keyboard_setting_auto_capitalization_clicked", "auto_capitalization", SETTING_OFF);
        }
    }


    static ViewItem getPredicationItem() {
        return new ViewItem(HSApplication.getContext().getString(R.string.setting_item_word_prediction),
                getStateListDrawable(SETTINGS_KEY_PREDICT_OFF, SETTINGS_KEY_PREDICT_ON)
                , new ViewItem.ViewItemListener() {
            @Override
            public void onItemClick(ViewItem item) {
                HSInputMethodSettings.setWordPredictionEnabled(!HSInputMethodSettings.getWordPredictionEnabled());
                updatePredicationSettings(item);
            }


            @Override
            void onItemViewInvalidate(ViewItem item) {
                item.updateSelectedStatus(HSInputMethodSettings.getWordPredictionEnabled());
            }
        }, HSInputMethodSettings.getWordPredictionEnabled());
    }

    private static void updatePredicationSettings(ViewItem item) {
        item.setSelected(HSInputMethodSettings.getWordPredictionEnabled());
    }


    static ViewItem getSwipeItem() {
        return new ViewItem(HSApplication.getContext().getString(R.string.setting_item_swipe),
                getStateListDrawable(SETTINGS_KEY_SWIPE_OFF, SETTINGS_KEY_SWIPE_ON)
                , new ViewItem.ViewItemListener() {
            @Override
            public void onItemClick(ViewItem item) {
                HSInputMethodSettings.setGestureTypingEnabled(!HSInputMethodSettings.getGestureTypingEnabled());
                updateSwipeSettings(item);
            }


            @Override
            void onItemViewInvalidate(ViewItem item) {
                item.updateSelectedStatus(HSInputMethodSettings.getGestureTypingEnabled());
            }
        }, HSInputMethodSettings.getGestureTypingEnabled());
    }

    private static void updateSwipeSettings(ViewItem item) {
        item.setSelected(HSInputMethodSettings.getGestureTypingEnabled());
        if (item.isSelected) {
            HSAnalytics.logEvent("keyboard_setting_swipe_clicked", "swipeInput", SETTING_OFF);
        } else {
            HSAnalytics.logEvent("keyboard_setting_swipe_clicked", "swipeInput", SETTING_ON);


        }
    }


    public static ViewItem getLanguageItem(ViewItem.ViewItemListener listener) {
        return new ViewItem(HSApplication.getContext().getString(R.string.setting_item_add_languages),
                getStateListDrawable(SETTINGS_KEY_ADD_LANGUAGE, SETTINGS_KEY_ADD_LANGUAGE)
                , listener, false);
    }

    static ViewItem getAdsItem() {
        if (adsItem == null) {
            adsItem = new ViewItem("Advertisement",
                    getStateListDrawable(SETTINGS_KEY_CAP_OFF, SETTINGS_KEY_CAP_ON)
                    , new ViewItem.ViewItemListener() {
                @Override
                public void onItemClick(ViewItem item) {
                }
            }, false);
        }
        return adsItem;
    }


    static ViewItem getMoreSettingsItem(ViewItem.ViewItemListener listener) {
        return new ViewItem(HSApplication.getContext().getString(R.string.setting_item_more_settings),
                getStateListDrawable(SETTINGS_KEY_MORE_SETTING, SETTINGS_KEY_MORE_SETTING)
                , listener, false);
    }


    private static StateListDrawable getStateListDrawable(String normalImageName, String pressedImageName) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        Drawable defNormalDrawable = getStyledDrawableFromResources(normalImageName);
        if (HSKeyboardThemeManager.getCurrentTheme().isDarkBg()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                defNormalDrawable.setColorFilter(ContextCompat.getColor(HSApplication.getContext(), R.color.settings_button_dark_normal), PorterDuff.Mode.SRC_IN);
            } else {
                DrawableCompat.setTint(defNormalDrawable, ContextCompat.getColor(HSApplication.getContext(), R.color.settings_button_dark_normal));
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                defNormalDrawable.setColorFilter(Color.parseColor("#29A0CB"), PorterDuff.Mode.SRC_IN);
            } else {
                DrawableCompat.setTint(defNormalDrawable, Color.parseColor("#29A0CB"));
            }
        }

        Drawable defPressedDrawable = getStyledDrawableFromResources(pressedImageName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            defPressedDrawable.setColorFilter(ContextCompat.getColor(HSApplication.getContext(), R.color.settings_button_press), PorterDuff.Mode.SRC_IN);
        } else {
            DrawableCompat.setTint(defPressedDrawable, ContextCompat.getColor(HSApplication.getContext(), R.color.settings_button_press));
        }

        stateListDrawable.addState(new int[]{android.R.attr.state_focused}, defPressedDrawable);
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, defPressedDrawable);
        stateListDrawable.addState(new int[]{android.R.attr.state_selected}, defPressedDrawable);
        stateListDrawable.addState(new int[]{}, defNormalDrawable);

        return stateListDrawable;
    }

    private static Drawable getStyledDrawableFromResources(String resName) {
        int resId = HSApplication.getContext().getResources().getIdentifier(resName, "drawable", HSApplication.getContext().getPackageName());
        if (resId != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return HSApplication.getContext().getResources().getDrawable(resId);
            } else {
                return VectorDrawableCompat.create(HSApplication.getContext().getResources(), resId, null);
            }
        } else {
            HSLog.e("getStyledDrawableFromResources() called with: resName = [" + resName + "]");
            return HSDrawableUtils.getTransparentBitmapDrawable();
        }
    }

    private static int getDarkerColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv); // convert to hsv
        // make darker
        hsv[1] = hsv[1] + 0.1f; // more saturation
        hsv[2] = hsv[2] - 0.1f; // less brightness
        return Color.HSVToColor(hsv);
    }

    public static void release() {
        if (adsItem != null) {
            if (adsItem.viewContainer != null) {
                adsItem.viewContainer.removeAllViews();
            }
            adsItem = null;
        }
    }
}
