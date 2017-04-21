package com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.modules.background;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base.BaseThemeItemProvider;
import com.keyboard.core.themes.custom.KCCustomThemeData;
import com.keyboard.core.themes.custom.KCElementResourseHelper;
import com.keyboard.core.themes.custom.elements.KCBackgroundElement;
import com.keyboard.core.themes.custom.elements.KCBaseElement;

import java.io.File;

/**
 * Created by chenyuanming on 31/10/2016.
 */

public class BackgroundProvider extends BaseThemeItemProvider<KCBackgroundElement, BaseThemeItemProvider.BaseItemHolder, BackgroundFragment> {

    public BackgroundProvider(BackgroundFragment fragment) {
        super(fragment);
    }

    private boolean hasDefaultItemSelectStateSet = false;

    protected void addCustomData(KCBaseElement item) {
        fragment.addChosenItem(item);
        fragment.refreshHeaderNextButtonState();
        fragment.getCustomThemeData().setElement(item);
    }

    @Override
    protected Drawable getChosedBackgroundDrawable() {
        return KCElementResourseHelper.getBackgroundChosedBackgroundDrawable();
    }

    @Override
    protected Drawable getLockedDrawable() {
        return KCElementResourseHelper.getBackgroundLockedDrawable();
    }

    @Override
    protected Drawable getNewMarkDrawable() {
        return KCElementResourseHelper.getBackgroundNewMarkDrawable();
    }

    @Override
    protected void selectItem(BaseItemHolder holder, KCBaseElement item) {
        super.selectItem(holder, item);
        boolean isBackgroundSelected = fragment.getCustomThemeData().getBackgroundImageSource() == KCCustomThemeData.ImageSource.Official;
        fragment.getCustomThemeData().setBackgroundImageSource(KCCustomThemeData.ImageSource.Official);
        if (!isBackgroundSelected) {
            fragment.notifyAdapterOnMainThread();
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull BaseItemHolder holder, @NonNull Object item) {
        super.onBindViewHolder(holder, item);
        ViewGroup.LayoutParams layoutParams = holder.mContentImageView.getLayoutParams();
        DisplayMetrics displayMetrics = holder.itemView.getResources().getDisplayMetrics();
        int width = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels) / fragment.SPAN_COUNT - holder.itemView.getResources().getDimensionPixelSize(R.dimen.custom_theme_item_margin) * 2 - 2;

        layoutParams.height = width;
        layoutParams.width = width;

        updateItemSelection(holder, item);
    }

    private void updateItemSelection(@NonNull BaseItemHolder holder, Object item) {
//        if (!hasDefaultItemSelectStateSet) {
        if (fragment.getCustomThemeData().getBackgroundImageSource() == KCCustomThemeData.ImageSource.Official && fragment.getCustomThemeData().isElementChecked(item)) {
            holder.mCheckImageView.setVisibility(View.VISIBLE);
        } else {
            holder.mCheckImageView.setVisibility(View.INVISIBLE);
        }
//        } /*else {
//            if (item instanceof KCBackgroundCamera || item instanceof KCBackgroundAlbum) {
//                selectItem(holder, item);
//            }
//        }*/
    }

    @Override
    protected void setItemDrawable(@NonNull BaseItemHolder holder, @NonNull Object item) {
        super.setItemDrawable(holder, item);
        KCBackgroundElement backgroundElement = (KCBackgroundElement) item;
        if (backgroundElement.hasLocalGifPreview()) {
            holder.mContentImageView.setImageURI(Uri.fromFile(new File(backgroundElement.getGifPreview())));
        }
    }

    @Override
    protected void setItemTouchListener(@NonNull final BaseItemHolder holder, @NonNull final KCBackgroundElement item) {
        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        doSelectAnimationOnItemViewTouch(v);
                        return true;
                    case MotionEvent.ACTION_UP:
                        doSelectAnimationOnItemViewRelease(v);
                        fragment.addChosenItem(item);
                        fragment.refreshHeaderNextButtonState();
                        onItemClicked(holder, item, true);
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        doSelectAnimationOnItemViewRelease(v);
                        return true;
                }
                return false;
            }
        });
    }


    @Override
    protected boolean isCustomThemeItemSelected(KCBaseElement item) {
        return item instanceof KCBackgroundElement && fragment.getCustomThemeData().getBackgroundImageSource() == KCCustomThemeData.ImageSource.Official &&
                fragment.getCustomThemeData().getBackgroundElement().getName().equals(item.getName());

    }

}
