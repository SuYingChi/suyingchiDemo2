package com.ihs.inputmethod.uimodules.softgame;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.nativeads.NativeAdView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanxia on 2017/7/27.
 */

public class SoftGameItemAdapter extends RecyclerView.Adapter<ViewHolder> {


    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .showImageOnFail(null)
            .imageScaleType(ImageScaleType.EXACTLY)
            .cacheOnDisk(true).build();
    private List<SoftGameItemBean> softGameDisplayItemList = new ArrayList<>();
    private NativeAdView nativeAdView;
    private static final int TYPE_GAME = 0;
    private static final int TYPE_AD = 1;

    public SoftGameItemAdapter(NativeAdView nativeAdView) {
        this.nativeAdView = nativeAdView;
    }

    public SoftGameItemAdapter() {

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
            final SoftGameItemBean softGameDisplayItem = softGameDisplayItemList.get(position);
            ImageLoader.getInstance().displayImage(softGameDisplayItem.getThumb(), new ImageViewAware(softGameItemViewHolder.softGameThumbnail), displayImageOptions);
            softGameItemViewHolder.softGameTitle.setText(softGameDisplayItem.getName());
            softGameItemViewHolder.softGameType.setText(softGameDisplayItem.getDescription());
            softGameItemViewHolder.softGamePlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        Intent intent = new Intent(HSApplication.getContext(), GameActivity.class);
                        intent.putExtra("url", softGameDisplayItem.getLink());
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        HSApplication.getContext().startActivity(intent);
                        HSAnalytics.logEvent("game_play_clicked", "game_play_clicked", softGameDisplayItem.getName());
                }
            });
        }
    }

    public void refreshDataList(List<SoftGameItemBean> softGameItemBeanList) {
        this.softGameDisplayItemList = softGameItemBeanList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
//        if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
//            return softGameDisplayItemList.size() + 1;
//        } else {
            return softGameDisplayItemList.size();
//        }
    }

    @Override
    public int getItemViewType(int position) {
//        if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
//            if (position == 0) {
//                return TYPE_AD;
//            } else {
//                return TYPE_GAME;
//            }
//        } else {
            return TYPE_GAME;
//        }
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
