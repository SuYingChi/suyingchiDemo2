package com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.modules.button;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base.BaseThemeItemProvider;
import com.keyboard.core.themes.custom.KCElementResourseHelper;
import com.keyboard.core.themes.custom.elements.KCButtonShapeElement;
import com.keyboard.core.themes.custom.elements.KCBaseElement;

/**
 * Created by chenyuanming on 31/10/2016.
 */

public class ButtonShapeProvider extends BaseThemeItemProvider<KCButtonShapeElement, BaseThemeItemProvider.BaseItemHolder, ButtonFragment> {
    public ButtonShapeProvider(ButtonFragment fragment) {
        super(fragment);
    }

    @Override
    protected boolean isCustomThemeItemSelected(KCBaseElement item) {
        return item instanceof KCButtonShapeElement &&
                fragment.getCustomThemeData().getButtonShapeElement().getName().equals(item.getName());
    }


    @Override
    protected void onBindViewHolder(@NonNull final BaseItemHolder holder, final @NonNull Object item) {
        super.onBindViewHolder(holder, item);
        if (Build.VERSION.SDK_INT == 15) {
            holder.mCheckImageView.requestLayout();
            holder.mCheckImageView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

                    holder.mCheckImageView.removeOnLayoutChangeListener(this);
                    final ViewGroup.LayoutParams layoutParams = holder.mCheckImageView.getLayoutParams();
                    int width = right - left;
                    Drawable drawable = getChosedBackgroundDrawable(); //item.getChosedBackgroundDrawable();
                    int height = drawable.getIntrinsicHeight() * width / drawable.getIntrinsicWidth();
                    layoutParams.width = width;
                    layoutParams.height = height;
                    bottom = top + height;

                    v.layout(left, top, right, top + height);
                    v.forceLayout();
                    v.requestLayout();
                    if (oldBottom != bottom) {
                        fragment.notifyAdapterOnMainThread();
                    }
                }
            });
        }
    }

    private Drawable chosedBackgroundDrawable;

    @Override
    protected Drawable getChosedBackgroundDrawable() {
        if(chosedBackgroundDrawable == null) {
            chosedBackgroundDrawable = KCElementResourseHelper.getButtonShapeChosedBackgroundDrawable();
        }
        return chosedBackgroundDrawable;
    }
}