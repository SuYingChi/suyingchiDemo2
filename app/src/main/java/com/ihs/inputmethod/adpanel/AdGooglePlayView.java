package com.ihs.inputmethod.adpanel;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.nativeads.NativeAdParams;
import com.ihs.keyboardutils.nativeads.NativeAdView;

/**
 * Created by yanxia on 2017/6/26.
 */

public class AdGooglePlayView extends RelativeLayout implements NativeAdView.OnAdLoadedListener {
    private String adPlacement;
    private NativeAdView nativeAdView;
    private int width;
    private AdGooglePlayDialog adGooglePlayDialog;
    private boolean hasPurchaseNoAds;
    private boolean isAdLoaded;

    public AdGooglePlayView(Context context) {
        super(context);
        init();
    }

    public AdGooglePlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AdGooglePlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        adGooglePlayDialog = new AdGooglePlayDialog(getContext());
        inflate(getContext(), R.layout.ad_google_play, this);
        width = HSDisplayUtils.getScreenWidthForContent() - HSDisplayUtils.dip2px(16);
        if (!hasPurchaseNoAds) {
            nativeAdView = new NativeAdView(getContext(), this);
            initAdView();
        }
    }

    private void initAdView() {
        if (!isAdLoaded) {
            nativeAdView.configParams(new NativeAdParams(adPlacement, width, 1.9f));
        }
    }

    public void configParams(boolean hasPurchaseNoAds, String adPlacement) {
        this.hasPurchaseNoAds = hasPurchaseNoAds;
        this.adPlacement = adPlacement;
    }

    public void showAdInDialog() {
        if (!isAdLoaded) {
            return;
        }
        adGooglePlayDialog.setContentView(this);
        if (!adGooglePlayDialog.isShowing()) {
            adGooglePlayDialog.show();
        }
    }

    @Override
    public void onAdLoaded(NativeAdView nativeAdView) {
        isAdLoaded = true;
        showAdInDialog();
    }
}
