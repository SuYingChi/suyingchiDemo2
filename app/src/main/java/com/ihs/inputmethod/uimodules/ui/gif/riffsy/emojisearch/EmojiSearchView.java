package com.ihs.inputmethod.uimodules.ui.gif.riffsy.emojisearch;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.HSEmojiViewAdapter;
import com.ihs.inputmethod.uimodules.ui.common.model.Emoji;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.model.GifItem;

import java.util.ArrayList;
import java.util.List;

public final class EmojiSearchView extends LinearLayout implements HSEmojiViewAdapter.OnEmojiClickListener {

	public interface OnEmojiSearchItemClickListener {
		void onEmojiSearchItemClick(GifItem esItem);
	}

	private OnEmojiSearchItemClickListener mListener;
	private HSEmojiViewAdapter adapter;
	private List<GifItem> emojiList=null;

	public EmojiSearchView(Context context) {
		this(context,null);
	}

	public EmojiSearchView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public EmojiSearchView(Context context, AttributeSet attrs, int defStyleAttr){
		super(context, attrs, defStyleAttr);
	}

	public void destroy() {
		mListener = null;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		final Resources res=getResources();
		final int defaultKeyboardHeight = HSResourceUtils.getDefaultKeyboardHeight(res);
		final int defaultKeyboardWidth  = HSResourceUtils.getDefaultKeyboardWidth(res);

		final int colNumber=res.getInteger(R.integer.config_emoji_col_count);
		final int rowNumber=res.getInteger(R.integer.config_emoji_row_count)+1;
        final int width= (int) (defaultKeyboardWidth/(colNumber+0.5));
		final int height=defaultKeyboardHeight/rowNumber;

		final RecyclerView emojiView = findViewById(R.id.emoji_viewpager);
		ViewGroup.LayoutParams lp=emojiView.getLayoutParams();
		if(lp==null){
			lp=new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,defaultKeyboardHeight);
		}else{
			lp.height=defaultKeyboardHeight;
		}
		emojiView.setLayoutParams(lp);
		emojiView.setLayoutManager(new StaggeredGridLayoutManager(rowNumber,StaggeredGridLayoutManager.HORIZONTAL));
		adapter=new HSEmojiViewAdapter(height,width,0.6f,this);
		emojiView.setAdapter(adapter);
	}

	public void prepare() {
		this.setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
		final List<Emoji> data=new ArrayList<>();
		for(final GifItem item:emojiList){
			final Emoji emoji=new Emoji(item.getId(),1,false);
			data.add(emoji);

		}
		adapter.setData(data);
	}

	public void setListener(final OnEmojiSearchItemClickListener listener) {
		mListener = listener;
	}

	public void setEmojiData(final List<GifItem> emojiList) {
		this.emojiList=emojiList;
	}

	@Override
	public void onEmojiClick(Emoji emoji) {
		if (mListener != null) {
			for(final GifItem item:emojiList){
				if(emoji.getLabel().equals(item.getId())){
					mListener.onEmojiSearchItemClick(item);
					break;
				}
			}
		}
	}
}