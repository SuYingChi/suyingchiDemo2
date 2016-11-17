package com.keyboard.colorkeyboard;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.util.ArrayMap;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.api.utils.HSViewUtils;
import com.ihs.inputmethod.framework.HSKeyboardPanelTab;
import com.ihs.inputmethod.uimodules.BaseControlPanelView;
import com.ihs.inputmethod.uimodules.KeyboardPluginManager;
import com.ihs.inputmethod.uimodules.panel.HSKeyboardPanel;
import com.ihs.keyboardutils.panelcontainer.KeyboardPanelSwitchContainer;
import com.keyboard.colorkeyboard.settings.SettingsButton;


public final class FunctionBar extends BaseControlPanelView {
    private static KeyboardPanelSwitchContainer keyboardPanelSwitchContainer;
    private LinearLayout functionLayout;

    private ArrayMap<String, HSKeyboardPanel> mPanelMap;// 键盘内容

    public FunctionBar(Context context) {
        this(context, null);
    }

    public FunctionBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FunctionBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        functionLayout = (LinearLayout) findViewById(R.id.function_layout);
        initFunction();
    }


    private void initFunction() {
        mPanelMap = new ArrayMap<>();

        final String[] panelNames = HSApplication.getContext().getResources().getStringArray(com.ihs.inputmethod.R.array.addtional_panel_names);
        for (String name : panelNames) {
            HSKeyboardPanel panel = KeyboardPluginManager.getInstance().getPanel(name);
            if (panel != null) {
                addPanel(panel);
            }
        }

        MasterKeyboardPluginManager.getInstance().getStack().add(new SettingsButton(getContext()));
        updateActionButton();
    }

    public void updateActionButton() {
        View actionView = MasterKeyboardPluginManager.getInstance().getStack().peek();
        if (functionLayout.getChildCount() != 0) {
            actionView.setSelected(false);//reset state
            functionLayout.removeAllViews();
        }
        HSViewUtils.removeFromParent(actionView);

        // Button layout params
        final LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        llp.gravity = Gravity.CENTER;
//        llp.leftMargin = YamlUtils.dp2px(5);
//        llp.rightMargin = YamlUtils.dp2px(5);
        llp.height = (int) HSApplication.getContext().getResources().getDimension(R.dimen.config_suggestions_strip_height);
        int padding = HSDisplayUtils.dip2px(10);
        actionView.setPadding(padding, 0, padding, 0);


        // Listener and layout
        actionView.setLayoutParams(llp);
        functionLayout.addView(actionView);

    }

    public void addPanel(final HSKeyboardPanel panel) {
        mPanelMap.put(panel.getPanelName(), panel);
//        addPanelTabbarBtn(panel);
    }

    public void addPanelTabbarBtn(final HSKeyboardPanel panel) {
//        // add normal button for panel
//        panel.initTab();
//        addButton(panel.getPanelName(), panel.getPanelTab());
    }


    public void addButton(final String panelName, final HSKeyboardPanelTab view) {
        if (view == null) {
            return;
        }

    }


    private boolean isSettingsPanel(String panelName) {
        return "settings".equals(panelName);
    }


    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final Resources res = getContext().getResources();
        final int width = HSResourceUtils.getDefaultKeyboardWidth(res);
        final int height = (int) res.getDimension(R.dimen.config_suggestions_strip_height);
        setMeasuredDimension(width, height);
    }

//
//    protected void showPanelByButton(final String panelName, final View button) {
//        // Get panel will be showing
//        final HSKeyboardPanel panel = getPanel(panelName);
//
//        // Prepare panel before showing
//        preparePanel(panel, button);
//
//        // Show panel by lib api
////        HSUIInputMethod.showPanel(panelName);
//        KeyboardPluginManager.getInstance().showPanel(panelName);
//    }
//
//    private HSKeyboardPanel getPanel(final String panelName) {
//        return mPanelMap.get(panelName);
//    }
//
//    private void preparePanel(final HSKeyboardPanel panel, final View button) {
//    }
//


//
//    @Override
//    public void onClick(View view) {
//        {
//            if (view instanceof ImageButton) {
//                for (View v : mAddedTabButtonMap.keySet()) {
//                    if (((LinearLayout) v).getChildAt(0) == view) {
//                        view = v;
//                        break;
//                    }
//                }
//            }
//
//            String panelName = mAddedTabButtonMap.get(view);
//            if (panelName != null) {
////                new ADECustomBoostAlert(HSApplication.getContext()).show();
//                if (isSettingsPanel(panelName)) {
//                    HSKeyboardPanel panel = getPanel(panelName);
//                    if (panel.getPanelView() == null) {
//                        //first time
//                        panel.show();
//                    } else {
//                        //因为panel show方法里面会改tabbar state,所以写在这里
//                        boolean newState = !panel.isSelected();
////                        if(newState){
////                            KeyboardPluginManager.getInstance().showPanel(panelName);
////                        }
//                        panel.setTabbarBtnState(newState);
//                    }
//                } else {
//                    showPanelByButton(panelName, view);
//                }
//                HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(HSGoogleAnalyticsConstants.GA_PARAM_ACTION_KEYBOARD_TAB_CHOSED, panelName);
//            }
//        }
//    }

}