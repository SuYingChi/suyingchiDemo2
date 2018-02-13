package com.ihs.inputmethod.home.adapter;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.home.model.HomeModel;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerDataManager;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerGroup;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.List;

import pl.droidsonroids.gif.GifImageView;


public final class HomeStickerCardAdapterDelegate extends AdapterDelegate<List<HomeModel>> {
    private RequestOptions requestOptions;

    private DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).imageScaleType(ImageScaleType.EXACTLY).build();
    private OnStickerClickListener onStickerClickListener;

    public HomeStickerCardAdapterDelegate(OnStickerClickListener onStickerClickListener) {
        this.onStickerClickListener = onStickerClickListener;

        Resources resources = HSApplication.getContext().getResources();
        int imageWidth = (int) (resources.getDisplayMetrics().widthPixels / 2 - resources.getDimension(R.dimen.theme_card_recycler_view_card_margin) * 2);
        int imageHeight = (int) (imageWidth / 1.6f);
        requestOptions = new RequestOptions().override(imageWidth, imageHeight);
    }

    @Override
    protected boolean isForViewType(@NonNull List<HomeModel> items, int position) {
        return items.get(position).isSticker;
    }

    @Override
    public int getSpanSize(List<HomeModel> items, int position) {
        return 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        StickerCardHomeViewHolder stickerCardHomeViewHolder = new StickerCardHomeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_sticker_card, parent, false));
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) stickerCardHomeViewHolder.itemView.getLayoutParams();
        int width = (HSApplication.getContext().getResources().getDisplayMetrics().widthPixels - layoutParams.leftMargin * 2 - layoutParams.rightMargin * 2 - parent.getPaddingLeft() - parent.getPaddingRight()) / 2;
        int height = (int) (107f / 165 * width);
        layoutParams.width = width;
        layoutParams.height = height;
        return stickerCardHomeViewHolder;
    }

    @Override
    protected void onBindViewHolder(@NonNull List<HomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        final HomeModel homeModel = items.get(position);
        final StickerGroup stickerGroup = (StickerGroup) homeModel.item;
        StickerCardHomeViewHolder stickerCardViewHolder = (StickerCardHomeViewHolder) holder;
        final String realImageUrl = stickerGroup.getStickerGroupDownloadPreviewImageUri();
        if (realImageUrl != null) {
            stickerCardViewHolder.stickerRealImage.setImageDrawable(null);
            Glide.with(HSApplication.getContext()).asBitmap().apply(requestOptions).load(realImageUrl).into(stickerCardViewHolder.stickerRealImage);
        } else {
            stickerCardViewHolder.stickerRealImage.setImageDrawable(null);
        }

        stickerCardViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onStickerClickListener != null) {
                    onStickerClickListener.onStickerClick(homeModel, stickerCardViewHolder.stickerRealImage.getDrawable());
                }
            }
        });

        if (stickerGroup.shouldShowAnimatedMark()) {
            stickerCardViewHolder.stickerAnimatedView.setVisibility(View.VISIBLE);
            stickerCardViewHolder.stickerNewImage.setVisibility(View.GONE);
        } else {
            stickerCardViewHolder.stickerAnimatedView.setVisibility(View.GONE);
            /**
             * 判断是否有当前sticker group 是否是new
             */
            if (isNewStickerGroup(stickerGroup)) {
                stickerCardViewHolder.stickerNewImage.setVisibility(View.VISIBLE);
                Uri uri = Uri.parse("android.resource://" + HSApplication.getContext().getPackageName() + "/" + R.raw.app_theme_new_gif);
                stickerCardViewHolder.stickerNewImage.setImageURI(uri);
            } else {
                stickerCardViewHolder.stickerNewImage.setVisibility(View.GONE);
            }
        }

    }

    private boolean isNewStickerGroup(StickerGroup stickerGroup) {
        return StickerDataManager.getInstance().isNewStickerGroup(stickerGroup);
    }

    public class StickerCardHomeViewHolder extends RecyclerView.ViewHolder {
        GifImageView stickerNewImage;
        ImageView stickerAnimatedView;
        ImageView stickerRealImage;


        public StickerCardHomeViewHolder(View itemView) {
            super(itemView);
            stickerRealImage = (ImageView) itemView.findViewById(R.id.sticker_image_real_view);
            stickerNewImage = (GifImageView) itemView.findViewById(R.id.sticker_new_view);
            stickerAnimatedView = (ImageView) itemView.findViewById(R.id.sticker_animated_view);
        }
    }

    public interface OnStickerClickListener {
        void onStickerClick( HomeModel homeModel, Drawable thumbnailDrawable);
    }
}
