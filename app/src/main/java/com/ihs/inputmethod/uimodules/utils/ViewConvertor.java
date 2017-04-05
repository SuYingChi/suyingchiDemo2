package com.ihs.inputmethod.uimodules.utils;

import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;

/**
 * Created by ihandysoft on 16/9/28.
 */
public class ViewConvertor {

    public static CardView toCardView(View view) {
        int temp = HSDisplayUtils.dip2px(HSApplication.getContext(), 2);
        CardView cardView = new CardView(HSApplication.getContext());
        cardView.setRadius(temp);
        cardView.setCardElevation(temp);
        cardView.setMaxCardElevation(temp);
        cardView.setPreventCornerOverlap(false);
        cardView.setContentPadding(0, 0 , 0, 0);
        FrameLayout.LayoutParams layoutParams = new CardView.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        cardView.setLayoutParams(layoutParams);
        cardView.addView(view);
        return cardView;
    }

    public static CardView getCardView() {
        int temp = HSDisplayUtils.dip2px(HSApplication.getContext(), 2);
        CardView cardView = new CardView(HSApplication.getContext());
        cardView.setRadius(temp);
        cardView.setCardElevation(temp);
        cardView.setMaxCardElevation(temp);
        cardView.setPreventCornerOverlap(false);
        cardView.setContentPadding(0, 0 , 0, 0);
        FrameLayout.LayoutParams layoutParams = new CardView.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        cardView.setLayoutParams(layoutParams);
        return cardView;
    }
}
