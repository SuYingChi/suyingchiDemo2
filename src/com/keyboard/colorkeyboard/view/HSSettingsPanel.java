package com.keyboard.colorkeyboard.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ihs.app.push.HSPushMgr;
import com.ihs.inputmethod.extended.api.HSKeyboard;
import com.ihs.inputmethod.extended.api.HSKeyboardPanel;
import com.ihs.inputmethod.extended.api.HSKeyboardPanel.IHSKeyboardPanelListener;
import com.ihs.inputmethod.extended.api.HSKeyboardSettings;
import com.ihs.inputmethod.extended.eventrecorder.HSGoogleAnalyticsEvent;
import com.ihs.inputmethod.extended.eventrecorder.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.extended.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.latin.utils.ResourceUtils;
import com.keyboard.colorkeyboard.R;

public class HSSettingsPanel {

    public HSSettingsPanel(Context context) {
        mContext = context;
    }

    private Context mContext;
    private HSSettingsPanelView mSettingsView;
    private Toast toast;

    private ImageButton btnSounds;
    private ImageButton btnCorrection;
    private ImageButton btnCapitalization;
    private ImageButton btnPredictive;
    private ImageButton btnSwipe;
    private ImageButton btnMore;

    private TextView btnSoundsText;
    private TextView btnCorrectionText;
    private TextView btnCapitalizationText;
    private TextView btnPredictiveText;
    private TextView btnSwipeText;
    private TextView btnMoreText;

    private IHSKeyboardPanelListener listener = new IHSKeyboardPanelListener() {

        @Override
        public void onShow() {
            refreshUI();
        }

        @Override
        public void onDismiss() {

        }
        
        @Override
        public void onCreatePanelView() {
            initSettingsView();
            HSKeyboard.getInstance().setPanelView(HSKeyboardPanel.KEYBOARD_PANEL_SETTINGS, mSettingsView);
        }
    };
    
    public void init() {
        Log.d("TAG", "notificaiton receive: " + HSPushMgr.HS_NOTIFICATION_DEVICETOKEN_RECEIVED_PARAM_TOKEN_STRING);

        Drawable icon = HSKeyboardThemeManager.getStyledAssetDrawable(null, HSKeyboardThemeManager.TABBAR_SETTINGS_ICON);
        Drawable iconSelected = HSKeyboardThemeManager.getStyledAssetDrawable(null, HSKeyboardThemeManager.TABBAR_SETTINGS_ICON_CHOSED);

        mSettingsView = null;
        HSKeyboard.getInstance().addPanelView(new HSKeyboardPanel(icon, iconSelected, mSettingsView, HSKeyboardPanel.KEYBOARD_PANEL_SETTINGS, listener));
    }

    private void initSettingsView() {
        Context mThemeContext = new ContextThemeWrapper(mContext, HSKeyboardThemeManager.getCurrentTheme().mStyleId);
        LayoutInflater inflater = LayoutInflater.from(mThemeContext);
        mSettingsView = (HSSettingsPanelView) inflater.inflate(R.layout.keyboard_settings_layout, null);
        final int width = ResourceUtils.getDefaultKeyboardWidth(mContext.getResources());
        final int height = ResourceUtils.getDefaultKeyboardHeight(mContext.getResources());
        mSettingsView.setLayoutParams(new LinearLayout.LayoutParams(width, height));

        int textColor = mSettingsView.getSettingsItemTextColor();

        btnSounds = (ImageButton) mSettingsView.findViewById(R.id.bt_sound);
        btnSoundsText = (TextView) mSettingsView.findViewById(R.id.bt_sound_label);
        btnSoundsText.setTextColor(textColor);
        btnSounds.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (HSKeyboardSettings.getKeySoundEnabled()) {
                    HSKeyboardSettings.setKeySoundEnabled(false);
                    HSSettingsPanel.this.showToast("Sounds Disabled");
                    
                    // Record event.
                    HSGoogleAnalyticsUtils.sendEvent(HSGoogleAnalyticsEvent.GA_EVENT_SETTING_SOUNDS_CLICKED, HSGoogleAnalyticsEvent.GA_PARAM_LABEL_SETTING_OFF);
                } else {
                    HSKeyboardSettings.setKeySoundEnabled(true);
                    HSSettingsPanel.this.showToast("Sounds Enabled");
                    
                    // Record event.
                    HSGoogleAnalyticsUtils.sendEvent(HSGoogleAnalyticsEvent.GA_EVENT_SETTING_SOUNDS_CLICKED, HSGoogleAnalyticsEvent.GA_PARAM_LABEL_SETTING_ON);
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
                if (HSKeyboardSettings.getAutoCorrectionEnabled()) {
                    HSKeyboardSettings.setAutoCorrectionEnabled(false);
                    HSSettingsPanel.this.showToast("Auto-Correction Disabled");
                    
                    // Record event.
                    HSGoogleAnalyticsUtils.sendEvent(HSGoogleAnalyticsEvent.GA_EVENT_SETTING_AUTO_CORRECTION_CLICKED, HSGoogleAnalyticsEvent.GA_PARAM_LABEL_SETTING_OFF);
                } else {
                    HSKeyboardSettings.setAutoCorrectionEnabled(true);
                    HSSettingsPanel.this.showToast("Auto-Correction Enabled");
                    
                    // Record event.
                    HSGoogleAnalyticsUtils.sendEvent(HSGoogleAnalyticsEvent.GA_EVENT_SETTING_AUTO_CORRECTION_CLICKED, HSGoogleAnalyticsEvent.GA_PARAM_LABEL_SETTING_ON);
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
                if (HSKeyboardSettings.getAutoCapitalizationEnabled()) {
                    HSKeyboardSettings.setAutoCapitalizationEnabled(false);
                    HSSettingsPanel.this.showToast("Auto-Capitalization Disabled");
                    
                    // Record event.
                    HSGoogleAnalyticsUtils.sendEvent(HSGoogleAnalyticsEvent.GA_EVENT_SETTING_AUTO_CAPITALIZATION_CLICKED, HSGoogleAnalyticsEvent.GA_PARAM_LABEL_SETTING_OFF);
                } else {
                    HSKeyboardSettings.setAutoCapitalizationEnabled(true);
                    HSSettingsPanel.this.showToast("Auto-Capitalization Enabled");
                    
                    // Record event.
                    HSGoogleAnalyticsUtils.sendEvent(HSGoogleAnalyticsEvent.GA_EVENT_SETTING_AUTO_CAPITALIZATION_CLICKED, HSGoogleAnalyticsEvent.GA_PARAM_LABEL_SETTING_ON);
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
                if (HSKeyboardSettings.getWordPredictionEnabled()) {
                    HSKeyboardSettings.setWordPredictionEnabled(false);
                    HSSettingsPanel.this.showToast("Word Prediction Disabled");
                    
                    // Record event.
                    HSGoogleAnalyticsUtils.sendEvent(HSGoogleAnalyticsEvent.GA_EVENT_SETTING_PREDICTION_CLICKED, HSGoogleAnalyticsEvent.GA_PARAM_LABEL_SETTING_OFF);
                } else {
                    HSKeyboardSettings.setWordPredictionEnabled(true);
                    HSSettingsPanel.this.showToast("Word Prediction Enabled");
                    
                    // Record event.
                    HSGoogleAnalyticsUtils.sendEvent(HSGoogleAnalyticsEvent.GA_EVENT_SETTING_PREDICTION_CLICKED, HSGoogleAnalyticsEvent.GA_PARAM_LABEL_SETTING_ON);
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
                if (HSKeyboardSettings.getGestureTypingEnabled()) {
                    HSKeyboardSettings.setGestureTypingEnabled(false);
                    HSSettingsPanel.this.showToast("Swipe Input Disabled");
                    
                    // Record event.
                    HSGoogleAnalyticsUtils.sendEvent(HSGoogleAnalyticsEvent.GA_EVENT_SETTING_SWIPE_CLICKED, HSGoogleAnalyticsEvent.GA_PARAM_LABEL_SETTING_OFF);
                } else {
                    HSKeyboardSettings.setGestureTypingEnabled(true);
                    HSSettingsPanel.this.showToast("Swipe Input Enabled");
                    
                    // Record event.
                    HSGoogleAnalyticsUtils.sendEvent(HSGoogleAnalyticsEvent.GA_EVENT_SETTING_SWIPE_CLICKED, HSGoogleAnalyticsEvent.GA_PARAM_LABEL_SETTING_ON);
                }
                HSSettingsPanel.this.updateSwipeSettings();
            }
        });
        updateSwipeSettings();

        btnMore = (ImageButton) mSettingsView.findViewById(R.id.bt_more);
        btnMoreText = (TextView) mSettingsView.findViewById(R.id.bt_more_label);
        btnMoreText.setTextColor(textColor);
        btnMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                HSKeyboardSettings.showMoreSettingsActivity();
                
                // Record event.
                HSGoogleAnalyticsUtils.sendEvent(HSGoogleAnalyticsEvent.GA_EVENT_SETTING_MORE_CLICKED, HSGoogleAnalyticsEvent.GA_PARAM_LABEL_NONE);
            }
        });

        StateListDrawable btnStatesDrawable = new StateListDrawable();
        btnStatesDrawable.addState(new int[] { android.R.attr.state_pressed },
                HSKeyboardThemeManager.getStyledAssetDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_more_off), HSKeyboardThemeManager.SETTINGS_KEY_MORE_OFF));
        btnStatesDrawable.addState(new int[] {},
                HSKeyboardThemeManager.getStyledAssetDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_more_on), HSKeyboardThemeManager.SETTINGS_KEY_MORE_ON));
        btnMore.setBackgroundDrawable(btnStatesDrawable);
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

        if (HSKeyboardSettings.getKeySoundEnabled()) {
            btnSounds.setBackgroundDrawable(HSKeyboardThemeManager.getStyledAssetDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_sound_on),
                    HSKeyboardThemeManager.SETTINGS_KEY_SOUND_ON));
        } else {
            btnSounds.setBackgroundDrawable(HSKeyboardThemeManager.getStyledAssetDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_sound_off),
                    HSKeyboardThemeManager.SETTINGS_KEY_SOUND_OFF));
        }
    }

    private void updateCorrectionSettings() {
        if (HSKeyboardSettings.getAutoCorrectionEnabled()) {
            btnCorrection.setBackgroundDrawable(HSKeyboardThemeManager.getStyledAssetDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_correction_on),
                    HSKeyboardThemeManager.SETTINGS_KEY_CORRECTION_ON));
        } else {
            btnCorrection.setBackgroundDrawable(HSKeyboardThemeManager.getStyledAssetDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_correction_off),
                    HSKeyboardThemeManager.SETTINGS_KEY_CORRECTION_OFF));
        }
    }

    private void updateCapitalizationSettings() {
        if (HSKeyboardSettings.getAutoCapitalizationEnabled()) {
            btnCapitalization.setBackgroundDrawable(HSKeyboardThemeManager.getStyledAssetDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_capitalization_on),
                    HSKeyboardThemeManager.SETTINGS_KEY_CAP_ON));
        } else {
            btnCapitalization.setBackgroundDrawable(HSKeyboardThemeManager.getStyledAssetDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_capitalization_off),
                    HSKeyboardThemeManager.SETTINGS_KEY_CAP_OFF));
        }
    }

    private void updatePredictiveSettings() {
        if (HSKeyboardSettings.getWordPredictionEnabled()) {
            btnPredictive.setBackgroundDrawable(HSKeyboardThemeManager.getStyledAssetDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_predictive_on),
                    HSKeyboardThemeManager.SETTINGS_KEY_PREDICT_ON));
        } else {
            btnPredictive.setBackgroundDrawable(HSKeyboardThemeManager.getStyledAssetDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_predictive_off),
                    HSKeyboardThemeManager.SETTINGS_KEY_PREDICT_OFF));
        }
    }

    private void updateSwipeSettings() {
        if (HSKeyboardSettings.getGestureTypingEnabled()) {
            btnSwipe.setBackgroundDrawable(HSKeyboardThemeManager.getStyledAssetDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_swipe_on),
                    HSKeyboardThemeManager.SETTINGS_KEY_SWIPE_ON));
        } else {
            btnSwipe.setBackgroundDrawable(HSKeyboardThemeManager.getStyledAssetDrawable(mContext.getResources().getDrawable(R.drawable.settings_key_swipe_off),
                    HSKeyboardThemeManager.SETTINGS_KEY_SWIPE_OFF));
        }
    }

    private void refreshUI() {
        updateSoundSettings();
        updateCorrectionSettings();
        updateCapitalizationSettings();
        updatePredictiveSettings();
        updateSwipeSettings();
    }

}
