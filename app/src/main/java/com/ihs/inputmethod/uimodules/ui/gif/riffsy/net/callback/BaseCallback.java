package com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.callback;


import com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.request.BaseRequest;

import java.util.List;

/**
 * Created by dsapphire on 16/1/21.
 */
public interface BaseCallback {
	void onFail();
	void onComplete(List<?> data, BaseRequest request);
}
