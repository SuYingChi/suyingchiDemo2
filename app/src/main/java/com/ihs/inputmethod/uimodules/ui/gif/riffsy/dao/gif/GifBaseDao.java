package com.ihs.inputmethod.uimodules.ui.gif.riffsy.dao.gif;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ihs.inputmethod.uimodules.ui.gif.riffsy.model.GifItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dsapphire on 16/1/20.
 */
public final class GifBaseDao {

	private final static String _ID = "_id";

	private final static String MEDIA_ID="media_id";
	private final static String URL ="url";
	private final static String GIF ="gif";
	private final static String MP4 ="mp4";

	private static GifBaseDao instance;

	public void createTable(SQLiteDatabase db,final String name){

		String sql="CREATE TABLE IF NOT EXISTS "+name+ " ("+_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ MEDIA_ID + " TEXT,"
				+ URL + " TEXT,"
				+ GIF + " TEXT,"
				+ MP4 + " TEXT"
				+");";
		db.execSQL(sql);

	}

	private GifBaseDao(){

	}

	public static void init(){
		if(instance==null){
			synchronized (GifBaseDao.class){
				if(instance==null){
					instance=new GifBaseDao();
				}
			}
		}
	}

	public static GifBaseDao getInstance(){
		if(instance==null){
			init();
		}
		return instance;
	}

	private final static String INSERT= " ("
					+ MEDIA_ID + ","
					+ URL + ","
					+ GIF + ","
					+ MP4
					+ ") "
					+ "values(?,?,?,?)";


	public void insert(final String tableName,List<GifItem> items){
		SQLiteDatabase database= GifBaseDaoHelper.getInstance().getWritableDatabase();
		database.beginTransaction();
		try{
			createTable(database,tableName);
			final String sql="insert into "+tableName+INSERT;
			for(GifItem item:items){
				database.execSQL(sql,new Object[]{
						item.id,
						item.url,
						item.gif,
						item.mp4
				});
			}
			database.setTransactionSuccessful();
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			database.endTransaction();
		}
	}

	public void clearAll(final String tableName){
		SQLiteDatabase database= GifBaseDaoHelper.getInstance().getWritableDatabase();
		database.beginTransaction();
		try{
			createTable(database,tableName);
			final String sql="delete from " +tableName;
			database.execSQL(sql);
			database.setTransactionSuccessful();
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			database.endTransaction();
		}
	}

	public List<GifItem> getAll(final String tableName){
		List<GifItem> all=new ArrayList<>();
		SQLiteDatabase database= GifBaseDaoHelper.getInstance().getReadableDatabase();
		Cursor cursor=null;
		GifItem item;
		try{
			createTable(database,tableName);
			final String sql="select * from " +tableName;
			cursor=database.rawQuery(sql,null);
			while (cursor.moveToNext()){
				item=getItem(cursor);
				all.add(item);
			}
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return all;
	}

	private GifItem getItem(Cursor cursor) {
		GifItem item=new GifItem();
		try {
			item.id=cursor.getString(cursor.getColumnIndex(MEDIA_ID));
			item.url=cursor.getString(cursor.getColumnIndex(URL));
			item.gif=cursor.getString(cursor.getColumnIndex(GIF));
			item.mp4=cursor.getString(cursor.getColumnIndex(MP4));
		}catch (Exception e){
			e.printStackTrace();
		}
		return item;
	}

	public final void delete(final String table,final GifItem data) {
		final String sql="delete from "+table+" where "+MEDIA_ID+"=?";
		SQLiteDatabase database=GifBaseDaoHelper.getInstance().getWritableDatabase();
		database.beginTransaction();
		try {
			createTable(database,table);
			database.execSQL(sql,new Object[]{
					data.id
			});
			database.setTransactionSuccessful();
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			database.endTransaction();
		}

	}
}
