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

import com.acb.adadapter.AcbInterstitialAd;
import com.acb.interstitialads.AcbInterstitialAdLoader;
import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.api.utils.HSToastUtils;
import com.ihs.inputmethod.charging.ChargingConfigManager;
import com.ihs.inputmethod.uimodules.NativeAdViewButtonHelper;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.constants.KeyboardActivationProcessor;
import com.ihs.inputmethod.uimodules.ui.theme.iap.IAPManager;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity;
import com.ihs.inputmethod.uimodules.utils.ViewConvertor;
import com.ihs.keyboardutils.nativeads.NativeAdParams;
import com.ihs.keyboardutils.nativeads.NativeAdView;

import java.util.List;

import static com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity.NOTIFICATION_SHOW_TRIAL_KEYBOARD;
import static com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity.hasTrialKeyboardShownWhenThemeCreated;


public final class TrialKeyboardDialog extends Dialog implements OnClickListener, KeyboardActivationProcessor.OnKeyboardActivationChangedListener {

    public interface OnTrialKeyboardStateChanged {

        void onTrialKeyShow(int requestCode);

        void onTrailKeyPrevented();

    }

    public final static String BUNDLE_KEY_SHOW_TRIAL_KEYBOARD_ACTIVITY = "bundle_key_show_trial_keyboard_activity";
    public final static String BUNDLE_KEY_HAS_TRIAL_KEYBOARD_SHOWN_WHEN_THEME_CREATED = "bundle_key_has_trial_keyboard_shown_when_theme_created";

    public final static String BUNDLE_KEY_SHOW_TRIAL_KEYBOARD_THEMENAME = "bundle_key_show_trial_keyboard_themename";

    Build build;
    private OnTrialKeyboardStateChanged onTrialKeyboardStateChanged;
    private boolean isSoftKeyboardOpened;
    private ViewGroup rootView;
    private String from;
    private int activationRequestCode;

    private boolean onlyCloseKeyboard = true;


    private TrialKeyboardDialog(Context context) {
        super(context, R.style.TrialKeyboardDialogStyle);
        setCancelable(true);
    }

    private TrialKeyboardDialog(Context context, Build build, String from, OnTrialKeyboardStateChanged onTrialKeyboardStateChanged) {
        this(context, build, from, onTrialKeyboardStateChanged, false);
    }

    private TrialKeyboardDialog(Context context, Build build, String from, OnTrialKeyboardStateChanged onTrialKeyboardStateChanged, boolean hasTrialKeyboardShownWhenThemeCreated) {
        super(context, R.style.TrialKeyboardDialogStyle);
        this.build = build;
        setCancelable(true);
        this.from = from;
        this.onTrialKeyboardStateChanged = onTrialKeyboardStateChanged;
//        this.hasTrialKeyboardShownWhenThemeCreated = hasTrialKeyboardShownWhenThemeCreated;
    }

    private void checkKeyboardState(Activity activity) {
        if (!HSInputMethod.isCurrentIMESelected()) {
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

    private long currentResumeTime;

    private NativeAdView nativeAdView;

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
                HSGoogleAnalyticsUtils.getInstance().logAppEvent("app_keyboardtest_view_show_time", String.valueOf(time));

                if (from.contains("ThemeHomeActivity")) {
                    int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                    if (currentapiVersion > android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        boolean isRateAlertShownThisTime = false;
                        HSLog.d("should delay rate alert for sdk version between 4.0 and 4.2");
                        if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("CUSTOM_THEME_SAVE", false)) {
                            if (!HSAlertMgr.isAlertShown()) {
                                //当前session未显示过
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
                }
                isSoftKeyboardOpened = false;
                if (nativeAdView != null) {
//                    nativeAdView.release();
                }
            }
        });
    }

    private void showInterstitialAds() {
        if (!CustomThemeActivity.hasTrialKeyboardShownWhenThemeCreated && getContext().getResources().getBoolean(R.bool.is_show_full_screen_ad_after_show_trial_keyboard) && !IAPManager.getManager().hasPurchaseNoAds()) {
            HSGoogleAnalyticsUtils.getInstance().logAppEvent(getContext().getResources().getString(R.string.ga_fullscreen_theme_apply_load_ad));
            List<AcbInterstitialAd> interstitialAds = AcbInterstitialAdLoader.fetch(HSApplication.getContext(), getContext().getResources().getString(R.string.placement_full_screen_trial_keyboard), 1);
            if (interstitialAds.size() > 0) {
                final AcbInterstitialAd interstitialAd = interstitialAds.get(0);
                interstitialAd.setInterstitialAdListener(new AcbInterstitialAd.IAcbInterstitialAdListener() {
                    long adDisplayTime = -1;

                    @Override
                    public void onAdDisplayed() {
                        HSGoogleAnalyticsUtils.getInstance().logAppEvent(getContext().getResources().getString(R.string.ga_fullscreen_theme_apply_show_ad));
                        adDisplayTime = System.currentTimeMillis();
                    }

                    @Override
                    public void onAdClicked() {
                        HSGoogleAnalyticsUtils.getInstance().logAppEvent(getContext().getResources().getString(R.string.ga_fullscreen_theme_apply_click_ad));
                    }

                    @Override
                    public void onAdClosed() {
                        long duration = System.currentTimeMillis() - adDisplayTime;
                        HSGoogleAnalyticsUtils.getInstance().logAppEvent(getContext().getResources().getString(R.string.ga_fullscreen_theme_apply_display_ad), String.format("%fs", duration / 1000f));
                        interstitialAd.release();
                    }
                });
                interstitialAd.show();
                hasTrialKeyboardShownWhenThemeCreated = false;
            } else {
                showChargingEnableAlert();
            }
        }else{
            hasTrialKeyboardShownWhenThemeCreated = false;
        }
    }


    private void showChargingEnableAlert() {
        if (ChargingConfigManager.getManager().shouldShowEnableChargingAlert(false)) {
            HSGoogleAnalyticsUtils.getInstance().logAppEvent("app_InterstitialRequestFailedAlert_prompt_show");
            CustomDesignAlert dialog = new CustomDesignAlert(HSApplication.getContext());
            dialog.setTitle(getContext().getString(R.string.charging_alert_title));
            dialog.setMessage(getContext().getString(R.string.charging_alert_message));
            dialog.setImageResource(R.drawable.enable_charging_alert_top_image);
            dialog.setCancelable(true);
            dialog.setPositiveButton(getContext().getString(R.string.enable), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ChargingManagerUtil.enableCharging(false);
                    HSToastUtils.toastCenterShort(getContext().getString(R.string.charging_enable_toast));
                    HSGoogleAnalyticsUtils.getInstance().logAppEvent("app_InterstitialRequestFailedAlert_prompt_click");
                }
            });
            dialog.show();
        }
    }

    @NonNull
    private NativeAdView addNativeAdView() {
        String placementName = HSApplication.getContext().getString(R.string.ad_placement_themetryad);

        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.ad_container);

        int width = HSDisplayUtils.getScreenWidthForContent() - HSDisplayUtils.dip2px(16);
        View view = LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ad_style_2, null);
        if(nativeAdView == null){
            nativeAdView = new NativeAdView(HSApplication.getContext(), view, null);
            nativeAdView.configParams(new NativeAdParams(placementName, width, 1.9f));
            final CardView cardView = ViewConvertor.toCardView(nativeAdView);
            NativeAdViewButtonHelper.autoHighlight(nativeAdView);

            nativeAdView.setOnAdLoadedListener(new NativeAdView.OnAdLoadedListener() {
                @Override
                public void onAdLoaded(NativeAdView nativeAdView) {
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

    public void show(Activity activity, int keyboardActivationRequestCode) {
        switch (keyboardActivationRequestCode) {
            case 10: // My Themes apply
                HSAnalytics.logEvent("keyboard_try_viewed", "from", "appliedTheme");
                HSAnalytics.logGoogleAnalyticsEvent("app", "TrialKeyboardDialog", "keyboard_try_viewed", "appliedTheme", null, null, null);
                break;
            case 12: // 新安装皮肤
                HSAnalytics.logEvent("keyboard_try_viewed", "from", "externalTheme");
                HSAnalytics.logGoogleAnalyticsEvent("app", "TrialKeyboardDialog", "keyboard_try_viewed", "externalTheme", null, null, null);
                break;
            case 15: // 自定义皮肤
                HSAnalytics.logEvent("keyboard_try_viewed", "from", "customizedTheme");
                HSAnalytics.logGoogleAnalyticsEvent("app", "TrialKeyboardDialog", "keyboard_try_viewed", "customizedTheme", null, null, null);
                break;
            default:
                break;
        }
        activationRequestCode = keyboardActivationRequestCode;
        checkKeyboardState(activity);
        new AcbInterstitialAdLoader(getContext(),getContext().getResources().getString(R.string.placement_full_screen_trial_keyboard)).load(1,null);
    }

    @Override
    public void show() {
        throw new RuntimeException("use show(Activity activity,int code) instead");
    }

    @Override
    public void onClick(View v) {
        if (build.listener != null) {
            if (v.getId() == R.id.dialog_cancel_button) {
                build.listener.onNegativeButtonClick();
                dismiss();
            } else if (v.getId() == R.id.dialog_ok_button) {
                build.listener.onPositiveButtonClick();
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


    public static class Build {
        DialogClickListener listener;

        String from;

        public Build(String from) {
            this.from = from;
        }

        public TrialKeyboardDialog create(Activity context, OnTrialKeyboardStateChanged onTrialKeyboardStateChanged) {
            return new TrialKeyboardDialog(context, this, from, onTrialKeyboardStateChanged);
        }

        public TrialKeyboardDialog create(Activity context, OnTrialKeyboardStateChanged onTrialKeyboardStateChanged, boolean hasTrialKeyboardShownWhenThemeCreated) {
            return new TrialKeyboardDialog(context, this, from, onTrialKeyboardStateChanged, hasTrialKeyboardShownWhenThemeCreated);
        }
    }

    public interface DialogClickListener {
        void onNegativeButtonClick();

        void onPositiveButtonClick();
    }


    public static void sendShowTrialKeyboardDialogNotification(String activityName, int activationRequestCode) {
        HSBundle bundle = new HSBundle();
        bundle.putInt(KeyboardActivationProcessor.BUNDLE_ACTIVATION_CODE, activationRequestCode);
        bundle.putString(BUNDLE_KEY_SHOW_TRIAL_KEYBOARD_ACTIVITY, activityName);
        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_SHOW_TRIAL_KEYBOARD, bundle);
    }

    private void setLayoutListenerToRootView() {
        final View rootView = findViewById(R.id.root_view);


        if (!IAPManager.getManager().hasPurchaseNoAds()) {
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
}
