package com.ihs.inputmethod.uimodules.ui.sticker;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.inputmethod.api.HSUIInputMethodService;

import java.util.List;

public class StickerSuggestionAdapter extends RecyclerView.Adapter<StickerSuggestionAdapter.ViewHolder> {

    private List<Sticker> stickerList;

    // data is passed into the constructor
    public StickerSuggestionAdapter(List<Sticker> stickerList) {
        this.stickerList = stickerList;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImageView itemView = new ImageView(parent.getContext());
        itemView.setLayoutParams(new ViewGroup.LayoutParams(DisplayUtils.dip2px(60), DisplayUtils.dip2px(60)));
        return new ViewHolder(itemView);
    }

    // binds the data to the view and textview in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ImageView itemView = (ImageView) holder.itemView;
        final Sticker sticker = stickerList.get(position);
        Glide.with(HSApplication.getContext()).load(sticker.getFilePath()).into(itemView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StickerUtils.share(sticker, HSUIInputMethodService.getInstance().getCurrentInputEditorInfo().packageName);
            }
        });
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return stickerList.size();
    }

    public void refreshData(List<Sticker> data) {
        this.stickerList = data;
        notifyDataSetChanged();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}