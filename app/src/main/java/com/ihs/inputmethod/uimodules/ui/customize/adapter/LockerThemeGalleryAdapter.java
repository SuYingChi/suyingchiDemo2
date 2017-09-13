package com.ihs.inputmethod.uimodules.ui.customize.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ihs.commons.config.HSConfig;
import com.ihs.feature.common.PromotionTracker;
import com.ihs.feature.common.ViewUtils;
import com.ihs.inputmethod.feature.common.CommonUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.customize.service.ICustomizeService;
import com.ihs.inputmethod.uimodules.ui.customize.service.ServiceListener;
import com.ihs.inputmethod.uimodules.ui.customize.view.ImagePressedTouchListener;
import com.ihs.inputmethod.uimodules.ui.customize.view.LockerThemeInfo;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by guonan.lv on 17/9/6.
 */

public class LockerThemeGalleryAdapter extends RecyclerView.Adapter<LockerThemeGalleryAdapter.ViewHolder>
        implements View.OnClickListener, ServiceListener {

    private static final int ITEM_TYPE_BANNER = 0;
    private static final int ITEM_TYPE_THEME_VIEW = 1;

    private static final String INCOMING_THEME_THUMBNAIL_SUFFIX = ".jpg";
    private static final String INCOMING_THEME_GIF_SUFFIX = ".gif";

    private Context mContext;
    private LayoutInflater mInflater;
    private List<LockerThemeInfo> mThemes = new ArrayList<>();
    private boolean mLockerInstalled = false;
    private String mInComingCallThemeUrl;

    public LockerThemeGalleryAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mLockerInstalled = CommonUtils.isPackageInstalled(HSConfig.optString("",
                "Application", "Promotions", "LockerPackage"));
        mInComingCallThemeUrl = HSConfig.optString("", "Application", "Server", "IncomingCallThemeURL");
        populateData();
    }

    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .cacheOnDisk(true).build();

    public boolean isLockerInstalled() {
        return mLockerInstalled;
    }

    @SuppressWarnings("unchecked")
    public void populateData() {
        List<Map<String, ?>> configs = (List<Map<String, ?>>) HSConfig.getList("Application", "IncomingCallTheme");
        mThemes.clear();
        for (Map<String, ?> themeConfig : configs) {
            LockerThemeInfo theme = LockerThemeInfo.ofConfig(themeConfig);
            if (theme != null) {
                // Rule out built-in themes of locker. Only take themes with valid package name and thumbnail URL.
//                theme.installed = CommonUtils.isPackageInstalled(theme.packageName);
                mThemes.add(theme);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mThemes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return ITEM_TYPE_THEME_VIEW;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View grid = mInflater.inflate(R.layout.locker_theme_gallery_item, parent, false);
        ThemeViewHolder themeHolder = new ThemeViewHolder(grid);
        ImagePressedTouchListener touchListener = new ImagePressedTouchListener(themeHolder.themeThumbnail);
        themeHolder.itemView.setOnTouchListener(touchListener);
        themeHolder.itemView.setOnClickListener(this);
        return themeHolder;

    }

    public static String getInComingCallThemeThumbnailUrl(String name) {
        return HSConfig.optString("", "Application", "Server", "IncomingCallThemeURL") + name + INCOMING_THEME_THUMBNAIL_SUFFIX;
    }

    public static String getInComingCallThemeGifUrl(String name) {
        return HSConfig.optString("", "Application", "Server", "IncomingCallThemeURL") + name + INCOMING_THEME_GIF_SUFFIX;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == ITEM_TYPE_BANNER) {
            return;
        }
        int themeIndex = position;
        final ThemeViewHolder themeHolder = (ThemeViewHolder) holder;
        holder.itemView.setTag(themeIndex);
        final LockerThemeInfo theme = mThemes.get(themeIndex);

        Glide.with((themeHolder.itemView.getContext())).load(getInComingCallThemeThumbnailUrl(theme.name)).asBitmap().fitCenter()
                .placeholder(R.drawable.locker_theme_thumbnail_loading).error(R.drawable.locker_theme_thumbnail_failed)
                .format(DecodeFormat.PREFER_RGB_565).diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(themeHolder.themeThumbnail);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.grid_root:
                int pos = (int) v.getTag();
                LockerThemeInfo themeInfo = mThemes.get(pos);
//                LauncherAnalytics.logEvent("Theme_Locker_Theme_Clicked", "type", themeInfo.packageName);
                break;
            case R.id.locker_install_btn:
            case R.id.locker_theme_gallery_banner:
//                LauncherAnalytics.logEvent("Theme_Locker_DownloadButton_Clicked");
                String lockerPackage = getLockerPackageName();
                browseMarketApp(lockerPackage);
                PromotionTracker.startTracking(lockerPackage, PromotionTracker.EVENT_LOG_APP_NAME_LOCKER);
                break;
        }
    }

    private String getLockerPackageName() {
        return HSConfig.optString("", "Application", "Promotions", "LockerPackage");
    }

    private void browseMarketApp(String packageName) {
//        CustomizeUtils.browseMarketApp(mServiceHolder.getService(), packageName);
    }

    @Override
    public void onServiceConnected(ICustomizeService service) {
        populateData();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class BannerViewHolder extends ViewHolder {
        Button installButton;

        public BannerViewHolder(View itemView) {
            super(itemView);
            installButton = ViewUtils.findViewById(itemView, R.id.locker_install_btn);
        }
    }

    static class ThemeViewHolder extends ViewHolder {
        ImageView themeThumbnail;
        ImageView marketMarker;

        public ThemeViewHolder(View itemView) {
            super(itemView);
            themeThumbnail = ViewUtils.findViewById(itemView, R.id.theme_thumbnail);
//            marketMarker = ViewUtils.findViewById(itemView, R.id.theme_market_marker);
        }
    }

    public static class GridSpanSizer extends GridLayoutManager.SpanSizeLookup {
        LockerThemeGalleryAdapter mAdapter;

        public GridSpanSizer(LockerThemeGalleryAdapter adapter) {
            super();
            mAdapter = adapter;
            setSpanIndexCacheEnabled(true);
        }

        @Override
        public int getSpanSize(int position) {
            if (mAdapter.getItemViewType(position) == ITEM_TYPE_BANNER) {
                return 2;
            }
            return 1;
        }
    }
}
