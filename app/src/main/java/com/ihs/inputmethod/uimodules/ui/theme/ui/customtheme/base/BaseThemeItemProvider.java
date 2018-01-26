package com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.artw.lockscreen.lockerappguide.LockerAppGuideManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.ClickUtils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.theme.HSThemeNewTipController;
import com.ihs.inputmethod.api.utils.HSDrawableUtils;
import com.ihs.inputmethod.constants.AdPlacements;
import com.ihs.inputmethod.feature.apkupdate.ApkUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.utils.LockedCardActionUtils;
import com.ihs.inputmethod.utils.HSConfigUtils;
import com.ihs.keyboardutils.adbuffer.AdLoadingView;
import com.ihs.keyboardutils.iap.RemoveAdsManager;
import com.ihs.keyboardutils.view.HSGifImageView;
import com.kc.utils.KCAnalytics;
import com.keyboard.core.mediacontroller.listeners.DownloadStatusListener;
import com.keyboard.core.themes.custom.KCCustomThemeManager;
import com.keyboard.core.themes.custom.elements.KCBaseElement;
import com.keyboard.core.themes.custom.elements.KCButtonShapeElement;
import com.keyboard.core.themes.custom.elements.KCButtonStyleElement;

import java.io.File;

import me.drakeet.multitype.ItemViewProvider;

/**
 * Created by chenyuanming on 31/10/2016.
 */

public abstract class BaseThemeItemProvider<I extends Object, V extends BaseThemeItemProvider.BaseItemHolder, F extends BaseThemeFragment> extends ItemViewProvider<Object, BaseThemeItemProvider.BaseItemHolder> {
    public Handler handler = new Handler(Looper.getMainLooper());
    public BaseItemHolder lastCheckedHolder;
    public Object lastCheckedItem;
    protected F fragment;
    private boolean hasDefaultItemSelectStateSet = false;

    public BaseThemeItemProvider(F fragment) {
        this.fragment = fragment;
        handler = new Handler(Looper.getMainLooper());
    }

    @NonNull
    @Override
    protected BaseItemHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return new BaseItemHolder(inflater.inflate(R.layout.ct_common_item, null));
    }

    @Override
    protected void onBindViewHolder(@NonNull final BaseItemHolder holder, @NonNull final Object item) {
        setItemTouchListener(holder, (I) item);
        setItemDrawable(holder, (KCBaseElement) item);
        setNewState(holder, (KCBaseElement) item);
        updateItemSelection((V) holder, (KCBaseElement) item);
        setItemBackground(holder, (KCBaseElement) item);

    }

    private void onItemClickedWithDownloading(final V holder, final I item, boolean showAd) {
        if (holder == lastCheckedHolder) {
            return;
        }
        addCustomData((KCBaseElement) item);

        if (((KCBaseElement) item).getTypeName().equals("background")) {
            KCAnalytics.logEvent("app_customize_background_background_clicked", "item", ((KCBaseElement) item).getName());
        } else if (((KCBaseElement) item).getTypeName().equals("button_style")) {
            KCAnalytics.logEvent("app_customize_button_style_clicked", "item", ((KCBaseElement) item).getName());
        } else if (((KCBaseElement) item).getTypeName().equals("button_shape")) {
            KCAnalytics.logEvent("app_customize_button_shape_clicked", "item", ((KCBaseElement) item).getName());
        } else if (((KCBaseElement) item).getTypeName().equals("font_color")) {
            KCAnalytics.logEvent("app_customize_font_color_clicked", "item", ((KCBaseElement) item).getName());
        } else if (((KCBaseElement) item).getTypeName().equals("font")) {
            KCAnalytics.logEvent("app_customize_font_font_clicked", "item", ((KCBaseElement) item).getName());
        } else if (((KCBaseElement) item).getTypeName().equals("click_sound")) {
            KCAnalytics.logEvent("app_customize_sound_clicked", "item", ((KCBaseElement) item).getName());
        }

        final KCBaseElement baseElement = (KCBaseElement) item;

        boolean hasDownloadThemeContent = baseElement.hasLocalContent();

        int delayAfterDownloadComplete = 1000;
        if (!hasDownloadThemeContent) {
            delayAfterDownloadComplete = 4000;
        }

        Drawable backgroundDrawable = getBackgroundDrawable(item);
        if (backgroundDrawable == null) {
            backgroundDrawable = new ColorDrawable(Color.BLACK);
        }

        setNotNew(holder, baseElement);
        if (!hasDownloadThemeContent) {
            final AdLoadingView adLoadingView = new AdLoadingView(fragment.getActivity());
            adLoadingView.configParams(backgroundDrawable, null,
                    HSApplication.getContext().getResources().getString(R.string.theme_card_downloading_tip),
                    HSApplication.getContext().getResources().getString(R.string.interstitial_ad_title_after_try_keyboard),
                    AdPlacements.NATIVE_APPLYING_ITEM,
                    new AdLoadingView.OnAdBufferingListener() {

                        @Override
                        public void onDismiss(boolean success, boolean manually) {
                            if (holder.downloadingProgressListener != null) {
                                onItemDownloadSucceeded(holder, item);
                            } else {
                                selectItem(holder, baseElement);
                                fragment.refreshKeyboardView();
                            }
                        }

                    }, delayAfterDownloadComplete, (RemoveAdsManager.getInstance().isRemoveAdsPurchased() || !showAd));

            startDownloadContent(holder, item);
            holder.downloadingProgressListener = new BaseItemHolder.OnDownloadingProgressListener() {
                @Override
                public void onUpdate(int percent) {
                    adLoadingView.updateProgressPercent(percent);
                }

                @Override
                public void onDownloadSucceeded() {
                }

                @Override
                public void onDownloadFailed() {
                    adLoadingView.setConnectionStateText(HSApplication.getContext().getString(R.string.foreground_download_failed));
                    adLoadingView.updateProgressPercent(0);
                }
            };
            Glide.with(HSApplication.getContext()).asBitmap().apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)).load(baseElement.getPreviewFileUrl()).into((ImageView) adLoadingView.findViewById(R.id.iv_icon));
            adLoadingView.showInDialog();
        } else {
            selectItem(holder, baseElement);
            fragment.refreshKeyboardView();
        }
    }

    protected void onItemClicked(final V holder, final I item, boolean showApplyAd) {
        onItemClickedWithDownloading(holder, item, showApplyAd);
    }

    private void checkItemBaseOnDownloadAndPurchaseSate(final V holder, final I item) {
        KCBaseElement baseElement = (KCBaseElement) item;
        boolean hasDownloadThemeContent = baseElement.hasLocalContent();
        setNotNew(holder, baseElement);
        if (!hasDownloadThemeContent) {
            startDownloadContent(holder, item);
        } else {
            selectItem(holder, baseElement);
            fragment.refreshKeyboardView();
        }
    }

    protected void selectItem(BaseItemHolder holder, KCBaseElement item) {
        if (holder != lastCheckedHolder) {
            if (lastCheckedHolder != null) {
                lastCheckedHolder.mCheckImageView.setVisibility(View.INVISIBLE);
                setNewState(lastCheckedHolder, (KCBaseElement) lastCheckedItem);
            }

            Drawable checked = getChosedBackgroundDrawable();//item.getChosedBackgroundDrawable();
            holder.mCheckImageView.setImageDrawable(checked);
            holder.mCheckImageView.setVisibility(View.VISIBLE);
            lastCheckedHolder = holder;
            lastCheckedItem = item;
        }
    }

    protected Drawable getChosedBackgroundDrawable() {
        return null;
    }

    protected Drawable getNewMarkDrawable() {
        return null;
    }

    protected Drawable getLockedDrawable() {
        return null;
    }

    protected Drawable getBackgroundDrawable(Object item) {
        return null;
    }

    protected Drawable getDarkerBackgroundDrawable() {
        return null;
    }

    protected Drawable getPlaceHolderDrawable() {
        return HSDrawableUtils.getTransparentBitmapDrawable();
    }

    private void setNewState(@NonNull BaseItemHolder holder, @NonNull KCBaseElement item) {
        //new mark view
        if (item.isNew() || HSConfigUtils.toBoolean(item.getConfigData().get("needNewVersionToUnlock"), false)) {
            Drawable newMarkDrawable = getNewMarkDrawable();
            if (newMarkDrawable != null) {
                holder.mNewMarkImageView.setImageDrawable(newMarkDrawable);
                holder.mNewMarkImageView.setVisibility(View.VISIBLE);
            }
        } else {
            holder.mNewMarkImageView.setVisibility(View.INVISIBLE);
            if (!item.hasLocalContent()
                    && ((LockerAppGuideManager.getInstance().shouldGuideToDownloadLocker() && (HSConfigUtils.toBoolean(item.getConfigData().get("downloadLockerToUnlock"), false)))
                    ||(HSConfigUtils.toBoolean(item.getConfigData().get("rateToUnlock"), false)
                    && ApkUtils.shouldShowRateAlert())
                    || (HSConfigUtils.toBoolean(item.getConfigData().get("shareToUnlock"), false)
                    && ApkUtils.isInstagramInstalled() && !ApkUtils.isSharedKeyboardOnInstagramBefore()))) {
                holder.mGiftIconImageView.setVisibility(View.VISIBLE);
            } else {
                holder.mGiftIconImageView.setVisibility(View.GONE);
            }
        }
    }

    protected boolean isCustomThemeItemSelected(KCBaseElement item) {
        return false;
    }

    protected void updateItemSelection(@NonNull final V holder, @NonNull final KCBaseElement item) {
        if (isCustomThemeItemSelected(item)) {
            if (!hasDefaultItemSelectStateSet) {
                addCustomData(item);
                checkItemBaseOnDownloadAndPurchaseSate(holder, (I) item);
                hasDefaultItemSelectStateSet = true;
            } else {
                selectItem(holder, item);
            }
        } else {
            if ((fragment.getCustomThemeData().isElementChecked(item) && lastCheckedItem == null) || lastCheckedItem == item) {
                //设置默认选中项(刚进来的时候，和remote正在下载滑动过程当前item被复用的情况，此时holder已经赋值过了)
                if (lastCheckedHolder != null) {
                    lastCheckedHolder.mCheckImageView.setVisibility(View.INVISIBLE);
                    setNewState(lastCheckedHolder, (KCBaseElement) lastCheckedItem);
                }
                Drawable checked = getChosedBackgroundDrawable();
                holder.mCheckImageView.setImageDrawable(checked);
                holder.mCheckImageView.setVisibility(View.VISIBLE);
                lastCheckedHolder = holder;
                lastCheckedItem = item;
            } else {
                holder.mCheckImageView.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * 记录当前选中的item
     * 1. 相机相册操作成功后记录
     * 2. local 直接记录
     * 3. remote 也直接记录，点击next的时候及时没下载完成也需要弹出iap
     *
     * @param item
     */
    protected void addCustomData(@NonNull KCBaseElement item) {
        fragment.getCustomThemeData().setElement(item);
    }

    protected void setItemBackground(@NonNull BaseItemHolder holder, @NonNull KCBaseElement item) {
        //background
        Drawable background = getBackgroundDrawable(item);
        holder.mBackgroundImageView.setImageDrawable(background);
        if (background != null) {
            holder.mBackgroundImageView.setVisibility(View.VISIBLE);
        } else {
            holder.mBackgroundImageView.setVisibility(View.INVISIBLE);
        }

    }

    protected void setItemDrawable(@NonNull final BaseItemHolder holder, @NonNull final Object obj) {
        final KCBaseElement item = (KCBaseElement) obj;

        holder.mPlaceholderView.setVisibility(View.VISIBLE);
        holder.mPlaceholderView.setImageDrawable(getPlaceHolderDrawable());
        Glide.with(HSApplication.getContext()).asBitmap().apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)).listener(new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                holder.mContentImageView.setImageDrawable(null);
                downloadPreview(holder, item);
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                holder.mPlaceholderView.setVisibility(View.GONE);
                holder.mContentImageView.setImageBitmap(resource);
                return false;
            }
        }).load(item.getPreviewFileUrl()).into(holder.mContentImageView);
    }

    /**
     * 下载预览图
     *
     * @param item
     */
    private void downloadPreview(final BaseItemHolder holder, final KCBaseElement item) {
        KCCustomThemeManager.getInstance().downloadElementResource(item, new DownloadStatusListener() {
            private int currentPosition = holder.getAdapterPosition();

            @Override
            public void onDownloadProgress(File file, float percent) {

            }

            @Override
            public void onDownloadSucceeded(File file) {
                if (currentPosition == holder.getAdapterPosition()) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            setItemDrawable(holder, item);
                            setNewState(holder, item);
                        }
                    });
                }
            }

            @Override
            public void onDownloadFailed(File file) {
                HSLog.d("BaseThemeItemProvider", "onDownloadFailed() called with: file = [" + file + "]");
            }
        }, true /* isPreview */);
    }


    private void startDownloadContent(BaseItemHolder holder, I item) {
        downloadContent(holder, item);
    }

    protected void setLayoutParams(final HSGifImageView mContentImageView, final View view) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                int height = mContentImageView.getMeasuredHeight();
                int width = mContentImageView.getMeasuredWidth();
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
                view.setLayoutParams(params);
            }
        });
    }

    private void setNotNew(V holder, KCBaseElement item) {
        if (item.isNew()) {
            holder.mNewMarkImageView.setImageDrawable(null);
            HSThemeNewTipController.getInstance().setElementNotNew(item);
            item.setNew(false);
        }
    }

    /**
     * 下载主题内容，background下载的是背景的zip，里面包含大图（也可能包含mp4背景）；font下载的是ttf的文件
     *
     * @param holder
     * @param item
     */
    private void downloadContent(final BaseItemHolder holder, final I item) {
        holder.setIsRecyclable(false);

        KCCustomThemeManager.getInstance().downloadElementResource((KCBaseElement) item, new DownloadStatusListener() {
            @Override
            public void onDownloadProgress(File file, final float percent) {
                //没有被复用
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (holder.downloadingProgressListener != null) {
                            holder.downloadingProgressListener.onUpdate((int) (percent * 100));
                        }
                    }
                });
            }

            @Override
            public void onDownloadSucceeded(File file) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (holder.downloadingProgressListener != null) {
                            holder.downloadingProgressListener.onDownloadSucceeded();
                        } else {
                            onItemDownloadSucceeded(holder, item);
                        }
                    }
                });
            }

            @Override
            public void onDownloadFailed(File file) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (holder.downloadingProgressListener != null) {
                            holder.downloadingProgressListener.onDownloadFailed();
                        }
                        resetToLastItem();
                        holder.setIsRecyclable(true);
                    }
                });
            }
        }, false /* isPreview */);
    }

    private void onItemDownloadSucceeded(final BaseItemHolder holder, final I item) {
        fragment.refreshHeaderNextButtonState();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (fragment.getCustomThemeData() != null && fragment.getCustomThemeData().isElementChecked(item)) {
                    selectItem(holder, (KCBaseElement) item);
                    fragment.refreshKeyboardView();
                } else {
                    holder.mCheckImageView.setVisibility(View.INVISIBLE);
                }
                holder.setIsRecyclable(true);
            }
        });
        HSGlobalNotificationCenter.sendNotificationOnMainThread(KCCustomThemeManager.NOTIFICATION_CUSTOM_THEME_CONTENT_DOWNLOAD_FINISHED);
    }

    protected void resetToLastItem() {
        if (lastCheckedItem != null) {
            addCustomData((KCBaseElement) lastCheckedItem);
            fragment.notifyAdapterOnMainThread();
        }
    }

    protected void setItemTouchListener(@NonNull final BaseItemHolder holder, @NonNull final I item) {
        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean isClickDisabled = item instanceof KCButtonStyleElement && fragment.getCustomThemeData().getButtonShapeElement().getName().equalsIgnoreCase("none");
                if (!isClickDisabled) {
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
                                    && LockerAppGuideManager.getInstance().shouldGuideToDownloadLocker()
                                    && HSConfigUtils.toBoolean(baseElement.getConfigData().get("downloadLockerToUnlock"), false)) {
                                LockerAppGuideManager.getInstance().showDownloadLockerAlert(fragment.getActivity(), LockerAppGuideManager.FLURRY_ALERT_UNLOCK);
                                return true;
                            }

                            if (!baseElement.hasLocalContent()
                                    && HSConfigUtils.toBoolean(baseElement.getConfigData().get("needNewVersionToUnlock"), false)
                                    && ApkUtils.isNewVersionAvailable()) {
                                holder.mNewMarkImageView.setVisibility(View.GONE);
                                ApkUtils.showCustomUpdateAlert(LockedCardActionUtils.LOCKED_CARD_FROM_CUSTOMIZE);
                                return true;
                            }

                            if (!baseElement.hasLocalContent()
                                    && HSConfigUtils.toBoolean(baseElement.getConfigData().get("rateToUnlock"), false)) {
                                if (ApkUtils.showCustomRateAlert(LockedCardActionUtils.LOCKED_CARD_FROM_CUSTOMIZE, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (holder.mGiftIconImageView.getVisibility() == View.VISIBLE) {
                                            holder.mGiftIconImageView.setVisibility(View.GONE);
                                        }
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                fragment.addChosenItem((KCBaseElement) item);
                                                fragment.refreshHeaderNextButtonState();
                                                onItemClicked((V) holder, item, false);
                                                if (item instanceof KCButtonShapeElement) {
                                                    fragment.notifyAdapterOnMainThread();//shape选择none以后，需要刷新style为不可用
                                                }
                                            }
                                        }, 1000);
                                    }
                                })) {
                                    return true;
                                }
                            }

                            if (!baseElement.hasLocalContent()
                                    && HSConfigUtils.toBoolean(baseElement.getConfigData().get("shareToUnlock"), false)
                                    && ApkUtils.isInstagramInstalled()
                                    && !ApkUtils.isSharedKeyboardOnInstagramBefore()) {
                                ApkUtils.showCustomShareAlert(LockedCardActionUtils.LOCKED_CARD_FROM_CUSTOMIZE, fragment.getActivity(), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (holder.mGiftIconImageView.getVisibility() == View.VISIBLE) {
                                            holder.mGiftIconImageView.setVisibility(View.GONE);
                                        }
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                fragment.addChosenItem((KCBaseElement) item);
                                                fragment.refreshHeaderNextButtonState();
                                                onItemClicked((V) holder, item, false);
                                                if (item instanceof KCButtonShapeElement) {
                                                    fragment.notifyAdapterOnMainThread();//shape选择none以后，需要刷新style为不可用
                                                }
                                            }
                                        }, 1000);
                                    }
                                });
                                return true;
                            }

                            boolean showApplyAd = true;
                            if (holder.mGiftIconImageView.getVisibility() == View.VISIBLE) {
                                holder.mGiftIconImageView.setVisibility(View.GONE);
                                showApplyAd = false;
                            }
                            fragment.addChosenItem((KCBaseElement) item);
                            fragment.refreshHeaderNextButtonState();
                            onItemClicked((V) holder, item, showApplyAd);
                            if (item instanceof KCButtonShapeElement) {
                                fragment.notifyAdapterOnMainThread();//shape选择none以后，需要刷新style为不可用
                            }
                            return true;
                        case MotionEvent.ACTION_CANCEL:
                            doSelectAnimationOnItemViewRelease(v);
                            return true;
                    }
                }
                return false;
            }
        });
    }

    protected void doSelectAnimationOnItemViewRelease(final View view) {
        final ScaleAnimation bigScale = new ScaleAnimation(0.9f, 1.05f, 0.9f, 1.05f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        bigScale.setDuration(120);

        final ScaleAnimation reset = new ScaleAnimation(1.05f, 1f, 1.05f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        reset.setDuration(80);

        Animation.AnimationListener bigScaleAnimationListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.startAnimation(reset);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };

        bigScale.setAnimationListener(bigScaleAnimationListener);
        view.startAnimation(bigScale);
    }

    protected void doSelectAnimationOnItemViewTouch(final View view) {
        final ScaleAnimation smallScale = new ScaleAnimation(1f, 0.9f, 1f, 0.9f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        smallScale.setFillAfter(true);
        smallScale.setDuration(0);
        view.startAnimation(smallScale);
    }

    public static class BaseItemHolder extends RecyclerView.ViewHolder {

        public HSGifImageView mGifView;
        public ImageView mContentImageView;
        public ImageView mCheckImageView;
        public ImageView mBackgroundImageView;
        public ImageView mNewMarkImageView;
        public ImageView mPlaceholderView;
        public ImageView mGiftIconImageView;
        public View itemView;
        public OnDownloadingProgressListener downloadingProgressListener;

        public BaseItemHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;

            mGifView = (HSGifImageView) itemView.findViewById(R.id.custom_theme_item_gif_content);
            mContentImageView = (ImageView) itemView.findViewById(R.id.custom_theme_item_content);
            mCheckImageView = (ImageView) itemView.findViewById(R.id.custom_theme_item_check_bg);
            mBackgroundImageView = (ImageView) itemView.findViewById(R.id.custom_theme_item_bg);
            mNewMarkImageView = (ImageView) itemView.findViewById(R.id.custom_theme_item_new_mark);
            mGiftIconImageView = (ImageView) itemView.findViewById(R.id.custom_theme_item_gift_icon);
            mPlaceholderView = (ImageView) itemView.findViewById(R.id.custom_theme_item_placeholder);

        }

        interface OnDownloadingProgressListener {
            void onUpdate(int percent);

            void onDownloadSucceeded();

            void onDownloadFailed();
        }
    }

}
