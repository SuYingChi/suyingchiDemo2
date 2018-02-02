/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ihs.inputmethod.uimodules.ui.textart;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Pair;

import com.ihs.inputmethod.api.utils.HSJsonUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.model.Emoji;
import com.ihs.inputmethod.uimodules.ui.common.model.EmojiGroup;

import java.util.ArrayList;
import java.util.List;

final class TextCategory {

	private final String RECENT="text_recent";

	private final String PRE_LAST_SHOWN_TAB_KEY="text_last_show_tab";
	private final String PREF_RECENT_TEXT = "text_recent_text";
	private final String PREF_RECENT_COLUMN = "text_recent_column";

	private List<EmojiGroup> tabs=new ArrayList<>();
	private final EmojiGroup recent;
	private final List<Emoji> recentTemp=new ArrayList<>();

	private final SharedPreferences prefs;
	private final int maxRecentCount;
	private final int rowCount;

	private String currentTabName = RECENT;
	private boolean hasPendingRecent;
	
	TextCategory(final SharedPreferences prefs, final Resources res) {
		this.prefs = prefs;

		this.maxRecentCount = res.getInteger(R.integer.config_text_art_recent_max_count);
		this.rowCount = res.getInteger(R.integer.config_text_art_row_count);

//		recent=new EmojiGroup(DELETE_PINS,false);
		TextArtLoader loader=new TextArtLoader();
		loader.loadTextArt();

//		tabs.add(recent);
		tabs.addAll(loader.getTextArtGroups());

		recent=tabs.get(0);
		loadRecent();
		while(recent.getEmojiList().size()>maxRecentCount){
			recent.removeLastEmoji();
		}

		currentTabName = this.prefs.getString(PRE_LAST_SHOWN_TAB_KEY,tabs.get(1).getTabName());
		if(!getTabs().contains(currentTabName)){
			currentTabName=getDefaultTab();
		}
		for(final EmojiGroup group:tabs){
			group.sortTextArt(rowCount);
		}
	}

	List<String> getTabs() {
		final List<String> shownTabs=new ArrayList<>();
		for(final EmojiGroup group:tabs){
			shownTabs.add(group.getTabName());
		}
		return shownTabs;
	}


	String getDefaultTab() {
		if (tabs.size() > 1) {
			return tabs.get(1).getTabName();
		}
		return RECENT;
	}

	String getCurrentTabName() {
		return currentTabName;
	}


	void setCurrentTabName(final String tabName) {
		currentTabName = tabName;
		prefs.edit().putString(PRE_LAST_SHOWN_TAB_KEY,tabName).apply();
	}

	String getTabNameForPosition(final int position){
		int sum = 0;
		for (int i = 0; i < tabs.size(); ++i) {
			final EmojiGroup group=tabs.get(i);
			if(position>=sum&&position<sum+group.size()){
				return group.getTabName();
			}
			sum+=group.size();
		}
		return RECENT;
	}

	boolean isRecentTab(final String tabName) {
		return RECENT.equals(tabName);
	}

	void saveRecent(){
		final List<Object> keys=new ArrayList<>();
		final List<Object> columns=new ArrayList<>();
		for(final Emoji emoji:recentTemp){
			if(emoji.toString().trim().length()>0){
				keys.add(emoji.toString());
				columns.add(emoji.getColumn());
			}
		}
		final String jsonStr = HSJsonUtils.listToJsonStr(keys);
		prefs.edit().putString(PREF_RECENT_TEXT, jsonStr).apply();
		final String columnStr = HSJsonUtils.listToJsonStr(columns);
		prefs.edit().putString(PREF_RECENT_COLUMN, columnStr).apply();
	}

	private void loadRecent(){
		final String str = prefs.getString(PREF_RECENT_TEXT, "");
		final String colStr = prefs.getString(PREF_RECENT_COLUMN, "");
		final List<Object> keys = HSJsonUtils.jsonStrToList(str);
		final List<Object> cols = HSJsonUtils.jsonStrToList(colStr);
		if(keys.size()>0){
			recent.clearEmoji();
		}
		for(int i=0;i<keys.size()&&i<cols.size();i++){
			final Emoji emoji=new Emoji(keys.get(i).toString(), Integer.parseInt(cols.get(i).toString()),true);
			recent.addEmoji(emoji);
		}
		recentTemp.addAll(recent.getEmojiList());
	}

	void pendingRecentEmoji(final Emoji key) {
		hasPendingRecent=true;
		final Emoji temp=new Emoji(key.getLabel(),key.getColumn(),key.isText());
		recentTemp.remove(temp);
		recentTemp.add(0,temp);
		if(recentTemp.size()> maxRecentCount){
			recentTemp.remove(maxRecentCount);
		}

		saveRecent();
	}
	
	boolean hasPendingRecent(){
		return hasPendingRecent;
	}
	
	void flushPendingRecentEmoji() {
		hasPendingRecent=false;
		recent.clearEmoji();
		for(Emoji emoji:recentTemp){
			recent.addEmoji(emoji);
		}
		recent.sortTextArt(rowCount);
	}

	Pair<Integer,Integer> getLastShownItemPositionForTab(String tab) {
		int sum = 0;
		for (int i = 0; i < tabs.size(); ++i) {
			final EmojiGroup group=tabs.get(i);
			if (tab.equals(group.getTabName())) {
				return new Pair<>(sum,0);
			}
			sum += group.size();
		}
		return new Pair<>(0,0);
	}

	List<Emoji> getSortEmoji() {
		List<Emoji> emojis=new ArrayList<>();
		for(EmojiGroup group:tabs){
			emojis.addAll(group.getEmojiList());
		}
		return emojis;
	}


	boolean isRecentEmpty() {
		return recentTemp.isEmpty();
	}
}
