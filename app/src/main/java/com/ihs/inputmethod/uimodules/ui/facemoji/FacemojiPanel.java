package com.ihs.inputmethod.uimodules.ui.facemoji;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.utils.HSDrawableUtils;
import com.ihs.inputmethod.api.utils.HSFileUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.constants.Notification;
import com.ihs.inputmethod.uimodules.mediacontroller.MediaController;
import com.ihs.inputmethod.uimodules.ui.gif.common.control.UIController;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class FacemojiPanel extends LinearLayout {
	private StateListDrawable mTabbarBtnStateListDrawable;
	private FacemojiPalettesView view;
	private ImageButton tabbarBtn;

	public FacemojiPanel(Context context) {
		this(context,null);
	}

	public FacemojiPanel(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);

		init();
	}

	private void init() {

		LayoutInflater inflater = (LayoutInflater) HSApplication.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = (com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiPalettesView) inflater.inflate(R.layout.facemoji_palettes_view, null);

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

		view.onPanelShow();

	}
	private INotificationObserver mImeActionObserver = new INotificationObserver() {
		@Override
		public void onReceive(String eventName, HSBundle notificaiton) {
			if (eventName == null) {
				return;
			}

			if (eventName.equals(Notification.FACE_CHANGED)) {
				if (tabbarBtn != null) {
					mTabbarBtnStateListDrawable = HSDrawableUtils.getDimmedForegroundDrawable(FacemojiManager.getCurrentFaceIcon(Color.WHITE));
					tabbarBtn.setBackgroundDrawable(mTabbarBtnStateListDrawable);
				}
				return;
			}

			if (eventName.equals(Notification.SHOW_FACE_LIST)) {
				if(view!=null) {
					view.setVisibility(View.INVISIBLE);
				}
				return;
			}
			if (eventName.equals(Notification.HIDE_FACE_LIST)) {
				if(view!=null) {
					view.setVisibility(View.VISIBLE);
				}
				return;
			}
		}
	};


    @Override
    public void onConfigurationChanged(Configuration conf) {
        super.onConfigurationChanged(conf);

        FacemojiManager.hideFaceSwitchView();
        FacemojiManager.initFaceSwitchView();
    }


}
