package com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.modules.background;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base.BaseThemeItemProvider;
import com.keyboard.core.themes.custom.KCCustomThemeData;
import com.keyboard.core.themes.custom.KCElementResourseHelper;

/**
 * Created by ihandysoft on 17/2/16.
 */

public class CameraAlbumProvider extends BaseThemeItemProvider<Integer, BaseThemeItemProvider.BaseItemHolder, BackgroundFragment> {

    private boolean hasDefaultItemSelectStateSet = false;

    public CameraAlbumProvider(BackgroundFragment fragment) {
        super(fragment);
    }


    protected void onAlbumItemClicked(final BaseItemHolder holder, final Integer item) {
        HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("app_customize_background_album_clicked");
        fragment.pickFromGallery(new BackgroundFragment.OnSelectCallback() {
            @Override
            public void onSelectItem(int type) {
                fragment.refreshHeaderNextButtonState();
            }
        });
    }

    protected void onCameraItemClicked(final BaseItemHolder holder, final Integer item) {
        HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("app_customize_background_camera_clicked");
        fragment.pickFromCamera(new BackgroundFragment.OnSelectCallback() {
            @Override
            public void onSelectItem(int type) {
                fragment.refreshHeaderNextButtonState();
            }
        });
    }

    protected void selectItem(BaseItemHolder holder, Integer item) {
        if ((fragment.getCustomThemeData().getBackgroundImageSource() == KCCustomThemeData.ImageSource.Album && item == R.drawable.custom_theme_background_album_fg) ||
                (fragment.getCustomThemeData().getBackgroundImageSource() == KCCustomThemeData.ImageSource.Camera && item == R.drawable.custom_theme_background_camera_fg)) {
            Drawable checked = getChosedBackgroundDrawable();//item.getChosedBackgroundDrawable();
            holder.mCheckImageView.setImageDrawable(checked);
            holder.mCheckImageView.setVisibility(View.VISIBLE);
        } else {
            holder.mCheckImageView.setVisibility(View.INVISIBLE);
        }
    }

    protected void adjustLayoutForDevice88(@NonNull BaseItemHolder holder, Integer item) {
        holder.mCheckImageView.setImageDrawable(getChosedBackgroundDrawable());
    }

    protected void setItemBackground(@NonNull BaseItemHolder holder, @NonNull Integer item) {
        //background
        Drawable background = getBackgroundDrawable(item);
        holder.mBackgroundImageView.setImageDrawable(background);
        if (background != null) {
            holder.mBackgroundImageView.setVisibility(View.VISIBLE);
        } else {
            holder.mBackgroundImageView.setVisibility(View.INVISIBLE);
        }

    }

    protected void setItemTouchListener(@NonNull final BaseItemHolder holder, @NonNull final Integer item) {

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
                        if (item == R.drawable.custom_theme_background_album_fg) {
                            onAlbumItemClicked(holder, item);
                        } else {
                            onCameraItemClicked(holder, item);
                        }
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        doSelectAnimationOnItemViewRelease(v);
                        return true;
                }
                return false;
            }
        });
    }

    @NonNull
    @Override
    protected BaseItemHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        BaseItemHolder holder = new BaseItemHolder(inflater.inflate(R.layout.ct_item_camera_or_gallery, null));
        int margin = holder.itemView.getResources().getDimensionPixelSize(R.dimen.custom_theme_item_margin);
        DisplayMetrics displayMetrics = holder.itemView.getResources().getDisplayMetrics();
        int width = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels) / fragment.SPAN_COUNT - margin * 2;
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(width,width);
        holder.itemView.setLayoutParams(layoutParams);
        return holder;
    }

    @Override
    protected void onBindViewHolder(@NonNull BaseItemHolder holder, @NonNull Object item) {
//        adjustLayoutForDevice88(holder, (Integer) item);
        setItemTouchListener(holder, (Integer) item);
        setItemDrawable(holder, item);
        setItemBackground(holder, (Integer) item);



        BackgroundFragment.EntryMode mode = fragment.getEntryMode();
        if (mode == BackgroundFragment.EntryMode.Camera && !hasDefaultItemSelectStateSet) {
            onCameraItemClicked(holder, (Integer) item);
            hasDefaultItemSelectStateSet = true;
        } else if (mode == BackgroundFragment.EntryMode.Gallery && !hasDefaultItemSelectStateSet) {
            onAlbumItemClicked(holder, (Integer) item);
            hasDefaultItemSelectStateSet = true;
        }

        selectItem(holder, (Integer) item);
    }

    @Override
    protected void setItemDrawable(@NonNull BaseItemHolder holder, @NonNull Object item) {
        holder.mContentImageView.setImageResource((Integer) item);
    }

    @Override
    protected Drawable getChosedBackgroundDrawable() {
        return KCElementResourseHelper.getCameraAndAlbumChosedBackgroundDrawable();
    }

    @Override
    protected Drawable getBackgroundDrawable(Object item) {
        return KCElementResourseHelper.getCameraAndAlbumBackgroundDrawable();
    }
}
