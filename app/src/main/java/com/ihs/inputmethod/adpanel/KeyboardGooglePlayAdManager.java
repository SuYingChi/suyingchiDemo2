package com.ihs.inputmethod.adpanel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.nativeads.KCNativeAdView;
import com.ihs.keyboardutils.utils.KCFeatureRestrictionConfig;


/**
 * Created by xiayan on 2017/6/25.
 */

public class KeyboardGooglePlayAdManager implements KCNativeAdView.OnAdLoadedListener, KCNativeAdView.OnAdClickedListener {
    private static final String PREF_AD_SHOW_TIME = "pref_ad_show_time";
    private String adPlacement;
    private KCNativeAdView nativeAdView;
    private AdGooglePlayDialog adGooglePlayDialog;

    public KeyboardGooglePlayAdManager(String adPlacement) {
        this.adPlacement = adPlacement;
    }

    private void initNativeAdView() {
        int width = getNativeAdViewWidth();
        View view = LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ad_google_play, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        nativeAdView = new KCNativeAdView(HSApplication.getContext());
        nativeAdView.setAdLayoutView(view);
        nativeAdView.setPrimaryViewSize(width, (int)(width / 1.9f));
        nativeAdView.load(adPlacement);
        nativeAdView.setOnAdLoadedListener(this);
        nativeAdView.setOnAdClickedListener(this);
    }

    private void initAndShowDialog() {
        adGooglePlayDialog = new AdGooglePlayDialog(HSApplication.getContext(), nativeAdView);
        adGooglePlayDialog.show();
        HSPreferenceHelper.getDefault().putLong(PREF_AD_SHOW_TIME, System.currentTimeMillis());
    }

    private boolean isShowedFixedTimeBefore() { // 是否在一定时间之前展示过广告
        Long lastTime = HSPreferenceHelper.getDefault().getLong(PREF_AD_SHOW_TIME, 0L);
        if (System.currentTimeMillis() > lastTime + (long) (24 * 3600 * 1000)) {
            return false;
        }
        return true;
    }

    private int getNativeAdViewWidth() {
        return (int) (DisplayUtils.getScreenWidthPixels() * 0.9);
    }

    public boolean loadAndShowAdIfConditionSatisfied() {
        if (!KCFeatureRestrictionConfig.isFeatureRestricted("AdGooglePlayNative")
                && HSConfig.optBoolean(false, "Application", "NativeAds", "GooglePlayNativeAd", "ShowAd")
                && !isShowedFixedTimeBefore()) {
            initNativeAdView();
            return true;
        } else {
            return false;
        }
    }

    public void cancel() {
        if (nativeAdView != null) {
            nativeAdView.setOnAdLoadedListener(null);
            nativeAdView.setOnAdClickedListener(null);
        }
        nativeAdView = null;
        adGooglePlayDialog = null;
    }

    @Override
    public void onAdLoaded(KCNativeAdView nativeAdView) {
        initAndShowDialog();
    }

    @Override
    public void onAdClicked(KCNativeAdView nativeAdView) {
        if (adGooglePlayDialog != null && adGooglePlayDialog.isShowing()) {
            adGooglePlayDialog.dismiss();
        }
    }
}
