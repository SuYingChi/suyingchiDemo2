package com.ihs.inputmethod.uimodules.ui.sticker;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
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
import com.ihs.inputmethod.uimodules.ui.common.BaseTabViewAdapter;

import java.util.List;

/**
 * Created by yanxia on 2017/6/5.
 */

public class StickerTabAdapter extends BaseTabViewAdapter {
    private RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.ic_sticker_loading_image).override( HSDisplayUtils.dip2px(30), HSDisplayUtils.dip2px(30));

    public StickerTabAdapter(List<String> stickerTabNameList, OnTabChangeListener onTabChangeListener) {
        super(stickerTabNameList, onTabChangeListener);
    }

    @Override
    public void onBindViewHolder(TagViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        final String tabName = tabNameList.get(position);
        Drawable tabDrawable = getTabView(tabName);
        if (tabDrawable == null) {
            ImageView stickerTabImageView = (ImageView) holder.itemView.findViewById(R.id.tab_icon_iv);
            StickerGroup stickerGroup = StickerUtils.getStickerGroupByName(tabName);
            String stickerPreviewImageUriStr;
            if (stickerGroup != null) {
                stickerPreviewImageUriStr = stickerGroup.getStickerGroupPreviewImageUri();
            } else {
                StringBuilder stickerPreviewImageUri = new StringBuilder(StickerUtils.getStickerDownloadBaseUrl())
                        .append(tabName).append("/").append(tabName).append(StickerUtils.STICKER_TAB_IMAGE_SUFFIX);
                stickerPreviewImageUriStr = stickerPreviewImageUri.toString();
            }
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            lp.width = HSDisplayUtils.dip2px(35);
            int padding = HSDisplayUtils.dip2px(8);
            stickerTabImageView.setPadding(padding, padding, padding, padding);
            Glide.with(HSApplication.getContext()).asBitmap().apply(requestOptions).load(stickerPreviewImageUriStr).listener(new RequestListener<Bitmap>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                    int padding = HSDisplayUtils.dip2px(6);
                    stickerTabImageView.setPadding(padding, padding, padding, padding);
                    if (tabName.equals(currentTab)) {
                        stickerTabImageView.setAlpha(1.0f);
                    } else {
                        stickerTabImageView.setAlpha(0.5f);
                    }
                    return false;
                }
            }).into(stickerTabImageView);
            holder.itemView.setLayoutParams(lp);
        }
    }


    @Override
    protected Drawable getTabView(String tab) {
        return getStickerTabDrawable(tab);
    }

    @Override
    protected int getTabIndex(String tab) {
        return super.getTabIndex(tab);
    }

    /**
     * just select tab
     *
     * @param tab
     */
    @Override
    public void setTabSelected(String tab) {
        super.setTabSelected(tab);
        final View stickerTabView = tabImageViews.get(tab);
        if (stickerTabView != null) {
            stickerTabView.setAlpha(1.0f);
        }
    }

    @Override
    protected void clearSelection() {
        super.clearSelection();
        for (final String tab : tabNameList) {
            final View iv = tabImageViews.get(tab);
            if (iv != null && !tab.equals(StickerPanelManager.STICKER_RECENT)) {
                iv.setAlpha(0.5f);
            }
        }
    }
}
