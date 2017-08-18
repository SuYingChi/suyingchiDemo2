package com.ihs.inputmethod.uimodules.ui.sticker;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.control.DownloadManager;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.ui.view.GifView;
import com.ihs.keyboardutils.view.HSGifImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.download.DownloadTask;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.download.GifDownloadTask;

import java.io.File;
import java.util.List;

/**
 * Created by yanxia on 2017/6/6.
 */

public class StickerPageAdapter extends RecyclerView.Adapter<StickerPageAdapter.ViewHolder> implements View.OnClickListener , StickerDownloadManager.LoadingAssetStickerCallback{

    public interface OnStickerClickListener {
        void onStickerClick(Sticker sticker);
    }

    private final int childViewHeight;
    private final int childViewWidth;
    private final StickerPageAdapter.OnStickerClickListener onStickerClickListener;
    private List<StickerPanelItem> stickerPanelItems;
    DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .showImageOnLoading(R.drawable.ic_sticker_loading_image)
            .showImageOnFail(null)
            .imageScaleType(ImageScaleType.EXACTLY)
            .cacheOnDisk(true).build();

    public StickerPageAdapter(int childViewHeight, int childViewWidth, OnStickerClickListener onStickerClickListener) {
        this.childViewHeight = childViewHeight;
        this.childViewWidth = childViewWidth;
        this.onStickerClickListener = onStickerClickListener;
    }

    @Override
    public StickerPageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new StickerPageAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.common_sticker_view, parent, false));
    }

    @Override
    public void onBindViewHolder(StickerPageAdapter.ViewHolder holder, int position) {
        if (stickerPanelItems == null) {
            return;
        }
        final ImageView stickerImageView = holder.stickerImageView;
        final HSGifImageView stickerGifView = holder.stickerGifView;
        stickerImageView.setSoundEffectsEnabled(false);
        stickerGifView.setVisibility(View.GONE);
        stickerImageView.setVisibility(View.VISIBLE);

        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        lp.height = childViewHeight;

        final StickerPanelItem stickerPanelItem = stickerPanelItems.get(position);
        if (stickerPanelItem.isSticker()) {
            Sticker stickerItem = stickerPanelItem.getSticker();
            String stickerImageUri = stickerItem.getStickerUri();

            if (!stickerItem.getStickerFileSuffix().endsWith(Sticker.STICKER_IMAGE_GIF_SUFFIX)) {
                ImageLoader.getInstance().displayImage(stickerImageUri, new ImageViewAware(stickerImageView), displayImageOptions, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        int padding = HSDisplayUtils.dip2px(10);
                        view.setPadding(padding, padding, padding, padding);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        int padding = HSDisplayUtils.dip2px(5);
                        view.setPadding(padding, padding, padding, padding);
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                    }
                });
                stickerGifView.setVisibility(View.GONE);
                stickerImageView.setVisibility(View.VISIBLE);

                stickerImageView.setTag(stickerPanelItem.getSticker());
                stickerImageView.setOnClickListener(this);
                stickerImageView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        final int action = event.getAction();
                        switch (action) {
                            case MotionEvent.ACTION_DOWN:
                                final Animation downAni = createScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f);
                                stickerImageView.startAnimation(downAni);
                                break;
                            case MotionEvent.ACTION_UP:
                            case MotionEvent.ACTION_CANCEL:
                                final Animation upAni = createScaleAnimation(0.9f, 1.0f, 0.9f, 1.0f);
                                stickerImageView.startAnimation(upAni);
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });

            }else {
                stickerGifView.setVisibility(View.VISIBLE);
                stickerImageView.setVisibility(View.GONE);

                String stickLocalFilePath = StickerUtils.getStickerLocalPath(stickerItem);
                File file  = new File(stickLocalFilePath);
                if (file.exists()) {
                    stickerGifView.setImageURI(Uri.fromFile(file));

                }else {
                    System.out.printf("Failed..."+stickLocalFilePath);

                    StickerDownloadManager.getInstance().tryLoadAssetSticker(stickerItem,holder.stickerGifView,this);

                }

                stickerGifView.setTag(stickerPanelItem.getSticker());
                stickerGifView.setOnClickListener(this);
                stickerGifView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        final int action = event.getAction();
                        switch (action) {
                            case MotionEvent.ACTION_DOWN:
                                final Animation downAni = createScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f);
                                stickerGifView.startAnimation(downAni);
                                break;
                            case MotionEvent.ACTION_UP:
                            case MotionEvent.ACTION_CANCEL:
                                final Animation upAni = createScaleAnimation(0.9f, 1.0f, 0.9f, 1.0f);
                                stickerGifView.startAnimation(upAni);
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });

            }


            lp.width = childViewWidth;

        } else if (stickerPanelItem.isDivider()) {
            stickerImageView.setVisibility(View.GONE);
            stickerImageView.setClickable(false);
            lp.width = HSDisplayUtils.dip2px(20);
        } else {
            stickerImageView.setVisibility(View.GONE);
            stickerImageView.setClickable(false);
            lp.width = childViewWidth;
        }
        holder.itemView.setLayoutParams(lp);
    }

    private static Animation createScaleAnimation(final float fromX, final float toX, final float fromY, final float toY) {
        final ScaleAnimation animation = new ScaleAnimation(fromX, toX, fromY, toY,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(10);
        animation.setFillAfter(true);
        return animation;
    }

    @Override
    public int getItemCount() {
        if (stickerPanelItems != null) {
            return stickerPanelItems.size();
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setData(List<StickerPanelItem> stickerPanelItems) {
        this.stickerPanelItems = stickerPanelItems;
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        final Object tag = v.getTag();
        if (tag != null && tag instanceof Sticker && onStickerClickListener != null) {
            onStickerClickListener.onStickerClick((Sticker) tag);
        }
    }

    @Override
    public void processSucceeded(Sticker sticker,File file, View view) {

        final HSGifImageView hsGifImageView = (HSGifImageView) view;
        if(file != null && file.exists() && hsGifImageView != null){
            hsGifImageView.setImageURI(Uri.fromFile(file));
            hsGifImageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void processFailed(Sticker sticker, Exception e){
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView stickerImageView;
        HSGifImageView  stickerGifView;
        public ViewHolder(View itemView) {
            super(itemView);
            stickerImageView = (ImageView) itemView.findViewById(R.id.sticker_view);
            stickerGifView = (HSGifImageView) itemView.findViewById(R.id.sticker_gif_view);
        }
    }
}
