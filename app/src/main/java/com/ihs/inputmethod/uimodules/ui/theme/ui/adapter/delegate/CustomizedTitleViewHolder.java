package com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ihs.inputmethod.uimodules.R;

/**
 * Created by wenbinduan on 2016/12/23.
 */

public final class CustomizedTitleViewHolder extends RecyclerView.ViewHolder {

	TextView title;

	public CustomizedTitleViewHolder(View itemView) {
		super(itemView);
		title= itemView.findViewById(R.id.customized_title);
	}
}
