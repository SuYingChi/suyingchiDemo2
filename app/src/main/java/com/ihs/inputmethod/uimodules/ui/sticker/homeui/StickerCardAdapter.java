package com.ihs.inputmethod.uimodules.ui.sticker.homeui;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

public class StickerCardAdapter extends RecyclerView.Adapter<StickerCardAdapter.StickerCardViewHolder> {

    private List<StickerModel> stickerModelList;
    private int imageWidth;
    private int imageHeight;
    private OnStickerCardClickListener onStickerCardClickListener;
    private String FROM_FRAGMENT_TYPE;
    public enum ITEM_TYPE {
        ITEM_TYPE_HOME,
        ITEM_TYPE_MY,
        ITEM_TYPE_MORE
    }

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
        if (viewType == ITEM_TYPE.ITEM_TYPE_MY.ordinal()) {
            return new MyStickerCardViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sticker_card, parent, false));
        } else {
            return new StickerCardHomeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sticker_card, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final StickerCardViewHolder holder, final int position) {
        if (stickerModelList == null) {
            return;
        }

        if (getItemViewType(position) == ITEM_TYPE.ITEM_TYPE_MORE.ordinal()) {

            Log.d("Kong", "ITEM_TYPE.ITEM_TYPE_MORE.ordinal(): " + ITEM_TYPE.ITEM_TYPE_MORE.ordinal());

            ((StickerCardHomeViewHolder) holder).moreStickersComing.setVisibility(View.VISIBLE);
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
        if (getItemViewType(position) == ITEM_TYPE.ITEM_TYPE_HOME.ordinal()) {
           ((StickerCardHomeViewHolder) holder).moreMenuImage.setVisibility(View.VISIBLE);
            ((StickerCardHomeViewHolder) holder).moreMenuImage.setImageResource(R.drawable.ic_download_icon);
            ((StickerCardHomeViewHolder) holder).moreMenuImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onStickerCardClickListener.onDownloadButtonClick(stickerModel, holder.stickerRealImage.getDrawable());
                }
            });
        } else {

        }
        holder.stickerRealImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStickerCardClickListener.onCardViewClick(stickerModel, holder.stickerRealImage.getDrawable());
            }
        });

        holder.stickerNewImage.setVisibility(TextUtils.equals(stickerModel.getStickerTag(), "YES") ? View.VISIBLE : View.GONE);
    }

    private boolean isFromHomeType() {
        return TextUtils.equals(FROM_FRAGMENT_TYPE, StickerHomeFragment.class.getSimpleName());
    }

    @Override
    public int getItemViewType(int position) {
        if (isFromHomeType() && position == getItemCount()-1) {
            return ITEM_TYPE.ITEM_TYPE_MORE.ordinal();
        }
        return isFromHomeType() ? ITEM_TYPE.ITEM_TYPE_HOME.ordinal() : ITEM_TYPE.ITEM_TYPE_MY.ordinal();
    }

    @Override
    public int getItemCount() {
        if (isFromHomeType()) {
            return stickerModelList.size()+1;
        }
        return stickerModelList.size();
    }

    public interface OnStickerCardClickListener {
        void onCardViewClick(StickerModel stickerModel, Drawable drawable);
        void onDownloadButtonClick(StickerModel stickerModel, Drawable drawable);
    }

    public class StickerCardViewHolder extends RecyclerView.ViewHolder {
        View stickerCardView;

        TextView stickerGroupName;
        ImageView stickerNewImage;
        ImageView stickerRealImage;


        public StickerCardViewHolder(View itemView) {
            super(itemView);

            stickerCardView = itemView.findViewById(R.id.sticker_card_view) ;
            stickerGroupName = (TextView) itemView.findViewById(R.id.sticker_name);
            stickerRealImage = (ImageView) itemView.findViewById(R.id.sticker_image_real_view);
            stickerNewImage = (ImageView) itemView.findViewById(R.id.sticker_new_view);
        }
    }

    private class StickerCardHomeViewHolder extends StickerCardViewHolder {
        TextView moreStickersComing;
        ImageView moreMenuImage;

        public StickerCardHomeViewHolder(View view) {
            super(view);
            moreMenuImage = (ImageView) itemView.findViewById(R.id.more_menu_image);
            moreStickersComing = (TextView) itemView.findViewById(R.id.more_sticker_coming);
        }
    }
    private class MyStickerCardViewHolder extends StickerCardViewHolder {
        public MyStickerCardViewHolder(View view) {
            super(view);
        }
    }

}

