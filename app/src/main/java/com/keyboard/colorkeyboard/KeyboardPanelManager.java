package com.keyboard.colorkeyboard;

import android.view.LayoutInflater;
import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.ui.keyboard.KeyboardPanel;
import com.ihs.keyboardutils.panelcontainer.KeyboardPanelSwitchContainer;

/**
 * Created by jixiang on 16/11/17.
 */

public class KeyboardPanelManager {
    private static KeyboardPanelManager instance = new KeyboardPanelManager();
    private KeyboardPanelManager(){

    }
    public static KeyboardPanelManager getInstance(){
        return instance;
    }


    private KeyboardPanelSwitchContainer keyboardPanelSwitchContainer;
    private FunctionBar functionBar;

    public KeyboardPanelSwitchContainer getKeyboardPanelSwitchContainer() {
        return keyboardPanelSwitchContainer;
    }

    public FunctionBar getFunctionBar() {
        return functionBar;
    }

    public View createKeyboardPanelSwitchContainer(View keyboardPanelView){
        if(keyboardPanelSwitchContainer == null) {
            keyboardPanelSwitchContainer = new KeyboardPanelSwitchContainer();
            keyboardPanelView.setBackgroundColor(HSApplication.getContext().getResources().getColor(R.color.com_facebook_blue));
            keyboardPanelSwitchContainer.setKeyboardPanel(KeyboardPanel.class, keyboardPanelView);
            createFunctionIfNeed();
            keyboardPanelSwitchContainer.setBarView(functionBar);
            keyboardPanelSwitchContainer.showPanel(KeyboardPanel.class);
        }
        keyboardPanelSwitchContainer.setBackgroundDrawable(HSKeyboardThemeManager.getCurrentTheme().getKeyboardBackground());
        return keyboardPanelSwitchContainer;
    }

    private void createFunctionIfNeed(){
        if(functionBar == null) {
            functionBar = (FunctionBar) LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.rainbow_funtion_bar, null);
        }
    }

}
