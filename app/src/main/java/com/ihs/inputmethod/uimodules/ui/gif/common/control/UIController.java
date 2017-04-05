package com.ihs.inputmethod.uimodules.ui.gif.common.control;

import android.os.Handler;

import com.ihs.app.framework.HSApplication;

/**
 * Created by dsapphire on 16/1/22.
 */
public final class UIController {

	private static UIController instance;

	private Handler handler;

	private UIController() {
		handler=new Handler(HSApplication.getContext().getMainLooper());
	}

	public static void init() {
		if (instance == null) {
			synchronized (UIController.class) {
				if (instance == null) {
					instance=new UIController();
				}
			}
		}
	}

	public static UIController getInstance() {
		if (instance == null) {
			init();
		}
		return instance;
	}

	public Handler getUIHandler(){
		return handler;
	}
}
