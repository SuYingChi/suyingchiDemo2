package com.ihs.inputmethod.uimodules.stickerplus;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerDataManager;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerGroup;
import com.ihs.inputmethod.uimodules.ui.sticker.homeui.StickerCardAdapter;

import java.util.List;

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
    private List<String> preStickerGroupNamesList;

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

        List<StickerGroup> stickerGroupList = StickerDataManager.getInstance().getStickerGroupList();
        preStickerGroupNamesList = StickerCardAdapter.getSavedPreStickerGroupNamelist();

        /**
         * 1. 如果第一次进入app，即使没有new，也显示小红点
         * 2. 如果检查有new，显示new
         * 3. 只要一次hide后，再也不显示
         * 4. 如果有new更新后，重新进入时候将会显示new
         */

        if (checkIsFirstIntoApp()) {
            showNewTip();
            saveFirstEnterKeyboardState();
        } else if (getShowNewTipState()) {
            showNewTip();
        } else if (checkStickerNewNumChange(stickerGroupList)) {//new sticker数目增加时候会显示小红点
            showNewTip();
        } else {
            hideNewTip();
        }
    }

    private boolean checkIsFirstIntoApp() {
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

    private boolean checkStickerNewNumChange(List<StickerGroup> stickerGroupList) {
        if (preStickerGroupNamesList == null) {// 没有进入过app且首次键盘弹出时
            return false;
        } else { // 如果不为空，说明已经在app对其进行了初始化，那么就比较现有的列表，看是否有增加的sticker
            List<String> currentStickerGroupNameList = StickerCardAdapter.getCurrentStickerGroupNameList(stickerGroupList);
            return currentStickerGroupNameList.size() > preStickerGroupNamesList.size();
        }
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

    /**
     * 已显示过new
     */
    public void saveShowNewTip() {
        helper.putBoolean(KEY_SHOW_NEW_MARK, false);
    }

    private boolean getShowNewTipState() {
        if (helper == null) {
            helper = HSPreferenceHelper.getDefault();
        }
        return helper.getBoolean(KEY_SHOW_NEW_MARK, true);
    }


}


