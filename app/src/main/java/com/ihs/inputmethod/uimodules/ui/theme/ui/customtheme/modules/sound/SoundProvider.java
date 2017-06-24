package com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.modules.sound;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base.BaseThemeItemProvider;
import com.keyboard.core.themes.custom.KCElementResourseHelper;
import com.keyboard.core.themes.custom.elements.KCBaseElement;
import com.keyboard.core.themes.custom.elements.KCSoundElement;

/**
 * Created by wenbinduan on 2016/12/12.
 */

public final class SoundProvider extends BaseThemeItemProvider<KCSoundElement, BaseThemeItemProvider.BaseItemHolder, SoundFragment> {

	public SoundProvider(SoundFragment fragment) {
		super(fragment);
	}

	@Override
	protected boolean isCustomThemeItemSelected(KCBaseElement item) {
		return item instanceof KCSoundElement && fragment.getCustomThemeData().getSoundElement() != null &&
				fragment.getCustomThemeData().getSoundElement().getName().equals(item.getName());
	}

	@NonNull
	@Override
	protected BaseItemHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
		BaseItemHolder v= super.onCreateViewHolder(inflater, parent);
//		v.mProgressView.setDrawCircle(true);
		return v;
	}

	@Override
	protected void onBindViewHolder(@NonNull BaseItemHolder holder, @NonNull Object item) {
		super.onBindViewHolder(holder, item);
		DisplayMetrics displayMetrics = holder.itemView.getResources().getDisplayMetrics();
		int height = Math.min(displayMetrics.widthPixels,displayMetrics.heightPixels) / fragment.SPAN_COUNT  -  holder.itemView.getResources().getDimensionPixelSize(R.dimen.custom_theme_item_margin) *2;
		int width = height;
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
		layoutParams.gravity = Gravity.CENTER;
		holder.itemView.setLayoutParams(layoutParams);
	}

	@Override
	protected Drawable getChosedBackgroundDrawable() {
		return KCElementResourseHelper.getSoundChosedBackgroundDrawable();
	}

	@Override
	protected Drawable getLockedDrawable() {
		return KCElementResourseHelper.getSoundLockedDrawable();
	}

	@Override
	protected Drawable getNewMarkDrawable() {
		return KCElementResourseHelper.getSoundNewMarkDrawable();
	}

	@Override
	protected Drawable getBackgroundDrawable(Object item) {
		return KCElementResourseHelper.getSoundBackgroundDrawable(((KCSoundElement)item).getBackgroundColor());
	}
}
