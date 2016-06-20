package com.ihs.customtheme.panels.theme;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.customtheme.R;
import com.ihs.inputmethod.api.HSInputMethod;
import com.ihs.inputmethod.api.HSInputMethodPanel;
import com.ihs.inputmethod.api.HSInputMethodTheme;
import com.ihs.inputmethod.theme.HSKeyboardThemeManager;
import com.tonicartos.superslim.LayoutManager;

import java.util.ArrayList;
import java.util.List;

public class HSThemeSelectPanel extends HSInputMethodPanel {

    public static final String PANEL_NAME_THEME = "theme";
    private int mHeaderDisplay;
    private boolean mAreMarginsFixed;

    public HSThemeSelectPanel() {
        super(PANEL_NAME_THEME);
        HSGlobalNotificationCenter.addObserver(HSInputMethod.HS_NOTIFICATION_THEME_LIST_CHANGED, themeListChanged);
    }

    private HSThemeSelectRecycler mThemeSelectRecyclerView;
    private HSThemeSelectRecyclerAdapter mThemeSelectRecyclerAdapter;
    private List<HSThemeSelectRecyclerAdapter.ThemeSelectViewItem> mThemeSelectViewItems;

    private INotificationObserver themeListChanged = new INotificationObserver() {
        @Override
        public void onReceive(String eventName, HSBundle notification) {
            if (eventName.equals(HSInputMethod.HS_NOTIFICATION_THEME_LIST_CHANGED)) {
                if (mThemeSelectRecyclerAdapter != null) {
                    reloadThemeItems();
                    mThemeSelectRecyclerAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    @Override
    public View onCreatePanelView() {
        Context mThemeContext = new ContextThemeWrapper(HSApplication.getContext(), HSInputMethodTheme.getCurrentThemeStyleId());
        LayoutInflater inflater = LayoutInflater.from(mThemeContext);

        mHeaderDisplay = LayoutManager.LayoutParams.HEADER_INLINE;
        mAreMarginsFixed = false;

        mThemeSelectRecyclerView = (HSThemeSelectRecycler) inflater.inflate(R.layout.theme_select_recycler, null);
        mThemeSelectRecyclerView.setLayoutManager(new LayoutManager(HSApplication.getContext()));

        mThemeSelectViewItems = new ArrayList<>();
        reloadThemeItems();

        mThemeSelectRecyclerAdapter = new HSThemeSelectRecyclerAdapter(HSApplication.getContext(), mHeaderDisplay, mThemeSelectViewItems);
        mThemeSelectRecyclerAdapter.setMarginsFixed(mAreMarginsFixed);
        mThemeSelectRecyclerAdapter.setHeaderDisplay(mHeaderDisplay);
        mThemeSelectRecyclerAdapter.setOnItemClickListener(new HSThemeSelectRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }
        });
        mThemeSelectRecyclerView.setAdapter(mThemeSelectRecyclerAdapter);

        return mThemeSelectRecyclerView;
    }

    public void reloadThemeItems() {
        mThemeSelectViewItems.clear();

        if (HSKeyboardThemeManager.KEYBOARD_CUSTOM_THEMES.length > 0) {
            int themeSectionManager = 0;
            int themeHeaderIndex = 0;
            int themeIndex = 0;

            mThemeSelectViewItems.add(new HSThemeSelectRecyclerAdapter.ThemeSelectViewItem(
                    HSApplication.getContext().getResources().getString(R.string.customized_themes), true, false, 0, null, themeSectionManager, themeHeaderIndex));
            themeIndex += 1;

            // Add Theme
            mThemeSelectViewItems.add(new HSThemeSelectRecyclerAdapter.ThemeSelectViewItem(null, false, true,
                    0, null, themeSectionManager, themeHeaderIndex));
            themeIndex += 1;

            // Custom Themes
            for (int i = 0; i < HSKeyboardThemeManager.KEYBOARD_CUSTOM_THEMES.length; i++, themeIndex++) {
                mThemeSelectViewItems.add(new HSThemeSelectRecyclerAdapter.ThemeSelectViewItem(null, false, false,
                        i, HSInputMethodTheme.getThemeNameByIndex(i), themeSectionManager, themeHeaderIndex));
            }

            // BuiltIn Themes
            themeSectionManager = 1;
            themeHeaderIndex = themeIndex;
            mThemeSelectViewItems.add(new HSThemeSelectRecyclerAdapter.ThemeSelectViewItem(
                    HSApplication.getContext().getResources().getString(R.string.default_themes), true, false, 0, null, themeSectionManager, themeHeaderIndex));
            themeIndex += 1;

            for (int i = 0; i < HSKeyboardThemeManager.KEYBOARD_BUILTIN_THEMES.length; i++, themeIndex++) {
                int index = i + HSKeyboardThemeManager.KEYBOARD_CUSTOM_THEMES.length;
                mThemeSelectViewItems.add(
                        new HSThemeSelectRecyclerAdapter.ThemeSelectViewItem(null, false, false,
                                index, HSInputMethodTheme.getThemeNameByIndex(index),
                                themeSectionManager, themeHeaderIndex));
            }
        } else {
            // Add Theme
            mThemeSelectViewItems.add(new HSThemeSelectRecyclerAdapter.ThemeSelectViewItem(null, false, true,
                    0, null, 0, 0));

            // BuiltIn Themes
            for (int i = 0; i < HSKeyboardThemeManager.KEYBOARD_BUILTIN_THEMES.length; i++) {
                mThemeSelectViewItems.add(new HSThemeSelectRecyclerAdapter.ThemeSelectViewItem(null, false, false,
                        i, HSInputMethodTheme.getThemeNameByIndex(i), 0, 0));
            }
        }
    }
}
