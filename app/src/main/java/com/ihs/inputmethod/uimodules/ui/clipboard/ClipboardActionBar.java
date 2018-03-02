package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.R;
import com.kc.utils.KCAnalytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ClipboardActionBar extends LinearLayout implements View.OnClickListener {

    public final static String PANEL_RECENT = "Recent";
    public final static String PANEL_PIN = "Pins";
    private String recentRecyclerViewName = "";
    private String pinsRecyclerViewName = "";
    private Map<String, TextView> relateViewsNameAndTabViewMap = new HashMap<String, TextView>();
    private List<String> relateViewsName = new ArrayList<String>();
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

    public void setRecyclerViewRelateToActionBar(String clipboardPanelRecentViewName, String clipboardPanelPinsViewName) {
        List<ClipboardActionbarTab> actionbarTabs = new ArrayList<>();
        recentRecyclerViewName = clipboardPanelRecentViewName;
        pinsRecyclerViewName = clipboardPanelPinsViewName;
        actionbarTabs.add(new ClipboardActionbarTab(PANEL_RECENT, recentRecyclerViewName));
        actionbarTabs.add(new ClipboardActionbarTab(PANEL_PIN, pinsRecyclerViewName));
        final int height = getResources().getDimensionPixelSize(R.dimen.clipboard_panel_actionbar_height);
        final int actionBarAmount = actionbarTabs.size();
        for (int i = 0; i < actionBarAmount; i++) {
            final String tabName = actionbarTabs.get(i).tabName;
            final String relateViewName = actionbarTabs.get(i).relateViewName;
            final LayoutParams params = new LayoutParams(0, height, 1.0f);

            TextView clipActionBarBtnView = (TextView) inflate(getContext(), R.layout.clipboard_bar_button, null);
            ((TextView) clipActionBarBtnView.findViewById(R.id.tv_title)).setText(tabName);
            clipActionBarBtnView.setTag(relateViewName);
            clipActionBarBtnView.setOnClickListener(this);
            clipActionBarBtnView.setTextColor(Color.WHITE);
            clipActionBarBtnView.setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
            addView(clipActionBarBtnView, params);
            relateViewsName.add(relateViewName);
            relateViewsNameAndTabViewMap.put(relateViewName, clipActionBarBtnView);
        }
    }

    interface ClipboardTabChangeListener {

        void onTabChange(String selectedViewName);
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
        String relateViewName = (String) v.getTag();
            if (!android.text.TextUtils.isEmpty(relateViewName)) {
                clipboardTabChangeListener.onTabChange(relateViewName);
                selectedViewBtn(relateViewName);
                HSLog.d(ClipboardActionBar.class.getSimpleName(), " to show " + relateViewName.toString());
                if(relateViewName.equals(pinsRecyclerViewName)){
                    KCAnalytics.logEvent("keyboard_clipboard_pin_clicked");
                }
            }
    }

    void selectedViewBtn(String recyclerViewName) {
        for (String recyclerView : relateViewsName) {
            TextView view = relateViewsNameAndTabViewMap.get(recyclerView);
            if (view != null) {
                view.setSelected(false);
                view.setTextColor(Color.WHITE);
                view.setTextColor(Color.argb(128, 255, 255, 255));
                view.setBackgroundColor(Color.argb(180, 0, 0, 0));
            }
        }

        TextView view = relateViewsNameAndTabViewMap.get(recyclerViewName);
        if (view != null) {
            view.setSelected(true);
        }
        if (view != null) {
            view.setTextColor(Color.WHITE);
            view.setBackgroundColor(Color.argb(0, 0, 0, 0));
        }
    }

    private class ClipboardActionbarTab {
        String tabName;
        public String relateViewName;

        ClipboardActionbarTab(String tabName, String relateViewName) {
            this.tabName = tabName;
            this.relateViewName = relateViewName;
        }
    }
}
