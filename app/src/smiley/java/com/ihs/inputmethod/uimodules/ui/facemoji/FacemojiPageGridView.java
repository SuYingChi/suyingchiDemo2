package com.ihs.inputmethod.uimodules.ui.facemoji;

import android.content.Context;
import android.util.AttributeSet;

import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiSticker;
import com.ihs.inputmethod.uimodules.ui.facemoji.ui.GridViewWithHeaderAndFooter;


public final class FacemojiPageGridView extends GridViewWithHeaderAndFooter implements Recoverable {

	public interface OnFacemojiClickListener {
		void onFacemojiClicked(FacemojiSticker stickerItem);
	}

	public FacemojiPageGridView(Context context, AttributeSet attrs) {
		this(context, attrs, com.ihs.inputmethod.R.attr.keyboardViewStyle);
	}

	public FacemojiPageGridView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public void save() {
		((FacemojiPageGridAdapter)(getAdapter())).save();
	}

	@Override
	public void restore() {
		((FacemojiPageGridAdapter)(getAdapter())).restore();
	}

	@Override
	public void release()
	{
		((FacemojiPageGridAdapter)(getAdapter())).release();
		((FacemojiPageGridAdapter)(getAdapter())).notifyDataSetChanged();
	}

	@Override
	public Recoverable.State currentState() {
		throw new UnsupportedOperationException();
	}
}
