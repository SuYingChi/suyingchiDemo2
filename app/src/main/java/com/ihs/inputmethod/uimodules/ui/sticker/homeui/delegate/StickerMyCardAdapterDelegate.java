package com.ihs.inputmethod.uimodules.ui.sticker.homeui.delegate;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerGroup;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.StickerHomeModel;

import java.util.List;


public final class StickerMyCardAdapterDelegate extends AdapterDelegate<List<StickerHomeModel>> {
    private RequestOptions requestOptions;
    public StickerMyCardAdapterDelegate() {
        Resources resources = HSApplication.getContext().getResources();
        int imageWidth = (int) (resources.getDisplayMetrics().widthPixels / 2 - resources.getDimension(R.dimen.theme_card_recycler_view_card_margin) * 2);
        int imageHeight = (int) (imageWidth / 1.6f);
        requestOptions = new RequestOptions().override(imageWidth, imageHeight);
    }

    @Override
    protected boolean isForViewType(@NonNull List<StickerHomeModel> items, int position) {
        return items.get(position).stickerGroup != null && items.get(position).isDownloaded;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new MyStickerCardViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sticker_card, parent, false));
    }

    @Override
    public int getSpanSize(List<StickerHomeModel> items, int position) {
        return 3;
    }

    @Override
    protected void onBindViewHolder(@NonNull List<StickerHomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        final StickerHomeModel stickerModel = items.get(position);
        final StickerGroup stickerGroup = stickerModel.stickerGroup;
        MyStickerCardViewHolder stickerCardViewHolder = (MyStickerCardViewHolder) holder;
        stickerCardViewHolder.stickerGroupName.setText(stickerGroup.getDownloadDisplayName());
        final String realImageUrl = stickerGroup.getStickerGroupDownloadPreviewImageUri();
        if (realImageUrl != null) {
            stickerCardViewHolder.stickerRealImage.setImageDrawable(null);
            Glide.with(HSApplication.getContext()).asBitmap().apply(requestOptions).load(realImageUrl).into(stickerCardViewHolder.stickerRealImage);
        }
    }

    public class MyStickerCardViewHolder extends RecyclerView.ViewHolder {
        // --Commented out by Inspection (18/1/11 下午2:41):View stickerCardView;
        // --Commented out by Inspection (18/1/11 下午2:41):ImageView moreMenuImage;

        TextView stickerGroupName;
        // --Commented out by Inspection (18/1/11 下午2:41):GifImageView stickerNewImage;
        ImageView stickerRealImage;


        public MyStickerCardViewHolder(View itemView) {
            super(itemView);

            stickerGroupName = itemView.findViewById(R.id.sticker_name);
            stickerRealImage = itemView.findViewById(R.id.sticker_image_real_view);

        }
    }

}
