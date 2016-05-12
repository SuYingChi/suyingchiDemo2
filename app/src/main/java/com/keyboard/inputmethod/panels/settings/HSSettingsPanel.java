package com.keyboard.inputmethod.panels.settings;

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

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.HSInputMethod;
import com.ihs.inputmethod.api.HSInputMethodCommonUtils;
import com.ihs.inputmethod.api.HSInputMethodPanel;
import com.ihs.inputmethod.api.HSInputMethodSettings;
import com.ihs.inputmethod.api.HSInputMethodTheme;
import com.keyboard.rainbow.R;
import com.keyboard.rainbow.utils.Constants;

public class HSSettingsPanel extends HSInputMethodPanel {




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
    private ImageButton btnSounds;
    private ImageButton btnCorrection;
    private ImageButton btnCapitalization;
    private ImageButton btnPredictive;
    private ImageButton btnSwipe;
    private ImageButton btnLanguage;
    private ImageButton btnMore;

    private TextView btnSoundsText;
    private TextView btnCorrectionText;
    private TextView btnCapitalizationText;
    private TextView btnPredictiveText;
    private TextView btnSwipeText;
    private TextView btnLanguageText;
    private TextView btnMoreText;


    public static final String PANEL_NAME_THEME = "settings";

    public HSSettingsPanel() {
        super(PANEL_NAME_THEME);
        mContext = HSApplication.getContext();
    }


    @Override
    public View onCreatePanelView() {
        Context mThemeContext = new ContextThemeWrapper(mContext, HSInputMethodTheme.getCurrentThemeStyleId());
        LayoutInflater inflater = LayoutInflater.from(mThemeContext);
        mSettingsView = (HSSettingsPanelView) inflater.inflate(R.layout.keyboard_settings_layout, null);
        final int width = HSInputMethodCommonUtils.getDefaultKeyboardWidth();
        final int height = HSInputMethodCommonUtils.getDefaultKeyboardHeight();
        mSettingsView.setLayoutParams(new LinearLayout.LayoutParams(width, height));

        int textColor = mSettingsView.getSettingsItemTextColor();

        btnSounds = (ImageButton) mSettingsView.findViewById(R.id.bt_sound);
        btnSoundsText = (TextView) mSettingsView.findViewById(R.id.bt_sound_label);
        btnSoundsText.setTextColor(textColor);
        btnSounds.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (HSInputMethodSettings.getKeySoundEnabled()) {
                    HSInputMethodSettings.setKeySoundEnabled(false);
                    HSSettingsPanel.this.showToast("Sounds Disabled");

                    // Record event.
                    HSGoogleAnalyticsUtils.logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_SOUNDS_CLICKED, SETTING_OFF);
                } else {
                    HSInputMethodSettings.setKeySoundEnabled(true);
                    HSSettingsPanel.this.showToast("Sounds Enabled");

                    // Record event.
                    HSGoogleAnalyticsUtils.logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_SOUNDS_CLICKED, SETTING_ON);
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
                    HSSettingsPanel.this.showToast("Auto-Correction Disabled");

                    // Record event.
                    HSGoogleAnalyticsUtils.logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_AUTO_CORRECTION_CLICKED, SETTING_OFF);
                } else {
                    HSInputMethodSettings.setAutoCorrectionEnabled(true);
                    HSSettingsPanel.this.showToast("Auto-Correction Enabled");

                    // Record event.
                    HSGoogleAnalyticsUtils.logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_AUTO_CORRECTION_CLICKED, SETTING_ON);
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
                    HSSettingsPanel.this.showToast("Auto-Capitalization Disabled");

                    // Record event.
                    HSGoogleAnalyticsUtils.logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_AUTO_CAPITALIZATION_CLICKED, SETTING_OFF);
                } else {
                    HSInputMethodSettings.setAutoCapitalizationEnabled(true);
                    HSSettingsPanel.this.showToast("Auto-Capitalization Enabled");

                    // Record event.
                    HSGoogleAnalyticsUtils.logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_AUTO_CAPITALIZATION_CLICKED, SETTING_ON);
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
                    HSSettingsPanel.this.showToast("Word Prediction Disabled");

                    // Record event.
                    HSGoogleAnalyticsUtils.logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_PREDICTION_CLICKED, SETTING_OFF);
                } else {
                    HSInputMethodSettings.setWordPredictionEnabled(true);
                    HSSettingsPanel.this.showToast("Word Prediction Enabled");

                    // Record event.
                    HSGoogleAnalyticsUtils.logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_PREDICTION_CLICKED, SETTING_ON);
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
                    HSSettingsPanel.this.showToast("Swipe Input Disabled");

                    // Record event.
                    HSGoogleAnalyticsUtils.logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_SWIPE_CLICKED, SETTING_OFF);
                } else {
                    HSInputMethodSettings.setGestureTypingEnabled(true);
                    HSSettingsPanel.this.showToast("Swipe Input Enabled");

                    // Record event.
                    HSGoogleAnalyticsUtils.logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_SWIPE_CLICKED, SETTING_ON);
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
                HSInputMethod.showLanguageSettingsActivity();
                HSGoogleAnalyticsUtils.logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_ADD_LANGUAGE_CLICKED);
            }
        });

        StateListDrawable addLanguageStatesDrawable = new StateListDrawable();
        addLanguageStatesDrawable.addState(new int[]{android.R.attr.state_pressed},
                HSInputMethodTheme.getStyledAssetDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_add_language_off), SETTINGS_KEY_ADD_LANGUAGE_OFF));
        addLanguageStatesDrawable.addState(new int[]{},
                HSInputMethodTheme.getStyledAssetDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_add_language_on), SETTINGS_KEY_ADD_LANGUAGE_ON));
        btnLanguage.setBackgroundDrawable(addLanguageStatesDrawable);


        btnMore = (ImageButton) mSettingsView.findViewById(R.id.bt_more);
        btnMoreText = (TextView) mSettingsView.findViewById(R.id.bt_more_label);
        btnMoreText.setTextColor(textColor);
        btnMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                HSInputMethod.showMoreSettingsActivity();

                // Record event.
                HSGoogleAnalyticsUtils.logKeyboardEvent(Constants.GA_PARAM_ACTION_SETTING_MORE_CLICKED);
            }
        });

        StateListDrawable btnStatesDrawable = new StateListDrawable();
        btnStatesDrawable.addState(new int[] { android.R.attr.state_pressed },
                HSInputMethodTheme.getStyledAssetDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_more_off), SETTINGS_KEY_MORE_OFF));
        btnStatesDrawable.addState(new int[]{},
                HSInputMethodTheme.getStyledAssetDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_more_on), SETTINGS_KEY_MORE_ON));
                HSInputMethodTheme.getStyledAssetDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_more_on), SETTINGS_KEY_MORE_ON);
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
            btnSounds.setBackgroundDrawable(HSInputMethodTheme.getStyledAssetDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_sound_on),
                    SETTINGS_KEY_SOUND_ON));
        } else {
            btnSounds.setBackgroundDrawable(HSInputMethodTheme.getStyledAssetDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_sound_off),
                    SETTINGS_KEY_SOUND_OFF));
        }
    }

    private void updateCorrectionSettings() {
        if (HSInputMethodSettings.getAutoCorrectionEnabled()) {
            btnCorrection.setBackgroundDrawable(HSInputMethodTheme.getStyledAssetDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_correction_on),
                    SETTINGS_KEY_CORRECTION_ON));
        } else {
            btnCorrection.setBackgroundDrawable(HSInputMethodTheme.getStyledAssetDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_correction_off),
                    SETTINGS_KEY_CORRECTION_OFF));
        }
    }

    private void updateCapitalizationSettings() {
        if (HSInputMethodSettings.getAutoCapitalizationEnabled()) {
            btnCapitalization.setBackgroundDrawable(HSInputMethodTheme.getStyledAssetDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_capitalization_on),
                    SETTINGS_KEY_CAP_ON));
        } else {
            btnCapitalization.setBackgroundDrawable(HSInputMethodTheme.getStyledAssetDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_capitalization_off),
                    SETTINGS_KEY_CAP_OFF));
        }
    }

    private void updatePredictiveSettings() {
        if (HSInputMethodSettings.getWordPredictionEnabled()) {
            btnPredictive.setBackgroundDrawable(HSInputMethodTheme.getStyledAssetDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_predictive_on),
                    SETTINGS_KEY_PREDICT_ON));
        } else {
            btnPredictive.setBackgroundDrawable(HSInputMethodTheme.getStyledAssetDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_predictive_off),
                    SETTINGS_KEY_PREDICT_OFF));
        }
    }

    private void updateSwipeSettings() {
        if (HSInputMethodSettings.getGestureTypingEnabled()) {
            btnSwipe.setBackgroundDrawable(HSInputMethodTheme.getStyledAssetDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_swipe_on),
                    SETTINGS_KEY_SWIPE_ON));
        } else {
            btnSwipe.setBackgroundDrawable(HSInputMethodTheme.getStyledAssetDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_swipe_off),
                    SETTINGS_KEY_SWIPE_OFF));
        }
    }


    @Override
    public void onShowPanelView() {
        super.onShowPanelView();
        updateSoundSettings();
        updateCorrectionSettings();
        updateCapitalizationSettings();
        updatePredictiveSettings();
        updateSwipeSettings();
    }
}
