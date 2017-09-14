package com.ihs.inputmethod.uimodules.ui.customize.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.uimodules.R;

/**
 * First page of wallpaper package preview view pager.
 *
 * 1. receive a WallpaperPackageInfo and fill the UI elements.
 * 2. receive animation order.
 */
public class WallpaperPackageCoverView extends RelativeLayout implements INotificationObserver {

    public static final String EVENT_PACKAGE_GUIDE_START = "EVENT_PACKAGE_GUIDE_START";
    public static final String EVENT_PACKAGE_GUIDE_END = "EVENT_PACKAGE_GUIDE_END";

    private TextView mName;
    private TextView mAddress;
    private TextView mQuotation;
    private ImageView mAvatar;

    public WallpaperPackageCoverView(Context context) {
        this(context, null);
    }

    public WallpaperPackageCoverView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WallpaperPackageCoverView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();

        mName = (TextView) findViewById(R.id.profile_name);
        mAddress = (TextView) findViewById(R.id.profile_address);
        mQuotation = (TextView) findViewById(R.id.quotation);
        mAvatar = (ImageView) findViewById(R.id.avatar);
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        HSGlobalNotificationCenter.addObserver(EVENT_PACKAGE_GUIDE_START, this);
        HSGlobalNotificationCenter.addObserver(EVENT_PACKAGE_GUIDE_END, this);
    }

    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        HSGlobalNotificationCenter.removeObserver(this);
    }

    @Override public void onReceive(String s, HSBundle hsBundle) {
        View guideView = findViewById(R.id.package_guide);
        switch (s) {
            case EVENT_PACKAGE_GUIDE_START:
                guideView.setScaleX(0.5f);
                guideView.setScaleY(0.5f);
                guideView.setAlpha(0f);

                guideView.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(140)
                        .setInterpolator(new LinearInterpolator())
                        .start();
                break;
            case EVENT_PACKAGE_GUIDE_END:
                guideView.animate()
                        .alpha(0f)
                        .scaleX(0.8f)
                        .scaleY(0.8f)
                        .setDuration(180)
                        .start();
                break;
        }
    }
}
