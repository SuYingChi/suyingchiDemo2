package com.ihs.inputmethod.uimodules.settings;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.widget.Toast;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.framework.HSInputMethodSettings;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.utils.Constants;


/**
 * Created by chenyuanming on 22/09/2016.
 */

final class ViewItemBuilder {

    private final static String SETTINGS_KEY_THEME = "settings_key_theme.png";
    private final static String SETTINGS_KEY_THEME_PRESSED = "settings_key_theme_pressed.png";


    private final static String SETTINGS_KEY_FONTS = "settings_key_fonts.png";
    private final static String SETTINGS_KEY_FONTS_PRESSED = "settings_key_fonts_pressed.png";

    private final static String SETTINGS_KEY_SOUND_OFF = "settings_key_sound_off.png";
    private final static String SETTINGS_KEY_SOUND_ON = "settings_key_sound_on.png";


    private final static String SETTINGS_KEY_CORRECTION_OFF = "settings_key_correction_off.png";
    private final static String SETTINGS_KEY_CORRECTION_ON = "settings_key_correction_on.png";


    private final static String SETTINGS_KEY_CAP_OFF = "settings_key_capitalization_off.png";
    private final static String SETTINGS_KEY_CAP_ON = "settings_key_capitalization_on.png";


    private final static String SETTINGS_KEY_PREDICT_OFF = "settings_key_predictive_off.png";
    private final static String SETTINGS_KEY_PREDICT_ON = "settings_key_predictive_on.png";


    private final static String SETTINGS_KEY_SWIPE_OFF = "settings_key_swipe_off.png";
    private final static String SETTINGS_KEY_SWIPE_ON = "settings_key_swipe_on.png";

    private final static String SETTINGS_KEY_ADD_LANGUAGE_OFF = "settings_key_add_language_on.png";
    private final static String SETTINGS_KEY_ADD_LANGUAGE_ON = "settings_key_add_language_off.png";


    private final static String SETTINGS_KEY_MORE_OFF = "settings_key_more_off.png";
    private final static String SETTINGS_KEY_MORE_ON = "settings_key_more_on.png";


    private static final String SETTING_ON = "on";
    private static final String SETTING_OFF = "off";


    private static ViewItem adsItem;


    static ViewItem getFontsItem(ViewItem.ViewItemListener viewItemListener) {
        return new ViewItem(HSApplication.getContext().getResources().getString(R.string.setting_item_fonts),
                getStateListDrawable(SETTINGS_KEY_FONTS, SETTINGS_KEY_FONTS_PRESSED)
                , viewItemListener, false);
    }

    static ViewItem getThemesItem(ViewItem.ViewItemListener viewItemListener) {
        return new ViewItem(HSApplication.getContext().getResources().getString(R.string.setting_item_themes),
                getStateListDrawable(SETTINGS_KEY_THEME, SETTINGS_KEY_THEME_PRESSED)
                , viewItemListener, false);
    }


    static ViewItem getSoundsItem() {

        return new ViewItem(HSApplication.getContext().getString(R.string.setting_item_sounds),
                getStateListDrawable(SETTINGS_KEY_SOUND_OFF, SETTINGS_KEY_SOUND_ON)
                , new ViewItem.ViewItemListener() {
            @Override
            public void onItemClick(ViewItem item) {
                HSInputMethodSettings.setKeySoundEnabled(!HSInputMethodSettings.getKeySoundEnabled());
                updateSoundsSettings(item);

            }


            @Override
            void onItemViewInvalidate(ViewItem item) {
                item.updateSelectedStatus(HSInputMethodSettings.getKeySoundEnabled());
            }
        }, HSInputMethodSettings.getKeySoundEnabled());

    }

    private static void updateSoundsSettings(ViewItem item) {
        item.setSelected(HSInputMethodSettings.getKeySoundEnabled());
        if (item.isSelected) {
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_SOUNDS_CLICKED, SETTING_ON);
        } else {
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_SOUNDS_CLICKED, SETTING_OFF);
        }
    }


    static ViewItem getAutoCorrectionItem() {
        return new ViewItem(HSApplication.getContext().getString(R.string.setting_item_correction),
                getStateListDrawable(SETTINGS_KEY_CORRECTION_OFF, SETTINGS_KEY_CORRECTION_ON)
                , new ViewItem.ViewItemListener() {
            @Override
            public void onItemClick(ViewItem item) {
                HSInputMethodSettings.setAutoCorrectionEnabled(!HSInputMethodSettings.getAutoCorrectionEnabled());
                updateAutoCorrectionSettings(item);
            }


            @Override
            void onItemViewInvalidate(ViewItem item) {
                item.updateSelectedStatus(HSInputMethodSettings.getAutoCorrectionEnabled());
            }
        }, HSInputMethodSettings.getAutoCorrectionEnabled());
    }

    private static void updateAutoCorrectionSettings(ViewItem item) {
        item.setSelected(HSInputMethodSettings.getAutoCorrectionEnabled());
        if (item.isSelected) {
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_AUTO_CORRECTION_CLICKED, SETTING_ON);
        } else {
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_AUTO_CORRECTION_CLICKED, SETTING_OFF);
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
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_AUTO_CAPITALIZATION_CLICKED, SETTING_ON);
        } else {
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_AUTO_CAPITALIZATION_CLICKED, SETTING_OFF);
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
        if (item.isSelected) {
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_PREDICTION_CLICKED, SETTING_ON);
        } else {
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_PREDICTION_CLICKED, SETTING_OFF);
        }
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
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_SWIPE_CLICKED, SETTING_ON);
        } else {
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_SWIPE_CLICKED, SETTING_OFF);
        }
    }


    public static ViewItem getLanguageItem(ViewItem.ViewItemListener listener) {
        return new ViewItem(HSApplication.getContext().getString(R.string.setting_item_add_languages),
                getStateListDrawable(SETTINGS_KEY_ADD_LANGUAGE_OFF, SETTINGS_KEY_ADD_LANGUAGE_ON)
                , listener, false);
    }

    static ViewItem getAdsItem() {
        if (adsItem == null) {
            adsItem = new ViewItem("Advertisement",
                    getStateListDrawable(SETTINGS_KEY_CAP_OFF, SETTINGS_KEY_CAP_ON)
                    , new ViewItem.ViewItemListener() {
                @Override
                public void onItemClick(ViewItem item) {
                    Toast.makeText(HSApplication.getContext(), HSApplication.getContext().getString(R.string.setting_toast_ad), Toast.LENGTH_SHORT).show();
                }
            }, false);
        }
        return adsItem;
    }


    static ViewItem getMoreSettingsItem(ViewItem.ViewItemListener listener) {
        return new ViewItem(HSApplication.getContext().getString(R.string.setting_item_more_settings),
                getStateListDrawable(SETTINGS_KEY_MORE_ON, SETTINGS_KEY_MORE_OFF)
                , listener, false);
    }


    private static StateListDrawable getStateListDrawable(String normalImageName, String pressedImageName) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        Drawable defNormalDrawable = HSKeyboardThemeManager.getCurrentTheme().getStyledDrawableFromResources(normalImageName);
        Drawable defPressedDrawable = HSKeyboardThemeManager.getCurrentTheme().getStyledDrawableFromResources(pressedImageName);

        stateListDrawable.addState(new int[]{android.R.attr.state_focused}, HSKeyboardThemeManager.getStyledDrawable(defPressedDrawable, SETTINGS_KEY_FONTS_PRESSED));
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, HSKeyboardThemeManager.getStyledDrawable(defPressedDrawable, SETTINGS_KEY_FONTS_PRESSED));
        stateListDrawable.addState(new int[]{android.R.attr.state_selected}, HSKeyboardThemeManager.getStyledDrawable(defPressedDrawable, SETTINGS_KEY_FONTS_PRESSED));
        stateListDrawable.addState(new int[]{}, HSKeyboardThemeManager.getStyledDrawable(defNormalDrawable, SETTINGS_KEY_FONTS));

        return stateListDrawable;
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
