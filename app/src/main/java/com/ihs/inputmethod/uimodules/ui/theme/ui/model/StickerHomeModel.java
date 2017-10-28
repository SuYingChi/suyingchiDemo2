package com.ihs.inputmethod.uimodules.ui.theme.ui.model;

import android.view.View;

import com.ihs.inputmethod.uimodules.ui.facemoji.bean.FacemojiSticker;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerGroup;

public final class StickerHomeModel {

	public StickerGroup stickerGroup;
	public FacemojiSticker facemojiSticker;
	public int span;

	public boolean isTitle;
	public boolean isFacemoji;
	public boolean isBigCreateFacemoji;
	public boolean isSmallCreateFacemoji;
	public boolean isDownloaded;
	public boolean isMoreComing;
	public boolean titleClickable;
	public View.OnClickListener titleClickListener;
	public String title;
	public String rightButton;

	public View.OnClickListener customizedTitleClickListener;

	public StickerHomeModel() {
		stickerGroup=null;
		isTitle=false;
		isDownloaded =false;
		isBigCreateFacemoji =false;
		isSmallCreateFacemoji =false;
		titleClickable=false;
		titleClickListener=null;
		title=null;
		rightButton=null;
		customizedTitleClickListener=null;
		span=1;
	}

}
