package com.ihs.inputmethod.uimodules.ui.gif.riffsy.dao;


import android.util.Log;

import com.ihs.commons.config.HSConfig;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.control.GifCategory;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.dao.base.BaseDaoHelper;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.dao.base.RequestDao;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.dao.gif.GifBaseDaoHelper;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.dao.gif.GifTagDaoHelper;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.model.GifItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by dsapphire on 16/1/20.
 */
public final class DaoHelper {
	private static final float UPDATE_INTERVAL_IN_HOUR_DEFAULT = 2.0f;

	private static DaoHelper instance;

	private static final String[] TABLE_NAME = {
			"recent",
			"favorite",
			"reactions",
			"featured",
			"trending",
			"emoji"
		};

	private HashMap<String,String> categoryToTable=new HashMap<>();

	public static DaoHelper getInstance(){
		if(instance==null){
			init();
		}
		return instance;
	}

	public static void init() {
		if(instance==null){
			synchronized (DaoHelper.class){
				if(instance==null){
					instance=new DaoHelper();
				}
			}
		}
	}

	private DaoHelper(){
		GifBaseDaoHelper.init();
		GifTagDaoHelper.init();
		BaseDaoHelper.init();
		categoryToTable.put(GifCategory.TAB_RECENT,TABLE_NAME[0]);
		categoryToTable.put(GifCategory.TAB_FAVORITE,TABLE_NAME[1]);
		categoryToTable.put(GifCategory.TAB_REACTIONS,TABLE_NAME[2]);
		categoryToTable.put(GifCategory.TAB_EXPLORE,TABLE_NAME[3]);
		categoryToTable.put(GifCategory.TAB_TRENDING,TABLE_NAME[4]);
		categoryToTable.put(GifCategory.TAB_EMOJI,TABLE_NAME[5]);
	}


	public static String[] getTableName(){
		return TABLE_NAME;
	}

	public static boolean isUserDB(final String name){
		return TABLE_NAME[1].equals(name)||TABLE_NAME[0].equals(name);
	}

	public void clearAllData(){
		GifBaseDaoHelper.getInstance().clearAll();
		GifTagDaoHelper.getInstance().clearAll();
	}

	public void clearAllData(final String category){
		final String table=categoryToTable.get(category);
		Log.e("DaoHelper",category+" will be clear and table is "+table);
		if(table!=null){
			GifBaseDaoHelper.getInstance().clearAll(table);
		}else if(GifTagDaoHelper.getInstance().containTag(category)){
			GifTagDaoHelper.getInstance().clearAll(category);
		}
	}

	public void switchLanguage(){
		GifBaseDaoHelper.createTables();
	}


	public boolean isRequestOutOfDate(String request){
		float intervalInHour = HSConfig.optFloat(UPDATE_INTERVAL_IN_HOUR_DEFAULT, "Application", "StickersGifs", "GifConfig", "UpdateIntervalInHour");
		final long last= RequestDao.getCurrentLanguageLastUpdateTime(request);
		final long now=System.currentTimeMillis();
		return now - last > intervalInHour * 60 * 60 * 1000;
	}

	public void updateRequestLastUpdateTime(String request){
		final long last=System.currentTimeMillis();
		RequestDao.updateLanguageLastUpdateTime(request,last);
	}

	public final void saveTabDataToDB(final String categoryName,final List<GifItem> list) {
		final String table=categoryToTable.get(categoryName);
		if(table!=null){
			GifBaseDaoHelper.getInstance().insert(table,list);
		}
	}

	public final void saveTagDataToDB(final String tag,final List<GifItem> list) {
		GifTagDaoHelper.getInstance().insert(tag,list);
	}


	public List<GifItem> getAllTabData(String category) {
		final String table=categoryToTable.get(category);
		if(table!=null)
			return GifBaseDaoHelper.getInstance().getAll(table);
		return new ArrayList<>();
	}

	public List<GifItem> getAllTagData(String tag) {
		if(GifTagDaoHelper.getInstance().containTag(tag))
			return GifTagDaoHelper.getInstance().getAll(tag);
		return new ArrayList<>();
	}

	public HashSet<String> getAllTags(){
		return GifTagDaoHelper.getInstance().getAllTags();
	}
}
