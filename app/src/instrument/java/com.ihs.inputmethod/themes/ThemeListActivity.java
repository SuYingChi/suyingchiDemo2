package com.ihs.inputmethod.themes;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;

import com.ihs.inputmethod.api.keyboard.HSKeyboardTheme;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.base.BaseListActivity;
import com.ihs.inputmethod.home.model.HomeModel;
import com.ihs.inputmethod.mydownload.MyDownloadsActivity;
import com.ihs.inputmethod.themes.adapter.ThemeAdapter;
import com.ihs.inputmethod.uimodules.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jixiang on 18/1/20.
 */

public class ThemeListActivity extends BaseListActivity {
    private ThemeAdapter themeAdapter;

    public static void startThisActivity(Activity activity) {
        activity.startActivity(new Intent(activity, ThemeListActivity.class));
    }

    @Override
    protected void initView() {
        showDownloadIcon(true);

        themeAdapter = new ThemeAdapter(this);
        themeAdapter.setDataList(getDataList());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(themeAdapter);
    }


    @Override
    protected int getTitleTextResId() {
        return R.string.activity_keyboard_themes_title;
    }


    @Override
    protected void onDownloadClick() {
        MyDownloadsActivity.startThisActivity(this, getString(R.string.my_download_tab_theme));
    }

    private List getDataList() {
        List<HomeModel> homeModelList = new ArrayList<>();
        HomeModel<HSKeyboardTheme> homeModel;
        List<HSKeyboardTheme> themeList = HSKeyboardThemeManager.getNeedDownloadThemeList();
        for (HSKeyboardTheme hsKeyboardTheme : themeList) {
            homeModel = new HomeModel<>();
            homeModel.item = hsKeyboardTheme;
            homeModel.isTheme = true;
            homeModelList.add(homeModel);
        }
        return homeModelList;
    }
}
