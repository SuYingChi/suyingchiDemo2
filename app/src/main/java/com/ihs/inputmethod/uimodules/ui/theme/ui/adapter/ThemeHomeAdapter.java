package com.ihs.inputmethod.uimodules.ui.theme.ui.adapter;

import android.app.Activity;

import com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate.ThemeAdAdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate.ThemeBackgroundAdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate.ThemeBannerAdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate.ThemeSmallAdAdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate.ThemeTitleAdapterDelegate;

/**
 * Created by wenbinduan on 2016/12/22.
 */

public final class ThemeHomeAdapter extends CommonThemeCardAdapter {
    public interface OnThemeAdItemClickListener {
        void onThemeAdClick(int position);
    }

    public ThemeHomeAdapter(Activity activity, ThemeCardItemClickListener themeCardItemClickListener, OnThemeAdItemClickListener themeAdItemClickListener, boolean themeAnalyticsEnabled) {
        super(activity, themeCardItemClickListener, themeAnalyticsEnabled);

        delegatesManager.addDelegate(new ThemeAdAdapterDelegate(themeAdItemClickListener))
                .addDelegate(new ThemeTitleAdapterDelegate())
                .addDelegate(new ThemeBackgroundAdapterDelegate(activity))
                .addDelegate(new ThemeBannerAdapterDelegate(activity, themeAnalyticsEnabled))
                .addDelegate(new ThemeSmallAdAdapterDelegate());

    }
}
