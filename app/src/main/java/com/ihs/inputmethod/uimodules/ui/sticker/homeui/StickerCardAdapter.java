package com.ihs.inputmethod.uimodules.ui.sticker.homeui;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerGroup;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.util.List;


/**
 * Created by guonan.lv on 17/8/10.
 */

public class StickerCardAdapter extends RecyclerView.Adapter<StickerCardViewHolder> {

    private List<StickerModel> stickerModelList;
    private int imageWidth;
    private int imageHeight;
    private OnStickerCardClickListener onStickerCardClickListener;
    public static final int MORE_STICKER_COMING = 2;
    private String FROM_FRAGMENT_TYPE;

    private DisplayImageOptions options=new DisplayImageOptions.Builder().cacheInMemory(false).cacheOnDisk(true).imageScaleType(ImageScaleType.EXACTLY).build();

    public StickerCardAdapter(List<StickerModel> data, OnStickerCardClickListener onStickerCardClickListener) {
        stickerModelList = data;
        Resources resources = HSApplication.getContext().getResources();
        imageWidth = (int) (resources.getDisplayMetrics().widthPixels / 2 - resources.getDimension(R.dimen.theme_card_recycler_view_card_margin) * 2);
        imageHeight = (int) (imageWidth / 1.6f);
        this.onStickerCardClickListener = onStickerCardClickListener;
    }

    public void setFragmentType(String type) {
        FROM_FRAGMENT_TYPE = type;
    }

    @Override
    public StickerCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new StickerCardViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sticker_card, parent, false));
    }

    @Override
    public void onBindViewHolder(StickerCardViewHolder holder, final int position) {
        if (stickerModelList == null) {
            return;
        }

        if (isFromHomeType() && getItemViewType(position) == MORE_STICKER_COMING) {
            holder.moreStickersComing.setVisibility(View.VISIBLE);
            holder.stickerCardView.setVisibility(View.GONE);
            return;
        }

        final StickerModel stickerModel = stickerModelList.get(position);
        final StickerGroup stickerGroup = stickerModel.getStickerGroup();
        holder.stickerGroupName.setText(stickerGroup.getStickerGroupName());
        final String realImageUrl = stickerGroup.getStickerGroupDownloadPreviewImageUri();
        if(realImageUrl != null) {
            ImageSize imageSize = new ImageSize(imageWidth,imageHeight);
            ImageLoader.getInstance().displayImage(realImageUrl, new ImageViewAware(holder.stickerRealImage), options, imageSize, null, null);
        }
        if(!stickerModel.getIsDownload()) {
            holder.moreMenuImage.setImageResource(R.drawable.ic_download_icon);
            holder.moreMenuImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onStickerCardClickListener.onDownloadButtonClick(stickerModel);
                }
            });
        } else {
            holder.moreMenuImage.setVisibility(View.GONE);
        }
        holder.stickerRealImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStickerCardClickListener.onCardViewClick(stickerModel);
            }
        });
        holder.stickerNewImage.setVisibility(stickerModel.getStickerTag() == null ? View.GONE : View.VISIBLE);
    }

    private boolean isFromHomeType() {
        return TextUtils.equals(FROM_FRAGMENT_TYPE, StickerHomeFragment.class.getSimpleName());
    }

    @Override
    public int getItemViewType(int position) {
        if (isFromHomeType() && position == getItemCount()-1) {
            return MORE_STICKER_COMING;
        }
        return 0;
    }

    @Override
    public int getItemCount() {
        if (isFromHomeType()) {
            return stickerModelList.size()+1;
        }
        return stickerModelList.size();
    }

    public interface OnStickerCardClickListener {
        void onCardViewClick(StickerModel stickerModel);
        void onDownloadButtonClick(StickerModel stickerModel);
    }
}

