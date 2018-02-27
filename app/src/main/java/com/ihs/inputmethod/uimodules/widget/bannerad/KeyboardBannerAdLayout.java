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

import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.constants.AdPlacements;
import com.ihs.inputmethod.uimodules.R;

import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdContainerView;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdIconView;
import net.appcloudbox.ads.nativead.AcbNativeAdLoader;
import net.appcloudbox.ads.nativead.AcbNativeAdManager;
import net.appcloudbox.common.utils.AcbError;

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


        AcbNativeAdLoader adLoader = AcbNativeAdManager.createLoaderWithPlacement(getContext(), AdPlacements.NATIVE_KEYBOARD_BANNER);
        adLoader.load(1, new AcbNativeAdLoader.AcbNativeAdLoadListener() {
            @Override
            public void onAdReceived(AcbNativeAdLoader acbNativeAdLoader, List<AcbNativeAd> list) {
                if (list.size() > 0) {
                    nativeAd = list.get(0);

                    RelativeLayout containerView;
                    boolean inFaceBook = nativeAd.getVendor().name().toLowerCase().contains("facebook");
                    if (inFaceBook) {
                        containerView = (RelativeLayout) inflate(getContext(), R.layout.keyboard_banner_facebook_layout, null);
                    } else {
                        containerView = (RelativeLayout) inflate(getContext(), R.layout.keyboard_banner_ad_layout, null);
                        setBackgroundColor(Color.WHITE);
                    }

                    final AcbNativeAdContainerView acbNativeAdContainerView = new AcbNativeAdContainerView(getContext());
                    acbNativeAdContainerView.hideAdCorner();
                    containerView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL));
                    acbNativeAdContainerView.addContentView(containerView);
                    AcbNativeAdIconView adIconView = containerView.findViewById(R.id.ad_icon);
                    acbNativeAdContainerView.setAdTitleView(containerView.findViewById(R.id.ad_title));
                    acbNativeAdContainerView.setAdSubTitleView(containerView.findViewById(R.id.ad_subtitle));
                    acbNativeAdContainerView.setAdChoiceView(containerView.findViewById(R.id.ad_choice));
                    acbNativeAdContainerView.setAdIconView(adIconView);
                    acbNativeAdContainerView.setAdActionView(containerView.findViewById(R.id.ad_call_to_action));
                    acbNativeAdContainerView.fillNativeAd(nativeAd);
                    if (TextUtils.isEmpty(nativeAd.getSubtitle())) {
                        containerView.findViewById(R.id.ad_subtitle).setVisibility(GONE);
                    }

                    if (inFaceBook) {
                        addView(acbNativeAdContainerView, ViewGroup.LayoutParams.MATCH_PARENT, HSDisplayUtils.dip2px(68));
                    } else {
                        addView(acbNativeAdContainerView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    }
                    closeBtn.setVisibility(VISIBLE);
                    closeBtn.bringToFront();
                }
            }

            @Override
            public void onAdFinished(AcbNativeAdLoader acbNativeAdLoader, AcbError hsError) {

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
