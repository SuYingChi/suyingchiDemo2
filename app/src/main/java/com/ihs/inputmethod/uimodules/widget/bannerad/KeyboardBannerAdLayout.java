package com.ihs.inputmethod.uimodules.widget.bannerad;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.acb.adadapter.AcbNativeAd;
import com.acb.adadapter.ContainerView.AcbNativeAdContainerView;
import com.acb.adadapter.ContainerView.AcbNativeAdIconView;
import com.acb.nativeads.AcbNativeAdLoader;
import com.ihs.commons.utils.HSError;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;

import java.util.List;

/**
 * Created by Arthur on 17/5/18.
 */

public class KeyboardBannerAdLayout extends FrameLayout {

    private AcbNativeAd nativeAd;

    public KeyboardBannerAdLayout(@NonNull Context context) {
        super(context);
        init();
    }

    private void init() {
        final ImageView closeBtn = new ImageView(getContext());
        VectorDrawableCompat closeDrawable = VectorDrawableCompat.create(getResources(), R.drawable.banner_ad_close_button, null);
        closeBtn.setImageDrawable(closeDrawable);
        closeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setVisibility(GONE);
                if (nativeAd != null) {
                    nativeAd.release();
                    nativeAd = null;
                }
            }
        });
        closeBtn.setScaleType(ImageView.ScaleType.FIT_XY);
        LayoutParams closeParams = new LayoutParams(HSDisplayUtils.dip2px(26), HSDisplayUtils.dip2px(26));
        closeParams.setMargins(0, 0, HSDisplayUtils.dip2px(6), 0);
        closeParams.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
        int padding = HSDisplayUtils.dip2px(3);
        closeBtn.setPadding(padding, padding, padding, padding);
        closeBtn.setVisibility(GONE);


        AcbNativeAdLoader adLoader = new AcbNativeAdLoader(getContext(), getContext().getResources().getString(R.string.ad_placement_keyboard_banner));
        adLoader.load(1, new AcbNativeAdLoader.AcbNativeAdLoadListener() {
            @Override
            public void onAdReceived(AcbNativeAdLoader acbNativeAdLoader, List<AcbNativeAd> list) {
                if (list.size() > 0) {
                    nativeAd = list.get(0);

                    RelativeLayout containerView;
                    if (nativeAd.getVendor().name().toLowerCase().contains("facebook")) {
                        containerView = (RelativeLayout) inflate(getContext(), R.layout.keyboard_banner_facebook_layout, null);
                    } else {
                        containerView = (RelativeLayout) inflate(getContext(), R.layout.keyboard_banner_ad_layout, null);
                        setBackgroundColor(Color.WHITE);
                    }

                    final AcbNativeAdContainerView acbNativeAdContainerView = new AcbNativeAdContainerView(getContext());
                    acbNativeAdContainerView.setClickViewList(null);
                    containerView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL));
                    acbNativeAdContainerView.addContentView(containerView);
                    AcbNativeAdIconView adIconView = (AcbNativeAdIconView) containerView.findViewById(R.id.ad_icon);
                    acbNativeAdContainerView.setAdTitleView((TextView) containerView.findViewById(R.id.ad_title));
                    acbNativeAdContainerView.setAdSubTitleView((TextView) containerView.findViewById(R.id.ad_subtitle));
                    acbNativeAdContainerView.setAdChoiceView((ViewGroup) containerView.findViewById(R.id.ad_choice));
                    acbNativeAdContainerView.setAdIconView(adIconView);
                    acbNativeAdContainerView.setAdActionView(containerView.findViewById(R.id.ad_call_to_action));
                    acbNativeAdContainerView.fillNativeAd(nativeAd);
                    if (TextUtils.isEmpty(nativeAd.getSubtitle())) {
                        containerView.findViewById(R.id.ad_subtitle).setVisibility(GONE);
                    }

                    addView(acbNativeAdContainerView, ViewGroup.LayoutParams.MATCH_PARENT, HSDisplayUtils.dip2px(68));
                    closeBtn.setVisibility(VISIBLE);
                }
            }

            @Override
            public void onAdFinished(AcbNativeAdLoader acbNativeAdLoader, HSError hsError) {

            }
        });

        addView(closeBtn, closeParams);
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (nativeAd != null) {
            nativeAd.release();
        }
    }
}
