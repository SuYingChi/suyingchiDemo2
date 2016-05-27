package com.keyboard.inputmethod.panels.gif.emojisearch;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

import com.keyboard.inputmethod.panels.gif.model.GifItem;
import com.keyboard.colorkeyboard.R;

public final class ESPageGridView extends GridView {

	public interface OnEmojiClickListener {
		void onEmojiClick(GifItem esItem);
	}

	public ESPageGridView(Context context, AttributeSet attrs) {
		super(context, attrs, R.attr.keyboardViewStyle);
	}

	public ESPageGridView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs,  R.attr.keyboardViewStyle);
	}
}