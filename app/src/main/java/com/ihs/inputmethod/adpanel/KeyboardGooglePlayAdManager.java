package com.ihs.inputmethod.adpanel;

import android.view.LayoutInflater;
import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.nativeads.NativeAdParams;
import com.ihs.keyboardutils.nativeads.NativeAdView;


/**
 * Created by xiayan on 2017/6/25.
 */

public class KeyboardGooglePlayAdManager implements NativeAdView.OnAdLoadedListener {
    private String adPlacement;
    private NativeAdView nativeAdView;
    private AdGooglePlayDialog adGooglePlayDialog;

    public KeyboardGooglePlayAdManager(String adPlacement) {
        this.adPlacement = adPlacement;
    }

    private void initNativeAdView() {
        int width = getNativeAdViewWidth();
        HSLog.d("xiayan width = " + width);
        View view = LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ad_google_play, null);
        nativeAdView = new NativeAdView(HSApplication.getContext(), view);
        nativeAdView.configParams(new NativeAdParams(adPlacement, width, 1.9f));
        nativeAdView.setOnAdLoadedListener(this);
    }

    private void initAndShowDialog() {
        adGooglePlayDialog = new AdGooglePlayDialog(HSApplication.getContext(), nativeAdView);
        adGooglePlayDialog.show();
    }

    private int getNativeAdViewWidth() {
        return (int) (DisplayUtils.getScreenWidthPixels() * 0.9);
    }

    public void loadAndShowAd() {
        initNativeAdView();
    }

    public void cancel() {
        nativeAdView.setOnAdLoadedListener(null);
        nativeAdView = null;
        adGooglePlayDialog = null;
    }

    @Override
    public void onAdLoaded(NativeAdView nativeAdView) {
        initAndShowDialog();
    }
}
