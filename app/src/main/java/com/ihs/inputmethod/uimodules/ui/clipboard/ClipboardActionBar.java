package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.R;
import com.kc.utils.KCAnalytics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public  class ClipboardActionBar extends LinearLayout implements View.OnClickListener {

    private OnClipboardTabChangeListener onClipboardTabChangeListener;
    private Map<String,TextView> tabViewsMap = new HashMap<String,TextView>();
    private final int DOMINANT_COLOR = HSKeyboardThemeManager.getCurrentTheme().getDominantColor();
    private final int ACTION_BAR_HEIGHT = getContext().getResources().getDimensionPixelSize(R.dimen.emoticon_panel_actionbar_height);
    private final int  TEXT_COLOR = HSKeyboardThemeManager.getCurrentTheme().isDarkBg()?Color.WHITE:Color.BLACK;
    public ClipboardActionBar(Context context) {
        super(context);
        setOrientation(HORIZONTAL);
        setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ACTION_BAR_HEIGHT ));
    }


    void setOnClipboardTabChangeListener(OnClipboardTabChangeListener onClipboardTabChangeListener) {
        this.onClipboardTabChangeListener = onClipboardTabChangeListener;
    }

    public void setActionBarTabName(List<String> tabNameList) {
        final int actionBarTabSize = tabNameList.size();
        for (int i = 0; i < actionBarTabSize; i++) {
            final String tabName = tabNameList.get(i);
            final LayoutParams params = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
            TextView clipActionBarBtnView =  new TextView(getContext());
            clipActionBarBtnView.setGravity(Gravity.CENTER);
            clipActionBarBtnView.setText(tabName);
            clipActionBarBtnView.setTextColor(Color.argb(128, 255, 255, 255));
            clipActionBarBtnView.setBackgroundColor(Color.argb(0, Color.red(DOMINANT_COLOR), Color.green(DOMINANT_COLOR), Color.blue(DOMINANT_COLOR)));
            clipActionBarBtnView.setTag(tabName);
            clipActionBarBtnView.setOnClickListener(this);
            addView(clipActionBarBtnView, params);
            tabViewsMap.put(tabName,clipActionBarBtnView);
        }
    }

    @Override
    public void onClick(View v) {
        String tabName = (String) v.getTag();
        if (!TextUtils.isEmpty(tabName)) {
            setCurrentClipboardTab(tabName);
            if(onClipboardTabChangeListener!=null){
                onClipboardTabChangeListener.onClipboardTabChange(tabName);
            }
            if(tabName.equals(ClipboardConstants.PANEL_PIN)){
                KCAnalytics.logEvent("keyboard_clipboard_pin_tab_clicked");
            }
            HSLog.d(ClipboardActionBar.class.getSimpleName(), " to show " + tabName);
        }
    }

    void setCurrentClipboardTab(String tabName) {
        for (TextView tabView : tabViewsMap.values()) {
            if (tabView != null) {
                tabView.setSelected(false);
                tabView.setTextColor(Color.argb(128, 255, 255, 255));
                tabView.setBackgroundColor(Color.argb(0, Color.red(DOMINANT_COLOR), Color.green(DOMINANT_COLOR), Color.blue(DOMINANT_COLOR)));
            }
        }
        TextView currentTabView = tabViewsMap.get(tabName);
        if (currentTabView != null) {
            currentTabView.setTextColor(TEXT_COLOR);
            currentTabView.setBackgroundColor(Color.argb(180, Color.red(DOMINANT_COLOR), Color.green(DOMINANT_COLOR), Color.blue(DOMINANT_COLOR)));
        }
    }
    interface OnClipboardTabChangeListener {

        void onClipboardTabChange(String tabName);
    }
}
