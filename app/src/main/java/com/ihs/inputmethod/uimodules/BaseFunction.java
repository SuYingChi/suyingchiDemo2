package com.ihs.inputmethod.uimodules;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;

import static com.ihs.inputmethod.uimodules.utils.RippleDrawableUtils.getTransparentRippleBackground;

/**
 * Created by jixiang on 16/12/12.
 */
public class BaseFunction extends FrameLayout {
    private View functionView;
    private View newTipView;
    private NewTipStatueChangeListener newTipStatueChangeListener;
    private boolean showNewMark;
    private final static int FUNCTION_VIEW_REAL_WIDTH = 18; // function real width 18dp
    private final static int FUNCTION_VIEW_MARGIN_LEFT = 15; //margin value,unit dp

    public BaseFunction(Context context) {
        super(context);
        this.setBackgroundDrawable(getTransparentRippleBackground());
    }

    public void setFunctionView(View view) {
        if (this.functionView != null) {
            removeView(this.functionView);
        }
        this.functionView = view;
        addView(functionView, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        updateNewTipPosition();
    }

    public void showNewTip() {
        if (newTipView == null) {
            newTipView = new View(HSApplication.getContext());
            GradientDrawable redPointDrawable = new GradientDrawable();
            redPointDrawable.setColor(Color.RED);
            redPointDrawable.setShape(GradientDrawable.OVAL);
            newTipView.setBackgroundDrawable(redPointDrawable);

            int width = HSDisplayUtils.dip2px(7);
            int height = HSDisplayUtils.dip2px(7);
            LayoutParams layoutParams = new LayoutParams(width, height);
            int leftMargin = HSDisplayUtils.dip2px(FUNCTION_VIEW_REAL_WIDTH) / 2;
            int bottomMargin = leftMargin;
            layoutParams.setMargins(leftMargin, 0, 0, bottomMargin);
            layoutParams.gravity = Gravity.CENTER;
            newTipView.setLayoutParams(layoutParams);
            addView(newTipView);
        }
        showNewMark = true;
    }

    private void updateNewTipPosition() {
        if (functionView != null) {
            int expectedWidth = (HSDisplayUtils.dip2px(FUNCTION_VIEW_REAL_WIDTH) + HSDisplayUtils.dip2px(FUNCTION_VIEW_MARGIN_LEFT) * 2);
            int horiMargin = (expectedWidth - functionView.getMeasuredWidth()) / 2;
            if (horiMargin > 0) {
                ((LayoutParams) functionView.getLayoutParams()).setMargins(horiMargin, 0, horiMargin, 0);
            }
        }
        if (newTipView != null && showNewMark) {
            newTipView.setVisibility(newTipStatueChangeListener.shouldShowTip() ? VISIBLE : GONE);
        }
    }

    public void hideNewTip() {
        if (newTipView != null) {
            removeView(newTipView);
            newTipView.setVisibility(GONE);
            newTipView = null;
        }
        showNewMark = false;
    }

    public void setNewTipStatueChangeListener(NewTipStatueChangeListener newTipStatueChangeListener) {
        this.newTipStatueChangeListener = newTipStatueChangeListener;
    }

    public interface NewTipStatueChangeListener {
        boolean shouldShowTip();
    }
}
