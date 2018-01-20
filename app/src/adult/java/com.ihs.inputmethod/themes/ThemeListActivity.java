package com.ihs.inputmethod.themes;

import android.support.v7.widget.GridLayoutManager;

import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.common.ListActivity;
import com.ihs.inputmethod.themes.adapter.ThemeAdapter;

/**
 * Created by jixiang on 18/1/20.
 */

public class ThemeListActivity extends ListActivity {
    private ThemeAdapter themeAdapter;

    @Override
    protected void initView() {
        themeAdapter = new ThemeAdapter(this);
        themeAdapter.setDataList(HSKeyboardThemeManager.getNeedDownloadThemeList());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(themeAdapter);
    }
}
