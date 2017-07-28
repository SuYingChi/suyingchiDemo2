package com.ihs.inputmethod.uimodules.softgame;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.feature.common.VectorCompat;
import com.ihs.inputmethod.api.HSUIInputMethodService;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;

import static android.widget.RelativeLayout.ALIGN_PARENT_RIGHT;

/**
 * Created by liuzhongtao on 17/7/18.
 */

public class SoftGameButton extends FrameLayout {
    private ImageView buttonIcon;
    private View newTipDotView; //小红点
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener;

    public SoftGameButton(@NonNull Context context) {
        super(context);
        initView();
    }

    public SoftGameButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public SoftGameButton(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        buttonIcon = new ImageView(getContext());
        buttonIcon.setImageDrawable(VectorCompat.createVectorDrawable(getContext(), R.drawable.float_button_tips_svg));

        final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;

        addView(buttonIcon, lp);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //跳转到详情页
                showNewTip();
                SoftGameDisplayHelper.DisplaySoftGames(getContext().getString(R.string.ad_placement_themetryad));
            }
        });
    }

    private void showNewGameTip() {
        // 显示新游戏提示
        View newGameTip = LayoutInflater.from(getContext()).inflate(R.layout.new_game_tip, this, false);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(DisplayUtils.dip2px(239), DisplayUtils.dip2px(112));

        int[] location = new int[2];
        buttonIcon.getLocationInWindow(location);
        lp.setMargins(0, location[1] - DisplayUtils.dip2px(112), 0, 0);
        lp.addRule(ALIGN_PARENT_RIGHT);

        HSUIInputMethodService.getKeyboardPanelMananger().getKeyboardPanelSwitchContainer().addView(newGameTip, lp);
    }

    public void showNewTip() {
        if (newTipDotView == null) {
            newTipDotView = new View(HSApplication.getContext());
            GradientDrawable redPointDrawable = new GradientDrawable();
            redPointDrawable.setColor(Color.RED);
            redPointDrawable.setShape(GradientDrawable.OVAL);
            newTipDotView.setBackgroundDrawable(redPointDrawable);

            int width = HSDisplayUtils.dip2px(7);
            int height = HSDisplayUtils.dip2px(7);
            LayoutParams layoutParams = new LayoutParams(width, height);
            layoutParams.setMargins(DisplayUtils.dip2px(18), 0, 0, 0);
            newTipDotView.setLayoutParams(layoutParams);
            addView(newTipDotView);
        }
    }

    public void hideNewTip() {
        if (newTipDotView != null) {
            removeView(newTipDotView);
            newTipDotView.setVisibility(GONE);
            newTipDotView = null;
        }
    }
}
