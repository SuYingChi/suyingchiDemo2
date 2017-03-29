package com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.ThemeHomeModel;

import java.util.List;

/**
 * Created by wenbinduan on 2016/12/22.
 */

public final class ThemeTitleAdapterDelegate extends AdapterDelegate<List<ThemeHomeModel>> {

	@Override
	protected boolean isForViewType(@NonNull List<ThemeHomeModel> items, int position) {
		return items.get(position).isTitle;
	}

	@NonNull
	@Override
	protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
		return new ThemeTitleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_theme_title, parent, false));
	}

	@Override
	protected void onBindViewHolder(@NonNull List<ThemeHomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {
		ThemeHomeModel model=items.get(position);
		ThemeTitleViewHolder viewHolder= (ThemeTitleViewHolder) holder;
		viewHolder.title.setText(model.title);

		if(model.rightButton!=null){
			viewHolder.btn.setText(model.rightButton);
		}else {
			viewHolder.btn.setText("");
		}

		if(model.titleClickListener!=null){
			holder.itemView.setOnClickListener(model.titleClickListener);
		}else {
			holder.itemView.setOnClickListener(null);
		}

		holder.itemView.setClickable(model.titleClickable);
	}

	@Override
	public int getSpanSize(List<ThemeHomeModel> items, int position) {
		return 2;
	}
}
