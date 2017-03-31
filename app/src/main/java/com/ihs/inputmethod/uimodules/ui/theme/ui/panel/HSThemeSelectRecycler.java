package com.ihs.inputmethod.uimodules.ui.theme.ui.panel;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSResourceUtils;

public final class HSThemeSelectRecycler extends RecyclerView {

	public HSThemeSelectRecycler(Context context) {
		super(context);
	}

	public HSThemeSelectRecycler(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
	}

	public HSThemeSelectRecycler(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int width = HSResourceUtils.getDefaultKeyboardWidth(HSApplication.getContext().getResources());
		final int height = HSResourceUtils.getDefaultKeyboardHeight(HSApplication.getContext().getResources());
		setMeasuredDimension(width, height);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		this.setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
	}
}
