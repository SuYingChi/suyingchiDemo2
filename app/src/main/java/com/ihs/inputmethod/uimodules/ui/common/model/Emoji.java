package com.ihs.inputmethod.uimodules.ui.common.model;

/**
 * Created by wenbinduan on 2016/11/14.
 */

public final class Emoji {

	private final String label;
	private final int column;
	private final boolean isText;
	private boolean isDivider =false;
	
	public Emoji(final String label, final int column, final boolean isText) {
		this.label = label;
		this.column = column;
		this.isText =isText;
	}

	public String getLabel() {
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Emoji emoji = (Emoji) o;

		return label != null ? label.equals(emoji.label) : emoji.label == null;

	}
}
