package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ihs.inputmethod.uimodules.ui.clipboard.ClipboardPresenter.RECENT_TABLE_SIZE;


//
public class ClipboardDataBaseOperateImpl implements ClipboardContact.ClipboardSQLiteOperate {

    private static final String CLIPBOARD_RECENT_TABLE = "clipboard_recent_table";
    private static final String CLIPBOARD_PINS_TABLE = "clipboard_pins_table";
    private static final String _ID = "_id";
    private static final String CLIPBOARD_RECENT_CONTENT_COLUMN_NAME = "clipboard_recent_content";
    private static final String CLIPBOARD_RECENT_ISPINED_COLUMN_NAME = "clipboard_recent_isPined";
    private static final String CLIPBOARD_PINS_CONTENT_COLUMN_NAME = "clipboard_PinS_content";
    private static ClipboardDataBaseOperateImpl clipboardDataBaseOperateImpl;
    private final static String TAG = ClipboardDataBaseOperateImpl.class.getSimpleName();
    private  SQLiteDatabase database;

    public static ClipboardDataBaseOperateImpl getInstance() {
        if (clipboardDataBaseOperateImpl == null) {
            synchronized (ClipboardDataBaseOperateImpl.class) {
                if (null == clipboardDataBaseOperateImpl) {
                    clipboardDataBaseOperateImpl = new ClipboardDataBaseOperateImpl();
                }
            }
        }
        return clipboardDataBaseOperateImpl;
    }

   private ClipboardDataBaseOperateImpl(){

       ClipboardSQLiteOpenHelper mDbHelper = ClipboardSQLiteOpenHelper.getInstance();
       database = mDbHelper.getWritableDatabase();
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

    @Override
    //获取Recent表的被反转的list,
    public List<ClipboardRecentViewAdapter.ClipboardRecentMessage> getRecentAllContentFromTable() {
        List<ClipboardRecentViewAdapter.ClipboardRecentMessage> all = new ArrayList<ClipboardRecentViewAdapter.ClipboardRecentMessage>();
        Cursor cursor = null;
        String recentContentItem;
        int isPined;
        ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage;
        try {
            cursor = database.query(CLIPBOARD_RECENT_TABLE, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                recentContentItem = cursor.getString(cursor.getColumnIndex(CLIPBOARD_RECENT_CONTENT_COLUMN_NAME));
                isPined = cursor.getInt(cursor.getColumnIndex(CLIPBOARD_RECENT_ISPINED_COLUMN_NAME));
                clipboardRecentMessage = new ClipboardRecentViewAdapter.ClipboardRecentMessage(recentContentItem, isPined);
                all.add(clipboardRecentMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Collections.reverse(all);
        HSLog.d(TAG, "AllRecentData in database is  " + all.toString());
        return all;
    }

    //获取Pins表反转的list
    @Override
    public List<String> getPinsAllContentFromTable() {
        List<String> all = new ArrayList<>();
        Cursor cursor = null;
        String pinsContentItem;
        try {
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
            }
        }
        Collections.reverse(all);
        HSLog.d(TAG, "AllPinsData database is  " + all.toString());
        return all;
    }


    @Override
    public boolean deleteItemInPinsTable(String item) {
        int isDelete = -1;
        try {
            isDelete = database.delete(CLIPBOARD_PINS_TABLE, CLIPBOARD_PINS_CONTENT_COLUMN_NAME + "=?", new String[]{item});
            HSLog.d(TAG, "   deleteItem   =" + item + "        InPinsTable" + "       deleteResult   ==== " + isDelete);
            if (isDelete == 0) {
                return false;
            } else if (isDelete == 1) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean setItemPositionToBottomInRecentTable(String item, int isPined) {
        database.beginTransaction();
        try {
            int deleteRow = -1;
            deleteRow = database.delete(CLIPBOARD_RECENT_TABLE, CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{item});
            HSLog.d(TAG, "   deleteItem  =" + item + "      InRecentTable" + "  deleteResult   ==== " + deleteRow);
            if (deleteRow == 0) {
                return false;
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(CLIPBOARD_RECENT_CONTENT_COLUMN_NAME, item);
            contentValues.put(CLIPBOARD_RECENT_ISPINED_COLUMN_NAME, isPined);
            long insertResult = database.insert(CLIPBOARD_RECENT_TABLE, null, contentValues);
            HSLog.d(TAG, "addItem   " + item + "   to bottom of RecentTable");
            if (insertResult == -1) {
                return false;
            }
            database.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            database.endTransaction();
        }
    }

    public int queryItemInRecentTableReversePosition(String item) {

        Cursor cursor = null;
        String recentContentItem = "";
        int position = 0;
        try {
            cursor = database.query(CLIPBOARD_RECENT_TABLE, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                recentContentItem = cursor.getString(cursor.getColumnIndex(CLIPBOARD_RECENT_CONTENT_COLUMN_NAME));
                if (item.equals(recentContentItem)) {
                    position = getRecentAllContentFromTable().size() - position - 1;
                    HSLog.d(TAG, "queryItemInRecentTableReversePosition-----" + "----item---" + item + "------position-----" + position);
                    return position;
                }
                position++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            HSLog.d(TAG, "queryItemInRecentTableReversePosition  Exception ");
        } finally {
            if (null != cursor) cursor.close();
        }
        return -1;
    }

    public int queryItemInPinsTableReversePosition(String item) {

        Cursor cursor = null;
        String pinsContentItem = "";
        int position = 0;
        try {
            cursor = database.query(CLIPBOARD_PINS_TABLE, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                pinsContentItem = cursor.getString(cursor.getColumnIndex(CLIPBOARD_PINS_CONTENT_COLUMN_NAME));
                if (item.equals(pinsContentItem)) {
                    position = getPinsAllContentFromTable().size() - position - 1;
                    HSLog.d(TAG, "database    queryItemInPinsTableReversePosition-----" + "----item---" + item + "------position-----" + position);
                    return position;
                }
                position++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            HSLog.d(TAG, "queryItemInPinsTableReversePosition  Exception ");
        } finally {
            if (null != cursor) cursor.close();
        }
        return -1;
    }

    @Override
    public ClipboardRecentViewAdapter.ClipboardRecentMessage getRecentItemFromTable(String item) {
        Cursor cursor = null;
        try {
            cursor = database.query(CLIPBOARD_RECENT_TABLE, new String[]{CLIPBOARD_RECENT_CONTENT_COLUMN_NAME, CLIPBOARD_RECENT_ISPINED_COLUMN_NAME}, CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{item}, null, null, null);
            if (cursor.moveToNext()) {
                int itemExists = cursor.getInt(cursor.getColumnIndex(CLIPBOARD_RECENT_ISPINED_COLUMN_NAME));
                return new ClipboardRecentViewAdapter.ClipboardRecentMessage(item, itemExists);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (null != cursor) cursor.close();
        }
    }

    boolean addItemToBottomInRecentTable(String item, int isPined, int currentRecentSize) {
        database.beginTransaction();
        Cursor cursor = null;
        try {
            //删除表里第一条数据
            if (currentRecentSize == RECENT_TABLE_SIZE) {
                cursor = database.query(CLIPBOARD_RECENT_TABLE,null,null,null,null,null,null);
                if(cursor.moveToFirst()){
                   String firstItem = cursor.getString(cursor.getColumnIndex(CLIPBOARD_RECENT_CONTENT_COLUMN_NAME));
                    int isDelete = database.delete(CLIPBOARD_RECENT_TABLE, CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{firstItem});
                    HSLog.d(TAG, "   deleteItem  =" + item + "      InRecentTable" + "  deleteResult   ==== " + isDelete);
                    if (isDelete == 0) {
                        return false;
                    }
                }else{
                    return false;
                }
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(CLIPBOARD_RECENT_CONTENT_COLUMN_NAME, item);
            contentValues.put(CLIPBOARD_RECENT_ISPINED_COLUMN_NAME, isPined);
            long insertResult = database.insert(CLIPBOARD_RECENT_TABLE, null, contentValues);
            HSLog.d(TAG, "addItem   " + item + "   to bottom of RecentTable  " + "  insertResult " + insertResult);
            if (insertResult == -1) {
                return false;
            }
            database.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            database.endTransaction();
        }
    }

    @Override
    public boolean queryItemExistsInRecentTable(String item) {
        Cursor cursor = null;
        try {
            cursor = database.query(CLIPBOARD_RECENT_TABLE, new String[]{CLIPBOARD_RECENT_CONTENT_COLUMN_NAME}, CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{item}, null, null, null);
            boolean itemExists = cursor.moveToNext() && item.equals(cursor.getString(cursor.getColumnIndex(CLIPBOARD_RECENT_CONTENT_COLUMN_NAME)));
            HSLog.d(TAG, "query      " + item + "      isExistsInRecentTable    " + itemExists);
            return itemExists;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor) cursor.close();
        }
        return false;

    }

    @Override
    public int queryItemExistsInPinsTable(String item) {
        Cursor cursor = null;
        try {
            cursor = database.query(CLIPBOARD_PINS_TABLE, new String[]{CLIPBOARD_PINS_CONTENT_COLUMN_NAME}, CLIPBOARD_PINS_CONTENT_COLUMN_NAME + "=?", new String[]{item}, null, null, null);
            boolean itemExists = cursor.moveToNext() && item.equals(cursor.getString(cursor.getColumnIndex(CLIPBOARD_PINS_CONTENT_COLUMN_NAME)));
            HSLog.d(TAG, "query     " + item + "       isExistsInPinsTable       " + itemExists);
            return itemExists ? 1 : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            if (null != cursor) cursor.close();
        }
    }

    @Override
    public boolean deleteRecentItemAndSetItemPositionToBottomInPins(String item) {
        int isDeleteRECENT = -1;
        int isDeletePins = -1;
        database.beginTransaction();
        try {
            isDeleteRECENT = database.delete(CLIPBOARD_RECENT_TABLE, CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{item});
            HSLog.d(TAG, "   deleteItem  =" + item + "      InRecentTable" + "  deleteResult   ==== " + isDeleteRECENT);
            if (isDeleteRECENT == 0) {
                return false;
            }
            isDeletePins = database.delete(CLIPBOARD_PINS_TABLE, CLIPBOARD_PINS_CONTENT_COLUMN_NAME + "=?", new String[]{item});
            HSLog.d(TAG, "   deleteItem   =" + item + "        InPinsTable" + "       deleteResult   ==== " + isDeletePins);
            if (isDeletePins == 0) {
                return false;
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(CLIPBOARD_PINS_CONTENT_COLUMN_NAME, item);
            long insertResult = database.insert(CLIPBOARD_PINS_TABLE, null, contentValues);
            HSLog.d(TAG, "addItem   " + item + "   to bottom of PinsTable " + "  insertResult  " + insertResult);
            if (insertResult == -1) {
                return false;
            }
            database.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            database.endTransaction();
        }
    }

    @Override
    public boolean deleteRecentItemAndAddToPins(String item) {
        int isDelete = -1;
        database.beginTransaction();
        try {
            isDelete = database.delete(CLIPBOARD_RECENT_TABLE, CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{item});
            if (isDelete == 0) {
                return false;
            }
            HSLog.d(TAG, "   deleteItem  =" + item + "      InRecentTable" + "  deleteResult   ==== " + isDelete);
            ContentValues contentValues = new ContentValues();
            contentValues.put(CLIPBOARD_PINS_CONTENT_COLUMN_NAME, item);
            long insertResult = database.insert(CLIPBOARD_PINS_TABLE, null, contentValues);
            HSLog.d(TAG, "addItem   " + item + "   to bottom of PinsTable" + "insert result " + insertResult);
            if (insertResult == -1) {
                return false;
            }
            database.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            database.endTransaction();
        }
    }

    @Override
    public boolean deletePinsItemAndUpdateRecentItemNoPined(String item) {
        int isDelete = -1;
        database.beginTransaction();
        try {
            isDelete = database.delete(CLIPBOARD_PINS_TABLE, CLIPBOARD_PINS_CONTENT_COLUMN_NAME + "=?", new String[]{item});
            HSLog.d(TAG, "   deleteItem   =" + item + "        InPinsTable" + "       deleteResult   ==== " + isDelete);
            if (isDelete == 0) {
                return false;
            }
            ContentValues values = new ContentValues();
            values.put(CLIPBOARD_RECENT_ISPINED_COLUMN_NAME, item);
            values.put(CLIPBOARD_RECENT_ISPINED_COLUMN_NAME, 0);
            int updateResult = database.update(CLIPBOARD_RECENT_TABLE, values, CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{item});
            if (updateResult < 0) {
                return false;
            }
            HSLog.d(TAG, item + "    updateRecentItem   " + item + "    NoPinedInPinsTable    ");
            database.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            database.endTransaction();
        }
    }
}