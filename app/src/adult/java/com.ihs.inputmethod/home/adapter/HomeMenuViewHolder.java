package com.ihs.inputmethod.home.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.inputmethod.uimodules.R;


/**
 * Created by wenbinduan on 2016/12/22.
 */

public final class HomeMenuViewHolder extends RecyclerView.ViewHolder {

    TextView menuTitle;
    ImageView menuBg;
    ImageView menuIcon;

    public HomeMenuViewHolder(View itemView) {
        super(itemView);
        menuTitle = itemView.findViewById(R.id.menu_title);
        menuBg = itemView.findViewById(R.id.menu_bg);
        menuIcon = itemView.findViewById(R.id.menu_icon);
    }
}
