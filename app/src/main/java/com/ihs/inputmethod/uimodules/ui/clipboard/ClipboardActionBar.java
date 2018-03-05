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

import java.util.ArrayList;
import java.util.List;

public final class ClipboardActionBar extends LinearLayout implements View.OnClickListener {

    private ClipboardTabChangeListener clipboardTabChangeListener;
    private  List<TextView> tabViewList = new ArrayList<TextView>();
    private List<String> actionbarTabsNames;

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

    public void relateToActionBar(List<String> tabNameList) {
        actionbarTabsNames = tabNameList;
        final int height = getResources().getDimensionPixelSize(R.dimen.clipboard_panel_actionbar_height);
        final int actionBarAmount = actionbarTabsNames.size();
        for (int i = 0; i < actionBarAmount; i++) {
            final String tabName = actionbarTabsNames.get(i);
            final LayoutParams params = new LayoutParams(0, height, 1.0f);
            TextView clipActionBarBtnView = (TextView) inflate(getContext(), R.layout.clipboard_bar_button, null);
            ((TextView) clipActionBarBtnView.findViewById(R.id.tv_title)).setText(tabName);
            clipActionBarBtnView.setTag(tabName);
            clipActionBarBtnView.setOnClickListener(this);
            clipActionBarBtnView.setTextColor(Color.WHITE);
            clipActionBarBtnView.setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
            addView(clipActionBarBtnView, params);
            tabViewList.add(clipActionBarBtnView);
        }
    }

    interface ClipboardTabChangeListener {

        void onTabChange(String tabName);
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
        String tabName = (String) v.getTag();
            if (!android.text.TextUtils.isEmpty(tabName)) {
                clipboardTabChangeListener.onTabChange(tabName);
                selectedViewBtn(tabName);
                HSLog.d(ClipboardActionBar.class.getSimpleName(), " to show " + tabName);
            }
    }

    void selectedViewBtn(String tabName) {
        for (TextView tabView : tabViewList) {
            if (tabView != null) {
                tabView.setSelected(false);
                tabView.setTextColor(Color.WHITE);
                tabView.setTextColor(Color.argb(128, 255, 255, 255));
                int color = HSKeyboardThemeManager.getCurrentTheme().getDominantColor();
                tabView.setBackgroundColor(Color.argb(0, Color.red(color), Color.green(color), Color.blue(color)));
            }
        }
        TextView selectedTabView  = tabViewList.get(actionbarTabsNames.indexOf(tabName));
        if (selectedTabView != null) {
            selectedTabView.setSelected(true);
        }
        if (selectedTabView != null) {
            selectedTabView.setTextColor(Color.WHITE);
            int color = HSKeyboardThemeManager.getCurrentTheme().getDominantColor();
            selectedTabView.setBackgroundColor(Color.argb(180, Color.red(color), Color.green(color), Color.blue(color)));
        }
    }

}
