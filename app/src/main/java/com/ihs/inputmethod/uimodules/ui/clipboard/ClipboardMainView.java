package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.Context;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.alerts.KCAlert;
import com.kc.utils.KCAnalytics;

import java.util.ArrayList;
import java.util.List;


public final class ClipboardMainView extends LinearLayout implements ClipboardActionBar.ClipboardTabChangeListener, ClipboardMainViewProxy, ClipboardRecentViewAdapter.SaveRecentItemToPinsListener, ClipboardPinsViewAdapter.DeleteFromPinsToRecentListener {

    private ClipboardRecentViewAdapter clipboardRecentViewAdapter;
    private ClipboardPinsViewAdapter clipboardPinsViewAdapter;
    private ClipboardActionBar actionBar;
    private RecyclerView clipboardPanelPinsView;
    private RecyclerView clipboardPanelRecentView;
    private ClipboardPresenter clipboardPresenter;
    private FrameLayout recyclerViewGroup;
    private RecyclerView currentView = null;
    int keyboardHeight = HSResourceUtils.getDefaultKeyboardHeight(getResources());
    private KCAlert deleteAlert;
    public final static String PANEL_RECENT = "Recent";
    public final static String PANEL_PIN = "Pins";
    List<String> tabNameList = new ArrayList<String>();

    public ClipboardMainView(Context context) {
        super(context);
        clipboardPresenter = new ClipboardPresenter();
        setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
        setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, keyboardHeight));
        setOrientation(VERTICAL);
        //填充frameLayout(填充RecyclerView)到此LinearLayout
        addRecyclerViewGroup();
        //填充RecyclerView到frameLayout
        addRecyclerViewToPanelViewGroup();
        //填充底部actionbar到此LinearLayout
        addBarView();
        //recyclerView与actionbar建立关联
        tabNameList.add(PANEL_RECENT);
        tabNameList.add(PANEL_PIN);
        actionBar.relateToActionBar(tabNameList);
        //初始的时候recent在显示，相应按钮设为被选中
        actionBar.selectedViewBtn((String) currentView.getTag());
        clipboardPresenter.setClipboardMainViewProxy(this);
    }


    @Override
    public void onTabChange(String tabName) {
        if (tabName.equals(PANEL_RECENT)) {
            addNewView(clipboardPanelRecentView);
        } else if (tabName.equals(PANEL_PIN)) {
            addNewView(clipboardPanelPinsView);
        }
    }

    private void addNewView(RecyclerView recyclerViewToShow) {
        if (currentView != null) {
            //如果此时选中的view　已经在显示，则不做处理
            if (recyclerViewToShow.equals(currentView)) {
                HSLog.e(ClipboardMainView.class.getSimpleName(), "selected clipboard view has been Showing");
                return;
            }
            //如果此时选中的view没有在显示但是已经添加，并已有VIEW在显示，则隐藏原来的view 并显示选中的view，设置为currentView
            inVisibleCurrentPanelView(recyclerViewToShow, currentView);
        } else {
            //如果此时选中的view没有在显示，并且panelViewGroup此时当前没有可显示的View，添加View并显示，设置为currentView
            this.addViewToPanelViewGroup(recyclerViewToShow);
        }
    }


    private void inVisibleCurrentPanelView(RecyclerView panelViewToShow, RecyclerView panelViewToInVisible) {
        if (panelViewToInVisible.getParent() == null) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) panelViewToInVisible.getLayoutParams();
            if (layoutParams == null) {
                layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            }
            recyclerViewGroup.addView(panelViewToInVisible, layoutParams);
        }
        panelViewToInVisible.setVisibility(INVISIBLE);
        addViewToPanelViewGroup(panelViewToShow);

    }

    @Override
    public void notifyRecentDataSetChange() {
        clipboardRecentViewAdapter.dataChangeAndRefresh(clipboardPresenter.getclipRecentData());
    }

    @Override
    public void notifyPinsDataSetChange() {
        clipboardPinsViewAdapter.dataChangeAndRefresh(clipboardPresenter.getClipPinsData());
    }


    public void showDeletedSuggestionAlert(String pinsContentItem) {
        if (deleteAlert == null) {
            deleteAlert = new KCAlert.Builder(HSApplication.getContext())
                    .setTitle(getResources().getString(R.string.clipboard_delete_title))
                    .setMessage(getResources().getString(R.string.clipboard_disable_suggestion_detail))
                    .setPositiveButton(getResources().getString(R.string.cancel), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            deleteAlert.dismiss();
                            KCAnalytics.logEvent("keyboard_clipboard_pin_cancelled");
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.clipboard_delete_pin), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            clipboardPresenter.clipDataOperateDeletePins(pinsContentItem);
                            deleteAlert.dismiss();
                        }
                    }).build();
        }
        deleteAlert.show();

    }

     @Override
     public void changeToShowPinsView() {
        addNewView(clipboardPanelPinsView);
        actionBar.selectedViewBtn((String) currentView.getTag());
    }
    @Override
    public void changeToShowRecentView() {
        addNewView(clipboardPanelRecentView);
        actionBar.selectedViewBtn((String) currentView.getTag());
    }

    private void addRecyclerViewToPanelViewGroup() {
        LinearLayoutManager recentLayoutManager = new LinearLayoutManager(getContext());
        recentLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        LinearLayoutManager pinsLayoutManager = new LinearLayoutManager(getContext());
        pinsLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        clipboardPanelRecentView = new RecyclerView(getContext());
        clipboardPanelPinsView = new RecyclerView(getContext());
        clipboardPanelRecentView.setLayoutManager(recentLayoutManager);
        clipboardPanelPinsView.setLayoutManager(pinsLayoutManager);
        clipboardPanelRecentView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        clipboardPanelPinsView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        recyclerViewGroup.addView(clipboardPanelRecentView, new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        recyclerViewGroup.addView(clipboardPanelPinsView, new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        HSLog.d(ClipboardMainView.class.getSimpleName(), "clipboard mainView created  clipboardRecentData = " + clipboardPresenter.getclipRecentData() + "    clipboardPinsData  = " + clipboardPresenter.getClipPinsData().toString());
        clipboardRecentViewAdapter = new ClipboardRecentViewAdapter(clipboardPresenter.getclipRecentData(), this);
        clipboardPinsViewAdapter = new ClipboardPinsViewAdapter(clipboardPresenter.getClipPinsData(), this);
        clipboardPanelRecentView.setAdapter(clipboardRecentViewAdapter);
        clipboardPanelPinsView.setAdapter(clipboardPinsViewAdapter);
        clipboardPanelRecentView.setTag(PANEL_RECENT);
        clipboardPanelPinsView.setTag(PANEL_PIN);
        clipboardPanelRecentView.setVisibility(VISIBLE);
        currentView = clipboardPanelRecentView;
        clipboardPanelPinsView.setVisibility(INVISIBLE);

    }


    private void addBarView() {
        actionBar = (ClipboardActionBar) View.inflate(HSApplication.getContext(), R.layout.clipboard_action_bar, null);
        actionBar.setClipboardTabChangeListener(this);
        LinearLayout.LayoutParams actionBarLayoutParams = (LinearLayout.LayoutParams) actionBar.getLayoutParams();
        if (actionBarLayoutParams == null) {
            actionBarLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        }
        addView(actionBar, actionBarLayoutParams);
    }

    private void addRecyclerViewGroup() {
        recyclerViewGroup = new FrameLayout(this.getContext());
        LinearLayout.LayoutParams panelViewGroupLayoutParams = (LinearLayout.LayoutParams) recyclerViewGroup.getLayoutParams();
        if (panelViewGroupLayoutParams == null) {
            panelViewGroupLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, keyboardHeight - getResources().getDimensionPixelSize(R.dimen.emoticon_panel_actionbar_height));

        }
        //防止点击事件穿透
        recyclerViewGroup.setClickable(true);
        addView(recyclerViewGroup, panelViewGroupLayoutParams);
    }

    private void addViewToPanelViewGroup(RecyclerView panelViewToShow) {
        if (panelViewToShow != null && panelViewToShow.getParent() == null) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) panelViewToShow.getLayoutParams();
            if (layoutParams == null) {
                layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            }

            recyclerViewGroup.addView(panelViewToShow, layoutParams);
        }
        if (panelViewToShow != null) {
            panelViewToShow.setVisibility(VISIBLE);
        }
        currentView = panelViewToShow;
    }

    //点击recent的条目的pins按钮时执行的回调
    @Override
    public void saveToPins(String itemPinsContent) {
        clipboardPresenter.clipDataOperateSaveToPins(itemPinsContent);
        KCAnalytics.logEvent("keyboard_clipboard_pin_clicked");

    }

    //点击pins的条目的删除按钮时的回调
    @Override
    public void deletePinsItem(String pinsContentItem) {

        showDeletedSuggestionAlert(pinsContentItem);
        KCAnalytics.logEvent("keyboard_clipboard_OneDeleted");
    }

}
