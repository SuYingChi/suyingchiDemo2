package com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.artw.lockscreen.LockerEnableDialog;
import com.artw.lockscreen.LockerSettings;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.kc.utils.KCAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.api.keyboard.HSKeyboardTheme;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSToastUtils;
import com.ihs.inputmethod.theme.ThemeLockerBgUtil;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.analytics.ThemeAnalyticsReporter;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.ThemePanelModel;
import com.keyboard.core.themes.custom.KCCustomThemeManager;

import java.util.List;

/**
 * Created by wenbinduan on 2017/1/4.
 */

public final class PanelThemeAdapterDelegate extends AdapterDelegate<List<ThemePanelModel>> {

    private int viewHeight;
    private int viewWidth;
    private int selectedHeight;
    private int selectedWidth;
    private int contentContainerHeight;
    private int contentContainerWidth;
    private final RequestOptions requestOptions;

    public PanelThemeAdapterDelegate(int spanCount) {
        float width1 = 280, width2 = 260, height1 = 150, height2 = 130;
        DisplayMetrics displayMetrics = HSApplication.getContext().getResources().getDisplayMetrics();
        final int viewWidth = displayMetrics.widthPixels / spanCount;

        this.viewWidth = viewWidth;
        this.viewHeight = (int) (viewWidth * height1 / width1);

        selectedWidth = viewWidth;
        selectedHeight = (int) (viewWidth * height1 / width1);

        contentContainerWidth = (int) (selectedWidth * width2 / width1);
        contentContainerHeight = (int) (selectedHeight * height2 / height1);

        requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE).override(contentContainerWidth,contentContainerHeight);
    }

    @Override
    protected boolean isForViewType(@NonNull List<ThemePanelModel> items, int position) {
        HSKeyboardTheme keyboardTheme = items.get(position).keyboardTheme;
        return keyboardTheme != null && keyboardTheme.mThemeName != null && keyboardTheme.mThemeName.trim().length() > 0;
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        PanelThemeViewHolder viewHolder = new PanelThemeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_panel_theme, parent, false));

        ViewGroup.LayoutParams itemLayoutParam = viewHolder.itemView.getLayoutParams();
        itemLayoutParam.width = viewWidth;
        itemLayoutParam.height = viewHeight;

        ViewGroup.LayoutParams layoutParams = viewHolder.check.getLayoutParams();
        layoutParams.width = selectedWidth;
        layoutParams.height = selectedHeight;

        ViewGroup.LayoutParams containerLayoutParams = viewHolder.contentContainer.getLayoutParams();
        containerLayoutParams.width = contentContainerWidth;
        containerLayoutParams.height = contentContainerHeight;

        return viewHolder;
    }

    @Override
    protected void onBindViewHolder(@NonNull List<ThemePanelModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        final PanelThemeViewHolder viewHolder = (PanelThemeViewHolder) holder;
        final ThemePanelModel themePanelModel = items.get(position);
        final String themeName = themePanelModel.keyboardTheme.mThemeName;
        final String themeShowName = themePanelModel.keyboardTheme.getThemeShowName();

        viewHolder.content.setImageDrawable(null);
        viewHolder.delete.setVisibility(View.GONE);
        String url = themePanelModel.keyboardTheme.getThemeType() == HSKeyboardTheme.ThemeType.BUILD_IN ? "file:///android_asset/" + themePanelModel.keyboardTheme.getThemePreviewPanelImageUrl() : themePanelModel.keyboardTheme.getThemePreviewPanelImageUrl();

        Glide.with(HSApplication.getContext()).asBitmap().apply(requestOptions).listener(new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                viewHolder.check.setVisibility(HSKeyboardThemeManager.getCurrentThemeName().equals(themeName) ? View.VISIBLE : View.GONE);
                if (themePanelModel.isCustomTheme && themePanelModel.isCustomThemeInEditMode) {
                    viewHolder.delete.setVisibility(HSKeyboardThemeManager.getCurrentThemeName().equals(themeName) ? View.GONE : View.VISIBLE);
                }
                return false;
            }
        }).load(url).into(viewHolder.content);

        viewHolder.contentContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (themePanelModel.isCustomTheme && themePanelModel.isCustomThemeInEditMode) {
                    return;
                }

                final int enableShowed = HSPreferenceHelper.getDefault().getInt("locker_enable_showed", 0);
                if (!HSKeyboardThemeManager.getCurrentTheme().mThemeName.equals(themeName)) {
                    if (enableShowed < HSConfig.optInteger(3, "Application", "Locker", "EnableAlertMaxShowCount") && LockerSettings.getLockerEnableStates() == 1 && !LockerSettings.isUserTouchedLockerSettings()) {
                        LockerEnableDialog.showLockerEnableDialog(HSApplication.getContext(), ThemeLockerBgUtil.getInstance().getThemeBgUrl(themeName),
                                HSApplication.getContext().getString(R.string.locker_enable_title_has_text),
                                new LockerEnableDialog.OnLockerBgLoadingListener() {
                                    @Override
                                    public void onFinish() {
                                        if (!HSKeyboardThemeManager.setKeyboardTheme(themeName)) {
                                            String failedString = HSApplication.getContext().getResources().getString(R.string.theme_apply_failed);
                                            HSToastUtils.toastCenterLong(String.format(failedString, themeShowName));
                                        }

                                        KCAnalytics.logEvent("keyboard_theme_chosed", "themeType", HSKeyboardThemeManager.isCustomTheme(themeName) ? "mytheme" : themeName);
                                        if (ThemeAnalyticsReporter.getInstance().isThemeAnalyticsEnabled()) {
                                            ThemeAnalyticsReporter.getInstance().recordThemeUsage(themeName);
                                        }
                                        int count = enableShowed + 1;
                                        HSPreferenceHelper.getDefault().putInt("locker_enable_showed", count);
                                    }
                                });
                    } else {
                        if (!HSKeyboardThemeManager.setKeyboardTheme(themeName)) {
                            String failedString = HSApplication.getContext().getResources().getString(R.string.theme_apply_failed);
                            HSToastUtils.toastCenterLong(String.format(failedString, themeShowName));
                        }

                        if (ThemeAnalyticsReporter.getInstance().isThemeAnalyticsEnabled()) {
                            ThemeAnalyticsReporter.getInstance().recordThemeUsage(themeName);
                        }

                    }

                }

            }
        });

        if (themePanelModel.isCustomTheme) {
            viewHolder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    KCCustomThemeManager.getInstance().removeCustomTheme(themeName);
                    KCAnalytics.logEvent("keyboard_customtheme_deleted");
                }
            });
        }
    }

}
