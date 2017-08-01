package com.ihs.inputmethod.uimodules.softgame;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.iap.RemoveAdsManager;
import com.ihs.keyboardutils.nativeads.NativeAdView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.util.List;

/**
 * Created by yanxia on 2017/7/27.
 */

public class SoftGameItemAdapter extends RecyclerView.Adapter<ViewHolder> {

    public interface OnSoftGameItemClickListener {
        public void OnSoftGameItemClick(SoftGameDisplayItem softGameDisplayItem);
    }

    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .showImageOnFail(null)
            .imageScaleType(ImageScaleType.EXACTLY)
            .cacheOnDisk(true).build();
    private List<SoftGameDisplayItem> softGameDisplayItemList;
    private OnSoftGameItemClickListener softGameItemClickListener;
    private NativeAdView nativeAdView;
    private static final int TYPE_GAME = 0;
    private static final int TYPE_AD = 1;

    public SoftGameItemAdapter(List<SoftGameDisplayItem> softGameDisplayItemList, OnSoftGameItemClickListener softGameItemClickListener, NativeAdView nativeAdView) {
        this.softGameDisplayItemList = softGameDisplayItemList;
        this.softGameItemClickListener = softGameItemClickListener;
        this.nativeAdView = nativeAdView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder viewHolder = null;
        if (viewType == TYPE_AD) {
            viewHolder = new ViewHolder(nativeAdView) {
            };
        } else if (viewType == TYPE_GAME) {
            viewHolder = new SoftGameItemViewHolder(LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.soft_game_item_view, parent, false));
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        int itemViewType = getItemViewType(position);
        if (itemViewType == TYPE_AD) {
            HSLog.d("current item is Ad.");
        } else {
            final SoftGameItemViewHolder softGameItemViewHolder = (SoftGameItemViewHolder) holder;
            if (softGameDisplayItemList.isEmpty()) {
                return;
            }
            if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
                position--;
            }
            final SoftGameDisplayItem softGameDisplayItem = softGameDisplayItemList.get(position);
            ImageLoader.getInstance().displayImage(softGameDisplayItem.getThumbBig(), new ImageViewAware(softGameItemViewHolder.softGameThumbnail), displayImageOptions);
            softGameItemViewHolder.softGameTitle.setText(softGameDisplayItem.getTitle());
            softGameItemViewHolder.softGameType.setText(softGameDisplayItem.getTypeText());
            softGameItemViewHolder.softGamePlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (softGameItemClickListener != null) {
                        softGameItemClickListener.OnSoftGameItemClick(softGameDisplayItem);
                    }
                }
            });
        }
    }

    public void refreshData(List<SoftGameDisplayItem> softGameDisplayItemList) {
        this.softGameDisplayItemList = softGameDisplayItemList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            return softGameDisplayItemList.size() + 1;
        } else {
            return softGameDisplayItemList.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            if (position == 0) {
                return TYPE_AD;
            } else {
                return TYPE_GAME;
            }
        } else {
            return TYPE_GAME;
        }
    }

    class SoftGameItemViewHolder extends ViewHolder {
        ImageView softGameThumbnail;
        TextView softGameTitle;
        TextView softGameType;
        TextView softGamePlayButton;

        SoftGameItemViewHolder(View itemView) {
            super(itemView);
            softGameThumbnail = (ImageView) itemView.findViewById(R.id.soft_game_thumbnail);
            softGameTitle = (TextView) itemView.findViewById(R.id.soft_game_title_tv);
            softGameType = (TextView) itemView.findViewById(R.id.soft_game_type_tv);
            softGamePlayButton = (TextView) itemView.findViewById(R.id.soft_game_play_button);
        }
    }
}
