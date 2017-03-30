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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
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
    ImageView loadingView;

    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(
            @NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View view = inflater.inflate(R.layout.ct_item_ads, parent, false);
        return new ViewHolder(view);
    }


    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder holder, @NonNull AdsItem adsItem) {
        holder.adsContainer.removeAllViews();
        Context context = holder.itemView.getContext();
        View view = LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ad_style_6, null);
        loadingView = new ImageView(context);
        loadingView.setImageResource(R.drawable.custom_theme_ad);
        loadingView.setScaleType(ImageView.ScaleType.CENTER_CROP);



        final NativeAdView nativeAdView = new NativeAdView(context, view, loadingView);
        final NativeAdParams nativeAdParams = new NativeAdParams(context.getString(R.string.ad_placement_customize_theme));
        nativeAdParams.setScaleType(ImageView.ScaleType.FIT_XY);
        nativeAdView.configParams(nativeAdParams);
        nativeAdView.setNativeAdType(NativeAdView.NativeAdType.ICON);

        loadingView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                doAnimation(nativeAdView);
            }
        });

        nativeAdView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                doAnimation(nativeAdView);
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
        holder.adsContainer.setLayoutParams(layoutParams);
        holder.adsContainer.addView(nativeAdView);
        holder.adsContainer.setCircle(adsItem.isCircleStyle);
        holder.setIsRecyclable(false);
    }

    private void doAnimation(NativeAdView nativeAdView) {
        ScaleAnimation animation = new ScaleAnimation(0.9f,1.2f,0.9f,1.2f, Animation.RELATIVE_TO_SELF,.5f,Animation.RELATIVE_TO_SELF,.5f);
        animation.setDuration(1000);
        animation.setRepeatCount(2);
        nativeAdView.startAnimation(animation);
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        RoundedCornerLayout adsContainer;


        ViewHolder(@NonNull View itemView) {
            super(itemView);
            adsContainer = (RoundedCornerLayout) itemView.findViewById(R.id.adsContainer);
        }
    }


}
