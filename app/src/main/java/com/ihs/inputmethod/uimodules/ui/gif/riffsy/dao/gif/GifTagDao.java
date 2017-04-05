package com.ihs.inputmethod.uimodules.ui.gif.riffsy.dao.gif;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.model.GifItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by dsapphire on 16/1/20.
 */
public final class GifTagDao {
	private final static String TABLE_ALL = "dao_all_tag";

	private final static String _ID = "_id";

	private final static String MEDIA_ID="media_id";
	private final static String URL ="url";
	private final static String GIF ="gif";
	private final static String MP4 ="mp4";

	private final static String NAME="all_tags_table";
	
	
	public void createTable(SQLiteDatabase db,final String tag){

		String sql="CREATE TABLE IF NOT EXISTS "+"_"+tag.replace(" ","")+ " ("+_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ MEDIA_ID + " TEXT,"
				+ URL + " TEXT,"
				+ GIF + " TEXT,"
				+ MP4 + " TEXT"
				+");";
		db.execSQL(sql);

	}

	public static void createAllTable(SQLiteDatabase db){
		String sql="CREATE TABLE IF NOT EXISTS "+TABLE_ALL+ " ("+_ID+" INTEGER PRIMARY KEY,"
				+ NAME + " TEXT"
				+");";
		db.execSQL(sql);
	}

	public static HashSet<String> getAllTags(){
		HashSet<String> all=new HashSet<>();
		SQLiteDatabase database= GifTagDaoHelper.getInstance().getReadableDatabase();
		Cursor cursor=null;
		String tag;
		try{
			createAllTable(database);
			cursor=database.rawQuery("select * from "+TABLE_ALL,null);
			while (cursor.moveToNext()){
				tag=cursor.getString(cursor.getColumnIndex(NAME));
				all.add(tag.toLowerCase());
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

	private static GifTagDao instance;

	private GifTagDao(){

	}

	public static void init(){
		if(instance==null){
			synchronized (GifTagDao.class){
				if(instance==null){
					instance=new GifTagDao();
				}
			}
		}
	}

	public static GifTagDao getInstance(){
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


	public void insert(final String tag,List<GifItem> items){
		SQLiteDatabase database= GifTagDaoHelper.getInstance().getWritableDatabase();
		database.beginTransaction();
		try{
			createTable(database,tag);
			final String sql="insert into "+"_"+tag.replace(" ","")+INSERT;
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

	public void clearAll(final String tag){
		SQLiteDatabase database= GifTagDaoHelper.getInstance().getWritableDatabase();
		database.beginTransaction();
		try{
			final String sql="delete from " +"_"+tag.replace(" ","");
			createTable(database,"_"+tag.replace(" ",""));
			database.execSQL(sql);
			database.setTransactionSuccessful();
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			database.endTransaction();
		}
	}

	public List<GifItem> getAll(final String tag){
		List<GifItem> all=new ArrayList<>();
		SQLiteDatabase database= GifTagDaoHelper.getInstance().getReadableDatabase();
		database.beginTransaction();
		Cursor cursor=null;
		GifItem item;
		try{
			createTable(database,"_"+tag.replace(" ",""));
			final String sql="select * from " +"_"+tag.replace(" ","");
			cursor=database.rawQuery(sql,null);
			while (cursor.moveToNext()){
				item=getItem(cursor);
				all.add(item);
			}
			database.setTransactionSuccessful();
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
			database.endTransaction();
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
}
