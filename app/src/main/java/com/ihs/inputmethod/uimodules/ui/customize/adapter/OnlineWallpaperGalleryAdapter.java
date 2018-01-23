package com.ihs.inputmethod.uimodules.ui.customize.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.kc.utils.KCAnalytics;
import com.ihs.commons.utils.HSLog;
import com.ihs.feature.common.CompatUtils;
import com.ihs.inputmethod.feature.common.CommonUtils;
import com.ihs.inputmethod.feature.common.Utils;
import com.ihs.inputmethod.feature.common.ViewUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.customize.WallpaperInfo;
import com.ihs.inputmethod.uimodules.ui.customize.WallpaperPreviewActivity;
import com.ihs.inputmethod.uimodules.ui.customize.util.WallpaperDownloadEngine;
import com.ihs.inputmethod.uimodules.ui.customize.view.LoadingProgressBar;

import java.util.ArrayList;
import java.util.List;

import static com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade;


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
    private int mMaxVisiblePosition;
    private int mCategoryIndex = -1;
    private List<Object> mDataSet = new ArrayList<>();
    private GridLayoutManager mLayoutManager;
    private FooterViewHolder mFooterViewHolder;

    private int mScreenWidth;

    private WallpaperDownloadEngine.OnLoadWallpaperListener mListener = new WallpaperDownloadEngine.OnLoadWallpaperListener() {
        @Override
        public void onLoadFinished(List<WallpaperInfo> wallpaperInfoList) {
//            int lastSize = mDataSet.size();
            mDataSet.addAll(wallpaperInfoList);
            notifyDataSetChanged();
//            notifyItemRangeInserted(lastSize, wallpaperInfoList.size());
        }

        @Override
        public void onLoadFailed() {
            if (mFooterViewHolder != null) {
                mFooterViewHolder.mLoadingHint.setVisibility(View.INVISIBLE);
                mFooterViewHolder.mProgressBar.setVisibility(View.INVISIBLE);
                mFooterViewHolder.mProgressBar.stopAnimation();
                mFooterViewHolder.mRetryHint.setVisibility(View.VISIBLE);
                mFooterViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadWallpaper();
                    }
                });
            }
        }
    };

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
            case WALLPAPER_FOOTER_VIEW_LOAD_MORE:
                mFooterViewHolder = new FooterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_auto, parent, false));
                return mFooterViewHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case WALLPAPER_IMAGE_VIEW:
                mMaxVisiblePosition = Math.max(mMaxVisiblePosition, position);
                WallpaperInfo info = (WallpaperInfo) mDataSet.get(position);
                RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.wallpaper_loading)
                        .error(R.drawable.wallpaper_load_failed).diskCacheStrategy(DiskCacheStrategy.DATA);
                Glide.with(holder.itemView.getContext()).asBitmap().apply(requestOptions)
                        .load(info.getThumbnailUrl()).transition(withCrossFade(500))
                        .into(((ImageViewHolder) holder).mImageView);
                holder.itemView.setTag(position);
                ImageViewHolder imageHolder = (ImageViewHolder) holder;
                imageHolder.mTvPopularity.setVisibility(View.INVISIBLE);
                imageHolder.mIvPopularity.setVisibility(View.INVISIBLE);

                break;
            case WALLPAPER_AD_VIEW:
                break;
            case WALLPAPER_FOOTER_VIEW_LOAD_MORE:
                loadWallpaper();
                break;
            case WALLPAPER_FOOTER_VIEW_NO_MORE:
                break;
        }
    }

    public WallpaperDownloadEngine.OnLoadWallpaperListener getLoadWallpaperListener() {
        return mListener;
    }

    @Override
    public int getItemCount() {
        return mDataSet.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mDataSet.size()) {
            return WALLPAPER_FOOTER_VIEW_LOAD_MORE;
        }
        return WALLPAPER_IMAGE_VIEW;
    }

    @Override
    public void onClick(View v) {
        int positionInAllWallpapers = (int) v.getTag();
        ArrayList<WallpaperInfo> allWallpapers = new ArrayList<>();
        ArrayList<WallpaperInfo> wallpapersToPreview = new ArrayList<>();
        for (Object item : mDataSet) {
            if (item instanceof WallpaperInfo) {
                allWallpapers.add((WallpaperInfo) item);
                wallpapersToPreview.add((WallpaperInfo) item);
            }
        }
        WallpaperInfo clickedWallpaper = allWallpapers.get(positionInAllWallpapers);
        if (clickedWallpaper.getCategory() == null) {
            KCAnalytics.logEvent("app_wallpaper_clicked", "name", clickedWallpaper.getName());
        } else {
            KCAnalytics.logEvent("app_wallpaper_clicked", "name", clickedWallpaper.getName(), "tabName", clickedWallpaper.getCategory().categoryName);
        }
        Intent intent = new Intent(mContext, WallpaperPreviewActivity.class);
//        intent.putExtra(WallpaperPreviewActivity.INTENT_KEY_SCENARIO, mScenario.ordinal());
        intent.putParcelableArrayListExtra(WallpaperPreviewActivity.INTENT_KEY_WALLPAPERS, wallpapersToPreview);
//        WallPaperConstant.wallpaperInfoList.addAll(wallpapersToPreview);
        intent.putExtra(WallpaperPreviewActivity.INTENT_KEY_INDEX, positionInAllWallpapers);
        try {
            mContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: consider sending wallpaper data through file to avoid TransactionTooLargeException
            HSLog.e("OnlineWallpaperGalleryAdapter", "Error launching WallpaperPreviewActivity, "
                    + "perhaps wallpaper data is too large to transact through binder." + mDataSet.size());
//            CrashlyticsCore.getInstance().logException(e);
        }
    }

    private boolean isDeviceAlwaysReturnCancel() {
        return CompatUtils.IS_HUAWEI_DEVICE && Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT;
    }

    public void onDestroy() {

    }

    private void loadWallpaper() {
        mFooterViewHolder.mProgressBar.startLoadingAnimation();
        mFooterViewHolder.mProgressBar.setVisibility(View.VISIBLE);
        mFooterViewHolder.mLoadingHint.setVisibility(View.VISIBLE);
        mFooterViewHolder.mRetryHint.setVisibility(View.INVISIBLE);
        if (Utils.isNetworkAvailable(-1)) {
            WallpaperDownloadEngine.getNextCategoryWallpaperList(mCategoryIndex, mListener);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mListener.onLoadFailed();
                }
            }, 1000);
        }
    }

    private static class FooterViewHolder extends RecyclerView.ViewHolder {
        private LoadingProgressBar mProgressBar;
        private TextView mLoadingHint;
        private TextView mRetryHint;

        public FooterViewHolder(View itemView) {
            super(itemView);
            mProgressBar = ViewUtils.findViewById(itemView, R.id.progress_bar);
            mLoadingHint = ViewUtils.findViewById(itemView, R.id.loading_hint);
            mRetryHint = ViewUtils.findViewById(itemView, R.id.retry_hint);
        }
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
