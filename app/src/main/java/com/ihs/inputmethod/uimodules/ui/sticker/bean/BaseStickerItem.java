package com.ihs.inputmethod.uimodules.ui.sticker.bean;

/**
 * Created by dsapphire on 15/11/27.
 */
public class BaseStickerItem {
	public String name;
	public String url;
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[" +
				"name: "+ this.name +
				"url: " + this.url + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BaseStickerItem item = (BaseStickerItem) o;

		return !(url != null ? !url.equals(item.url) : item.url != null);
	}

	@Override
	public int hashCode() {
		return url != null ? url.hashCode() : 0;
	}
}
