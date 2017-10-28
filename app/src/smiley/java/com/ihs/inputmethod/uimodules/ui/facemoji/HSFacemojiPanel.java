package com.ihs.inputmethod.uimodules.ui.facemoji;

import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSFileUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.mediacontroller.MediaController;
import com.ihs.inputmethod.uimodules.ui.facemoji.faceswitcher.HSSwitchFacePanel;
import com.ihs.panelcontainer.BasePanel;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by jixiang on 17/9/19.
 */

public class HSFacemojiPanel extends BasePanel{
    private com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiPalettesView panelView;
    private INotificationObserver notificationObserver = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (HSInputMethod.HS_NOTIFICATION_HIDE_WINDOW.equals(s)){
                panelView.stopAllAnim();
            }
        }
    };

    @Override
    protected View onCreatePanelView() {
        if (panelView == null) {
            panelView = (com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiPalettesView) View.inflate(HSApplication.getContext(), R.layout.panel_facemoji, null);
            panelView.setOnItemClickListener(new FacemojiPalettesView.OnItemClickListener() {
                @Override
                public void onSwitchFaceClick() {
                    getPanelActionListener().showChildPanel(HSSwitchFacePanel.class,null);
                    getPanelActionListener().getBarView().setVisibility(View.GONE);
                }
            });
            panelView.setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
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
                return HSFileUtils.getFileName(com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiManager.getCurrentFacePicUri());
            }
        });

        HSGlobalNotificationCenter.addObserver(HSInputMethod.HS_NOTIFICATION_HIDE_WINDOW,notificationObserver);

        panelView.onPanelShow();
    }

    @Override
    protected boolean onShowPanelView(int appearMode) {
        panelView.restartAnim();
        return super.onShowPanelView(appearMode);
    }

    @Override
    protected boolean onHidePanelView(int appearMode) {
        panelView.stopAllAnim();
        return super.onHidePanelView(appearMode);
    }

    @Override
    protected void onDestroy() {
        panelView.onDestory();
        HSGlobalNotificationCenter.removeObserver(notificationObserver);
        super.onDestroy();
    }
}
