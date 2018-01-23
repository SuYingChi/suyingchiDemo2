package com.ihs.inputmethod.home;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.inputmethod.fonts.stickers.FontListActivity;
import com.ihs.inputmethod.home.adapter.HomeAdapter;
import com.ihs.inputmethod.home.adapter.HomeStickerCardAdapterDelegate;
import com.ihs.inputmethod.home.model.HomeMenu;
import com.ihs.inputmethod.home.model.HomeModel;
import com.ihs.inputmethod.stickers.StickerListActivity;
import com.ihs.inputmethod.themes.ThemeListActivity;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerDataManager;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerDownloadManager;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerGroup;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerUtils;
import com.ihs.inputmethod.uimodules.ui.theme.analytics.ThemeAnalyticsReporter;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity;
import com.ihs.inputmethod.utils.DownloadUtils;
import com.ihs.keyboardutils.adbuffer.AdLoadingView;
import com.kc.utils.KCAnalytics;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import static com.ihs.inputmethod.uimodules.ui.sticker.StickerUtils.STICKER_DOWNLOAD_ZIP_SUFFIX;

/**
 * Created by jixiang on 18/1/17.
 */

public class HomeActivity extends HSAppCompatActivity implements HomeStickerCardAdapterDelegate.OnStickerClickListener, View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    public final static String NOTIFICATION_HOME_DESTROY = "HomeActivity.destroy";

    private List<HomeModel> homeModelList;
    private RecyclerView recyclerView;
    private HomeAdapter homeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(GravityCompat.START);
            }
        });
        toggle.setDrawerIndicatorEnabled(false);
        toggle.setHomeAsUpIndicator(VectorDrawableCompat.create(getResources(), R.drawable.ic_hamburg, null));
        toggle.syncState();

        initView();
    }

    private void initView() {
        findViewById(R.id.create_theme).setOnClickListener(this);

        recyclerView = findViewById(R.id.recycler_view);
        homeAdapter = new HomeAdapter(this, this, ThemeAnalyticsReporter.getInstance().isThemeAnalyticsEnabled());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return homeAdapter.getSpanSize(position);
            }
        });

        homeModelList = getHomeData();
        homeAdapter.setItems(homeModelList);

        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(homeAdapter);
    }

    private List<HomeModel> getHomeData() {
        List<HomeModel> homeModelList = new ArrayList<>();

        //banner data
        HomeModel homeModel = new HomeModel();
        homeModel.isBackgroundBanner = true;
        homeModelList.add(homeModel);

        // menuText
        homeModel = new HomeModel();
        homeModel.isMenu = true;
        homeModel.item = HomeMenu.KeyboardThemes;
        homeModelList.add(homeModel);

        homeModel = new HomeModel();
        homeModel.isMenu = true;
        homeModel.item = HomeMenu.AdultStickers;
        homeModelList.add(homeModel);

        homeModel = new HomeModel();
        homeModel.isMenu = true;
        homeModel.item = HomeMenu.SexyWallpaper;
        homeModelList.add(homeModel);

        homeModel = new HomeModel();
        homeModel.isMenu = true;
        homeModel.item = HomeMenu.CallFlash;
        homeModelList.add(homeModel);

        homeModel = new HomeModel();
        homeModel.isTitle = true;
        homeModel.title = getString(R.string.home_hot_themes);
        homeModel.titleClickable = true;
        homeModel.titleClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goThemeList();
            }
        };
        homeModel.rightButtonText = getString(R.string.theme_more);
        homeModelList.add(homeModel);

        homeModel = new HomeModel();
        homeModel.isThemeBanner = true;
        homeModelList.add(homeModel);

        homeModel = new HomeModel();
        homeModel.isTitle = true;
        homeModel.title = getString(R.string.home_recommend_stickers);
        homeModel.titleClickable = true;
        homeModel.rightButtonText = getString(R.string.theme_more);
        homeModel.titleClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goStickerList();
            }
        };
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

    private void goThemeList() {
        startActivity(new Intent(HomeActivity.this, ThemeListActivity.class));
    }

    private void goStickerList() {
        startActivity(new Intent(HomeActivity.this, StickerListActivity.class));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_HOME_DESTROY);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_theme:
                startActivity(new Intent(this, CustomThemeActivity.class));
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_call_font:
                startActivity(new Intent(this, FontListActivity.class));
                break;
        }
        return false;
    }

    @Override
    public void onStickerClick(HomeModel homeModel, Drawable thumbnailDrawable) {
        final StickerGroup stickerGroup = (StickerGroup) homeModel.item;
        final String stickerGroupName = stickerGroup.getStickerGroupName();
        final String stickerGroupDownloadedFilePath = StickerUtils.getStickerFolderPath(stickerGroupName) + STICKER_DOWNLOAD_ZIP_SUFFIX;

        // 移除点击过的new角标
        StickerDataManager.getInstance().removeNewTipOfStickerGroup(stickerGroup);
        homeAdapter.notifyItemChanged(homeModelList.indexOf(homeModel));

        DownloadUtils.getInstance().startForegroundDownloading(HomeActivity.this, stickerGroupName,
                stickerGroupDownloadedFilePath, stickerGroup.getStickerGroupDownloadUri(),
                new BitmapDrawable(ImageLoader.getInstance().loadImageSync(stickerGroup.getStickerGroupDownloadPreviewImageUri())), new AdLoadingView.OnAdBufferingListener() {
                    @Override
                    public void onDismiss(boolean success, boolean manually) {
                        if (success) {
                            KCAnalytics.logEvent("sticker_download_succeed", "StickerGroupName", stickerGroupName);
                            StickerDownloadManager.getInstance().unzipStickerGroup(stickerGroupDownloadedFilePath, stickerGroup);

                            int position = homeModelList.indexOf(homeModel);
                            if (position > 0 && position < homeModelList.size()) {
                                homeModelList.remove(position);
                                homeAdapter.notifyItemRemoved(position);
                            }
                        }
                    }

                });
    }


}
