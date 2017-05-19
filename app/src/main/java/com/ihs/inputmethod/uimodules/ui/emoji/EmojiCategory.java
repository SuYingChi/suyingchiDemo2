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

package com.ihs.inputmethod.uimodules.ui.emoji;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Pair;

import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.framework.HSEmojiSuggestionManager;
import com.ihs.inputmethod.api.utils.HSJsonUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.model.Emoji;
import com.ihs.inputmethod.uimodules.ui.common.model.EmojiGroup;

import java.util.ArrayList;
import java.util.List;

final class EmojiCategory {

	static final String SUGGESTION="emoji_suggestion";
	private final String RECENT="emoji_recent";

	private final String PRE_LAST_SHOWN_TAB_KEY="emoji_last_show_tab";
//	private final String PREF_LAST_SHOWN_ITEM_POSITION = "emoji_tab_last_show_item_position_";
//	private final String PREF_LAST_SHOWN_ITEM_OFFSET = "emoji_tab_last_show_item_offset_";
	private final String PREF_EMOJI_RECENT_KEYS = "emoji_recent_keys";
	private final List<Emoji> recentTemp=new ArrayList<>();
	private final EmojiGroup suggestion;
	private final SharedPreferences prefs;
	private final int maxRecentCount;
	private final int rowCount;
	private List<EmojiGroup> tabs=new ArrayList<>();
	private EmojiGroup recent;
	private String currentTabName = RECENT;
	private boolean hasPendingRecent;
	private HSEmojiPanelView panelView;
	private boolean suggestionAdded=false;

	EmojiCategory(final SharedPreferences prefs, final Resources res,HSEmojiPanelView panelView) {
		this.prefs = prefs;
		this.panelView=panelView;

		this.maxRecentCount = res.getInteger(R.integer.config_emoji_recent_max_count);
		this.rowCount = res.getInteger(R.integer.config_emoji_row_count);

		suggestion=new EmojiGroup(SUGGESTION,false);


		currentTabName = this.prefs.getString(PRE_LAST_SHOWN_TAB_KEY,getDefaultTab());

	}

	void loadDataAsync(){
		new LoadDataTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	void addSuggestionTab() {
		loadSuggestionTabData();
		suggestionAdded=true;
	}

	private void loadSuggestionTabData() {
		suggestion.clearEmoji();
		final List<String> suggestionEmojis= HSEmojiSuggestionManager.getFollowEmojiForTypedWords();
		for(final String suggestionStr:suggestionEmojis){
			final Emoji emoji=new Emoji(suggestionStr,1,false);
			if(EmojiLoader.getInstance().isTextEmoji(emoji)){
				final Emoji text=new Emoji(suggestionStr,1,true);
				suggestion.addEmoji(text);
				continue;
			}
			suggestion.addEmoji(emoji);
		}
		suggestion.sortEmoji(rowCount);
	}

	List<String> getTabs() {
		return EmojiLoader.getInstance().getTabs();
	}

	String getDefaultTab() {
		final List<String> tab=getTabs();
		if (tab.size() > 1) {
			return tab.get(1);
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
		HSLog.e("calculation error!");
		return RECENT;
	}

	boolean isRecentTab(final String tabName) {
		return RECENT.equals(tabName);
	}

//	void saveLastShownItemPosition(final int position, final int offset) {
//		final String tab=getTabNameForPosition(position);
//		int itemPosition=position;
//		for(EmojiGroup group:tabs){
//			if(group.getTabName().equals(tab)){
//				break;
//			}
//			itemPosition-=group.size();
//		}
//		prefs.edit().putInt(PREF_LAST_SHOWN_ITEM_POSITION + tab, itemPosition).apply();
//		prefs.edit().putInt(PREF_LAST_SHOWN_ITEM_OFFSET + tab, offset).apply();
//	}

	void saveRecent(){
		final List<Object> keys=new ArrayList<>();
		for(final Emoji emoji:recentTemp){
			if(emoji.toString().trim().length()>0){
				keys.add(emoji.toString());
			}
		}
		final String jsonStr = HSJsonUtils.listToJsonStr(keys);
		prefs.edit().putString(PREF_EMOJI_RECENT_KEYS, jsonStr).apply();
	}

	private void loadRecent(){
		final String str = prefs.getString(PREF_EMOJI_RECENT_KEYS, "");
		final List<Object> keys = HSJsonUtils.jsonStrToList(str);
		if(keys.size()>0){
			recent.clearEmoji();
		}
		for(final Object key:keys){
			final Emoji emoji=new Emoji(key.toString(),1,false);
			if(EmojiLoader.getInstance().isTextEmoji(emoji)){
				final Emoji text=new Emoji(key.toString(),1,true);
				recent.addEmoji(text);
				continue;
			}
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
		recent.sortEmoji(rowCount);
	}
	
	Pair<Integer,Integer> getLastShownItemPositionForTab(String tab) {
//		final int lastShowItem =prefs.getInt(PREF_LAST_SHOWN_ITEM_POSITION +tab,0);
//		final int offset =prefs.getInt(PREF_LAST_SHOWN_ITEM_OFFSET +tab,0);
		int sum = 0;
		for (int i = 0; i < tabs.size(); ++i) {
			final EmojiGroup group=tabs.get(i);
			if (tab.equals(group.getTabName())) {
//				return new Pair<>(sum+lastShowItem,offset);
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

	private class LoadDataTask extends AsyncTask<Void,Void,Void>{

		@Override
		protected Void doInBackground(Void... params) {
			EmojiLoader.getInstance().loadEmoji();

			tabs.addAll(EmojiLoader.getInstance().getEmojiGroups());

			recent=tabs.get(0);
			loadRecent();
			while(recent.getEmojiList().size()>maxRecentCount){
				recent.removeLastEmoji();
			}

			if(suggestionAdded){
				tabs.add(1,suggestion);
			}

			for(final EmojiGroup group:tabs){
				group.sortEmoji(rowCount);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			panelView.onDataLoaded();
		}
	}
}
