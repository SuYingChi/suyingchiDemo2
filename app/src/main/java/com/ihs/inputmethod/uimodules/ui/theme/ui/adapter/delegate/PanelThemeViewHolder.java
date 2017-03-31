package com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.view.RoundedImageView;

/**
 * Created by wenbinduan on 2017/1/4.
 */

public final class PanelThemeViewHolder extends RecyclerView.ViewHolder {

	RoundedImageView content;
	ImageView check;
	ImageView delete;

	public PanelThemeViewHolder(View itemView) {
		super(itemView);
		content=(RoundedImageView) itemView.findViewById(R.id.theme_preview);
		check=(ImageView) itemView.findViewById(R.id.theme_check);
		delete=(ImageView) itemView.findViewById(R.id.theme_delete);
	}
}
