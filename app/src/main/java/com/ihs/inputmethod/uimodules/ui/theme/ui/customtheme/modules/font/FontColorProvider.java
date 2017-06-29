package com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.modules.font;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base.BaseThemeItemProvider;
import com.keyboard.core.themes.custom.KCElementResourseHelper;
import com.keyboard.core.themes.custom.elements.KCBaseElement;
import com.keyboard.core.themes.custom.elements.KCTextColorElement;

/**
 * Created by chenyuanming on 31/10/2016.
 */

public class FontColorProvider extends BaseThemeItemProvider<KCTextColorElement, BaseThemeItemProvider.BaseItemHolder, FontFragment> {
    public FontColorProvider(FontFragment fragment) {
        super(fragment);
    }


    @Override
    protected boolean isCustomThemeItemSelected(KCBaseElement item) {
        return item instanceof KCTextColorElement &&
                fragment.getCustomThemeData().getTextColorElement().getColor() == ((KCTextColorElement)item).getColor();
    }

    @NonNull
    @Override
    protected BaseItemHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        BaseItemHolder holder = super.onCreateViewHolder(inflater, parent);
        DisplayMetrics displayMetrics = holder.itemView.getResources().getDisplayMetrics();
        int width = Math.min(displayMetrics.widthPixels,displayMetrics.heightPixels) / fragment.SPAN_COUNT  - holder.itemView.getResources().getDimensionPixelSize(R.dimen.custom_theme_item_margin) *2;
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, width);
        layoutParams.gravity = Gravity.CENTER;
        holder.itemView.setLayoutParams(layoutParams);
        return holder;
    }

    @Override
    protected Drawable getChosedBackgroundDrawable() {
        return KCElementResourseHelper.getTextColorChosedBackgroundDrawable();
    }
}