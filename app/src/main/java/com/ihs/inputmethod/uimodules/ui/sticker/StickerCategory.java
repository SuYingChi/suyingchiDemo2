package com.ihs.inputmethod.uimodules.ui.sticker;

import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;

import com.ihs.inputmethod.uimodules.ui.sticker.bean.BaseStickerItem;
import com.ihs.inputmethod.api.utils.HSJsonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

final class StickerCategory {
	private final String TAG = "StickerCategory";
	private static final String PREF_LAST_SHOWN_CATEGORY_ID="last_shown_sticker_category_id";
	private static final String PREF_CATEGORY_LAST_PAGE_ID="sticker_category_last_page_id ";
	private static final String PREF_CATEGORY_RECENT="sticker_category_recent";
	public static final int ID_UNSPECIFIED = -1;
	public static final int ID_RECENTS = 0;
	public static final int ID_YELLOW_SMILE = 1;
	public static final int ID_BLUE_SMILE = 2;
	public static final int ID_LOVE = 3;
	public static final int ID_SPORTS = 4;
	public static final int ID_PREPARED_FOOD = 5;

	public final class CategoryProperties {
		public final int mCategoryId;
		public final int mPageCount;
		public CategoryProperties(final int categoryId, final int pageCount) {
			mCategoryId = categoryId;
			mPageCount = pageCount;
		}
	}

	public static final String[] sCategoryName = {
			"recent",
			"emoji",
			"king",
			"flower",
			"car",
			"tri"};

	private SharedPreferences mPrefs;
	private final HashMap<String, Integer> mCategoryNameToIdMap = new HashMap<>();

	private final ArrayList<CategoryProperties> mShownCategories = new ArrayList<>();

	private int mCurrentCategoryId = StickerCategory.ID_UNSPECIFIED;
	private int mCurrentCategoryPageId = 0;
	private StickerManager stickerManager;

	private List<BaseStickerItem> recentList=new ArrayList<>();

	public StickerCategory(SharedPreferences prefs) {
		stickerManager=StickerManager.getInstance();
		mPrefs = prefs;
		for (int i = 0; i < sCategoryName.length; ++i) {
			mCategoryNameToIdMap.put(sCategoryName[i], i);
		}
		addShownCategoryId(StickerCategory.ID_RECENTS);
		addShownCategoryId(StickerCategory.ID_YELLOW_SMILE);
		addShownCategoryId(StickerCategory.ID_BLUE_SMILE);
		addShownCategoryId(StickerCategory.ID_LOVE);
		addShownCategoryId(StickerCategory.ID_SPORTS);
		addShownCategoryId(StickerCategory.ID_PREPARED_FOOD);
		mCurrentCategoryId=mPrefs.getInt(PREF_LAST_SHOWN_CATEGORY_ID,StickerCategory.ID_YELLOW_SMILE);
	}

	private void addShownCategoryId(final int categoryId) {
		final CategoryProperties properties = new CategoryProperties(categoryId, getCategoryPageCount(categoryId));
		mShownCategories.add(properties);
	}

	public String getCategoryName(final int categoryId, final int categoryPageId) {
		return sCategoryName[categoryId] + "-" + categoryPageId;
	}

	public int getCategoryId(final String name) {
		final String[] strings = name.split("-");
		return mCategoryNameToIdMap.get(strings[0]);
	}

	public ArrayList<CategoryProperties> getShownCategories() {
		return mShownCategories;
	}
	public int getCurrentCategoryId() {
		return mCurrentCategoryId;
	}

	public int getCurrentCategoryPageSize() {
		return getCategoryPageSize(mCurrentCategoryId);
	}

	public int getCategoryPageSize(final int categoryId) {
		for (final CategoryProperties prop : mShownCategories) {
			if (prop.mCategoryId == categoryId) {
				return prop.mPageCount;
			}
		}
		Log.e(TAG, "Invalid category id: " + categoryId);
		return 0;
	}

	public void setCurrentCategoryId(final int categoryId) {
		mCurrentCategoryId = categoryId;
		mPrefs.edit().putInt(PREF_LAST_SHOWN_CATEGORY_ID,categoryId).apply();
	}

	public void setCurrentCategoryPageId(final int id) {
		mCurrentCategoryPageId = id;
	}


	public int getTabIdFromCategoryId(final int categoryId) {
		for (int i = 0; i < mShownCategories.size(); ++i) {
			if (mShownCategories.get(i).mCategoryId == categoryId) {
				return i;
			}
		}
		Log.e(TAG, "categoryId not found: " + categoryId);
		return 0;
	}
	public String getCategoryTabIconName(final int categoryId) {
		return sCategoryName[categoryId];
	}

	public int getPageIdFromCategoryId(final int categoryId) {
		String key=PREF_CATEGORY_LAST_PAGE_ID+categoryId;
		final int lastSavedCategoryPageId = mPrefs.getInt(key,0);
		int sum = 0;
		for (int i = 0; i < mShownCategories.size(); ++i) {
			final CategoryProperties props = mShownCategories.get(i);
			if (props.mCategoryId == categoryId) {
				return sum + lastSavedCategoryPageId;
			}
			sum += props.mPageCount;
		}
		Log.e(TAG, "categoryId not found: " + categoryId);
		return 0;
	}

	private int getCategoryPageCount(final int categoryId) {
		return stickerManager.getStickerPagerSize(categoryId);
	}
	public int getCurrentCategoryPageId() {
		return mCurrentCategoryPageId;
	}
	public Pair<Integer, Integer> getCategoryIdAndPageIdFromPagePosition(final int position) {
		int sum = 0;
		for (final CategoryProperties properties : mShownCategories) {
			final int temp = sum;
			sum += properties.mPageCount;
			if (sum > position) {
				return new Pair<>(properties.mCategoryId, position - temp);
			}
		}
		Log.e(TAG,"null pair");
		return null;
	}
	public void saveRecent(){
		ArrayList<Object> saveList=new ArrayList<>();
		for(BaseStickerItem item:recentList){
			saveList.add(item.url);
		}
		mPrefs.edit().putString(PREF_CATEGORY_RECENT, HSJsonUtils.listToJsonStr(saveList)).apply();
	}
	public List<BaseStickerItem> getDataFromPagePosition(final int position) {
		Pair<Integer,Integer> pair=getCategoryIdAndPageIdFromPagePosition(position);
		if(pair==null){
			return new ArrayList<>();
		}
		if(pair.first==ID_RECENTS)
			return getRecentList(pair.first,pair.second);
		return stickerManager.getStickerListData(pair.first,pair.second);
	}

	public List<BaseStickerItem> getRecentList(){
		if(recentList.size()==0){
			String recent=mPrefs.getString(PREF_CATEGORY_RECENT,"");
			List<Object> recents=HSJsonUtils.jsonStrToList(recent);
			for(Object o:recents){
				BaseStickerItem item=new BaseStickerItem();
				item.url=o.toString();
				recentList.add(item);
			}
			if(recentList.size()==0){
				recentList.addAll(stickerManager.getStickerList(ID_RECENTS));
			}
		}
		return recentList;
	}
	public List<BaseStickerItem> getRecentList(int category,int page){
		getRecentList();
		List<BaseStickerItem> data=new ArrayList<>();
		int start = stickerManager.getCurrentSize() * page;
		int end = start + stickerManager.getCurrentSize();
		end = recentList.size() > end ? end : recentList.size();
		for(int i=start;i<end;i++){
			data.add(recentList.get(i));
		}
		return data;
	}
	public int getTotalPageCountOfCurrentCategory() {
		int sum = 0;
		for (CategoryProperties properties : mShownCategories) {
			sum += properties.mPageCount;
		}
		return sum;
	}

}