package com.ihs.inputmethod.uimodules.ui.facemoji.faceswitcher;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.facemoji.ui.FaceListActivity;
import com.ihs.panelcontainer.BasePanel;

/**
 * Created by jixiang on 17/9/19.
 */

public class HSSwitchFacePanel extends BasePanel{
    private FacePalettesView panelView;

    @Override
    protected View onCreatePanelView() {
        if (panelView == null) {
            panelView = (FacePalettesView) LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.face_switcher_layout, null);
            panelView.setOnFaceSwitchListener(new FacePageGridViewAdapter.OnFaceSwitchListener() {
                @Override
                public void onFaceSwitch() {
                    finish();
                }
            });
            panelView.setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
            init();
        }
        return panelView;
    }

    private void finish() {
        getPanelActionListener().backToParentPanel(false);
        getPanelActionListener().getBarView().setVisibility(View.VISIBLE);
    }

    private void init() {
        final View closeButton = panelView.findViewById(R.id.face_switch_close_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final View editButton = panelView.findViewById(R.id.face_switch_edit_btn);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HSInputMethod.hideWindow();
                Intent i = new Intent(HSApplication.getContext(), FaceListActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra(FaceListActivity.TOGGLE_MANAGE_FACE_MODE, true);
                HSApplication.getContext().startActivity(i);

            }
        });
    }

}
