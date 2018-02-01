package com.ihs.inputmethod.uimodules.ui.sticker.homeui.delegate;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.StickerHomeModel;

import java.util.List;

public final class StickerTitleAdapterDelegate extends AdapterDelegate<List<StickerHomeModel>> {
    @Override
    protected boolean isForViewType(@NonNull List<StickerHomeModel> items, int position) {
        return items.get(position).isTitle;
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new StickerTitleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_theme_title, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull List<StickerHomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        StickerHomeModel model = items.get(position);
        StickerTitleViewHolder viewHolder = (StickerTitleViewHolder) holder;
        viewHolder.title.setText(model.title);

        if (model.rightButton != null) {
            viewHolder.btn.setText(model.rightButton);
        } else {
            viewHolder.btn.setText("");
        }

        if (model.titleClickListener != null) {
            holder.itemView.setOnClickListener(model.titleClickListener);
        } else {
            holder.itemView.setOnClickListener(null);
        }

        holder.itemView.setClickable(model.titleClickable);
    }

    @Override
    public int getSpanSize(List<StickerHomeModel> items, int position) {
        return 6;
    }

    public final class StickerTitleViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView btn;

        public StickerTitleViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title_left);
            btn = itemView.findViewById(R.id.title_btn_right);
        }
    }
}
