package com.ihs.inputmethod.uimodules.ui.fonts.locker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;

/**
 * Created by dsapphire on 15/12/22.
 */
public class FontView extends LinearLayout {
	public FontView(Context context) {
		this(context,null);
	}

	public FontView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public FontView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		final TypedArray keyboardViewAttr = context.obtainStyledAttributes(attrs, com.ihs.inputmethod.R.styleable.FontSelectPannel, defStyleAttr, com.ihs.inputmethod.R.style.KeyboardView);
		setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
		setDividerDrawable(new ColorDrawable(keyboardViewAttr.getColor(com.ihs.inputmethod.R.styleable.FontSelectPannel_fontTextColor, 0)));
		keyboardViewAttr.recycle();
	}
//	@Override
//	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
//		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//		final int width = HSInputMethodCommonUtils.getDefaultKeyboardWidth();
//		final int height = HSInputMethodCommonUtils.getDefaultKeyboardHeight();
//		setMeasuredDimension(width, height);
//	}
}
