package com.keyboard.inputmethod.panels.gif.net.callback;


import com.keyboard.inputmethod.panels.gif.net.request.BaseRequest;

import java.util.List;

/**
 * Created by dsapphire on 16/1/21.
 */
public interface BaseCallback {
	void onFail();
	void onComplete(List<?> data, BaseRequest request);
}
