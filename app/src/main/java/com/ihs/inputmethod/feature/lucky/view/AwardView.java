package com.ihs.inputmethod.feature.lucky.view;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.acb.adadapter.AcbNativeAd;
import com.acb.nativeads.AcbNativeAdLoader;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.feature.common.ViewUtils;
import com.ihs.inputmethod.feature.lucky.GameConfig;
import com.ihs.inputmethod.feature.lucky.LuckyActivity;
import com.ihs.inputmethod.feature.lucky.LuckyPreloadManager;
import com.ihs.inputmethod.feature.lucky.MusicPlayer;
import com.ihs.inputmethod.feature.lucky.TargetInfo;
import com.ihs.inputmethod.uimodules.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * Container for popup view that shows award.
 */
public class AwardView extends FrameLayout implements View.OnClickListener {

    private static final String TAG = AwardView.class.getSimpleName();

    private static final String VIEW_TAG = "view_tag_on_ad_container";

    private static final long EMPTY_DELAY_DURATION = 133;
    private static final long NO_NETWORK_DELAY_DURATION = 367;
    private static final long WALLPAPER_DELAY_DURATION = 800;
    private static final long AD_DELAY_DURATION = 733;
    private static final long THEME_DELAY_DURATION = 733;
    private static final long OTHER_VIEW_DELAY_DURATION = 800;

    @SuppressLint("UseSparseArrays")
    private static Map<TargetInfo.Color, Bitmap> sBoxBitmapCache = new HashMap<>(13);
    private static Map<TargetInfo.Color, Bitmap> sCoverBitmapCache = new HashMap<>(13);

    private static Random sRand = new Random();

    private LayoutInflater mInflater;

    private AwardViewSizeAdapter mSizeAdapter;

    private BoxView mBoxView;
    private PrizeView mPrizeView;
    private NothingView mEmptyView;
    private BombView mBombView;
    private NoNetworkView mNoNetwork;
    private ThemeView mThemeView;

    private View mChancesView;
    private Animator mChancesAnimator;
    private ChancesAnimationAdapter mChancesAnimationAdapter;
    private AnimatorSet mAwardAnimators;

    private MusicPlayer mMusicPlayer;

    private AcbNativeAd mAd;
    private int mSmallBoxAdCount;
    private int mCurrentSmallAdCount;
    private int mLargeBoxAdCount;
    private int mCurrentLargeAdCount;

    private LuckyActivity.ViewState mViewState;
    private boolean mShouldRefresh = true;

    public AwardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mInflater = LayoutInflater.from(getContext());
        mSmallBoxAdCount = HSConfig.optInteger(3, "Application", "Lucky", "SmallBoxAdCount");
        mLargeBoxAdCount = HSConfig.optInteger(3, "Application", "Lucky", "LargeBoxAdCount");
    }

    public void setSizeAdapter(AwardViewSizeAdapter adapter) {
        mSizeAdapter = adapter;
    }

    public void setMusicPlayer(MusicPlayer musicPlayer) {
        mMusicPlayer = musicPlayer;
    }


    private BoxView getBoxView() {
        if (mBoxView == null) {
            HSLog.d(TAG, "Inflate ad box container");
            mBoxView = (BoxView) mInflater.inflate(R.layout.lucky_award_box_container, this, false);
        }
        return mBoxView;
    }


    private PrizeView getPrizeView() {
        if (mPrizeView == null) {
            HSLog.d(TAG, "Inflate ad view");
            mPrizeView = (PrizeView) mInflater.inflate(R.layout.lucky_award_ad_view, this, false);
        }
        mPrizeView.setVisibility(VISIBLE);
        return mPrizeView;
    }


    private NothingView getEmptyView() {
        if (mEmptyView == null) {
            mEmptyView = (NothingView) mInflater.inflate(R.layout.lucky_award_nothing, this, false);
        }
        return mEmptyView;
    }



    private ThemeView getThemeView() {
        if (mThemeView == null) {
            mThemeView = (ThemeView) mInflater.inflate(R.layout.lucky_award_theme_view, this, false);
        }
        return mThemeView;
    }


    private NoNetworkView getNetworkErrorView() {
        if (mNoNetwork == null) {
            mNoNetwork = (NoNetworkView) mInflater.inflate(R.layout.lucky_award_no_network, this, false);
        }
        return mNoNetwork;
    }


    private View getBombView() {
        if (mBombView == null) {
            mBombView = (BombView) mInflater.inflate(R.layout.lucky_award_bomb, this, false);
        }
        return mBombView;
    }


    private View getChancesView() {
        if (mChancesView == null) {
            View chancesView = mInflater.inflate(R.layout.lucky_award_chances, this, false);
            mChancesAnimationAdapter = new ChancesAnimationAdapter(chancesView);
            View chancesReceive = ViewUtils.findViewById(chancesView, R.id.lucky_game_award_chances_receive_btn);
            chancesReceive.setOnClickListener(this);

            mChancesView = chancesView;
        }
        return mChancesView;
    }

    public void show(LuckyActivity.ViewState state, TargetInfo.Color color) {
        if (getChildCount() != 0) {
            HSLog.w(TAG, "Cannot show award view as it is already showing: " + getChildAt(0));
            return;
        }

        mSizeAdapter.setVisibility(VISIBLE);
        if (state != LuckyActivity.ViewState.GAME && state != LuckyActivity.ViewState.AWARD_BOMB) {
            getBoxView().setBoxBodyBitmap(getBoxBitmap(getContext(), color, true));
            getBoxView().setBoxCoverBitmap(getBoxBitmap(getContext(), color, false));
        }
        mViewState = state;
        switch (state) {
            case AWARD_BOMB:
                HSAnalytics.logEvent("Lucky_Award_Bomb_Shown");
                getBombView().setVisibility(INVISIBLE);
                addView(getBombView());
                showBomb();
                break;
            case AWARD_LARGE:
            case AWARD_SMALL:
                boolean isSmall = false;
                boolean shouldShowAd;
                if (state == LuckyActivity.ViewState.AWARD_SMALL) {
                    shouldShowAd = mCurrentSmallAdCount < mSmallBoxAdCount;
                    isSmall = true;
                } else {
                    shouldShowAd = mCurrentLargeAdCount < mLargeBoxAdCount;
                }
                if (shouldShowAd) {
                    List<AcbNativeAd> ads = AcbNativeAdLoader.fetch(HSApplication.getContext(), HSApplication.getContext().getString(R.string.ad_placement_lucky), 1);
                    mAd = ads.isEmpty() ? null : ads.get(0);

                    String showed;
                    HashMap<String,String> map = new HashMap<>();
                    if (mAd != null) {
                        showed = "true";
                    } else {
                        showed = "no";
                    }
                    map.put("show",showed);
                    HSAnalytics.logEvent("Lucky_Ad_should_show",map);

                } else {
                    mAd = null;
                    if (isSmall) {
                        mCurrentSmallAdCount = 0;
                    } else {
                        mCurrentLargeAdCount = 0;
                    }
                }

                if (mAd != null) {
                    HSAnalytics.logEvent("Lucky_Award_Ad_Shown");
                    ((LuckyActivity) getContext()).setBoxViewState(LuckyActivity.ViewState.AWARD_AD);
                    ViewGroup boxContainer = getBoxView();
                    PrizeView prizeView = getPrizeView();
                    prizeView.resetVisible();
                    if (isSmall) {
                        mCurrentSmallAdCount++;
                    } else {
                        mCurrentLargeAdCount++;
                    }
                    prizeView.fillAd(mAd);
                    if (prizeView.getParent() == null) {
                        prizeView.setTag(VIEW_TAG);
                        boxContainer.addView(prizeView, 0);
                    }
                    boxContainer.setVisibility(GONE);
                    addView(boxContainer);
                    showBoxWithAd(prizeView.getAdAnimation(mAd));

                    mMusicPlayer.play(getContext(), R.raw.lucky_sound_award_open);
                } else {
                    if (isSmall) {
                        GameConfig config = ((LuckyActivity) getContext()).getGameConfig();
                        if (config == null || sRand.nextFloat() <= config.getSmallBoxChancesProbability()) {
                            HSAnalytics.logEvent("Lucky_Award_Chances_Shown");
                            ((LuckyActivity) getContext()).setBoxViewState(LuckyActivity.ViewState.AWARD_CHANCES);
                            getBoxView().setVisibility(GONE);

                            if (getChancesView().getParent() == null) {
                                getChancesView().setTag(VIEW_TAG);
                                getBoxView().addView(getChancesView(), 0);
                            }
                            getChancesView().setVisibility(INVISIBLE);
                            addView(getBoxView());
                            mChancesAnimator = getChancesAnimation();
                            showChanceAwardView();
                            mMusicPlayer.play(getContext(), R.raw.lucky_sound_award_open);
                        } else {
                            ((LuckyActivity) getContext()).setBoxViewState(LuckyActivity.ViewState.AWARD_NOTHING);
                            showEmpty();
                        }
                        mShouldRefresh = false;
                    } else {
                        if (getThemeView().fetchTheme()) {
                            HSAnalytics.logEvent("Lucky_Award_Theme_Shown");
                            ((LuckyActivity) getContext()).setBoxViewState(LuckyActivity.ViewState.AWARD_THEME);
                            if (getThemeView().getParent() == null) {
                                getThemeView().setTag(VIEW_TAG);
                                getBoxView().addView(getThemeView(), 0);
                            }
                            getThemeView().resetVisible();
                            getBoxView().setVisibility(GONE);
                            addView(getBoxView());
                            showThemeView();
                            mMusicPlayer.play(getContext(), R.raw.lucky_sound_award_open);
                        } else {
                            ((LuckyActivity) getContext()).setBoxViewState(LuckyActivity.ViewState.AWARD_NOTHING);
                            showEmpty();
                            mShouldRefresh = false;
                        }
                    }
                }
                break;
            case NETWORK_ERROR:
                HSAnalytics.logEvent("Lucky_Award_Nonet_Shown");
                getBoxView().setVisibility(GONE);
                if (getNetworkErrorView().getParent() == null) {
                    getNetworkErrorView().setTag(VIEW_TAG);
                    getBoxView().addView(getNetworkErrorView(), 0);
                }
                addView(getBoxView());
                showNetworkError();
                break;
            case GAME:
                HSLog.w(TAG, "ViewState.GAME is not handled by AwardView");
                break;
        }

        ((LuckyActivity) getContext()).requestAds();
        mMusicPlayer.pauseBackground();
    }

    public void hide() {
        if (getChildCount() == 0) {
            HSLog.w(TAG, "Cannot hide award view as it is not showing");
        }

        resetViews();

        if (mAd != null) {
            mAd.release();
            mAd = null;
        }

        if (mShouldRefresh) {

            //// TODO: 17/5/20 launcher 小礼物刷新墙纸
//            if (mViewState == LuckyActivity.ViewState.AWARD_SMALL) {
//                LuckyPreloadManager.getInstance().refreshWallpaper(true);
//            } else if (mViewState == LuckyActivity.ViewState.AWARD_LARGE) {
//                LuckyPreloadManager.getInstance().refreshTheme(true);
//            }

            LuckyPreloadManager.getInstance().refreshTheme(true);
        }
        mShouldRefresh = true;

        if (mAwardAnimators != null && mAwardAnimators.isRunning()) {
            mAwardAnimators.end();
        }
        if (mChancesAnimator != null && mChancesAnimator.isRunning()) {
            mChancesAnimator.end();
        }

        mSizeAdapter.setVisibility(GONE);

        mMusicPlayer.resumeBackground();
    }

    private void resetViews() {
        for (int i = 0; i < getBoxView().getChildCount(); i++) {
            View child = getBoxView().getChildAt(i);
            if (child.getTag() != null && child.getTag().equals(VIEW_TAG)) {
                getBoxView().removeView(child);
            }
        }

        if (mPrizeView != null) {
            mPrizeView.resetVisible();
        }

        removeAllViews();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lucky_game_award_chances_receive_btn:
                ((LuckyActivity) getContext()).hideAwardView("Receive");
                break;
        }
    }

    private void showEmpty() {
        HSAnalytics.logEvent("Lucky_Award_Nothing_Shown");
        if (getEmptyView().getParent() == null) {
            getEmptyView().setTag(VIEW_TAG);
            getBoxView().addView(getEmptyView(), 0);
        }
        getBoxView().setVisibility(GONE);
        addView(getBoxView());
        showNothingView();
    }

    private void showBoxWithAd(Animator ad) {
        if (mAwardAnimators != null && mAwardAnimators.isRunning()) {
            mAwardAnimators.end();
            getBoxView().setVisibility(GONE);
        }

        AnimatorSet box = getBoxView().getBoxAnimation();

        mAwardAnimators = new AnimatorSet();
        if (ad != null) {
            ad.setStartDelay(AD_DELAY_DURATION);
            mAwardAnimators.playTogether(box, ad);
        } else {
            mAwardAnimators.play(box);
        }

        mAwardAnimators.start();
    }

    private void showChanceAwardView() {
        if (mAwardAnimators != null && mAwardAnimators.isRunning()) {
            mAwardAnimators.end();
            getBoxView().setVisibility(GONE);
        }

        mChancesAnimator.setStartDelay(OTHER_VIEW_DELAY_DURATION);
        mChancesAnimator.addListener(new com.ihs.inputmethod.feature.common.AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                getChancesView().setVisibility(VISIBLE);
            }
        });

        mAwardAnimators = new AnimatorSet();
        mAwardAnimators.playTogether(getBoxView().getBoxAnimation(), mChancesAnimator);

        mAwardAnimators.start();
    }


    private void showThemeView() {
        if (mAwardAnimators != null && mAwardAnimators.isRunning()) {
            mAwardAnimators.end();
            getBoxView().setVisibility(GONE);
        }

        AnimatorSet theme = getThemeView().getThemeAnimation();
        theme.setStartDelay(THEME_DELAY_DURATION);
        theme.addListener(new com.ihs.inputmethod.feature.common.AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
            }
        });

        mAwardAnimators = new AnimatorSet();
        mAwardAnimators.playTogether(getBoxView().getBoxAnimation(), theme);
        mAwardAnimators.start();
    }

    private void showNetworkError() {
        if (mAwardAnimators != null && mAwardAnimators.isRunning()) {
            mAwardAnimators.end();
            mBoxView.setVisibility(GONE);
        }

        AnimatorSet box = getBoxView().getBoxAnimation();

        AnimatorSet networkError = mNoNetwork.getNoNetworkAnimation();
        networkError.setStartDelay(NO_NETWORK_DELAY_DURATION);

        mAwardAnimators = new AnimatorSet();
        mAwardAnimators.playTogether(box, networkError);
        mAwardAnimators.start();
    }

    private void showBomb() {
        if (mAwardAnimators != null && mAwardAnimators.isRunning()) {
            mAwardAnimators.end();
        }

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(this, "alpha", 0.0f, 1.0f);
        fadeIn.setDuration(100);

        ObjectAnimator holdOn = ObjectAnimator.ofFloat(this, "alpha", 1.0f, 1.0f);
        holdOn.setDuration(167);

        holdOn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                getBombView().setVisibility(VISIBLE);
            }
        });

        mAwardAnimators = new AnimatorSet();
        mAwardAnimators.playSequentially(fadeIn, holdOn, mBombView.getBombAnimation());
        mAwardAnimators.start();
    }

    private void showNothingView() {
        if (mAwardAnimators != null && mAwardAnimators.isRunning()) {
            mAwardAnimators.end();
            getBoxView().setVisibility(GONE);
        }

        AnimatorSet box = getBoxView().getBoxAnimation();

        AnimatorSet empty = getEmptyView().getEmptyAnimation();
        empty.setStartDelay(EMPTY_DELAY_DURATION);

        mAwardAnimators = new AnimatorSet();
        mAwardAnimators.playTogether(box, empty);
        mAwardAnimators.start();
    }

    private AnimatorSet getChancesAnimation() {
        AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.lucky_award_chances_open);
        set.setTarget(mChancesAnimationAdapter);
        set.addListener(mChancesAnimationAdapter);
        return set;
    }

    private static Bitmap getBoxBitmap(Context context, TargetInfo.Color color, boolean isBody) {
        Bitmap bitmap;
        if (isBody) {
            bitmap = sBoxBitmapCache.get(color);
        } else {
            bitmap = sCoverBitmapCache.get(color);
        }

        if (bitmap == null) {
            Resources res = context.getResources();
            @DrawableRes int resId = R.drawable.lucky_award_golden_box_body;
            switch (color) {
                case GOLDEN:
                    resId = isBody ? R.drawable.lucky_award_golden_box_body : R.drawable.lucky_award_golden_box_cover;
                    break;
                case RED:
                    resId = isBody ? R.drawable.lucky_award_red_box_body : R.drawable.lucky_award_red_box_cover;
                    break;
                case GREEN:
                    resId = isBody ? R.drawable.lucky_award_green_box_body : R.drawable.lucky_award_green_box_cover;
                    break;
            }
            bitmap = BitmapFactory.decodeResource(res, resId);
            if (isBody) {
                sBoxBitmapCache.put(color, bitmap);
            } else {
                sCoverBitmapCache.put(color, bitmap);
            }
        }
        return bitmap;
    }


    public static void release() {
        // These bitmaps are obtained by BitmapFactory.decodeResource(), for which the framework
        // maintains a resource cache and the bitmaps created are held and reused. We shall NOT
        // RECYCLE any of these bitmaps here when we don't need them.
        sBoxBitmapCache.clear();
        sCoverBitmapCache.clear();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    public boolean isShowing() {
        return mSizeAdapter != null && (mSizeAdapter.getVisibility() == View.VISIBLE);
    }
}
