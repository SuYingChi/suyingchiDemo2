package com.ihs.inputmethod.themes.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.artw.lockscreen.LockerEnableDialog;
import com.artw.lockscreen.LockerSettings;
import com.bumptech.glide.Glide;
import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.keyboard.HSKeyboardTheme;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.theme.HSThemeNewTipController;
import com.ihs.inputmethod.api.utils.HSToastUtils;
import com.ihs.inputmethod.common.adapter.CommonAdapter;
import com.ihs.inputmethod.theme.ThemeLockerBgUtil;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeDetailActivity;
import com.ihs.inputmethod.uimodules.widget.TrialKeyboardDialog;

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by jixiang on 18/1/18.
 */

public class ThemeAdapter<T> extends CommonAdapter<HSKeyboardTheme> {
    private HSKeyboardTheme keyboardThemeOnKeyboardActivation;

    public interface ThemeCardItemClickListener {
        void onCardClick(HSKeyboardTheme keyboardTheme);

        void onMenuShareClick(HSKeyboardTheme keyboardTheme);

        void onMenuDownloadClick(HSKeyboardTheme keyboardTheme);

        void onMenuDeleteClick(HSKeyboardTheme keyboardTheme);

        void onMenuAppliedClick(HSKeyboardTheme keyboardTheme);

        void onKeyboardActivationStart();
    }


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

    public void finishKeyboardActivation(boolean success) {
        if (success && keyboardThemeOnKeyboardActivation != null) {
            HSKeyboardTheme keyboardTheme = keyboardThemeOnKeyboardActivation;
            keyboardThemeOnKeyboardActivation = null;
            if (!HSKeyboardThemeManager.setKeyboardTheme(keyboardTheme.mThemeName)) {
                String failedString = HSApplication.getContext().getResources().getString(R.string.theme_apply_failed);
                HSToastUtils.toastCenterLong(String.format(failedString, keyboardTheme.getThemeShowName()));
                return;
            }else {
                if (LockerSettings.isLockerEnableShowSatisfied()) {
                    LockerEnableDialog.showLockerEnableDialog(activity,
                            ThemeLockerBgUtil.getInstance().getThemeBgUrl(HSKeyboardThemeManager.getCurrentThemeName()),
                            activity.getString(R.string.locker_enable_title_has_text),
                            this::showTryKeyboardDialog);
                } else {
                    showTryKeyboardDialog();
                }
            }
        }
    }

    private void showTryKeyboardDialog() {
        TrialKeyboardDialog trialKeyboardDialog = new TrialKeyboardDialog.Builder(activity).create();
        trialKeyboardDialog.show(true);
    }

    private void setThemeNotNew(HSKeyboardTheme keyboardTheme) {
        HSThemeNewTipController.getInstance().setThemeNotNew(keyboardTheme.mThemeName);
    }
}
