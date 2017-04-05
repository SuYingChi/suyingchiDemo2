package com.mobipioneer.inputmethod.panels.settings.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.panelcontainer.BasePanel;

/**
 * Created by chenyuanming on 12/10/2016.
 */

public abstract class ActionButton extends ImageView {
    public ActionButton(Context context) {
        super(context);
        init();
    }

    public ActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected void init() {
        setImageDrawable();

        setOnClickListener(getOnClickListener());

    }

    private void setImageDrawable() {
        StateListDrawable mTabbarBtnDrawable = new StateListDrawable();
        Drawable drawable = getNormalDrawable();
        Drawable pressedDrawable = getPressedDrawable();


        mTabbarBtnDrawable.addState(new int[]{android.R.attr.state_focused}, pressedDrawable);
        mTabbarBtnDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
        mTabbarBtnDrawable.addState(new int[]{android.R.attr.state_selected}, pressedDrawable);
        mTabbarBtnDrawable.addState(new int[]{}, drawable);

        setImageDrawable(mTabbarBtnDrawable);
    }

    protected abstract Drawable getPressedDrawable();

    protected abstract Drawable getNormalDrawable();

    protected abstract OnClickListener getOnClickListener();

    public abstract BasePanel getPanel();


    INotificationObserver observer = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (HSKeyboardThemeManager.HS_NOTIFICATION_THEME_CHANGED.equals(s)) {
                setImageDrawable();//reset theme drawable
            }
        }
    };
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        HSGlobalNotificationCenter.addObserver(HSKeyboardThemeManager.HS_NOTIFICATION_THEME_CHANGED, observer);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        HSGlobalNotificationCenter.removeObserver(observer);
    }
}
