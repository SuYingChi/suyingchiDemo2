package com.ihs.inputmethod.uimodules.ui.gif.riffsy.ui.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.mediacontroller.listeners.DownloadStatusListener;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.control.DataManager;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.control.GifCategory;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.control.GifManager;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.model.GifItem;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.callback.UICallback;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.download.GifDownloadTask;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.request.BaseRequest;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.request.SearchRequest;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.request.TagRequest;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.request.TrendRequest;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.ui.GifPanel;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.ui.adapter.GifHorizontalScrollViewAdapter;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.ui.adapter.TabViewAdapter;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.utils.MediaShareUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class GifPanelView extends LinearLayout implements TabViewAdapter.OnTabChangeListener, UICallback, GifView.OnGifClickListener {

    private TabViewAdapter tabViewAdapter = null;
    private RecyclerView mGifHSView;
    private GifHorizontalScrollViewAdapter mGifHSViewAdapter;
    private FrameLayout container;
    private View keyboardView;
    private GifPanel.OnPanelActionListener panelActionListener;

    private final GifCategory mGifCategory;

    private GifLoadingView mGifLoadingView;

    private boolean mLoadingRemote;

    // We should restore hot data
    private int mLastHotPosition;
    private int mLastHotOffset;
    private int mLastHotItemCount;
    private boolean mShouldRestoreHot;


    private GifStripView mStripView;
    private View gifBarTabHost;
    private View gifSearchButton;

    //background to hide recycler view to avoid flashing when enter gif panel
    private View mGifPanelBg;//background view with specific background color
    private volatile boolean isPaintingBg;

    private String mp4PackageName = HSInputMethod.getCurrentHostAppPackageName();
    private String shareUrl = "";
    private GifDownloadTask.Callback mp4DownloadCallback = new GifDownloadTask.Callback() {
        @Override
        public void onDownloadProgress(final File file, View view, float percent) {
            if (!mGifLoadingView.isShowing()) {
                mGifLoadingView.setBackgroundColor(0xcc000000);
                mGifLoadingView.show();
            }
        }

        @Override
        public void onDownloadSucceeded(final File file, View view) {
            mGifLoadingView.hide();
            try {
                MediaShareUtils.shareImageByIntent(Uri.fromFile(file), MediaShareUtils.MIME_MP4, mp4PackageName);
            } catch (Exception e) {
                HSInputMethod.inputText(shareUrl + "");
            }
        }

        @Override
        public void onDownloadFailed(View view) {
            mGifLoadingView.hide();
        }
    };

    private BaseRequest lastRequest = null;

    public GifPanelView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GifPanelView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        mGifCategory = new GifCategory();
        this.setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (tabViewAdapter == null) {
            List<String> tabs = new ArrayList<>();
            tabs.addAll(mGifCategory.getShownCategories());
            tabViewAdapter = new TabViewAdapter(tabs, this);
            final RecyclerView tabView = (RecyclerView) findViewById(R.id.image_category_tabhost);
            tabView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
            tabView.setAdapter(tabViewAdapter);
        }
        gifBarTabHost = findViewById(R.id.gif_bar_tabs);
        final Resources res = getResources();
        final int height = HSResourceUtils.getDefaultKeyboardHeight(res)
                - res.getDimensionPixelSize(R.dimen.emoticon_panel_actionbar_height);
        final int rowNumber;
        if (res.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            rowNumber = 1;
        } else {
            rowNumber = 2;
        }
        final int viewHeight = height / rowNumber;
        final int viewWidth = 4 * viewHeight / 3;

        container = (FrameLayout) findViewById(R.id.dropdown_container);
        final View gifContainer = findViewById(R.id.gif_container);
        gifContainer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));

        mGifHSViewAdapter = new GifHorizontalScrollViewAdapter(this, viewWidth, viewHeight);
        mGifHSViewAdapter.setHasStableIds(true);

        mGifHSView = (RecyclerView) findViewById(R.id.gif_horizontal_scroll_view);
        mGifHSView.setLayoutManager(new StaggeredGridLayoutManager(rowNumber, StaggeredGridLayoutManager.HORIZONTAL));
        mGifHSView.setAdapter(mGifHSViewAdapter);
        mGifHSView.addItemDecoration(new GridSpacingItemDecoration(res.getDimensionPixelSize(R.dimen.config_gif_grid_spacing), 0));
        mGifHSView.addOnChildAttachStateChangeListener(new LoadScrollListener());

        mGifLoadingView = (GifLoadingView) findViewById(R.id.gif_loading_view);
        mGifPanelBg = findViewById(R.id.gif_panel_view_alpha);

        mStripView = (GifStripView) findViewById(R.id.gif_strip_view);
        mStripView.bindPanelView(this);
        gifSearchButton = findViewById(R.id.gif_bar_search_button);
        if (!HSKeyboardThemeManager.getCurrentTheme().isDarkBg()) {
            ((ImageView) gifSearchButton).getDrawable().setColorFilter(res.getColor(R.color.gif_panel_search_button_color_light), PorterDuff.Mode.SRC_IN);
        } else {
            ((ImageView) gifSearchButton).getDrawable().setColorFilter(res.getColor(R.color.gif_panel_search_button_color), PorterDuff.Mode.SRC_IN);
        }
        gifSearchButton.setBackgroundDrawable(getBackgroudDrawableForButton(gifSearchButton.getBackground()));
        gifSearchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showStripView();
                mStripView.showStripViewToSearch();
                HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_gif_search_click");
            }
        });

    }

    private Drawable getBackgroudDrawableForButton(Drawable drawable) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        if (drawable.getConstantState() != null) {
            Drawable pressedDrawable = drawable.getConstantState().newDrawable();
            pressedDrawable.setColorFilter(Color.parseColor("#20000000"), PorterDuff.Mode.SRC_IN);
            stateListDrawable.addState(new int[]{android.R.attr.state_focused}, pressedDrawable);
            stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
            stateListDrawable.addState(new int[]{android.R.attr.state_selected}, pressedDrawable);
        }

        drawable.setColorFilter(getResources().getColor(R.color.gif_panel_search_bar_background_light), PorterDuff.Mode.SRC_IN);
        stateListDrawable.addState(new int[]{}, drawable);

        return stateListDrawable;
    }

    private void showStripView() {
        gifBarTabHost.setVisibility(GONE);
        mStripView.setVisibility(VISIBLE);
    }

    private void hideStripView() {
        gifBarTabHost.setVisibility(VISIBLE);
        mStripView.setVisibility(GONE);
    }

    public void reloadCurrentTab() {
        hideStripView();
        onTabChanged(mGifCategory.getCurrentCategoryId());
    }

    @Override
    public void onTabChanged(final String tabId) {
        final String last = mGifCategory.getCurrentExtendedCategoryId();
        if (last.equals(tabId) && mGifHSViewAdapter.getItemCount() > 0) {
            return;
        }
        lastClickedView = null;
        clear();
        // request data
        mGifCategory.setCurrentCategoryId(tabId);
        try {
            lastRequest = getRequest(tabId);
            GifManager.getInstance().sendRequest(lastRequest);
        } catch (Exception e) {
            e.printStackTrace();
            onFail();
        }
        HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("keyboard_gif_tab_switched", mGifCategory.getCurrentLogCategoryId());
    }

    private BaseRequest getRequest(String tabId) {
        final int count;
        if (mShouldRestoreHot) {
            count = mLastHotItemCount;
        } else {
            count = 20;
        }
        if (GifCategory.isTagTab(tabId)) {
            return new TagRequest(this, tabId, count);
        }
        //only trend
        return new TrendRequest(this, tabId, count);
    }

    public void showPanelView() {

        if (mGifHSViewAdapter.getItemCount() == 0) {
            tabViewAdapter.setCurrentTab(GifCategory.TAB_REACTIONS, GifCategory.TAB_EXPLORE);
        } else if (mGifHSViewAdapter.getItemCount() > 0 && !mGifCategory.getCurrentExtendedCategoryId().equals(GifCategory.TAB_REACTIONS)) {
            //enter hot , show background to hide recycler view
            isPaintingBg = true;
            mGifPanelBg.setVisibility(VISIBLE);
            tabViewAdapter.setCurrentTab(GifCategory.TAB_REACTIONS, GifCategory.TAB_EXPLORE);
        }

        gifSearchButton.setVisibility(INVISIBLE);
        Animator transX = ObjectAnimator.ofFloat(gifSearchButton, "translationX", 100, 0);
        transX.setStartDelay(400);
        transX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                gifSearchButton.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        transX.setDuration(100).start();
    }

    private void setCurrentGifPositionIfNeeded(final BaseRequest request) {
        if (request == null) {
            return;
        }
        // if we back from a tag rank
        if (GifCategory.isTagTab(request.categoryName)) {
            if (mShouldRestoreHot) {
                setCurrentGifPosition(mLastHotPosition, mLastHotOffset);
                mLastHotPosition = 0;
                mLastHotOffset = 0;
                mShouldRestoreHot = false;
                return;
            }
        }
        // otherwise we always back to front
        if (request.offset == 0) {
            setCurrentGifPosition(0, 0);
        }
    }

    private void setCurrentGifPosition(final int position, final int offset) {
        ((StaggeredGridLayoutManager) mGifHSView.getLayoutManager()).scrollToPositionWithOffset(position, offset);
    }

    private void onTagClicked(GifItem tagGifItem) {
        recordCurrentPosition();
        String tag = tagGifItem.getId();
        if (tag.startsWith("#")) {
            tag = tag.substring(1);
        }
        HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("keyboard_gif_tag_clicked", tag);
        mGifCategory.setCurrentExtendedCategoryId(tag);

        clear();
        showStripView();
        mStripView.setTagOnStripView(tag);
        try {
            lastRequest = new SearchRequest(this, tagGifItem.getUrl(), tag);
            GifManager.getInstance().sendRequest(lastRequest);
        } catch (Exception e) {
            onFail();
        }
    }

    public void onEmojiSearchItemClicked(GifItem tagGifItem) {
        recordCurrentPosition();
        mGifCategory.setCurrentLogCategoryId("emojiSearch");
        mGifCategory.setCurrentExtendedCategoryId(tagGifItem.getId());
        clear();
        try {
            lastRequest = new SearchRequest(this, tagGifItem.getUrl(), tagGifItem.getId());
            GifManager.getInstance().sendRequest(lastRequest);
        } catch (Exception e) {
            onFail();
        }
    }

    private void clear() {
        if (mGifLoadingView.isShowing()) {
            mGifLoadingView.hide();
        }
        mGifHSViewAdapter.clear();
    }

    public void performActionSearch(final String keyWord) {
        recordCurrentPosition();
        mGifPanelBg.setVisibility(VISIBLE);
        isPaintingBg = true;

        clear();
        mGifCategory.setCurrentLogCategoryId("search");
        mGifCategory.setCurrentExtendedCategoryId(keyWord);
        try {
            lastRequest = new SearchRequest(this, keyWord, 20);
            GifManager.getInstance().sendRequest(lastRequest);
        } catch (Exception e) {
            onFail();
        }

    }

    private void recordCurrentPosition() {
        if (!GifCategory.isTagTab(mGifCategory.getCurrentExtendedCategoryId())) {
            return;
        }
        mShouldRestoreHot = true;

        final StaggeredGridLayoutManager sglm = (StaggeredGridLayoutManager) mGifHSView.getLayoutManager();

        final int pos[] = new int[2];
        sglm.findFirstVisibleItemPositions(pos);
        mLastHotPosition = pos[0];

        mLastHotOffset = 0;
        final View firstVisibleItem = sglm.findViewByPosition(mLastHotPosition);
        if (firstVisibleItem != null) {
            mLastHotOffset = firstVisibleItem.getLeft();
        }

        mLastHotItemCount = mGifHSViewAdapter.getItemCount();
    }

    @Override
    public void onFetchRemote() {
        if (mGifHSViewAdapter.getItemCount() == 0) {
            mGifLoadingView.show();
            mLoadingRemote = true;
        }
    }

    @Override
    public void onFail() {
        if (mGifLoadingView.isShowing()) {
            mGifLoadingView.hide();
            mLoadingRemote = false;
        }
        isPaintingBg = false;
        mGifPanelBg.postDelayed(new Runnable() {
            @Override
            public void run() {
                mGifPanelBg.setVisibility(INVISIBLE);
            }
        }, 200);

        if (mGifHSViewAdapter.getItemCount() == 0) {
            if (mGifCategory.getCurrentExtendedCategoryId().equals(GifCategory.TAB_RECENT)) {
                mGifLoadingView.showResult(getContext().getString(R.string.result_no_recent));
                return;
            }

            if (mGifCategory.getCurrentExtendedCategoryId().equals(GifCategory.TAB_FAVORITE)) {
                mGifLoadingView.showResult(getContext().getString(R.string.result_no_favorites));
                return;
            }

            mGifLoadingView.showResult(getContext().getString(R.string.result_no_data_on_fail));
        }
    }

    @Override
    public synchronized void onComplete(List<?> list, final BaseRequest request) {
        HSLog.d("notifyDataCompleted " + "->" + request.categoryName);
        if (mLoadingRemote) {
            mLoadingRemote = false;
        }

        if (mGifLoadingView.isShowing()) {
            mGifLoadingView.hide();
        }

        if (request.categoryName.equals(mGifCategory.getCurrentExtendedCategoryId())) {
            if (request.offset == mGifHSViewAdapter.getItemCount()) {
                if (list != null && !list.isEmpty()) {
                    List<GifItem> data = (List<GifItem>) list;
                    HSLog.d(list.size() + " items");
                    mGifHSViewAdapter.addData(data);
                    mGifHSViewAdapter.notifyItemRangeInserted(mGifHSViewAdapter.getItemCount(), data.size());
                }
            }
        }
        //if background is visible, set it invisible
        if (isPaintingBg) {
            isPaintingBg = false;
            mGifPanelBg.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mGifPanelBg.setVisibility(INVISIBLE);
                }
            }, 200);
        }
        if (mGifHSViewAdapter.getItemCount() == 0) {
            if (mGifCategory.getCurrentExtendedCategoryId().equals(GifCategory.TAB_RECENT)) {
                mGifLoadingView.showResult(getContext().getString(R.string.result_no_recent));
            } else if (mGifCategory.getCurrentExtendedCategoryId().equals(GifCategory.TAB_FAVORITE)) {
                mGifLoadingView.showResult(getContext().getString(R.string.result_no_favorites));
            } else {
                mGifLoadingView.showResult(getContext().getString(R.string.result_no));
            }
        }

        // set current gif position
        if (request.categoryName.equals(mGifCategory.getCurrentExtendedCategoryId())) {
            this.post(new Runnable() {
                @Override
                public void run() {
                    setCurrentGifPositionIfNeeded(request);
                }
            });
        }

    }

    GifView lastClickedView;

    @Override
    public void onGifClick(GifItem item, GifView view) {

        if (lastClickedView != view && lastClickedView != null) {
            lastClickedView.hideFavoriteView();
        }
        lastClickedView = view;

        if (item.isTag()) {
            onTagClicked(item);
            return;
        }
        onGifClicked(view);
    }

    @Override
    public void onGifLongClick(final GifItem item, final GifView view) {
        if (lastClickedView != view && lastClickedView != null) {
            lastClickedView.hideFavoriteView();
        }
        lastClickedView = view;

        if (GifCategory.isTagTab(mGifCategory.getCurrentExtendedCategoryId())) {
            return;
        }
        if (mGifCategory.getCurrentExtendedCategoryId().equals(GifCategory.TAB_FAVORITE)) {
            view.showFavoriteDeleteView();
            return;
        }
        DataManager.getInstance().addFavorite(item);
        DataManager.getInstance().addRecent(item);
        view.showFavoriteAddView(true);
    }

    @Override
    public void onFavoriteIconClick(GifItem item, GifView view) {
        if (lastClickedView != view && lastClickedView != null) {
            lastClickedView.hideFavoriteView();
        }
        lastClickedView = view;
        final boolean isAdded = DataManager.getInstance().isAddedToFavorite(item);
        if (isAdded) {
            DataManager.getInstance().removeFavorite(item);
        } else {
            DataManager.getInstance().addFavorite(item);
        }
        view.showFavoriteAddView(!isAdded);
    }

    @Override
    public void onDeleteIconClick(GifItem item, GifView view) {
        if (lastClickedView != view && lastClickedView != null) {
            lastClickedView.hideFavoriteView();
        }
        lastClickedView = null;
        DataManager.getInstance().removeFavorite(item);
        mGifHSViewAdapter.removeItem(item);
        if (mGifHSViewAdapter.getItemCount() == 0) {
            onFail();
        }
    }

    public void onLastImageShowing() {
        if (GifCategory.isTagTab(mGifCategory.getCurrentExtendedCategoryId()) || mGifHSViewAdapter.getItemCount() == 0) {
            return;
        }
        loadMoreData();
    }

    private void loadMoreData() {
        HSLog.d("loadMoreDataNow...");
        final String categoryId = mGifCategory.getCurrentExtendedCategoryId();
        try {
            lastRequest.offset = mGifHSViewAdapter.getItemCount();

            if (lastRequest.getParams().size() > 0) {
                lastRequest.addParams("pos", DataManager.getInstance().getNextPos(categoryId));
            } else {
                lastRequest.addParamsToUrl("pos", DataManager.getInstance().getNextPos(categoryId));
            }

            GifManager.getInstance().sendRequest(lastRequest);
        } catch (Exception e) {
            onFail();
        }

    }

    public void removeDropDownView(View view) {
        if (view.getParent() == container) {
            container.removeView(view);
        }
        if (container.getChildCount() == 1)
            panelActionListener.setBarVisibility(VISIBLE);
    }

    public void addDropDownView(View view) {
        panelActionListener.setBarVisibility(GONE);
        if (view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
        container.addView(view);
    }

    public void setOnPanelActionListener(GifPanel.OnPanelActionListener panelActionListener) {
        this.panelActionListener = panelActionListener;
    }

    public void showKeyboardAsDropDownView() {
        if (keyboardView == null) {
            keyboardView = panelActionListener.getKeyboardView();
        }
        keyboardView.setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
        addDropDownView(keyboardView);
    }

    public void closeKeyboardDropDownView() {
        if (keyboardView != null) {
            removeDropDownView(keyboardView);
            keyboardView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

	private void onGifClicked(GifView view) {
		HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("keyboard_gif_clicked", mGifCategory.getCurrentLogCategoryId());
		mp4PackageName = HSInputMethod.getCurrentHostAppPackageName();
		shareUrl = view.getGifItem().getUrl();
		notifyImageClicked(view, mp4PackageName, mp4DownloadCallback);
	}

	public void notifyImageClicked(final GifView item, final String packageName, final GifDownloadTask.Callback callback) {
		if (item != null) {
			DataManager.getInstance().addRecent(item.getGifItem());
            GifManager.getInstance().share(item.getGifItem(), packageName, callback);
//			item.shareHdGif(new DownloadStatusListener() {
//				@Override
//				public void onDownloadProgress(File file, final float percent) {
//
//				}
//
//				@Override
//				public void onDownloadSucceeded(File file) {
//					GifManager.getInstance().share(item.getGifItem(), packageName, callback);
//				}
//
//				@Override
//				public void onDownloadFailed(File file) {
//
//				}
//			});
		}
	}

}