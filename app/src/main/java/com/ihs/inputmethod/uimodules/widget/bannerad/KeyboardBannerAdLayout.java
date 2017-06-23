package com.ihs.inputmethod.uimodules.widget.bannerad;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.nativeads.NativeAdParams;
import com.ihs.keyboardutils.nativeads.NativeAdView;

/**
 * Created by Arthur on 17/5/18.
 */

public class KeyboardBannerAdLayout extends FrameLayout {
    private int lastX;
    private int lastY;

    private boolean lockedMode  = true;
    private OnCustomizeBarListener customizeBarListener;

    public interface OnCustomizeBarListener{
        void onHide();
    }

    public KeyboardBannerAdLayout(@NonNull Context context,OnCustomizeBarListener customizeBarListener) {
        super(context);
        this.customizeBarListener = customizeBarListener;
        init();

    }

    private void init() {

        View containerView = inflate(getContext(), R.layout.keyboard_banner_ad_layout, null);
        setBackgroundColor(Color.WHITE);
        final NativeAdView  nativeAdView = new NativeAdView(HSApplication.getContext(), containerView);

        containerView.findViewById(R.id.iv_close_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                customizeBarListener.onHide();
                setVisibility(GONE);
                nativeAdView.release();
            }
        });
        NativeAdParams nativeAdParams = new NativeAdParams(getContext().getResources().getString(R.string.ad_placement_keyboard_banner));
        nativeAdView.configParams(nativeAdParams);
        nativeAdView.setOnAdLoadedListener(new NativeAdView.OnAdLoadedListener() {
            @Override
            public void onAdLoaded(NativeAdView nativeAdView) {
                HSLog.e("广告");
                addView(nativeAdView, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });
        nativeAdView.setOnAdClickedListener(new NativeAdView.OnAdClickedListener() {
            @Override
            public void onAdClicked(NativeAdView nativeAdView) {
                HSLog.e("广告 ddd");
            }
        });
    }


    private KeyboardBannerAdLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private KeyboardBannerAdLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


}
