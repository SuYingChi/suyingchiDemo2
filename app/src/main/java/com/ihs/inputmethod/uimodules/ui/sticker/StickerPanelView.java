package com.ihs.inputmethod.uimodules.ui.sticker;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.stickerplus.PlusButton;
import com.ihs.inputmethod.uimodules.ui.common.BaseTabViewAdapter;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanxia on 2017/6/5.
 */

public class StickerPanelView extends LinearLayout implements BaseTabViewAdapter.OnTabChangeListener, StickerPageAdapter.OnStickerClickListener {

    private RecyclerView stickerTabRecyclerView;
    private StickerTabAdapter stickerTabAdapter; // sticker 顶部 tab bar
    private RecyclerView stickerMainPagerRecyclerView; // sticker 主要显示部分
    private StickerPageAdapter stickerMainRecyclerViewAdapter; // sticker 主要显示部分 adapter
    private StickerPanelManager stickerPanelManager;

    private PlusButton plusButton;

    private ViewPager stickerPanelViewPager;
    private StickerViewPagerAdapter stickerViewPagerAdapter;
    private List<String> stickerExceptNeedDownloadNameList;
    private INotificationObserver observer = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            HSLog.d("sticker regainDataAndNotifyDataSetChange...");
            regainDataAndNotifyDataSetChange();
        }
    };

    private void regainDataAndNotifyDataSetChange() {
        if (stickerViewPagerAdapter != null) {
            stickerViewPagerAdapter.setNeedDownloadStickerGroupList(stickerPanelManager.getNeedDownloadStickerGroupList());
        }
        if (stickerTabAdapter != null) {
            stickerExceptNeedDownloadNameList.clear();
            stickerExceptNeedDownloadNameList.addAll(stickerPanelManager.getSortedExceptNeedDownloadStickerGroupNameList());
            stickerTabAdapter.setTabNameList(stickerExceptNeedDownloadNameList);
        }


        stickerPanelManager.loadData();
    }

    public StickerPanelView(Context context) {
        this(context, null);
    }

    public StickerPanelView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StickerPanelView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        final Resources res = context.getResources();
        stickerPanelManager = new StickerPanelManager(PreferenceManager.getDefaultSharedPreferences(context), res, this);
        this.setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
        final int height = HSResourceUtils.getDefaultKeyboardHeight(res)
                + res.getDimensionPixelSize(R.dimen.config_suggestions_strip_height)
                - res.getDimensionPixelSize(R.dimen.emoticon_panel_actionbar_height);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        HSGlobalNotificationCenter.addObserver(StickerDataManager.STICKER_DATA_LOAD_FINISH_NOTIFICATION, observer);
        HSGlobalNotificationCenter.addObserver(StickerDataManager.STICKER_GROUP_DOWNLOAD_SUCCESS_NOTIFICATION, observer);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (stickerTabAdapter == null) {
            stickerExceptNeedDownloadNameList = new ArrayList<>();
            stickerExceptNeedDownloadNameList.addAll(stickerPanelManager.getSortedExceptNeedDownloadStickerGroupNameList());
            stickerTabAdapter = new StickerTabAdapter(stickerExceptNeedDownloadNameList, this);
            stickerTabRecyclerView = (RecyclerView) findViewById(R.id.sticker_category_tab_host);
            stickerTabRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            stickerTabRecyclerView.setAdapter(stickerTabAdapter);

            plusButton = (PlusButton) findViewById(R.id.plus_container);
            plusButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Bundle bundle = new Bundle();
                    bundle.putInt(ThemeHomeActivity.BUNDLE_KEY_HOME_INIT, ThemeHomeActivity.HOME_VIEWPAGER_STICKER_PAGE);
                    HSInputMethod.hideWindow();
                    plusButton.saveUnshowNewTipState();
                    plusButton.hideNewTip();
                    StickerDataManager.getInstance().saveShowNewTipState(false);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            final Intent intent = new Intent();
                            intent.setClass(HSApplication.getContext(), com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeActivity.class);
                            intent.putExtras(bundle);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            HSApplication.getContext().startActivity(intent);
                        }
                    }, 200);
                }
            });
        }
        final Resources res = getResources();
        final int height = HSResourceUtils.getDefaultKeyboardHeight(res)
                - res.getDimensionPixelSize(R.dimen.emoticon_panel_actionbar_height);
        final int width = HSResourceUtils.getDefaultKeyboardWidth(res);
        final int stickerCol = res.getInteger(R.integer.config_sticker_col_count); // 一行显示多少sticker
        final int stickerRow = res.getInteger(R.integer.config_sticker_row_count); // 一页显示几行
        final int stickerHeight = height / stickerRow;
        final int stickerWidth = (int) (width / (stickerCol + 0.5f));

        LayoutInflater inflater = LayoutInflater.from(HSApplication.getContext());
        View sticker_panel_first_page = inflater.inflate(R.layout.common_sticker_panel_first_page, null);
        stickerViewPagerAdapter = new StickerViewPagerAdapter(sticker_panel_first_page);
        stickerViewPagerAdapter.setNeedDownloadStickerGroupList(stickerPanelManager.getNeedDownloadStickerGroupList());
        stickerPanelViewPager = (ViewPager) findViewById(R.id.sticker_panel_view_pager);
        stickerPanelViewPager.setAdapter(stickerViewPagerAdapter);
        stickerPanelViewPager.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        stickerPanelViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position != 0) { // 下载页之间的滑动
                    stickerTabRecyclerView.getLayoutManager().scrollToPosition(position + 1);
                    final String stickerGroupName = stickerPanelManager.getNeedDownloadStickerGroupList().get(position - 1).getStickerGroupName();
                    stickerTabAdapter.setTabSelected(stickerGroupName);
                } else { //从下载页面滑到首页，先滑动到下载好的最后一项
                    stickerTabRecyclerView.getLayoutManager().scrollToPosition(0);
                    String lastDownloadedStickerTabName = stickerPanelManager.getLastDownloadedTabName();
                    stickerTabAdapter.setTabSelected(lastDownloadedStickerTabName);
                    Pair<Integer, Integer> positionPair = stickerPanelManager.getLastShownItemPositionForTab(lastDownloadedStickerTabName);
                    setCurrentItemPosition(positionPair.first, positionPair.second);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        stickerMainRecyclerViewAdapter = new StickerPageAdapter(stickerHeight, stickerWidth, this);
        stickerMainRecyclerViewAdapter.setHasStableIds(true);
        stickerMainPagerRecyclerView = (RecyclerView) sticker_panel_first_page.findViewById(R.id.sticker_horizontal_scroll_view);
        stickerMainPagerRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(stickerRow, StaggeredGridLayoutManager.HORIZONTAL));
        stickerMainPagerRecyclerView.setAdapter(stickerMainRecyclerViewAdapter);
        stickerMainPagerRecyclerView.addOnScrollListener(new OnStickerScrollListener());
        stickerMainPagerRecyclerView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        RecyclerView.ItemAnimator animator = stickerMainPagerRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
    }

    @Override
    public void onTabChanged(String nextTab) {
        HSLog.d("StickerPanelView change to tab " + nextTab);
        stickerPanelManager.setCurrentTabName(nextTab);
        if (stickerPanelManager.isRecentTab(nextTab) && stickerPanelManager.hasPendingRecent()) { // 是recent 并且有点击过sticker且没回到过recent
            stickerPanelManager.flushPendingRecentSticker();
            stickerMainRecyclerViewAdapter.setData(stickerPanelManager.getStickerPanelItemList());
            setCurrentItemPosition(0, 0);
        } else if (StickerUtils.getStickerGroupByName(nextTab) != null && !StickerUtils.getStickerGroupByName(nextTab).isStickerGroupDownloaded()) { // 如果是需要下载的
            List<StickerGroup> stickerGroups = stickerPanelManager.getNeedDownloadStickerGroupList();
            for (int i = 0; i < stickerGroups.size(); i++) {
                if (stickerGroups.get(i).getStickerGroupName().equals(nextTab)) {
                    stickerPanelViewPager.setCurrentItem(i + 1);
                    break;
                }
            }
        } else {
            if (stickerPanelViewPager.getCurrentItem() != 0) {
                stickerPanelViewPager.setCurrentItem(0);
                stickerTabAdapter.setTabSelected(nextTab);
            }
            Pair<Integer, Integer> position = stickerPanelManager.getLastShownItemPositionForTab(nextTab);
            setCurrentItemPosition(position.first, position.second);
        }
    }

    @Override
    public void onStickerClick(Sticker sticker) {
        if (sticker.getStickerUri().trim().length() < 1) {
            HSLog.e("wrong sticker!");
            return;
        }
        HSAnalytics.logEvent("sticker_input", "stickerName", sticker.getStickerName());
        HSAnalytics.logEvent("sticker_input_tab", "stickerGroupName", sticker.getStickerGroupName());
        stickerPanelManager.pendingRecentSticker(sticker);
        String currentPackageName = HSInputMethod.getCurrentHostAppPackageName();
        StickerUtils.share(sticker, currentPackageName);
    }

    public void showPanelView() {
        setHardwareAcceleratedDrawingEnabled(HSInputMethod.isHardwareAcceleratedDrawingEnabled());
        stickerPanelManager.loadData();
    }

    private void setHardwareAcceleratedDrawingEnabled(final boolean enabled) {
        if (!enabled)
            return;
        // TODO: Should use LAYER_TYPE_SOFTWARE when hardware acceleration is off?
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    void onDataLoaded() {
        stickerMainRecyclerViewAdapter.setData(stickerPanelManager.getStickerPanelItemList());
        stickerTabAdapter.setCurrentTab(stickerPanelManager.getDefaultTab(), stickerPanelManager.getDefaultTab());
    }

    public void saveRecent() {
        stickerPanelManager.saveRecent();
    }

    public void removeNotification() {
        HSGlobalNotificationCenter.removeObserver(observer);
    }

    private final class OnStickerScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (dx == 0) {
                return;
            }
            StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) stickerMainPagerRecyclerView.getLayoutManager();
            int[] into = new int[getResources().getInteger(R.integer.config_sticker_row_count)];
            layoutManager.findFirstCompletelyVisibleItemPositions(into);
            String tab = stickerPanelManager.getTabNameForPosition(into[0]);
            if (stickerPanelManager.getStickerPanelItemList().get(into[0]).isDivider()) {
                final int stickerRow = getResources().getInteger(R.integer.config_sticker_row_count);
                tab = stickerPanelManager.getTabNameForPosition(into[0] + stickerRow); // 如果第一个可见item是分隔sticker，则选取下一列内容为当前选中tab的依据
            }
            if (!tab.equals(stickerPanelManager.getCurrentTabName())) {
                stickerPanelManager.setCurrentTabName(tab);
                stickerTabAdapter.setTabSelected(tab);
                stickerTabRecyclerView.scrollToPosition(stickerTabAdapter.getTabIndex(tab));
                if (stickerPanelManager.isRecentTab(tab)) { // 滑到recent
                    stickerPanelManager.flushPendingRecentSticker();
                    stickerMainRecyclerViewAdapter.setData(stickerPanelManager.getStickerPanelItemList());
                }
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                if (!stickerMainPagerRecyclerView.canScrollHorizontally(-1) // 不能向左滑
                        && !stickerPanelManager.isRecentTab(stickerPanelManager.getCurrentTabName()) //当前不是recent
                        && !stickerPanelManager.isRecentEmpty()) { //recent不为空
                    stickerPanelManager.flushPendingRecentSticker();
                    stickerMainRecyclerViewAdapter.setData(stickerPanelManager.getStickerPanelItemList());
                    Pair<Integer, Integer> itemPosition = stickerPanelManager.getLastShownItemPositionForTab(stickerPanelManager.getCurrentTabName());
                    setCurrentItemPosition(itemPosition.first, itemPosition.second);
                }
            }
        }
    }

    private void setCurrentItemPosition(final int position, final int offset) {
        if (stickerMainPagerRecyclerView == null) {
            return;
        }
        ((StaggeredGridLayoutManager) stickerMainPagerRecyclerView.getLayoutManager()).scrollToPositionWithOffset(position, offset);
    }

}
