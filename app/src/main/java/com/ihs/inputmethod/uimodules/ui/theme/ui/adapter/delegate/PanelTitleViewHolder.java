package com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.inputmethod.uimodules.R;

/**
 * Created by wenbinduan on 2017/1/4.
 */

public final class PanelTitleViewHolder extends RecyclerView.ViewHolder {
	ImageView editButton;
	View doneButton;
	TextView title;
	View editLayout;

	public PanelTitleViewHolder(View itemView) {
		super(itemView);
		title= itemView.findViewById(R.id.theme_title);
		doneButton=itemView.findViewById(R.id.done_button);
		editButton= itemView.findViewById(R.id.edit_button);
		editLayout=itemView.findViewById(R.id.edit_layout);
	}
}
