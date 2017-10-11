package com.ihs.inputmethod.uimodules.ui.sticker.homeui.delegate;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiAnimationView;
import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiView;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.StickerHomeModel;

import java.util.List;

public class StickerFacemojiAdapterDelegate extends AdapterDelegate<List<StickerHomeModel>> {

    @Override
    protected boolean isForViewType(@NonNull List<StickerHomeModel> items, int position) {
        return items.get(position).isFacemoji;
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        StickerFacemojiViewHolder viewHolder = new StickerFacemojiViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.facemoji_custom_view, parent, false));
        viewHolder.itemView.getLayoutParams().height = HSApplication.getContext().getResources().getDisplayMetrics().widthPixels / 3;
        return viewHolder;
    }

    @Override
    protected void onBindViewHolder(@NonNull List<StickerHomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        StickerFacemojiViewHolder h = (StickerFacemojiViewHolder) holder;
        h.facemojiAnimationView.setSticker(items.get(position).facemojiSticker);
    }

    @Override
    protected void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        ((StickerFacemojiViewHolder) holder).facemojiView.startAnimation();
    }

    @Override
    protected void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        ((StickerFacemojiViewHolder) holder).facemojiView.stopAnimation();
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public int getSpanSize(List<StickerHomeModel> items, int position) {
        return 2;
    }


    public final class StickerFacemojiViewHolder extends RecyclerView.ViewHolder {
        FacemojiView facemojiView;
        FacemojiAnimationView facemojiAnimationView;

        public StickerFacemojiViewHolder(View itemView) {
            super(itemView);
            facemojiView = (FacemojiView) itemView.findViewById(R.id.facemoji_layout);
            facemojiAnimationView = (FacemojiAnimationView) itemView.findViewById(R.id.sticker_player_view);
        }
    }
}
