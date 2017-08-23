package com.ihs.inputmethod.uimodules.ui.settings.common;

import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.HSUIInputMethod;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.framework.HSInputMethodSettings;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacterManager;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSDrawableUtils;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.utils.Constants;
import com.ihs.panelcontainer.BasePanel;

public class HSSettingsPanel extends BasePanel {


    private final static String SETTINGS_KEY_CAP_OFF = "settings_key_capitalization_off.png";
    private final static String SETTINGS_KEY_CAP_ON = "settings_key_capitalization_on.png";
    private final static String SETTINGS_KEY_CORRECTION_OFF = "settings_key_correction_off.png";
    private final static String SETTINGS_KEY_CORRECTION_ON = "settings_key_correction_on.png";
    private final static String SETTINGS_KEY_MORE_OFF = "settings_key_more_off.png";
    private final static String SETTINGS_KEY_MORE_ON = "settings_key_more_on.png";
    private final static String SETTINGS_KEY_SOUND_OFF = "settings_key_sound_off.png";
    private final static String SETTINGS_KEY_SOUND_ON = "settings_key_sound_on.png";
    private final static String SETTINGS_KEY_PREDICT_OFF = "settings_key_predictive_off.png";
    private final static String SETTINGS_KEY_PREDICT_ON = "settings_key_predictive_on.png";
    private final static String SETTINGS_KEY_SWIPE_OFF = "settings_key_swipe_off.png";
    private final static String SETTINGS_KEY_SWIPE_ON = "settings_key_swipe_on.png";
    private final static String SETTINGS_KEY_ADD_LANGUAGE_OFF = "settings_key_add_language_on.png";
    private final static String SETTINGS_KEY_ADD_LANGUAGE_ON = "settings_key_add_language_off.png";


    private static final String SETTING_ON = "on";
    private static final String SETTING_OFF = "off";


    private HSSettingsPanelView mSettingsView;
    private Toast toast;

    private Context mContext;
    private ImageButton btnTellAFriend;
    private ImageButton btnSounds;
    private ImageButton btnCorrection;
    private ImageButton btnCapitalization;
    private ImageButton btnPredictive;
    private ImageButton btnSwipe;
    private ImageButton btnLanguage;
    private ImageButton btnMore;

    private TextView btnSoundsText;
    private TextView btnTellAFriendText;
    private TextView btnCorrectionText;
    private TextView btnCapitalizationText;
    private TextView btnPredictiveText;
    private TextView btnSwipeText;
    private TextView btnLanguageText;
    private TextView btnMoreText;


    public HSSettingsPanel() {
        mContext = HSApplication.getContext();
    }


    @Override
    public View onCreatePanelView() {
        Context mThemeContext = new ContextThemeWrapper(mContext, HSKeyboardThemeManager.getCurrentTheme().mStyleId);
        LayoutInflater inflater = LayoutInflater.from(mThemeContext);
        mSettingsView = (HSSettingsPanelView) inflater.inflate(R.layout.rainbow_keyboard_settings_layout, null);
        final int width = HSResourceUtils.getDefaultKeyboardWidth(mContext.getResources());
        final int height = HSResourceUtils.getDefaultKeyboardHeight(mContext.getResources());
        mSettingsView.setLayoutParams(new LinearLayout.LayoutParams(width, height));

        int textColor = mSettingsView.getSettingsItemTextColor();

        btnTellAFriend = (ImageButton) mSettingsView.findViewById(R.id.bt_tell_a_friend);
        HSDrawableUtils.init(btnTellAFriend).setBgNormalDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_tell_a_friend))
                .setBgPressedDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_tell_a_friend_pressed)).applyBackground(false);
        btnTellAFriendText = (TextView) mSettingsView.findViewById(R.id.bt_tell_a_friend_label);
        btnTellAFriendText.setTextColor(textColor);
        btnTellAFriend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                HSSpecialCharacterManager.setTempDisableSpecialCharacter();
                HSInputMethod.inputText(mContext.getResources().getString(R.string.settings_button_tell_a_friend_content));
                HSSettingsPanel.this.showToast(mContext.getResources().getString(R.string.settings_button_tell_a_friend_tip));
            }
        });

        btnSounds = (ImageButton) mSettingsView.findViewById(R.id.bt_sound);
        btnSoundsText = (TextView) mSettingsView.findViewById(R.id.bt_sound_label);
        btnSoundsText.setTextColor(textColor);
        btnSounds.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (HSInputMethodSettings.getKeySoundEnabled()) {
                    HSInputMethodSettings.setKeySoundEnabled(false);
                    HSSettingsPanel.this.showToast(HSApplication.getContext().getString(R.string.settings_button_sound) + " " + HSApplication.getContext().getString(R.string.disabled));

                    // Record event.
//                    HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_SOUNDS_CLICKED, SETTING_OFF);
                    HSAnalytics.logEvent("keyboard_setting_sounds_clicked", "item_sound", SETTING_OFF);
                } else {
                    HSInputMethodSettings.setKeySoundEnabled(true);
                    HSSettingsPanel.this.showToast(HSApplication.getContext().getString(R.string.settings_button_sound) + " " + HSApplication.getContext().getString(R.string.enable));

                    // Record event.
//                    HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_SOUNDS_CLICKED, SETTING_ON);
                    HSAnalytics.logEvent("keyboard_setting_sounds_clicked", "item_sound", SETTING_ON);
                }
                HSSettingsPanel.this.updateSoundSettings();
            }
        });
        updateSoundSettings();

        btnCorrection = (ImageButton) mSettingsView.findViewById(R.id.bt_correction);
        btnCorrectionText = (TextView) mSettingsView.findViewById(R.id.bt_correction_label);
        btnCorrectionText.setTextColor(textColor);
        btnCorrection.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (HSInputMethodSettings.getAutoCorrectionEnabled()) {
                    HSInputMethodSettings.setAutoCorrectionEnabled(false);
                    HSSettingsPanel.this.showToast(HSApplication.getContext().getString(R.string.settings_button_auto_correction) + " " + HSApplication.getContext().getString(R.string.disabled));

                    // Record event.
//                    HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_AUTO_CORRECTION_CLICKED, SETTING_OFF);
                    HSAnalytics.logEvent("keyboard_setting_auto_correction_clicked", "auto_correction", SETTING_OFF);
                } else {
                    HSInputMethodSettings.setAutoCorrectionEnabled(true);
                    HSSettingsPanel.this.showToast(HSApplication.getContext().getString(R.string.settings_button_auto_correction) + " " + HSApplication.getContext().getString(R.string.enable));

                    // Record event.
//                    HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_AUTO_CORRECTION_CLICKED, SETTING_ON);
                    HSAnalytics.logEvent("keyboard_setting_auto_correction_clicked", "auto_correction", SETTING_ON);
                }
                HSSettingsPanel.this.updateCorrectionSettings();
            }
        });
        updateCorrectionSettings();

        btnCapitalization = (ImageButton) mSettingsView.findViewById(R.id.bt_capitalization);
        btnCapitalizationText = (TextView) mSettingsView.findViewById(R.id.bt_capitalization_label);
        btnCapitalizationText.setTextColor(textColor);
        btnCapitalization.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (HSInputMethodSettings.getAutoCapitalizationEnabled()) {
                    HSInputMethodSettings.setAutoCapitalizationEnabled(false);
                    HSSettingsPanel.this.showToast(HSApplication.getContext().getString(R.string.settings_button_auto_capitalization) + " " + HSApplication.getContext().getString(R.string.disabled));

                    // Record event.
//                    HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_AUTO_CAPITALIZATION_CLICKED, SETTING_OFF);
                    HSAnalytics.logEvent("keyboard_setting_auto_capitalization_clicked", "auto_capitalization", SETTING_OFF);
                } else {
                    HSInputMethodSettings.setAutoCapitalizationEnabled(true);
                    HSSettingsPanel.this.showToast(HSApplication.getContext().getString(R.string.settings_button_auto_capitalization) + " " + HSApplication.getContext().getString(R.string.enable));

                    // Record event.
//                    HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_AUTO_CAPITALIZATION_CLICKED, SETTING_ON);
                    HSAnalytics.logEvent("keyboard_setting_auto_capitalization_clicked", "auto_capitalization", SETTING_ON);
                }
                HSSettingsPanel.this.updateCapitalizationSettings();
            }
        });
        updateCapitalizationSettings();

        btnPredictive = (ImageButton) mSettingsView.findViewById(R.id.bt_predictive);
        btnPredictiveText = (TextView) mSettingsView.findViewById(R.id.bt_predictive_label);
        btnPredictiveText.setTextColor(textColor);
        btnPredictive.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (HSInputMethodSettings.getWordPredictionEnabled()) {
                    HSInputMethodSettings.setWordPredictionEnabled(false);
                    HSSettingsPanel.this.showToast(HSApplication.getContext().getString(R.string.settings_button_word_prediction) + " " + HSApplication.getContext().getString(R.string.disabled));

                    // Record event.
//                    HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_PREDICTION_CLICKED, SETTING_OFF);
                    HSAnalytics.logEvent("keyboard_setting_prediction_clicked", "word_prediction", SETTING_OFF);
                } else {
                    HSInputMethodSettings.setWordPredictionEnabled(true);
                    HSSettingsPanel.this.showToast(HSApplication.getContext().getString(R.string.settings_button_word_prediction) + " " + HSApplication.getContext().getString(R.string.enable));

                    // Record event.
//                    HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_PREDICTION_CLICKED, SETTING_ON);
                    HSAnalytics.logEvent("keyboard_setting_prediction_clicked", "word_prediction", SETTING_ON);

                }
                HSSettingsPanel.this.updatePredictiveSettings();
            }
        });
        updatePredictiveSettings();

        btnSwipe = (ImageButton) mSettingsView.findViewById(R.id.bt_swipe);
        btnSwipeText = (TextView) mSettingsView.findViewById(R.id.bt_swipe_label);
        btnSwipeText.setTextColor(textColor);
        btnSwipe.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (HSInputMethodSettings.getGestureTypingEnabled()) {
                    HSInputMethodSettings.setGestureTypingEnabled(false);
                    HSSettingsPanel.this.showToast(HSApplication.getContext().getString(R.string.setting_swipe_input) + " " + HSApplication.getContext().getString(R.string.disabled));

                    // Record event.
//                    HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_SWIPE_CLICKED, SETTING_OFF);
                    HSAnalytics.logEvent("keyboard_setting_swipe_clicked", "swipeInput", SETTING_OFF);
                } else {
                    HSInputMethodSettings.setGestureTypingEnabled(true);
                    HSSettingsPanel.this.showToast(HSApplication.getContext().getString(R.string.setting_swipe_input) + " " + HSApplication.getContext().getString(R.string.enable));

                    // Record event.
//                    HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_SWIPE_CLICKED, SETTING_ON);
                    HSAnalytics.logEvent("keyboard_setting_swipe_clicked", "swipeInput", SETTING_ON);
                }
                HSSettingsPanel.this.updateSwipeSettings();
            }
        });
        updateSwipeSettings();

        btnLanguage = (ImageButton) mSettingsView.findViewById(R.id.bt_add_lauguage);
        btnLanguageText = (TextView) mSettingsView.findViewById(R.id.bt_add_lauguage_label);
        btnLanguageText.setTextColor(textColor);
        btnLanguage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                HSUIInputMethod.launchMoreLanguageActivity();
                // HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_ADD_LANGUAGE_CLICKED);
            }
        });

        StateListDrawable addLanguageStatesDrawable = new StateListDrawable();
        addLanguageStatesDrawable.addState(new int[]{android.R.attr.state_pressed},
                HSKeyboardThemeManager.getStyledDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_add_language_off), SETTINGS_KEY_ADD_LANGUAGE_OFF));
        addLanguageStatesDrawable.addState(new int[]{},
                HSKeyboardThemeManager.getStyledDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_add_language_on), SETTINGS_KEY_ADD_LANGUAGE_ON));
        btnLanguage.setBackgroundDrawable(addLanguageStatesDrawable);


        btnMore = (ImageButton) mSettingsView.findViewById(R.id.bt_more);
        btnMoreText = (TextView) mSettingsView.findViewById(R.id.bt_more_label);
        btnMoreText.setTextColor(textColor);
        btnMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                HSUIInputMethod.launchSettingsActivity();

                // Record event.
//                HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_MORE_CLICKED);
                HSAnalytics.logEvent("keyboard_setting_more_clicked");
            }
        });

        StateListDrawable btnStatesDrawable = new StateListDrawable();
        btnStatesDrawable.addState(new int[]{android.R.attr.state_pressed},
                HSKeyboardThemeManager.getStyledDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_more_off), SETTINGS_KEY_MORE_OFF));
        btnStatesDrawable.addState(new int[]{},
                HSKeyboardThemeManager.getStyledDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_more_on), SETTINGS_KEY_MORE_ON));
        HSKeyboardThemeManager.getStyledDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_more_on), SETTINGS_KEY_MORE_ON);
        btnMore.setBackgroundDrawable(btnStatesDrawable);

        return mSettingsView;
    }


    private void showToast(String text) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
        toast.setText(text);
        toast.show();
    }

    private void updateSoundSettings() {

        if (HSInputMethodSettings.getKeySoundEnabled()) {
            btnSounds.setBackgroundDrawable(HSKeyboardThemeManager.getStyledDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_sound_on),
                    SETTINGS_KEY_SOUND_ON));
        } else {
            btnSounds.setBackgroundDrawable(HSKeyboardThemeManager.getStyledDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_sound_off),
                    SETTINGS_KEY_SOUND_OFF));
        }
    }

    private void updateCorrectionSettings() {
        if (HSInputMethodSettings.getAutoCorrectionEnabled()) {
            btnCorrection.setBackgroundDrawable(HSKeyboardThemeManager.getStyledDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_correction_on),
                    SETTINGS_KEY_CORRECTION_ON));
        } else {
            btnCorrection.setBackgroundDrawable(HSKeyboardThemeManager.getStyledDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_correction_off),
                    SETTINGS_KEY_CORRECTION_OFF));
        }
    }

    private void updateCapitalizationSettings() {
        if (HSInputMethodSettings.getAutoCapitalizationEnabled()) {
            btnCapitalization.setBackgroundDrawable(HSKeyboardThemeManager.getStyledDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_capitalization_on),
                    SETTINGS_KEY_CAP_ON));
        } else {
            btnCapitalization.setBackgroundDrawable(HSKeyboardThemeManager.getStyledDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_capitalization_off),
                    SETTINGS_KEY_CAP_OFF));
        }
    }

    private void updatePredictiveSettings() {
        if (HSInputMethodSettings.getWordPredictionEnabled()) {
            btnPredictive.setBackgroundDrawable(HSKeyboardThemeManager.getStyledDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_predictive_on),
                    SETTINGS_KEY_PREDICT_ON));
        } else {
            btnPredictive.setBackgroundDrawable(HSKeyboardThemeManager.getStyledDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_predictive_off),
                    SETTINGS_KEY_PREDICT_OFF));
        }
    }

    private void updateSwipeSettings() {
        if (HSInputMethodSettings.getGestureTypingEnabled()) {
            btnSwipe.setBackgroundDrawable(HSKeyboardThemeManager.getStyledDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_swipe_on),
                    SETTINGS_KEY_SWIPE_ON));
        } else {
            btnSwipe.setBackgroundDrawable(HSKeyboardThemeManager.getStyledDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_swipe_off),
                    SETTINGS_KEY_SWIPE_OFF));
        }
    }

    @Override
    protected boolean onShowPanelView(int appearMode) {
        updateSoundSettings();
        updateCorrectionSettings();
        updateCapitalizationSettings();
        updatePredictiveSettings();
        updateSwipeSettings();
        return super.onShowPanelView(appearMode);
    }
}
