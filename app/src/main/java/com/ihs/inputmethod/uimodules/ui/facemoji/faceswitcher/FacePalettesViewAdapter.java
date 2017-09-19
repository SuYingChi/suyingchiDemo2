package com.ihs.inputmethod.uimodules.ui.facemoji.faceswitcher;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiManager;

class FacePalettesViewAdapter extends PagerAdapter {

	private FaceLayoutParams mFaceLayoutParams;
	private SparseArray<GridView> mActivePageViews = new SparseArray<>();
	private int currentPosition;

	public FacePalettesViewAdapter(FaceLayoutParams faceLayoutParams) {
		mFaceLayoutParams = faceLayoutParams;
	}

	@Override
	public void destroyItem(final ViewGroup container, final int position, final Object object) {
		if (object instanceof View) {
			container.removeView((View) object);
		}
		mActivePageViews.remove(position);
	}

	@Override
	public int getCount() {
		return FacemojiManager.getFacePageCount();
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	@Override
	public Object instantiateItem(final ViewGroup container, final int position) {

		GridView view = mActivePageViews.get(position);
		if (view != null) {
			container.addView(view);
			return view;
		}
		mActivePageViews.remove(position);
		final GridView pageGridView = new GridView(container.getContext());
		pageGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		mFaceLayoutParams.setPageGridViewProperties(pageGridView);

		final FacePageGridViewAdapter adapter = new FacePageGridViewAdapter(this, FacemojiManager.getFaceByPagePosition(position), mFaceLayoutParams);

		pageGridView.setAdapter(adapter);

		container.addView(pageGridView);
		mActivePageViews.put(position, pageGridView);
		return pageGridView;
	}

	@Override
	public boolean isViewFromObject(final View view, final Object object) {
		return view == object;
	}

	@Override
	public void setPrimaryItem(final ViewGroup container, final int position, final Object object) {
		currentPosition = position;
	}

	public void onFaceSelected(){
		for(int i =0; i<mActivePageViews.size();i++){
			if(i==currentPosition||mActivePageViews.get(i)==null){
				continue;
			}
			FacePageGridViewAdapter tempAdapter = (FacePageGridViewAdapter)mActivePageViews.get(i).getAdapter();
			if(tempAdapter!=null){
				tempAdapter.notifyDataSetChanged();
			}
		}
	}

}