package com.ihs.inputmethod.uimodules.ui.textart;

import android.content.res.Resources;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.model.Emoji;
import com.ihs.inputmethod.uimodules.ui.common.model.EmojiGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wenbinduan on 2016/11/14.
 */

final class TextArtLoader {

	private List<EmojiGroup> textArtGroups;

	TextArtLoader() {
		textArtGroups=new ArrayList<>();
	}


	List<EmojiGroup> getTextArtGroups() {
		return textArtGroups;
	}

	void loadTextArt() {
		final Resources res=HSApplication.getContext().getResources();

		final String[] textArtGroupNames=res.getStringArray(R.array.text_art_groups);

		final String packageName=HSApplication.getContext().getPackageName();

		for (final String groupName:textArtGroupNames) {

			final EmojiGroup group=new EmojiGroup(groupName,true);

			final String[] textArtStrings=res.getStringArray(res.getIdentifier(groupName,"array",packageName));
			final int[] columns=res.getIntArray(res.getIdentifier(groupName+"_column","array",packageName));

			for (int i=0;i<textArtStrings.length&&i<columns.length;i++) {
				final Emoji emoji=new Emoji(textArtStrings[i].trim(),columns[i],true);
				group.addEmoji(emoji);
			}
			textArtGroups.add(group);
		}
	}

}
