package com.ihs.inputmethod.feature.boost.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.honeycomb.launcher.R;
import com.honeycomb.launcher.boost.BoostConditionManager;
import com.honeycomb.launcher.boost.BoostSource;
import com.honeycomb.launcher.boost.BoostType;
import com.honeycomb.launcher.boost.RamUsageDisplayUpdater;
import com.honeycomb.launcher.dialog.LauncherTipManager;
import com.honeycomb.launcher.util.CommonUtils;
import com.ihs.commons.utils.HSLog;

import java.util.Random;

public class BlackHoleLayout extends RelativeLayout {

    public interface BlackHoleAnimationListener {
        void onEnd();
    }

    private static final int FRAME_TIME = 40;

    // Circle
    private static final int DURATION_CIRCLE_ALPHA_ADD = 28 * FRAME_TIME;
    private static final int DURATION_CIRCLE_MIDDLE_ALPHA_START_OFF = 8 * FRAME_TIME;
    private static final int DURATION_CIRCLE_OUT_ALPHA_START_OFF = 16 * FRAME_TIME;
    private static final int DURATION_ROTATE_MAIN = 110 * FRAME_TIME;

    private View background;
    private View container;

    private ImageView vortex;
    private ImageView circleInIv;
    private ImageView circleMiddleIv;
    private ImageView circleOutIv;
    private ImageView boostCenterIv;
    private TextView tvMemory;
    private RelativeLayout boostIconContainer;

    private int mBeforeBoostRamUsage;
    private int mAfterBoostRamUsage;

    private BoostType mBoostType = BoostType.RAM;
    private BoostSource mBoostSource = BoostSource.BoostActivity;

    private BlackHoleAnimationListener listener;

    public BlackHoleLayout(Context context) {
        this(context, null);
    }

    public BlackHoleLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BlackHoleLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(getContext()).inflate(R.layout.boost_black_hole, this, true);

        background = findViewById(R.id.background);
        container = findViewById(R.id.black_hole_container);

        vortex = (ImageView) findViewById(R.id.center_vortex);
        circleInIv = (ImageView) findViewById(R.id.circle_in_iv);
        circleMiddleIv = (ImageView) findViewById(R.id.circle_middle_iv);
        circleOutIv = (ImageView) findViewById(R.id.circle_out_iv);
        boostCenterIv = (ImageView) findViewById(R.id.boost_center_iv);
        boostIconContainer = (RelativeLayout) findViewById(R.id.boost_icon);
        tvMemory = (TextView) findViewById(R.id.txt_ball_memory);

        mBeforeBoostRamUsage = RamUsageDisplayUpdater.getInstance().getDisplayedRamUsage();
        tvMemory.setText(String.valueOf(mBeforeBoostRamUsage));
    }

    public void setBoostType(BoostType type){
        mBoostType = type;
    }

    public void setBoostSource(BoostSource source) {
        mBoostSource = source;
    }

    public void startAnimation() {
        // Ram usage text
        mAfterBoostRamUsage = RamUsageDisplayUpdater.getInstance().startBoost();
        if (mAfterBoostRamUsage > 0 && mAfterBoostRamUsage < mBeforeBoostRamUsage) {
            ValueAnimator ramUsageAnimation = ValueAnimator.ofInt(mBeforeBoostRamUsage, mAfterBoostRamUsage);
            ramUsageAnimation.setDuration(DURATION_ROTATE_MAIN);
            ramUsageAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
            ramUsageAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    tvMemory.setText(String.valueOf(value));
                }
            });
            ramUsageAnimation.start();
        } else {
            mAfterBoostRamUsage = mBeforeBoostRamUsage;
        }

        // icons (copied from Super Security)
        startImgCurveAnimation();

        // background
        ObjectAnimator backgroundAppearAnimation = ObjectAnimator.ofFloat(background, View.ALPHA, 1);
        backgroundAppearAnimation.setDuration(15 * FRAME_TIME);
        backgroundAppearAnimation.start();

        //container
        container.setScaleX(0);
        container.setScaleY(0);
        container.setVisibility(View.VISIBLE);
        ObjectAnimator containerAppearAnimation = ObjectAnimator.ofPropertyValuesHolder(container,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1));
        containerAppearAnimation.setInterpolator(new OvershootInterpolator(2));
        containerAppearAnimation.setDuration(12 * FRAME_TIME);
        containerAppearAnimation.start();
        ObjectAnimator containerDisappearAnimation = ObjectAnimator.ofPropertyValuesHolder(container,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0));
        containerDisappearAnimation.setDuration(8 * FRAME_TIME);
        containerDisappearAnimation.setStartDelay(DURATION_ROTATE_MAIN - 8 * FRAME_TIME);
        containerDisappearAnimation.start();

        // vortex
        Animation vortexInAlphaAppearAnimation = BoostAnimationManager.getAlphaAppearAnimation(DURATION_CIRCLE_ALPHA_ADD, 0);
        Animation vortexRotate = BoostAnimationManager.getRotateAnimation(DURATION_ROTATE_MAIN + 5 * FRAME_TIME, 1000);
        BoostAnimationManager.startSetAnimation(vortex, vortexInAlphaAppearAnimation, vortexRotate);

        // circle
        Animation circleInAlphaAppearAnimation = BoostAnimationManager.getAlphaAppearAnimation(DURATION_CIRCLE_ALPHA_ADD, 0);
        Animation circleInRotate = BoostAnimationManager.getBoostRotateAnimation(DURATION_ROTATE_MAIN);
        BoostAnimationManager.startSetAnimation(circleInIv, circleInAlphaAppearAnimation, circleInRotate);

        Animation circleMiddleAlphaAppearAnimation = BoostAnimationManager.getAlphaAppearAnimation(DURATION_CIRCLE_ALPHA_ADD, DURATION_CIRCLE_MIDDLE_ALPHA_START_OFF);
        Animation circleMiddleRotate = BoostAnimationManager.getBoostRotateAnimation(DURATION_ROTATE_MAIN);
        BoostAnimationManager.startSetAnimation(circleMiddleIv, circleMiddleAlphaAppearAnimation, circleMiddleRotate);

        Animation circleOutAlphaAppearAnimation = BoostAnimationManager.getAlphaAppearAnimation(DURATION_CIRCLE_ALPHA_ADD, DURATION_CIRCLE_OUT_ALPHA_START_OFF);
        Animation circleOutRotate = BoostAnimationManager.getBoostRotateAnimation(DURATION_ROTATE_MAIN);
        BoostAnimationManager.startSetAnimation(circleOutIv, circleOutAlphaAppearAnimation, circleOutRotate);

        // dots
        for (int i = 0; i < 15; i++) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    startDotAnimation();
                }
            }, (i + 1) * 5 * FRAME_TIME);
        }

        // show tip and dismiss view
        postDelayed(new Runnable() {
            @Override
            public void run() {
                showResultTip(mAfterBoostRamUsage);

                ObjectAnimator selfDisappearAnimation = ObjectAnimator.ofFloat(BlackHoleLayout.this, View.ALPHA, 0);
                selfDisappearAnimation.setDuration(10 * FRAME_TIME);
                selfDisappearAnimation.start();
                selfDisappearAnimation.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (listener != null) {
                            listener.onEnd();
                        }
                    }
                });
            }
        }, DURATION_ROTATE_MAIN);
    }

    private void showResultTip(int afterBoostRamUsage) {
        HSLog.d("BoostActivity", "After boost RAM usage: " + afterBoostRamUsage);
        final int percentageBoosted = mBeforeBoostRamUsage - afterBoostRamUsage;

        LauncherTipManager.getInstance().showFinishBoostAlert(getContext(), mBoostSource, mBoostType, percentageBoosted);
        BoostConditionManager.getInstance().reportBoostDone(percentageBoosted);
    }

    private void startImgCurveAnimation() {
        int[] location = new int[2];
        boostCenterIv.getLocationOnScreen(location);
        float endX = location[0];
        float endY = location[1];
        BoostAnimationManager boostAnimationManager = new BoostAnimationManager(endX, endY);

        ImageView iconOneV = (ImageView) findViewById(R.id.boost_icon_1_iv);
        ImageView iconTwoV = (ImageView) findViewById(R.id.boost_icon_2_iv);
        ImageView iconThreeV = (ImageView) findViewById(R.id.boost_icon_3_iv);
        ImageView iconFourV = (ImageView) findViewById(R.id.boost_icon_4_iv);
        ImageView iconFiveV = (ImageView) findViewById(R.id.boost_icon_5_iv);
        ImageView iconSixV = (ImageView) findViewById(R.id.boost_icon_6_iv);
        ImageView iconSevenV = (ImageView) findViewById(R.id.boost_icon_7_iv);

        Drawable[] drawables = boostAnimationManager.getBoostAppIconDrawables(getContext());
        if (drawables.length == 0) {
            return;
        }

        if (BoostAnimationManager.Boost.ICON_ONE < drawables.length) {
            iconOneV.setImageDrawable(drawables[BoostAnimationManager.Boost.ICON_ONE]);
            boostAnimationManager.startIconAnimation(iconOneV, BoostAnimationManager.Boost.ICON_ONE);
        }
        if (BoostAnimationManager.Boost.ICON_TWO < drawables.length) {
            iconTwoV.setImageDrawable(drawables[BoostAnimationManager.Boost.ICON_TWO]);
            boostAnimationManager.startIconAnimation(iconTwoV, BoostAnimationManager.Boost.ICON_TWO);
        }
        if (BoostAnimationManager.Boost.ICON_THREE < drawables.length) {
            iconThreeV.setImageDrawable(drawables[BoostAnimationManager.Boost.ICON_THREE]);
            boostAnimationManager.startIconAnimation(iconThreeV, BoostAnimationManager.Boost.ICON_THREE);
        }
        if (BoostAnimationManager.Boost.ICON_FOUR < drawables.length) {
            iconFourV.setImageDrawable(drawables[BoostAnimationManager.Boost.ICON_FOUR]);
            boostAnimationManager.startIconAnimation(iconFourV, BoostAnimationManager.Boost.ICON_FOUR);
        }
        if (BoostAnimationManager.Boost.ICON_FIVE < drawables.length) {
            iconFiveV.setImageDrawable(drawables[BoostAnimationManager.Boost.ICON_FIVE]);
            boostAnimationManager.startIconAnimation(iconFiveV, BoostAnimationManager.Boost.ICON_FIVE);
        }
        if (BoostAnimationManager.Boost.ICON_SIX < drawables.length) {
            iconSixV.setImageDrawable(drawables[BoostAnimationManager.Boost.ICON_SIX]);
            boostAnimationManager.startIconAnimation(iconSixV, BoostAnimationManager.Boost.ICON_SIX);
        }
        if (BoostAnimationManager.Boost.ICON_SEVEN < drawables.length) {
            iconSevenV.setImageDrawable(drawables[BoostAnimationManager.Boost.ICON_SEVEN]);
            boostAnimationManager.startIconAnimation(iconSevenV, BoostAnimationManager.Boost.ICON_SEVEN);
        }
    }

    private void startDotAnimation() {
        final ImageView dotView = new ImageView(getContext());
        dotView.setImageResource(R.drawable.boost_black_hole_dot);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_LEFT, R.id.dot_anchor);
        params.addRule(RelativeLayout.ALIGN_TOP, R.id.dot_anchor);
        Random random = new Random();
        int radius = random.nextInt(CommonUtils.pxFromDp(50)) + CommonUtils.pxFromDp(100);
        double radians = random.nextDouble() * 2 * Math.PI;
        int leftMargin = (int) (radius * Math.sin(radians));
        int topMargin = (int) (radius * Math.cos(radians));
        params.leftMargin = leftMargin;
        params.topMargin = topMargin;
        boostIconContainer.addView(dotView, params);

        ObjectAnimator dotAnimation = ObjectAnimator.ofPropertyValuesHolder(dotView,
                PropertyValuesHolder.ofFloat(View.ALPHA, 0.2f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0),
                PropertyValuesHolder.ofFloat(View.TRANSLATION_X, -leftMargin),
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, -topMargin));
        dotAnimation.setDuration(8 * FRAME_TIME);
        dotAnimation.setInterpolator(new AccelerateInterpolator(2));
        dotAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                boostIconContainer.removeView(dotView);
            }
        });
        dotAnimation.start();
    }

    public void setBlackHoleAnimationListener(BlackHoleAnimationListener listener) {
        this.listener = listener;
    }
}
