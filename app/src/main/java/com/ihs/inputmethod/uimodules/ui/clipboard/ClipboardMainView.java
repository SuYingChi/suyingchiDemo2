package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.res.Resources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.R;

import static com.ihs.inputmethod.uimodules.ui.clipboard.ClipboardPresenter.PINS;
import static com.ihs.inputmethod.uimodules.ui.clipboard.ClipboardPresenter.RECENT;


public final class ClipboardMainView extends RelativeLayout implements ClipBoardActionBar.ClipboardTabChangeListener, ClipboardPresenter.OnAdapterCreatedListener {

    private ClipBoardActionBar actionBar;
    private Resources res = getContext().getResources();
    private final int KEYBOARD_HEIGHT = HSResourceUtils.getDefaultKeyboardHeight(res) + res.getDimensionPixelSize(R.dimen.config_suggestions_strip_height);

    private RecyclerView clipboardPanelPinsView;
    private RecyclerView clipboardPanelRecentView;
    private ClipboardPresenter clipboardPresenter;
    private FrameLayout panelViewGroup;
    private RecyclerView currentView = null;
    private RelativeLayout groupContainer = null;
    private int heightMode = ViewGroup.LayoutParams.MATCH_PARENT;

    public ClipboardMainView() {
        super(HSApplication.getContext());
        setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        clipboardPanelRecentView = new RecyclerView(getContext());
        clipboardPanelPinsView = new RecyclerView(getContext());
        clipboardPanelRecentView.setLayoutManager(layoutManager);
        clipboardPanelPinsView.setLayoutManager(layoutManager);


        actionBar = (ClipBoardActionBar) View.inflate(HSApplication.getContext(), R.layout.clipboard_action_bar, null);
        actionBar.setId(R.id.clipboard_barsViewGroup);
        setBarView();
        actionBar.setClipboardTabChangeListener(this);
        actionBar.setClipboardPinsRecyclerView(clipboardPanelPinsView);
        actionBar.setClipboardRecentRecyclerView(clipboardPanelRecentView);

        panelViewGroup = new FrameLayout(this.getContext());
        panelViewGroup.setId(R.id.clipboard_panelViewGroup);
        panelViewGroup.addView(clipboardPanelRecentView,new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        panelViewGroup.addView(clipboardPanelPinsView,new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        clipboardPanelRecentView.setVisibility(VISIBLE);
        clipboardPanelPinsView.setVisibility(INVISIBLE);
        actionBar.selectedViewBtn(clipboardPanelRecentView);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, KEYBOARD_HEIGHT));
        adjustViewPosition();

        clipboardPresenter = ClipboardPresenter.getInstance();
        clipboardPresenter.setOnAdapterCreatedListener(this);

    }

    //view有回调会实时根据用户操作更新，
    @Override
    public void showView(RecyclerView selectedView) {
        addNewView(selectedView);
        //完成显示后 调selectedViewBtn将对应的btn设为selected
        actionBar.selectedViewBtn(selectedView);
    }

    private void addNewView(RecyclerView recyclerViewToShow) {
        if (currentView != null) {
            //如果此时选中的view　已经在显示，则不做处理
            if (recyclerViewToShow.equals(currentView)) {
                HSLog.e("panelView", "panelView Showed");
                return;
            }
            //如果此时选中的view没有在显示但是已经添加，并已有VIEW在显示，则隐藏原来的view 并显示选中的view，设置为currentView
            inVisibleCurrentPanelView(recyclerViewToShow, currentView);
        } else {
            //如果此时选中的view没有在显示，并且panelViewGroup此时当前没有可显示的View，添加View并显示，设置为currentView
            this.addViewToPanelViewGroup(recyclerViewToShow);
        }
    }

    private void addViewToPanelViewGroup(RecyclerView panelViewToShow) {
        if (panelViewToShow != null && panelViewToShow.getParent() == null) {
            android.widget.RelativeLayout.LayoutParams layoutParams = (android.widget.RelativeLayout.LayoutParams) panelViewToShow.getLayoutParams();
            if (layoutParams == null) {
                layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            }
            layoutParams.addRule(CENTER_IN_PARENT);
            panelViewGroup.addView(panelViewToShow);
        }
        if (panelViewToShow != null) {
            panelViewToShow.setVisibility(VISIBLE);
        }
        currentView = panelViewToShow;
    }

    private void inVisibleCurrentPanelView(RecyclerView panelViewToShow, RecyclerView panelViewToInVisible) {
        if (panelViewToInVisible.getParent() == null) {
            android.widget.RelativeLayout.LayoutParams layoutParams = (android.widget.RelativeLayout.LayoutParams) panelViewToInVisible.getLayoutParams();
            if (layoutParams == null) {
                layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            }
            layoutParams.addRule(CENTER_IN_PARENT);
            panelViewGroup.addView(panelViewToInVisible, layoutParams);
        }
        panelViewToInVisible.setVisibility(INVISIBLE);
        addViewToPanelViewGroup(panelViewToShow);

    }

    private void setBarView() {
        if (actionBar.getParent() != null) {
            ((ViewGroup) actionBar.getParent()).removeView(actionBar);
        }

        android.widget.RelativeLayout.LayoutParams actionBarLayoutParams = (android.widget.RelativeLayout.LayoutParams) actionBar.getLayoutParams();
        if (actionBarLayoutParams == null) {
            actionBarLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        }
        actionBarLayoutParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);
        groupContainer.addView(actionBar, actionBarLayoutParams);
    }

    private void adjustViewPosition() {
        android.widget.RelativeLayout.LayoutParams barParams = (android.widget.RelativeLayout.LayoutParams) this.actionBar.getLayoutParams();
        android.widget.RelativeLayout.LayoutParams panelParams = (android.widget.RelativeLayout.LayoutParams) this.panelViewGroup.getLayoutParams();
        android.widget.RelativeLayout.LayoutParams groupContainerParams = (android.widget.RelativeLayout.LayoutParams) this.groupContainer.getLayoutParams();
        boolean needAddView = false;
        if (barParams == null || panelParams == null || groupContainerParams == null) {
            needAddView = true;
            barParams = new android.widget.RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            panelParams = new android.widget.RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            groupContainerParams = new android.widget.RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, KEYBOARD_HEIGHT);
        }
        barParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);
        panelParams.addRule(ALIGN_PARENT_TOP, TRUE);
        groupContainerParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);
        panelParams.addRule(ABOVE, actionBar.getId());
        if (needAddView) {
            addView(this.groupContainer, groupContainerParams);
            groupContainer.addView(actionBar, barParams);
            groupContainer.addView(panelViewGroup, panelParams);
        } else {
            requestLayout();
        }
    }


    public void setLayoutParams(LayoutParams params) {
        if (this.heightMode != params.height) {
            this.heightMode = params.height;
            this.adjustViewPosition();
        }
        super.setLayoutParams(params);
    }

    @Override
    public void adapterCreated(RecyclerView.Adapter adapter, int adapterType) {
        if (adapterType == RECENT) {
            clipboardPanelRecentView.setAdapter(adapter);
        } else if (adapterType == PINS) {
            clipboardPanelPinsView.setAdapter(adapter);
        }
    }
}