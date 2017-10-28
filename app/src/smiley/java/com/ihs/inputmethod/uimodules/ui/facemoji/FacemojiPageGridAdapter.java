package com.ihs.inputmethod.uimodules.ui.facemoji;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiSticker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dsapphire on 15/11/27.
 */
public class FacemojiPageGridAdapter extends BaseAdapter implements FacemojiView.OnFacemojiEventListener,
        Recoverable {

    private FacemojiPageGridView.OnFacemojiClickListener mListener;
    private List<FacemojiSticker> mData;
    private int stickerWidth;
    private int stickerHeight;
    private LayoutInflater mInflater;

    public FacemojiPageGridAdapter(final List<FacemojiSticker> data,
                                   FacemojiPageGridView.OnFacemojiClickListener listener,
                                   final Context context,
                                   final int stickerWidth, int stickerHeight) {
        this.mData = data;
        this.mListener = listener;
        this.mInflater = LayoutInflater.from(context);
        this.stickerWidth = stickerWidth;
        this.stickerHeight = stickerHeight;
    }

    @Override
    public int getCount() {
        if (mData == null) return 0;
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        if (mData != null) {
            if (mData.size() > position)
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
        StickerViewHolder holder;
        FacemojiSticker sticker = (FacemojiSticker) getItem(position);
        if (convertView != null) {
            holder = (StickerViewHolder) convertView.getTag();
            if (!sticker.equals(holder.facemojiView.getSticker())) {
                holder.facemojiView.setSticker(sticker);
                holder.facemojiView.setTag(sticker);
                ((FacemojiView) convertView).setOnFacemojiEventListener(this);
                convertView.setTag(holder);
            }
            return convertView;
        }
        convertView = mInflater.inflate(R.layout.facemoji_custom_view, null);
        final View containerLayout = convertView.findViewById(R.id.facemoji_layout);
        containerLayout.setLayoutParams(new GridView.LayoutParams(stickerWidth, stickerHeight));
        final FacemojiAnimationView facemojiView = (FacemojiAnimationView) containerLayout.findViewById(R.id.sticker_player_view);
        facemojiView.setSticker(sticker);
        facemojiView.setTag(sticker);
        ((FacemojiView) convertView).setOnFacemojiEventListener(this);
        holder = new StickerViewHolder();
        holder.facemojiView = facemojiView;
        convertView.setTag(holder);

        return convertView;
    }

    @Override
    public void onPressFacemoji() {
    }

    @Override
    public void onReleaseFacemoji(final FacemojiSticker facemojiSticker) {
        if (facemojiSticker != null) {
            mListener.onFacemojiClicked(facemojiSticker);
        }
    }

    public void setData(List<FacemojiSticker> data) {
        mData = data;
    }

    class StickerViewHolder {
        public FacemojiAnimationView facemojiView;
    }

    @Override
    public void save() {
        mDataSnapshot.clear();
        mDataSnapshot.addAll(mData);
    }

    @Override
    public void release() {
        mData.clear();
    }

    @Override
    public void restore() {
        mData.addAll(mDataSnapshot);
        mDataSnapshot.clear();
    }

    @Override
    public Recoverable.State currentState() {
        throw new UnsupportedOperationException();
    }

    private List<FacemojiSticker> mDataSnapshot = new ArrayList<>();
}
