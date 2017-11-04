package com.ihs.inputmethod.uimodules.ui.facemoji;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.BaseTabViewAdapter;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiCategory;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerPanelManager;

import java.util.List;

public class FacemoijTabAdapter extends BaseTabViewAdapter {
    List<FacemojiCategory> facemojiCategoryList;
    boolean isCurrentThemeDarkBg;

    public FacemoijTabAdapter(List<FacemojiCategory> facemojiCategoryList, List<String> facemojiCategoryNameList, OnTabChangeListener onTabChangeListener) {
        super(facemojiCategoryNameList, onTabChangeListener);
        this.facemojiCategoryList = facemojiCategoryList;
        isCurrentThemeDarkBg = HSKeyboardThemeManager.getCurrentTheme().isDarkBg();
    }

    @Override
    public void onBindViewHolder(TagViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        final String tabName = tabNameList.get(position);
        FacemojiCategory facemojiCategory = facemojiCategoryList.get(position);
        if (facemojiCategory.isBuildIn() || facemojiCategory.isDownloadedSuccess()){
            holder.iv_tab_icon.setImageDrawable(facemojiCategory.getCategoryIcon());
            if (tabName.equals(currentTab)) {
                holder.iv_tab_icon.setAlpha(1.0f);
            } else {
                holder.iv_tab_icon.setAlpha(0.5f);
            }
        }else {
            Drawable drawable;
            if (isCurrentThemeDarkBg){
                drawable = HSApplication.getContext().getResources().getDrawable(R.drawable.ic_sticker_loading_image);
            }else {
                drawable = HSApplication.getContext().getResources().getDrawable(R.drawable.ic_sticker_loading_image_grey);
            }
            int padding = HSDisplayUtils.dip2px(4);
            holder.iv_tab_icon.setPadding(padding, padding, padding, padding);
            holder.iv_tab_icon.setAlpha(0.5f);

            RequestOptions requestOptions = new RequestOptions().placeholder(drawable).diskCacheStrategy(DiskCacheStrategy.DATA);
            Glide.with(HSApplication.getContext()).asBitmap().apply(requestOptions).load(FacemojiDownloadManager.getInstance().getRemoteTabIconPath(facemojiCategory.getName())).listener(new RequestListener<Bitmap>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                    holder.iv_tab_icon.setPadding(0, 0, 0, 0);
                    if (tabName.equals(currentTab)) {
                        holder.iv_tab_icon.setAlpha(1.0f);
                    } else {
                        holder.iv_tab_icon.setAlpha(0.5f);
                    }
                    return false;
                }
            }).into(holder.iv_tab_icon);
        }
    }

    @Override
    public TagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TagViewHolder tagViewHolder = super.onCreateViewHolder(parent, viewType);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(HSDisplayUtils.dip2px(35),HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.config_suggestions_strip_height));
        tagViewHolder.itemView.setLayoutParams(lp);

        tagViewHolder.iv_tab_icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        int topMargin = HSDisplayUtils.dip2px(6);
        imageLayoutParams.gravity = Gravity.CENTER;
        imageLayoutParams.topMargin = topMargin;
        imageLayoutParams.bottomMargin = topMargin;
        tagViewHolder.iv_tab_icon.setLayoutParams(imageLayoutParams);
        return tagViewHolder;
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
     */
    public void setTabSelected(int position) {
        String name = facemojiCategoryList.get(position).getName();
        super.setTabSelected(name);
        final View stickerTabView = tabImageViews.get(name);
        if (stickerTabView != null) {
            stickerTabView.setAlpha(1.0f);
        }
    }

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
