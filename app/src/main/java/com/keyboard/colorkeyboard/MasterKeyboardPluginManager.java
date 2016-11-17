package com.keyboard.colorkeyboard;

import android.view.View;

import com.keyboard.colorkeyboard.settings.SettingsButton;

import java.util.Stack;

/**
 * Created by chenyuanming on 12/10/2016.
 */

public class MasterKeyboardPluginManager {

    private static MasterKeyboardPluginManager instance = new MasterKeyboardPluginManager();
    private Stack<View> stack = new Stack<>();

    private MasterKeyboardPluginManager() {
    }

    public static MasterKeyboardPluginManager getInstance() {
        return instance;
    }

    public void addActionButtonToStack(View view) {
        if (!stack.contains(view)) {
            stack.add(view);
        }
        updateActionView();
    }

    public void removeActionButtonToStack(View view) {
        stack.remove(view);
        updateActionView();
    }

    protected void updateActionView() {
        FunctionBar functionBar = KeyboardPanelManager.getInstance().getFunctionBar();
        if (functionBar != null) {
            functionBar.updateActionButton();
        }
    }


    public void resetActionButtonState() {

        for (int i = stack.size() - 1; i >= 0; i--) {
            View view = stack.get(i);
            if (view instanceof SettingsButton) {
                view.setSelected(false);
            } else {
                //移出栈中其他ActionButton，比如font/theme panel中的
                stack.remove(i);
            }
        }
        updateActionView();
    }


    public Stack<View> getStack() {
        return stack;
    }
}
