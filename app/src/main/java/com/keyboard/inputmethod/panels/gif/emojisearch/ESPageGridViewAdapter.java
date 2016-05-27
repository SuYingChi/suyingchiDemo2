package com.keyboard.inputmethod.panels.gif.emojisearch;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ihs.inputmethod.theme.HSKeyboardThemeManager;
import com.keyboard.inputmethod.panels.gif.model.GifItem;
import com.keyboard.colorkeyboard.R;

import java.util.List;

public final class ESPageGridViewAdapter extends BaseAdapter implements View.OnClickListener{

	private int mGridWidth;
	private int mGridHeight;
	private int mTextSize;
	private int mEmojiDimen;
	private List<GifItem> mData;
	private LayoutInflater mInflater;
	private ESPageGridView.OnEmojiClickListener mListener;

	public ESPageGridViewAdapter(final List<GifItem> data,
								 final ESPageGridView.OnEmojiClickListener listener,
								 final Context context,
								 final ESLayoutParams esLayoutParams) {
		mData       = data;
		mListener   = listener;
		mInflater   = LayoutInflater.from(context);
		mGridWidth  = esLayoutParams.getGridWidth();
		mGridHeight = esLayoutParams.getGridHeight();
		mEmojiDimen = Math.min(mGridHeight, mGridWidth);
		mTextSize   = (int) (mEmojiDimen * 0.7f);
	}

	@Override
	public int getCount() {
		return (mData != null) ? mData.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final EmojiViewHolder holder;
		final GifItem esItem = mData.get(position);

		if (convertView != null) {
			if (esItem.equals(convertView.getTag())) {
				return convertView;
			}
		}

		convertView = mInflater.inflate(R.layout.emoji_search_emoji_view, null);
		// container layout
		final View containerLayout = convertView.findViewById(R.id.emoji_layout);
		containerLayout.setLayoutParams(new GridView.LayoutParams(mGridWidth, mGridHeight));

		// emoji
		holder = new EmojiViewHolder();
		holder.emojiTextView = (TextView) convertView.findViewById(R.id.emoji);
		holder.emojiTextView.setText(esItem.getId());
		holder.emojiTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX , mTextSize);
		holder.emojiTextView.setTextColor(HSKeyboardThemeManager.getTextColorFromStyleOfCurrentTheme("GifEmojiSearchContentTextViewStyle"));

		// emoji text layout
		holder.emojiTextView.setLayoutParams(new RelativeLayout.LayoutParams(mEmojiDimen, mEmojiDimen));

		// click action
		holder.emojiTextView.setOnClickListener(this);
		holder.emojiTextView.setTag(esItem);

		convertView.setTag(esItem);

		return convertView;
	}

	private boolean isEmoji(final Object o){
		return o instanceof GifItem;
	}

	@Override
	public void onClick(View arg0) {
		final Object obj = arg0.getTag();
		if (obj != null) {
			if (isEmoji(obj)) {
				mListener.onEmojiClick((GifItem) obj);
			}
		}
	}

	class EmojiViewHolder {
		public TextView emojiTextView;
	}
}