package com.ihs.inputmethod.home.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ihs.inputmethod.home.model.HomeModel;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;

import java.util.List;

public final class HomeTitleAdapterDelegate extends AdapterDelegate<List<HomeModel>> {

    @Override
    protected boolean isForViewType(@NonNull List<HomeModel> items, int position) {
        return items.get(position).isTitle;
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new HomeTitleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_theme_title, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull List<HomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        HomeModel model = items.get(position);
        HomeTitleViewHolder viewHolder = (HomeTitleViewHolder) holder;
        viewHolder.title.setText(model.title);

        if (model.rightButtonText != null) {
            viewHolder.btn.setText(model.rightButtonText);
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
    public int getSpanSize(List<HomeModel> items, int position) {
        return 2;
    }
}
