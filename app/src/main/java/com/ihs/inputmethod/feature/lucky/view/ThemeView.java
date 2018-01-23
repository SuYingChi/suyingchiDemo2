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

import com.bumptech.glide.Glide;
import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.keyboard.HSKeyboardTheme;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSToastUtils;
import com.ihs.inputmethod.feature.common.AnimatorListenerAdapter;
import com.ihs.inputmethod.feature.common.ViewUtils;
import com.ihs.inputmethod.feature.lucky.LuckyPreloadManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.utils.ThemeZipDownloadUtils;
import com.ihs.keyboardutils.adbuffer.AdLoadingView;

import java.io.File;


public class ThemeView extends FlyAwardBaseView implements View.OnClickListener {

    public static final String THEME_DIRECTORY = "preload" + File.separator + "theme";
    public static final String ICON = "Icon";
    public static final String BANNER = "banner";

    private View mContainer;
    private ImageView mBanner;
    private TextView mTitle;
    private TextView mBody;
    private HSKeyboardTheme themeItem;

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
                File themeIcon = new File(LuckyPreloadManager.getDirectory(themeItem.mThemeName), ThemeView.ICON);
                Glide.with(HSApplication.getContext()).load(Uri.fromFile(themeIcon).toString()).into(mDragIcon);
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
        themeItem = LuckyPreloadManager.getInstance().getThemeInfo();
        if (themeItem == null) {
            return false;
        }

        File theme = new File(LuckyPreloadManager.getDirectory(themeItem.mThemeName), ThemeView.BANNER);
        Glide.with(HSApplication.getContext()).load(Uri.fromFile(theme).toString()).into(mBanner);
        mTitle.setText(themeItem.getThemeShowName());
//        mBody.setText(LauncherConfig.getMultilingualString(themeItem., "ShortDescription"));
        return true;
    }

    public void resetVisible() {
        mContainer.setVisibility(INVISIBLE);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.lucky_game_theme_action) {
//            ThemeDownloadManager.getInstance().downloadTheme(themeItem);

            String from = "lucky";
            ThemeZipDownloadUtils.startDownloadThemeZip(getContext(), from, themeItem.mThemeName, themeItem.getSmallPreivewImgUrl(), new AdLoadingView.OnAdBufferingListener() {
                @Override
                public void onDismiss(boolean success, boolean manually) {
                    if (success) {
                        ThemeZipDownloadUtils.logDownloadSuccessEvent(themeItem.mThemeName, from);
                        if (HSKeyboardThemeManager.isThemeZipFileDownloadAndUnzipSuccess(themeItem.mThemeName)) {
                            HSKeyboardThemeManager.moveNeedDownloadThemeToDownloadedList(themeItem.mThemeName, true);
                            //直接应用主题
                            if (HSKeyboardThemeManager.setKeyboardTheme(themeItem.mThemeName)) {

                            } else {
                                String failedString = HSApplication.getContext().getResources().getString(R.string.theme_apply_failed);
                                HSToastUtils.toastCenterLong(String.format(failedString, themeItem.getThemeShowName()));
                            }
                        }
                    }
                }
            });

        }
    }
}
