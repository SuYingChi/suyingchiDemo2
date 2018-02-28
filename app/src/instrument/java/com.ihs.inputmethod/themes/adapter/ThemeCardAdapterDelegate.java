package com.ihs.inputmethod.themes.adapter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.keyboard.HSKeyboardTheme;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.home.model.HomeModel;
import com.ihs.inputmethod.themes.ThemeDetailActivity;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;

import java.util.List;

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by jixiang on 18/1/30.
 */

public class ThemeCardAdapterDelegate extends AdapterDelegate<List<HomeModel>> {

    @Override
    protected boolean isForViewType(@NonNull List<HomeModel> items, int position) {
        return items.get(position).isTheme;
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new ThemeViewHolder(View.inflate(parent.getContext(), R.layout.item_theme, null));
    }

    @Override
    protected void onBindViewHolder(@NonNull List<HomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        ThemeViewHolder themeViewHolder = (ThemeViewHolder) holder;
        HSKeyboardTheme hsKeyboardTheme = (HSKeyboardTheme) items.get(position).item;

        switch (hsKeyboardTheme.getThemeType()) {
            case CUSTOM:
                themeViewHolder.themeImage.setImageDrawable(HSKeyboardThemeManager.getThemePreviewDrawable(hsKeyboardTheme));
            case BUILD_IN:
                themeViewHolder.themeImage.setImageDrawable(HSKeyboardThemeManager.getThemePreviewDrawable(hsKeyboardTheme));
                break;
            case NEED_DOWNLOAD:
            case DOWNLOADED:
                Glide.with(HSApplication.getContext()).load(hsKeyboardTheme.getSmallPreivewImgUrl()).into(themeViewHolder.themeImage);
                break;
        }

        themeViewHolder.themeImageContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HSApplication.getContext(), ThemeDetailActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(ThemeDetailActivity.INTENT_KEY_THEME_NAME, hsKeyboardTheme.mThemeName);
                HSApplication.getContext().startActivity(intent);
            }
        });
    }

    private static class ThemeViewHolder extends RecyclerView.ViewHolder {
        FrameLayout themeImageContainer;
        ImageView themeImage;
        ImageView themeDelete;
        GifImageView themeNewImage;
        ImageView themeAnimatedImage;

        public ThemeViewHolder(View itemView) {
            super(itemView);
            themeImageContainer = itemView.findViewById(R.id.theme_image_container);
            themeImage = itemView.findViewById(R.id.theme_image_view);
            themeDelete = itemView.findViewById(R.id.theme_delete_view);
//            themeDelete.setBackgroundDrawable(HSDrawableUtils.getDimmedForegroundDrawable(BitmapFactory.decodeResource(HSApplication.getContext().getResources(), R.drawable.preview_keyboard_delete)));
            themeNewImage = itemView.findViewById(R.id.theme_new_view);
            themeAnimatedImage = itemView.findViewById(R.id.theme_animated_view);
        }
    }
}
