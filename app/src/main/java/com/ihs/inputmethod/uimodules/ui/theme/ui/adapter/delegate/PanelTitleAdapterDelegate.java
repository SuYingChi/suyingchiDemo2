package com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSDrawableUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.ThemePanelModel;

import java.util.List;

/**
 * Created by wenbinduan on 2017/1/4.
 */

public final class PanelTitleAdapterDelegate extends AdapterDelegate<List<ThemePanelModel>> {

	private int spanCount;

	public PanelTitleAdapterDelegate(int spanCount) {
		this.spanCount = spanCount;
	}

	@Override
	protected boolean isForViewType(@NonNull List<ThemePanelModel> items, int position) {
		return items.get(position).isTitle;
	}

	@NonNull
	@Override
	protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
		return new PanelTitleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_panel_theme_header,parent,false));
	}

	@Override
	protected void onBindViewHolder(@NonNull List<ThemePanelModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {
		final PanelTitleViewHolder viewHolder= (PanelTitleViewHolder) holder;
		final ThemePanelModel model=items.get(position);
		viewHolder.title.setText(model.title);
		viewHolder.title.setTextColor(HSKeyboardThemeManager.getCurrentTheme().isDarkBg() ? Color.WHITE : Color.parseColor("#68747a"));

		if (model.isCustomThemeTitle) {
			viewHolder.editLayout.setVisibility(View.VISIBLE);
			HSDrawableUtils.init(viewHolder.doneButton)
					.setRadius(5)
					.setBgNormalDrawable(0x7fffffff)
					.applyNormalDrawable();
			viewHolder.editButton.setImageDrawable(HSKeyboardThemeManager.getCurrentTheme().getStyledDrawableFromResources("theme_edit_button"));

			if(model.isCustomThemeInEditMode){
				viewHolder.doneButton.setVisibility(View.VISIBLE);
				viewHolder.editButton.setVisibility(View.INVISIBLE);
			}else{
				viewHolder.doneButton.setVisibility(View.INVISIBLE);
				viewHolder.editButton.setVisibility(View.VISIBLE);
			}
			viewHolder.editLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					model.isCustomThemeInEditMode=!model.isCustomThemeInEditMode;
					if(model.isCustomThemeInEditMode){
						// HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("keyboard_customtheme_trash_clicked");
						viewHolder.doneButton.setVisibility(View.VISIBLE);
						viewHolder.editButton.setVisibility(View.INVISIBLE);
					}else{
						viewHolder.doneButton.setVisibility(View.INVISIBLE);
						viewHolder.editButton.setVisibility(View.VISIBLE);
					}
					model.customTitleOnClickListener.onClick(v);
				}
			});
		}else{
			viewHolder.editLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public int getSpanSize(List<ThemePanelModel> items, int position) {
		return spanCount;
	}
}
