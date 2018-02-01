package com.ihs.inputmethod.stickers.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.ihs.inputmethod.base.adapter.BaseListAdapter;
import com.ihs.inputmethod.stickers.model.StickerModel;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerGroup;

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by jixiang on 18/1/23.
 */

public class StickerAdapter extends BaseListAdapter<StickerModel> {
    OnStickerClickListener onStickerClickListener;

    public StickerAdapter(Activity activity, OnStickerClickListener onStickerClickListener) {
        super(activity);
        this.onStickerClickListener = onStickerClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        StickerViewHolder stickerViewHolder = new StickerViewHolder(View.inflate(parent.getContext(), R.layout.item_sticker, null));
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = parent.getResources().getDimensionPixelSize(R.dimen.theme_card_recycler_view_card_margin);
        layoutParams.setMargins(margin, margin, margin, margin);
        stickerViewHolder.itemView.setLayoutParams(layoutParams);
        return stickerViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        StickerAdapter.StickerViewHolder stickerViewHolder = (StickerAdapter.StickerViewHolder) holder;
        StickerModel stickerModel = dataList.get(position);
        StickerGroup stickerGroup = stickerModel.stickerGroup;
        stickerViewHolder.stickerRealImage.setImageBitmap(null);
        Glide.with(activity).load(stickerGroup.getStickerGroupDownloadPreviewImageUri()).into(stickerViewHolder.stickerRealImage);
        if (!stickerModel.isDownloaded) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onStickerClickListener != null) {
                        onStickerClickListener.onStickerClick(position);
                    }
                }
            });
        }
    }

    private static class StickerViewHolder extends RecyclerView.ViewHolder {
        GifImageView stickerNewImage;
        ImageView stickerAnimatedView;
        ImageView stickerRealImage;


        public StickerViewHolder(View itemView) {
            super(itemView);
            stickerRealImage = itemView.findViewById(R.id.sticker_image_real_view);
            stickerNewImage = itemView.findViewById(R.id.sticker_new_view);
            stickerAnimatedView = itemView.findViewById(R.id.sticker_animated_view);
        }
    }

    public interface OnStickerClickListener {
        void onStickerClick(int position);
    }
}
