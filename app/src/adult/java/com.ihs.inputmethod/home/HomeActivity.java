package com.ihs.inputmethod.home;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.inputmethod.home.HomeModel.HomeModel;
import com.ihs.inputmethod.home.adapter.HomeAdapter;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerDataManager;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerGroup;
import com.ihs.inputmethod.uimodules.ui.theme.analytics.ThemeAnalyticsReporter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jixiang on 18/1/17.
 */

public class HomeActivity extends HSAppCompatActivity {
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.english_ime_name);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        initView();
    }

    private void initView() {
        recyclerView = findViewById(R.id.recycler_view);
        HomeAdapter homeAdapter = new HomeAdapter(this, null, ThemeAnalyticsReporter.getInstance().isThemeAnalyticsEnabled());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return homeAdapter.getSpanSize(position);
            }
        });
        homeAdapter.setItems(getHomeData());
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(homeAdapter);
    }

    private List<HomeModel> getHomeData() {
        List<HomeModel> homeModelList = new ArrayList<>();

        //banner data
        HomeModel homeModel = new HomeModel();
        homeModel.isBanner = true;
        homeModelList.add(homeModel);

        // menuText
        homeModel = new HomeModel();
        homeModel.isMenu = true;
        homeModel.menuTextResId = R.string.home_menu_keyboard_themes;
        homeModel.menuImageResId = R.drawable.home_menu_keyboard_theme;
        homeModelList.add(homeModel);

        homeModel = new HomeModel();
        homeModel.isMenu = true;
        homeModel.menuTextResId = R.string.home_menu_adult_stickers;
        homeModel.menuImageResId = R.drawable.home_menu_sticker;
        homeModelList.add(homeModel);

        homeModel = new HomeModel();
        homeModel.isMenu = true;
        homeModel.menuTextResId = R.string.home_menu_sexy_wallpaper;
        homeModel.menuImageResId = R.drawable.home_menu_wallpaper;
        homeModelList.add(homeModel);

        homeModel = new HomeModel();
        homeModel.isMenu = true;
        homeModel.menuTextResId = R.string.home_menu_call_flash;
        homeModel.menuImageResId = R.drawable.home_menu_call_flash;
        homeModelList.add(homeModel);

        homeModel = new HomeModel();
        homeModel.isTitle = true;
        homeModel.title = getString(R.string.home_hot_themes);
        homeModel.titleClickable = true;
        homeModel.rightButtonText = getString(R.string.theme_more);
        homeModelList.add(homeModel);

        homeModel = new HomeModel();
        homeModel.isThemeList = true;
        homeModelList.add(homeModel);

        homeModel = new HomeModel();
        homeModel.isTitle = true;
        homeModel.title = getString(R.string.home_recommend_stickers);
        homeModel.titleClickable = true;
        homeModel.rightButtonText = getString(R.string.theme_more);
        homeModelList.add(homeModel);

        List<StickerGroup> stickerGroupList = StickerDataManager.getInstance().getStickerGroupList();
        for (StickerGroup stickerGroup : stickerGroupList) {
            if (!stickerGroup.isStickerGroupDownloaded()) {
                homeModel = new HomeModel();
                homeModel.isSticker = true;
                homeModel.item = stickerGroup;
                homeModelList.add(homeModel);
            }
        }

        return homeModelList;
    }
}
