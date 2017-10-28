package com.ihs.inputmethod.uimodules.ui.sticker.homeui;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegatesManager;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiSticker;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.StickerHomeModel;

import java.util.List;


/**
 * Created by guonan.lv on 17/8/10.
 */

public class CommonStickerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public interface OnStickerItemClickListener {
        void onFacemojiClick(FacemojiSticker facemojiSticker);
        void onCardClick(StickerHomeModel stickerHomeModel, Drawable drawable);
        void onDownloadClick(StickerHomeModel stickerHomeModel, Drawable drawable);
    }

    protected AdapterDelegatesManager<List<StickerHomeModel>> delegatesManager;
    private List<StickerHomeModel> items;

    public CommonStickerAdapter() {
        delegatesManager = new AdapterDelegatesManager<>();
    }

    public void setItems(List<StickerHomeModel> items) {
        this.items = items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return delegatesManager.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        delegatesManager.onBindViewHolder(items, position, holder);
    }

    @Override
    public int getItemViewType(int position) {
        return delegatesManager.getItemViewType(items, position);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        delegatesManager.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        delegatesManager.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        delegatesManager.onViewRecycled(holder);
    }

    @Override
    public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
        return delegatesManager.onFailedToRecycleView(holder);
    }

    public int getSpanSize(int position) {
        return delegatesManager.getSpanSize(items, position);
    }
}
