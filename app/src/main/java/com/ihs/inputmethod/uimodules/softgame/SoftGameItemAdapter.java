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


    public SoftGameItemAdapter(List<SoftGameDisplayItem> softGameDisplayItemList, OnSoftGameItemClickListener softGameItemClickListener, NativeAdView nativeAdView) {
        this.softGameDisplayItemList = softGameDisplayItemList;
        this.softGameItemClickListener = softGameItemClickListener;
        this.nativeAdView = nativeAdView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder viewHolder = null;
        if (viewType == SoftGameDisplayItem.TYPE_AD) {
            viewHolder = new ViewHolder(nativeAdView) {
            };
        } else if (viewType == SoftGameDisplayItem.TYPE_GAME) {
            viewHolder = new SoftGameItemViewHolder(LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.soft_game_item_view, parent, false));
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        int itemViewType = getItemViewType(position);
        if (itemViewType == SoftGameDisplayItem.TYPE_AD) {
            HSLog.d("current item is Ad.");
        } else {
            final SoftGameItemViewHolder softGameItemViewHolder = (SoftGameItemViewHolder) holder;
            if (position >= softGameDisplayItemList.size()) {
                return;
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
        int adCount = 0;
        for (SoftGameDisplayItem softGameDisplayItem : softGameDisplayItemList) {
            if (softGameDisplayItem.getType() == SoftGameDisplayItem.TYPE_AD) {
                adCount++;
            }
        }
        return SoftGameDisplayActivity.SOFT_GAME_LOAD_COUNT + adCount;
    }

    /**
     * Return the view type of the item at <code>position</code> for the purposes
     * of view recycling.
     * <p>
     * <p>The default implementation of this method returns 0, making the assumption of
     * a single view type for the adapter. Unlike ListView adapters, types need not
     * be contiguous. Consider using id resources to uniquely identify item view types.
     *
     * @param position position to query
     * @return integer value identifying the type of the view needed to represent the item at
     * <code>position</code>. Type codes need not be contiguous.
     */
    @Override
    public int getItemViewType(int position) {
        if (softGameDisplayItemList.isEmpty()) {
            return SoftGameDisplayItem.TYPE_GAME;
        } else {
            if (softGameDisplayItemList.size() > position) {
                return softGameDisplayItemList.get(position).getType();
            } else {
                return SoftGameDisplayItem.TYPE_GAME;
            }
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
