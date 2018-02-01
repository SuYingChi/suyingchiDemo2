package com.ihs.inputmethod.uimodules.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.inputmethod.uimodules.R;


/**
 * Created by yanxia on 2017/11/27.
 * Copy from http://blog.csdn.net/kaifa1321/article/details/49759599
 */

public class DotsRadioGroup extends RadioGroup implements ViewPager.OnPageChangeListener {

    /**
     * 当前显示指示点
     */
    private int mPosition;

    /**
     * 指示点集合
     */
    private RadioButton[] mDotsButton;

    private StateListDrawable stateListDrawable;

    public DotsRadioGroup(Context context) {
        this(context, null);
    }

    public DotsRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        stateListDrawable = new StateListDrawable();

        ShapeDrawable selectedDrawable = new ShapeDrawable(new OvalShape());
        selectedDrawable.setIntrinsicHeight(DisplayUtils.dip2px(5));
        selectedDrawable.setIntrinsicWidth(DisplayUtils.dip2px(5));
//        if (HSKeyboardThemeManager.getCurrentTheme().isDarkBg()) {
//            selectedDrawable.getPaint().setColor(getDarkerColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor()));
//        } else {
        selectedDrawable.getPaint().setColor(getContext().getResources().getColor(R.color.settings_button_light_icon));
//        }

        ShapeDrawable normalDrawable = new ShapeDrawable(new OvalShape());
        normalDrawable.setIntrinsicHeight(DisplayUtils.dip2px(5));
        normalDrawable.setIntrinsicWidth(DisplayUtils.dip2px(5));
        normalDrawable.getPaint().setColor(getContext().getResources().getColor(R.color.theme_trial_keyboard_edit_text_cursor_color));

        stateListDrawable.addState(new int[]{android.R.attr.state_checked}, selectedDrawable);
        stateListDrawable.addState(new int[]{}, normalDrawable);
    }

    private int getDarkerColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv); // convert to hsv
        // make darker
        hsv[1] = hsv[1] + 0.1f; // more saturation
        hsv[2] = hsv[2] - 0.1f; // less brightness
        return Color.HSVToColor(hsv);
    }

    /**
     * This method will be invoked when the current page is scrolled, either as part
     * of a programmatically initiated smooth scroll or a user initiated touch scroll.
     *
     * @param position             Position index of the first page currently being displayed.
     *                             Page position+1 will be visible if positionOffset is nonzero.
     * @param positionOffset       Value from [0, 1) indicating the offset from the page at position.
     * @param positionOffsetPixels Value in pixels indicating the offset from position.
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    /**
     * This method will be invoked when a new page becomes selected. Animation is not
     * necessarily complete.
     *
     * @param position Position index of the new selected page.
     */
    @Override
    public void onPageSelected(int position) {
        mPosition = position;
        if (mDotsButton != null) {
            for (int i = 0; i < mDotsButton.length; i++) {
                boolean isChecked = i == mPosition;
                mDotsButton[i].setChecked(isChecked);
            }
        }
    }

    /**
     * Called when the scroll state changes. Useful for discovering when the user
     * begins dragging, when the pager is automatically settling to the current page,
     * or when it is fully stopped/idle.
     *
     * @param state The new scroll state.
     * @see ViewPager#SCROLL_STATE_IDLE
     * @see ViewPager#SCROLL_STATE_DRAGGING
     * @see ViewPager#SCROLL_STATE_SETTLING
     */
    @Override
    public void onPageScrollStateChanged(int state) {

    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    /**
//     * 获得该位置
//     *
//     * @return
//     */
//    public int getmPosition() {
//        return mPosition;
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    /**
     * 关联Viewpager并初始化指示点
     *
     * @param viewPager
     * @param pageCount
     */
    public void setDotView(ViewPager viewPager, int pageCount) {
        if (pageCount < 2) {
            setVisibility(View.GONE);
            return;
        }
        // 清理所有的点
        setVisibility(View.VISIBLE);
        removeAllViews();
        mDotsButton = new RadioButton[pageCount];
        viewPager.addOnPageChangeListener(this);
        // 设置属性
        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 0, 10, 0);
        params.gravity = Gravity.CENTER;

        RadioButton radioButton;
        for (int i = 0; i < pageCount; i++) {
            radioButton = new RadioButton(HSApplication.getContext());
            //TODO: 为什么 stateListDrawable.mutate() 不起作用？
            Drawable.ConstantState constantState = stateListDrawable.getConstantState();
            if (constantState != null) {
                radioButton.setButtonDrawable(constantState.newDrawable().mutate());
            }
            radioButton.setLayoutParams(params);
            radioButton.setClickable(false);
            addView(radioButton, params);
            mDotsButton[i] = radioButton;
        }

        // 第一个默认选中
        mDotsButton[0].setChecked(true);
    }
}
