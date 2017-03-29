package com.ihs.inputmethod.uimodules.ui.gif.riffsy.dao.base;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ihs.app.framework.HSApplication;

/**
 * Created by dsapphire on 16/1/20.
 */
public final class BaseDaoHelper extends SQLiteOpenHelper{

	public final static String MONKEY_DB_NAME =  "dao_base.db";
	public final static int DB_TABLE_VERSION = 1;


	private static BaseDaoHelper instance;

	protected BaseDaoHelper(Context context){
		super(context, MONKEY_DB_NAME, null, DB_TABLE_VERSION);
	}

	public static void init(){
		if(null==instance){
			synchronized (BaseDaoHelper.class){
				if(null==instance){
					instance=new BaseDaoHelper(HSApplication.getContext());
					instance.getWritableDatabase();
				}
			}
		}
	}

	public static BaseDaoHelper getInstance(){
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

	private void createTables(SQLiteDatabase db) {
		RequestDao.createTable(db);
	}
}
