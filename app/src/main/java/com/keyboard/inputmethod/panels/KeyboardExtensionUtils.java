package com.keyboard.inputmethod.panels;

import com.ihs.app.framework.HSApplication;
import com.ihs.customtheme.panels.theme.HSThemeSelectPanel;
import com.ihs.inputmethod.api.HSInputMethod;
import com.ihs.inputmethod.api.HSInputMethodPanel;
import com.keyboard.inputmethod.panels.fonts.HSFontSelectPanel;
import com.keyboard.inputmethod.panels.gif.ui.panel.GifPanel;
import com.keyboard.inputmethod.panels.settings.HSSettingsPanel;
import com.keyboard.rainbow.R;
import com.keyboard.rainbow.utils.Constants;


/**
 * Created by xu.zhang on 10/30/15.
 */
public class KeyboardExtensionUtils {


    public static void loadPanels() {
        final String[] panelNames = HSApplication.getContext().getResources().getStringArray(R.array.addtional_panel_names);
        for (String name : panelNames) {
            HSInputMethod.addPanel(createPanel(name));
        }
    }


    private static HSInputMethodPanel createPanel(String name) {
        if (name.equals(Constants.PANEL_NAME_FONTS))
            return new HSFontSelectPanel();
        else if (name.equals(Constants.PANEL_NAME_THEME))
            return new HSThemeSelectPanel();
         else if (name.equals(Constants.PANEL_NAME_SETTINGS))
            return new HSSettingsPanel();
        else if (name.equals(Constants.PANEL_NAME_GIFS))
            return new GifPanel();
        return null;
    }
}
