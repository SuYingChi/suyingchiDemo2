package com.ihs.inputmethod.uimodules.ui.gif.riffsy.dao.base;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by dsapphire on 16/1/20.
 */
public final class RequestDao {

	private final static String NAME="request_table";
	private final static String _ID = "_id";

	private final static String REQUEST_NAME ="request";
	private final static String LANG ="lang";
	private final static String LAST_UPDATE ="last_update";

	public static void createTable(SQLiteDatabase db){

		String sql="CREATE TABLE IF NOT EXISTS "+NAME+ " ("+_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ REQUEST_NAME + " TEXT,"
				+ LANG + " TEXT,"
				+ LAST_UPDATE + " TEXT"
				+");";
		db.execSQL(sql);

	}

	private static RequestDao instance;

	private RequestDao(){

	}

	public static void init(){
		if(instance==null){
			synchronized (RequestDao.class){
				if(instance==null){
					instance=new RequestDao();
				}
			}
		}
	}

// --Commented out by Inspection START (18/1/11 下午2:41):
//	public static RequestDao getInstance(){
//		if(instance==null){
//			init();
//		}
//		return instance;
//	}
// --Commented out by Inspection STOP (18/1/11 下午2:41)

	private final static String INSERT= "insert into " +NAME
					+ " ("
					+ REQUEST_NAME + ","
					+ LANG + ","
					+ LAST_UPDATE
					+ ") "
					+ "values(?,?,?)";

	private final static String QUERY="select * from "+NAME
			+ " where "+REQUEST_NAME+"=?"
			+ " and "+LANG+"=?";

	private final static String UPDATE="update "+NAME+" set "
			+ LAST_UPDATE+"=?"
			+ " where "+REQUEST_NAME+"=?"
			+ " and "+LANG+"=?";

	public static long getCurrentLanguageLastUpdateTime(String request){
		long last=0;
		final String lang= LanguageDao.getCurrentLanguageForDB();
		SQLiteDatabase database = BaseDaoHelper.getInstance().getReadableDatabase();
		Cursor cursor=null;
		try{
			cursor=database.rawQuery(QUERY,new String[]{
					request,
					lang
			});
			if(cursor.moveToNext()){
				String value=cursor.getString(cursor.getColumnIndex(LAST_UPDATE));
				if(value!=null&&value.trim().length()>0){
					last=Long.valueOf(value);
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if(cursor!=null){
				cursor.close();
			}
		}

		return last;
	}

	public static void updateLanguageLastUpdateTime(final String request, final long time){
		SQLiteDatabase database = BaseDaoHelper.getInstance().getReadableDatabase();
		final String lang= LanguageDao.getCurrentLanguageForDB();
		Cursor cursor=null;
		try{
			cursor=database.rawQuery(QUERY,new String[]{
					request,
					lang
			});
			if(cursor.moveToNext()){
				database.execSQL(UPDATE,new Object[]{
						String.valueOf(time),
						request,
						lang
				});
			}else{
				database.execSQL(INSERT,new Object[]{
						request,
						lang,
						String.valueOf(time)
				});
			}
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if(cursor!=null){
				cursor.close();
			}
		}
	}

}
