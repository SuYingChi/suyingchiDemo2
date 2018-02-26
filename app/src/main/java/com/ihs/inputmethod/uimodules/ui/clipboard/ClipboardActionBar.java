package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ClipboardActionBar extends LinearLayout implements View.OnClickListener {

    public final static String PANEL_RECENT = "Recent";
    public final static String PANEL_PIN = "Pins";
    private RecyclerView recentRecyclerView = null;
    private RecyclerView pinsRecyclerView = null;
    private Map<RecyclerView, TextView> btnMap = new HashMap<>();
    private Map<String, RecyclerView> clipboardViews = new HashMap<>();
    private ClipboardTabChangeListener clipboardTabChangeListener;

    public ClipboardActionBar(Context context) {
        this(context, null);
    }

    public ClipboardActionBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClipboardActionBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    void setClipboardTabChangeListener(ClipboardTabChangeListener clipboardTabChangeListener) {
        this.clipboardTabChangeListener = clipboardTabChangeListener;
    }

    public void setRecyclerViewRelateToActionBar(RecyclerView clipboardPanelRecentView, RecyclerView clipboardPanelPinsView) {
        List<ClipboardActionbarTab> actionbarTabs = new ArrayList<>();
        recentRecyclerView = clipboardPanelRecentView;
        pinsRecyclerView = clipboardPanelPinsView;
        actionbarTabs.add(new ClipboardActionbarTab(PANEL_RECENT, recentRecyclerView));
        actionbarTabs.add(new ClipboardActionbarTab(PANEL_PIN, pinsRecyclerView));
        final int height = getResources().getDimensionPixelSize(R.dimen.clipboard_panel_actionbar_height);
        final int actionBarAmount = actionbarTabs.size();
        for (int i = 0; i < actionBarAmount; i++) {
            final String viewName = actionbarTabs.get(i).viewName;
            final RecyclerView recyclerView = actionbarTabs.get(i).view;
            final LayoutParams params = new LayoutParams(0, height, 1.0f);

            TextView clipActionBarBtnView = (TextView) inflate(getContext(), R.layout.clipboard_bar_button, null);
            ((TextView) clipActionBarBtnView.findViewById(R.id.tv_title)).setText(viewName);
            clipActionBarBtnView.setTag(viewName);
            clipActionBarBtnView.setOnClickListener(this);
            clipActionBarBtnView.setTextColor(Color.WHITE);
            clipActionBarBtnView.setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
            addView(clipActionBarBtnView, params);
            clipboardViews.put(viewName, recyclerView);
            btnMap.put(recyclerView, clipActionBarBtnView);
        }
    }

    interface ClipboardTabChangeListener {

        void showView(RecyclerView selectedView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final Resources res = getContext().getResources();
        final int width = res.getDisplayMetrics().widthPixels;
        final int height = res.getDimensionPixelSize(R.dimen.emoticon_panel_actionbar_height);
        setMeasuredDimension(width, height);
    }

    @Override
    public void onClick(View v) {
        final Object tag = v.getTag();
        if (tag != null && tag instanceof String) {
            RecyclerView view = clipboardViews.get(tag);
            if (view != null) {
                clipboardTabChangeListener.showView(view);
                selectedViewBtn(view);
                HSLog.d(ClipboardActionBar.class.getSimpleName(), " to show " + tag.toString());
            }
        }
    }

    void selectedViewBtn(RecyclerView recyclerView) {
        for (RecyclerView recyclerViewValue : clipboardViews.values()) {
            TextView view = btnMap.get(recyclerViewValue);
            if (view != null) {
                view.setSelected(false);
                view.setTextColor(Color.WHITE);
                view.setTextColor(Color.argb(128, 255, 255, 255));
                view.setBackgroundColor(Color.argb(180, 0, 0, 0));
            }
        }

        TextView view = btnMap.get(recyclerView);
        if (view != null) {
            view.setSelected(true);
        }
        if (view != null) {
            view.setTextColor(Color.WHITE);
            view.setBackgroundColor(Color.argb(0, 0, 0, 0));
        }
    }

    private class ClipboardActionbarTab {
        String viewName;
        public RecyclerView view;

        ClipboardActionbarTab(String viewName, RecyclerView view) {
            this.viewName = viewName;
            this.view = view;
        }
    }
}
