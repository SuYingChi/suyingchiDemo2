package com.keyboard.inputmethod.panels.theme;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.HSInputMethodPanel;
import com.ihs.inputmethod.api.HSInputMethodTheme;
import com.keyboard.rainbow.R;

public class HSThemeSelectPanel extends HSInputMethodPanel {

    public static final String PANEL_NAME_THEME = "theme";

    public HSThemeSelectPanel() {
        super(PANEL_NAME_THEME);
    }


    private HSThemeSelectView mThemeSelectView;
    private HSThemeSelectViewAdapter mAdapter;


    @Override
    public View onCreatePanelView() {
        Context mThemeContext = new ContextThemeWrapper(HSApplication.getContext(), HSInputMethodTheme.getCurrentThemeStyleId());
        LayoutInflater inflater = LayoutInflater.from(mThemeContext);
        mThemeSelectView = (HSThemeSelectView) inflater.inflate(R.layout.theme_select_layout, null);
        mAdapter = new HSThemeSelectViewAdapter(HSApplication.getContext(), mThemeSelectView);
        mThemeSelectView.setAdapter(mAdapter);
        return mThemeSelectView;
    }
}
