package com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.artw.lockscreen.lockerappguide.LockerAppGuideManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.ClickUtils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.theme.HSThemeNewTipController;
import com.ihs.inputmethod.feature.apkupdate.ApkUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeActivity;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity;
import com.ihs.inputmethod.uimodules.ui.theme.ui.decoration.BackgroundItemDecoration;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.ThemeHomeModel;
import com.ihs.inputmethod.uimodules.ui.theme.utils.CompatUtils;
import com.ihs.inputmethod.uimodules.ui.theme.utils.LockedCardActionUtils;
import com.ihs.inputmethod.utils.HSConfigUtils;
import com.ihs.keyboardutils.nativeads.KCNativeAdView;
import com.kc.utils.KCAnalytics;
import com.keyboard.core.mediacontroller.listeners.DownloadStatusListener;
import com.keyboard.core.themes.custom.KCCustomThemeManager;
import com.keyboard.core.themes.custom.KCElementResourseHelper;
import com.keyboard.core.themes.custom.elements.KCBackgroundElement;
import com.keyboard.core.themes.custom.elements.KCBaseElement;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.view.View.GONE;
import static com.ihs.keyboardutils.iap.RemoveAdsManager.NOTIFICATION_REMOVEADS_PURCHASED;

/**
 * Created by wenbinduan on 2016/12/22.
 */

public final class ThemeBackgroundAdapterDelegate extends AdapterDelegate<List<ThemeHomeModel>> {

    private static final int BACKGROUND_COLUMN_NUM = 6;
    private Activity activity;


    public ThemeBackgroundAdapterDelegate(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected boolean isForViewType(@NonNull List<ThemeHomeModel> items, int position) {
        return items.get(position).isBackground;
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        RecyclerView recyclerView = (RecyclerView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_theme_backgrounds, parent, false);
        recyclerView.addItemDecoration(new BackgroundItemDecoration(HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.theme_store_background_recycler_view_row_gap),
                HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.theme_store_background_recycler_view_column_gap)));
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext(), LinearLayoutManager.HORIZONTAL, false));
        ThemeBackgroundAdapter adapter = new ThemeBackgroundAdapter(activity);
        recyclerView.setAdapter(adapter);
        adapter.updateBackgroundList();
        return new ThemeBackgroundViewHolder(recyclerView);
    }

    @Override
    protected void onBindViewHolder(@NonNull List<ThemeHomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {

    }

    @Override
    protected void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
//        ThemeBackgroundViewHolder viewHolder = (ThemeBackgroundViewHolder) holder;
//        viewHolder.recyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public int getSpanSize(List<ThemeHomeModel> items, int position) {
        return 2;
    }


    static class ThemeBackgroundAdapter extends RecyclerView.Adapter<ThemeBackgroundAdapter.Holder> {
        private Activity activity;
        private int portraitScreenWidth;
        private final RequestOptions requestOptions;
        private List<Object> backgrounds = new ArrayList<>();
        private Map<String, KCNativeAdView> backgroundNativeAdViews = new HashMap<>();
        private final INotificationObserver notificationObserver = new INotificationObserver() {
            @Override
            public void onReceive(String s, HSBundle hsBundle) {
                if (KCCustomThemeManager.NOTIFICATION_KEY_CUSTOM_THEME_ELEMENT_CHANGED.equals(s)) {
                    updateBackgroundList();
                } else if (ThemeHomeFragment.NOTIFICATION_THEME_HOME_DESTROY.equals(s)) {
                    release();
                    HSGlobalNotificationCenter.removeObserver(notificationObserver);
                } else if(NOTIFICATION_REMOVEADS_PURCHASED.equals(s)) {
                    Iterator<Object> iterator = backgrounds.iterator();
                    while (iterator.hasNext()) {
                        Object obj = iterator.next();
                        if(obj instanceof NativeAdInfo) {
                            NativeAdInfo nativeAdInfo = (NativeAdInfo) obj;
                            if (nativeAdInfo.hasAd) {
                                iterator.remove();
                            }
                        }
                    }
                    notifyDataSetChanged();
                }

            }
        };
        private int itemDimension;

        public ThemeBackgroundAdapter(Activity activity) {
            this.activity = activity;
            int widthPixels = HSApplication.getContext().getResources().getDisplayMetrics().widthPixels;
            int heightPixels = HSApplication.getContext().getResources().getDisplayMetrics().heightPixels;
            requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE);
            portraitScreenWidth = widthPixels < heightPixels ? widthPixels : heightPixels;
            HSGlobalNotificationCenter.addObserver(KCCustomThemeManager.NOTIFICATION_KEY_CUSTOM_THEME_ELEMENT_CHANGED, notificationObserver);
            HSGlobalNotificationCenter.addObserver(ThemeHomeFragment.NOTIFICATION_THEME_HOME_DESTROY, notificationObserver);
            HSGlobalNotificationCenter.addObserver(NOTIFICATION_REMOVEADS_PURCHASED, notificationObserver);
        }

        public void release() {
            Iterator<Object> iterator = backgrounds.iterator();
            while (iterator.hasNext()) {
                Object obj = iterator.next();
                if(obj instanceof NativeAdInfo) {
                    NativeAdInfo nativeAdInfo = (NativeAdInfo) obj;
                    if (nativeAdInfo.hasAd) {
                        nativeAdInfo.nativeAdView.release();
                    }
                }
            }
            activity = null;
        }

        private void destroyCurrentData() {
            backgroundNativeAdViews.clear();
            backgrounds.clear();
        }

        void updateBackgroundList() {
            destroyCurrentData();
            backgrounds.addAll(KCCustomThemeManager.getInstance().getBackgroundHomeElements());
            notifyDataSetChanged();
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_theme_background, parent, false);
            ViewGroup.LayoutParams layoutParams = item.getLayoutParams();
            itemDimension = (portraitScreenWidth - parent.getPaddingLeft() - parent.getPaddingRight()) / BACKGROUND_COLUMN_NUM;
            layoutParams.width = itemDimension;
            layoutParams.height = itemDimension;
            item.setLayoutParams(layoutParams);
            return new Holder(item);
        }

        private void startCustomThemeActivity(Bundle bundle) {
            if (activity instanceof ThemeHomeActivity) {
                ((ThemeHomeActivity) activity).showCustomThemeActivity(bundle);
            }
        }

        @Override
        public void onBindViewHolder(final Holder holder, final int position) {
            removeNativeAdViewFromHolder(holder);

            if (position == 0) {
                holder.backgroundContent.setVisibility(View.VISIBLE);
                holder.backgroundContent.setImageResource(R.drawable.image_placeholder);
                Glide.with(HSApplication.getContext()).asBitmap().apply(requestOptions).load(R.drawable.camera_icon).into(holder.backgroundContent);
                holder.backgroundNewMark.setVisibility(GONE);
                holder.backgroundGiftIcon.setVisibility(GONE);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        String customEntry = "store_camera";
                        bundle.putBoolean(CustomThemeActivity.BUNDLE_KEY_BACKGROUND_USE_CAMERA, true);
                        bundle.putString(CustomThemeActivity.BUNDLE_KEY_CUSTOMIZE_ENTRY, customEntry);
                        startCustomThemeActivity(bundle);
                        KCAnalytics.logEvent("shortcut_customize_background_clicked", "camera");

                    }
                });
            } else if (position == 1) {
                holder.backgroundContent.setVisibility(View.VISIBLE);
                holder.backgroundContent.setImageResource(R.drawable.image_placeholder);
                Glide.with(HSApplication.getContext()).asBitmap().apply(requestOptions).load(R.drawable.gallery_icon).into(holder.backgroundContent);
                holder.backgroundNewMark.setVisibility(GONE);
                holder.backgroundGiftIcon.setVisibility(GONE);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        String customEntry = "store_album";
                        bundle.putBoolean(CustomThemeActivity.BUNDLE_KEY_BACKGROUND_USE_GALLERY, true);
                        bundle.putString(CustomThemeActivity.BUNDLE_KEY_CUSTOMIZE_ENTRY, customEntry);
                        startCustomThemeActivity(bundle);
                        KCAnalytics.logEvent("shortcut_customize_background_clicked", "album");
                    }
                });
            } else {

                if (backgrounds.get(position) instanceof KCBackgroundElement) {
                    holder.backgroundContent.setVisibility(View.VISIBLE);
                    holder.backgroundNewMark.setVisibility(View.VISIBLE);
                    final KCBackgroundElement customThemeItemBase = (KCBackgroundElement) backgrounds.get(position);
                    holder.backgroundContent.setImageResource(R.drawable.image_placeholder);

                    boolean hasLocalGifPreview = customThemeItemBase.hasLocalGifPreview();
                    boolean hasLocalPreview = customThemeItemBase.hasLocalPreview();

                    if (hasLocalGifPreview || hasLocalPreview) {
                        if (hasLocalGifPreview) {
                            holder.backgroundContent.setImageURI(Uri.fromFile(new File(customThemeItemBase.getGifPreview())));
                        } else if (hasLocalPreview) {
                            holder.backgroundContent.setImageBitmap(null);
                            Glide.with(HSApplication.getContext()).asBitmap().apply(requestOptions).load(customThemeItemBase.getPreviewFileUrl()).into(holder.backgroundContent);
                        }
                        if (customThemeItemBase.isNew() || HSConfigUtils.toBoolean(customThemeItemBase.getConfigData().get("needNewVersionToUnlock"), false)) {
                            Drawable newMarkDrawable = KCElementResourseHelper.getBackgroundNewMarkDrawable();
                            if (newMarkDrawable != null) {
                                holder.backgroundNewMark.setImageDrawable(newMarkDrawable);
                                holder.backgroundNewMark.setVisibility(View.VISIBLE);
                            }
                        } else {
                            holder.backgroundNewMark.setImageDrawable(null);
                            holder.backgroundNewMark.setVisibility(GONE);
                            if (!customThemeItemBase.hasLocalContent()
                                    && ((LockerAppGuideManager.getInstance().shouldGuideToDownloadLocker() && (HSConfigUtils.toBoolean(customThemeItemBase.getConfigData().get("downloadLockerToUnlock"), false)))
                                    ||(HSConfigUtils.toBoolean(customThemeItemBase.getConfigData().get("rateToUnlock"), false)
                                    && ApkUtils.shouldShowRateAlert())
                                    || (HSConfigUtils.toBoolean(customThemeItemBase.getConfigData().get("shareToUnlock"), false)
                                    && ApkUtils.isInstagramInstalled() && !ApkUtils.isSharedKeyboardOnInstagramBefore()))) {
                                holder.backgroundGiftIcon.setVisibility(View.VISIBLE);
                            } else {
                                holder.backgroundGiftIcon.setVisibility(View.GONE);
                            }
                        }
                    } else {
                        downloadPreview(holder.itemView, position, customThemeItemBase);
                    }
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (ClickUtils.isFastDoubleClick()) {
                                return;
                            }

                            if (!customThemeItemBase.hasLocalContent()
                                    && LockerAppGuideManager.getInstance().shouldGuideToDownloadLocker()
                                    && HSConfigUtils.toBoolean(customThemeItemBase.getConfigData().get("downloadLockerToUnlock"), false)) {
                                LockerAppGuideManager.getInstance().showDownloadLockerAlert(activity, LockerAppGuideManager.FLURRY_ALERT_UNLOCK);
                                return;
                            }


                            if (!customThemeItemBase.hasLocalContent()
                                    && HSConfigUtils.toBoolean(customThemeItemBase.getConfigData().get("needNewVersionToUnlock"), false)
                                    && ApkUtils.isNewVersionAvailable()) {
                                holder.backgroundNewMark.setVisibility(GONE);
                                ApkUtils.showCustomUpdateAlert(LockedCardActionUtils.LOCKED_CARD_FROM_HOME_BACKGROUND);
                                return;
                            }

                            if (!customThemeItemBase.hasLocalContent()
                                    && HSConfigUtils.toBoolean(customThemeItemBase.getConfigData().get("rateToUnlock"), false)) {
                                if (ApkUtils.showCustomRateAlert(LockedCardActionUtils.LOCKED_CARD_FROM_HOME_BACKGROUND, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (holder.backgroundGiftIcon.getVisibility() == View.VISIBLE) {
                                            holder.backgroundGiftIcon.setVisibility(GONE);
                                        }
                                        notifyDataSetChanged();
                                        holder.setIsRecyclable(true);
                                        KCBackgroundElement background = (KCBackgroundElement) backgrounds.get(position);
                                        setNotNew(background);

                                        Bundle bundle = new Bundle();
                                        String customEntry = "store_bg";
                                        String backgroundItemName = background.getName();
                                        bundle.putString(CustomThemeActivity.BUNDLE_KEY_BACKGROUND_NAME, backgroundItemName);
                                        bundle.putString(CustomThemeActivity.BUNDLE_KEY_CUSTOMIZE_ENTRY, customEntry);
                                        startCustomThemeActivity(bundle);
                                    }
                                })) {
                                    return;
                                }
                            }

                            if (!customThemeItemBase.hasLocalContent()
                                    && HSConfigUtils.toBoolean(customThemeItemBase.getConfigData().get("shareToUnlock"), false)
                                    && ApkUtils.isInstagramInstalled()
                                    && !ApkUtils.isSharedKeyboardOnInstagramBefore()) {
                                ApkUtils.showCustomShareAlert(LockedCardActionUtils.LOCKED_CARD_FROM_HOME_BACKGROUND, activity, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (holder.backgroundGiftIcon.getVisibility() == View.VISIBLE) {
                                            holder.backgroundGiftIcon.setVisibility(GONE);
                                        }
                                        notifyDataSetChanged();
                                        holder.setIsRecyclable(true);
                                        KCBackgroundElement background = (KCBackgroundElement) backgrounds.get(position);
                                        setNotNew(background);

                                        Bundle bundle = new Bundle();
                                        String customEntry = "store_bg";
                                        String backgroundItemName = background.getName();
                                        bundle.putString(CustomThemeActivity.BUNDLE_KEY_BACKGROUND_NAME, backgroundItemName);
                                        bundle.putString(CustomThemeActivity.BUNDLE_KEY_CUSTOMIZE_ENTRY, customEntry);
                                        startCustomThemeActivity(bundle);
                                    }
                                });
                                return;
                            }

                            holder.setIsRecyclable(true);
                            KCBackgroundElement background = (KCBackgroundElement) backgrounds.get(position);
                            setNotNew(background);
                            notifyItemChanged(holder.getAdapterPosition());

                            Bundle bundle = new Bundle();
                            String customEntry = "store_bg";
                            String backgroundItemName = background.getName();
                            bundle.putString(CustomThemeActivity.BUNDLE_KEY_BACKGROUND_NAME, backgroundItemName);
                            bundle.putString(CustomThemeActivity.BUNDLE_KEY_CUSTOMIZE_ENTRY, customEntry);
                            startCustomThemeActivity(bundle);
                            KCAnalytics.logEvent("shortcut_customize_background_clicked", background.getName());
                        }
                    });
                } else if (backgrounds.get(position) instanceof NativeAdInfo) {
                    holder.backgroundContent.setVisibility(View.INVISIBLE);
                    holder.backgroundNewMark.setVisibility(View.INVISIBLE);
                    NativeAdInfo nativeAdInfo = (NativeAdInfo) backgrounds.get(position);
                    addNativeAdViewToHolder(holder, nativeAdInfo);
                }
            }
        }

        private void removeNativeAdViewFromHolder(final Holder holder) {
            View nativeAdView = holder.itemView.findViewWithTag("nativeadview");
            if (nativeAdView != null) {
                ((ViewGroup) holder.itemView).removeView(nativeAdView);
            }
        }

        private void setItemVisibilitya(View itemView, boolean isVisible) {
            RecyclerView.LayoutParams param = (RecyclerView.LayoutParams) itemView.getLayoutParams();
            if (param == null) {
                param = new RecyclerView.LayoutParams(0, 0);
            }
            if (isVisible) {
                param.height = itemDimension;
                param.width = itemDimension;
                itemView.setVisibility(View.VISIBLE);
            } else {
                itemView.setVisibility(View.GONE);
                param.height = 0;
                param.width = 0;
                itemView.setMinimumHeight(0);
                itemView.setMinimumWidth(0);
            }
            itemView.setLayoutParams(param);
        }

        private void addNativeAdViewToHolder(final Holder holder, final NativeAdInfo nativeAdInfo) {
            holder.itemView.setClickable(false);
            ViewGroup parent = (ViewGroup) nativeAdInfo.nativeAdView.getParent();
            if (parent != null) {
                parent.removeView(nativeAdInfo.nativeAdView);
            }
            ((ViewGroup) holder.itemView).addView(nativeAdInfo.nativeAdView);
        }

        private void downloadPreview(final View holder, final int position, final KCBackgroundElement item) {

            KCCustomThemeManager.getInstance().downloadElementResource(item, new DownloadStatusListener() {
                @Override
                public void onDownloadProgress(File file, float percent) {

                }

                @Override
                public void onDownloadSucceeded(File file) {
                    if (holder != null && holder.isShown()) {
                        holder.post(new Runnable() {
                            @Override
                            public void run() {
                                notifyItemChanged(position);
                            }
                        });
                    }
                }

                @Override
                public void onDownloadFailed(File file) {

                }
            }, true /* isPreview */);
        }

        @Override
        public int getItemCount() {
            return backgrounds == null ? 0 : backgrounds.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private void setNotNew(KCBaseElement item) {
            if (item.isNew()) {
                item.setNew(false);
                HSThemeNewTipController.getInstance().setElementNotNew(item);
            }
        }

        static class NativeAdInfo implements Comparable<NativeAdInfo> {
            private String nativeAd;
            private int position;
            private boolean hasAd = false;
            private KCNativeAdView nativeAdView = null;


            @Override
            public int compareTo(@NonNull NativeAdInfo o) {
                if (position < o.position) {
                    return -1;
                }
                if (position > o.position) {
                    return 1;
                }
                return 0;
            }
        }

        static class Holder extends RecyclerView.ViewHolder {
            ImageView backgroundContent;
            ImageView backgroundNewMark;
            ImageView backgroundGiftIcon;

            public Holder(View itemView) {
                super(itemView);
                backgroundContent = (ImageView) itemView.findViewById(R.id.background_content);
                backgroundNewMark = (ImageView) itemView.findViewById(R.id.background_new_mark);
                backgroundGiftIcon = (ImageView) itemView.findViewById(R.id.background_gift_icon);
                CompatUtils.setCardViewMaxElevation((CardView) itemView);
            }
        }
    }
}
