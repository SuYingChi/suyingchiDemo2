package com.ihs.inputmethod.uimodules;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.uimodules.constants.Constants;
import com.ihs.inputmethod.uimodules.constants.Notification;
import com.ihs.panelcontainer.BasePanel;

/**
 * Created by ihs on 16/6/28.
 */
public abstract class BaseControlPanelView extends LinearLayout implements View.OnClickListener  {

    public static final String NOTIFICATION_SHOW_PANEL = "notification.ControlPanelView.SHOW_PANEL";

    public static final String PARAM_PANEL_NAME = "panel_name";
    public static final String PARAM_PANEL_BUTTON = "panel_button";

    protected INotificationObserver mImeActionObserver = new INotificationObserver() {
        @Override
        public void onReceive(String eventName, HSBundle notificaiton) {
            if (eventName == null) {
                return;
            } else if (eventName.equals(Notification.SERVICE_START_INPUT_VIEW)) {
                if (notificaiton != null) {
                    final String ourPackageName = HSApplication.getContext().getPackageName();
                    if (!ourPackageName.equals(notificaiton.getString(Constants.HS_NOTIFICATION_PARAM_EDITOR_OWNER_PACKAGE_NAME, ""))) {
                        onStartInputView();
                    }
                }
            }
        }
    };


    public BaseControlPanelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void onConfigurationChanged() {
        HSGlobalNotificationCenter.removeObserver(mImeActionObserver);
    }

    public void onStartInputView() {

    }

    public  void onPanelShow(final BasePanel panel) {

    }

    @Override
    public void onClick(View view) {

    }
}
