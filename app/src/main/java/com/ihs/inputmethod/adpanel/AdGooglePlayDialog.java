package com.ihs.inputmethod.adpanel;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.nativeads.NativeAdView;

/**
 * Created by yanxia on 2017/6/26.
 */

public class AdGooglePlayDialog extends Dialog {
    private static final int AD_DELAY = 1000;
    private ProgressBar progressBar;
    private FrameLayout frameLayoutAdContainer;
    private NativeAdView nativeAdView;
    private Handler handler = new Handler();

    public AdGooglePlayDialog(@NonNull Context context, NativeAdView nativeAdView) {
        super(context, R.style.DesignDialog);
        this.nativeAdView = nativeAdView;
        init();
    }

    public AdGooglePlayDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        init();
    }

    private void init() {
        HSLog.d("xiayan init");
        setCanceledOnTouchOutside(true);
    }

    /**
     * Similar to {@link Activity#onCreate}, you should initialize your dialog
     * in this method, including calling {@link #setContentView}.
     *
     * @param savedInstanceState If this dialog is being reinitialized after a
     *                           the hosting activity was previously shut down, holds the result from
     *                           the most recent call to {@link #onSaveInstanceState}, or null if this
     *                           is the first time.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HSLog.d("xiayan onCreate");
        setContentView(R.layout.google_play_dialog_ad);
        progressBar = (ProgressBar) findViewById(R.id.google_play_ad_progress_bar);
        frameLayoutAdContainer = (FrameLayout) findViewById(R.id.google_play_ad_container);
    }

    /**
     * Called when the dialog is starting.
     */
    @Override
    protected void onStart() {
        super.onStart();
        HSLog.d("xiayan onStart");
        progressBar.setVisibility(View.VISIBLE);
        frameLayoutAdContainer.setVisibility(View.GONE);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                if (nativeAdView.getParent() != null) {
                    ((ViewGroup) nativeAdView.getParent()).removeView(nativeAdView);
                }
                HSLog.d("xiayan width = " + nativeAdView.getWidth() + " height = " + nativeAdView.getHeight());
                frameLayoutAdContainer.addView(nativeAdView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                frameLayoutAdContainer.setVisibility(View.VISIBLE);
            }
        }, AD_DELAY);
    }

    /**
     * Start the dialog and display it on screen.  The window is placed in the
     * application layer and opaque.  Note that you should not override this
     * method to do initialization when the dialog is shown, instead implement
     * that in {@link #onStart}.
     */
    @Override
    public void show() {
        HSLog.d("xiayan show");
        try {
            Window window = getWindow();
            if (!(getContext() instanceof Activity) && window != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(HSApplication.getContext())) {
                    window.setType(WindowManager.LayoutParams.TYPE_TOAST);
                } else {
                    window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
                }
            }
            super.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called to tell you that you're stopping.
     */
    @Override
    protected void onStop() {
        frameLayoutAdContainer.removeAllViews();
        if (nativeAdView != null) {
            nativeAdView.release();
        }
        handler.removeCallbacksAndMessages(null);
        super.onStop();
    }

}
