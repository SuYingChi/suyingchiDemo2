package com.keyboard.inputmethod.panels.gif.dao.gif;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ihs.app.framework.HSApplication;
import com.keyboard.inputmethod.panels.gif.model.GifItem;

import java.util.HashSet;
import java.util.List;

/**
 * Created by dsapphire on 16/1/20.
 */
public final class GifTagDaoHelper extends SQLiteOpenHelper{

	public final static String MONKEY_DB_NAME =  "dao_gif_tag.db";
	public final static int DB_TABLE_VERSION = 1;

	private static GifTagDaoHelper instance;

	private HashSet<String> tags=new HashSet<>();
	private GifTagDao dao;
	protected GifTagDaoHelper(Context context){
		super(context, MONKEY_DB_NAME, null, DB_TABLE_VERSION);
		dao=GifTagDao.getInstance();
	}

	public static void init(){
		if(null==instance){
			synchronized (GifTagDaoHelper.class){
				if(null==instance){
					instance=new GifTagDaoHelper(HSApplication.getContext());
					instance.getWritableDatabase();
					instance.loadTagTableName();
				}
			}
		}
	}

	public static GifTagDaoHelper getInstance(){
		if(instance==null){
			init();
		}
		return instance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.beginTransaction();
		try {
			createTables(db);
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}

	private void createTables(SQLiteDatabase db) {
		GifTagDao.createAllTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.beginTransaction();
		try {
			createTables(db);
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}


	private void loadTagTableName(){
		tags.addAll(GifTagDao.getAllTags());
	}

	public final boolean containTag(final String tag) {
		return tag != null && tags.contains(tag);
	}

	public void clearAll(){
		for(String tag:tags){
			dao.clearAll(tag);
		}

	}

	public void insert(final String tag,List<GifItem> items){
		dao.insert(tag,items);
	}

	public void clearAll(final String tag){
		dao.clearAll(tag);
	}

	public List<GifItem> getAll(final String tag){
		return dao.getAll(tag);
	}

	public HashSet<String> getAllTags() {
		return tags;
	}

}
