package com.ihs.inputmethod.home.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.home.model.HomeModel;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;

import java.util.List;

public final class HomeMenuAdapterDelegate extends AdapterDelegate<List<HomeModel>> {
    private int margin = HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.home_activity_horizontal_margin);

    @Override
    protected boolean isForViewType(@NonNull List<HomeModel> items, int position) {
        return items.get(position).isMenu;
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        HomeMenuViewHolder holder = new HomeMenuViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_menu, parent, false));

        int width = (HSApplication.getContext().getResources().getDisplayMetrics().widthPixels - margin * 3) / 2;
        int height = (int) (111.0 / 165 * width);
        holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(width, height));
        return holder;
    }

    @Override
    protected void onBindViewHolder(@NonNull List<HomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        if (position % 2 == 1) {
            layoutParams.leftMargin = margin;
            layoutParams.rightMargin = margin / 2;
        } else {
            layoutParams.leftMargin = margin / 2;
            layoutParams.rightMargin = margin;
        }

        HomeMenuViewHolder viewHolder = (HomeMenuViewHolder) holder;
        RelativeLayout.LayoutParams menuTitleLayoutParams = (RelativeLayout.LayoutParams) viewHolder.menuTitle.getLayoutParams();
        menuTitleLayoutParams.leftMargin = (int) (layoutParams.width * 0.066);
        menuTitleLayoutParams.topMargin = (int) (layoutParams.height * 0.066);

        HomeModel model = items.get(position);
        viewHolder.menuTitle.setText(model.menuTextResId);
        viewHolder.menuBg.setImageResource(model.menuBgResId);
        viewHolder.menuIcon.setImageResource(model.menuIconResId);
    }

    @Override
    public int getSpanSize(List<HomeModel> items, int position) {
        return 1;
    }
}
