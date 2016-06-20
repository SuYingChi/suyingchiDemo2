package com.ihs.customtheme.app.ui;


import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.ihs.app.framework.HSApplication;
import com.ihs.customtheme.R;
import com.ihs.customtheme.app.iap.IAPManager;
import com.ihs.inputmethod.theme.HSCustomThemeItemBase;


public class CustomThemeItemViewHolder extends RecyclerView.ViewHolder implements CustomThemeItemView {
    private View mView;
    private ImageView mContentImageView;
    private ImageView mCheckImageView;
    private ImageView mBackgroundImageView;
    private ImageView mLockedImageView;

    HSCustomThemeItemBase item;

    CustomThemeItemViewHolder(final View view) {
        super(view);
        mView = view;
        mContentImageView = (ImageView) mView.findViewById(R.id.custom_theme_item_image);
        mCheckImageView = (ImageView) mView.findViewById(R.id.custom_theme_item_check_bg);
        mBackgroundImageView = (ImageView) mView.findViewById(R.id.custom_theme_item_bg);
        mLockedImageView = (ImageView) mView.findViewById(R.id.custom_theme_item_locked);
    }

    public void bindItem(final HSCustomThemeItemBase item) {
        this.item = item;
        final HSCustomThemeItemBase.ItemType itemType = this.item.getItemType();
        final Drawable drawable = this.item.getDrawable();

        //locked view
        Drawable locked = this.item.getItemLockedDrawable();
        if (locked != null) {
            mLockedImageView.setImageDrawable(locked);
        }

        switch (itemType) {
            case BACKGROUND:
                final float density = mView.getContext().getResources().getDisplayMetrics().density;
                if (drawable != null) {
                    RoundedBitmapDrawable backgroundIconDrawable = (RoundedBitmapDrawable) drawable;
                    if (HSApplication.getContext().getResources().getBoolean(R.bool.isTablet)) {
                        backgroundIconDrawable.setCornerRadius(3 * density);
                    } else {
                        backgroundIconDrawable.setCornerRadius(3 * density);
                    }
                }
                break;
            default:
                break;
        }
        //check is need show lock
        if (!IAPManager.getManager().isOwnProduct(item)) {
            mLockedImageView.setVisibility(View.VISIBLE);
        } else {
            mLockedImageView.setVisibility(View.GONE);
        }

        //content view
        mContentImageView.setImageDrawable(drawable);

        //check view
        Drawable checked = this.item.getChosedBackgroundDrawable();
        mCheckImageView.setImageDrawable(checked);
        mCheckImageView.setVisibility(View.INVISIBLE);
        mCheckImageView.setVisibility(item.isChecked ? View.VISIBLE : View.INVISIBLE);

        //background
        Drawable background = this.item.getItemBackgroundDrawable();
        if (background != null) {
            mBackgroundImageView.setVisibility(View.VISIBLE);
            mBackgroundImageView.setImageDrawable(background);
        } else {
            mBackgroundImageView.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void selectedThemeItem() {
        if (mCheckImageView != null) {
            mCheckImageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void unSelectedThemeItem() {
        if (mCheckImageView != null) {
            mCheckImageView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void lockedThemeItem() {
        if (mLockedImageView != null) {
            mLockedImageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void unLockedThemeItem() {
        if (mLockedImageView != null) {
            mLockedImageView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public HSCustomThemeItemBase getCustomThemeItem() {
        if (item != null) {
            return item;
        }
        return null;
    }
}
