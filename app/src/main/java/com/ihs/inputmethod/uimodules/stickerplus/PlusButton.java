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
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by fanxu.kong on 17/9/7.
 */


public class PlusButton extends FrameLayout {
    private static final String KEY_SHOW_NEW_MARK = "key_show_new_mark";
    private static final String STICKER_NEW_NUMBER = "sticker_new_num";
    private AppCompatImageView plusImage;
    private View newTipView;
    private final static int FUNCTION_VIEW_REAL_WIDTH = 18; // function real width 18dp
    private final static int FUNCTION_VIEW_MARGIN_LEFT = 15; //margin value,unit dp
    private boolean showNewMark;
    public static final String KEY_FIRST_INTO_APP = "key_first_into_app";
    private HSPreferenceHelper helper;

    public PlusButton(Context context) {
        this(context, null);
    }

    public PlusButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlusButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(HSDisplayUtils.dip2px(22), HSDisplayUtils.dip2px(22));
        lp.gravity = Gravity.CENTER;
        lp.leftMargin = HSDisplayUtils.dip2px(10);
        lp.rightMargin = HSDisplayUtils.dip2px(10);

        plusImage = new AppCompatImageView(getContext());
        plusImage.setLayoutParams(lp);
        plusImage.setImageDrawable(HSApplication.getContext().getResources().getDrawable(R.drawable.common_tab_plus));
        addView(plusImage);

//
//        // 如果第一次进入app，这里返回true，进入
//        if (getShowNewTipState()) {
//            // 如果是第一次进入app，进入
//            if (checkIsFirstIntoApp() || checkIsHaveNewSticker()) {
//                showNewTip();
//                Log.e("kong", "---------- show");
//            }
//        } else if (getShowNewTipState()) {
//            hideNewTip();
//            saveShowNewTip();
//            Log.e("kong", "---------- hide");
//        }

        /**
         * 1. 如果第一次进入app，显示new
         * 2. 如果检查有new，显示new
         * 3. 只要一次hide后，再也不显示
         * 4. 如果有new更新后，重新进入时候将会显示new
         */

        if (checkIsFirstIntoApp()) {
            showNewTip();
        } else if (checkIsHaveNewSticker() && getShowNewTipState()) {
            showNewTip();
        } else if (checkStickerNewNumChange()) {//new sticker 数目变化时候要进入显示;并且点击后再也不显示;默认情况下是return false的，当第二次进入的时候就会去取存储的数目来进行比较，走不一致的逻辑
            Log.e("dongdong", "checkStickerNewNumChange");
            showNewTip();
        } else {
            hideNewTip();
        }
    }

    private boolean checkIsFirstIntoApp() {
        helper = HSPreferenceHelper.getDefault();
        if (helper.getBoolean(KEY_FIRST_INTO_APP, true)) {
            helper.putBoolean(KEY_FIRST_INTO_APP, false);
        }
        return helper.getBoolean(KEY_FIRST_INTO_APP, true);
    }

    private boolean checkStickerNewNumChange() {
        List<Map<String, Object>> stickerConfigList = (List<Map<String, Object>>) HSConfig.getList("Application", "StickerGroupList");
        List<StickerGroup> stickerGroups = new ArrayList<>();
        for (Map<String, Object> map : stickerConfigList) {
            String stickerGroupName = (String) map.get("name");
            StickerGroup stickerGroup = new StickerGroup(stickerGroupName);
            if (stickerGroup.isStickerGroupDownloaded()) {
                continue;
            }
            String stickerTag = (String) map.get("showNewMark");
            if (android.text.TextUtils.equals(stickerTag, "YES")) {
                stickerGroups.add(stickerGroup);

            }
        }
        int num = stickerGroups.size();
        Log.e("dongdong", "num:    " + num);
        if (helper == null) {
            helper = HSPreferenceHelper.getDefault();
        }

        if (num <= helper.getInt(STICKER_NEW_NUMBER, num)) {//当和存储的数目相同或小于原有的时候，sticker没有新的
            Log.e("dongdong", "num <= helper.getInt(STICKER_NEW_NUMBER, num):  put前     " + helper.getInt(STICKER_NEW_NUMBER, num));
            helper.putInt(STICKER_NEW_NUMBER, num);
            Log.e("dongdong", "num <= helper.getInt(STICKER_NEW_NUMBER, num):   put后    " + helper.getInt(STICKER_NEW_NUMBER, num));
            return false;
        } else if (num > helper.getInt(STICKER_NEW_NUMBER, num)){
            Log.e("dongdong", "num > helper.getInt(STICKER_NEW_NUMBER, num):    put前   " + helper.getInt(STICKER_NEW_NUMBER, num));

            helper.putInt(STICKER_NEW_NUMBER, num);

            Log.e("dongdong", "num > helper.getInt(STICKER_NEW_NUMBER, num):     put后  " + helper.getInt(STICKER_NEW_NUMBER, num));

            return true;
        }
        return false;
    }

    private boolean checkIsHaveNewSticker() {
        List<Map<String, Object>> stickerConfigList = (List<Map<String, Object>>) HSConfig.getList("Application", "StickerGroupList");
        List<StickerGroup> stickerGroups = new ArrayList<>();
        for (Map<String, Object> map : stickerConfigList) {
            String stickerGroupName = (String) map.get("name");
            StickerGroup stickerGroup = new StickerGroup(stickerGroupName);
            if (stickerGroup.isStickerGroupDownloaded()) {
                continue;
            }
            String stickerTag = (String) map.get("showNewMark");
            if (android.text.TextUtils.equals(stickerTag, "YES")) {
                stickerGroups.add(stickerGroup);
                return true;
            }
        }
        return false;
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


//    public void saveNotShowNewTip() {
//        helper.putBoolean(KEY_SHOW_NEW_MARK, true);
//    }

    /**
     * 已显示过new，下次不显示
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


