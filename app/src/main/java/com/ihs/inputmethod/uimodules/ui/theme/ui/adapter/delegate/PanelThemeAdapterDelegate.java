package com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.artw.lockscreen.LockerEnableDialog;
import com.artw.lockscreen.LockerSettings;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSToastUtils;
import com.ihs.inputmethod.theme.ThemeLockerBgUtil;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.analytics.ThemeAnalyticsReporter;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.ThemePanelModel;
import com.keyboard.core.themes.custom.KCCustomThemeManager;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

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
    }

    @Override
    protected boolean isForViewType(@NonNull List<ThemePanelModel> items, int position) {
        String themeName = items.get(position).themeName;
        return themeName != null && themeName.trim().length() > 0;
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
        final ThemePanelModel model = items.get(position);

        HSKeyboardThemeManager.loadThemePreviewPanelDrawable(model.themeName, contentContainerWidth, contentContainerHeight, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                viewHolder.content.setImageDrawable(null);
                viewHolder.delete.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                viewHolder.content.setImageBitmap(loadedImage);
                viewHolder.check.setVisibility(HSKeyboardThemeManager.getCurrentThemeName().equals(model.themeName) ? View.VISIBLE : View.GONE);
                if (model.isCustomTheme && model.isCustomThemeInEditMode) {
                    viewHolder.delete.setVisibility(HSKeyboardThemeManager.getCurrentThemeName().equals(model.themeName) ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });

        viewHolder.contentContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (model.isCustomTheme && model.isCustomThemeInEditMode) {
                    return;
                }

                final int enableShowed = HSPreferenceHelper.getDefault().getInt("locker_enable_showed", 0);
                if (!HSKeyboardThemeManager.getCurrentTheme().mThemeName.equals(model.themeName)) {
                    if (enableShowed < HSConfig.optInteger(3, "Application", "Locker", "EnableAlertMaxShowCount") && !LockerSettings.isLockerEnableShowSatisfied()) {
                        LockerEnableDialog.showLockerEnableDialog(HSApplication.getContext(), ThemeLockerBgUtil.getInstance().getThemeBgUrl(model.themeName), new LockerEnableDialog.OnLockerBgLoadingListener() {
                            @Override
                            public void onFinish() {
                                if (!HSKeyboardThemeManager.setKeyboardTheme(model.themeName)) {
                                    String failedString = HSApplication.getContext().getResources().getString(R.string.theme_apply_failed);
                                    HSToastUtils.toastCenterLong(String.format(failedString, model.themeShowName));
                                }

                                HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("keyboard_theme_chosed", HSKeyboardThemeManager.isCustomTheme(model.themeName) ? "mytheme" : model.themeName);
                                if (ThemeAnalyticsReporter.getInstance().isThemeAnalyticsEnabled()) {
                                    ThemeAnalyticsReporter.getInstance().recordThemeUsage(model.themeName);
                                }
                                int count = enableShowed + 1;
                                HSPreferenceHelper.getDefault().putInt("locker_enable_showed", count);
                            }
                        });
                    } else {
                        if (!HSKeyboardThemeManager.setKeyboardTheme(model.themeName)) {
                            String failedString = HSApplication.getContext().getResources().getString(R.string.theme_apply_failed);
                            HSToastUtils.toastCenterLong(String.format(failedString, model.themeShowName));
                        }

                        HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("keyboard_theme_chosed", HSKeyboardThemeManager.isCustomTheme(model.themeName) ? "mytheme" : model.themeName);
                        if (ThemeAnalyticsReporter.getInstance().isThemeAnalyticsEnabled()) {
                            ThemeAnalyticsReporter.getInstance().recordThemeUsage(model.themeName);
                        }

                    }

                }

            }
        });

        if (model.isCustomTheme) {
            viewHolder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//					HSKeyboardThemeManager.removeTheme(model.themeName);
                    KCCustomThemeManager.getInstance().removeCustomTheme(model.themeName);
                    HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("keyboard_customtheme_delete_clicked");
                }
            });
        }
    }

}
