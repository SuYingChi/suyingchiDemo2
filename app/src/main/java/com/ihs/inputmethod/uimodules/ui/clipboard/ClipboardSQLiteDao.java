package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.uimodules.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ihs.inputmethod.uimodules.ui.clipboard.ClipboardPresenter.RECENT_TABLE_SIZE;


//
public class ClipboardSQLiteDao implements ClipboardSQLiteOperate {

    private static final String CLIPBOARD_RECENT_TABLE = "clipboard_recent_table";
    private static final String CLIPBOARD_PINS_TABLE = "clipboard_pins_table";
    private static final String _ID = "_id";
    private static final String CLIPBOARD_RECENT_CONTENT_COLUMN_NAME = "clipboard_recent_content";
    private static final String CLIPBOARD_RECENT_ISPINED_COLUMN_NAME = "clipboard_recent_isPined";
    private static final String CLIPBOARD_PINS_CONTENT_COLUMN_NAME = "clipboard_PinS_content";
    private static volatile ClipboardSQLiteDao clipboardSQLiteDao;
    private final static String TAG = ClipboardSQLiteDao.class.getSimpleName();
    private OnClipboardDataBaseOperateFinishListener onDataBaseOperateFinishListener;

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

    @Override
    public
        //获取Recent表的被反转的list,
    List<ClipboardRecentViewAdapter.ClipboardRecentMessage> getRecentAllContentFromTable() {
        List<ClipboardRecentViewAdapter.ClipboardRecentMessage> all = new ArrayList<ClipboardRecentViewAdapter.ClipboardRecentMessage>();
        SQLiteDatabase database = openClipboardDatabase();
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

    @Override
    public
        //获取Pins表反转的list
    List<String> getPinsAllContentFromTable() {
        List<String> all = new ArrayList<>();
        SQLiteDatabase database = openClipboardDatabase();
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

    private SQLiteDatabase openClipboardDatabase() {

        ClipboardSQLiteOpenHelper mDbHelper = ClipboardSQLiteOpenHelper.getInstance();
        return mDbHelper.getWritableDatabase();

    }

    @Override
    public void deleteItemInRecentTable(String item) {
        SQLiteDatabase database = openClipboardDatabase();
        int isDelete = -1;
        try {
            isDelete = database.delete(CLIPBOARD_RECENT_TABLE, CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{item});
            HSLog.d(TAG, "   deleteItem  =" + item + "      InRecentTable" + "  deleteResult   ==== " + isDelete);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(HSApplication.getContext(), R.string.clipboard_database_operate_fail, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void deleteItemInPinsTable(String item) {
        SQLiteDatabase database = openClipboardDatabase();
        int isDelete = -1;
        try {
            isDelete = database.delete(CLIPBOARD_PINS_TABLE, CLIPBOARD_PINS_CONTENT_COLUMN_NAME + "=?", new String[]{item});
            HSLog.d(TAG, "   deleteItem   =" + item + "        InPinsTable" + "       deleteResult   ==== " + isDelete);
            if (onDataBaseOperateFinishListener != null) {
                onDataBaseOperateFinishListener.deletePinsItemSuccess();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(HSApplication.getContext(), R.string.clipboard_database_operate_fail, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void setItemPositionToBottomInRecentTable(String item) {
        SQLiteDatabase database = openClipboardDatabase();
        try {
            deleteItemInRecentTable(item);
            addItemToBottomInRecentTable(item);
            HSLog.d(TAG, " change " + item + "posion to the bottom of recentTable");
            if (onDataBaseOperateFinishListener != null) {
                onDataBaseOperateFinishListener.setRecentItemToTopSuccess();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(HSApplication.getContext(), R.string.clipboard_database_operate_fail, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void setItemPositionToBottomInPinsTable(String item) {
        SQLiteDatabase database = openClipboardDatabase();
        try {
            deleteItemInPinsTable(item);
            addItemToBottomInPinsTable(item);
            HSLog.d(TAG, " change " + item + "posion to the bottom of recentTable");
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(HSApplication.getContext(), R.string.clipboard_database_operate_fail, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void addItemToBottomInPinsTable(String item) {
        SQLiteDatabase database = openClipboardDatabase();
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(CLIPBOARD_PINS_CONTENT_COLUMN_NAME, item);
            if (getPinsAllContentFromTable().size() == ClipboardPresenter.PINS_TABLE_SIZE) {
                deleteItemInPinsTable(getPinsAllContentFromTable().get(ClipboardPresenter.PINS_TABLE_SIZE - 1));
            }
            long i = database.insert(CLIPBOARD_PINS_TABLE, null, contentValues);
            HSLog.d(TAG, "addItem   " + item + "   to bottom of PinsTable");
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(HSApplication.getContext(), R.string.clipboard_database_operate_fail, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void addItemToBottomInRecentTable(String item) {
        SQLiteDatabase database = openClipboardDatabase();
        try {
            boolean pined = queryItemExistsInPinsTable(item);
            int isPined = pined ? 1 : 0;
            ContentValues contentValues = new ContentValues();
            contentValues.put(CLIPBOARD_RECENT_CONTENT_COLUMN_NAME, item);
            contentValues.put(CLIPBOARD_RECENT_ISPINED_COLUMN_NAME, isPined);
            if (getRecentAllContentFromTable().size() == RECENT_TABLE_SIZE) {
                deleteItemInRecentTable(getRecentAllContentFromTable().get(RECENT_TABLE_SIZE - 1).recentClipItemContent);
            }
            long i = database.insert(CLIPBOARD_RECENT_TABLE, null, contentValues);
            HSLog.d(TAG, "addItem   " + item + "   to bottom of RecentTable");
            if (onDataBaseOperateFinishListener != null) {
                onDataBaseOperateFinishListener.addRecentItemSuccess();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(HSApplication.getContext(), R.string.clipboard_database_operate_fail, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean queryItemExistsInRecentTable(String item) {
        SQLiteDatabase database = openClipboardDatabase();
        Cursor cursor = null;
        try {
            cursor = database.query(CLIPBOARD_RECENT_TABLE, new String[]{CLIPBOARD_RECENT_CONTENT_COLUMN_NAME}, CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{item}, null, null, null);
            boolean itemExists = cursor.moveToNext() && item.equals(cursor.getString(cursor.getColumnIndex(CLIPBOARD_RECENT_CONTENT_COLUMN_NAME)));
            HSLog.d(TAG, "query      " + item + "      isExistsInRecentTable    " + itemExists);
            return itemExists;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(HSApplication.getContext(), R.string.clipboard_database_operate_fail, Toast.LENGTH_SHORT).show();
        } finally {
            if (null != cursor) cursor.close();
        }
        return false;

    }

    @Override
    public boolean queryItemExistsInPinsTable(String item) {

        SQLiteDatabase database = openClipboardDatabase();
        Cursor cursor = null;
        try {
            cursor = database.query(CLIPBOARD_PINS_TABLE, new String[]{CLIPBOARD_PINS_CONTENT_COLUMN_NAME}, CLIPBOARD_PINS_CONTENT_COLUMN_NAME + "=?", new String[]{item}, null, null, null);
            boolean itemExists = cursor.moveToNext() && item.equals(cursor.getString(cursor.getColumnIndex(CLIPBOARD_PINS_CONTENT_COLUMN_NAME)));
            HSLog.d(TAG, "query     " + item + "       isExistsInPinsTable       " + itemExists);
            return itemExists;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(HSApplication.getContext(), R.string.clipboard_database_operate_fail, Toast.LENGTH_SHORT).show();
        } finally {
            if (null != cursor) cursor.close();
        }
        return false;
    }

    @Override
    //实时监听用户新增复制内容时的操作
    public void clipDataOperateAddRecent(String item) {
        //用户新增recent数据，小于最大条数并且内容未与recent重复增加新内容,添加并置顶
        if (getRecentAllContentFromTable().size() <= RECENT_TABLE_SIZE & !queryItemExistsInRecentTable(item)) {
            ClipboardSQLiteDao.getInstance().addItemToBottomInRecentTable(item);
        }
        //用户新增recent数据，与recent已有内容重复，则置顶重复内容
        else if (queryItemExistsInRecentTable(item)) {
            ClipboardSQLiteDao.getInstance().setItemPositionToBottomInRecentTable(item);
        }
    }

    @Override
    public void deleteRecentItemAndSetItemPositionToBottomInPins(String item) {
        SQLiteDatabase database = openClipboardDatabase();
        int isDelete = -1;
        database.beginTransaction();
        try {
            isDelete = database.delete(CLIPBOARD_RECENT_TABLE, CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{item});
            HSLog.d(TAG, "   deleteItem  =" + item + "      InRecentTable" + "  deleteResult   ==== " + isDelete);
            isDelete = database.delete(CLIPBOARD_PINS_TABLE, CLIPBOARD_PINS_CONTENT_COLUMN_NAME + "=?", new String[]{item});
            HSLog.d(TAG, "   deleteItem   =" + item + "        InPinsTable" + "       deleteResult   ==== " + isDelete);
            ContentValues contentValues = new ContentValues();
            contentValues.put(CLIPBOARD_PINS_CONTENT_COLUMN_NAME, item);
            if (getPinsAllContentFromTable().size() == ClipboardPresenter.PINS_TABLE_SIZE) {
                deleteItemInPinsTable(getPinsAllContentFromTable().get(ClipboardPresenter.PINS_TABLE_SIZE - 1));
            }
            long i = database.insert(CLIPBOARD_PINS_TABLE, null, contentValues);
            HSLog.d(TAG, "addItem   " + item + "   to bottom of PinsTable");
            if (onDataBaseOperateFinishListener != null) {
                onDataBaseOperateFinishListener.deleteRecentItemAndSetItemPositionToBottomInPins();
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(HSApplication.getContext(), R.string.clipboard_database_operate_fail, Toast.LENGTH_SHORT).show();
        } finally {
            database.endTransaction();
        }
    }

    @Override
    public void deleteRecentItemAndAddToPins(String item) {
        SQLiteDatabase database = openClipboardDatabase();
        int isDelete = -1;
        database.beginTransaction();
        try {
            isDelete = database.delete(CLIPBOARD_RECENT_TABLE, CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{item});
            HSLog.d(TAG, "   deleteItem  =" + item + "      InRecentTable" + "  deleteResult   ==== " + isDelete);
            ContentValues contentValues = new ContentValues();
            contentValues.put(CLIPBOARD_PINS_CONTENT_COLUMN_NAME, item);
            if (getPinsAllContentFromTable().size() == ClipboardPresenter.PINS_TABLE_SIZE) {
                deleteItemInPinsTable(getPinsAllContentFromTable().get(ClipboardPresenter.PINS_TABLE_SIZE - 1));
            }
            long i = database.insert(CLIPBOARD_PINS_TABLE, null, contentValues);
            HSLog.d(TAG, "addItem   " + item + "   to bottom of PinsTable");
            if (onDataBaseOperateFinishListener != null) {
                onDataBaseOperateFinishListener.deleteRecentItemAndAddToPins();
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(HSApplication.getContext(), R.string.clipboard_database_operate_fail, Toast.LENGTH_SHORT).show();
        } finally {
            database.endTransaction();
        }
    }

    @Override
    public void deletePinsItemAndUpdateRecentItemNoPined(String item) {
        SQLiteDatabase database = openClipboardDatabase();
        int isDelete = -1;
        database.beginTransaction();
        try {
            isDelete = database.delete(CLIPBOARD_PINS_TABLE, CLIPBOARD_PINS_CONTENT_COLUMN_NAME + "=?", new String[]{item});
            HSLog.d(TAG, "   deleteItem   =" + item + "        InPinsTable" + "       deleteResult   ==== " + isDelete);
            ContentValues values = new ContentValues();
            values.put(CLIPBOARD_RECENT_ISPINED_COLUMN_NAME, item);
            values.put(CLIPBOARD_RECENT_ISPINED_COLUMN_NAME, 0);
            database.update(CLIPBOARD_RECENT_TABLE, values, CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{item});
            HSLog.d(TAG, item + "    updateRecentItem   " + item + "    NoPinedInPinsTable    ");
            if (onDataBaseOperateFinishListener != null) {
                onDataBaseOperateFinishListener.deletePinsItemAndUpdateRecentItemNoPined();
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(HSApplication.getContext(), R.string.clipboard_database_operate_fail, Toast.LENGTH_SHORT).show();
        } finally {
            database.endTransaction();
        }
    }

    void setOnDataBaseOperateFinishListener(OnClipboardDataBaseOperateFinishListener onDataBaseOperateFinishListener) {
        this.onDataBaseOperateFinishListener = onDataBaseOperateFinishListener;
    }
}