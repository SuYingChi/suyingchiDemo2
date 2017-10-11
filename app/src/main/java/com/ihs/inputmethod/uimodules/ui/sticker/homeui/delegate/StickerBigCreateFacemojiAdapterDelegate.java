package com.ihs.inputmethod.uimodules.ui.sticker.homeui.delegate;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.facemoji.ui.CameraActivity;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.StickerHomeModel;

import java.util.List;

public class StickerBigCreateFacemojiAdapterDelegate extends AdapterDelegate<List<StickerHomeModel>> {

    @Override
    protected boolean isForViewType(@NonNull List<StickerHomeModel> items, int position) {
        return items.get(position).isBigCreateFacemoji;
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new StickerBigCreateFacemojiViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_big_create_facemoji, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull List<StickerHomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        StickerBigCreateFacemojiViewHolder h = (StickerBigCreateFacemojiViewHolder) holder;
        h.createBtn.setOnClickListener(new View.OnClickListener() {
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
        return 6;
    }


    public final class StickerBigCreateFacemojiViewHolder extends RecyclerView.ViewHolder {

        Button createBtn;

        public StickerBigCreateFacemojiViewHolder(View itemView) {
            super(itemView);
            createBtn = (Button) itemView.findViewById(R.id.facemoji_create);
        }
    }
}
