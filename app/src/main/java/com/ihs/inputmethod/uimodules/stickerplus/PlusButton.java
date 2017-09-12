package com.ihs.inputmethod.uimodules.stickerplus;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerDataManager;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerGroup;

import java.util.List;
import java.util.Set;

import static com.ihs.inputmethod.uimodules.utils.RippleDrawableUtils.getTransparentRippleBackground;

/**
 * Created by fanxu.kong on 17/9/7.
 */

public class PlusButton extends FrameLayout {
    private static final String KEY_SHOW_NEW_MARK = "key_show_new_mark";
    private View newTipView;
    private final static int FUNCTION_VIEW_REAL_WIDTH = 18;
    public static final String KEY_FIRST_KEYBOARD_APPEAR = "keyboard_first_appear";
    private HSPreferenceHelper helper;

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
        plusImage.setImageDrawable(HSApplication.getContext().getResources().getDrawable(R.drawable.common_tab_plus));
        addView(plusImage);

        if (isFirstKeyboardAppear()) {
            showNewTip();
            saveFirstEnterKeyboardState();
        } else if (isShowNewTip()) {// 控制点没有点击，如果点击了，则不再显示，默认情况是未点击的时候，是显示的
            showNewTip();
        } else if (StickerDataManager.getInstance().getShowNewTipState()) { // 有没有更新，如果更新了则显示；没有更新，则不显示
            showNewTip();
        } else {
            hideNewTip();
        }
    }

    private boolean isFirstKeyboardAppear() {
        if (helper == null) {
            helper = HSPreferenceHelper.getDefault();
        }

        return helper.getBoolean(KEY_FIRST_KEYBOARD_APPEAR, true);
    }

    public void saveFirstEnterKeyboardState() {
        if (helper == null) {
            helper = HSPreferenceHelper.getDefault();
        }
        helper.putBoolean(KEY_FIRST_KEYBOARD_APPEAR, false);
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

    public void saveUnshowNewTipState() {
        helper.putBoolean(KEY_SHOW_NEW_MARK, false);
    }

    private boolean isShowNewTip() {
        if (helper == null) {
            helper = HSPreferenceHelper.getDefault();
        }
        return helper.getBoolean(KEY_SHOW_NEW_MARK, true);
    }


}


