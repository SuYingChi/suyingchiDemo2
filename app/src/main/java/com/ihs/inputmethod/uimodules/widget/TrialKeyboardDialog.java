package com.ihs.inputmethod.uimodules.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.ads.fullscreen.KeyboardFullScreenAd;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.constants.AdConstants;
import com.ihs.inputmethod.feature.apkupdate.ApkUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.utils.ViewConvertor;
import com.ihs.inputmethod.utils.TrialKeyboardDialogAlertUtils;
import com.ihs.keyboardutils.ads.KCInterstitialAd;
import com.ihs.keyboardutils.iap.RemoveAdsManager;
import com.ihs.keyboardutils.nativeads.KCNativeAdView;


public final class TrialKeyboardDialog extends Dialog {

    public final static String BUNDLE_ACTIVATION_CODE = "bundle_activation_code";
    public final static String BUNDLE_KEY_SHOW_TRIAL_KEYBOARD_ACTIVITY = "bundle_key_show_trial_keyboard_activity";
    private boolean isSoftKeyboardOpened;
    private ViewGroup rootView;
    private EditText inputTextView;
    private boolean showAdOnDismiss;
    private KCNativeAdView nativeAdView;

    private TrialKeyboardDialog(Context context) {
        super(context, R.style.TrialKeyboardDialogStyle);
        setCancelable(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trial_keyboard_dialog);
        inputTextView = (EditText) findViewById(R.id.edit_text_input);
        rootView = (ViewGroup) findViewById(R.id.root_view);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        inputTextView.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setCanceledOnTouchOutside(true);
    }

    private void showInterstitialAds() {
        if (showAdOnDismiss && !RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            boolean adShown = KCInterstitialAd.show(AdConstants.INTERSTITIAL_SPRING,
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
        String placementName = AdConstants.NATIVE_THEME_TRY;

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

    public void show(boolean showAdOnDismiss) {
        super.show();

        this.showAdOnDismiss = showAdOnDismiss;

        setLayoutListenerToRootView();
        if (showAdOnDismiss && KeyboardFullScreenAd.canShowSessionAd &&!RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            KCInterstitialAd.load(AdConstants.INTERSTITIAL_SPRING);
        }
    }

    @Override
    public void show() {
        throw new RuntimeException("use show(Activity activity,int code) instead");
    }

    @Override
    public void dismiss() {
        super.dismiss();

        inputTextView.setText(null);

        int currentVersion = android.os.Build.VERSION.SDK_INT;
        if (currentVersion > android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            boolean isRateAlertShownThisTime = false;
            HSLog.d("should delay rate alert for sdk version between 4.0 and 4.2");
            if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("CUSTOM_THEME_SAVE", false)) {
                if (!HSAlertMgr.isAlertShown() && !ApkUtils.isRateButtonClicked()) {
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

        isSoftKeyboardOpened = false;
        if (nativeAdView != null) {
            nativeAdView.release();
            nativeAdView = null;
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

    public static class Builder {
        Context context;

        public Builder(Context context) {
            this.context = context;
        }

        public TrialKeyboardDialog create() {
            return new TrialKeyboardDialog(this.context);
        }
    }
}
