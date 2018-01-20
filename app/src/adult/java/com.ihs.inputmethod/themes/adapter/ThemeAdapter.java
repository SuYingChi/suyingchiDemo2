package com.ihs.inputmethod.themes.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.ihs.inputmethod.api.keyboard.HSKeyboardTheme;
import com.ihs.inputmethod.common.adapter.CommonAdapter;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeDetailActivity;

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by jixiang on 18/1/18.
 */

public class ThemeAdapter<T> extends CommonAdapter<HSKeyboardTheme> {
    public ThemeAdapter(Activity activity) {
        super(activity);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ThemeViewHolder(View.inflate(parent.getContext(), R.layout.item_theme, null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ThemeViewHolder themeViewHolder = (ThemeViewHolder) holder;
        HSKeyboardTheme hsKeyboardTheme = dataList.get(position);
        Glide.with(activity).load(hsKeyboardTheme.getSmallPreivewImgUrl()).into(themeViewHolder.themeImage);
        themeViewHolder.themeImageContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ThemeDetailActivity.class);
                intent.putExtra(ThemeDetailActivity.INTENT_KEY_THEME_NAME, hsKeyboardTheme.mThemeName);
                activity.startActivity(intent);
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
