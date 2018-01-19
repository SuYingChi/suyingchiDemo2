package com.ihs.inputmethod.home.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.ihs.inputmethod.home.HomeModel.HomeModel;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;
import com.ihs.inputmethod.uimodules.utils.DisplayUtils;

import java.util.List;

public final class HomeMenuAdapterDelegate extends AdapterDelegate<List<HomeModel>> {

    @Override
    protected boolean isForViewType(@NonNull List<HomeModel> items, int position) {
        return items.get(position).isMenu;
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        HomeMenuViewHolder homeMenuViewHolder = new HomeMenuViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_menu, parent, false));
        int width = (parent.getMeasuredWidth() - parent.getPaddingLeft() - parent.getPaddingRight() - DisplayUtils.dip2px(parent.getContext(), 8)) / 2;
        int height = (int) (111.0 / 165 * width);
        homeMenuViewHolder.itemView.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
        return homeMenuViewHolder;
    }

    @Override
    protected void onBindViewHolder(@NonNull List<HomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        HomeModel model = items.get(position);
        HomeMenuViewHolder viewHolder = (HomeMenuViewHolder) holder;
        viewHolder.menuTitle.setText(model.menuTextResId);
        viewHolder.menuBg.setImageResource(model.menuBgResId);
        viewHolder.menuIcon.setImageResource(model.menuIconResId);

    }

    @Override
    public int getSpanSize(List<HomeModel> items, int position) {
        return 1;
    }
}
