package com.ihs.booster.boost.floating;

import android.animation.ArgbEvaluator;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.booster.HSBoostManager;
import com.ihs.booster.R;
import com.ihs.booster.boost.common.FloatWindowManager;
import com.ihs.booster.boost.common.PrefsUtils;
import com.ihs.booster.boost.common.viewdata.BoostApp;
import com.ihs.booster.common.asynctask.BoostProgress;
import com.ihs.booster.common.asynctask.MBAsyncTask.OnProcessListener;
import com.ihs.booster.common.sizeformat.FormatSizeBuilder;
import com.ihs.booster.common.view.SeekCircleProgressBar;
import com.ihs.booster.constants.AnimationConstant;
import com.ihs.booster.manager.BoostManager;
import com.ihs.booster.manager.MBMemoryManager;
import com.ihs.booster.utils.GAnalyticsUtils;
import com.ihs.booster.utils.L;
import com.ihs.keyboardutils.nativeads.NativeAdParams;
import com.ihs.keyboardutils.nativeads.NativeAdView;

import java.util.List;

public class FloatBoostView extends RelativeLayout {

    /**
     * 记录小悬浮窗的宽度
     */
    public static int viewWidth;

    /**
     * 记录小悬浮窗的高度
     */
    public static int viewHeight;

    /**
     * 记录小悬浮窗的宽度
     */
    public static int ball_viewWidth;

    /**
     * 记录小悬浮窗的高度
     */
    public static int ball_viewHeight;
    /**
     * 记录小悬浮窗的高度
     */
    public static int ball_y;

    /**
     * 记录系统状态栏的高度
     */
    private static int statusBarHeight;
    protected GradientDrawable layoutBoostBG;
    Animation alpha_anim_seek_progress_appear;
    Animation alpha_anim_highlight_bg_appear;
    Animation alpha_anim_txt_percent;
    Animation scale_anima_txt_percent;
    AnimationSet animset_txt_percent_appear;
    AnimationSet animation_boost_done;
    ValueAnimator value_anim_seek_progress;
    ValueAnimator value_anim_app_count;
    ValueAnimator color_anim_clean_bg;
    Animation txt_boost_indicator_animation;
    Animation txt_boost_indicator_done_animation;
    Animation img_boosting_done_animation;
    Animation img_boosting_done_scale_animation;
    Animation rotate_anim_img_half_ring_with_star;
    Animation alpha_anim_img_half_ring_with_star;
    Animation alpha_anim_ring_full_appear;
    Animation translate_anim_app_name_one;
    Animation alpha_anim_app_name_one;
    AnimationSet animation_set_app_name_one;
    AnimationSet animation_set_app_name_three;
    Animation alpha_anim_app_name_three;
    Animation translate_anim_app_name_three;
    AnimationSet translate_animation_set_app_name;
    AnimationSet animationSet;
    /**
     * 小悬浮窗的参数
     */
    private WindowManager.LayoutParams mParams;
    private View root_view;
    private View layoutBoost;
    private View alertRoot;
    private ImageView img_highlight_bg;
    private ImageView img_ring_dark_outside;
    private ImageView img_ring_dark_inside;
    private ImageView img_ring_full_white;
    private SeekCircleProgressBar seek_progressbar;
    private ImageView img_ring_half_with_star;
    private ImageView img_boosting_done;
    private TextView txt_boosting_percent;
    private TextView txt_boosted_size;
    private ImageView btnClose;
    private MBMemoryManager memoryManager = MBMemoryManager.getInstance();
    private View layoutAD;
    private boolean isCleanExpired = false;

    private final boolean adAvalible;

    private static final String PLACEMENT_BOOST = "BoostDone1";
    private NativeAdView nativeAdView;

    public FloatBoostView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.float_memory_clean, this);
        root_view = findViewById(R.id.root_view);
        layoutBoost = findViewById(R.id.layout_boost);
        alertRoot = findViewById(R.id.alert_root);
        int w = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int h = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        root_view.measure(w, h);
        viewHeight = root_view.getMeasuredHeight();
        viewWidth = root_view.getMeasuredWidth();
        img_highlight_bg = (ImageView) layoutBoost.findViewById(R.id.img_highlight_bg);
        WindowManager windowManager = FloatWindowManager.getInstance().getWindowManager();
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        LayoutParams params = (LayoutParams) img_highlight_bg.getLayoutParams();
        params.width = screenWidth;
        params.height = screenWidth;
        img_highlight_bg.setLayoutParams(params);
        img_highlight_bg.setVisibility(View.INVISIBLE);
        btnClose = (ImageView) layoutBoost.findViewById(R.id.btn_close);
        btnClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                HSAnalytics.logEvent("Boost_Window_Close_Clicked");
                GAnalyticsUtils.logEvent("Boost", "Boost_Window_Close_Clicked");
                dismiss();
            }
        });
        img_ring_dark_outside = (ImageView) layoutBoost.findViewById(R.id.img_ring_dark_outside);
        img_ring_dark_inside = (ImageView) layoutBoost.findViewById(R.id.img_ring_dark_inside);

        seek_progressbar = (SeekCircleProgressBar) layoutBoost.findViewById(R.id.seek_circle_progressbar);
        //rotate_anim_img_half_ring_with_star
        img_ring_half_with_star = (ImageView) layoutBoost.findViewById(R.id.img_ring_half_with_star);
        img_ring_full_white = (ImageView) layoutBoost.findViewById(R.id.img_ring_full_white);

        img_boosting_done = (ImageView) layoutBoost.findViewById(R.id.img_clean_done);
        txt_boosted_size = (TextView) layoutBoost.findViewById(R.id.txt_boosted_size);
        txt_boosting_percent = (TextView) layoutBoost.findViewById(R.id.txt_boosting_size);
        layoutAD = View.inflate(context, R.layout.ad_float_boost, null);
        init_appear_Animation();
        init_clean_progress_animation();
        isCleanExpired = memoryManager.isCleanExpired();
        ScaleAnimation scaleAnimation = new ScaleAnimation(0, 1, 0, 1, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        animationSet = new AnimationSet(true);
        animationSet.addAnimation(scaleAnimation);
        animationSet.setDuration(200);
        alertRoot.startAnimation(animationSet);
        animationSet.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isCleanExpired) {
                    startCleanAnimation();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        L.l("isCleanExpired:" + isCleanExpired);
        if (isCleanExpired) {
            memoryManager.scan(new OnProcessListener() {
                @Override
                public void onStarted() {

                }

                @Override
                public void onProgressUpdated(BoostProgress boostProgress) {

                }

                @Override
                public void onCompleted(List<BoostApp> apps) {
                    memoryManager.clean(null);
                }
            });
        } else {
            animation_show_done();
        }

        adAvalible = NativeAdView.isLocalAdAvailable(context, PLACEMENT_BOOST);


        if (adAvalible) {
            HSAnalytics.logEvent("Boost_AD_Viewed");
            GAnalyticsUtils.logEvent("Boost", "Boost_AD_Viewed");
            layoutBoost.setBackgroundResource(R.drawable.float_boost_top_bg);
            initAdView();
        } else {
            HSAnalytics.logEvent("Boost_AD_None");
            GAnalyticsUtils.logEvent("Boost", "Boost_AD_None");
            layoutAD.setVisibility(GONE);
            layoutBoost.setBackgroundResource(R.drawable.float_boost_top_bg2);
        }
        layoutBoostBG = (GradientDrawable) layoutBoost.getBackground();
        setBackColor(BoostManager.getInstance().getFunctionColor(BoostManager.Function.MEMORY));
        HSAnalytics.logEvent("Boost_Window_Viewed");
        GAnalyticsUtils.logEvent("Boost", "Boost_Window_Viewed");
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        L.l("newConfig:" + newConfig.toString() + " orientation:" + newConfig.orientation);
    }

    public void initAdView() {
        layoutAD.setVisibility(VISIBLE);

        nativeAdView = new NativeAdView(getContext(), layoutAD);
        nativeAdView.configParams(new NativeAdParams(PLACEMENT_BOOST));
        nativeAdView.setOnAdClickedListener(new NativeAdView.OnAdClickedListener() {
            @Override
            public void onAdClicked(NativeAdView nativeAdView) {
                HSAnalytics.logEvent("Boost_AD_Clicked");
                GAnalyticsUtils.logEvent("Boost", "Boost_AD_Clicked");
                dismiss();
            }
        });
    }

    private void setBackColor(int color) {
        layoutBoostBG.setColor(color);
        layoutBoost.invalidate();
    }

    private void init_appear_Animation() {
        img_ring_dark_outside.setVisibility(View.INVISIBLE);
        img_ring_dark_inside.setVisibility(View.INVISIBLE);
        img_ring_full_white.setVisibility(View.INVISIBLE);
        seek_progressbar.setBarBackColor(AnimationConstant.SCALE_BACK_COLOR);
        seek_progressbar.setBarForeColor(AnimationConstant.SCALE_FORE_COLOR);
        seek_progressbar.setBarWidth(9);
        seek_progressbar.setVisibility(View.INVISIBLE);
        img_ring_half_with_star.setVisibility(View.INVISIBLE);
        img_boosting_done.setVisibility(View.INVISIBLE);
        txt_boosted_size.setVisibility(INVISIBLE);
        txt_boosting_percent.setVisibility(View.INVISIBLE);
        // animation 2: progress
        alpha_anim_seek_progress_appear = new AlphaAnimation(0f, 1);
        alpha_anim_seek_progress_appear.setDuration(AnimationConstant.PROGRESS_DURATION);
        alpha_anim_seek_progress_appear.setStartOffset(AnimationConstant.PROGRESS_START_TIME);

        // animation 3: highlight bg
        alpha_anim_highlight_bg_appear = new AlphaAnimation(0f, 1f);
        alpha_anim_highlight_bg_appear.setDuration(AnimationConstant.CENTER_HIGHLIGHT_DURATION);
        alpha_anim_highlight_bg_appear.setStartOffset(AnimationConstant.CENTER_HIGHLIGHT_START_TIME);

        // animation 4:txt_boost_percent
        alpha_anim_txt_percent = new AlphaAnimation(AnimationConstant.BOOSTING_PERCENT_START_ALPHA, AnimationConstant.BOOSTING_PERCENT_END_ALPHA);
        scale_anima_txt_percent = new ScaleAnimation(AnimationConstant.BOOSTING_PERCENT_START_SCALE, AnimationConstant.BOOSTING_PERCENT_END_SCALE,
                AnimationConstant.BOOSTING_PERCENT_START_SCALE, AnimationConstant.BOOSTING_PERCENT_END_SCALE, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF,
                0.5f);

        animset_txt_percent_appear = new AnimationSet(true);
        animset_txt_percent_appear.addAnimation(alpha_anim_txt_percent);
        animset_txt_percent_appear.addAnimation(scale_anima_txt_percent);
        animset_txt_percent_appear.setDuration(AnimationConstant.LAYOUT_DATA_SIZE_APPEAR_ANIM_DURATION);
        animset_txt_percent_appear.setStartOffset(AnimationConstant.LAYOUT_DATA_SIZE_APPEAR_ANIM_START_OFFSET);
        animset_txt_percent_appear.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startSeekProgressAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void setProgress(int progress) {
        seek_progressbar.setProgress(progress);
    }

    private void init_clean_progress_animation() {
        // ------ start progress
        // animation 5:seekProgressAnimation
        value_anim_seek_progress = ObjectAnimator.ofInt(FloatBoostView.this, "progress", 0, 100);
        value_anim_seek_progress.setDuration(AnimationConstant.CLEAN_PROGRESS_DURATION);
        value_anim_seek_progress.setEvaluator(new IntEvaluator());
        value_anim_seek_progress.setInterpolator(new AccelerateDecelerateInterpolator());
        value_anim_seek_progress.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int progress = Integer.parseInt(animation.getAnimatedValue().toString());
                updateProgressUI(progress);
                if (progress == 97) {
                    // animation 11: ring_full_appear
                    img_ring_full_white.setVisibility(View.VISIBLE);
                    img_ring_full_white.startAnimation(alpha_anim_ring_full_appear);
                }
                if (progress == 100) {
                    AlphaAnimation alpha_anim_disappear = new AlphaAnimation(1f, 0f);
                    alpha_anim_disappear.setDuration(AnimationConstant.TXT_BOOST_INDICATOR_DONE_DURATION);
                    alpha_anim_disappear.setStartOffset(100);
                    txt_boosting_percent.startAnimation(alpha_anim_disappear);
                    alpha_anim_disappear.setAnimationListener(new AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            txt_boosting_percent.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    //txt_boost_percent
                    animation_show_done();
                }
            }
        });

        // animation 6:img_rotatering_half_animation
        rotate_anim_img_half_ring_with_star = new RotateAnimation(0f, 360 * 5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate_anim_img_half_ring_with_star.setDuration(AnimationConstant.CLEAN_PROGRESS_DURATION);
        rotate_anim_img_half_ring_with_star.setInterpolator(new AccelerateInterpolator(1.4f));
        //title & back color
        // animation 7: backgroud color
        color_anim_clean_bg = ObjectAnimator.ofInt(this, "backColor", BoostManager.getInstance().getCleanAnimColorArray(BoostManager.Function.MEMORY));
        color_anim_clean_bg.setDuration(AnimationConstant.CLEAN_PROGRESS_DURATION);
        color_anim_clean_bg.setEvaluator(new ArgbEvaluator());

        // ======= Done Animation
        // animation 8: txt_boost_indicator_animation
        txt_boost_indicator_animation = new AlphaAnimation(1f, 0f);
        txt_boost_indicator_animation.setDuration(AnimationConstant.TXT_BOOST_INDICATOR_DONE_DURATION);

        // animation 9: txt_boost_indicator_done_animation
        txt_boost_indicator_done_animation = new AlphaAnimation(0f, 1f);
        txt_boost_indicator_done_animation.setDuration(AnimationConstant.TXT_BOOST_INDICATOR_DONE_DURATION);

        // animation 10: img_boosting_done
        img_boosting_done_animation = new AlphaAnimation(0f, 1f);
        img_boosting_done_scale_animation = new ScaleAnimation(0.1f, 1f, 0.1f, 1f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        animation_boost_done = new AnimationSet(true);
        animation_boost_done.setDuration(AnimationConstant.TXT_BOOST_INDICATOR_DONE_DURATION);
        animation_boost_done.setStartOffset(AnimationConstant.TXT_BOOST_INDICATOR_DONE_DURATION);
        animation_boost_done.setInterpolator(new AnticipateOvershootInterpolator());
        animation_boost_done.addAnimation(img_boosting_done_animation);
        animation_boost_done.addAnimation(img_boosting_done_scale_animation);

        // animation 11: ring_full_appear
        alpha_anim_ring_full_appear = new AlphaAnimation(0f, 1f);
        alpha_anim_ring_full_appear.setDuration(AnimationConstant.RING_FULL_APPEAR_DURATION);

        // animation 12: img_rotatering_half_animation
        alpha_anim_img_half_ring_with_star = new AlphaAnimation(1f, 0f);
        alpha_anim_img_half_ring_with_star.setDuration(AnimationConstant.TXT_BOOST_INDICATOR_DONE_DURATION);
        alpha_anim_img_half_ring_with_star.setStartOffset(100);
        alpha_anim_img_half_ring_with_star.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                img_ring_half_with_star.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void startCleanAnimation() {
        // animation 1: rings
        img_ring_dark_outside.setVisibility(View.VISIBLE);
        img_ring_dark_inside.setVisibility(View.VISIBLE);
        animation_scale_ring(img_ring_dark_outside, AnimationConstant.RING_OUTSIDE_SIZE_SCALE_FACTORS, AnimationConstant.RING_OUTSIDE_DURATIONS, AnimationConstant.RING_OUTSIDE_START_TIME);
        animation_scale_ring(img_ring_dark_inside, AnimationConstant.RING_INSIDE_SIZE_SCALE_FACTORS, AnimationConstant.RING_INSIDE_DURATIONS, AnimationConstant.RING_INSIDE_START_TIME);

        // animation 2: progress
        seek_progressbar.startAnimation(alpha_anim_seek_progress_appear);
        seek_progressbar.setVisibility(View.VISIBLE);

        // animation 3: highlight bg
        img_highlight_bg.setVisibility(View.VISIBLE);
        img_highlight_bg.startAnimation(alpha_anim_highlight_bg_appear);

        // animation 4:txt_boost_percent
        txt_boosting_percent.setText(String.format(HSApplication.getContext().getString(R.string.txt_boosting_percent), 0));
        txt_boosting_percent.startAnimation(animset_txt_percent_appear);
        txt_boosting_percent.setVisibility(View.VISIBLE);
    }

    private void startSeekProgressAnimation() {
        // 5 seekProgressAnimation
        value_anim_seek_progress.start();
        // 6 img_rotatering_half
        img_ring_half_with_star.setVisibility(View.VISIBLE);
        img_ring_half_with_star.startAnimation(rotate_anim_img_half_ring_with_star);

        // 7 backgroud color
        color_anim_clean_bg.start();
        // app sliding
    }

    private void updateProgressUI(int progress) {
        String progressStr = String.format(HSApplication.getContext().getString(R.string.txt_boosting_percent), progress);
        txt_boosting_percent.setText(progressStr);
        txt_boosting_percent.invalidate();
    }

    private void animation_show_done() {
        txt_boosting_percent.setVisibility(View.INVISIBLE);
        img_ring_full_white.setVisibility(View.VISIBLE);
        // animation 10: img_boosting_done
        img_boosting_done.setVisibility(View.VISIBLE);
        img_boosting_done.startAnimation(animation_boost_done);

        animation_boost_done.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                img_ring_half_with_star.setVisibility(View.INVISIBLE);
                if (isCleanExpired) {
                    FormatSizeBuilder formatSizeBuilder = new FormatSizeBuilder(PrefsUtils.getLastMemoryBoostedSize());
                    String faster_percent = formatSizeBuilder.size + formatSizeBuilder.unit;
                    txt_boosted_size.setText(String.format(getContext().getString(R.string.txt_alarm_toast_boosted_memory), faster_percent));
                } else {
                    txt_boosted_size.setText(getContext().getString(R.string.txt_toast_already_boosted_memory));
                }
                txt_boosted_size.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        // animation 12: img_rotatering_half_animation
        img_ring_half_with_star.startAnimation(alpha_anim_img_half_ring_with_star);
        img_ring_half_with_star.setVisibility(View.INVISIBLE);

        //img_ring_dark_outside
        img_ring_dark_outside.startAnimation(alpha_anim_img_half_ring_with_star);
        img_ring_dark_outside.setVisibility(View.INVISIBLE);
        //img_ring_dark_inside
        img_ring_dark_inside.startAnimation(alpha_anim_img_half_ring_with_star);
        img_ring_dark_inside.setVisibility(View.INVISIBLE);
        //seek_progressbar
        seek_progressbar.startAnimation(alpha_anim_img_half_ring_with_star);
        seek_progressbar.setVisibility(View.INVISIBLE);
    }

    private void animation_scale_ring(final View view, final float animationSizeFactors[], final long animationDurations[], long startTime) {
        final Animation[] animations = new Animation[animationDurations.length];
        for (int i = 0; i < animations.length; i++) {
            animations[i] = new ScaleAnimation(animationSizeFactors[i], animationSizeFactors[i + 1], animationSizeFactors[i], animationSizeFactors[i + 1],
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
            animations[i].setDuration(animationDurations[i]);
            animations[i].setInterpolator(new LinearInterpolator());
            animations[i].setFillAfter(true);
            if (i == 0) {
                animations[0].setStartOffset(startTime);
            }
        }

        for (int i = 0; i < animations.length; i++) {
            final int index = i;
            animations[i].setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (index + 1 < animations.length) {
                        view.startAnimation(animations[index + 1]);
                    }
                    view.setVisibility(View.VISIBLE);
                }
            });
            view.startAnimation(animations[0]);
        }
    }

    public void dismiss() {
        L.l("dismiss");
        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1, 0, 1, 0, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        animationSet = new AnimationSet(true);
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(scaleAnimation);
        animationSet.setDuration(300);
        alertRoot.startAnimation(animationSet);
        animationSet.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                L.l("onAnimationStart");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                L.l("onAnimationEnd");

                HSAnalytics.logEvent("Boost_Window_Dismissed");
                GAnalyticsUtils.logEvent("Boost", "Boost_Window_Dismissed");
                animationSet = null;
                FloatWindowManager.getInstance().removeBoostWindow();
                if (isCleanExpired) {
                    HSBoostManager.getInstance().startBoostedAnimation();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void cleanAnimation() {
        if (animation_set_app_name_one != null) {
            animation_set_app_name_one.cancel();
        }
        if (animation_set_app_name_three != null) {
            animation_set_app_name_three.cancel();
        }
        if (alpha_anim_app_name_one != null) {
            alpha_anim_app_name_one.cancel();
        }
        if (alpha_anim_app_name_three != null) {
            alpha_anim_app_name_three.cancel();
        }
        if (translate_animation_set_app_name != null) {
            translate_animation_set_app_name.cancel();
        }
        if (translate_anim_app_name_one != null) {
            translate_anim_app_name_one.cancel();
        }
        if (translate_anim_app_name_three != null) {
            translate_anim_app_name_three.cancel();
        }
        if (alpha_anim_img_half_ring_with_star != null) {
            alpha_anim_img_half_ring_with_star.cancel();
        }
        if (animation_boost_done != null) {
            animation_boost_done.cancel();
        }
        if (alpha_anim_ring_full_appear != null) {
            alpha_anim_ring_full_appear.cancel();
        }
        if (img_boosting_done_scale_animation != null) {
            img_boosting_done_scale_animation.cancel();
        }
        if (img_boosting_done_animation != null) {
            img_boosting_done_animation.cancel();
        }
        if (rotate_anim_img_half_ring_with_star != null) {
            rotate_anim_img_half_ring_with_star.cancel();
        }
        if (alpha_anim_txt_percent != null) {
            alpha_anim_txt_percent.cancel();
        }
        if (alpha_anim_highlight_bg_appear != null) {
            alpha_anim_highlight_bg_appear.cancel();
        }
        if (alpha_anim_txt_percent != null) {
            alpha_anim_txt_percent.cancel();
        }
        if (scale_anima_txt_percent != null) {
            scale_anima_txt_percent.cancel();
        }
        if (animset_txt_percent_appear != null) {
            animset_txt_percent_appear.cancel();
        }
        if (value_anim_seek_progress != null) {
            value_anim_seek_progress.cancel();
            value_anim_seek_progress.removeAllUpdateListeners();
        }
        if (value_anim_app_count != null) {
            value_anim_app_count.cancel();
            value_anim_app_count.removeAllUpdateListeners();
        }
        if (color_anim_clean_bg != null) {
            color_anim_clean_bg.cancel();
            color_anim_clean_bg.removeAllUpdateListeners();
        }
        if (txt_boost_indicator_animation != null) {
            txt_boost_indicator_animation.cancel();
        }
        if (txt_boost_indicator_done_animation != null) {
            txt_boost_indicator_done_animation.cancel();
        }
        L.l("animationset cancel");
        if (animationSet != null) {
            animationSet.cancel();
        }
    }

    /**
     * 将小悬浮窗的参数传入，用于更新小悬浮窗的位置。
     *
     * @param params 小悬浮窗的参数
     */
    public void setParams(WindowManager.LayoutParams params) {
        mParams = params;
        setLayoutParams(mParams);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_MENU:
                // 处理自己的逻辑break;
                HSAnalytics.logEvent("Boost_Back_clicked");
                GAnalyticsUtils.logEvent("Boost", "Boost_Back_clicked");
            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }
}
