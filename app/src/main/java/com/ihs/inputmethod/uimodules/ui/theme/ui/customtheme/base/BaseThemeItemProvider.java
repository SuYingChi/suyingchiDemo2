package com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.theme.HSThemeNewTipController;
import com.ihs.inputmethod.api.utils.HSDrawableUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.iap.IAPManager;
import com.ihs.inputmethod.uimodules.ui.theme.ui.view.RoundedImageView;
import com.ihs.keyboardutils.adbuffer.AdLoadingView;
import com.ihs.keyboardutils.view.HSGifImageView;
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
    protected F fragment;


    public Handler handler = new Handler(Looper.getMainLooper());
    public BaseItemHolder lastCheckedHolder;
    public Object lastCheckedItem;

    public BaseThemeItemProvider(F fragment) {
        this.fragment = fragment;
        handler = new Handler(Looper.getMainLooper());
    }

    private boolean hasDefaultItemSelectStateSet = false;

    public static class BaseItemHolder extends RecyclerView.ViewHolder {

        interface OnDownloadingProgressListener {
            void onUpdate(int percent);

            void onDownloadSucceeded();

            void onDownloadFailed();
        }

        public HSGifImageView mContentImageView;
        public RoundedImageView mCheckImageView;
        public RoundedImageView mBackgroundImageView;
        public RoundedImageView mLockedImageView;
        public RoundedImageView mNewMarkImageView;
        public RoundedImageView mPlaceholderView;
//        public SectorProgressView mProgressView;
        public View itemView;
        public OnDownloadingProgressListener downloadingProgressListener;

        public BaseItemHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;

            mContentImageView = (HSGifImageView) itemView.findViewById(R.id.custom_theme_item_content);
            mCheckImageView = (RoundedImageView) itemView.findViewById(R.id.custom_theme_item_check_bg);
            mBackgroundImageView = (RoundedImageView) itemView.findViewById(R.id.custom_theme_item_bg);
            mLockedImageView = (RoundedImageView) itemView.findViewById(R.id.custom_theme_item_locked);
            mNewMarkImageView = (RoundedImageView) itemView.findViewById(R.id.custom_theme_item_new_mark);
            mPlaceholderView = (RoundedImageView) itemView.findViewById(R.id.custom_theme_item_placeholder);
//            mProgressView = (SectorProgressView) itemView.findViewById(R.id.custom_theme_item_progress);

            mPlaceholderView.setCornerRadius(HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.custom_theme_background_item_corners_radius));
        }
    }

    @NonNull
    @Override
    protected BaseItemHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return new BaseItemHolder(inflater.inflate(R.layout.ct_item_background, null));
    }

    @Override
    protected void onBindViewHolder(@NonNull final BaseItemHolder holder, @NonNull final Object item) {
        adjustLayoutForDevice88(holder, (KCBaseElement) item);
        setItemTouchListener(holder, (I) item);
        setItemDrawable(holder, (KCBaseElement) item);
        setNewOrLockedState(holder, (KCBaseElement) item);
        updateItemSelection((V) holder, (KCBaseElement) item);
        setItemBackground(holder, (KCBaseElement) item);

    }

    protected void adjustLayoutForDevice88(@NonNull BaseItemHolder holder, KCBaseElement item) {
        holder.mCheckImageView.setImageDrawable(getChosedBackgroundDrawable());
    }


    protected void onItemClicked(final V holder, final I item, String adPlacementName) {
        if (holder == lastCheckedHolder) {
            return;
        }
        addCustomData((KCBaseElement) item);
        if (android.text.TextUtils.isEmpty(adPlacementName)) {
            checkItemBaseOnDownloadAndPurchaseSate(holder, item);
        } else {
            final KCBaseElement baseElement = (KCBaseElement) item;

            final AdLoadingView adLoadingView = new AdLoadingView(HSApplication.getContext());
            boolean hasDownloadThemeContent = baseElement.hasLocalContent();

            int delayAfterDownloadComplete = 1000;
            if (!hasDownloadThemeContent) {
                delayAfterDownloadComplete = 4000;
            }

            adLoadingView.configParams(getBackgroundDrawable(item), baseElement.getPreview(), "Applying...", "Applied Successfully", adPlacementName, new AdLoadingView.OnAdBufferingListener() {
                @Override
                public void onDismiss() {
                    if (holder.downloadingProgressListener != null) {
                        onItemDownloadSucceeded(holder, item);
                    } else {
                        showPurchaseAlertIfNeeded(item);
                        selectItem(holder, baseElement);
                        fragment.refreshKeyboardView();
                    }
                }

            }, delayAfterDownloadComplete);
            setNotNew(holder, baseElement);
            if (!hasDownloadThemeContent) {
                startDownloadContent(holder, item);
                holder.downloadingProgressListener = new BaseItemHolder.OnDownloadingProgressListener() {
                    @Override
                    public void onUpdate(int percent) {
                        HSLog.e("onUpdate +" + percent);
                        adLoadingView.updateProgressPercent(percent);
                    }

                    @Override
                    public void onDownloadSucceeded() {
                    }

                    @Override
                    public void onDownloadFailed() {
                        //// TODO: 17/4/14 applying failed ;
                    }
                };
            } else {
                adLoadingView.startFakeLoading();
            }
            adLoadingView.showInDialog(fragment.getActivity());
        }
    }

    protected void onItemClicked(final V holder, final I item, boolean showApplyAd) {
        if (showApplyAd) {
            onItemClicked(holder, item, HSApplication.getContext().getResources().getString(R.string.ad_placement_applying));
        } else {
            addCustomData((KCBaseElement) item);
            checkItemBaseOnDownloadAndPurchaseSate(holder, item);
        }
    }

    private void checkItemBaseOnDownloadAndPurchaseSate(final V holder, final I item) {
        KCBaseElement baseElement = (KCBaseElement) item;
        boolean hasDownloadThemeContent = baseElement.hasLocalContent();
        setNotNew(holder, baseElement);
        if (!hasDownloadThemeContent) {
            startDownloadContent(holder, item);
        } else {
            showPurchaseAlertIfNeeded(item);
            selectItem(holder, baseElement);
            fragment.refreshKeyboardView();
        }
    }

    private void showPurchaseAlertIfNeeded(I item) {
        if (!IAPManager.getManager().isOwnProduct((KCBaseElement) item)) {
            fragment.showPromptPurchaseView((KCBaseElement) item);
        } else {
            fragment.showPromptPurchaseView(null);
        }
    }

    protected void selectItem(BaseItemHolder holder, KCBaseElement item) {
        if (holder != lastCheckedHolder) {
            if (lastCheckedHolder != null) {
                lastCheckedHolder.mCheckImageView.setVisibility(View.INVISIBLE);
                setNewOrLockedState(lastCheckedHolder, (KCBaseElement) lastCheckedItem);
            }

            Drawable checked = getChosedBackgroundDrawable();//item.getChosedBackgroundDrawable();
            holder.mCheckImageView.setImageDrawable(checked);
            holder.mCheckImageView.setVisibility(View.VISIBLE);
            holder.mLockedImageView.setVisibility(View.INVISIBLE);
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

    private void setNewOrLockedState(@NonNull BaseItemHolder holder, @NonNull KCBaseElement item) {
        //new mark view
        if (isNewItem(item)) {
            Drawable newMarkDrawable = getNewMarkDrawable();
            if (newMarkDrawable != null) {
                holder.mNewMarkImageView.setImageDrawable(newMarkDrawable);
                holder.mNewMarkImageView.setVisibility(View.VISIBLE);
            }
        } else {
            holder.mNewMarkImageView.setVisibility(View.INVISIBLE);

            //备注 2017.04.14 取消iap弹窗 liuyu1需求
            //check is need show lock
            if (!IAPManager.getManager().isOwnProduct(item)) {
                //locked view
                Drawable locked = getLockedDrawable();
                if (locked != null) {
                    holder.mLockedImageView.setImageDrawable(locked);
                }
                holder.mLockedImageView.setVisibility(View.VISIBLE);
            } else {
                holder.mLockedImageView.setVisibility(View.INVISIBLE);
            }
        }
    }

    protected boolean isCustomThemeItemSelected(KCBaseElement item) {
        return false;
    }

    protected void updateItemSelection(@NonNull final V holder, @NonNull final KCBaseElement item) {
        if (isCustomThemeItemSelected(item)) {
            if (!hasDefaultItemSelectStateSet) {
                onItemClicked(holder, (I) item, false);
                hasDefaultItemSelectStateSet = true;
            } else {
                selectItem(holder, item);
            }
        } else {
            if ((fragment.getCustomThemeData().isElementChecked(item) && lastCheckedItem == null) || lastCheckedItem == item) {
                //设置默认选中项(刚进来的时候，和remote正在下载滑动过程当前item被复用的情况，此时holder已经赋值过了)
                if (lastCheckedHolder != null) {
                    lastCheckedHolder.mCheckImageView.setVisibility(View.INVISIBLE);
                    setNewOrLockedState(lastCheckedHolder, (KCBaseElement) lastCheckedItem);
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

    protected void setItemDrawable(@NonNull BaseItemHolder holder, @NonNull Object item) {
        //content
        Drawable drawable = ((KCBaseElement) item).getPreview();
        //如果icon不存在，则下载
        if (drawable != null) {
            //content view
            holder.mPlaceholderView.setVisibility(View.INVISIBLE);
            holder.mContentImageView.setImageDrawable(drawable);
        } else {
            holder.mPlaceholderView.setVisibility(View.VISIBLE);
            holder.mPlaceholderView.setImageDrawable(getPlaceHolderDrawable());
            holder.mLockedImageView.setVisibility(View.INVISIBLE);
            holder.mContentImageView.setImageDrawable(drawable);
            downloadPreview(holder, (KCBaseElement) item);
        }
    }


//    private boolean isCustomBackgroundType(HSCustomThemeItemBase item) {
//        return (item instanceof HSCustomThemeItemBackground)
//                && (((HSCustomThemeItemBackground) item).getCustomizedSource() == CAMERA
//                || ((HSCustomThemeItemBackground) item).getCustomizedSource() == PHOTO_ALBUM
//        );
//    }


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
                            setNewOrLockedState(holder, item);
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


    private boolean isNewItem(KCBaseElement item) {
        boolean isNewTheme = item.isNew();
        if (isNewTheme) {
            isNewTheme = HSThemeNewTipController.getInstance().isCustomThemeElementNew(item);
        }
        return isNewTheme;
    }


    private void startDownloadContent(BaseItemHolder holder, I item) {
//        holder.mProgressView.setVisibility(View.VISIBLE);
//        setLayoutParams(holder.mContentImageView, holder.mProgressView);
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
            HSThemeNewTipController.getInstance().setCustomThemeElementNotNew(item);
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
//                        holder.mProgressView.setVisibility(View.VISIBLE);
//                        holder.mProgressView.setPercent((int) (percent * 100));
//                        holder.mProgressView.postInvalidate();
                        if (holder.downloadingProgressListener != null) {
                            holder.downloadingProgressListener.onUpdate((int) (percent * 100));
                        }
                    }
                });
            }

            @Override
            public void onDownloadSucceeded(File file) {
                if (holder.downloadingProgressListener != null) {
                    holder.downloadingProgressListener.onDownloadSucceeded();
                } else {
                    onItemDownloadSucceeded(holder, item);
                }
            }

            @Override
            public void onDownloadFailed(File file) {
                if (holder.downloadingProgressListener != null) {
                    holder.downloadingProgressListener.onDownloadFailed();
                }

                resetToLastItem();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
//                        holder.mProgressView.setVisibility(View.INVISIBLE);
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
//                holder.mProgressView.setVisibility(View.INVISIBLE);
                if (fragment.getCustomThemeData() != null && fragment.getCustomThemeData().isElementChecked(item)) {
                    showPurchaseAlertIfNeeded(item);
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
                            fragment.addChosenItem((KCBaseElement) item);
                            fragment.refreshHeaderNextButtonState();
                            onItemClicked((V) holder, item, true);
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

//        Animation.AnimationListener resetAnimationListener = new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        };


        bigScale.setAnimationListener(bigScaleAnimationListener);
//        reset.setAnimationListener(resetAnimationListener);
        view.startAnimation(bigScale);
    }

    protected void doSelectAnimationOnItemViewTouch(final View view) {
        final ScaleAnimation smallScale = new ScaleAnimation(1f, 0.9f, 1f, 0.9f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        smallScale.setFillAfter(true);
        smallScale.setDuration(0);
        view.startAnimation(smallScale);
    }
}
