package com.ihs.inputmethod.home.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.callflash.CallFlashListActivity;
import com.ihs.inputmethod.home.model.HomeMenu;
import com.ihs.inputmethod.home.model.HomeModel;
import com.ihs.inputmethod.sexywallpaper.SexyWallpaperActivity;
import com.ihs.inputmethod.stickers.StickerListActivity;
import com.ihs.inputmethod.themes.ThemeListActivity;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;

import java.util.List;

public final class HomeMenuAdapterDelegate extends AdapterDelegate<List<HomeModel>> {
    private Activity activity;
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
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        int width = (HSApplication.getContext().getResources().getDisplayMetrics().widthPixels - layoutParams.leftMargin * 2 - layoutParams.rightMargin * 2 - parent.getPaddingLeft() - parent.getPaddingRight()) / 2;
        int height = (int) (111.0 / 165 * width);
        layoutParams.width = width;
        layoutParams.height = height;
        return holder;
    }

    @Override
    protected void onBindViewHolder(@NonNull List<HomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        HomeModel model = items.get(position);
        HomeMenu homeMenu = (HomeMenu) model.item;

        HomeMenuViewHolder viewHolder = (HomeMenuViewHolder) holder;

        if (homeMenu == HomeMenu.CallFlash) {
            viewHolder.menuTitle.setTextSize(19.9f);
        } else {
            viewHolder.menuTitle.setTextSize(18.3f);
        }

        viewHolder.menuTitle.setText(homeMenu.getTextResId());
        viewHolder.menuBg.setImageResource(homeMenu.getMenuBgResId());
        viewHolder.menuIcon.setImageResource(homeMenu.getMenuIconResId());
        viewHolder.menuBgContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (homeMenu) {
                    case KeyboardThemes:
                        ThemeListActivity.startThisActivity(activity);
                        break;
                    case AdultStickers:
                        StickerListActivity.startThisActivity(activity);
                        break;
                    case SexyWallpaper:
                        SexyWallpaperActivity.startThisActivity(activity);
                        break;
                    case CallFlash:
                        CallFlashListActivity.startThisActivity(activity);
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
