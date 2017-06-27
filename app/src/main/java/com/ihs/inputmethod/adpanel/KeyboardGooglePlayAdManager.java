package com.ihs.inputmethod.adpanel;

import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.nativeads.NativeAdParams;
import com.ihs.keyboardutils.nativeads.NativeAdView;

/**
 * Created by xiayan on 2017/6/25.
 */

public class KeyboardGooglePlayAdManager implements NativeAdView.OnAdLoadedListener, NativeAdView.OnAdClickedListener {
    private String adPlacement;
    private NativeAdView nativeAdView;
    private int width;
    private AdGooglePlayDialog adGooglePlayDialog;

    public KeyboardGooglePlayAdManager(String adPlacement) {
        this.adPlacement = adPlacement;
        init();
    }

    private void init() {
        adGooglePlayDialog = new AdGooglePlayDialog(HSApplication.getContext());
        width = HSDisplayUtils.getScreenWidthForContent() - HSDisplayUtils.dip2px(16);
        View view = LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ad_google_play, null);
        nativeAdView = new NativeAdView(HSApplication.getContext(), view);
        adGooglePlayDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                nativeAdView.release();
            }
        });
    }

    public void loadAdAndShow() {
        if (nativeAdView != null && nativeAdView.isAdLoaded()) {
            showAdInDialog();
        } else {
            nativeAdView.configParams(new NativeAdParams(adPlacement, width, 1.9f));
            nativeAdView.setOnAdLoadedListener(this);
        }
    }

    private void showAdInDialog() {
        if (!adGooglePlayDialog.isShowing()) {
            adGooglePlayDialog.show();
        }
    }

    public void cancel() {
        nativeAdView.setOnAdLoadedListener(null);
    }

    @Override
    public void onAdLoaded(NativeAdView nativeAdView) {
        adGooglePlayDialog.setContentView(nativeAdView);
        showAdInDialog();
    }

    @Override
    public void onAdClicked(NativeAdView nativeAdView) {
        adGooglePlayDialog.dismiss();
    }
}
