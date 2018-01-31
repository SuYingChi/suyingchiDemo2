package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import java.util.LinkedList;


public final class ClipboardMainView extends RelativeLayout implements ClipBoardActionBar.ClipboardTabChangeListener,ClipboardPresenter.ClipDataResult {
    final static int SHOW_NO_KEEP = 0;
    final static int SHOW_KEEP = 3;
	final ClipBoardActionBar actionBar;
    private RecyclerView clipboardPanelPinsView;
    private RecyclerView clipboardPanelRecentView;
    private ClipboardPresenter clipboardPresenter;
    private FrameLayout panelViewGroup;
    //
    private RecyclerView currentView = null;
    private FrameLayout barViewGroup;
    private RelativeLayout groupContainer = null;
    private int heightMode = -1;

    //
    public ClipboardMainView() {
        super(HSApplication.getContext());
		this.setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
		actionBar= (ClipBoardActionBar) View.inflate(HSApplication.getContext(), R.layout.clipboard_action_bar,null);
        barViewGroup = new FrameLayout(HSApplication.getContext());
        barViewGroup.setId(R.id.clipboard_barsViewGroup);
		setBarView(actionBar);
		//actionBar.setPanelActionListener(this);
        actionBar.setClipboardTabChangeListener(this);
		final Resources res = getContext().getResources();
		final int height = HSResourceUtils.getDefaultKeyboardHeight(res) +res.getDimensionPixelSize(R.dimen.config_suggestions_strip_height);
		//控制该自定义view高度
		setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        clipboardPresenter= ClipboardPresenter.getInstance();
        this.panelViewGroup = new FrameLayout(this.getContext());
        this.panelViewGroup.setId(R.id.clipboard_panelViewGroup);
        initRecyclerView();
        clipboardPresenter = ClipboardPresenter.getInstance();
        clipboardPresenter.setClipboardPanelRecentView(clipboardPanelRecentView);
        clipboardPresenter.setClipboardPanelPinsView(clipboardPanelPinsView);
        clipboardPresenter.setClipDataResult(this);
        showView(clipboardPanelRecentView);
        this.adjustViewPosition();

	}

    private void initRecyclerView() {
        LinearLayoutManager layoutManager= new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        clipboardPanelRecentView = new RecyclerView(getContext());
        clipboardPanelPinsView   = new RecyclerView(getContext());
        clipboardPanelRecentView.setLayoutManager(layoutManager);
        clipboardPanelPinsView.setLayoutManager(layoutManager);
        clipboardPanelRecentView.setAdapter(ClipboardPresenter.getInstance().getClipboardRecentViewAdapter());
        clipboardPanelPinsView.setAdapter(ClipboardPresenter.getInstance().getClipboardPinsViewAdapter());

    }

 /*   @Override
	public void showPanel(Class panelClass) {
		super.showPanel(panelClass);
		actionBar.selectPanelBtn(panelClass);
	}*/

/*	public void showLastPanel() {
		Class<?> clazz=actionBar.getPanelClass(HSEmoticonActionBar.getLastPanelName());
		showPanel(clazz);
	}*/

    //如果此时选中的view　已经在显示，则不做处理
    //如果此时选中的view没有在显示，则隐藏原来的view 并显示选中的view
    //如果此时选中的view 没有在显示，并且没有被添加到panel里的view的panelViewGroup的时候，先添加再显示
    //view有回调会实时根据用户操作更新重新绘制，这里不用考虑，直接显示就行
    //完成显示后 调selectedViewBtn将对应的btn设为selected
////////////////////////////////改写
    @Override
    public void showView(RecyclerView recyclerView) {
        this.addNewView(recyclerView, false, SHOW_NO_KEEP);
    }

    public void showViewAndKeepSelf(RecyclerView recyclerView) {
        this.addNewView(recyclerView, true, SHOW_KEEP);
    }

    private void addNewView(RecyclerView recyclerView, boolean keepCurrent, int showingType) {
            if(this.currentView != null) {
                if(recyclerView == this.currentView) {
                    HSLog.e("panelView", "panelView Showed");
                    return;
                }

                removeCurrentPanelView(keepCurrent, showingType, recyclerView,this.currentView);
            } else {
                this.addPanelViewToRoot(recyclerView);
            }

    }
    private void removeViewFromParent(View view) {
        if(view.getParent() != null) {
            ((ViewGroup)view.getParent()).removeView(view);
        }

    }
    private void addPanelViewToRoot(RecyclerView recyclerView) {
        this.currentView = recyclerView;

        LayoutParams layoutParams = new LayoutParams(-1, -1);
        if(currentView.getParent() != null) {
            this.removeViewFromParent(currentView);
        }
        this.panelViewGroup.addView(currentView, layoutParams);
    }

    private void removeCurrentPanelView(boolean keepCurrent, int showingType, @Nullable RecyclerView panelViewToShow, RecyclerView panelViewToRemove) {
        switch(showingType) {
            case SHOW_NO_KEEP:
                this.panelViewGroup.removeAllViews();
                break;
            case SHOW_KEEP:
                this.panelViewGroup.removeView(panelViewToRemove);
        }

        if(!keepCurrent ) {
            removeViewFromParent(panelViewToRemove);
            if(this.currentView != null && this.currentView == panelViewToRemove) {
                this.currentView = null;
            }

            System.gc();
            HSLog.e("cause GC");
        } else {

        }

        if(panelViewToShow != null) {
            this.addPanelViewToRoot(panelViewToShow);
            actionBar.selectedViewBtn(panelViewToShow);
        }

    }

    @Override
    public void noData(String spName) {
        //空数据占位符 防止在顶上
        android.widget.RelativeLayout.LayoutParams barParams = (android.widget.RelativeLayout.LayoutParams)this.barViewGroup.getLayoutParams();
        android.widget.RelativeLayout.LayoutParams panelParams = (android.widget.RelativeLayout.LayoutParams)this.panelViewGroup.getLayoutParams();
        barParams.addRule(12);
    }

    private void setBarView(View view) {
        if(view.getParent() != null) {
            ((ViewGroup)view.getParent()).removeView(view);
        }

        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        if(layoutParams == null) {
            layoutParams = new LayoutParams(-1, -2);
        }
        barViewGroup.addView(view, layoutParams);
    }

    private void adjustViewPosition() {
        android.widget.RelativeLayout.LayoutParams barParams = (android.widget.RelativeLayout.LayoutParams)this.barViewGroup.getLayoutParams();
        android.widget.RelativeLayout.LayoutParams panelParams = (android.widget.RelativeLayout.LayoutParams)this.panelViewGroup.getLayoutParams();
        android.widget.RelativeLayout.LayoutParams groupContainerParams = (android.widget.RelativeLayout.LayoutParams)this.groupContainer.getLayoutParams();
        boolean needAddView = false;
        if(barParams == null || panelParams == null  || groupContainerParams == null) {
            needAddView = true;
            barParams = new android.widget.RelativeLayout.LayoutParams(-1, -2);
            panelParams = new android.widget.RelativeLayout.LayoutParams(-1, -2);
            groupContainerParams = new android.widget.RelativeLayout.LayoutParams(-1, -2);
        }
        barParams.addRule(12, -1);
        panelParams.addRule(12, -1);
        if(this.heightMode == -1) {
            groupContainerParams.addRule(12, -1);
        }
        barParams.addRule(3, this.panelViewGroup.getId());
        if(needAddView) {
            this.addView(this.groupContainer, groupContainerParams);
            this.groupContainer.addView(this.barViewGroup, barParams);
            this.groupContainer.addView(this.panelViewGroup, panelParams);
        } else {
            this.requestLayout();
        }
        }


    public void setLayoutParams(LayoutParams params) {
        if(this.heightMode != params.height) {
           this.heightMode = params.height;
            this.adjustViewPosition();
        }

        super.setLayoutParams(params);
    }
}