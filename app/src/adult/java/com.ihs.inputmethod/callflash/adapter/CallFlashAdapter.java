package com.ihs.inputmethod.callflash.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.acb.call.themes.Type;
import com.acb.call.views.InCallActionView;
import com.acb.call.views.ThemePreviewWindow;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.ihs.feature.common.ViewUtils;
import com.ihs.inputmethod.common.adapter.CommonAdapter;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.customize.InCallThemePreviewActivity;

import static com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade;

/**
 * Created by jixiang on 18/1/23.
 */

public class CallFlashAdapter extends CommonAdapter<Type> implements View.OnClickListener {
    public CallFlashAdapter(Activity activity) {
        super(activity);
    }

    private RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.locker_theme_thumbnail_loading)
            .error(R.drawable.locker_theme_thumbnail_failed).diskCacheStrategy(DiskCacheStrategy.RESOURCE);

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View grid = View.inflate(parent.getContext(), R.layout.item_call_flash, null);
        CallFlashViewHolder viewHolder = new CallFlashViewHolder(grid);
        viewHolder.itemView.setOnClickListener(this);

        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = parent.getResources().getDimensionPixelSize(R.dimen.theme_card_recycler_view_card_margin);
        layoutParams.setMargins(margin, margin, margin, margin);
        viewHolder.itemView.setLayoutParams(layoutParams);

        viewHolder.inCallActionView.setAutoRun(false);
        viewHolder.themePreviewWindow.setPreviewType(ThemePreviewWindow.PreviewType.PREVIEW);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int themeIndex = position;
        final CallFlashViewHolder viewHolder = (CallFlashViewHolder) holder;
        holder.itemView.setTag(themeIndex);
        final Type themeType = dataList.get(themeIndex);
        viewHolder.themePreviewWindow.updateThemeLayout(themeType);

        Glide.with(activity).asBitmap().apply(requestOptions)
                .load(themeType.getPreviewImage()).transition(withCrossFade(500))
                .into(viewHolder.callFlashImage);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.grid_root:
                int pos = (int) v.getTag();
                Type themeType = dataList.get(pos);
                Intent intent = new Intent(activity, InCallThemePreviewActivity.class);
                intent.putExtra("CallThemeType", themeType);
                activity.startActivity(intent);
                break;
            default:
                break;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class CallFlashViewHolder extends ViewHolder {
        ImageView callFlashImage;
        InCallActionView inCallActionView;
        ThemePreviewWindow themePreviewWindow;
        public CallFlashViewHolder(View itemView) {
            super(itemView);
            callFlashImage = ViewUtils.findViewById(itemView, R.id.call_flash_image);
            inCallActionView = ViewUtils.findViewById(itemView, R.id.in_call_view);
            themePreviewWindow = ViewUtils.findViewById(itemView, R.id.flash_view);
        }
    }
}
