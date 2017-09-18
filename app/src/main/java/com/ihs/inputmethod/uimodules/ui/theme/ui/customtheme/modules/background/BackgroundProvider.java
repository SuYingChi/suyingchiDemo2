package com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.modules.background;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.chargingscreen.utils.ClickUtils;
import com.ihs.inputmethod.feature.apkupdate.ApkUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base.BaseThemeItemProvider;
import com.ihs.inputmethod.utils.HSConfigUtils;
import com.keyboard.core.themes.custom.KCCustomThemeData;
import com.keyboard.core.themes.custom.KCElementResourseHelper;
import com.keyboard.core.themes.custom.elements.KCBackgroundElement;
import com.keyboard.core.themes.custom.elements.KCBaseElement;

import java.io.File;

/**
 * Created by chenyuanming on 31/10/2016.
 */

public class BackgroundProvider extends BaseThemeItemProvider<KCBackgroundElement, BaseThemeItemProvider.BaseItemHolder, BackgroundFragment> {
    private Drawable choosingDrawable;
    private Drawable newMarkDrawable;
    private boolean hasDefaultItemSelectStateSet = false;

    public BackgroundProvider(BackgroundFragment fragment) {
        super(fragment);
    }

    protected void addCustomData(KCBaseElement item) {
        fragment.addChosenItem(item);
        fragment.refreshHeaderNextButtonState();
        fragment.getCustomThemeData().setElement(item);
    }

    @Override
    protected Drawable getChosedBackgroundDrawable() {
        if (choosingDrawable == null) {
            choosingDrawable = KCElementResourseHelper.getBackgroundChosedBackgroundDrawable();
        }
        return choosingDrawable;
    }

    @Override
    protected Drawable getLockedDrawable() {
        return KCElementResourseHelper.getBackgroundLockedDrawable();

    }

    @Override
    protected Drawable getNewMarkDrawable() {
        if (newMarkDrawable == null) {
            newMarkDrawable = KCElementResourseHelper.getBackgroundNewMarkDrawable();
        }
        return newMarkDrawable;
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

    @NonNull
    @Override
    protected BaseItemHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        BaseItemHolder holder = new BaseItemHolder(inflater.inflate(R.layout.ct_item_background, null));

        int margin = holder.itemView.getResources().getDimensionPixelSize(R.dimen.custom_theme_item_margin);
        DisplayMetrics displayMetrics = holder.itemView.getResources().getDisplayMetrics();
        int width = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels) / fragment.SPAN_COUNT - margin * 2;
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(width,width);
        holder.itemView.setLayoutParams(layoutParams);
        return holder;
    }

    @Override
    protected void onBindViewHolder(@NonNull BaseItemHolder holder, @NonNull Object item) {
        super.onBindViewHolder(holder, item);
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
        KCBackgroundElement backgroundElement = (KCBackgroundElement) item;
        if (backgroundElement.hasLocalGifPreview()) {
            holder.mPlaceholderView.setVisibility(View.GONE);
            holder.mGifView.setVisibility(View.VISIBLE);
            holder.mContentImageView.setImageDrawable(null);
            holder.mGifView.setImageURI(Uri.fromFile(new File(backgroundElement.getGifPreview())));
        }else {
            super.setItemDrawable(holder, item);
            holder.mGifView.setImageDrawable(null);
            holder.mGifView.setVisibility(View.GONE);
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
                        final KCBaseElement baseElement = (KCBaseElement) item;
                        if (ClickUtils.isFastDoubleClick()) {
                            return true;
                        }

                        if (!baseElement.hasLocalContent()
                                && HSConfigUtils.toBoolean(baseElement.getData().get("needNewVersionToUnlock"), false)
                                && ApkUtils.shouldUpdate()) {
                            ApkUtils.showUpdateAlert();
                            return true;
                        }

                        if (!baseElement.hasLocalContent()
                                &&HSConfigUtils.toBoolean(baseElement.getData().get("rateToUnlock"), false)
                                && ApkUtils.isGooglePlayAvailable()
                                && !ApkUtils.isRateAlertButtonClickedInCurrentAppVersion()) {
                            ApkUtils.showCustomRateAlert(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (holder.mGiftIconImageView.getVisibility() == View.VISIBLE) {
                                        holder.mGiftIconImageView.setVisibility(View.GONE);
                                    }
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            fragment.addChosenItem(item);
                                            fragment.refreshHeaderNextButtonState();
                                            onItemClicked(holder, item, false);
                                        }
                                    }, 1000);
                                }
                            });
                            return true;
                        }
                        if (holder.mGiftIconImageView.getVisibility() == View.VISIBLE) {
                            holder.mGiftIconImageView.setVisibility(View.GONE);
                            fragment.addChosenItem(item);
                            fragment.refreshHeaderNextButtonState();
                            onItemClicked(holder, item, false);
                            return true;
                        }
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
