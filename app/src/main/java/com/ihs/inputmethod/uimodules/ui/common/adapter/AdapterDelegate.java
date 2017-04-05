package com.ihs.inputmethod.uimodules.ui.common.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * Created by wenbinduan on 2016/12/21.
 */

public abstract class AdapterDelegate<T> {


	protected abstract boolean isForViewType(@NonNull T items, int position);

	@NonNull
	abstract protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent);


	protected abstract void onBindViewHolder(@NonNull T items, int position, @NonNull RecyclerView.ViewHolder holder);


	protected void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
	}

	protected boolean onFailedToRecycleView(@NonNull RecyclerView.ViewHolder holder) {
		return false;
	}

	protected void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
	}

	protected void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
	}

	public int getSpanSize(T items, int position) {
		return 1;
	}
}
