package com.ihs.inputmethod.uimodules.ui.common.model;

import java.util.List;

/**
 * Created by wenbinduan on 2016/11/14.
 */

public final class Emoji {

	private final String label;
	private final int column;
	private final boolean isText;
	private boolean isDivider =false;



	private List<Emoji> skinItems;//皮肤族
	private int skinSelectedIndex = -1;//记录被选中的表情族序号
	private Emoji superEmoji;//父表情
	
	public Emoji(final String label, final int column, final boolean isText) {
		this.label = label;
		this.column = column;
		this.isText =isText;
	}

	public String getLabel() {
		if (this.skinSelectedIndex != -1 && skinItems != null && skinItems.size() > skinSelectedIndex) {
			Emoji selectedEmoji = skinItems.get(skinSelectedIndex);
			return selectedEmoji.getLabel();
		}
		return label;
	}

	public int getColumn() {
		return column;
	}

	public boolean isText() {
		return isText;
	}
	
	public boolean isDivider() {
		return isDivider;
	}
	
	public void setDivider(boolean divider) {
		isDivider = divider;
	}
	
	@Override
	public String toString() {
		return label;
	}

	@Override
	public int hashCode() {
		return label.hashCode();
	}

	public List<Emoji> getSkinItems() {
		return skinItems;
	}

	public void setSkinItems(List<Emoji> skinItems) {
		this.skinItems = skinItems;
		if (this.skinItems != null ) {
			for (Emoji item:this.skinItems) {
				item.superEmoji  = this;
			}
		}

	}

	/**
	 * 是否支持皮肤功能
	 * @return
	 */
	public boolean supportSkin() {
		return this.skinItems != null;
	}
	public Emoji getSuperEmoji() {
		return superEmoji;
	}

// --Commented out by Inspection START (18/1/11 下午2:41):
//	public void setSuperEmoji(Emoji superEmoji) {
//		this.superEmoji = superEmoji;
//	}
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//	public int getSkinSelectedIndex() {
//		return skinSelectedIndex;
//	}
// --Commented out by Inspection STOP (18/1/11 下午2:41)

	public void setSkinSelectedIndex(int skinSelectedIndex) {
		this.skinSelectedIndex = skinSelectedIndex;
	}

	public void selected() {
		Emoji superEmoji = this.superEmoji;
		if (superEmoji == null) {
			return ;
		}
		int index = superEmoji.getSkinItems().indexOf(this);
		superEmoji.setSkinSelectedIndex(index);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Emoji emoji = (Emoji) o;

		return label != null ? label.equals(emoji.label) : emoji.label == null;

	}

	/**
	 * Emoji string to Emoji unicode value string
	 * @param str
	 * @return
	 */
	private String convert(String str) {
		str = (str == null ? "" : str);
		String tmp;
		StringBuffer sb = new StringBuffer(1000);
		char c;
		int i, j;
		sb.setLength(0);
		for (i = 0; i < str.length(); i++) {
			c = str.charAt(i);
			sb.append("\\u");
			j = (c >>> 8); //取出高8位
			tmp = Integer.toHexString(j);
			if (tmp.length() == 1)
				sb.append("0");
			sb.append(tmp);
			j = (c & 0xFF); //取出低8位
			tmp = Integer.toHexString(j);
			if (tmp.length() == 1)
				sb.append("0");
						sb.append(tmp);
		}
		return (new String(sb));
	}

	public String getUnicodeStr(){
		return convert(this.label);
	}
}
