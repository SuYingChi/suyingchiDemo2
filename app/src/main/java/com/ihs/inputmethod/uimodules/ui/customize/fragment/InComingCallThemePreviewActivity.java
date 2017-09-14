package com.ihs.inputmethod.uimodules.ui.customize.fragment;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.acb.call.service.InCallWindow;
import com.acb.call.themes.Type;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.feature.common.ViewUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.customize.adapter.LockerThemeGalleryAdapter;

import java.util.HashMap;
import java.util.Map;

import static com.acb.call.themes.Type.CONFIG_KEY_GIF_URL;
import static com.acb.call.themes.Type.CONFIG_KEY_HOT;
import static com.acb.call.themes.Type.CONFIG_KEY_ICON_ACCEPT;
import static com.acb.call.themes.Type.CONFIG_KEY_ICON_REJECT;
import static com.acb.call.themes.Type.CONFIG_KEY_ID;
import static com.acb.call.themes.Type.CONFIG_KEY_ID_NAME;
import static com.acb.call.themes.Type.CONFIG_KEY_RES_TYPE;
import static com.acb.call.themes.Type.STARS;

/**
 * Created by guonan.lv on 17/9/13.
 */

public class InComingCallThemePreviewActivity extends Activity implements View.OnClickListener {

    private View returnView;
    private ImageView callThemeGifPreview;
    private ProgressBar loadingProgressBar;

    private String themeName;
    private InCallWindow.MyBinder myBinder;
    private ImageButton button;

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
        button = ViewUtils.findViewById(this, R.id.button);
        button.setOnClickListener(this);
        themeName = getIntent().getStringExtra("ThemeInfo");
        initView();
    }

    private void initView() {
        RequestOptions options = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE);
//        Glide.with(this).load(LockerThemeGalleryAdapter.getInComingCallThemeThumbnailUrl(themeName)).apply(options)
//                .into(callThemeGifPreview);

        RequestListener<Drawable> requestListener = new RequestListener<Drawable>() {


            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                callThemeGifPreview.setImageDrawable(resource);
                return false;
            }
        };

        RequestBuilder<Drawable> requestBuilder = Glide.with(this).load(LockerThemeGalleryAdapter.getInComingCallThemeGifUrl(themeName)).apply(options);
//                .listener(requestListener);
//        requestBuilder.into(callThemeGifPreview);
        requestBuilder.thumbnail(Glide.with(this).load(LockerThemeGalleryAdapter.getInComingCallThemeThumbnailUrl(themeName))).into((callThemeGifPreview));
//        Glide.with(this).asGif().load(LockerThemeGalleryAdapter.getInComingCallThemeGifUrl(themeName))
//                .thumbnail(Glide.with(this).load(LockerThemeGalleryAdapter.getInComingCallThemeThumbnailUrl(themeName)).listener(requestListener)).into(callThemeGifPreview);
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
            case R.id.button:
                Intent intent = new Intent(InComingCallThemePreviewActivity.this, InCallWindow.class);
                bindService(intent, serviceConnection, BIND_AUTO_CREATE);
                break;
        }
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (InCallWindow.MyBinder) service;
            Map<String, Object> item = new HashMap<>();
            item.put(CONFIG_KEY_RES_TYPE, "resName");
            item.put("Name", "custom_theme_font");
            item.put(CONFIG_KEY_ICON_ACCEPT, "acb_phone_call_answer");
            item.put(CONFIG_KEY_ICON_REJECT, "acb_phone_call_refuse");
            item.put(CONFIG_KEY_HOT, false);
            item.put(CONFIG_KEY_GIF_URL, LockerThemeGalleryAdapter.getInComingCallThemeGifUrl(themeName));
            item.put(CONFIG_KEY_ID, STARS);
            item.put(CONFIG_KEY_ID_NAME, themeName);
            Type themeType = Type.typeFromMap(item);
            HSLog.e("eee", "service");
            myBinder.startDownload("2", themeType);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private class CustomImageLoadingTarget extends ImageViewTarget<Drawable> {

        public CustomImageLoadingTarget(ImageView view) {
            super(view);
        }

        @Override
        public void onLoadStarted(@Nullable Drawable placeholder) {
            super.onLoadStarted(placeholder);
        }

        @Override
        public void onLoadFailed(@Nullable Drawable errorDrawable) {
            super.onLoadFailed(errorDrawable);
        }

        @Override
        public void onResourceReady(Drawable resource, @Nullable Transition<? super Drawable> transition) {

        }

        @Override
        protected void setResource(@Nullable Drawable resource) {

        }

        @Override
        public void setRequest(Request request) {
            view.setTag(R.id.glide_tag_id, request);
        }

        @Override
        public Request getRequest() {
            return (Request) view.getTag(R.id.glide_tag_id);
        }
    }

}
