package com.ihs.inputmethod.uimodules.ui.customize.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.acb.call.themes.Type;
import com.ihs.commons.config.HSConfig;
import com.ihs.feature.common.PromotionTracker;
import com.ihs.feature.common.ViewUtils;
import com.ihs.inputmethod.feature.common.CommonUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.customize.InCallThemePreviewActivity;
import com.ihs.inputmethod.uimodules.ui.customize.service.ICustomizeService;
import com.ihs.inputmethod.uimodules.ui.customize.service.ServiceListener;
import com.ihs.inputmethod.uimodules.ui.customize.view.LockerThemeInfo;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.acb.call.themes.Type.CONFIG_KEY_GIF_URL;
import static com.acb.call.themes.Type.CONFIG_KEY_HOT;
import static com.acb.call.themes.Type.CONFIG_KEY_ICON_ACCEPT;
import static com.acb.call.themes.Type.CONFIG_KEY_ICON_REJECT;
import static com.acb.call.themes.Type.CONFIG_KEY_ID;
import static com.acb.call.themes.Type.CONFIG_KEY_ID_NAME;
import static com.acb.call.themes.Type.CONFIG_KEY_PREVIEW_IMAGE;
import static com.acb.call.themes.Type.CONFIG_KEY_RES_TYPE;


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
            .showImageOnFail(R.drawable.locker_theme_thumbnail_failed)
            .showImageOnLoading(R.drawable.locker_theme_thumbnail_loading)
            .cacheOnDisk(true).build();

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

        ImageLoader.getInstance().displayImage(getInComingCallThemeThumbnailUrl(theme.name), themeHolder.themeThumbnail, displayImageOptions);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.grid_root:
                int pos = (int) v.getTag();
                LockerThemeInfo themeInfo = mThemes.get(pos);
                if (mContext instanceof ThemeHomeActivity) {
                    Intent intent = new Intent(mContext, InCallThemePreviewActivity.class);
                    String themeName = themeInfo.name;
                    Map<String, Object> item = new HashMap<>();
                    item.put(CONFIG_KEY_RES_TYPE, "url");
                    item.put("Name", themeName);
                    item.put(CONFIG_KEY_ICON_ACCEPT, "acb_phone_call_answer");
                    item.put(CONFIG_KEY_ICON_REJECT, "acb_phone_call_refuse");
                    item.put(CONFIG_KEY_HOT, false);
                    item.put(CONFIG_KEY_GIF_URL, LockerThemeGalleryAdapter.getInComingCallThemeGifUrl(themeName));
                    item.put(CONFIG_KEY_PREVIEW_IMAGE, LockerThemeGalleryAdapter.getInComingCallThemeThumbnailUrl(themeName));
                    item.put(CONFIG_KEY_ID, themeName.hashCode());
                    item.put(CONFIG_KEY_ID_NAME, themeName);
                    Type themeType = Type.typeFromMap(item);
//                    Type.addGifToTypes(themeType);
                    intent.putExtra("CallThemeType", themeType);
//                    mContext.startActivity(intent);
                }
                break;
            case R.id.locker_install_btn:
            case R.id.locker_theme_gallery_banner:
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

    static class ThemeViewHolder extends ViewHolder {
        ImageView themeThumbnail;

        public ThemeViewHolder(View itemView) {
            super(itemView);
            themeThumbnail = ViewUtils.findViewById(itemView, R.id.theme_thumbnail);
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
