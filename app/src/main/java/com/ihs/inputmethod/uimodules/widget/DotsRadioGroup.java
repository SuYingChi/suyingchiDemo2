package com.ihs.inputmethod.uimodules.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.support.v4.view.ViewPager;

import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.R;


/**
 * Created by yanxia on 2017/11/27.
 * Copy from http://blog.csdn.net/kaifa1321/article/details/49759599
 */

public class DotsRadioGroup extends RadioGroup implements ViewPager.OnPageChangeListener {

    /**
     * 上下文
     */
    private Context mContext;
    /**
     * 关联的Viewpager
     */
    private ViewPager mVPContent;

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
        this.mContext = context;
        stateListDrawable = new StateListDrawable();
        ShapeDrawable selectedDrawable = new ShapeDrawable(new OvalShape());
        selectedDrawable.setIntrinsicHeight(7);
        selectedDrawable.setIntrinsicWidth(7);
        if (HSKeyboardThemeManager.getCurrentTheme().isDarkBg()) {
            DrawableCompat.setTint(selectedDrawable, getDarkerColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor()));
        } else {
            DrawableCompat.setTint(selectedDrawable, Color.parseColor("#29A0CB"));
        }
        ShapeDrawable normalDrawable = new ShapeDrawable(new OvalShape());
        normalDrawable.setIntrinsicHeight(7);
        normalDrawable.setIntrinsicWidth(7);
        DrawableCompat.setTint(normalDrawable, ContextCompat.getColor(getContext(), R.color.theme_trial_keyboard_edit_text_cursor_color));
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

    /**
     * 获得该位置
     *
     * @return
     */
    public int getmPosition() {
        return mPosition;
    }

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
        this.mVPContent = viewPager;
        mVPContent.addOnPageChangeListener(this);
        // 设置属性
        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 0, 10, 0);
        params.gravity = Gravity.CENTER;

        RadioButton radioButton = null;
        for (int i = 0; i < pageCount; i++) {
            radioButton = new RadioButton(mContext);
            //TODO: 为什么 stateListDrawable.mutate() 不起作用？
            radioButton.setButtonDrawable(stateListDrawable.getConstantState().newDrawable().mutate());
            radioButton.setLayoutParams(params);
            radioButton.setClickable(false);
            addView(radioButton, params);
            mDotsButton[i] = radioButton;
        }

        // 第一个默认选中
        mDotsButton[0].setChecked(true);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mVPContent != null) {
            mVPContent.removeOnPageChangeListener(this);
        }
        mVPContent = null;
        mContext = null;
    }
}
