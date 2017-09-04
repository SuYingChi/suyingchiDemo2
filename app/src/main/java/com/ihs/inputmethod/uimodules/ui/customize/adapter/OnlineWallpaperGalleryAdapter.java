package com.ihs.inputmethod.uimodules.ui.customize.adapter;

import android.app.WallpaperInfo;
import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.acb.adadapter.AcbNativeAd;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ihs.inputmethod.feature.common.CommonUtils;
import com.ihs.inputmethod.feature.common.ViewUtils;
import com.ihs.inputmethod.uimodules.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guonan.lv on 17/9/4.
 */

public class OnlineWallpaperGalleryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements View.OnClickListener {

    private static final int WALLPAPER_IMAGE_VIEW = 0;
    private static final int WALLPAPER_AD_VIEW = 1;
    private static final int WALLPAPER_FOOTER_VIEW_LOAD_MORE = 2;
    private static final int WALLPAPER_FOOTER_VIEW_NO_MORE = 3;

    private static final int CATEGORY_TAB_COUNT_WITH_ADS = 3;
    private static final int MAX_CONCURRENT_AD_REQUEST_COUNT = 3;

    private Context mContext;
    private int mCategoryIndex = -1;
    private List<Object> mDataSet = new ArrayList<>();
    private GridLayoutManager mLayoutManager;

    private int mScreenWidth;

    public OnlineWallpaperGalleryAdapter(Context context) {
        super();
        mContext = context;
        mScreenWidth = CommonUtils.getPhoneWidth(context);

//        ((CustomizeActivity) mContext).addActivityResultHandler(this);

        GridLayoutManager.SpanSizeLookup spanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (getItemViewType(position)) {
                    case WALLPAPER_IMAGE_VIEW:
                        return 1;
                    case WALLPAPER_AD_VIEW:
                        return 2;
                    case WALLPAPER_FOOTER_VIEW_LOAD_MORE:
                        return 2;
                    case WALLPAPER_FOOTER_VIEW_NO_MORE:
                        return 2;
                    default:
                        return 1;
                }
            }
        };
        mLayoutManager = new GridLayoutManager(mContext, 2);
        mLayoutManager.setSpanSizeLookup(spanSizeLookup);
    }

    public void setCategoryIndex(int index) {
        mCategoryIndex = index;
    }

    public GridLayoutManager getLayoutManager() {
        return mLayoutManager;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case WALLPAPER_IMAGE_VIEW:
                View wallpaperImageView = LayoutInflater.from(parent.getContext()).inflate(R.layout.online_wallpaper_image_item, parent, false);
                wallpaperImageView.setOnClickListener(this);
                return new ImageViewHolder(wallpaperImageView);
//            case WALLPAPER_FOOTER_VIEW_LOAD_MORE:
//                mFooterViewHolder = new FooterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_auto, parent, false));
//                return mFooterViewHolder;
//            case WALLPAPER_FOOTER_VIEW_NO_MORE:
//                View noMoreView = LayoutInflater.from(parent.getContext()).inflate(
//                        R.layout.gallery_no_more_foot_item, parent, false);
//                FootViewHolder footHolder = new FootViewHolder(noMoreView);
//                footHolder.tvFoot.setText(R.string.online_3d_wallpaper_foot_text);
//                return footHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case WALLPAPER_IMAGE_VIEW:
                mMaxVisiblePosition = Math.max(mMaxVisiblePosition, position);
                WallpaperInfo info = (WallpaperInfo) mDataSet.get(position);
                Glide.with(holder.itemView.getContext()).load(info.getThumbnailUrl()).asBitmap().placeholder(R.drawable.wallpaper_loading)
                        .error(R.drawable.wallpaper_load_failed).crossFade(550).format(DecodeFormat.PREFER_RGB_565)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(
                        ((ImageViewHolder) holder).mImageView);
                holder.itemView.setTag(position);
                ImageViewHolder imageHolder = (ImageViewHolder) holder;
                if (mScenario == WallpaperMgr.Scenario.ONLINE_HOT && info.getType() == WallpaperInfo.WALLPAPER_TYPE_ONLINE) {
                    imageHolder.mTvPopularity.setVisibility(View.VISIBLE);
                    imageHolder.mTvPopularity.setText(String.valueOf(info.getPopularity()));
                    imageHolder.mIvPopularity.setVisibility(View.VISIBLE);
                } else {
                    imageHolder.mTvPopularity.setVisibility(View.INVISIBLE);
                    imageHolder.mIvPopularity.setVisibility(View.INVISIBLE);
                }
                if (info.getType() == WallpaperInfo.WALLPAPER_TYPE_3D) {
                    imageHolder.m3DMark.setVisibility(View.VISIBLE);
                } else {
                    imageHolder.m3DMark.setVisibility(View.INVISIBLE);
                }
                if (shouldShowAds(position)) {
                    loadAds();
                }
                break;
            case WALLPAPER_AD_VIEW:
                AcbNativeAd ad = (AcbNativeAd) mDataSet.get(position);
                ((OnlineWallpaperGalleryAdapter.AdHolder) holder).mAdContentView.fillNativeAd(ad);
                break;
            case WALLPAPER_FOOTER_VIEW_LOAD_MORE:
                loadWallpaper();
                break;
            case WALLPAPER_FOOTER_VIEW_NO_MORE:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    @Override
    public int getItemViewType(int position) {
        return WALLPAPER_IMAGE_VIEW;
    }

    @Override
    public void onClick(View v) {

    }

    private static class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView mImageView;
        private TextView mTvPopularity;
        private ImageView mIvPopularity;

        public ImageViewHolder(View itemView) {
            super(itemView);

            mImageView = ViewUtils.findViewById(itemView, R.id.iv_wallpaper);
            mTvPopularity = ViewUtils.findViewById(itemView, R.id.tv_popularity);
            mIvPopularity = ViewUtils.findViewById(itemView, R.id.iv_popularity);
        }
    }
}
