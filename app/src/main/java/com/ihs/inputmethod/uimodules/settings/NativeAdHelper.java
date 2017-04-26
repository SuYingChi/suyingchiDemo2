package com.ihs.inputmethod.uimodules.settings;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.nativeads.NativeAdParams;
import com.ihs.keyboardutils.nativeads.NativeAdView;

/**
 * Created by jixiang on 16/11/18.
 */

public class NativeAdHelper {
    NativeAdView nativeAdView;
    String adPoolName = HSApplication.getContext().getResources().getString(R.string.ad_placement_keyboardsettingsad);

    public NativeAdHelper() {
    }

    public void createAd() {
        if (nativeAdView == null) {
            AdView adView = new AdView(HSApplication.getContext());
            //it will change its parent(NativeContentAdView) LayoutParams too
            adView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            nativeAdView = new NativeAdView(HSApplication.getContext(), adView);
            nativeAdView.setNativeAdType(NativeAdView.NativeAdType.ICON);
            NativeAdParams nativeAdParams = new NativeAdParams(adPoolName);
            nativeAdView.configParams(nativeAdParams);

            ViewItemBuilder.getAdsItem().viewContainer.removeAllViews();
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            ViewItemBuilder.getAdsItem().viewContainer.addView(nativeAdView, layoutParams);
        }
    }

    public void releaseAd() {
        if (nativeAdView != null) {
            nativeAdView.release();
            nativeAdView = null;
        }
    }

    private static class AdView extends LinearLayout {

        public AdView(Context context) {
            super(context);
            View.inflate(context, R.layout.panel_settings_ad_item, this);
            findViewById(R.id.ad_call_to_action).setVisibility(View.VISIBLE);
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
