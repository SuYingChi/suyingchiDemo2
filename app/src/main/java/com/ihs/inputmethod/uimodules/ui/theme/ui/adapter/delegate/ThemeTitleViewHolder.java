package com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ihs.inputmethod.uimodules.R;


/**
 * Created by wenbinduan on 2016/12/22.
 */

public final class ThemeTitleViewHolder extends RecyclerView.ViewHolder {

	TextView title;
	TextView btn;

	public ThemeTitleViewHolder(View itemView) {
		super(itemView);
		title= itemView.findViewById(R.id.title_left);
		btn= itemView.findViewById(R.id.title_btn_right);
	}
}
