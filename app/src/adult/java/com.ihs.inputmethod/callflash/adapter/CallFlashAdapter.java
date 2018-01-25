package com.ihs.inputmethod.callflash.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.acb.call.themes.Type;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.ihs.feature.common.ViewUtils;
import com.ihs.inputmethod.common.adapter.CommonAdapter;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.customize.InCallThemePreviewActivity;
import com.ihs.inputmethod.uimodules.ui.customize.adapter.LockerThemeGalleryAdapter;

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
        ThemeViewHolder themeHolder = new ThemeViewHolder(grid);
        themeHolder.itemView.setOnClickListener(this);

        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = parent.getResources().getDimensionPixelSize(R.dimen.theme_card_recycler_view_card_margin);
        layoutParams.setMargins(margin, margin, margin, margin);
        themeHolder.itemView.setLayoutParams(layoutParams);
        return themeHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int themeIndex = position;
        final ThemeViewHolder themeHolder = (ThemeViewHolder) holder;
        holder.itemView.setTag(themeIndex);
        final Type themeType = dataList.get(themeIndex);


        Glide.with(activity).asBitmap().apply(requestOptions)
                .load(themeType.getPreviewImage()).transition(withCrossFade(500))
                .into(themeHolder.themeThumbnail);
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

    static class ThemeViewHolder extends ViewHolder {
        ImageView themeThumbnail;

        public ThemeViewHolder(View itemView) {
            super(itemView);
            themeThumbnail = ViewUtils.findViewById(itemView, R.id.theme_thumbnail);
        }
    }

    public static class GridSpanSizer extends GridLayoutManager.SpanSizeLookup {
        LockerThemeGalleryAdapter mAdapter;

        public GridSpanSizer(LockerThemeGalleryAdapter adapter) {
            super();
            mAdapter = adapter;
            setSpanIndexCacheEnabled(true);
        }

        @Override
        public int getSpanSize(int position) {
            return 1;
        }
    }
}
