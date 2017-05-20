package com.ihs.inputmethod.feature.lucky.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.acb.adadapter.AcbNativeAd;
import com.acb.adadapter.ContainerView.AcbNativeAdContainerView;
import com.acb.adadapter.ContainerView.AcbNativeAdPrimaryView;
import com.ihs.inputmethod.feature.common.AnimatorListenerAdapter;
import com.ihs.inputmethod.feature.common.ViewUtils;
import com.ihs.inputmethod.feature.lucky.LuckyActivity;
import com.ihs.inputmethod.uimodules.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;


public class PrizeView extends FlyAwardBaseView {

    private View mContainer;
    private TextView mFoundApp;
    private AcbNativeAdContainerView mAdContentView;


    public PrizeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mDragIcon = ViewUtils.findViewById(this, R.id.lucky_game_drag_ad_icon);
        mFoundApp = ViewUtils.findViewById(this, R.id.lucky_game_ad_found_text);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        mContainer = inflater.inflate(R.layout.lucky_award_ad_container_hs, this, false);
        mAdContentView = new AcbNativeAdContainerView(getContext());
        mAdContentView.addContentView(mContainer);

        mIcon = ViewUtils.findViewById(mContainer, R.id.lucky_game_ad_icon);
        mAdContentView.setAdIconView(mIcon);

        TextView title = ViewUtils.findViewById(mContainer, R.id.lucky_game_ad_title);
        mAdContentView.setAdTitleView(title);

        TextView body = ViewUtils.findViewById(mContainer, R.id.lucky_game_ad_body);
        body.setAlpha(0.5f);
        mAdContentView.setAdBodyView(body);

        AcbNativeAdPrimaryView bigImage = ViewUtils.findViewById(mContainer, R.id.lucky_game_ad_image_container);
        bigImage.setImageViewScaleType(ImageView.ScaleType.CENTER_CROP);
        mAdContentView.setAdPrimaryView(bigImage);

        View action = ViewUtils.findViewById(mContainer, R.id.lucky_game_ad_action);
        mAdContentView.setAdActionView(action);

        FrameLayout choice = ViewUtils.findViewById(mContainer, R.id.lucky_game_ad_choice);
        mAdContentView.setAdChoiceView(choice);


        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.lucky_game_ad_found_text);
        addView(mAdContentView, 0, layoutParams);

        calculateAnimationDistance(PrizeView.this);
    }

    public AnimatorSet getAdAnimation(final Object ad) {
        ObjectAnimator holdOn = ObjectAnimator.ofFloat(mDragIcon, "alpha", 1.0f, 1.0f);
        holdOn.setDuration(QUESTION_MARK_HOLD_ON_DURATION);
        holdOn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                AcbNativeAd nativeAd = (AcbNativeAd) ad;
                String path = nativeAd.getResourceFilePath(AcbNativeAd.LOAD_RESOURCE_TYPE_ICON);
                File file = new File(path);
                if (file.exists()) {
                    ImageLoader.getInstance().displayImage(Uri.fromFile(file).toString(), mDragIcon);
                } else {
                    ImageLoader.getInstance().displayImage(nativeAd.getIconUrl(), mDragIcon);
                }
            }
        });

        ObjectAnimator fadeIn = fadeIn(mContainer);
        fadeIn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAdContentView.setVisibility(VISIBLE);

                mFoundApp.setVisibility(VISIBLE);
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.playSequentially(dragUp(), holdOn, flipToIcon(), fadeIn);

        return set;
    }

    public void fillAd(Object ad) {
        AcbNativeAd nativeAd = (AcbNativeAd) ad;
        mAdContentView.fillNativeAd(nativeAd);
        nativeAd.setNativeClickListener((LuckyActivity) getContext());
    }

    public void resetVisible() {
        setVisibility(VISIBLE);
        mAdContentView.setVisibility(INVISIBLE);
        mFoundApp.setVisibility(INVISIBLE);
    }
}
