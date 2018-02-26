package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class ClipboardSQLiteDao {

    private static final String CLIPBOARD_RECENT_TABLE = "clipboard_recent_table";
    private static final String CLIPBOARD_PINS_TABLE = "clipboard_pins_table";
    private static final String _ID = "_id";
    private static final String CLIPBOARD_RECENT_CONTENT_COLUMN_NAME = "clipboard_recent_content";
    private static final String CLIPBOARD_RECENT_ISPINED_COLUMN_NAME = "clipboard_recent_isPined";
    private static final String CLIPBOARD_PINS_CONTENT_COLUMN_NAME = "clipboard_PinS_content";
    private static  volatile ClipboardSQLiteDao clipboardSQLiteDao;
    private final static String TAG = ClipboardSQLiteDao.class.getSimpleName();

    public static ClipboardSQLiteDao getInstance() {
        if (clipboardSQLiteDao == null) {
            synchronized (ClipboardSQLiteDao.class) {
                if (null == clipboardSQLiteDao) {
                    clipboardSQLiteDao = new ClipboardSQLiteDao();
                }
            }
        }
        return clipboardSQLiteDao;
    }

    //创建所有表
    void createAllTable(SQLiteDatabase db) {
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
        HSLog.d(TAG, "create recent table and pins table");
    }

    //获取Recent表的被反转的list,
    List<ClipboardRecentViewAdapter.ClipboardRecentMessage> getRecentAllContentFromTable() {
        List<ClipboardRecentViewAdapter.ClipboardRecentMessage> all = new ArrayList<ClipboardRecentViewAdapter.ClipboardRecentMessage>();
        SQLiteDatabase database = openClipboardDatabase();
        Cursor cursor = null;
        String recentContentItem;
        int isPined;
        ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage;
        try {
            database.beginTransaction();
            cursor = database.query(CLIPBOARD_RECENT_TABLE, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                recentContentItem = cursor.getString(cursor.getColumnIndex(CLIPBOARD_RECENT_CONTENT_COLUMN_NAME));
                isPined = cursor.getInt(cursor.getColumnIndex(CLIPBOARD_RECENT_ISPINED_COLUMN_NAME));
                clipboardRecentMessage = new ClipboardRecentViewAdapter.ClipboardRecentMessage(recentContentItem, isPined);
                all.add(clipboardRecentMessage);
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            database.endTransaction();
        }
        Collections.reverse(all);
        HSLog.d(TAG, "AllRecentData in database is  " + all.toString());
        return all;
    }

    //获取Pins表反转的list
    List<String> getPinsAllContentFromTable() {
        List<String> all = new ArrayList<>();
        SQLiteDatabase database = openClipboardDatabase();
        Cursor cursor = null;
        String pinsContentItem;
        try {
            database.beginTransaction();
            cursor = database.rawQuery("select * from " + CLIPBOARD_PINS_TABLE, null);
            while (cursor.moveToNext()) {
                pinsContentItem = cursor.getString(cursor.getColumnIndex(CLIPBOARD_PINS_CONTENT_COLUMN_NAME));
                all.add(pinsContentItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                database.setTransactionSuccessful();
            }
        }
        database.endTransaction();
        Collections.reverse(all);
        HSLog.d(TAG, "AllPinsData database is  " + all.toString());
        return all;
    }

    private SQLiteDatabase openClipboardDatabase() {

        ClipboardSQLiteOpenHelper mDbHelper = ClipboardSQLiteOpenHelper.getInstance();
        return mDbHelper.getWritableDatabase();

    }

    //根据内容删除表项
    void deleteItemInTable(String item, int viewType) {

        SQLiteDatabase database = openClipboardDatabase();
        int isDelete = -1;
        try {
            if (viewType == ClipboardPresenter.RECENT_VIEW) {
                isDelete = database.delete(CLIPBOARD_RECENT_TABLE, CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{item});
                HSLog.d(TAG, "   deleteItem  =" + item + "      InRecentTable" + "  deleteResult   ==== " + isDelete);
            } else if (viewType == ClipboardPresenter.PINS_VIEW) {
                isDelete = database.delete(CLIPBOARD_PINS_TABLE, CLIPBOARD_PINS_CONTENT_COLUMN_NAME + "=?", new String[]{item});
                HSLog.d(TAG, "   deleteItem   =" + item + "        InPinsTable" + "       deleteResult   ==== " + isDelete);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //特定内容的表项置顶
    void setItemPositionToBottomInTable(String item, int viewType) {
        try {
            if (viewType == ClipboardPresenter.RECENT_VIEW) {
                deleteItemInTable(item, ClipboardPresenter.RECENT_VIEW);
                addItemToBottomInTable(item, ClipboardPresenter.RECENT_VIEW);
                HSLog.d(TAG, " change " + item + "posion to the bottom of recentTable");
            } else if (viewType == ClipboardPresenter.PINS_VIEW) {
                deleteItemInTable(item, ClipboardPresenter.PINS_VIEW);
                addItemToBottomInTable(item, ClipboardPresenter.PINS_VIEW);
                HSLog.d(TAG, " change " + item + "posion to the bottom of PinsTable");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //新增表项，内含判断保证size不超出最大值
    void addItemToBottomInTable(String item, int viewType) {
        SQLiteDatabase database = openClipboardDatabase();
        try {
            if (viewType == ClipboardPresenter.RECENT_VIEW) {
                boolean pined = queryItemExistsInTable(item, ClipboardPresenter.PINS_VIEW);
                int isPined = pined ? 1 : 0;
                ContentValues contentValues = new ContentValues();
                contentValues.put(CLIPBOARD_RECENT_CONTENT_COLUMN_NAME, item);
                contentValues.put(CLIPBOARD_RECENT_ISPINED_COLUMN_NAME, isPined);
                if (getRecentAllContentFromTable().size() == ClipboardPresenter.RECENT_TABLE_SIZE) {
                    deleteItemInTable(getRecentAllContentFromTable().get(ClipboardPresenter.RECENT_TABLE_SIZE - 1).recentClipItemContent, ClipboardPresenter.RECENT_VIEW);
                }
                long i = database.insert(CLIPBOARD_RECENT_TABLE, null, contentValues);
                HSLog.d(TAG, "addItem   " + item + "   to bottom of RecentTable");
            } else if (viewType == ClipboardPresenter.PINS_VIEW) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(CLIPBOARD_PINS_CONTENT_COLUMN_NAME, item);
                if (getPinsAllContentFromTable().size() == ClipboardPresenter.PINS_TABLE_SIZE) {
                    deleteItemInTable(getPinsAllContentFromTable().get(ClipboardPresenter.PINS_TABLE_SIZE - 1), ClipboardPresenter.PINS_VIEW);
                }
                long i = database.insert(CLIPBOARD_PINS_TABLE, null, contentValues);
                HSLog.d(TAG, "addItem   " + item + "   to bottom of PinsTable");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //查询表中某项内容是否存在
    boolean queryItemExistsInTable(String item, int viewType) {
        SQLiteDatabase database = openClipboardDatabase();
        Cursor cursor = null;
        try {
            if (viewType == ClipboardPresenter.RECENT_VIEW) {
                cursor = database.query(CLIPBOARD_RECENT_TABLE, new String[]{CLIPBOARD_RECENT_CONTENT_COLUMN_NAME}, CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{item}, null, null, null);
                boolean itemExists = cursor.moveToNext() && item.equals(cursor.getString(cursor.getColumnIndex(CLIPBOARD_RECENT_CONTENT_COLUMN_NAME)));
                HSLog.d(TAG, "query      " + item + "      isExistsInRecentTable    " + itemExists);
                return itemExists;
            } else if (viewType == ClipboardPresenter.PINS_VIEW) {
                cursor = database.query(CLIPBOARD_PINS_TABLE, new String[]{CLIPBOARD_PINS_CONTENT_COLUMN_NAME}, CLIPBOARD_PINS_CONTENT_COLUMN_NAME + "=?", new String[]{item}, null, null, null);
                boolean itemExists = cursor.moveToNext() && item.equals(cursor.getString(cursor.getColumnIndex(CLIPBOARD_PINS_CONTENT_COLUMN_NAME)));
                HSLog.d(TAG, "query     " + item + "       isExistsInPinsTable       " + itemExists);
                return itemExists;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor) cursor.close();
        }
        return false;
    }

    //标明Recent表里某项item已经不被收藏
    void updateRecentItemNoPinedInPinsTable(String item) {
        SQLiteDatabase database = openClipboardDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(CLIPBOARD_RECENT_ISPINED_COLUMN_NAME, item);
            values.put(CLIPBOARD_RECENT_ISPINED_COLUMN_NAME, 0);
            database.update(CLIPBOARD_RECENT_TABLE, values, CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{item});
            HSLog.d(TAG, item + "    updateRecentItem   " + item + "    NoPinedInPinsTable    ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}