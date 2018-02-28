package com.ihs.inputmethod.uimodules.ui.customize.view;

import android.animation.Animator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.ihs.feature.common.Utils;
import com.ihs.inputmethod.feature.common.CommonUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.customize.WallpaperInfo;
import com.ihs.inputmethod.uimodules.ui.customize.adapter.OnlineWallpaperGalleryAdapter;
import com.ihs.inputmethod.uimodules.ui.customize.util.WallpaperDownloadEngine;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeActivity;

import java.util.List;

/**
 * Created by guonan.lv on 17/9/4.
 */

public class OnlineWallpaperListView extends FrameLayout {

    // --Commented out by Inspection (18/1/11 下午2:41):private static final String PREF_KEY_HOT_3D_WALLPAPER_START_INDEX = "hot_3d_wallpaper_start_index";

    // --Commented out by Inspection (18/1/11 下午2:41):private static final int HOT_3D_WALLPAPER_COUNT = 4;

    public ProgressBar progressBar;
    private LinearLayout retryLayout;
    public RecyclerView recyclerView;
    public OnlineWallpaperGalleryAdapter adapter;
    public int mCategoryIndex;
    // --Commented out by Inspection (18/1/11 下午2:41):private FrameLayout mPackageContainer;
    // --Commented out by Inspection (18/1/11 下午2:41):private Animator mPackageAnimator;


    public OnlineWallpaperListView(Context context) {
        this(context, null);
    }

    public OnlineWallpaperListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OnlineWallpaperListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private WallpaperDownloadEngine.OnLoadWallpaperListener mListener = new WallpaperDownloadEngine.OnLoadWallpaperListener() {
        @Override
        public void onLoadFinished(List<WallpaperInfo> wallpaperInfoList, int totalSize) {
            progressBar.setVisibility(View.INVISIBLE);
            retryLayout.setVisibility(View.INVISIBLE);
            if (adapter != null) {
                adapter.getLoadWallpaperListener().onLoadFinished(wallpaperInfoList, totalSize);
            }
        }

        @Override
        public void onLoadFailed() {
            progressBar.setVisibility(View.INVISIBLE);
            retryLayout.setVisibility(View.VISIBLE);
            if (adapter != null) {
                adapter.getLoadWallpaperListener().onLoadFailed();
            }
        }
    };

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        this.recyclerView = findViewById(R.id.recycler_view);
        this.retryLayout = findViewById(R.id.retry_downloading_layout);
        retryLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startLoading();
            }
        });
        this.progressBar = findViewById(R.id.wallpaper_loading_progress_bar);

        adapter = new OnlineWallpaperGalleryAdapter(getContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(adapter.getLayoutManager());
        GridItemDecoration decoration = new GridItemDecoration(CommonUtils.pxFromDp(2));
        decoration.setAdapter(adapter);
        recyclerView.addItemDecoration(decoration);
        ThemeHomeActivity.bindScrollListener(getContext(), recyclerView, true);
//        startLoading();
    }

    public void startLoading() {
        if (adapter.getItemCount() != 0) {
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        retryLayout.setVisibility(View.INVISIBLE);
        if (!Utils.isNetworkAvailable(-1)) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.INVISIBLE);
                    retryLayout.setVisibility(View.INVISIBLE);
                }
            }, 500);
        } else {
//            if (mScenario.equals(WallpaperMgr.Scenario.ONLINE_HOT)) {
//                WallpaperDownloadEngine.getNextHotWallpaperList(mListener);
//            } else {
            WallpaperDownloadEngine.getNextCategoryWallpaperList(mCategoryIndex, mListener);
//            }
        }
    }

    public void setCategoryName(String categoryName) {
//        recyclerView.addOnScrollListener(new ScrollLogger(categoryName));
    }

    public void setCategoryIndex(int categoryIndex) {
        mCategoryIndex = categoryIndex;
        if (adapter != null) {
            adapter.setCategoryIndex(categoryIndex);
        }
    }
}