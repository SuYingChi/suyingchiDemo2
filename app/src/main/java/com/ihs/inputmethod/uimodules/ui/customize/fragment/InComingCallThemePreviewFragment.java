package com.ihs.inputmethod.uimodules.ui.customize.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.customize.adapter.LockerThemeGalleryAdapter;

/**
 * Created by guonan.lv on 17/9/13.
 */

public class InComingCallThemePreviewFragment extends Fragment implements View.OnClickListener{

    private View returnView;
    private ImageView callThemeGifPreview;
    private ProgressBar loadingProgressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.incoming_call_theme_preview, container, false);
        returnView = view.findViewById(R.id.view_return);
        returnView.setOnClickListener(this);
        callThemeGifPreview = (ImageView) view.findViewById(R.id.call_theme_gif_view);
        callThemeGifPreview.setOnClickListener(this);
        loadingProgressBar = (ProgressBar) view.findViewById(R.id.loading_progress);
        loadingProgressBar.setOnClickListener(this);
        initView();
        return view;
    }

    private void initView() {
        Glide.with(getActivity()).load(LockerThemeGalleryAdapter.getInComingCallThemeThumbnailUrl("name")).into(callThemeGifPreview);

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

    }
}
