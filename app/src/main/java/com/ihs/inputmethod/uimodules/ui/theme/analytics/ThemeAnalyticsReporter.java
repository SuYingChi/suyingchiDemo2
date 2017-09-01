package com.ihs.inputmethod.uimodules.ui.theme.analytics;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSJsonUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by wenbinduan on 2016/12/5.
 */

public final class ThemeAnalyticsReporter {

	private static ThemeAnalyticsReporter reporter;

	private Set<String> reportedTheme=new HashSet<>();
	private Set<String> shownTheme=new HashSet<>();
	private Set<String> bannerShownTheme=new HashSet<>();
	private Set<String> detailReported=new HashSet<>();
	private Set<String> detailShown=new HashSet<>();
	private String currentTheme;
	private long currentThemeStartTime;
	private final INotificationObserver sessionObserver=new INotificationObserver() {
		@Override
		public void onReceive(String s, HSBundle hsBundle) {
			if(s.equals(HSNotificationConstant.HS_SESSION_END)){
				recordThemeAnalyticsOnSessionEnd();
			}
		}
	};

	private ThemeAnalyticsReporter(){
		SharedPreferences pre= PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext());
		currentTheme=pre.getString("ThemeAnalyticsReporter.PRE_KEY_THEME_ANALYTICS_CURRENT_THEME","Classic");
		currentThemeStartTime=pre.getLong("ThemeAnalyticsReporter.PRE_KEY_THEME_ANALYTICS_CURRENT_THEME_START",System.currentTimeMillis());

		if(pre.getBoolean("ThemeAnalyticsReporter.PRE_KEY_THEME_ANALYTICS_ENABLED",false)){
			HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_END,sessionObserver);
		}
	}

	public static ThemeAnalyticsReporter getInstance(){
		if(reporter==null){
			synchronized (ThemeAnalyticsReporter.class){
				if(reporter==null){
					reporter=new ThemeAnalyticsReporter();
					reporter.loadReportedTheme();
				}
			}
		}
		return reporter;
	}

	private void loadReportedTheme() {
		SharedPreferences pre= PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext());
		final String themesStr=pre.getString("ThemeAnalyticsReporter.PRE_KEY_THEME_ANALYTICS_REPORTED_THEMES","");
		final List<Object> themes = HSJsonUtils.jsonStrToList(themesStr);
		for(final Object theme:themes){
			reportedTheme.add(theme.toString().trim());
		}

		final String detailStr=pre.getString("ThemeAnalyticsReporter.PRE_KEY_THEME_ANALYTICS_DETAIL_THEMES","");
		final List<Object> details = HSJsonUtils.jsonStrToList(detailStr);
		for(final Object theme:details){
			detailReported.add(theme.toString().trim());
		}

	}

	private void saveReportedTheme(){
		SharedPreferences pre= PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext());
		List<Object> themes=new ArrayList<>();
		for(String theme:reportedTheme){
			themes.add(theme);
		}
		final String jsonStr = HSJsonUtils.listToJsonStr(themes);
		pre.edit().putString("ThemeAnalyticsReporter.PRE_KEY_THEME_ANALYTICS_REPORTED_THEMES",jsonStr).apply();

		List<Object> details=new ArrayList<>();
		for(String theme:detailReported){
			details.add(theme);
		}
		final String detailStr = HSJsonUtils.listToJsonStr(details);
		pre.edit().putString("ThemeAnalyticsReporter.PRE_KEY_THEME_ANALYTICS_DETAIL_THEMES",detailStr).apply();
	}

	public void enableThemeAnalytics(final String currentTheme){
		HSLog.d("new user. theme analytics enabled.");
		SharedPreferences pre= PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext());
		pre.edit().putBoolean("ThemeAnalyticsReporter.PRE_KEY_THEME_ANALYTICS_ENABLED",true).apply();

		this.currentTheme=currentTheme;
		currentThemeStartTime=System.currentTimeMillis();
		saveCurrentThemeUsageInfo();
		HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_END,sessionObserver);
	}

	public boolean isThemeAnalyticsEnabled(){
		SharedPreferences pre= PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext());
		return pre.getBoolean("ThemeAnalyticsReporter.PRE_KEY_THEME_ANALYTICS_ENABLED",false);
	}

	public boolean isThemeReported(final String theme) {
		return reportedTheme.contains(theme.trim())||shownTheme.contains(theme.trim())||bannerShownTheme.contains(theme.trim());
	}

	public void recordThemeShown(final String theme) {
		HSLog.d(theme+" shown in card");
		shownTheme.add(theme.trim());
	}

	public void recordBannerThemeShown(String banner) {
		HSLog.d(banner+" shown in banner");
		bannerShownTheme.add(banner.trim());
	}

	public void recordThemeClick(String theme) {
		HSLog.d(theme);
		theme=theme.trim();
		if(!reportedTheme.contains(theme)){
			reportedTheme.add(theme);
			HSLog.d(theme+"_true");
		}
	}

	public void recordThemeApply(String theme) {
		recordThemeApplyInDetailActivity(theme);
	}

	public void recordThemeDownload(String theme) {
		recordThemeDownloadInDetailActivity(theme);
	}

	public void recordThemeUsage(String theme) {
		HSLog.d("applied theme "+theme);
		long currentTime=System.currentTimeMillis();
		String timeLabel=getTimeLabel(currentTime-currentThemeStartTime);
		HSLog.d("last theme "+currentTheme+" :"+timeLabel);
		currentTheme=theme;
		currentThemeStartTime=currentTime;

		saveCurrentThemeUsageInfo();
	}

	private void saveCurrentThemeUsageInfo() {
		SharedPreferences pre= PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext());
		pre.edit().putString("ThemeAnalyticsReporter.PRE_KEY_THEME_ANALYTICS_CURRENT_THEME",currentTheme).apply();
		pre.edit().putLong("ThemeAnalyticsReporter.PRE_KEY_THEME_ANALYTICS_CURRENT_THEME_START",currentThemeStartTime).apply();
	}

	private String getTimeLabel(long time) {
		if(time<=1000*60*60L){
			return "0-1h";
		}else if(time<=1000*60*60*24L){
			return "1h-1d";
		}else if(time<=1000*60*60*24*3L){
			return "1d-3d";
		}else if(time<=1000*60*60*24*7L){
			return "3d-7d";
		}
		return "7d and above";
	}

	public void recordBannerThemeClick(String banner) {
		HSLog.d(banner);
		banner=banner.trim();
		if(!reportedTheme.contains(banner)){
			reportedTheme.add(banner);
			HSLog.d(banner+"_true");
		}
	}

	public void recordThemeShownInDetailActivity(String theme) {
		theme=theme.trim();
		HSLog.d(theme+" shown in detail");
		detailShown.add(theme.trim());
	}

	public void recordThemeApplyInDetailActivity(String theme) {
		theme=theme.trim();
		recordThemeUsage(theme);
		if(!detailReported.contains(theme)){
			detailReported.add(theme);
			HSLog.d(theme+"_true");
		}
	}

	public void recordThemeDownloadInDetailActivity(String theme) {
		HSLog.d(theme);
		theme=theme.trim();
		if(!detailReported.contains(theme)){
			detailReported.add(theme);
			HSLog.d(theme+"_true");
		}
	}

	private void recordThemeAnalyticsOnSessionEnd() {
		for(final String theme:shownTheme){
			if(!reportedTheme.contains(theme)){
				reportedTheme.add(theme);
				recordThemeClickFalse(theme);
			}
		}

		for(final String banner:bannerShownTheme){
			if(!reportedTheme.contains(banner)){
				reportedTheme.add(banner);
				recordBannerThemeClickFalse(banner);
			}
		}

		for(final String detail:detailShown){
			if(!detailReported.contains(detail)){
				detailReported.add(detail);
				recordThemeClickFalseInDetailActivity(detail);
			}
		}

		saveReportedTheme();
	}

	private void recordThemeClickFalse(String theme) {
		HSLog.d(theme+"_false");
	}

	private void recordBannerThemeClickFalse(String banner) {
		HSLog.d(banner+"_false");
	}

	private void recordThemeClickFalseInDetailActivity(String theme) {
		HSLog.d(theme+"_false");
	}
}
