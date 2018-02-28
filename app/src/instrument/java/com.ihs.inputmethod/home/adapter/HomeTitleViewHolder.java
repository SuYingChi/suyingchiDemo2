package com.ihs.inputmethod.home.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ihs.inputmethod.uimodules.R;


/**
 * Created by wenbinduan on 2016/12/22.
 */

public final class HomeTitleViewHolder extends RecyclerView.ViewHolder {

	TextView title;
	TextView btn;

	public HomeTitleViewHolder(View itemView) {
		super(itemView);
		title= (TextView) itemView.findViewById(R.id.title_left);
		btn= (TextView) itemView.findViewById(R.id.title_btn_right);
	}
}
