/*
 * Copyright 2016 drakeet. https://github.com/drakeet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.ads;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base.BaseThemeFragment;
import com.ihs.inputmethod.uimodules.widget.RoundedCornerLayout;
import com.ihs.keyboardutils.nativeads.NativeAdParams;
import com.ihs.keyboardutils.nativeads.NativeAdView;

import me.drakeet.multitype.ItemViewProvider;


/**
 * @author drakeet
 */
public class AdsProvider extends ItemViewProvider<AdsItem, AdsProvider.ViewHolder> {

    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(
            @NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View view = inflater.inflate(R.layout.ct_item_ads, parent, false);
        return new ViewHolder(view);
    }


    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder holder, @NonNull AdsItem adsItem) {
        holder.layoutContainer.removeAllViews();
        Context context = holder.itemView.getContext();
        View view = LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ad_style_6, null);
        RoundedCornerLayout roundedCornerLayout = (RoundedCornerLayout) view.findViewById(R.id.adsContainer);
        RoundedCornerLayout loadingLayout = (RoundedCornerLayout) LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ct_item_loading, null);
        loadingLayout.setCircle(adsItem.isCircleStyle);


        final NativeAdView nativeAdView = new NativeAdView(HSApplication.getContext(), view, loadingLayout);
        final NativeAdParams nativeAdParams = new NativeAdParams(context.getString(R.string.ad_placement_customize_theme));
        nativeAdView.configParams(nativeAdParams);
        nativeAdParams.setScaleType(ImageView.ScaleType.CENTER_INSIDE);


        loadingLayout.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                if (nativeAdView.getNativeAdContainerView() != null && nativeAdView.getNativeAdContainerView().getAdIconView() != null) {
                    nativeAdView.getNativeAdContainerView().getAdIconView().setImageViewScaleType(ImageView.ScaleType.FIT_CENTER);
                }
                doAnimation(holder.layoutContainer);
            }
        });

        nativeAdView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                if (nativeAdView.getNativeAdContainerView() != null) {
                    if (nativeAdView.getNativeAdContainerView().getAdIconView() != null) {
                        nativeAdView.getNativeAdContainerView().getAdIconView().setImageViewScaleType(ImageView.ScaleType.FIT_CENTER);
                    }
                }
                doAnimation(holder.layoutContainer);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
            }
        });
        DisplayMetrics displayMetrics = holder.itemView.getResources().getDisplayMetrics();
        double standardSize = 0.85 * Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels) / BaseThemeFragment.SPAN_COUNT;
        int width = (int) (standardSize * adsItem.widthRatio);
        int height = (int) (standardSize * adsItem.heightRatio);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, height);
        layoutParams.gravity = Gravity.CENTER;
        nativeAdView.setLayoutParams(layoutParams);
        holder.layoutContainer.addView(nativeAdView);
        roundedCornerLayout.setCircle(adsItem.isCircleStyle);
        holder.setIsRecyclable(false);
    }

    private void doAnimation(View view) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator xAnimator = ObjectAnimator.ofFloat(view,"scaleX",1,1.2f,1,1.2f,1);
        ObjectAnimator yAnimator = ObjectAnimator.ofFloat(view,"scaleY",1,1.2f,1,1.2f,1);
        animatorSet.playTogether(xAnimator,yAnimator);
        animatorSet.setDuration(900);
        animatorSet.start();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout layoutContainer;


        ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutContainer = (FrameLayout) itemView.findViewById(R.id.layoutContainer);
        }
    }


}
