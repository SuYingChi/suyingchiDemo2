package com.ihs.inputmethod.uimodules.ui.sticker.homeui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegatesManager;
import com.ihs.inputmethod.uimodules.ui.sticker.homeui.delegate.StickerHomeCardAdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.StickerHomeModel;

import java.util.List;


/**
 * Created by guonan.lv on 17/8/10.
 */

public class CommonStickerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    public interface OnStickerItemClickListener {
        void onCardClick(StickerHomeModel stickerHomeModel);

        void onDownloadClick(StickerHomeModel stickerHomeModel);

    }


    private OnStickerItemClickListener StickerItemClickListener;
    protected AdapterDelegatesManager<List<StickerHomeModel>> delegatesManager;
    private List<StickerHomeModel> items;

    public CommonStickerAdapter(OnStickerItemClickListener StickerItemClickListener) {
        this.StickerItemClickListener = StickerItemClickListener;
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

    @Override
    public void onClick(final View v) {
        final StickerHomeModel model = (StickerHomeModel) v.getTag();
        final int key = (int) v.getTag(R.id.theme_card_view_tag_key_action);

        switch (key) {
            case StickerHomeCardAdapterDelegate.TAG_CARD:
                StickerItemClickListener.onCardClick(model);
                break;
            case StickerHomeCardAdapterDelegate.TAG_DOWNLOAD:
                StickerItemClickListener.onDownloadClick(model);
                break;
            default:
                break;
        }
    }

}
