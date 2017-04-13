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

public final class CustomizedTitleAdapterDelegate extends AdapterDelegate<List<ThemeHomeModel>> {


	public CustomizedTitleAdapterDelegate() {
	}

	@Override
	protected boolean isForViewType(@NonNull List<ThemeHomeModel> items, int position) {
		return items.get(position).isCustomizedTitle;
	}

	@NonNull
	@Override
	protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
		return new CustomizedTitleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_theme_customized_title,parent,false));
	}

	@Override
	protected void onBindViewHolder(@NonNull List<ThemeHomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {
		final CustomizedTitleViewHolder viewHolder= (CustomizedTitleViewHolder) holder;
		final ThemeHomeModel model=items.get(position);

		viewHolder.title.setText(model.customizedTitle);
	}

	@Override
	public int getSpanSize(List<ThemeHomeModel> items, int position) {
		return 2;
	}
}
