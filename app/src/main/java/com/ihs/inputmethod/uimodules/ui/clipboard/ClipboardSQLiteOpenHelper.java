package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;


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
        ClipboardDataBaseOperateImpl.getInstance().createAllTable(db);
        HSLog.d("ClipboardSQLiteOpenHelper", "create  " + CLIPBOARD_DATABASE_NAME + "   database ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        ClipboardDataBaseOperateImpl.getInstance().createAllTable(db);
    }
}