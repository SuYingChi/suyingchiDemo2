package com.ihs.inputmethod.uimodules.settings;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.nativeads.KCNativeAdView;
import com.ihs.keyboardutils.view.FlashFrameLayout;

/**
 * Created by jixiang on 16/11/18.
 */

public class NativeAdHelper {
    KCNativeAdView nativeAdView;
    private static FlashFrameLayout flashAdContainer;
    private static String adPoolName = HSApplication.getContext().getResources().getString(R.string.ad_placement_keyboardsettingsad);
    private boolean isAdFlashAnimationPlayed = false;
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener;

    public static boolean isAdPoolExist(){
        return HSConfig.exists("nativeAds",adPoolName);
    }

    public NativeAdHelper() {
    }

    public void createAd() {
        if (nativeAdView == null && ViewItemBuilder.getAdsItem().viewContainer != null) {
            AdView adView = new AdView(HSApplication.getContext());
            //it will change its parent(NativeContentAdView) LayoutParams too
            adView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            nativeAdView = new KCNativeAdView(HSApplication.getContext());
            nativeAdView.setAdLayoutView(adView);
            nativeAdView.setNativeAdType(KCNativeAdView.NativeAdType.ICON);
            nativeAdView.load(adPoolName);
            ViewItemBuilder.getAdsItem().viewContainer.removeAllViews();
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            ViewItemBuilder.getAdsItem().viewContainer.addView(nativeAdView, layoutParams);
            nativeAdView.getViewTreeObserver().addOnGlobalLayoutListener(getLayoutListener());
        }
    }

    public void releaseAd() {
        if (nativeAdView != null) {
            nativeAdView.getViewTreeObserver().removeGlobalOnLayoutListener(mGlobalLayoutListener);
            nativeAdView.release();
            nativeAdView = null;
        }
    }

    private ViewTreeObserver.OnGlobalLayoutListener getLayoutListener() {
        if (mGlobalLayoutListener == null) {
            mGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    showAdFlashAnimationIfNecessary();
                }
            };
        }
        return mGlobalLayoutListener;
    }

    public void setAdFlashAnimationPlayed(boolean adFlashAnimationPlayed) {
        isAdFlashAnimationPlayed = adFlashAnimationPlayed;
    }

    private void showAdFlashAnimationIfNecessary() {
        if (!isAdFlashAnimationPlayed) {
            showAdFlashAnimation();
        }
    }

    private void showAdFlashAnimation() {
        if (nativeAdView != null && nativeAdView.isAdLoaded()) {
            flashAdContainer.startShimmerAnimation();
            isAdFlashAnimationPlayed = true;
        } else {
            isAdFlashAnimationPlayed = false;
        }
    }

    private static class AdView extends LinearLayout {

        public AdView(Context context) {
            super(context);
            View.inflate(context, R.layout.panel_settings_ad_item, this);
            findViewById(R.id.ad_call_to_action).setVisibility(View.VISIBLE);
            flashAdContainer = (FlashFrameLayout) findViewById(R.id.ad_container);
            TextView adTitleText = (TextView) findViewById(R.id.ad_title);
            adTitleText.setTextColor(HSKeyboardThemeManager.getCurrentTheme().getStyledTextColor());
        }
    }

    public static class AdLoadingView extends RelativeLayout {
        private ImageView progressImageView;
        private RelativeLayout progressbarRoot;
        private boolean isShowingProgressbar;

        public AdLoadingView(Context context) {
            super(context);
            progressbarRoot = (RelativeLayout) View.inflate(context, R.layout.panel_settings_ad_loading_layout, this);
            progressImageView = (ImageView) progressbarRoot.findViewById(R.id.progress_imgview);
        }

        public void showLoading() {
            if (isShowingProgressbar) {
                return;
            }
            isShowingProgressbar = true;
            progressImageView.setBackgroundDrawable(HSKeyboardThemeManager.getStyledDrawable(null, "settings_ad_bg.png"));
            TextView tv_loading = (TextView) progressbarRoot.findViewById(R.id.tv_loading);
            tv_loading.setTextColor(HSKeyboardThemeManager.getCurrentTheme().getStyledTextColor());
            progressbarRoot.setVisibility(View.VISIBLE);
        }
    }
}
