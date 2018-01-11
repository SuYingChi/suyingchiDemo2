package com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ihs.inputmethod.uimodules.R;

/**
 * Created by wenbinduan on 2017/1/4.
 */

public final class PanelCreateViewHolder extends RecyclerView.ViewHolder {

	TextView tv;

	public PanelCreateViewHolder(View itemView) {
		super(itemView);
		tv = itemView.findViewById(R.id.tv);
	}
}
