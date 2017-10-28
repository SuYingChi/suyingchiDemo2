package com.ihs.inputmethod.uimodules.ui.facemoji.faceswitcher;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FaceItem;

public final class FacePageGridView extends GridView {

	public interface OnFaceClickListener {
		void onFaceClick(FaceItem faceItem);
	}

	public FacePageGridView(Context context, AttributeSet attrs) {
		this(context, attrs, com.ihs.inputmethod.R.attr.keyboardViewStyle);
	}

	public FacePageGridView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
}