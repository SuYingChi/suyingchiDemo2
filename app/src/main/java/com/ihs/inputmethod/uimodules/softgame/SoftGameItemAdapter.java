package com.ihs.inputmethod.uimodules.softgame;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.util.List;

/**
 * Created by yanxia on 2017/7/27.
 */

public class SoftGameItemAdapter extends RecyclerView.Adapter<SoftGameItemAdapter.SoftGameItemViewHolder> {

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


    public SoftGameItemAdapter(List<SoftGameDisplayItem> softGameDisplayItemList, OnSoftGameItemClickListener softGameItemClickListener) {
        this.softGameDisplayItemList = softGameDisplayItemList;
        this.softGameItemClickListener = softGameItemClickListener;
    }

    @Override
    public SoftGameItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SoftGameItemViewHolder(LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.soft_game_item_view, parent, false));
    }

    @Override
    public void onBindViewHolder(SoftGameItemViewHolder holder, int position) {
        if (softGameDisplayItemList.isEmpty()) {
            return;
        }
        final SoftGameDisplayItem softGameDisplayItem = softGameDisplayItemList.get(position);
        ImageLoader.getInstance().displayImage(softGameDisplayItem.getJsonObject().optString("thumbBig"), new ImageViewAware(holder.softGameThumbnail), displayImageOptions);
        holder.softGameTitle.setText(softGameDisplayItem.getJsonObject().optString("title"));
        holder.softGameType.setText(softGameDisplayItem.getJsonObject().optString("type"));
        holder.softGamePlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (softGameItemClickListener != null) {
                    softGameItemClickListener.OnSoftGameItemClick(softGameDisplayItem);
                }
            }
        });
    }

    public void refreshData(List<SoftGameDisplayItem> softGameDisplayItemList) {
        this.softGameDisplayItemList = softGameDisplayItemList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return SoftGameDisplayActivity.SOFT_GAME_LOAD_COUNT;
    }

    class SoftGameItemViewHolder extends RecyclerView.ViewHolder {
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
