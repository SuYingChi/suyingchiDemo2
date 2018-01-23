package com.ihs.inputmethod.uimodules.ui.sticker;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.view.HSGifImageView;

import java.io.File;
import java.util.List;

/**
 * Created by yanxia on 2017/6/6.
 */

public class StickerPageAdapter extends RecyclerView.Adapter<StickerPageAdapter.ViewHolder> implements StickerDownloadManager.LoadingAssetStickerCallback{

    public interface OnStickerClickListener {
        void onStickerClick(Sticker sticker);
    }

    private final int childViewHeight;
    private final int childViewWidth;
    private final StickerPageAdapter.OnStickerClickListener onStickerClickListener;
    private List<StickerPanelItem> stickerPanelItems;
    private RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.ic_sticker_loading_image);

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
                int padding = HSDisplayUtils.dip2px(10);
                stickerImageView.setPadding(padding, padding, padding, padding);
                Glide.with(HSApplication.getContext()).asBitmap().apply(requestOptions).load(stickerImageUri).listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        int padding = HSDisplayUtils.dip2px(5);
                        stickerImageView.setPadding(padding, padding, padding, padding);
                        return false;
                    }
                }).into(stickerImageView);
                stickerGifView.setVisibility(View.GONE);
                stickerImageView.setVisibility(View.VISIBLE);

                stickerImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onStickerClickListener != null) {
                            onStickerClickListener.onStickerClick(stickerPanelItem.getSticker());
                        }
                    }
                });
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
                stickerGifView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onStickerClickListener != null) {
                            onStickerClickListener.onStickerClick(stickerPanelItem.getSticker());
                        }
                    }
                });
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
            stickerGifView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }
}
