package com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate;

import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.ThemeHomeModel;
import com.ihs.inputmethod.uimodules.utils.ViewConvertor;
import com.ihs.keyboardutils.nativeads.NativeAdParams;
import com.ihs.keyboardutils.nativeads.NativeAdView;

import java.util.List;

/**
 * Created by ihandysoft on 17/3/11.
 */
public final class ThemeSmallAdAdapterDelegate extends ThemeAdAdapterDelegate {

    public ThemeSmallAdAdapterDelegate() {
        super(null);
    }

    @Override
    protected boolean isForViewType(@NonNull List<ThemeHomeModel> items, int position) {
        return items.get(position).span == 1 && items.get(position).isAd;
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
//        width = (parent.getMeasuredWidth() - parent.getPaddingLeft() - parent.getPaddingRight()) / 2 + HSDisplayUtils.dip2px(4);

        width = (parent.getMeasuredWidth() - parent.getPaddingLeft() - parent.getPaddingRight()) / 2 - HSDisplayUtils.dip2px(4) * 2 ;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            width += -HSDisplayUtils.dip2px(2) * 2;
        }

        CardView cardView = ViewConvertor.getCardView();
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) cardView.getLayoutParams();
        int margin = HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.theme_card_recycler_view_card_margin);
        layoutParams.setMargins(margin, margin, margin, margin);
        cardView.setLayoutParams(layoutParams);
        cardView.setCardBackgroundColor(Color.WHITE);
        HSLog.e("CardWidth:" + cardView.getWidth());
        return new ThemeAdViewHolder(cardView);
    }

    @Override
    protected void onBindViewHolder(@NonNull List<ThemeHomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        CardView cardView = (CardView) holder.itemView;
        String nativeAd = getNativeAd(position);
        if (nativeAdViewCached.get(nativeAd) == null) {
            View view = LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ad_style_5, null);
            LinearLayout loadingView = (LinearLayout) LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ad_loading_3, null);
            LinearLayout.LayoutParams loadingLP = new LinearLayout.LayoutParams(width, -1);
            loadingView.setLayoutParams(loadingLP);
            loadingView.setGravity(Gravity.CENTER);
            NativeAdView nativeAdView = new NativeAdView(HSApplication.getContext(), view, loadingView);
            NativeAdParams nativeAdParams = new NativeAdParams(nativeAd, width, 1.6f);
            nativeAdParams.setScaleType(ImageView.ScaleType.CENTER_CROP);
            nativeAdView.configParams(nativeAdParams);
            cardView.addView(nativeAdView);

            nativeAdViewCached.put(nativeAd, nativeAdView);
        } else {
            ViewGroup parent = ((ViewGroup) nativeAdViewCached.get(nativeAd).getParent());
            if (parent != null) {
                parent.removeView(nativeAdViewCached.get(nativeAd));
            }
            cardView.addView(nativeAdViewCached.get(nativeAd));
        }
    }

    @Override
    public int getSpanSize(List<ThemeHomeModel> items, int position) {
        return 1;
    }
}