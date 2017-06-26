package com.ihs.inputmethod.adpanel;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.nativeads.NativeAdParams;
import com.ihs.keyboardutils.nativeads.NativeAdView;

/**
 * Created by xiayan on 2017/6/25.
 */

public class KeyboardGooglePlayAdManager implements NativeAdView.OnAdLoadedListener{
    private String adPlacement;
    private NativeAdView nativeAdView;
    private ProgressBar progressBar;
    private int width;

    public KeyboardGooglePlayAdManager(String adPlacement) {
        this.adPlacement = adPlacement;
    }

    public void init() {
        width = HSDisplayUtils.getScreenWidthForContent() - HSDisplayUtils.dip2px(16);
        View view = LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ad_style_1, null);
        LinearLayout loadingView = (LinearLayout) LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ad_loading_3, null);
        LinearLayout.LayoutParams loadingLP = new LinearLayout.LayoutParams(width, (int) (width / 1.9f));
        loadingView.setLayoutParams(loadingLP);
        loadingView.setGravity(Gravity.CENTER);
        nativeAdView = new NativeAdView(HSApplication.getContext(), view, loadingView);
    }

    public void loadAd() {
        nativeAdView.configParams(new NativeAdParams(adPlacement, width, 1.9f));
    }

    @Override
    public void onAdLoaded(NativeAdView nativeAdView) {

    }
}
