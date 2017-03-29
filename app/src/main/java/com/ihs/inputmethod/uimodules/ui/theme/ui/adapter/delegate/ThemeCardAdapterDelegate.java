package com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.keyboard.HSKeyboardTheme;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.theme.HSThemeNewTipController;
import com.ihs.inputmethod.api.utils.HSImageLoader;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.analytics.ThemeAnalyticsReporter;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.ThemeHomeModel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.util.List;


/**
 * Created by wenbinduan on 2016/12/22.
 */

public final class ThemeCardAdapterDelegate extends AdapterDelegate<List<ThemeHomeModel>>{

	public final static int TAG_DELETE =0;
	public final static int TAG_MENU   =1;
	public final static int TAG_CARD   =2;

	private boolean themeAnalyticsEnabled;
	private View.OnClickListener cardViewOnClickListener;

	private DisplayImageOptions options=new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).build();

	public ThemeCardAdapterDelegate(boolean themeAnalyticsEnabled, View.OnClickListener cardViewOnClickListener) {
		this.themeAnalyticsEnabled = themeAnalyticsEnabled;
		this.cardViewOnClickListener = cardViewOnClickListener;
	}

	@Override
	protected boolean isForViewType(@NonNull List<ThemeHomeModel> items, int position) {
		return items.get(position).keyboardTheme!=null;
	}

	@NonNull
	@Override
	protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
		return new ThemeCardViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_theme_card, parent, false));
	}

	@Override
	protected void onBindViewHolder(@NonNull List<ThemeHomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {
		ThemeCardViewHolder themeCardViewHolder = (ThemeCardViewHolder) holder;

		themeCardViewHolder.themeRealImage.setImageDrawable(null);
		final HSKeyboardTheme keyboardTheme = items.get(position).keyboardTheme;
		holder.itemView.setTag(keyboardTheme.mThemeName);

		themeCardViewHolder.themeDelete.setVisibility(View.GONE);
		
		themeCardViewHolder.themeNewImage.setVisibility(keyboardTheme.isNewTheme()&& HSThemeNewTipController.getInstance().isThemeNew(keyboardTheme.mThemeName) ? View.VISIBLE : View.GONE);
		switch (keyboardTheme.getThemeType()) {
			case CUSTOM:
				themeCardViewHolder.themeName.setText(HSApplication.getContext().getString(R.string.theme_card_custom_theme_default_name));
				themeCardViewHolder.themeRealImage.setImageDrawable(HSKeyboardThemeManager.getThemePreviewDrawable(keyboardTheme.mThemeName));
				themeCardViewHolder.themeDelete.setVisibility(items.get(position).deleteEnable ? View.VISIBLE : View.GONE);
				break;
			case BUILD_IN:
				themeCardViewHolder.themeRealImage.setImageDrawable(HSKeyboardThemeManager.getThemePreviewDrawable(keyboardTheme.mThemeName));
				themeCardViewHolder.themeName.setText(keyboardTheme.getThemeShowName());
				break;
			case DOWNLOADED:
			case NEED_DOWNLOAD:
				final String smallPreviewImgUrl = keyboardTheme.getSmallPreivewImgUrl();
				if (smallPreviewImgUrl != null) {
					HSImageLoader.getInstance().displayImage(smallPreviewImgUrl, themeCardViewHolder.themeRealImage, options);
				}
				themeCardViewHolder.themeName.setText(keyboardTheme.getThemeShowName());
				break;
		}

		ThemeHomeModel model=items.get(position);

		themeCardViewHolder.themeDelete.setTag(model);
		themeCardViewHolder.themeDelete.setTag(R.id.theme_card_view_tag_key_action,TAG_DELETE);
		themeCardViewHolder.themeDelete.setTag(R.id.theme_card_view_tag_key_position,position);
		themeCardViewHolder.themeDelete.setOnClickListener(cardViewOnClickListener);

		themeCardViewHolder.moreMenuImage.setTag(model);
		themeCardViewHolder.moreMenuImage.setTag(R.id.theme_card_view_tag_key_action,TAG_MENU);
		themeCardViewHolder.moreMenuImage.setTag(R.id.theme_card_view_tag_key_position,position);
		themeCardViewHolder.moreMenuImage.setOnClickListener(cardViewOnClickListener);

		themeCardViewHolder.themeCardView.setTag(model);
		themeCardViewHolder.themeCardView.setTag(R.id.theme_card_view_tag_key_action,TAG_CARD);
		themeCardViewHolder.themeCardView.setTag(R.id.theme_card_view_tag_key_position,position);
		themeCardViewHolder.themeCardView.setOnClickListener(cardViewOnClickListener);
	}



	@Override
	protected void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
		super.onViewAttachedToWindow(holder);
		if(themeAnalyticsEnabled){
			final String theme=holder.itemView.getTag().toString();
			ThemeAnalyticsReporter.getInstance().recordThemeShown(theme);
		}
	}


}
