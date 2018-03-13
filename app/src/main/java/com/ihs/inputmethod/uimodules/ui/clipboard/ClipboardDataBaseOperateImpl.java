package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ClipboardDataBaseOperateImpl implements ClipboardContract.ClipboardSQLiteOperate {


    private static volatile ClipboardDataBaseOperateImpl clipboardDataBaseOperateImpl;
    private final static String TAG = ClipboardDataBaseOperateImpl.class.getSimpleName();
    private SQLiteDatabase database = ClipboardSQLiteOpenHelper.getInstance().getWritableDatabase();

    public static ClipboardDataBaseOperateImpl getInstance() {
        if (clipboardDataBaseOperateImpl == null) {
            synchronized (ClipboardDataBaseOperateImpl.class) {
                if (clipboardDataBaseOperateImpl == null) {
                    clipboardDataBaseOperateImpl = new ClipboardDataBaseOperateImpl();
                }
            }
        }
        return clipboardDataBaseOperateImpl;
    }


    @Override
    //获取Recent表的被反转的list,
    public List<ClipboardRecentViewAdapter.ClipboardRecentMessage> getRecentAllContentList() {
        List<ClipboardRecentViewAdapter.ClipboardRecentMessage> all = new ArrayList<ClipboardRecentViewAdapter.ClipboardRecentMessage>();
        Cursor cursor = null;
        String recentContentItem;
        int isPined;
        ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage;
        try {
            cursor = database.query(ClipboardConstants.CLIPBOARD_RECENT_TABLE, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                recentContentItem = cursor.getString(cursor.getColumnIndex(ClipboardConstants.CLIPBOARD_RECENT_CONTENT_COLUMN_NAME));
                isPined = cursor.getInt(cursor.getColumnIndex(ClipboardConstants.CLIPBOARD_RECENT_ISPINED_COLUMN_NAME));
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
    public List<String> getPinsAllContentList() {
        List<String> all = new ArrayList<>();
        Cursor cursor = null;
        String pinsContentItem;
        try {
            cursor = database.rawQuery("select * from " + ClipboardConstants.CLIPBOARD_PINS_TABLE, null);
            while (cursor.moveToNext()) {
                pinsContentItem = cursor.getString(cursor.getColumnIndex(ClipboardConstants.CLIPBOARD_PINS_CONTENT_COLUMN_NAME));
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
    public boolean deletePinItem(String deletePinItem) {
        int isDelete = -1;
        try {
            isDelete = database.delete(ClipboardConstants.CLIPBOARD_PINS_TABLE, ClipboardConstants.CLIPBOARD_PINS_CONTENT_COLUMN_NAME + "=?", new String[]{deletePinItem});
            HSLog.d(TAG, "   deleteItem   =" + deletePinItem + "        InPinsTable" + "       deleteResult   ==== " + isDelete);
            return isDelete > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean moveExistRecentItemToBottom(String existRecentItem, int isPined) {
        database.beginTransaction();
        try {
            int deleteRow = -1;
            deleteRow = database.delete(ClipboardConstants.CLIPBOARD_RECENT_TABLE, ClipboardConstants.CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{existRecentItem});
            HSLog.d(TAG, "   deleteItem  =" + existRecentItem + "      InRecentTable" + "  deleteResult   ==== " + deleteRow);
            if (deleteRow == 0) {
                return false;
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(ClipboardConstants.CLIPBOARD_RECENT_CONTENT_COLUMN_NAME, existRecentItem);
            contentValues.put(ClipboardConstants.CLIPBOARD_RECENT_ISPINED_COLUMN_NAME, isPined);
            long insertResult = database.insert(ClipboardConstants.CLIPBOARD_RECENT_TABLE, null, contentValues);
            HSLog.d(TAG, "addItem   " + existRecentItem + "   to bottom of RecentTable");
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
    public ClipboardRecentViewAdapter.ClipboardRecentMessage getRecentItem(String recentStringItem) {
        Cursor cursor = null;
        try {
            cursor = database.query(ClipboardConstants.CLIPBOARD_RECENT_TABLE, new String[]{ClipboardConstants.CLIPBOARD_RECENT_CONTENT_COLUMN_NAME, ClipboardConstants.CLIPBOARD_RECENT_ISPINED_COLUMN_NAME}, ClipboardConstants.CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{recentStringItem}, null, null, null);
            if (cursor.moveToNext()) {
                int itemExists = cursor.getInt(cursor.getColumnIndex(ClipboardConstants.CLIPBOARD_RECENT_ISPINED_COLUMN_NAME));
                return new ClipboardRecentViewAdapter.ClipboardRecentMessage(recentStringItem, itemExists);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (null != cursor) cursor.close();
        }
    }

    @Override
    public boolean insertRecentItem(String insertRecentItem, int isPined, int currentRecentSize) {
        database.beginTransaction();
        Cursor cursor = null;
        try {
            //删除表里第一条数据
            if (currentRecentSize == ClipboardConstants.RECENT_TABLE_SIZE) {
                cursor = database.query(ClipboardConstants.CLIPBOARD_RECENT_TABLE, null, null, null, null, null, null);
                if (cursor.moveToFirst()) {
                    String firstItem = cursor.getString(cursor.getColumnIndex(ClipboardConstants.CLIPBOARD_RECENT_CONTENT_COLUMN_NAME));
                    int isDelete = database.delete(ClipboardConstants.CLIPBOARD_RECENT_TABLE, ClipboardConstants.CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{firstItem});
                    HSLog.d(TAG, "   deleteItem  =" + insertRecentItem + "      InRecentTable" + "  deleteResult   ==== " + isDelete);
                    if (isDelete == 0) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(ClipboardConstants.CLIPBOARD_RECENT_CONTENT_COLUMN_NAME, insertRecentItem);
            contentValues.put(ClipboardConstants.CLIPBOARD_RECENT_ISPINED_COLUMN_NAME, isPined);
            long insertResult = database.insert(ClipboardConstants.CLIPBOARD_RECENT_TABLE, null, contentValues);
            HSLog.d(TAG, "addItem   " + insertRecentItem + "   to bottom of RecentTable  " + "  insertResult " + insertResult);
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
    public boolean isRecentItemExists(String recentItem) {
        Cursor cursor = null;
        try {
            cursor = database.query(ClipboardConstants.CLIPBOARD_RECENT_TABLE, new String[]{ClipboardConstants.CLIPBOARD_RECENT_CONTENT_COLUMN_NAME}, ClipboardConstants.CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{recentItem}, null, null, null);
            boolean itemExists = cursor.moveToNext() && recentItem.equals(cursor.getString(cursor.getColumnIndex(ClipboardConstants.CLIPBOARD_RECENT_CONTENT_COLUMN_NAME)));
            HSLog.d(TAG, "query      " + recentItem + "      isExistsInRecentTable    " + itemExists);
            return itemExists;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor) cursor.close();
        }
        return false;

    }

    @Override
    public boolean isPinItemExists(String pinItem) {
        Cursor cursor = null;
        try {
            cursor = database.query(ClipboardConstants.CLIPBOARD_PINS_TABLE, new String[]{ClipboardConstants.CLIPBOARD_PINS_CONTENT_COLUMN_NAME}, ClipboardConstants.CLIPBOARD_PINS_CONTENT_COLUMN_NAME + "=?", new String[]{pinItem}, null, null, null);
            boolean itemExists = cursor.moveToNext() && pinItem.equals(cursor.getString(cursor.getColumnIndex(ClipboardConstants.CLIPBOARD_PINS_CONTENT_COLUMN_NAME)));
            HSLog.d(TAG, "query     " + pinItem + "       isExistsInPinsTable       " + itemExists);
            return itemExists;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (null != cursor) cursor.close();
        }
    }

    @Override
    public boolean deleteRecentItemAndMovePinedItemToBottom(String recentItemString) {
        int isDeleteRECENT = -1;
        int isDeletePins = -1;
        database.beginTransaction();
        try {
            isDeleteRECENT = database.delete(ClipboardConstants.CLIPBOARD_RECENT_TABLE, ClipboardConstants.CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{recentItemString});
            HSLog.d(TAG, "   deleteItem  =" + recentItemString + "      InRecentTable" + "  deleteResult   ==== " + isDeleteRECENT);
            if (isDeleteRECENT == 0) {
                return false;
            }
            isDeletePins = database.delete(ClipboardConstants.CLIPBOARD_PINS_TABLE, ClipboardConstants.CLIPBOARD_PINS_CONTENT_COLUMN_NAME + "=?", new String[]{recentItemString});
            HSLog.d(TAG, "   deleteItem   =" + recentItemString + "        InPinsTable" + "       deleteResult   ==== " + isDeletePins);
            if (isDeletePins == 0) {
                return false;
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(ClipboardConstants.CLIPBOARD_PINS_CONTENT_COLUMN_NAME, recentItemString);
            long insertResult = database.insert(ClipboardConstants.CLIPBOARD_PINS_TABLE, null, contentValues);
            HSLog.d(TAG, "addItem   " + recentItemString + "   to bottom of PinsTable " + "  insertResult  " + insertResult);
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
    public boolean deleteRecentItemAndAddPinItemToBottom(String deleteRecentItem) {
        int isDelete = -1;
        database.beginTransaction();
        try {
            isDelete = database.delete(ClipboardConstants.CLIPBOARD_RECENT_TABLE, ClipboardConstants.CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{deleteRecentItem});
            if (isDelete == 0) {
                return false;
            }
            HSLog.d(TAG, "   deleteItem  =" + deleteRecentItem + "      InRecentTable" + "  deleteResult   ==== " + isDelete);
            ContentValues contentValues = new ContentValues();
            contentValues.put(ClipboardConstants.CLIPBOARD_PINS_CONTENT_COLUMN_NAME, deleteRecentItem);
            long insertResult = database.insert(ClipboardConstants.CLIPBOARD_PINS_TABLE, null, contentValues);
            HSLog.d(TAG, "addItem   " + deleteRecentItem + "   to bottom of PinsTable" + "insert result " + insertResult);
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
    public boolean deletePinItemAndUnpinRecentItem(String deletePinItem) {
        int isDelete = -1;
        database.beginTransaction();
        try {
            isDelete = database.delete(ClipboardConstants.CLIPBOARD_PINS_TABLE, ClipboardConstants.CLIPBOARD_PINS_CONTENT_COLUMN_NAME + "=?", new String[]{deletePinItem});
            HSLog.d(TAG, "   deleteItem   =" + deletePinItem + "        InPinsTable" + "       deleteResult   ==== " + isDelete);
            if (isDelete == 0) {
                return false;
            }
            ContentValues values = new ContentValues();
            values.put(ClipboardConstants.CLIPBOARD_RECENT_CONTENT_COLUMN_NAME, deletePinItem);
            values.put(ClipboardConstants.CLIPBOARD_RECENT_ISPINED_COLUMN_NAME, 0);
            int updateResult = database.update(ClipboardConstants.CLIPBOARD_RECENT_TABLE, values, ClipboardConstants.CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{deletePinItem});
            if (updateResult < 0) {
                return false;
            }
            HSLog.d(TAG, deletePinItem + "    updateRecentItem   " + deletePinItem + "    NoPinedInPinsTable    ");
            database.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            database.endTransaction();
        }
    }

    public int getRecentSize() {
        Cursor cursor = null;
        try {
            cursor = database.query(ClipboardConstants.CLIPBOARD_RECENT_TABLE, null, null, null, null, null, null);
            return cursor.getCount();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            if (null != cursor) cursor.close();
        }
    }

    @Override
    public int getPinsSize() {
        Cursor cursor = null;
        try {
            cursor = database.query(ClipboardConstants.CLIPBOARD_PINS_TABLE, null, null, null, null, null, null);
            return cursor.getCount();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            if (null != cursor) cursor.close();
        }
    }
}