package com.ihs.inputmethod.uimodules.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.framework.HSInputMethodListManager;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.feature.apkupdate.ApkUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.constants.KeyboardActivationProcessor;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeActivity;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity;
import com.ihs.inputmethod.uimodules.ui.theme.utils.ThemeMenuUtils;
import com.ihs.inputmethod.uimodules.utils.ViewConvertor;
import com.ihs.inputmethod.utils.TrialKeyboardDialogAlertUtils;
import com.ihs.keyboardutils.ads.KCInterstitialAd;
import com.ihs.keyboardutils.iap.RemoveAdsManager;
import com.ihs.keyboardutils.nativeads.KCNativeAdView;

import static com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity.NOTIFICATION_SHOW_TRIAL_KEYBOARD;


public final class TrialKeyboardDialog extends Dialog implements OnClickListener, KeyboardActivationProcessor.OnKeyboardActivationChangedListener {

    public final static String BUNDLE_KEY_SHOW_TRIAL_KEYBOARD_ACTIVITY = "bundle_key_show_trial_keyboard_activity";
    Builder builder;
    private OnTrialKeyboardStateChanged onTrialKeyboardStateChanged;
    private boolean isSoftKeyboardOpened;
    private ViewGroup rootView;
    private String from;
    private int activationRequestCode;
    private boolean showAdOnDismiss;
    private boolean onlyCloseKeyboard = true;
    private long currentResumeTime;
    private KCNativeAdView nativeAdView;

    private TrialKeyboardDialog(Context context, Builder build, String from, OnTrialKeyboardStateChanged onTrialKeyboardStateChanged) {
        super(context, R.style.TrialKeyboardDialogStyle);
        this.builder = build;
        setCancelable(true);
        this.from = from;
        this.onTrialKeyboardStateChanged = onTrialKeyboardStateChanged;
    }

    public static void sendShowTrialKeyboardDialogNotification(String activityName, int activationRequestCode) {
        HSBundle bundle = new HSBundle();
        bundle.putInt(KeyboardActivationProcessor.BUNDLE_ACTIVATION_CODE, activationRequestCode);
        bundle.putString(BUNDLE_KEY_SHOW_TRIAL_KEYBOARD_ACTIVITY, activityName);
        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_SHOW_TRIAL_KEYBOARD, bundle);
    }

    private void checkKeyboardState(Activity activity) {
        if (!HSInputMethodListManager.isMyInputMethodSelected()) {
            KeyboardActivationProcessor keyboardActivationProcessor;
            try {
                keyboardActivationProcessor = new KeyboardActivationProcessor(Class.forName(from), this);
                keyboardActivationProcessor.activateKeyboard(activity, true, activationRequestCode);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            keyboardSelected(activationRequestCode);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trial_keyboard_dialog);
        final EditText editText = (EditText) findViewById(R.id.edit_text_input);
        rootView = (ViewGroup) findViewById(R.id.root_view);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        editText.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setCanceledOnTouchOutside(true);

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                editText.setText("");
                long time = (System.currentTimeMillis() - currentResumeTime) / 1000;
                HSLog.e("app_keyboardtest_view_show_time : " + time);

                int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                if (currentapiVersion > android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    boolean isRateAlertShownThisTime = false;
                    HSLog.d("should delay rate alert for sdk version between 4.0 and 4.2");
                    if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("CUSTOM_THEME_SAVE", false)) {
                        if (!HSAlertMgr.isAlertShown() && !ApkUtils.isRateButtonClicked()) {
                            //当前session未显示过且用户未点击rate button
                            HSLog.d("TrialKeyboardDialog", "RateAlert当前session未显示过");
                            HSAlertMgr.showRateAlert();
                            if (HSAlertMgr.isAlertShown()) {
                                //本次弹出了RateAlert
                                isRateAlertShownThisTime = true;
                                HSLog.d("TrialKeyboardDialog", "本次弹出了RateAlert");
                            }
                        }

                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("CUSTOM_THEME_SAVE", false).apply();
                    } else {
                        HSLog.e("CUSTOM_THEME_SAVE_NULL");
                    }
                    if (!isRateAlertShownThisTime) {
                        showInterstitialAds();
                    }
                }

                isSoftKeyboardOpened = false;
                if (nativeAdView != null) {
//                    nativeAdView.release();
                }
            }
        });
    }

    private void showInterstitialAds() {
        if (showAdOnDismiss && !RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            boolean adShown = KCInterstitialAd.show(getContext().getString(R.string.placement_full_screen_open_keyboard),
                    getContext().getString(R.string.interstitial_ad_title_after_try_keyboard),
                    getContext().getString(R.string.interstitial_ad_subtitle_after_try_keyboard));
            if (!adShown) {
                showChargingEnableAlert();
            }
        }
    }

    private void showChargingEnableAlert() {
        TrialKeyboardDialogAlertUtils.showSpecialFunctionEnableAlert();
    }

    @NonNull
    private KCNativeAdView addNativeAdView() {
        String placementName = HSApplication.getContext().getString(R.string.ad_placement_themetryad);

        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.ad_container);

        int width = HSDisplayUtils.getScreenWidthForContent() - HSDisplayUtils.dip2px(16);
        View view = LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ad_style_2, null);
        if (nativeAdView == null) {
            nativeAdView = new KCNativeAdView(HSApplication.getContext());
            nativeAdView.setAdLayoutView(view);
            nativeAdView.setPrimaryViewSize(width, (int)(width / 1.9f));
            nativeAdView.load(placementName);
            final CardView cardView = ViewConvertor.toCardView(nativeAdView);

            nativeAdView.setOnAdLoadedListener(new KCNativeAdView.OnAdLoadedListener() {
                @Override
                public void onAdLoaded(KCNativeAdView nativeAdView) {
                    HSLog.e("themetry got ad");
                    if (cardView.getParent() == null) {
                        HSLog.e("themetry ad view added");
                        linearLayout.addView(cardView);
                    }
                }
            });
            linearLayout.addView(cardView);
        }
        return nativeAdView;
    }

    public void show(Activity activity, int keyboardActivationRequestCode, boolean showAdOnDismiss) {
        switch (keyboardActivationRequestCode) {
            case ThemeMenuUtils.keyboardActivationFromAdapter: // My Themes apply
                HSAnalytics.logEvent("keyboard_try_viewed", "from", "appliedTheme");
                HSAnalytics.logGoogleAnalyticsEvent("app", "TrialKeyboardDialog", "keyboard_try_viewed", "appliedTheme", null, null, null);
                break;
            case ThemeHomeActivity.keyboardActivationFromHomeWithTrial: // 新安装皮肤
                HSAnalytics.logEvent("keyboard_try_viewed", "from", "externalTheme");
                HSAnalytics.logGoogleAnalyticsEvent("app", "TrialKeyboardDialog", "keyboard_try_viewed", "externalTheme", null, null, null);
                break;
            case CustomThemeActivity.keyboardActivationFromCustom: // 自定义皮肤
                HSAnalytics.logEvent("keyboard_try_viewed", "from", "customizedTheme");
                HSAnalytics.logGoogleAnalyticsEvent("app", "TrialKeyboardDialog", "keyboard_try_viewed", "customizedTheme", null, null, null);
                break;
            default:
                break;
        }
        activationRequestCode = keyboardActivationRequestCode;
        this.showAdOnDismiss = showAdOnDismiss;
        checkKeyboardState(activity);
        if (showAdOnDismiss && !RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            KCInterstitialAd.load(getContext().getString(R.string.placement_full_screen_open_keyboard));
        }
    }

    @Override
    public void show() {
        throw new RuntimeException("use show(Activity activity,int code) instead");
    }

    @Override
    public void onClick(View v) {
        if (builder.listener != null) {
            if (v.getId() == R.id.dialog_cancel_button) {
                builder.listener.onNegativeButtonClick();
                dismiss();
            } else if (v.getId() == R.id.dialog_ok_button) {
                builder.listener.onPositiveButtonClick();
                dismiss();
            }
        }
    }

    @Override
    public void dismiss() {
        if (getContext().getResources().getBoolean(R.bool.trail_key_show_ad_before_close) && onlyCloseKeyboard) {
            onlyCloseKeyboard = false;
            HSInputMethod.hideWindow();
        } else {
            onlyCloseKeyboard = true;
            super.dismiss();
        }
    }

    private void setLayoutListenerToRootView() {
        final View rootView = findViewById(R.id.root_view);


        if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            nativeAdView = addNativeAdView();
        }

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();
                if (heightDiff > 100) { // We assume that keyboard height must more than 100
                    isSoftKeyboardOpened = true;
                } else if (isSoftKeyboardOpened) {
                    dismiss();
                    isSoftKeyboardOpened = false;
                }
            }
        });

    }

    @Override
    public void activeDialogShowing() {

    }

    @Override
    public void keyboardSelected(int requestCode) {
        onTrialKeyboardStateChanged.onTrialKeyShow(requestCode);

        try {
//            getWindow().setType(WindowManager.LayoutParams.TYPE_S);
            super.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        currentResumeTime = System.currentTimeMillis();
        setLayoutListenerToRootView();

    }

    @Override
    public void activeDialogCanceled() {
        onTrialKeyboardStateChanged.onTrailKeyPrevented();
    }

    @Override
    public void activeDialogDismissed() {

    }

    public interface OnTrialKeyboardStateChanged {

        void onTrialKeyShow(int requestCode);

        void onTrailKeyPrevented();

    }

    public interface DialogClickListener {
        void onNegativeButtonClick();

        void onPositiveButtonClick();
    }

    public static class Builder {
        DialogClickListener listener;

        String from;

        public Builder(String from) {
            this.from = from;
        }

        public TrialKeyboardDialog create(Activity context, OnTrialKeyboardStateChanged onTrialKeyboardStateChanged) {
            return new TrialKeyboardDialog(context, this, from, onTrialKeyboardStateChanged);
        }
    }
}
