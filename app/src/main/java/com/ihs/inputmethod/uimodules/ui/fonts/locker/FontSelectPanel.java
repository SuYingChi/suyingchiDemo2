package com.ihs.inputmethod.uimodules.ui.fonts.locker;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacterManager;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.panelcontainer.BasePanel;
import com.ihs.panelcontainer.panel.KeyboardPanel;

public class FontSelectPanel extends BasePanel {

    public FontSelectPanel() {
        mContext = HSApplication.getContext();
        HSGlobalNotificationCenter.addObserver(HSSpecialCharacterManager.HS_NOTIFICATION_SPECIAL_CHARACTERS_LOAD_FINISHED, loadDataObserver);
    }


    private Context mContext;

    private FontSelectView mFontSelectView;
    private FontSelectViewAdapter mAdapter;
    private View view;
    private INotificationObserver loadDataObserver = new INotificationObserver() {
        @Override
        public void onReceive(String eventName, HSBundle notificaiton) {
            if (eventName.equals(HSSpecialCharacterManager.HS_NOTIFICATION_SPECIAL_CHARACTERS_LOAD_FINISHED)) {
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    @Override
    public View onCreatePanelView() {
        Context mThemeContext = new ContextThemeWrapper(mContext, HSKeyboardThemeManager.getCurrentTheme().mStyleId);
        LayoutInflater inflater = LayoutInflater.from(mThemeContext);
        view = inflater.inflate(R.layout.locker_font_select_layout, null);
        mFontSelectView= (FontSelectView) view.findViewById(R.id.font_select_listview);
        view.setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
        mAdapter = new FontSelectViewAdapter(mContext, mFontSelectView, new FontSelectViewAdapter.OnFontClickListener() {
            @Override
            public void onFontClick() {
                panelActionListener.showPanel(KeyboardPanel.class);
            }
        });
        mFontSelectView.setAdapter(mAdapter);
        view.findViewById(R.id.sticker_keyboard_top_divider).setBackgroundDrawable(HSKeyboardThemeManager.getStyledDrawable(null, HSKeyboardThemeManager.IMG_EMOJI_KEYBOARD_DIVIDER));
        view.findViewById(R.id.font_ad_tell).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ad = HSConfig.getString("Application", "ShareContents", "Keyboard", "ShareTexts", "ForFonts");
                if (ad.length() > 2)
                    HSInputMethod.inputText(ad);
            }
        });
        return view;
    }


    @Override
    public void onDestroy(){
        HSGlobalNotificationCenter.removeObserver(loadDataObserver);
        super.onDestroy();
    }

}
