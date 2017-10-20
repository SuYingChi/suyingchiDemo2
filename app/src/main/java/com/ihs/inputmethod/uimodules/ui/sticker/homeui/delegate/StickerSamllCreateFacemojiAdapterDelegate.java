package com.ihs.inputmethod.uimodules.ui.sticker.homeui.delegate;

import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.facemoji.ui.CameraActivity;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.StickerHomeModel;

import java.util.List;

public class StickerSamllCreateFacemojiAdapterDelegate extends AdapterDelegate<List<StickerHomeModel>> {

    @Override
    protected boolean isForViewType(@NonNull List<StickerHomeModel> items, int position) {
        return items.get(position).isSmallCreateFacemoji;
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        RecyclerView.ViewHolder viewHolder = new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_small_create_facemoji, parent, false)) {
        };
        Resources res = HSApplication.getContext().getResources();
        ViewGroup.LayoutParams layoutParams = viewHolder.itemView.getLayoutParams();
        int width = (int) ((res.getDisplayMetrics().widthPixels - res.getDimension(R.dimen.theme_card_recycler_view_padding_left) - res.getDimension(R.dimen.theme_card_recycler_view_padding_right) ) / 3 - res.getDimension(R.dimen.theme_card_recycler_view_card_margin) * 2);
        layoutParams.height = layoutParams.width = width;
        return viewHolder;
    }

    @Override
    protected void onBindViewHolder(@NonNull List<StickerHomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HSApplication.getContext(), CameraActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                HSApplication.getContext().startActivity(i);
            }
        });
    }

    @Override
    public int getSpanSize(List<StickerHomeModel> items, int position) {
        return 2;
    }
}
