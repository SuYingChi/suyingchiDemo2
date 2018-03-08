package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import static com.ihs.inputmethod.uimodules.ui.clipboard.ClipboardDataBaseOperateImpl.CLIPBOARD_PINS_CONTENT_COLUMN_NAME;
import static com.ihs.inputmethod.uimodules.ui.clipboard.ClipboardDataBaseOperateImpl.CLIPBOARD_PINS_TABLE;
import static com.ihs.inputmethod.uimodules.ui.clipboard.ClipboardDataBaseOperateImpl.CLIPBOARD_RECENT_CONTENT_COLUMN_NAME;
import static com.ihs.inputmethod.uimodules.ui.clipboard.ClipboardDataBaseOperateImpl.CLIPBOARD_RECENT_ISPINED_COLUMN_NAME;
import static com.ihs.inputmethod.uimodules.ui.clipboard.ClipboardDataBaseOperateImpl.CLIPBOARD_RECENT_TABLE;
import static com.ihs.inputmethod.uimodules.ui.clipboard.ClipboardDataBaseOperateImpl._ID;


public class ClipboardSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String CLIPBOARD_DATABASE_NAME = "clipboard";
    private static final int CLIPBOARD_DATABASE_VERSION = 1;
    private static ClipboardSQLiteOpenHelper clipboardSQLiteOpenHelper;

    public static ClipboardSQLiteOpenHelper getInstance() {
        if (null == clipboardSQLiteOpenHelper) {
            synchronized (ClipboardSQLiteOpenHelper.class) {
                if (null == clipboardSQLiteOpenHelper) {
                    clipboardSQLiteOpenHelper = new ClipboardSQLiteOpenHelper(HSApplication.getContext());
                }
            }
        }
        return clipboardSQLiteOpenHelper;
    }


    private ClipboardSQLiteOpenHelper(Context context) {
        super(context, CLIPBOARD_DATABASE_NAME, null, CLIPBOARD_DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createAllTable(db);
        HSLog.d(ClipboardSQLiteOpenHelper.class.getSimpleName(), "create  " + CLIPBOARD_DATABASE_NAME + "   database ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    //创建所有表
    private void createAllTable(SQLiteDatabase db) {
        String pinsSql = "CREATE TABLE IF NOT EXISTS " + CLIPBOARD_PINS_TABLE + " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CLIPBOARD_PINS_CONTENT_COLUMN_NAME + " TEXT NOT NULL DEFAULT '' ," + "UNIQUE (" + CLIPBOARD_PINS_CONTENT_COLUMN_NAME + ")" + ");";
        String recentSql = "CREATE TABLE IF NOT EXISTS " + CLIPBOARD_RECENT_TABLE + " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + " TEXT NOT NULL DEFAULT '' ,"
                + CLIPBOARD_RECENT_ISPINED_COLUMN_NAME + " INTEGER NOT NULL DEFAULT 0 ," + "UNIQUE (" + CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + ")" + ");";
        try {
            db.execSQL(pinsSql);
            db.execSQL(recentSql);

        } catch (Exception e) {
            e.printStackTrace();
        }
        HSLog.d(ClipboardSQLiteOpenHelper.class.getSimpleName(), "create recent table and pins table");
    }
}