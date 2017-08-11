package com.ihs.inputmethod.uimodules.ui.sticker.homeui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.inputmethod.uimodules.R;

/**
 * Created by guonan.lv on 17/8/11.
 */

public class StickerCardViewHolder extends RecyclerView.ViewHolder {
    View stickerCardView;

    TextView stickerGroupName;
    ImageView moreMenuImage;
    ImageView stickerNewImage;
    ImageView stickerRealImage;

    public StickerCardViewHolder(View itemView) {
        super(itemView);

        stickerCardView = itemView.findViewById(R.id.sticker_card_view) ;
        stickerGroupName = (TextView) itemView.findViewById(R.id.sticker_name);
        stickerRealImage = (ImageView) itemView.findViewById(R.id.sticker_image_real_view);
        moreMenuImage = (ImageView) itemView.findViewById(R.id.more_menu_image);
        stickerNewImage = (ImageView) itemView.findViewById(R.id.sticker_new_view);
    }
}
