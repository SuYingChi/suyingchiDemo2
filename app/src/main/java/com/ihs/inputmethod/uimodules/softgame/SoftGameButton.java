package com.ihs.inputmethod.uimodules.softgame;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.inputmethod.api.HSUIInputMethodService;
import com.ihs.inputmethod.api.framework.HSInputMethodService;
import com.ihs.inputmethod.uimodules.NewTipLayout;
import com.ihs.inputmethod.uimodules.R;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.zip.Inflater;

import static android.widget.RelativeLayout.ALIGN_PARENT_RIGHT;

/**
 * Created by liuzhongtao on 17/7/18.
 *
 */

public class SoftGameButton extends NewTipLayout {
    private ImageView buttonIcon;

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
        buttonIcon.setImageResource(R.drawable.float_button_tips_svg);

        final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;

        addView(buttonIcon, lp);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewGameTip();
            }
        });

        setNewTipMargin(DisplayUtils.dip2px(18), 0, 0, 0);
    }

    private void showNewGameTip() {
        // 显示新游戏提示
        View newGameTip = LayoutInflater.from(getContext()).inflate(R.layout.new_game_tip, this, false);
//        RoundedImageView play = (RoundedImageView) newGameTip.findViewById(R.id.newGamePlay);
//        play.setCornerRadius(DisplayUtils.dip2px(2));

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(DisplayUtils.dip2px(239), DisplayUtils.dip2px(112));

        int[] location = new int[2];
        buttonIcon.getLocationInWindow(location);
        lp.setMargins(0, location[1] - DisplayUtils.dip2px(112), 0, 0);
        lp.addRule(ALIGN_PARENT_RIGHT);

        HSUIInputMethodService.getKeyboardPanelMananger().getKeyboardPanelSwitchContainer().addView(newGameTip, lp);
    }
}
