package com.ihs.inputmethod.uimodules.ui.facemoji;

import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSFileUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.mediacontroller.MediaController;
import com.ihs.inputmethod.uimodules.ui.gif.common.control.UIController;
import com.ihs.panelcontainer.BasePanel;
import com.ihs.panelcontainer.panel.KeyboardPanel;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by jixiang on 17/9/19.
 */

public class HSFacemojiPanel extends BasePanel implements View.OnClickListener {
    private View panelView;
    private FacemojiPalettesView facemojiPalettesView;

    @Override
    protected View onCreatePanelView() {
        if (panelView == null) {
            panelView = View.inflate(HSApplication.getContext(), R.layout.panel_facemoji, null);
            panelView.setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
            initView();
        }
        return panelView;
    }


    private void initView() {
        facemojiPalettesView = (FacemojiPalettesView) panelView.findViewById(R.id.facemoji_palettes_view);

        if (!ImageLoader.getInstance().isInited()) {
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(HSApplication.getContext()).build();
            ImageLoader.getInstance().init(config);
        }

        MediaController.setFaceNameProvider(new MediaController.FaceNameProvider() {
            @Override
            public String faceName() {
                return HSFileUtils.getFileName(FacemojiManager.getCurrentFacePicUri());
            }
        });
        MediaController.setHandler(UIController.getInstance().getUIHandler());

        facemojiPalettesView.onPanelShow();

        panelView.findViewById(R.id.recent).setOnClickListener(this);
        panelView.findViewById(R.id.btn_1).setOnClickListener(this);
        panelView.findViewById(R.id.btn_switch_face).setOnClickListener(this);

        TextView alphabetLeft = (TextView) panelView.findViewById(R.id.keyboard_alphabet_left);
        alphabetLeft.setTextColor(HSKeyboardThemeManager.getCurrentTheme().getFuncKeyTextColor());
        alphabetLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX, HSKeyboardThemeManager.getCurrentTheme().getFuncKeyLabelSize());
        alphabetLeft.setText(HSInputMethod.getSwitchToAlphaKeyLabel());
        alphabetLeft.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.recent:
                showToast("recent click");
                break;
            case R.id.btn_1:
                showToast("btn1 click");
                break;
            case R.id.btn_switch_face:
                FacemojiManager.showFaceSwitchView();
                break;
            case R.id.keyboard_alphabet_left:
                getPanelActionListener().showPanel(KeyboardPanel.class);
                getPanelActionListener().setBarVisibility(View.VISIBLE);
                break;
        }
    }

    private void showToast(String toast){
        Toast.makeText(HSApplication.getContext(),toast,Toast.LENGTH_SHORT).show();
    }
}
