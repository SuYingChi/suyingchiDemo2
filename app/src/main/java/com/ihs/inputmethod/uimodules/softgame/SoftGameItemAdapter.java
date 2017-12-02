package com.ihs.inputmethod.uimodules.softgame;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.iap.RemoveAdsManager;
import com.ihs.keyboardutils.nativeads.KCNativeAdView;
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


    private String placementName = "";
    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .showImageOnFail(null)
            .imageScaleType(ImageScaleType.EXACTLY)
            .cacheOnDisk(true).build();
    private List<SoftGameItemBean> softGameDisplayItemList = new ArrayList<>();
    private static final int TYPE_GAME = 0;
    private static final int TYPE_AD = 1;
    private KCNativeAdView nativeAdView;

    public SoftGameItemAdapter() {
        this.placementName = HSApplication.getContext().getString(R.string.ad_placement_themetryad);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder viewHolder = null;
        if (viewType == TYPE_AD) {
            View view = LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ad_style_theme_card, null);
            LinearLayout loadingView = (LinearLayout) LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ad_loading_3, null);
            int width = HSDisplayUtils.getScreenWidthForContent() - HSDisplayUtils.dip2px(16);
            LinearLayout.LayoutParams loadingLP = new LinearLayout.LayoutParams(width, (int) (width / 1.9f));
            loadingView.setLayoutParams(loadingLP);
            loadingView.setGravity(Gravity.CENTER);
            nativeAdView = new KCNativeAdView(HSApplication.getContext());
            nativeAdView.setAdLayoutView(view);
            nativeAdView.setLoadingView(loadingView);
            nativeAdView.setPrimaryViewSize(width, (int)(width / 1.9f));
            nativeAdView.load(placementName);
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
            if (softGameDisplayItemList == null || softGameDisplayItemList.isEmpty()) {
                return;
            }
            if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
                position--;
            }

            final SoftGameItemViewHolder softGameItemViewHolder = (SoftGameItemViewHolder) holder;
            final SoftGameItemBean softGameDisplayItem = softGameDisplayItemList.get(position);
            ImageLoader.getInstance().displayImage(softGameDisplayItem.getThumb(), new ImageViewAware(softGameItemViewHolder.softGameThumbnail), displayImageOptions);
            softGameItemViewHolder.softGameTitle.setText(softGameDisplayItem.getName());
            softGameItemViewHolder.softGameType.setText(softGameDisplayItem.getDescription());
            softGameItemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GameActivity.startGame(softGameDisplayItem.getLink(),"game_play_clicked",softGameDisplayItem.getName());
                }
            });
            softGameItemViewHolder.softGamePlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GameActivity.startGame(softGameDisplayItem.getLink(),"game_play_clicked",softGameDisplayItem.getName());
                }
            });
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (nativeAdView != null) {
            nativeAdView.release();
            nativeAdView = null;
        }
    }

    public void refreshDataList(List<SoftGameItemBean> softGameItemBeanList) {
        this.softGameDisplayItemList = softGameItemBeanList;
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
