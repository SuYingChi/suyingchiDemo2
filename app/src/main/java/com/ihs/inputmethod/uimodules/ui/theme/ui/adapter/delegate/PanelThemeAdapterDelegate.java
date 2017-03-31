package com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSToastUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.analytics.ThemeAnalyticsReporter;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.ThemePanelModel;
import com.ihs.inputmethod.uimodules.ui.theme.ui.view.RoundedImageView;
import com.keyboard.core.themes.custom.KCCustomThemeManager;
import com.makeramen.roundedimageview.RoundedDrawable;

import java.util.List;

/**
 * Created by wenbinduan on 2017/1/4.
 */

public final class PanelThemeAdapterDelegate extends AdapterDelegate<List<ThemePanelModel>> {

	private int spanCount;

	public PanelThemeAdapterDelegate(int spanCount) {
		this.spanCount = spanCount;
	}

	@Override
	protected boolean isForViewType(@NonNull List<ThemePanelModel> items, int position) {
		String themeName = items.get(position).themeName;
		return themeName != null && themeName.trim().length() > 0;
	}

	@NonNull
	@Override
	protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
		return new PanelThemeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_panel_theme, parent, false));
	}

	@Override
	protected void onBindViewHolder(@NonNull List<ThemePanelModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {
		final PanelThemeViewHolder viewHolder = (PanelThemeViewHolder) holder;
		final ThemePanelModel model = items.get(position);

		Drawable previewDrawable = HSKeyboardThemeManager.getThemePreviewPanelDrawable(model.themeName);
		viewHolder.content.setImageDrawable(getPreviewStateListDrawable(viewHolder.content,previewDrawable));
		viewHolder.check.setVisibility(HSKeyboardThemeManager.getCurrentThemeName().equals(model.themeName) ? View.VISIBLE : View.GONE);

		viewHolder.delete.setVisibility(View.GONE);

		if (model.isCustomTheme&&model.isCustomThemeInEditMode) {
			viewHolder.delete.setVisibility(HSKeyboardThemeManager.getCurrentThemeName().equals(model.themeName) ? View.GONE : View.VISIBLE);
		}

		RecyclerView.LayoutParams lp= (RecyclerView.LayoutParams) viewHolder.itemView.getLayoutParams();

		float width1 = 280, width2 = 260, height1 = 150, height2 = 130;
		DisplayMetrics displayMetrics = viewHolder.itemView.getContext().getResources().getDisplayMetrics();
		final int viewWidth = displayMetrics.widthPixels / spanCount;

		lp.width=viewWidth;
		lp.height= (int) (viewWidth * height1 / width1);
		viewHolder.itemView.setLayoutParams(lp);

		viewHolder.check.measure(-1, -1);
		ViewGroup.LayoutParams layoutParams = viewHolder.check.getLayoutParams();
		layoutParams.width = viewWidth;
		layoutParams.height = (int) (viewWidth * height1 / width1);
		viewHolder.check.setLayoutParams(layoutParams);

		ViewGroup.LayoutParams params = viewHolder.content.getLayoutParams();
		params.width = (int) (layoutParams.width * width2 / width1);
		params.height = (int) (layoutParams.height * height2 / height1);
		viewHolder.content.setLayoutParams(params);

		viewHolder.content.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (model.isCustomTheme && model.isCustomThemeInEditMode) {
					return;
				}

				if (!HSKeyboardThemeManager.setKeyboardTheme(model.themeName)) {
					String failedString = HSApplication.getContext().getResources().getString(R.string.theme_apply_failed);
					HSToastUtils.toastCenterLong(String.format(failedString, model.themeShowName));
				}

				HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("keyboard_theme_chosed", HSKeyboardThemeManager.isCustomTheme(model.themeName) ? "mytheme": model.themeName);
				if (ThemeAnalyticsReporter.getInstance().isThemeAnalyticsEnabled()) {
					ThemeAnalyticsReporter.getInstance().recordThemeUsage(model.themeName);
				}
			}
		});

		if (model.isCustomTheme) {
			viewHolder.delete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
//					HSKeyboardThemeManager.removeTheme(model.themeName);
					KCCustomThemeManager.getInstance().removeCustomTheme(model.themeName);
					HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("keyboard_customtheme_delete_clicked");
				}
			});
		}
	}

	private Drawable getPreviewStateListDrawable(RoundedImageView content, Drawable drawable) {
		// Make rounded default drawable
		Drawable defaultDrawable = RoundedDrawable.fromDrawable(drawable);
		content.updateAttrs(defaultDrawable);

		// Make pressed drawable
		Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
		Bitmap pressedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(pressedBitmap);
		Paint paint = new Paint();
		paint.setColorFilter(new PorterDuffColorFilter(Color.parseColor("#ff666666"), PorterDuff.Mode.MULTIPLY));
		canvas.drawBitmap(bitmap, 0, 0, paint);
		Drawable pressedDrawable = new BitmapDrawable(pressedBitmap);

		// Make rounded pressed drawable
		pressedDrawable = RoundedDrawable.fromDrawable(pressedDrawable);
		content.updateAttrs(pressedDrawable);

		// Make state list drawable
		StateListDrawable stateListDrawable = new StateListDrawable();
		stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
		stateListDrawable.addState(new int[]{android.R.attr.state_focused}, pressedDrawable);
		stateListDrawable.addState(new int[]{android.R.attr.state_selected}, pressedDrawable);
		stateListDrawable.addState(new int[]{}, defaultDrawable);

		return stateListDrawable;
	}
}
