package com.ihs.inputmethod.uimodules.ui.sticker;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

import com.ihs.inputmethod.R;
import com.ihs.inputmethod.uimodules.ui.sticker.bean.BaseStickerItem;


public final class StickerPageGridView extends GridView {

	public interface OnStickerClickListener {
		void onStickerClicked(BaseStickerItem stickerItem);
	}

	public StickerPageGridView(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.keyboardViewStyle);
	}

	public StickerPageGridView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

//	@Override
//	public void save() {
//		((StickerPageGridAdapter)(getAdapter())).save();
//	}
//
//	@Override
//	public void restore() {
//		((StickerPageGridAdapter)(getAdapter())).restore();
//		((StickerPageGridAdapter)(getAdapter())).notifyDataSetChanged();
//	}
//
//	@Override
//	public void release()
//	{
//		((StickerPageGridAdapter)(getAdapter())).release();
//		((StickerPageGridAdapter)(getAdapter())).notifyDataSetChanged();
//		System.gc();
//	}
//
//	@Override
//	public Recoverable.State currentState() {
//		throw new UnsupportedOperationException();
//	}
}
