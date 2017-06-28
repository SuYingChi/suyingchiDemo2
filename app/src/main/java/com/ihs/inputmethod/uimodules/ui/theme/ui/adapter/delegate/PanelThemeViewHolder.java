package com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.ihs.inputmethod.uimodules.R;

/**
 * Created by wenbinduan on 2017/1/4.
 */

public final class PanelThemeViewHolder extends RecyclerView.ViewHolder {

	View contentContainer;
	ImageView content;
	ImageView check;
	ImageView delete;

	public PanelThemeViewHolder(View itemView) {
		super(itemView);
		contentContainer=itemView.findViewById(R.id.theme_preview_container);
		content=(ImageView) itemView.findViewById(R.id.theme_preview);
		check=(ImageView) itemView.findViewById(R.id.theme_check);
		delete=(ImageView) itemView.findViewById(R.id.theme_delete);
	}
}
