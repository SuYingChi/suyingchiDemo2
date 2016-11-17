package com.keyboard.colorkeyboard.settings;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.widget.Toast;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.HSUIInputMethod;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.framework.HSInputMethodSettings;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.KeyboardPluginManager;
import com.ihs.inputmethod.uimodules.panel.HSKeyboardPanel;
import com.ihs.inputmethod.uimodules.ui.fonts.common.HSFontSelectPanel;
import com.ihs.inputmethod.uimodules.ui.theme.ui.panel.HSThemeSelectPanel;
import com.ihs.inputmethod.uimodules.ui.theme.utils.Constants;
import com.keyboard.colorkeyboard.KeyboardPanelManager;
import com.keyboard.colorkeyboard.MasterKeyboardPluginManager;
import com.keyboard.colorkeyboard.R;


/**
 * Created by chenyuanming on 22/09/2016.
 */

public class ViewItemBuilder {

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


    public static ViewItem getFontsItem() {
        return new ViewItem("Fonts",
                getStateListDrawable(SETTINGS_KEY_FONTS, SETTINGS_KEY_FONTS_PRESSED)
                , new ViewItem.ViewItemListener() {
            @Override
            public void onItemClick(ViewItem item) {
                MasterKeyboardPluginManager.getInstance().addActionButtonToStack(new CloseButton(HSApplication.getContext()) {
                    @Override
                    public HSKeyboardPanel getPanel() {
                        return KeyboardPluginManager.getInstance().getPanel(HSApplication.getContext().getString(R.string.panel_fonts));
                    }
                });
//                KeyboardPluginManager.getInstance().showPanel(HSApplication.getContext().getString(R.string.panel_fonts));
                KeyboardPanelManager.getInstance().getKeyboardPanelSwitchContainer().showPanel(HSFontSelectPanel.class);
                HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(GoogleAnalyticsConstants.GA_PARAM_ACTION_SETTING_FONTS_CLICKED);
            }
        });
    }

    public static ViewItem getThemesItem() {
        return new ViewItem("Themes",
                getStateListDrawable(SETTINGS_KEY_THEME, SETTINGS_KEY_THEME_PRESSED)
                , new ViewItem.ViewItemListener() {
            @Override
            public void onItemClick(ViewItem item) {
                MasterKeyboardPluginManager.getInstance().addActionButtonToStack(new CloseButton(HSApplication.getContext()) {
                    @Override
                    public HSKeyboardPanel getPanel() {
                        return KeyboardPluginManager.getInstance().getPanel(HSApplication.getContext().getString(R.string.panel_theme));
                    }
                });
//                KeyboardPluginManager.getInstance().showPanel(HSApplication.getContext().getString(R.string.panel_theme));
                KeyboardPanelManager.getInstance().getKeyboardPanelSwitchContainer().showPanel(HSThemeSelectPanel.class);
                HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(GoogleAnalyticsConstants.GA_PARAM_ACTION_SETTING_THEMES_CLICKED);
            }
        });
    }


    public static ViewItem getSoundsItem() {

        return new ViewItem("Sounds",
                getStateListDrawable(SETTINGS_KEY_SOUND_OFF, SETTINGS_KEY_SOUND_ON)
                , new ViewItem.ViewItemListener() {
            @Override
            public void onItemClick(ViewItem item) {
                HSInputMethodSettings.setKeySoundEnabled(!HSInputMethodSettings.getKeySoundEnabled());
                updateSoundsSettings(item);

            }

            @Override
            public void onItemViewCreated(ViewItem item) {
                super.onItemViewCreated(item);
                updateSoundsSettings(item);
            }
        });

    }

    private static void updateSoundsSettings(ViewItem item) {
        item.setSelected(HSInputMethodSettings.getKeySoundEnabled());
        if (item.isSelected) {
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_SOUNDS_CLICKED, SETTING_ON);
        } else {
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_SOUNDS_CLICKED, SETTING_OFF);
        }
    }


    public static ViewItem getAutoCorrectionItem() {
        return new ViewItem("Auto-Correction",
                getStateListDrawable(SETTINGS_KEY_CORRECTION_OFF, SETTINGS_KEY_CORRECTION_ON)
                , new ViewItem.ViewItemListener() {
            @Override
            public void onItemClick(ViewItem item) {
                HSInputMethodSettings.setAutoCorrectionEnabled(!HSInputMethodSettings.getAutoCorrectionEnabled());
                updateAutoCorrectionSettings(item);
            }

            @Override
            public void onItemViewCreated(ViewItem item) {
                super.onItemViewCreated(item);
                updateAutoCorrectionSettings(item);
            }
        });
    }

    private static void updateAutoCorrectionSettings(ViewItem item) {
        item.setSelected(HSInputMethodSettings.getAutoCorrectionEnabled());
        if (item.isSelected) {
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_AUTO_CORRECTION_CLICKED, SETTING_ON);
        } else {
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_AUTO_CORRECTION_CLICKED, SETTING_OFF);
        }
    }


    public static ViewItem getAutoCapitalizationItem() {
        return new ViewItem("Auto-Capitalization",
                getStateListDrawable(SETTINGS_KEY_CAP_OFF, SETTINGS_KEY_CAP_ON)
                , new ViewItem.ViewItemListener() {
            @Override
            public void onItemClick(ViewItem item) {
                HSInputMethodSettings.setAutoCapitalizationEnabled(!HSInputMethodSettings.getAutoCapitalizationEnabled());
                updateAutoCapitalizationSettings(item);
            }

            @Override
            public void onItemViewCreated(ViewItem item) {
                super.onItemViewCreated(item);
                updateAutoCapitalizationSettings(item);
            }
        });
    }

    private static void updateAutoCapitalizationSettings(ViewItem item) {
        item.setSelected(HSInputMethodSettings.getAutoCapitalizationEnabled());
        if (item.isSelected) {
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_AUTO_CAPITALIZATION_CLICKED, SETTING_ON);
        } else {
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_AUTO_CAPITALIZATION_CLICKED, SETTING_OFF);
        }
    }


    public static ViewItem getPredicationItem() {
        return new ViewItem("Word Prediction",
                getStateListDrawable(SETTINGS_KEY_PREDICT_OFF, SETTINGS_KEY_PREDICT_ON)
                , new ViewItem.ViewItemListener() {
            @Override
            public void onItemClick(ViewItem item) {
                HSInputMethodSettings.setWordPredictionEnabled(!HSInputMethodSettings.getWordPredictionEnabled());
                updatePredicationSettings(item);
            }

            @Override
            public void onItemViewCreated(ViewItem item) {
                super.onItemViewCreated(item);
                updatePredicationSettings(item);
            }
        });
    }

    private static void updatePredicationSettings(ViewItem item) {
        item.setSelected(HSInputMethodSettings.getWordPredictionEnabled());
        if (item.isSelected) {
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_PREDICTION_CLICKED, SETTING_ON);
        } else {
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_PREDICTION_CLICKED, SETTING_OFF);
        }
    }


    public static ViewItem getSwipeItem() {
        return new ViewItem("Swipe",
                getStateListDrawable(SETTINGS_KEY_SWIPE_OFF, SETTINGS_KEY_SWIPE_ON)
                , new ViewItem.ViewItemListener() {
            @Override
            public void onItemClick(ViewItem item) {
                HSInputMethodSettings.setGestureTypingEnabled(!HSInputMethodSettings.getGestureTypingEnabled());
                updateSwipeSettings(item);
            }

            @Override
            public void onItemViewCreated(ViewItem item) {
                super.onItemViewCreated(item);
                updateSwipeSettings(item);
            }
        });
    }

    private static void updateSwipeSettings(ViewItem item) {
        item.setSelected(HSInputMethodSettings.getGestureTypingEnabled());
        if (item.isSelected) {
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_SWIPE_CLICKED, SETTING_ON);
        } else {
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_SWIPE_CLICKED, SETTING_OFF);
        }
    }


    public static ViewItem getLanguageItem() {
        return new ViewItem("Add Language",
                getStateListDrawable(SETTINGS_KEY_ADD_LANGUAGE_OFF, SETTINGS_KEY_ADD_LANGUAGE_ON)
                , new ViewItem.ViewItemListener() {
            @Override
            public void onItemClick(ViewItem item) {
                HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(GoogleAnalyticsConstants.GA_PARAM_ACTION_SETTING_ADD_LANGUAGE_CLICKED);
                HSUIInputMethod.launchMoreLanguageActivity();
            }
        });
    }

    public static ViewItem getAdsItem() {
        if (adsItem == null) {
            adsItem = new ViewItem("Advertisement",
                    getStateListDrawable(SETTINGS_KEY_CAP_OFF, SETTINGS_KEY_CAP_ON)
                    , new ViewItem.ViewItemListener() {
                @Override
                public void onItemClick(ViewItem item) {
//                    showToast("show ads");
                }
            });
        }
        return adsItem;
    }


    public static ViewItem getMoreSettingsItem() {
        return new ViewItem("More",
                getStateListDrawable(SETTINGS_KEY_MORE_ON, SETTINGS_KEY_MORE_OFF)
                , new ViewItem.ViewItemListener() {
            @Override
            public void onItemClick(ViewItem item) {
                HSUIInputMethod.launchSettingsActivity();
                HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_MORE_CLICKED);
            }
        });
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


    private static void showToast(String msg) {
        Toast.makeText(HSApplication.getContext(), msg, Toast.LENGTH_SHORT).show();
    }


}
