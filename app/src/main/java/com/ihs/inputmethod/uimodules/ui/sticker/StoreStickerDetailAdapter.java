package com.ihs.inputmethod.uimodules.ui.sticker;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.utils.RippleDrawableUtils;


/**
 * Created by yanxia on 2017/7/6.
 */

public class StoreStickerDetailAdapter extends RecyclerView.Adapter<StoreStickerDetailAdapter.StoreStickerViewHolder> {


    private StickerGroup stickerGroup;


    public StoreStickerDetailAdapter(StickerGroup stickerGroup) {
        this.stickerGroup = stickerGroup;
    }

    @Override
    public StoreStickerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new StoreStickerViewHolder(View.inflate(HSApplication.getContext(), R.layout.sticker_store_detail_item, null));
    }

    @Override
    public void onBindViewHolder(StoreStickerViewHolder holder, int position) {
        final ImageView stickerImageView = holder.imageView;
        String stickerGroupName = stickerGroup.getStickerGroupName();
        String stickerImageSerialNumber;
        if (position < 10) {
            stickerImageSerialNumber = "-0" + position; // -00, -01, -02...
        } else {
            stickerImageSerialNumber = "-" + position;
        }
        @SuppressWarnings("StringBufferReplaceableByString") StringBuilder stringBuilder = new StringBuilder(StickerUtils.getStickerDownloadBaseUrl()).append(stickerGroupName).append("/").append(stickerGroupName).append("/").append(stickerGroupName).append(stickerImageSerialNumber).append(stickerGroup.getPicFormat());
        String stickerImageUri = stringBuilder.toString();
        Glide.with(stickerImageView).load(stickerImageUri).apply(new RequestOptions()
                .placeholder(R.drawable.sticker_store_image_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)).into(stickerImageView);
        holder.itemView.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(Color.TRANSPARENT, Color.parseColor("#dfdfdf"), DisplayUtils.dip2px(6)));
    }

    @Override
    public int getItemCount() {
        return stickerGroup.getShowCount();
    }

    class StoreStickerViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        StoreStickerViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.sticker_store_detail_iv);
        }
    }
}
