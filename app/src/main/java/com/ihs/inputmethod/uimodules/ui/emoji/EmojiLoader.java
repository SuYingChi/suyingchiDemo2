package com.ihs.inputmethod.uimodules.ui.emoji;

import android.content.res.Resources;
import android.os.Build;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.model.Emoji;
import com.ihs.inputmethod.uimodules.ui.common.model.EmojiGroup;
import com.ihs.inputmethod.uimodules.utils.ReleaseVersionUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by wenbinduan on 2016/11/15.
 */

final class EmojiLoader {

	private List<EmojiGroup> emojiGroups;
	private List<Emoji> textEmoji=new ArrayList<>();

	EmojiLoader() {
		emojiGroups=new ArrayList<>();
	}

	void loadEmoji(){
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
		final String[] textGroupNames=res.getStringArray(R.array.emoji_groups_text);

		for (final String groupName : emojiGroupNames) {
			final EmojiGroup group = new EmojiGroup(groupName, false);
			int emojiGroupId=res.getIdentifier(groupName+version,"array",packageName);
			if(emojiGroupId<=0){
				emojiGroupId=res.getIdentifier(groupName, "array", packageName);
			}
			final boolean isText=contain(textGroupNames,groupName);
			final String[] emojiStrings = res.getStringArray(emojiGroupId);

			if(isText){
				for (final String emojiStr : emojiStrings) {
					final Emoji emoji = new Emoji(emojiStr, 1, true);
					group.addEmoji(emoji);
				}
				textEmoji.addAll(group.getEmojiList());
			}else{
				for (final String emojiStr : emojiStrings) {
					final Emoji emoji = new Emoji(codeToEmoji(emojiStr), 1, false);
					group.addEmoji(emoji);
				}
			}

			emojiGroups.add(group);
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
}
