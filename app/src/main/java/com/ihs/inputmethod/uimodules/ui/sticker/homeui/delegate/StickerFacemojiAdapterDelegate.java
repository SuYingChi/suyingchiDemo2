package com.ihs.inputmethod.uimodules.ui.sticker.homeui.delegate;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiAnimationView;
import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiView;
import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiSticker;
import com.ihs.inputmethod.uimodules.ui.sticker.homeui.CommonStickerAdapter;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.StickerHomeModel;

import java.util.List;

public class StickerFacemojiAdapterDelegate extends AdapterDelegate<List<StickerHomeModel>> {
    private CommonStickerAdapter.OnStickerItemClickListener onStickerItemClickListener;

    public StickerFacemojiAdapterDelegate(CommonStickerAdapter.OnStickerItemClickListener onStickerItemClickListener) {
        this.onStickerItemClickListener = onStickerItemClickListener;
    }

    @Override
    protected boolean isForViewType(@NonNull List<StickerHomeModel> items, int position) {
        return items.get(position).isFacemoji;
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        StickerFacemojiViewHolder viewHolder = new StickerFacemojiViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.facemoji_card_item, parent, false));
        Resources res = HSApplication.getContext().getResources();
        ViewGroup.LayoutParams layoutParams = viewHolder.itemView.getLayoutParams();
        int width = (int) ((res.getDisplayMetrics().widthPixels - res.getDimension(R.dimen.theme_card_recycler_view_padding_left) - res.getDimension(R.dimen.theme_card_recycler_view_padding_right) ) / 3 - res.getDimension(R.dimen.theme_card_recycler_view_card_margin) * 2);
        layoutParams.height = layoutParams.width = width;
        return viewHolder;
    }

    @Override
    protected void onBindViewHolder(@NonNull List<StickerHomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        StickerFacemojiViewHolder h = (StickerFacemojiViewHolder) holder;
        FacemojiSticker facemojiSticker = items.get(position).facemojiSticker;
        h.facemojiAnimationView.setSticker(facemojiSticker);
        h.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onStickerItemClickListener != null){
                    onStickerItemClickListener.onFacemojiClick(facemojiSticker);
                }
            }
        });
    }

    @Override
    protected void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        ((StickerFacemojiViewHolder) holder).facemojiView.startAnimation();
    }

    @Override
    protected void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        ((StickerFacemojiViewHolder) holder).facemojiView.stopAnimation();
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public int getSpanSize(List<StickerHomeModel> items, int position) {
        return 2;
    }


    public final class StickerFacemojiViewHolder extends RecyclerView.ViewHolder {
        FacemojiView facemojiView;
        FacemojiAnimationView facemojiAnimationView;

        public StickerFacemojiViewHolder(View itemView) {
            super(itemView);
            facemojiView = (FacemojiView) itemView.findViewById(R.id.facemoji_layout);
            facemojiAnimationView = (FacemojiAnimationView) itemView.findViewById(R.id.sticker_player_view);
        }
    }
}
