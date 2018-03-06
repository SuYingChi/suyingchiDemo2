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
public class ClipboardDataBaseOperateImpl implements ClipboardSQLiteOperate {

    private static final String CLIPBOARD_RECENT_TABLE = "clipboard_recent_table";
    private static final String CLIPBOARD_PINS_TABLE = "clipboard_pins_table";
    private static final String _ID = "_id";
    private static final String CLIPBOARD_RECENT_CONTENT_COLUMN_NAME = "clipboard_recent_content";
    private static final String CLIPBOARD_RECENT_ISPINED_COLUMN_NAME = "clipboard_recent_isPined";
    private static final String CLIPBOARD_PINS_CONTENT_COLUMN_NAME = "clipboard_PinS_content";
    private static volatile ClipboardDataBaseOperateImpl clipboardDataBaseOperateImpl;
    private final static String TAG = ClipboardDataBaseOperateImpl.class.getSimpleName();
    private OnClipboardDataBaseOperateFinishListener onDataBaseOperateFinishListener;

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
            Toast.makeText(HSApplication.getContext(), R.string.clipboard_database_operate_fail, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(HSApplication.getContext(), R.string.clipboard_database_operate_fail, Toast.LENGTH_SHORT).show();
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
            if (isDelete == 0 && onDataBaseOperateFinishListener != null) {
                onDataBaseOperateFinishListener.clipboardDataBaseOperateFail();
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (onDataBaseOperateFinishListener != null) {
                onDataBaseOperateFinishListener.clipboardDataBaseOperateFail();
            }
        }
    }

    @Override
    public void deleteItemInPinsTable(String item) {
        SQLiteDatabase database = openClipboardDatabase();
        int isDelete = -1;
        try {
            isDelete = database.delete(CLIPBOARD_PINS_TABLE, CLIPBOARD_PINS_CONTENT_COLUMN_NAME + "=?", new String[]{item});
            HSLog.d(TAG, "   deleteItem   =" + item + "        InPinsTable" + "       deleteResult   ==== " + isDelete);
            if (onDataBaseOperateFinishListener != null && isDelete == 0) {
                onDataBaseOperateFinishListener.clipboardDataBaseOperateFail();
            } else if (onDataBaseOperateFinishListener != null && isDelete == 1) {
                onDataBaseOperateFinishListener.deletePinsItemSuccess();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (onDataBaseOperateFinishListener != null) {
                onDataBaseOperateFinishListener.clipboardDataBaseOperateFail();
            }
        }
    }

    @Override
    public void setItemPositionToBottomInRecentTable(String item) {
        SQLiteDatabase database = openClipboardDatabase();
        database.beginTransaction();
        int position;
        ClipboardRecentViewAdapter.ClipboardRecentMessage lastRecent;
        try {
            int deleteRow = -1;
            position = queryItemInRecentTableReversePosition(item);
            lastRecent = getRecentAllContentFromTable().get(position);
            deleteRow = database.delete(CLIPBOARD_RECENT_TABLE, CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{item});
            HSLog.d(TAG, "   deleteItem  =" + item + "      InRecentTable" + "  deleteResult   ==== " + deleteRow);
            if (deleteRow == 0) {
                Toast.makeText(HSApplication.getContext(), R.string.clipboard_database_operate_fail, Toast.LENGTH_SHORT).show();
                return;
            }
            boolean pined = queryItemExistsInPinsTable(item);
            int isPined = pined ? 1 : 0;
            ContentValues contentValues = new ContentValues();
            contentValues.put(CLIPBOARD_RECENT_CONTENT_COLUMN_NAME, item);
            contentValues.put(CLIPBOARD_RECENT_ISPINED_COLUMN_NAME, isPined);
            if (getRecentAllContentFromTable().size() == RECENT_TABLE_SIZE) {
                deleteItemInRecentTable(getRecentAllContentFromTable().get(RECENT_TABLE_SIZE - 1).recentClipItemContent);
            }
            long insertResult = database.insert(CLIPBOARD_RECENT_TABLE, null, contentValues);
            HSLog.d(TAG, "addItem   " + item + "   to bottom of RecentTable");
            if (insertResult == -1) {
                Toast.makeText(HSApplication.getContext(), R.string.clipboard_database_operate_fail, Toast.LENGTH_SHORT).show();
                return;
            }
            if (onDataBaseOperateFinishListener != null) {
                onDataBaseOperateFinishListener.setRecentItemToTopSuccess(lastRecent, position);
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(HSApplication.getContext(), R.string.clipboard_database_operate_fail, Toast.LENGTH_SHORT).show();
        } finally {
            database.endTransaction();
        }
    }

    public int queryItemInRecentTableReversePosition(String item) {

        SQLiteDatabase database = openClipboardDatabase();
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
            Toast.makeText(HSApplication.getContext(), R.string.clipboard_database_operate_fail, Toast.LENGTH_SHORT).show();
            HSLog.d(TAG, "queryItemInRecentTableReversePosition  Exception ");
        } finally {
            if (null != cursor) cursor.close();
        }
        return -1;
    }

    public int queryItemInPinsTableReversePosition(String item) {

        SQLiteDatabase database = openClipboardDatabase();
        Cursor cursor = null;
        String pinsContentItem = "";
        int position = 0;
        try {
            cursor = database.query(CLIPBOARD_PINS_TABLE, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                pinsContentItem = cursor.getString(cursor.getColumnIndex(CLIPBOARD_PINS_CONTENT_COLUMN_NAME));
                if (item.equals(pinsContentItem)) {
                    position = getRecentAllContentFromTable().size() - position - 1;
                    HSLog.d(TAG, "queryItemInPinsTableReversePosition-----" + "----item---" + item + "------position-----" + position);
                    return position;
                }
                position++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(HSApplication.getContext(), R.string.clipboard_database_operate_fail, Toast.LENGTH_SHORT).show();
            HSLog.d(TAG, "queryItemInPinsTableReversePosition  Exception ");
        } finally {
            if (null != cursor) cursor.close();
        }
        return -1;
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
            long insertResult = database.insert(CLIPBOARD_RECENT_TABLE, null, contentValues);
            HSLog.d(TAG, "addItem   " + item + "   to bottom of RecentTable  " + "  insertResult " + insertResult);
            if (insertResult == -1) {
                //View不管是否在显示，都需要弹toast，所以这里直接弹，不需要通过onDataBaseOperateFinishListener回调失败接口
                Toast.makeText(HSApplication.getContext(), R.string.clipboard_database_operate_fail, Toast.LENGTH_SHORT).show();
                return;
            }
            //view在显示则回调刷新界面
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
    public void deleteRecentItemAndSetItemPositionToBottomInPins(String item) {
        SQLiteDatabase database = openClipboardDatabase();
        int isDeleteRECENT = -1;
        int isDeletePins = -1;
        int lastPosition = -1;
        database.beginTransaction();
        try {
            lastPosition = queryItemInPinsTableReversePosition(item);
            isDeleteRECENT = database.delete(CLIPBOARD_RECENT_TABLE, CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{item});
            HSLog.d(TAG, "   deleteItem  =" + item + "      InRecentTable" + "  deleteResult   ==== " + isDeleteRECENT);
            if (isDeleteRECENT == 0 && onDataBaseOperateFinishListener != null) {
                onDataBaseOperateFinishListener.clipboardDataBaseOperateFail();
                return;
            }
            isDeletePins = database.delete(CLIPBOARD_PINS_TABLE, CLIPBOARD_PINS_CONTENT_COLUMN_NAME + "=?", new String[]{item});
            HSLog.d(TAG, "   deleteItem   =" + item + "        InPinsTable" + "       deleteResult   ==== " + isDeletePins);
            if (isDeletePins == 0 && onDataBaseOperateFinishListener != null) {
                onDataBaseOperateFinishListener.clipboardDataBaseOperateFail();
                return;
            }

            ContentValues contentValues = new ContentValues();
            contentValues.put(CLIPBOARD_PINS_CONTENT_COLUMN_NAME, item);
            if (getPinsAllContentFromTable().size() == ClipboardPresenter.PINS_TABLE_SIZE) {
                deleteItemInPinsTable(getPinsAllContentFromTable().get(ClipboardPresenter.PINS_TABLE_SIZE - 1));
            }
            long insertResult = database.insert(CLIPBOARD_PINS_TABLE, null, contentValues);
            HSLog.d(TAG, "addItem   " + item + "   to bottom of PinsTable " + "  insertResult  " + insertResult);
            if (insertResult == -1 && onDataBaseOperateFinishListener != null) {
                onDataBaseOperateFinishListener.clipboardDataBaseOperateFail();
                return;
            }

            if (onDataBaseOperateFinishListener != null) {
                onDataBaseOperateFinishListener.deleteRecentItemAndSetItemPositionToBottomInPins(lastPosition);
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
            if (isDelete == 0 && onDataBaseOperateFinishListener != null) {
                onDataBaseOperateFinishListener.clipboardDataBaseOperateFail();
                return;
            }
            HSLog.d(TAG, "   deleteItem  =" + item + "      InRecentTable" + "  deleteResult   ==== " + isDelete);
            ContentValues contentValues = new ContentValues();
            contentValues.put(CLIPBOARD_PINS_CONTENT_COLUMN_NAME, item);
            if (getPinsAllContentFromTable().size() == ClipboardPresenter.PINS_TABLE_SIZE) {
                deleteItemInPinsTable(getPinsAllContentFromTable().get(ClipboardPresenter.PINS_TABLE_SIZE - 1));
            }
            long insertResult = database.insert(CLIPBOARD_PINS_TABLE, null, contentValues);
            HSLog.d(TAG, "addItem   " + item + "   to bottom of PinsTable" + "insert result " + insertResult);
            if (insertResult == -1 && onDataBaseOperateFinishListener != null) {
                onDataBaseOperateFinishListener.clipboardDataBaseOperateFail();
                return;
            }
            if (onDataBaseOperateFinishListener != null) {
                onDataBaseOperateFinishListener.deleteRecentItemAndAddToPins();
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            if (onDataBaseOperateFinishListener != null) {
                onDataBaseOperateFinishListener.clipboardDataBaseOperateFail();
            }
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
            if (isDelete == 0 && onDataBaseOperateFinishListener != null) {
                onDataBaseOperateFinishListener.clipboardDataBaseOperateFail();
                return;
            }
            ContentValues values = new ContentValues();
            values.put(CLIPBOARD_RECENT_ISPINED_COLUMN_NAME, item);
            values.put(CLIPBOARD_RECENT_ISPINED_COLUMN_NAME, 0);
            int updateResult = database.update(CLIPBOARD_RECENT_TABLE, values, CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{item});
            if (updateResult < 0 && onDataBaseOperateFinishListener != null) {
                onDataBaseOperateFinishListener.clipboardDataBaseOperateFail();
                return;
            }
            HSLog.d(TAG, item + "    updateRecentItem   " + item + "    NoPinedInPinsTable    ");
            if (onDataBaseOperateFinishListener != null) {
                onDataBaseOperateFinishListener.deletePinsItemAndUpdateRecentItemNoPined(queryItemInRecentTableReversePosition(item));
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            if (onDataBaseOperateFinishListener != null) {
                onDataBaseOperateFinishListener.clipboardDataBaseOperateFail();
            }
        } finally {
            database.endTransaction();
        }
    }

    public void setOnDataBaseOperateFinishListener(OnClipboardDataBaseOperateFinishListener onDataBaseOperateFinishListener) {
        this.onDataBaseOperateFinishListener = onDataBaseOperateFinishListener;
    }
}