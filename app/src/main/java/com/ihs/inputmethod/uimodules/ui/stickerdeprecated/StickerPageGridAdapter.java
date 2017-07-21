package com.ihs.inputmethod.uimodules.ui.stickerdeprecated;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.stickerdeprecated.bean.BaseStickerItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dsapphire on 15/11/27.
 */
public class StickerPageGridAdapter extends BaseAdapter implements View.OnClickListener{

	private StickerPageGridView.OnStickerClickListener mListener;
	private List<BaseStickerItem> mData;
	private int mStickerShowingWidth;
	private int mStickerShowingHeight;
	private LayoutInflater mInflater;

	public StickerPageGridAdapter(final List<BaseStickerItem> data,
	                              StickerPageGridView.OnStickerClickListener listener,
	                              final Context context,
	                              final int keyWidth,
	                              final int keyHeight) {
		this.mData = data;
		this.mListener = listener;
		this.mInflater = LayoutInflater.from(context);
		this.mStickerShowingWidth = keyWidth;
		this.mStickerShowingHeight = keyHeight;
	}

	@Override
	public int getCount() {
		if (mData == null) return 0;
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		if(mData!=null){
			if(mData.size()>position)
				return mData.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final StickerViewHolder holder;
		final BaseStickerItem data = mData.get(position);
        final Uri uri = Uri.parse(data.url);

		if(convertView != null){
            holder = (StickerViewHolder)convertView.getTag();
            holder.view.setImageURI(uri);
		} else {
            convertView = mInflater.inflate(R.layout.joy_sticker_view, null);
            // container layout
            final View containerLayout = convertView.findViewById(R.id.sticker_layout);
            containerLayout.setLayoutParams(new StickerPageGridView.LayoutParams(mStickerShowingWidth, mStickerShowingHeight));

            holder = new StickerViewHolder();
            holder.view = (ImageView) convertView.findViewById(R.id.view);
            holder.view.setLayoutParams(new RelativeLayout.LayoutParams((int)(mStickerShowingWidth * 0.9f), (int)(mStickerShowingHeight * 0.9f)));
            holder.view.setTag(data);
            holder.view.setOnClickListener(this);
            convertView.setTag(holder);

			holder.view.setImageURI(uri);

        }




		return convertView;
	}

	@Override
	public void onClick(View arg0) {
		final Object obj = arg0.getTag();
		if (obj != null) {
			if (isSticker(obj)) {
				mListener.onStickerClicked((BaseStickerItem) obj);
			}
		}
	}
	public void setData(List<BaseStickerItem> data){
		mData=data;
	}
	private boolean isSticker(Object o){
		return o instanceof BaseStickerItem;
	}

	class StickerViewHolder{
		public ImageView view;
	}

//
//	@Override
//	public void save() {
//		mDataSnapshot.clear();
//		mDataSnapshot.addAll(mData);
//	}
//
//	@Override
//	public void release() {
//		mData.clear();
//	}
//
//	@Override
//	public void restore() {
//		mData.addAll(mDataSnapshot);
//		mDataSnapshot.clear();
//	}
//
//    @Override
//    public Recoverable.State currentState() {
//        throw new UnsupportedOperationException();
//    }

	private List<BaseStickerItem> mDataSnapshot = new ArrayList<>() ;
}
