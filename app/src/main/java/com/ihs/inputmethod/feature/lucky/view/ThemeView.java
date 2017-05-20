package com.ihs.inputmethod.feature.lucky.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.utils.HSMarketUtils;
import com.ihs.inputmethod.feature.common.AnimatorListenerAdapter;
import com.ihs.inputmethod.feature.common.LauncherConfig;
import com.ihs.inputmethod.feature.common.Utils;
import com.ihs.inputmethod.feature.common.ViewUtils;
import com.ihs.inputmethod.feature.lucky.LuckyPreloadManager;
import com.ihs.inputmethod.uimodules.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.Map;


public class ThemeView extends FlyAwardBaseView implements View.OnClickListener {

    public static final String THEME_DIRECTORY = "preload" + File.separator + "theme";
    public static final String ICON = "icon";
    public static final String THEME = "theme";

    private String mPackageName;

    private View mContainer;
    private ImageView mBanner;
    private TextView mTitle;
    private TextView mBody;

    public ThemeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mContainer = ViewUtils.findViewById(this, R.id.lucky_game_theme_container);
        mIcon = ViewUtils.findViewById(this, R.id.lucky_game_theme_icon);
        mBanner = ViewUtils.findViewById(this, R.id.lucky_game_theme_image_container);
        mTitle = ViewUtils.findViewById(this, R.id.lucky_game_theme_title);
        mBody = ViewUtils.findViewById(this, R.id.lucky_game_theme_body);
        mBody.setAlpha(0.5f);
        mDragIcon = ViewUtils.findViewById(this, R.id.lucky_game_drag_theme_icon);
        View install = ViewUtils.findViewById(this, R.id.lucky_game_theme_action);
        install.setOnClickListener(this);

        LayoutParams layoutParams = new LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(0, getContext().getResources().getDimensionPixelSize(R.dimen.lucky_award_ad_view_container_top_margin), 0, 0);
        setLayoutParams(layoutParams);

        calculateAnimationDistance(ThemeView.this);
    }

    public AnimatorSet getThemeAnimation() {
        ObjectAnimator holdOn = ObjectAnimator.ofFloat(mDragIcon, "alpha", 1.0f, 1.0f);
        holdOn.setDuration(QUESTION_MARK_HOLD_ON_DURATION);
        holdOn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                File themeIcon = new File(Utils.getDirectory(ThemeView.THEME_DIRECTORY), ThemeView.ICON);
                ImageLoader.getInstance().displayImage(
                        Uri.fromFile(themeIcon).toString(),
                        mDragIcon);
            }
        });

        ObjectAnimator fadeIn = fadeIn(this);
        fadeIn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mContainer.setVisibility(VISIBLE);
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.playSequentially(dragUp(), holdOn, flipToIcon(), fadeIn);

        return set;
    }

    public boolean fetchTheme() {
        Map info = LuckyPreloadManager.getInstance().getThemeInfo();
        if (info == null) {
            return false;
        }

        File theme = new File(Utils.getDirectory(ThemeView.THEME_DIRECTORY), ThemeView.THEME);

        ImageLoader.getInstance().displayImage(
                Uri.fromFile(theme).toString(),
                mBanner);

        mPackageName = (String) info.get("packageName");
        mTitle.setText(LauncherConfig.getMultilingualString(info, "Name"));
        mBody.setText(LauncherConfig.getMultilingualString(info, "ShortDescription"));

        return true;
    }

    public void resetVisible() {
        mContainer.setVisibility(INVISIBLE);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.lucky_game_theme_action) {
            HSMarketUtils.browseAPP(mPackageName);
            HSAnalytics.logEvent("Lucky_Award_Theme_Install_Clicked");
        }
    }
}
