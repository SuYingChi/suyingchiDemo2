package com.ihs.inputmethod.home.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.home.model.HomeMenu;
import com.ihs.inputmethod.home.model.HomeModel;
import com.ihs.inputmethod.themes.ThemeListActivity;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;

import java.util.List;

public final class HomeMenuAdapterDelegate extends AdapterDelegate<List<HomeModel>> {
    private Activity activity;
    private int margin = HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.home_activity_horizontal_margin);

    public HomeMenuAdapterDelegate(Activity activity) {
        this.activity = activity;
    }

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
        HomeModel model = items.get(position);
        HomeMenu homeMenu = (HomeMenu) model.item;

        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        if (position % 2 == 1) {
            layoutParams.leftMargin = margin;
            layoutParams.rightMargin = margin / 2;
        } else {
            layoutParams.leftMargin = margin / 2;
            layoutParams.rightMargin = margin;
        }

        HomeMenuViewHolder viewHolder = (HomeMenuViewHolder) holder;
        FrameLayout.LayoutParams menuTitleLayoutParams = (FrameLayout.LayoutParams) viewHolder.menuTitle.getLayoutParams();
        menuTitleLayoutParams.leftMargin = (int) (layoutParams.width * 0.066);
        menuTitleLayoutParams.topMargin = (int) (layoutParams.height * 0.066);

        if (homeMenu == HomeMenu.CallFlash) {
            viewHolder.menuTitle.setTextSize(19.9f);
        }else {
            viewHolder.menuTitle.setTextSize(18.3f);
        }

        viewHolder.menuTitle.setText(homeMenu.getTextResId());
        viewHolder.menuBg.setImageResource(homeMenu.getMenuBgResId());
        viewHolder.menuIcon.setImageResource(homeMenu.getMenuIconResId());
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (homeMenu) {
                    case KeyboardThemes:
                        activity.startActivity(new Intent(activity, ThemeListActivity.class));
                        break;
                    case AdultStickers:
                        Toast.makeText(HSApplication.getContext(), "进入Stickers列表", Toast.LENGTH_SHORT).show();
                        break;
                    case SexyWallpaper:
                        Toast.makeText(HSApplication.getContext(), "进入Wallpaper列表", Toast.LENGTH_SHORT).show();
                        break;
                    case CallFlash:
                        Toast.makeText(HSApplication.getContext(), "进入CallFlash列表", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    @Override
    public int getSpanSize(List<HomeModel> items, int position) {
        return 1;
    }
}
