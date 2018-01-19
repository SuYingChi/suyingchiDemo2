package com.ihs.inputmethod.home.adapter;

import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.home.HomeModel.HomeModel;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerDataManager;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerGroup;
import com.ihs.inputmethod.uimodules.utils.DisplayUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.util.List;

import pl.droidsonroids.gif.GifImageView;


public final class HomeStickerCardAdapterDelegate extends AdapterDelegate<List<HomeModel>> {
    private int margin = HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.home_activity_horizontal_margin);
    private int imageWidth;
    private int imageHeight;

    private DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).imageScaleType(ImageScaleType.EXACTLY).build();

    public HomeStickerCardAdapterDelegate() {
        Resources resources = HSApplication.getContext().getResources();
        imageWidth = (int) (resources.getDisplayMetrics().widthPixels / 2 - resources.getDimension(R.dimen.theme_card_recycler_view_card_margin) * 2);
        imageHeight = (int) (imageWidth / 1.6f);
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
        int width = (parent.getMeasuredWidth() - parent.getPaddingLeft() - parent.getPaddingRight() - margin * 3) / 2;
        int height = (int) (107f / 165 * width);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(width, height);
        layoutParams.topMargin = DisplayUtils.dip2px(HSApplication.getContext(), 8);
        stickerCardHomeViewHolder.itemView.setLayoutParams(layoutParams);
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
            ImageSize imageSize = new ImageSize(imageWidth, imageHeight);
            ImageLoader.getInstance().displayImage(realImageUrl, new ImageViewAware(stickerCardViewHolder.stickerRealImage), options, imageSize, null, null);
        } else {
            stickerCardViewHolder.stickerRealImage.setImageDrawable(null);
        }

        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        if (position % 2 == 0) {
            layoutParams.leftMargin = margin;
            layoutParams.rightMargin = margin / 2;
        } else {
            layoutParams.leftMargin = margin / 2;
            layoutParams.rightMargin = margin;
        }

        stickerCardViewHolder.stickerRealImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (onStickerItemClickListener != null) {
//                    onStickerItemClickListener.onCardClick(homeModel, stickerCardViewHolder.stickerRealImage.getDrawable());
//                }
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
        View stickerCardView;
        GifImageView stickerNewImage;
        ImageView stickerAnimatedView;
        ImageView stickerRealImage;


        public StickerCardHomeViewHolder(View itemView) {
            super(itemView);
            stickerCardView = itemView.findViewById(R.id.sticker_card_view);
            stickerRealImage = (ImageView) itemView.findViewById(R.id.sticker_image_real_view);
            stickerNewImage = (GifImageView) itemView.findViewById(R.id.sticker_new_view);
            stickerAnimatedView = (ImageView) itemView.findViewById(R.id.sticker_animated_view);
        }
    }
}
