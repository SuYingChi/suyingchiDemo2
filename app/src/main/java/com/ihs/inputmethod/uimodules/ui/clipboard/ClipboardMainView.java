package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.panelcontainer.KeyboardPanelSwitchContainer;

public final class ClipboardMainView extends KeyboardPanelSwitchContainer implements ClipBoardActionBar.ClipboardTabChangeListener,ClipboardPresenter.ClipDataResult{

	final ClipBoardActionBar actionBar;
    private ClipboardPanelPinsView clipboardPanelPinsView;
    private ClipboardPanelRecentView clipboardPanelRecentView;
    private ClipboardPresenter clipboardPresenter;
    private ViewGroup panelViewGroup;

    public ClipboardMainView() {
        super();
		this.setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
		setBarPosition(BAR_BOTTOM);
		actionBar= (ClipBoardActionBar) View.inflate(HSApplication.getContext(), R.layout.clipboard_action_bar,null);
		setBarView(actionBar);
		//actionBar.setPanelActionListener(this);
        actionBar.setClipboardTabChangeListener(this);
		final Resources res = getContext().getResources();
		final int height = HSResourceUtils.getDefaultKeyboardHeight(res) +res.getDimensionPixelSize(R.dimen.config_suggestions_strip_height);
		setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        clipboardPresenter= ClipboardPresenter.getInstance();
	}

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        clipboardPanelRecentView = (ClipboardPanelRecentView)View.inflate(HSApplication.getContext(),R.layout.clipboard_recent_layout,null);
        clipboardPanelPinsView = (ClipboardPanelPinsView)View.inflate(HSApplication.getContext(),R.layout.clipboard_pins_layout,null);
        clipboardPresenter = ClipboardPresenter.getInstance();
        clipboardPresenter.setClipboardPanelRecentView(clipboardPanelRecentView);
        clipboardPresenter.setClipboardPanelPinsView(clipboardPanelPinsView);
        ClipboardMonitor.getInstance().registerClipboardMonitor(clipboardPresenter);
        clipboardPresenter.setClipDataResult(this);
        panelViewGroup = getPanelViewGroup();
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

	@Override
	public void onDestroy() {
		super.onDestroy();
		actionBar.release();
	}

    @Override
    public void showView(RecyclerView selectedView,RecyclerView noSelectedRecyclerView) {
        //如果此时选中的view　已经在显示，则不做处理
	    //如果此时选中的view没有在显示，则隐藏原来的view 并显示选中的view
            panelViewGroup.removeView(noSelectedRecyclerView);
            panelViewGroup.addView(selectedView);
    }

    @Override
    public void noData(String spName) {
        //占位符 防止在顶上
    }
}