package com.ihs.inputmethod.home.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.ihs.inputmethod.home.HomeModel.HomeModel;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;
import com.ihs.inputmethod.uimodules.utils.DisplayUtils;

import java.util.List;


public final class HomeThemeListAdapterDelegate extends AdapterDelegate<List<HomeModel>> {
    public HomeThemeListAdapterDelegate() {

    }

    @Override
    protected boolean isForViewType(@NonNull List<HomeModel> items, int position) {
        return items.get(position).isThemeList;
    }

    @Override
    public int getSpanSize(List<HomeModel> items, int position) {
        return 2;
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        RecyclerView.ViewHolder viewHolder = new RecyclerView.ViewHolder(new RecyclerView(parent.getContext())) {
        };
        viewHolder.itemView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtils.dip2px(parent.getContext(), 150)));
        return viewHolder;
    }

    @Override
    protected void onBindViewHolder(@NonNull List<HomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {

    }
}
