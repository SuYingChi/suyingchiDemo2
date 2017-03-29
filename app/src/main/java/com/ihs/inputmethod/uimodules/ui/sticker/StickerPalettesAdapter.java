package com.ihs.inputmethod.uimodules.ui.sticker;

import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.sticker.bean.BaseStickerItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class StickerPalettesAdapter extends PagerAdapter{
	private static String TAG = "FacemojiPalettesAdapter";
	private SparseArray<StickerPageView> mActivePageViews = new SparseArray<>();
    private List<StickerPageView> unusedPageVies = new ArrayList<>();

	private StickerCategory mImageCategory;
	private StickerLayoutParams mImageLayoutParams;

	private int mActivePosition = 0;
	private StickerPalettesView mGifPalettesView;
	private List<BaseStickerItem> recent;

	private HashMap<Integer,StickerPageGridAdapter> recentAdapters;
	private int recentPageSize;
	public StickerPalettesAdapter(StickerCategory imageCategory,
	                              StickerPalettesView gifPalettesView,
	                              StickerLayoutParams imageLayoutParams) {
		mImageCategory = imageCategory;
		mGifPalettesView = gifPalettesView;
		mImageLayoutParams = imageLayoutParams;
		recent=mImageCategory.getRecentList();
		recentPageSize=StickerManager.getInstance().getStickerPagerSize(0);
		recentAdapters=new HashMap<>();
	}
	
	public void flushPendingRecentStickers() {
		for(int i=0;i<recentPageSize&&i<recentAdapters.size();i++){
			StickerPageGridAdapter adapter=recentAdapters.get(i);
			if(adapter!=null){
				adapter.setData(mImageCategory.getDataFromPagePosition(i));
				adapter.notifyDataSetChanged();
			}
		}
		mImageCategory.saveRecent();
	}
	
	public void addRecentSticker(BaseStickerItem sticker) {
		int oldSize=recent.size();
		recent.remove(sticker);
		recent.add(0,sticker);
		if(recent.size()>oldSize){
			recent.remove(oldSize);
		}
	}
	@Override
	public int getCount() {
		return mImageCategory.getTotalPageCountOfCurrentCategory();
	}
	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}
	@Override
	public void setPrimaryItem(final ViewGroup container, final int position, final Object object) {
		if (mActivePosition == position) {
			return;
		}
		mActivePosition = position;
	}
	public void clear() {
		mActivePageViews.clear();
	}
	@Override
	public Object instantiateItem(final ViewGroup container, final int position) {
		StickerPageView view = mActivePageViews.get(position);
		if (view != null) {
			if(position<recentPageSize){
				flushPendingRecentStickers();
			}
			container.addView(view);
			return view;
		}
		mActivePageViews.remove(position);

        final StickerPageView gifPageView;
        StickerPageGridAdapter adapter;
        if (unusedPageVies.size() > 0) {
            gifPageView = unusedPageVies.remove(0);
        } else {
            final LayoutInflater inflater = LayoutInflater.from(container.getContext());
            gifPageView = (StickerPageView) inflater.inflate(R.layout.joy_sticker_page_view, container, false);
        }

		final StickerPageGridView gifPageGridView = (StickerPageGridView)gifPageView.findViewById(R.id.sticker_page_gridview);
		mImageLayoutParams.setPageGridViewProperties(gifPageGridView);
        adapter = new StickerPageGridAdapter(
											mImageCategory.getDataFromPagePosition(position),
											mGifPalettesView,
											container.getContext(),
											mImageLayoutParams.getViewWidth(),
											mImageLayoutParams.getViewHeight());
		gifPageGridView.setAdapter(adapter);
		container.addView(gifPageView);
		mActivePageViews.put(position, gifPageView);
		if(position<recentPageSize){
			recentAdapters.put(position,adapter);
		}
		return gifPageView;
	}
	@Override
	public boolean isViewFromObject(final View view, final Object object) {
		return view == object;
	}

	@Override
	public void destroyItem(final ViewGroup container, final int position, final Object object) {
        StickerPageView pageView = mActivePageViews.get(position);
		container.removeView(pageView);
		mActivePageViews.remove(position);
        unusedPageVies.add(0,pageView);
	}


//	@Override
//	public void save() {
//		for (int i = 0, len = mActivePageViews.size(); i < len; i++) {
//			StickerPageView pageView = mActivePageViews.valueAt(i);
//			StickerPageGridView gridView = (StickerPageGridView)pageView.findViewById(R.id.sticker_page_gridview);
//			if (gridView != null) {
//				gridView.save();
//			}
//		}
//	}
//
//	@Override
//	public void restore() {
//		for (int i = 0, len = mActivePageViews.size(); i < len; i++) {
//			StickerPageView pageView = mActivePageViews.valueAt(i);
//			StickerPageGridView gridView = (StickerPageGridView)pageView.findViewById(R.id.sticker_page_gridview);
//			if (gridView != null) {
//				gridView.restore();
//			}
//		}
//	}
//
//	@Override
//	public void release()
//	{
//
//		for (int i = 0, len = mActivePageViews.size(); i < len; i++) {
//            StickerPageView pageView = mActivePageViews.valueAt(i);
//            StickerPageGridView gridView = (StickerPageGridView) pageView.findViewById(R.id.sticker_page_gridview);
//            if (gridView != null) {
//                gridView.release();
//            }
//        }
//	}
//
//    @Override
//    public Recoverable.State currentState() {
//        throw new UnsupportedOperationException();
//    }
//
//	private boolean isCurrentPage(final int position) {
//		return position == mGifPalettesView.getCurrentPagerPosition();
//	}
}