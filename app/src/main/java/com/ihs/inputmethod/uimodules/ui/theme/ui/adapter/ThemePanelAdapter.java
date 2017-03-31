package com.ihs.inputmethod.uimodules.ui.theme.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegatesManager;
import com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate.PanelCreateAdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate.PanelMoreAdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate.PanelThemeAdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate.PanelTitleAdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.ThemePanelModel;

import java.util.List;

/**
 * Created by wenbinduan on 2017/1/4.
 */

public final class ThemePanelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private AdapterDelegatesManager<List<ThemePanelModel>> delegatesManager;
	private List<ThemePanelModel> items;

	public ThemePanelAdapter(int spanCount) {
		delegatesManager = new AdapterDelegatesManager<>();
		delegatesManager.addDelegate(new PanelCreateAdapterDelegate(spanCount))
				.addDelegate(new PanelTitleAdapterDelegate(spanCount))
				.addDelegate(new PanelThemeAdapterDelegate(spanCount))
				.addDelegate(new PanelMoreAdapterDelegate(spanCount));
	}

	public void setItems(List<ThemePanelModel> items) {
		this.items = items;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return delegatesManager.onCreateViewHolder(parent, viewType);
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		delegatesManager.onBindViewHolder(items, position, holder);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		return delegatesManager.getItemViewType(items, position);
	}

	@Override
	public int getItemCount() {
		return items == null ? 0 : items.size();
	}

	@Override
	public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
		delegatesManager.onViewAttachedToWindow(holder);
	}

	@Override
	public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
		delegatesManager.onViewDetachedFromWindow(holder);
	}

	@Override
	public void onViewRecycled(RecyclerView.ViewHolder holder) {
		delegatesManager.onViewRecycled(holder);
	}

	@Override
	public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
		return delegatesManager.onFailedToRecycleView(holder);
	}

	public int getSpanSize(int position) {
		return delegatesManager.getSpanSize(items, position);
	}

}
