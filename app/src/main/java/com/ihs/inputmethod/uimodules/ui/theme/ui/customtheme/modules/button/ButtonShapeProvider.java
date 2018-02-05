package com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.modules.button;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base.BaseThemeFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base.BaseThemeItemProvider;
import com.keyboard.core.themes.custom.KCElementResourseHelper;
import com.keyboard.core.themes.custom.elements.KCBaseElement;
import com.keyboard.core.themes.custom.elements.KCButtonShapeElement;

/**
 * Created by chenyuanming on 31/10/2016.
 */

public class ButtonShapeProvider extends BaseThemeItemProvider<KCButtonShapeElement, BaseThemeItemProvider.BaseItemHolder, ButtonFragment> {
    private Drawable chosedBackgroundDrawable;

    public ButtonShapeProvider(ButtonFragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    protected BaseItemHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        BaseItemHolder holder = super.onCreateViewHolder(inflater, parent);
        DisplayMetrics displayMetrics = holder.itemView.getResources().getDisplayMetrics();
        int width = Math.min(displayMetrics.widthPixels,displayMetrics.heightPixels) / BaseThemeFragment.SPAN_COUNT -  holder.itemView.getResources().getDimensionPixelSize(R.dimen.custom_theme_item_margin) *2;
        int height = (int) (width *192.0f/168);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
        layoutParams.gravity = Gravity.CENTER;
        holder.itemView.setLayoutParams(layoutParams);

        return holder;
    }

    @Override
    protected boolean isCustomThemeItemSelected(KCBaseElement item) {
        return item instanceof KCButtonShapeElement &&
                fragment.getCustomThemeData().getButtonShapeElement().getName().equals(item.getName());
    }

    @Override
    protected Drawable getChosedBackgroundDrawable() {
        if(chosedBackgroundDrawable == null) {
            chosedBackgroundDrawable = KCElementResourseHelper.getButtonShapeChosedBackgroundDrawable();
        }
        return chosedBackgroundDrawable;
    }
}