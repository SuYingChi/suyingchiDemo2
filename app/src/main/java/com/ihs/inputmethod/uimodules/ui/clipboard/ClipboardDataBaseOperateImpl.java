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

    static final String CLIPBOARD_RECENT_TABLE = "clipboard_recent_table";
    static final String CLIPBOARD_PINS_TABLE = "clipboard_pins_table";
    static final String _ID = "_id";
    static final String CLIPBOARD_RECENT_CONTENT_COLUMN_NAME = "clipboard_recent_content";
    static final String CLIPBOARD_RECENT_ISPINED_COLUMN_NAME = "clipboard_recent_isPined";
    static final String CLIPBOARD_PINS_CONTENT_COLUMN_NAME = "clipboard_PinS_content";
    private static ClipboardDataBaseOperateImpl clipboardDataBaseOperateImpl;
    private final static String TAG = ClipboardDataBaseOperateImpl.class.getSimpleName();
    private SQLiteDatabase database = ClipboardSQLiteOpenHelper.getInstance().getWritableDatabase();

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


    @Override
    //获取Recent表的被反转的list,
    public List<ClipboardRecentViewAdapter.ClipboardRecentMessage> getRecentAllContentList() {
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
    public List<String> getPinsAllContentList() {
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
    public boolean deletePinItem(String deletePinItem) {
        int isDelete = -1;
        try {
            isDelete = database.delete(CLIPBOARD_PINS_TABLE, CLIPBOARD_PINS_CONTENT_COLUMN_NAME + "=?", new String[]{deletePinItem});
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
            deleteRow = database.delete(CLIPBOARD_RECENT_TABLE, CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{existRecentItem});
            HSLog.d(TAG, "   deleteItem  =" + existRecentItem + "      InRecentTable" + "  deleteResult   ==== " + deleteRow);
            if (deleteRow == 0) {
                return false;
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(CLIPBOARD_RECENT_CONTENT_COLUMN_NAME, existRecentItem);
            contentValues.put(CLIPBOARD_RECENT_ISPINED_COLUMN_NAME, isPined);
            long insertResult = database.insert(CLIPBOARD_RECENT_TABLE, null, contentValues);
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

/*    public int queryItemInRecentTableReversePosition(String item) {

        Cursor cursor = null;
        String recentContentItem = "";
        int position = 0;
        try {
            cursor = database.query(CLIPBOARD_RECENT_TABLE, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                recentContentItem = cursor.getString(cursor.getColumnIndex(CLIPBOARD_RECENT_CONTENT_COLUMN_NAME));
                if (item.equals(recentContentItem)) {
                    position = getRecentAllContentList().size() - position - 1;
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
    }*/

/*    public int queryItemInPinsTableReversePosition(String item) {

        Cursor cursor = null;
        String pinsContentItem = "";
        int position = 0;
        try {
            cursor = database.query(CLIPBOARD_PINS_TABLE, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                pinsContentItem = cursor.getString(cursor.getColumnIndex(CLIPBOARD_PINS_CONTENT_COLUMN_NAME));
                if (item.equals(pinsContentItem)) {
                    position = getPinsAllContentList().size() - position - 1;
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
    }*/

    @Override
    public ClipboardRecentViewAdapter.ClipboardRecentMessage getRecentItem(String recentStringItem) {
        Cursor cursor = null;
        try {
            cursor = database.query(CLIPBOARD_RECENT_TABLE, new String[]{CLIPBOARD_RECENT_CONTENT_COLUMN_NAME, CLIPBOARD_RECENT_ISPINED_COLUMN_NAME}, CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{recentStringItem}, null, null, null);
            if (cursor.moveToNext()) {
                int itemExists = cursor.getInt(cursor.getColumnIndex(CLIPBOARD_RECENT_ISPINED_COLUMN_NAME));
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
            if (currentRecentSize == RECENT_TABLE_SIZE) {
                cursor = database.query(CLIPBOARD_RECENT_TABLE, null, null, null, null, null, null);
                if (cursor.moveToFirst()) {
                    String firstItem = cursor.getString(cursor.getColumnIndex(CLIPBOARD_RECENT_CONTENT_COLUMN_NAME));
                    int isDelete = database.delete(CLIPBOARD_RECENT_TABLE, CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{firstItem});
                    HSLog.d(TAG, "   deleteItem  =" + insertRecentItem + "      InRecentTable" + "  deleteResult   ==== " + isDelete);
                    if (isDelete == 0) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(CLIPBOARD_RECENT_CONTENT_COLUMN_NAME, insertRecentItem);
            contentValues.put(CLIPBOARD_RECENT_ISPINED_COLUMN_NAME, isPined);
            long insertResult = database.insert(CLIPBOARD_RECENT_TABLE, null, contentValues);
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
            cursor = database.query(CLIPBOARD_RECENT_TABLE, new String[]{CLIPBOARD_RECENT_CONTENT_COLUMN_NAME}, CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{recentItem}, null, null, null);
            boolean itemExists = cursor.moveToNext() && recentItem.equals(cursor.getString(cursor.getColumnIndex(CLIPBOARD_RECENT_CONTENT_COLUMN_NAME)));
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
    public int isPinItemExists(String pinItem) {
        Cursor cursor = null;
        try {
            cursor = database.query(CLIPBOARD_PINS_TABLE, new String[]{CLIPBOARD_PINS_CONTENT_COLUMN_NAME}, CLIPBOARD_PINS_CONTENT_COLUMN_NAME + "=?", new String[]{pinItem}, null, null, null);
            boolean itemExists = cursor.moveToNext() && pinItem.equals(cursor.getString(cursor.getColumnIndex(CLIPBOARD_PINS_CONTENT_COLUMN_NAME)));
            HSLog.d(TAG, "query     " + pinItem + "       isExistsInPinsTable       " + itemExists);
            return itemExists ? 1 : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            if (null != cursor) cursor.close();
        }
    }

    @Override
    public boolean deleteRecentItemAndSetItemPositionToBottomInPins(String deleteRecentItem) {
        int isDeleteRECENT = -1;
        int isDeletePins = -1;
        database.beginTransaction();
        try {
            isDeleteRECENT = database.delete(CLIPBOARD_RECENT_TABLE, CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{deleteRecentItem});
            HSLog.d(TAG, "   deleteItem  =" + deleteRecentItem + "      InRecentTable" + "  deleteResult   ==== " + isDeleteRECENT);
            if (isDeleteRECENT == 0) {
                return false;
            }
            isDeletePins = database.delete(CLIPBOARD_PINS_TABLE, CLIPBOARD_PINS_CONTENT_COLUMN_NAME + "=?", new String[]{deleteRecentItem});
            HSLog.d(TAG, "   deleteItem   =" + deleteRecentItem + "        InPinsTable" + "       deleteResult   ==== " + isDeletePins);
            if (isDeletePins == 0) {
                return false;
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(CLIPBOARD_PINS_CONTENT_COLUMN_NAME, deleteRecentItem);
            long insertResult = database.insert(CLIPBOARD_PINS_TABLE, null, contentValues);
            HSLog.d(TAG, "addItem   " + deleteRecentItem + "   to bottom of PinsTable " + "  insertResult  " + insertResult);
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
    public boolean deleteRecentItemAndAddToPins(String deleteRecentItem) {
        int isDelete = -1;
        database.beginTransaction();
        try {
            isDelete = database.delete(CLIPBOARD_RECENT_TABLE, CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{deleteRecentItem});
            if (isDelete == 0) {
                return false;
            }
            HSLog.d(TAG, "   deleteItem  =" + deleteRecentItem + "      InRecentTable" + "  deleteResult   ==== " + isDelete);
            ContentValues contentValues = new ContentValues();
            contentValues.put(CLIPBOARD_PINS_CONTENT_COLUMN_NAME, deleteRecentItem);
            long insertResult = database.insert(CLIPBOARD_PINS_TABLE, null, contentValues);
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
            isDelete = database.delete(CLIPBOARD_PINS_TABLE, CLIPBOARD_PINS_CONTENT_COLUMN_NAME + "=?", new String[]{deletePinItem});
            HSLog.d(TAG, "   deleteItem   =" + deletePinItem + "        InPinsTable" + "       deleteResult   ==== " + isDelete);
            if (isDelete == 0) {
                return false;
            }
            ContentValues values = new ContentValues();
            values.put(CLIPBOARD_RECENT_CONTENT_COLUMN_NAME, deletePinItem);
            values.put(CLIPBOARD_RECENT_ISPINED_COLUMN_NAME, 0);
            int updateResult = database.update(CLIPBOARD_RECENT_TABLE, values, CLIPBOARD_RECENT_CONTENT_COLUMN_NAME + "=?", new String[]{deletePinItem});
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
            cursor = database.query(CLIPBOARD_RECENT_TABLE, null, null, null, null, null, null);
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
            cursor = database.query(CLIPBOARD_PINS_TABLE, null, null, null, null, null, null);
            return cursor.getCount();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            if (null != cursor) cursor.close();
        }
    }
}