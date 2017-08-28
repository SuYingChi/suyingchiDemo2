package com.ihs.inputmethod.uimodules.ui.emoji;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.model.Emoji;
import com.ihs.inputmethod.uimodules.ui.common.model.EmojiGroup;
import com.ihs.inputmethod.uimodules.utils.ReleaseVersionUtil;
import com.kc.commons.configfile.KCList;
import com.kc.commons.configfile.KCMap;
import com.kc.commons.configfile.KCParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by wenbinduan on 2016/11/15.
 */

final class EmojiLoader {
	private final String SKIN_PATH = "emoji_skin";
	private final String SKIN_FILE = "Emoji_Skin_Map.kc";

	private static EmojiLoader instance;
	private List<EmojiGroup> emojiGroups;
	private List<Emoji> textEmoji=new ArrayList<>();

	private Map<String,ArrayList<Emoji>> emojiSkinMapping;

	private EmojiLoader() {
		emojiGroups=new ArrayList<>();
	}

	public static EmojiLoader getInstance(){
		if(instance == null){
			synchronized (EmojiLoader.class){
				if(instance == null){
					instance = new EmojiLoader();
				}
			}
		}
		return instance;
	}


	void loadEmoji(){
		if (emojiGroups.size() == 0) {
			final Resources res = HSApplication.getContext().getResources();
			final String packageName = HSApplication.getContext().getPackageName();
			final String release = Build.VERSION.RELEASE;
			final String[] versions = res.getStringArray(R.array.emoji_group_versions);
			final String version = getRightVersion(versions, release).replace(".", "_");

			int emojiGroupArrayId = res.getIdentifier("emoji_groups" + version, "array", packageName);
			if (emojiGroupArrayId <= 0) {
				emojiGroupArrayId = R.array.emoji_groups;
			}
			final String[] emojiGroupNames = res.getStringArray(emojiGroupArrayId);
			final String[] textGroupNames = res.getStringArray(R.array.emoji_groups_text);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				KCMap emojiMapping = getEmojiSkinConfigMap();
				resolveSkinMapping(emojiMapping);
			}

			for (final String groupName : emojiGroupNames) {
				final EmojiGroup group = new EmojiGroup(groupName, false);
				int emojiGroupId = res.getIdentifier(groupName + version, "array", packageName);
				if (emojiGroupId <= 0) {
					emojiGroupId = res.getIdentifier(groupName, "array", packageName);
				}
				final boolean isText = contain(textGroupNames, groupName);
				final String[] emojiStrings = res.getStringArray(emojiGroupId);

				if (isText) {
					for (final String emojiStr : emojiStrings) {
						final Emoji emoji = new Emoji(emojiStr, 1, true);
						setEmojiSkin(emoji);
						group.addEmoji(emoji);
					}
					textEmoji.addAll(group.getEmojiList());
				} else {
					for (final String emojiStr : emojiStrings) {
						final Emoji emoji = new Emoji(codeToEmoji(emojiStr), 1, false);
						setEmojiSkin(emoji);
						group.addEmoji(emoji);
					}
				}

				emojiGroups.add(group);
			}
		}
	}

	List<String> getTabs(){
		List<String> tabs=new ArrayList<>();
		final Resources res= HSApplication.getContext().getResources();
		final String packageName=HSApplication.getContext().getPackageName();
		final String release=Build.VERSION.RELEASE;
		final String[] versions=res.getStringArray(R.array.emoji_group_versions);
		final String version=getRightVersion(versions,release).replace(".","_");

		int emojiGroupArrayId=res.getIdentifier("emoji_groups"+version,"array",packageName);
		if(emojiGroupArrayId<=0){
			emojiGroupArrayId=R.array.emoji_groups;
		}
		final String[] emojiGroupNames=res.getStringArray(emojiGroupArrayId);
		Collections.addAll(tabs,emojiGroupNames);
		return tabs;
	}

	private boolean contain(String[] textGroupNames, String groupName) {
		for(String text:textGroupNames){
			if(text.equals(groupName)){
				return true;
			}
		}
		return false;
	}

	boolean isTextEmoji(final Emoji emoji){
		for(Emoji text:textEmoji){
			if(text.equals(emoji)){
				return true;
			}
		}
		return false;
	}

	private String getRightVersion(final String[] versions, final String release) {
		String targetVersion="";
		for (final String version : versions) {
			if (ReleaseVersionUtil.compareReleaseVersion(version,release)) {
				targetVersion = "_"+version;
			}
		}
		return targetVersion;
	}

	List<EmojiGroup> getEmojiGroups() {
		return emojiGroups;
	}

	private String codeToEmoji(final String code){
		final StringBuilder sb = new StringBuilder();
		for (final String codeInHex : code.split("U\\+")) {
			if(codeInHex.trim().length()>0){
				final int codePoint = Integer.parseInt(codeInHex.trim(), 16);
				sb.appendCodePoint(codePoint);
			}
		}
		return sb.toString();
	}


	private KCMap getEmojiSkinConfigMap() {
		KCMap kcMap = null;
		try {
			   //KEY: emoji unicode string
			   // VALUE: Emoji string as the value.
				AssetManager assetManager = HSApplication.getContext().getAssets();
				kcMap = KCParser.parseMap(assetManager.open(SKIN_PATH + File.separator + SKIN_FILE));
			return kcMap;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void resolveSkinMapping(KCMap kcMap) {
		if (kcMap != null) {
			emojiSkinMapping = new HashMap<>();
			Set<String> allKey = kcMap.keySet();
			KCList emojiArray = null;
			for (String key: allKey ) {
				emojiArray =  kcMap.getList(key);
				ArrayList<Emoji> skinItems = new ArrayList<>();

				Iterator<Object> emojiArrayIterator = emojiArray.iterator();

				while (emojiArrayIterator.hasNext()) {
					String emojiStr  = (String)emojiArrayIterator.next();
					final Emoji emoji = new Emoji(emojiStr, 1, true);
					skinItems.add(emoji);

				}
				this.emojiSkinMapping.put(key,skinItems);
			}
		}




	}

	private void setEmojiSkin(Emoji emoji) {
		if (this.emojiSkinMapping != null) {
			String emojiUnicodeStr = emoji.getUnicodeStr();
			ArrayList<Emoji> emojiSkinItems = this.emojiSkinMapping.get(emojiUnicodeStr);
			if (emojiSkinItems == null) {
				//remove emoji specific variation selectors
				emojiUnicodeStr = emojiUnicodeStr.replace("\\ufe0f","");
				emojiUnicodeStr = emojiUnicodeStr.replace("\\ufe0e","");
				emojiSkinItems = this.emojiSkinMapping.get(emojiUnicodeStr);
			}
			emoji.setSkinItems(emojiSkinItems);
		}
	}
}
