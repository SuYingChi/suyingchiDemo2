package com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.modules.button;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base.BaseThemeFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base.BaseThemeItemProvider;
import com.keyboard.core.themes.custom.KCElementResourseHelper;
import com.keyboard.core.themes.custom.elements.KCBaseElement;
import com.keyboard.core.themes.custom.elements.KCButtonStyleElement;

/**
 * Created by chenyuanming on 31/10/2016.
 */

public class ButtonStyleProvider extends BaseThemeItemProvider<KCButtonStyleElement, BaseThemeItemProvider.BaseItemHolder, ButtonFragment> {

    private int mainColor = -1;
    private Drawable backgroundDrawable;
    private Drawable darkBackgroundDrawable;
    private Drawable lockedDrawable;
    private Drawable chosedBackgroundDrawable;
    public ButtonStyleProvider(ButtonFragment fragment) {
        super(fragment);
    }

    public static Drawable getButtonStyleBackgroundDrawable(int backgroundMainColor) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.OVAL);
        gradientDrawable.setColor(backgroundMainColor);
        return gradientDrawable;
    }

    @Override
    protected boolean isCustomThemeItemSelected(KCBaseElement item) {
        return item instanceof KCButtonStyleElement &&
                fragment.getCustomThemeData().getButtonStyleElement().getName().equals(item.getName());
    }

    @Override
    protected void onBindViewHolder(@NonNull BaseItemHolder holder, @NonNull Object item) {
        super.onBindViewHolder(holder, item);
        DisplayMetrics displayMetrics = holder.itemView.getResources().getDisplayMetrics();
        int width = Math.min(displayMetrics.widthPixels,displayMetrics.heightPixels) / BaseThemeFragment.SPAN_COUNT -  holder.itemView.getResources().getDimensionPixelSize(R.dimen.custom_theme_item_margin) *2;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, width);
        layoutParams.gravity = Gravity.CENTER;
        holder.itemView.setLayoutParams(layoutParams);

        if ("none".equals(fragment.getCustomThemeData().getButtonShapeElement().getName())) {
            //button shape为none的时候,button style手动设置为none,并且不可点击
            holder.mBackgroundImageView.setImageDrawable(getDarkerBackgroundDrawable());
            holder.mCheckImageView.setVisibility(View.INVISIBLE);
        } else {
            holder.mBackgroundImageView.setImageDrawable(getBackgroundDrawable(item));
            if(fragment.getCustomThemeData().isElementChecked(item)) {
                holder.mCheckImageView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected Drawable getChosedBackgroundDrawable() {
        if(chosedBackgroundDrawable == null) {
            chosedBackgroundDrawable = KCElementResourseHelper.getButtonStyleChosedBackgroundDrawable();
        }
        return chosedBackgroundDrawable;
    }

    @Override
    protected Drawable getLockedDrawable() {
        if(lockedDrawable == null) {
            lockedDrawable = KCElementResourseHelper.getButtonStyleLockedDrawable();
        }
        return lockedDrawable;
    }

    @Override
    protected Drawable getBackgroundDrawable(Object item) {
        if (mainColor == -1) {
            mainColor = fragment.getCustomThemeData().getBackgroundMainColor();
        }
        if (backgroundDrawable == null) {
            backgroundDrawable = getButtonStyleBackgroundDrawable(mainColor);
        }
        return getButtonStyleBackgroundDrawable(mainColor);
    }

    @Override
    protected Drawable getDarkerBackgroundDrawable() {
        if(mainColor == -1) {
            mainColor = fragment.getCustomThemeData().getBackgroundMainColor();
        }
        if(darkBackgroundDrawable == null) {
            darkBackgroundDrawable = KCElementResourseHelper.getButtonStyleDarkerBackgroundDrawable(mainColor);
        }
        return darkBackgroundDrawable;
    }
}