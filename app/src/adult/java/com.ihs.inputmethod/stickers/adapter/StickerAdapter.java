package com.ihs.inputmethod.stickers.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.ihs.inputmethod.common.adapter.CommonAdapter;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerGroup;

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by jixiang on 18/1/23.
 */

public class StickerAdapter extends CommonAdapter<StickerGroup> {
    public StickerAdapter(Activity activity) {
        super(activity);
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
        StickerGroup stickerGroup = dataList.get(position);
        stickerViewHolder.stickerRealImage.setImageBitmap(null);
        Glide.with(activity).load(stickerGroup.getStickerGroupDownloadPreviewImageUri()).into(stickerViewHolder.stickerRealImage);
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
}
