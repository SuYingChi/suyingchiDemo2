package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public  class ClipboardActionBar extends LinearLayout implements View.OnClickListener {

    private onClipboardTabChangeListener onClipboardTabChangeListener;
    private Map<String,TextView> tabViewsMap = new HashMap<String,TextView>();
    private int backgroundColor;

    public ClipboardActionBar(Context context) {
        this(context, null);
    }

    public ClipboardActionBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClipboardActionBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    void setOnClipboardTabChangeListener(onClipboardTabChangeListener onClipboardTabChangeListener) {
        this.onClipboardTabChangeListener = onClipboardTabChangeListener;
    }

    public void setActionBarTabName(List<String> tabNameList) {
        backgroundColor = HSKeyboardThemeManager.getCurrentTheme().getDominantColor();
        final int actionBarTabSize = tabNameList.size();
        for (int i = 0; i < actionBarTabSize; i++) {
            final String tabName = tabNameList.get(i);
            final LayoutParams params = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
            TextView clipActionBarBtnView =  new TextView(getContext());
            clipActionBarBtnView.setGravity(Gravity.CENTER);
            clipActionBarBtnView.setTag(tabName);
            clipActionBarBtnView.setOnClickListener(this);
            clipActionBarBtnView.setTextColor(Color.WHITE);
            clipActionBarBtnView.setBackgroundColor(backgroundColor);
            addView(clipActionBarBtnView, params);
            tabViewsMap.put(tabName,clipActionBarBtnView);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
         final int clipboardActionBarWidth = HSApplication.getContext().getResources().getDisplayMetrics().widthPixels;
         final int clipboardActionBarHeight = HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.emoticon_panel_actionbar_height);
        setMeasuredDimension(clipboardActionBarWidth, clipboardActionBarHeight);
    }

    @Override
    public void onClick(View v) {
        String tabName = (String) v.getTag();
        if (!android.text.TextUtils.isEmpty(tabName)) {
            onClipboardTabChangeListener.onClipboardTabChange(tabName);
            setCurrentTab(tabName);
            if(tabName.equals(ClipboardConstants.PANEL_PIN)){
                HSAnalytics.logEvent("keyboard_clipboard_pin_tab_clicked");
            }
            HSLog.d(ClipboardActionBar.class.getSimpleName(), " to show " + tabName);
        }
    }

    void setCurrentTab(String tabName) {
        for (TextView tabView : tabViewsMap.values()) {
            if (tabView != null) {
                tabView.setSelected(false);
                tabView.setTextColor(Color.WHITE);
                tabView.setTextColor(Color.argb(128, 255, 255, 255));
                tabView.setBackgroundColor(Color.argb(0, Color.red(backgroundColor), Color.green(backgroundColor), Color.blue(backgroundColor)));
            }
        }
        TextView currentTabView = tabViewsMap.get(tabName);
        if (currentTabView != null) {
            currentTabView.setSelected(true);
        }
        if (currentTabView != null) {
            currentTabView.setTextColor(Color.WHITE);
            currentTabView.setBackgroundColor(Color.argb(180, Color.red(backgroundColor), Color.green(backgroundColor), Color.blue(backgroundColor)));
        }
    }
    interface onClipboardTabChangeListener {

        void onClipboardTabChange(String tabName);
    }
}
