package com.ihs.inputmethod.uimodules.ui.facemoji;

import android.view.View;

import com.ihs.app.framework.HSApplication;
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

public class HSFacemojiPanel extends BasePanel implements FacemojiPalettesView.AlphaKeyboardClickListener {
    private FacemojiPalettesView panelView;

    @Override
    protected View onCreatePanelView() {
        if (panelView == null) {
            panelView = (FacemojiPalettesView) View.inflate(HSApplication.getContext(), R.layout.panel_facemoji, null);
            panelView.setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
            panelView.setAlphaKeyboardClickListener(this);
            init();
        }
        return panelView;
    }


    private void init() {
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

        panelView.onPanelShow();
    }

    @Override
    public void onAlphaClick() {
        getPanelActionListener().showPanel(KeyboardPanel.class);
        getPanelActionListener().setBarVisibility(View.VISIBLE);
    }
}
