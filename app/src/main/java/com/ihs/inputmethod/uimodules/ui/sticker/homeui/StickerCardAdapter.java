package com.ihs.inputmethod.uimodules.ui.sticker.homeui;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
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

    private DisplayImageOptions options=new DisplayImageOptions.Builder().cacheInMemory(false).cacheOnDisk(true).imageScaleType(ImageScaleType.EXACTLY).build();

    public StickerCardAdapter(List<StickerModel> data) {
        stickerModelList = data;
        Resources resources = HSApplication.getContext().getResources();
        imageWidth = (int) (resources.getDisplayMetrics().widthPixels / 2 - resources.getDimension(R.dimen.theme_card_recycler_view_card_margin) * 2);
        imageHeight = (int) (imageWidth / 1.6f);
    }

    @Override
    public StickerCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new StickerCardViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sticker_card, parent, false));
    }

    @Override
    public void onBindViewHolder(StickerCardViewHolder holder, int position) {
        if (stickerModelList == null) {
            return;
        }

        StickerModel stickerModel = stickerModelList.get(position);
        StickerGroup stickerGroup = stickerModel.getStickerGroup();
        holder.stickerGroupName.setText(stickerGroup.getStickerGroupName());
        final String realImageUrl = stickerGroup.getStickerGroupDownloadPreviewImageUri();
        if(realImageUrl != null) {
            ImageSize imageSize = new ImageSize(imageWidth,imageHeight);
            ImageLoader.getInstance().displayImage(realImageUrl, new ImageViewAware(holder.stickerRealImage), options,imageSize,null,null);
        }
        if(!stickerModel.getIsDownload()) {
            holder.moreMenuImage.setImageResource(R.drawable.ic_download_icon);
        } else {
            holder.moreMenuImage.setVisibility(View.GONE);
        }
        holder.stickerNewImage.setVisibility(stickerModel.getStickerTag() == null ? View.GONE : View.VISIBLE);

    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return stickerModelList.size();
    }
}

