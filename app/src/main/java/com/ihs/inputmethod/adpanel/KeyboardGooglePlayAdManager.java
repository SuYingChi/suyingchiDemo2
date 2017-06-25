package com.ihs.inputmethod.adpanel;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.nativeads.NativeAdParams;
import com.ihs.keyboardutils.nativeads.NativeAdView;

/**
 * Created by xiayan on 2017/6/25.
 */

public class KeyboardGooglePlayAdManager {
    private NativeAdView nativeAdView;
    private ProgressBar progressBar;

    public void init() {
        View view = LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ad_style_1, null);
        LinearLayout loadingView = (LinearLayout) LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ad_loading_3, null);
        LinearLayout.LayoutParams loadingLP = new LinearLayout.LayoutParams(width, (int) (width / 1.9f));
        loadingView.setLayoutParams(loadingLP);
        loadingView.setGravity(Gravity.CENTER);
        nativeAdView = new NativeAdView(HSApplication.getContext(), view, loadingView);

    }

    public void loadAd() {
        nativeAdView.configParams(new NativeAdParams(HSApplication.getContext().getString(R.string.ad_placement_themetryad), width, 1.9f));
    }
}
