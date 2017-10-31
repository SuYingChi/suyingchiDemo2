package com.ihs.inputmethod.uimodules.stickerplus;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;

import static com.ihs.inputmethod.uimodules.utils.RippleDrawableUtils.getTransparentRippleBackground;

/**
 * Created by fanxu.kong on 17/9/7.
 */

public class PlusButton extends FrameLayout {
    private View newTipView;
    private final static int FUNCTION_VIEW_REAL_WIDTH = 18;

    public PlusButton(Context context) {
        this(context, null);
    }

    public PlusButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlusButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setBackgroundDrawable(getTransparentRippleBackground());

        final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(HSDisplayUtils.dip2px(22), HSDisplayUtils.dip2px(22));
        lp.gravity = Gravity.CENTER;
        lp.leftMargin = HSDisplayUtils.dip2px(10);
        lp.rightMargin = HSDisplayUtils.dip2px(10);

        AppCompatImageView plusImage = new AppCompatImageView(getContext());
        plusImage.setLayoutParams(lp);
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.common_tab_plus);
        if (!HSKeyboardThemeManager.getCurrentTheme().isDarkBg()) {
            DrawableCompat.setTint(drawable, Color.parseColor("#37474f"));
        }
        plusImage.setImageDrawable(drawable);
        plusImage.setAlpha(0.8f);
        addView(plusImage);
    }


    public void showNewTip() {
        if (newTipView == null) {
            newTipView = new View(HSApplication.getContext());
            GradientDrawable redPointDrawable = new GradientDrawable();
            redPointDrawable.setColor(Color.RED);
            redPointDrawable.setShape(GradientDrawable.OVAL);
            newTipView.setBackgroundDrawable(redPointDrawable);

            int width = HSDisplayUtils.dip2px(5);
            int height = HSDisplayUtils.dip2px(5);
            LayoutParams layoutParams = new LayoutParams(width, height);
            int leftMargin = HSDisplayUtils.dip2px(FUNCTION_VIEW_REAL_WIDTH) / 2;
            int bottomMargin = leftMargin;
            layoutParams.setMargins(leftMargin, 0, 0, bottomMargin);
            layoutParams.gravity = Gravity.CENTER;
            newTipView.setLayoutParams(layoutParams);
            addView(newTipView);
        }
    }

    public void hideNewTip() {
        if (newTipView != null) {
            removeView(newTipView);
            newTipView.setVisibility(GONE);
            newTipView = null;
        }
    }

}


