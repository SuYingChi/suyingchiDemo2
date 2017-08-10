package com.ihs.inputmethod.uimodules.ui.theme.ui;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.keyboard.HSKeyboardTheme;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.CommonThemeCardAdapter;
import com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.MyThemeAdapter;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.ThemeHomeModel;
import com.keyboard.core.themes.custom.KCCustomThemeManager;

import java.util.ArrayList;
import java.util.List;


public class MyThemeFragment extends Fragment implements CommonThemeCardAdapter.ThemeCardItemClickListener, View.OnClickListener {

	private RecyclerView recyclerView;
	private MyThemeAdapter adapter;
	private List<ThemeHomeModel> themes=new ArrayList<>();
	private List<ThemeHomeModel> downloaded=new ArrayList<>();
	private List<ThemeHomeModel> custom=new ArrayList<>();


	private ThemeHomeModel customTitle=new ThemeHomeModel();

	private boolean showCustomTheme = false;
	private boolean isDeleteEnable = false;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_my_theme, container, false);
		recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
		loadData();
		HSGlobalNotificationCenter.addObserver(HSKeyboardThemeManager.HS_NOTIFICATION_THEME_LIST_CHANGED, notificationObserver);
		return view;
	}

	private void loadData() {

		adapter=new MyThemeAdapter(getActivity(),this);
		GridLayoutManager layoutManager=new GridLayoutManager(getActivity(), 2);
		layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
			@Override
			public int getSpanSize(int position) {
				return adapter.getSpanSize(position);
			}
		});
		recyclerView.setLayoutManager(layoutManager);

		customTitle.isCustomizedTitle=true;
		customTitle.deleteButtonVisible=false;
		customTitle.customizedTitle=getString(R.string.my_theme_customized_theme_title);
		customTitle.customizedTitleClickListener=this;

		updateCustomThemes();

		ThemeHomeModel title=new ThemeHomeModel();
		title.isTitle=true;
		title.titleClickable=true;
		title.title=getString(R.string.my_theme_downloaded_theme_title);
		themes.add(title);

		updateDownloadedThemes();

		ThemeHomeModel blank=new ThemeHomeModel();
		blank.isBlankView=true;
		themes.add(blank);

		adapter.setItems(themes);
		recyclerView.setAdapter(adapter);
	}

	private void updateCustomThemes() {
		if (KCCustomThemeManager.getInstance().getAllCustomThemes().size() > 0) {
			if(!showCustomTheme){
				themes.add(0,customTitle);
				if(themes.size()>1){
					adapter.notifyItemInserted(0);
				}
			}
			showCustomTheme = true;

			List<HSKeyboardTheme> customThemes=KCCustomThemeManager.getInstance().getAllCustomThemes();
			updateThemes(custom,customThemes,1,true);
		} else {
			if (showCustomTheme) {
				showCustomTheme = false;
				isDeleteEnable = false;

				themes.remove(customTitle);
				adapter.notifyItemRemoved(0);

				if(custom.size()>0){
					themes.removeAll(custom);
					adapter.notifyItemRangeRemoved(0,custom.size());
					custom.clear();
				}
			}

		}
	}

	private void updateDownloadedThemes() {

		ArrayList<HSKeyboardTheme> downloadedKeyboardThemes = new ArrayList<>();
		downloadedKeyboardThemes.addAll(HSKeyboardThemeManager.getBuiltInThemeList());
		downloadedKeyboardThemes.addAll(HSKeyboardThemeManager.getDownloadedThemeList());

		if(custom.size()>0){
			updateThemes(downloaded,downloadedKeyboardThemes,custom.size()+2, false);
		}else{
			updateThemes(downloaded,downloadedKeyboardThemes,custom.size()+1, false);
		}
	}

	private void updateThemes(List<ThemeHomeModel> origin, List<HSKeyboardTheme> current, int location, boolean isCustom/** is custom theme */) {
		int startIndex=-1;
		final int size=origin.size();
		if(size>0){
			startIndex=themes.indexOf(origin.get(0));
		}

		themes.removeAll(origin);
		origin.clear();

		for(HSKeyboardTheme theme :current){
			ThemeHomeModel themeModel=new ThemeHomeModel();
			themeModel.keyboardTheme=theme;
			if(isCustom){
				themeModel.deleteEnable = isDeleteEnable;
			}
			origin.add(themeModel);
		}

		themes.addAll(location,origin);

		if(startIndex>0){
			int currentSize=current.size();
			if(size>currentSize){
				adapter.notifyItemRangeRemoved(startIndex+currentSize,size-currentSize);
			}else if(size<currentSize){
				adapter.notifyItemRangeInserted(startIndex+size,currentSize-size);
			}

			if(Math.min(currentSize,size)>0){
				adapter.notifyItemRangeChanged(startIndex,Math.min(currentSize,size));
			}
		}
	}

	private INotificationObserver notificationObserver = new INotificationObserver() {
		@Override
		public void onReceive(String s, HSBundle hsBundle) {
			if (HSKeyboardThemeManager.HS_NOTIFICATION_THEME_LIST_CHANGED.equals(s)) {
				updateCustomThemes();
				updateDownloadedThemes();
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		setEditEnable(false);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			setEditEnable(false);
		}
	}


	@Override
	public void onDestroy() {
		HSGlobalNotificationCenter.removeObserver(notificationObserver);
		super.onDestroy();
	}

	@Override
	public void onCardClick(HSKeyboardTheme keyboardTheme) {
		HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("mythemes_preview_clicked",
				keyboardTheme.getThemeType() == HSKeyboardTheme.ThemeType.CUSTOM ? getString(R.string.theme_card_custom_theme_default_name) : keyboardTheme.mThemeName);
	}

	@Override
	public void onMenuApplyClick(HSKeyboardTheme keyboardTheme) {
		HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("mythemes_apply_clicked", keyboardTheme.mThemeName);
	}

	@Override
	public void onMenuShareClick(HSKeyboardTheme keyboardTheme) {
		HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("mythemes_share_clicked", keyboardTheme.mThemeName);
	}

	@Override
	public void onMenuDownloadClick(HSKeyboardTheme keyboardTheme) {

	}

	@Override
	public void onMenuDeleteClick(HSKeyboardTheme keyboardTheme) {

	}

	@Override
	public void onMenuAppliedClick(HSKeyboardTheme keyboardTheme) {

	}

	@Override
	public void onClick(View v) {
		setEditEnable(!isDeleteEnable);
	}

	private void setEditEnable(boolean editEnable) {
		boolean editableChanged = editEnable!=isDeleteEnable;
		isDeleteEnable = editEnable;
		customTitle.deleteButtonVisible= !editEnable;
		if(editableChanged&&custom.size()>0){
			for(ThemeHomeModel model:custom){
				model.deleteEnable=editEnable;
			}
			adapter.notifyItemRangeChanged(0,1+custom.size());
		}
	}

}
