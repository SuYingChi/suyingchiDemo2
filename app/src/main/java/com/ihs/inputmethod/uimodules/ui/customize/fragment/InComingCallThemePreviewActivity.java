package com.ihs.inputmethod.uimodules.ui.customize.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.ihs.inputmethod.feature.common.ViewUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.customize.adapter.LockerThemeGalleryAdapter;

/**
 * Created by guonan.lv on 17/9/13.
 */

public class InComingCallThemePreviewActivity extends Activity implements View.OnClickListener{

    private View returnView;
    private ImageView callThemeGifPreview;
    private ProgressBar loadingProgressBar;

    private String themeName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.incoming_call_theme_preview);
        returnView = ViewUtils.findViewById(this, R.id.view_return);
        returnView.setOnClickListener(this);
        callThemeGifPreview = ViewUtils.findViewById(this, R.id.call_theme_gif_view);
        callThemeGifPreview.setOnClickListener(this);
        loadingProgressBar = ViewUtils.findViewById(this, R.id.loading_progress);
        loadingProgressBar.setOnClickListener(this);
        themeName = getIntent().getStringExtra("ThemeInfo");
        initView();
    }

    private void initView() {
        RequestOptions options = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE);
        Glide.with(this).load(LockerThemeGalleryAdapter.getInComingCallThemeThumbnailUrl(themeName)).apply(options)
                .into(callThemeGifPreview);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_return:
                this.finish();
                break;
            case R.id.call_theme_gif_view:
                break;
        }
    }
}
