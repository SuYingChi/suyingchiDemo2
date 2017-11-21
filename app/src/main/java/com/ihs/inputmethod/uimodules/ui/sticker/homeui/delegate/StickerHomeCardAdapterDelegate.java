package com.ihs.inputmethod.uimodules.ui.sticker.homeui.delegate;

import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.artw.lockscreen.LockerAppGuideManager;
import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerDataManager;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerGroup;
import com.ihs.inputmethod.uimodules.ui.sticker.homeui.CommonStickerAdapter;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.StickerHomeModel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.util.List;

import pl.droidsonroids.gif.GifImageView;


public final class StickerHomeCardAdapterDelegate extends AdapterDelegate<List<StickerHomeModel>> {
    public final static int TAG_DOWNLOAD = 1;
    public final static int TAG_CARD = 2;

    private CommonStickerAdapter.OnStickerItemClickListener onStickerItemClickListener;
    private int imageWidth;
    private int imageHeight;

    private DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).imageScaleType(ImageScaleType.EXACTLY).build();

    public StickerHomeCardAdapterDelegate(CommonStickerAdapter.OnStickerItemClickListener onStickerItemClickListener) {
        this.onStickerItemClickListener = onStickerItemClickListener;
        Resources resources = HSApplication.getContext().getResources();
        imageWidth = (int) (resources.getDisplayMetrics().widthPixels / 2 - resources.getDimension(R.dimen.theme_card_recycler_view_card_margin) * 2);
        imageHeight = (int) (imageWidth / 1.6f);
    }

    @Override
    protected boolean isForViewType(@NonNull List<StickerHomeModel> items, int position) {
        return items.get(position).stickerGroup != null && !items.get(position).isDownloaded;
    }

    @Override
    public int getSpanSize(List<StickerHomeModel> items, int position) {
        return 3;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new StickerCardHomeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sticker_card, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull List<StickerHomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        final StickerHomeModel stickerModel = items.get(position);
        final StickerGroup stickerGroup = stickerModel.stickerGroup;
        StickerCardHomeViewHolder stickerCardViewHolder = (StickerCardHomeViewHolder) holder;
        stickerCardViewHolder.stickerGroupName.setText(stickerGroup.getDownloadDisplayName());
        final String realImageUrl = stickerGroup.getStickerGroupDownloadPreviewImageUri();
        if (realImageUrl != null) {
            stickerCardViewHolder.stickerRealImage.setImageDrawable(null);
            ImageSize imageSize = new ImageSize(imageWidth, imageHeight);
            ImageLoader.getInstance().displayImage(realImageUrl, new ImageViewAware(stickerCardViewHolder.stickerRealImage), options, imageSize, null, null);
        } else {
            stickerCardViewHolder.stickerRealImage.setImageDrawable(null);
        }
        stickerCardViewHolder.downloadBtn.setVisibility(View.VISIBLE);
        if (LockerAppGuideManager.getInstance().shouldGuideToDownloadLocker() && stickerGroup.downloadLockerToUnlock){
            stickerCardViewHolder.downloadBtn.setImageResource(R.drawable.lock_normal);
        }else {
            stickerCardViewHolder.downloadBtn.setImageResource(R.drawable.ic_download_icon);
        }
        stickerCardViewHolder.downloadBtn.setTag(stickerModel);
        stickerCardViewHolder.downloadBtn.setTag(R.id.theme_card_view_tag_key_action, TAG_DOWNLOAD);
        stickerCardViewHolder.downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onStickerItemClickListener != null){
                    onStickerItemClickListener.onDownloadClick(stickerModel,stickerCardViewHolder.stickerRealImage.getDrawable());
                }
            }
        });

        stickerCardViewHolder.stickerRealImage.setTag(stickerModel);
        stickerCardViewHolder.stickerRealImage.setTag(R.id.theme_card_view_tag_key_action, TAG_CARD);
        stickerCardViewHolder.stickerRealImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onStickerItemClickListener != null){
                    onStickerItemClickListener.onCardClick(stickerModel,stickerCardViewHolder.stickerRealImage.getDrawable());
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
        View stickerCardView;
        ImageView downloadBtn;

        TextView stickerGroupName;
        GifImageView stickerNewImage;
        ImageView stickerAnimatedView;
        ImageView stickerRealImage;


        public StickerCardHomeViewHolder(View itemView) {
            super(itemView);

            stickerCardView = itemView.findViewById(R.id.sticker_card_view);
            stickerGroupName = (TextView) itemView.findViewById(R.id.sticker_name);
            stickerRealImage = (ImageView) itemView.findViewById(R.id.sticker_image_real_view);
            stickerNewImage = (GifImageView) itemView.findViewById(R.id.sticker_new_view);
            stickerAnimatedView = (ImageView) itemView.findViewById(R.id.sticker_animated_view);
            downloadBtn = (ImageView) itemView.findViewById(R.id.download_icon);
        }
    }
}
