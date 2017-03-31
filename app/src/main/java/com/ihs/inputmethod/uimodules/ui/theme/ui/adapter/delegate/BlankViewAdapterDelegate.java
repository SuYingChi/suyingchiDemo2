package com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.ThemeHomeModel;

import java.util.List;

/**
 * Created by wenbinduan on 2016/12/23.
 */

public final class BlankViewAdapterDelegate extends AdapterDelegate<List<ThemeHomeModel>> {

	private int blankHeight;

	public BlankViewAdapterDelegate(int blankHeight) {
		this.blankHeight = blankHeight;
	}

	@Override
	protected boolean isForViewType(@NonNull List<ThemeHomeModel> items, int position) {
		return items.get(position).isBlankView;
	}

	@NonNull
	@Override
	protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
		View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blank_view,parent,false);
		ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
		layoutParams.height = blankHeight;
		view.setLayoutParams(layoutParams);
		return new BlankViewHolder(view);
	}

	@Override
	protected void onBindViewHolder(@NonNull List<ThemeHomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {

	}

	@Override
	public int getSpanSize(List<ThemeHomeModel> items, int position) {
		return 2;
	}
}
