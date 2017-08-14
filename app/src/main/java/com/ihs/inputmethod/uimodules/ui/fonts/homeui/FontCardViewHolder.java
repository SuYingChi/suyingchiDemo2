package com.ihs.inputmethod.uimodules.ui.fonts.homeui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ihs.inputmethod.uimodules.R;

/**
 * Created by guonan.lv on 17/8/14.
 */

public class FontCardViewHolder extends RecyclerView.ViewHolder {
    View fontCardView;

    ImageView downloadIcon;
    RadioButton radioButton;

    TextView fontCardContent;
    public FontCardViewHolder(View itemView) {
        super(itemView);

        fontCardView = itemView.findViewById(R.id.font_card_view);
        fontCardContent = (TextView) itemView.findViewById(R.id.font_content);
        downloadIcon = (ImageView) itemView.findViewById(R.id.more_menu_image);
        radioButton = (RadioButton) itemView.findViewById(R.id.font_radion_button);
    }
}
