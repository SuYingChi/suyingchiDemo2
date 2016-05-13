package com.keyboard.inputmethod.panels.gif.emojisearch;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

final class ESPalettesViewAdapter extends PagerAdapter {

	private ESPalettesView mESPalettesView;
	private ESLayoutParams mESLayoutParams;

	public ESPalettesViewAdapter(ESPalettesView esPalettesView, ESLayoutParams esLayoutParams) {
		mESPalettesView = esPalettesView;
		mESLayoutParams = esLayoutParams;
	}

	@Override
	public void destroyItem(final ViewGroup container, final int position, final Object object) {
		if (object instanceof View) {
			container.removeView((View) object);
		}
	}

	@Override
	public int getCount() {
		return ESManager.getInstance().getPageCount();
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	@Override
	public Object instantiateItem(final ViewGroup container, final int position) {

		final GridView pageGridView = new GridView(container.getContext());
		pageGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		mESLayoutParams.setPageGridViewProperties(pageGridView);

		final ESPageGridViewAdapter adapter = new ESPageGridViewAdapter(
			ESManager.getInstance().getDataByPagePosition(position),
			mESPalettesView,
			container.getContext(),
			mESLayoutParams);

		pageGridView.setAdapter(adapter);

		container.addView(pageGridView);

		return pageGridView;
	}

	@Override
	public boolean isViewFromObject(final View view, final Object object) {
		return view == object;
	}

	@Override
	public void setPrimaryItem(final ViewGroup container, final int position, final Object object) {}
}