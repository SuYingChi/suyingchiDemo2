package com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSDrawableUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.iap.IAPManager;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeActivity;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.ThemePanelModel;

import java.util.List;

/**
 * Created by wenbinduan on 2017/1/4.
 */

public final class PanelCreateAdapterDelegate extends AdapterDelegate<List<ThemePanelModel>> {

	private int spanCount;

	public PanelCreateAdapterDelegate(int spanCount) {
		this.spanCount = spanCount;
	}

	@Override
	protected boolean isForViewType(@NonNull List<ThemePanelModel> items, int position) {
		return items.get(position).isCreateButton;
	}

	@NonNull
	@Override
	protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
		return new PanelCreateViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_panel_theme_create, parent, false));
	}

	@Override
	protected void onBindViewHolder(@NonNull List<ThemePanelModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {
		final PanelCreateViewHolder viewHolder = (PanelCreateViewHolder) holder;
		final ThemePanelModel model = items.get(position);

		int paddingTop = viewHolder.tv.getPaddingTop();

		if(paddingTop==0){
			float width1 = 280f, width2 = 260, height1 = 150, height2 = 130f;
			DisplayMetrics displayMetrics = viewHolder.itemView.getContext().getResources().getDisplayMetrics();
			final int viewWidth = displayMetrics.widthPixels / spanCount;
			RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) viewHolder.itemView.getLayoutParams();
			layoutParams.width = viewWidth;
			layoutParams.height = (int) (viewWidth * height1 / width1);
			viewHolder.itemView.setLayoutParams(layoutParams);

			viewHolder.tv.measure(0, 0);
			int initHeight = viewHolder.tv.getMeasuredHeight();

			final int newHeight = (int) (layoutParams.height * height2 / height1);
			ViewGroup.LayoutParams params = viewHolder.tv.getLayoutParams();
			params.width = (int) (layoutParams.width * width2 / width1);
			params.height = newHeight;
			viewHolder.tv.setLayoutParams(params);

			paddingTop = (newHeight - initHeight) / 2;
		}

		Drawable backgroundDrawable;
		Drawable drawable;
		if (model.isCustomThemeInEditMode) {
			backgroundDrawable = HSKeyboardThemeManager.getCurrentTheme().getStyledDrawableFromResources("theme_create_disable_bg");
			drawable = HSKeyboardThemeManager.getCurrentTheme().getStyledDrawableFromResources("theme_more_icon_disable");
			viewHolder.tv.setTextColor(Color.argb(127, 255, 255, 255));
		} else {
			Drawable normalStatusDrawable = HSKeyboardThemeManager.getCurrentTheme().getStyledDrawableFromResources("theme_create_plus");
			Drawable pressedStatusDrawable = HSKeyboardThemeManager.getCurrentTheme().getStyledDrawableFromResources("theme_create_plus_pressed");
			drawable = HSDrawableUtils.getDimmedDrawable(normalStatusDrawable, pressedStatusDrawable);
			backgroundDrawable = HSDrawableUtils.getDimmedDrawable(HSKeyboardThemeManager.getCurrentTheme().getStyledDrawableFromResources("theme_create_bg"),
					HSKeyboardThemeManager.getCurrentTheme().getStyledDrawableFromResources("theme_create_bg_pressed"));
			boolean isDarkBg = HSKeyboardThemeManager.getCurrentTheme().isDarkBg();
			int normalColor = isDarkBg ? Color.WHITE : Color.parseColor("#68747a");
			int pressedColor = isDarkBg ? Color.argb(127, 255, 255, 255) : Color.parseColor("#8f999d");
			viewHolder.tv.setTextColor(HSDrawableUtils.createColorStateList(normalColor, pressedColor));
		}
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		viewHolder.tv.setCompoundDrawables(null, drawable, null, null);
		viewHolder.tv.setBackgroundDrawable(backgroundDrawable);
		viewHolder.tv.setPadding(0, paddingTop, 0, 0);

		viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (model.isCustomThemeInEditMode) {
					return;
				}
				if (model.isAddButtonClickToThemeHome) {
					startThemeHomeActivity();
				} else {
					Bundle bundle = new Bundle();
					String customEntry = "keyboard_create";
					bundle.putString(CustomThemeActivity.BUNDLE_KEY_CUSTOMIZE_ENTRY, customEntry);
					IAPManager.getManager().startCustomThemeActivityIfSlotAvailable(null, bundle);
				}
				HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("keyboard_customtheme_create_clicked");
			}
		});
	}

	private void startThemeHomeActivity() {
		HSInputMethod.hideWindow();
		final Context context = HSApplication.getContext();
		final Intent intent = new Intent(context, ThemeHomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}
}
