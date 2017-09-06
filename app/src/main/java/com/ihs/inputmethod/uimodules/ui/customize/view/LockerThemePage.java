package com.ihs.inputmethod.uimodules.ui.customize.view;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.ihs.inputmethod.feature.common.ViewUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.customize.CustomizeActivity;
import com.ihs.inputmethod.uimodules.ui.customize.adapter.LockerThemeGalleryAdapter;
import com.ihs.inputmethod.uimodules.ui.customize.service.ICustomizeService;
import com.ihs.inputmethod.uimodules.ui.customize.service.ServiceListener;

/**
 * Created by guonan.lv on 17/9/6.
 */

public class LockerThemePage extends LinearLayout implements ServiceListener {

    public LockerThemePage(Context context) {
        this(context, null);
    }

    public LockerThemePage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LockerThemePage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
                Context context = getContext();
        RecyclerView lockerThemesList = ViewUtils.findViewById(this, R.id.locker_theme_recyclerview);
        int padding = context.getResources().getDimensionPixelSize(R.dimen.customize_spacing_large);
        int spacing = context.getResources().getDimensionPixelSize(R.dimen.customize_spacing_small);
        final int spanCount = 2;
        lockerThemesList.setPadding(padding, padding, padding, padding);
        lockerThemesList.setClipToPadding(false);
        LockerThemeGalleryAdapter adapter = new LockerThemeGalleryAdapter(context);
//        adapter.onServiceConnected(service);
        GridLayoutManager layoutManager = new SafeGridLayoutManager(context, spanCount);
        layoutManager.setSpanSizeLookup(new LockerThemeGalleryAdapter.GridSpanSizer(adapter));
        lockerThemesList.setLayoutManager(layoutManager);
        lockerThemesList.addItemDecoration(
                new GridSpacingItemDecoration(spanCount, spacing, false, !adapter.isLockerInstalled()));
        lockerThemesList.setAdapter(adapter);
        CustomizeActivity.bindScrollListener(context, lockerThemesList, true);
    }

    @Override
    public void onServiceConnected(ICustomizeService service) {

    }
}