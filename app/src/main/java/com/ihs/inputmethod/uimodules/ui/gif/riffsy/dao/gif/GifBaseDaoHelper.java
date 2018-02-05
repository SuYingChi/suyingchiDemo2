package com.ihs.inputmethod.uimodules.ui.gif.riffsy.dao.gif;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.dao.DaoHelper;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.dao.base.LanguageDao;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.model.GifItem;

import java.util.List;

/**
 * Created by dsapphire on 16/1/20.
 */
public class GifBaseDaoHelper extends SQLiteOpenHelper{

	public final static int DB_TABLE_VERSION = 1;
	public final static String MONKEY_DB_NAME =  "dao_gif_data.db";

	private static GifBaseDaoHelper instance;

	private GifBaseDao dao;
	private String lang;
	protected GifBaseDaoHelper(final Context context){
		super(context, MONKEY_DB_NAME, null, DB_TABLE_VERSION);
		dao=GifBaseDao.getInstance();
		lang= LanguageDao.getCurrentLanguageForDB();
	}

	public static void init(){
		if(null==instance){
			synchronized (GifBaseDaoHelper.class){
				if(instance==null){
					instance=new GifBaseDaoHelper(HSApplication.getContext());
					instance.getWritableDatabase();
				}
			}
		}
	}

	public static GifBaseDaoHelper getInstance(){
		if(instance==null){
			init();
		}
		return instance;
	}

	public static void createTables(){
		instance=getInstance();
		instance.lang=LanguageDao.getCurrentLanguageForDB();
		instance.onCreate(instance.getWritableDatabase());
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
		String[] tables= DaoHelper.getTableName();
		for (String name:tables){
			dao.createTable(db,getTableName(name));
		}
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

	public static String getTableName(final String name){
		if(DaoHelper.isUserDB(name)){
			return name;
		}
		final String lang= LanguageDao.getCurrentLanguageForDB();
		return name+"_"+lang;
	}

	public final void clearAll(){
		String[] tables=DaoHelper.getTableName();
		for (String name:tables){
			if(!DaoHelper.isUserDB(name)){
				dao.clearAll(name+"_"+lang);
			}
		}
	}

	public final void insert(final String tableName,List<GifItem> items){
		final String table=getTableName(tableName);
		dao.insert(table,items);
	}

	public final void clearAll(final String tableName){
		if(!DaoHelper.isUserDB(tableName)){
			dao.clearAll(tableName+"_"+lang);
		}else{
			dao.clearAll(tableName);
		}
	}

	public List<GifItem> getAll(final String tableName){
		return dao.getAll(getTableName(tableName));
	}

// --Commented out by Inspection START (18/1/11 下午2:41):
//	public final void delete(final String tableName,final GifItem data) {
//		final String table=getTableName(tableName);
//		dao.delete(table,data);
//	}
// --Commented out by Inspection STOP (18/1/11 下午2:41)
}
