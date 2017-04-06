package com.ihs.inputmethod.uimodules.ui.theme.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.keyboard.HSKeyboardTheme;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.analytics.ThemeAnalyticsReporter;
import com.ihs.inputmethod.uimodules.ui.theme.iap.IAPManager;
import com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.CommonThemeCardAdapter;
import com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.ThemeHomeAdapter;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.ThemeHomeModel;
import com.keyboard.core.themes.custom.KCCustomThemeManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class ThemeHomeFragment extends Fragment implements CommonThemeCardAdapter.ThemeCardItemClickListener, ThemeHomeAdapter.OnThemeAdItemClickListener {

	public final static String NOTIFICATION_THEME_HOME_DESTROY="ThemeHomeFragment.destroy";
	public final static String NOTIFICATION_THEME_HOME_STOP="ThemeHomeFragment.stop";
	public final static String NOTIFICATION_REMOVEADS_PURCHASED=HSConfig.getString("Application", "RemoveAds", "iapID");

	private RecyclerView recyclerView;
	private ThemeHomeAdapter adapter;
	private List<ThemeHomeModel> themeHomeModelList=new ArrayList<>();

	private List<ThemeHomeModel> themeList=new ArrayList<>();

	private final static int adPosition = 6;
	private boolean isThemeAnalyticsEnabled = false;
	private long currentResumeTime;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_theme_home, container, false);
		initView();
		HSGlobalNotificationCenter.addObserver(HSKeyboardThemeManager.HS_NOTIFICATION_THEME_LIST_CHANGED, notificationObserver);
		HSGlobalNotificationCenter.addObserver(ThemeHomeFragment.NOTIFICATION_REMOVEADS_PURCHASED, notificationObserver);
		return recyclerView;
	}

	private void initView() {
		isThemeAnalyticsEnabled = ThemeAnalyticsReporter.getInstance().isThemeAnalyticsEnabled();
		adapter=new ThemeHomeAdapter(getActivity(),this,this,isThemeAnalyticsEnabled);


		GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
		gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
			@Override
			public int getSpanSize(int position) {
				return adapter.getSpanSize(position);
			}
		});
		recyclerView.setLayoutManager(gridLayoutManager);

		//banner data
		ThemeHomeModel banner=new ThemeHomeModel();
		banner.isBanner=true;
		themeHomeModelList.add(banner);

		ThemeHomeModel backgroundTitle=new ThemeHomeModel();
		backgroundTitle.isTitle=true;
		backgroundTitle.titleClickable=true;
		backgroundTitle.title=getString(R.string.theme_store_background_title);
		backgroundTitle.rightButton=getString(R.string.theme_store_more);
		backgroundTitle.titleClickListener=new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("shortcut_customize_background_more_clicked");
				HSAnalytics.logEvent("shortcut_customize_background_more_clicked");
				Bundle bundle = new Bundle();
				String customEntry = "store_more";
				bundle.putString(CustomThemeActivity.BUNDLE_KEY_CUSTOMIZE_ENTRY, customEntry);
				IAPManager.getManager().startCustomThemeActivityIfSlotAvailable(getActivity(),bundle);
			}
		};
		themeHomeModelList.add(backgroundTitle);

		//background data
		ThemeHomeModel backgrounds=new ThemeHomeModel();
		backgrounds.isBackground=true;
		themeHomeModelList.add(backgrounds);

		ThemeHomeModel themeTitle=new ThemeHomeModel();
		themeTitle.isTitle=true;
		themeTitle.title=getString(R.string.theme_store_theme_title);
		themeHomeModelList.add(themeTitle);


		//card data
		updateThemeList();

		ThemeHomeModel blank=new ThemeHomeModel();
		blank.isBlankView=true;
		themeHomeModelList.add(blank);

		adapter.setItems(themeHomeModelList);
		recyclerView.setAdapter(adapter);
	}

	private void updateThemeList() {
		int startIndex=0;
		final int size=themeList.size();
		if(size>0){
			startIndex=themeHomeModelList.indexOf(themeList.get(0));
		}

		themeHomeModelList.removeAll(themeList);
		themeList.clear();

		List<HSKeyboardTheme> keyboardThemeList = new ArrayList<>();
		keyboardThemeList.addAll(HSKeyboardThemeManager.getAllKeyboardThemeList());
		keyboardThemeList.removeAll(KCCustomThemeManager.getInstance().getAllCustomThemes());
		keyboardThemeList.removeAll(HSKeyboardThemeManager.getDownloadedThemeList());
		keyboardThemeList.removeAll(HSKeyboardThemeManager.getBuiltInThemeList());

		for(HSKeyboardTheme theme :keyboardThemeList){
			ThemeHomeModel themeModel=new ThemeHomeModel();
			themeModel.keyboardTheme=theme;
			themeList.add(themeModel);
		}


		if(!IAPManager.getManager().hasPurchaseNoAds()) {
			// 获取所有远端配置广告的位置, 并按照position排序
			List<Integer> positions = new ArrayList<>();
			List<Map<String, Object>> themeAdInfos = (List<Map<String, Object>>) HSConfig.getList("Application", "NativeAds", "NativeAdPosition", "ThemeAd");
			for(Map<String, Object> item : themeAdInfos) {
				int adPosition = (int)item.get("Position");
				positions.add(adPosition);
			}
			Collections.sort(positions);
			// 插入广告对象
			for(Integer pos : positions) {
				ThemeHomeModel ad = new ThemeHomeModel();
				ad.isAd = true;
				for(Map<String, Object> item : themeAdInfos) {
					int adPosition = (int) item.get("Position");
					if(adPosition == pos) {
						ad.span = (int)item.get("Span");
						ad.adPlacement = (String) item.get("NativeAd");
					}
				}
				if (pos <= themeList.size()) {
					themeList.add(pos, ad);
				}
				else {
					themeList.add(ad);
				}
			}
		}

		themeHomeModelList.addAll(4,themeList);

		if(startIndex>0){
			int currentSize=themeList.size();
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

	private void removeAds() {
		Iterator<ThemeHomeModel> iterator = themeHomeModelList.iterator();
		while(iterator.hasNext()) {
			ThemeHomeModel themeHomeModel = iterator.next();
			if(themeHomeModel.isAd) {
				iterator.remove();
			}
		}
		adapter.notifyDataSetChanged();
	}


	@Override
	public void onCardClick(HSKeyboardTheme keyboardTheme) {
		HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("store_themes_preview_clicked", keyboardTheme.mThemeName);
		if (isThemeAnalyticsEnabled) {
			ThemeAnalyticsReporter.getInstance().recordThemeClick(keyboardTheme.mThemeName);
		}
	}

	@Override
	public void onMenuApplyClick(HSKeyboardTheme keyboardTheme) {
		HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("store_themes_apply_clicked", keyboardTheme.mThemeName);
		if (isThemeAnalyticsEnabled) {
			ThemeAnalyticsReporter.getInstance().recordThemeApply(keyboardTheme.mThemeName);
		}
	}

	@Override
	public void onMenuShareClick(HSKeyboardTheme keyboardTheme) {
		HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("store_themes_share_clicked", keyboardTheme.mThemeName);
	}

	@Override
	public void onMenuDownloadClick(HSKeyboardTheme keyboardTheme) {
		HSGoogleAnalyticsUtils.getInstance().logAppEvent("store_themes_download_clicked", keyboardTheme.mThemeName);
		if (isThemeAnalyticsEnabled) {
			ThemeAnalyticsReporter.getInstance().recordThemeDownload(keyboardTheme.mThemeName);
		}
	}

	@Override
	public void onMenuDeleteClick(HSKeyboardTheme keyboardTheme) {

	}

	@Override
	public void onMenuAppliedClick(HSKeyboardTheme keyboardTheme) {

	}

	private INotificationObserver notificationObserver = new INotificationObserver() {
		@Override
		public void onReceive(String s, HSBundle hsBundle) {
			if (HSKeyboardThemeManager.HS_NOTIFICATION_THEME_LIST_CHANGED.equals(s)) {
				updateThemeList();
			}
			else if(ThemeHomeFragment.NOTIFICATION_REMOVEADS_PURCHASED.equals(s)) {
				removeAds();
			}
 		}
	};



	public void onResume() {
		super.onResume();
		currentResumeTime = System.currentTimeMillis();
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		long time = (System.currentTimeMillis() - currentResumeTime) / 1000;
		HSGoogleAnalyticsUtils.getInstance().logAppEvent("app_mainpage_show_time", time);
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
		HSGlobalNotificationCenter.sendNotification("NOTIFICATION_THEME_HOME_STOP");
	}

	@Override
	public void onDestroy() {
		HSGlobalNotificationCenter.removeObserver(notificationObserver);
		HSGlobalNotificationCenter.sendNotificationOnMainThread(NOTIFICATION_THEME_HOME_DESTROY);
		super.onDestroy();
	}

	@Override
	public void onThemeAdClick(int position) {
		ThemeHomeModel item = themeHomeModelList.get(position);

		// Which ad's placement is ThemeAd
		if (item.isThemeAd()) {
			themeHomeModelList.remove(position);
			adapter.notifyItemRemoved(position);
		}
	}
}
