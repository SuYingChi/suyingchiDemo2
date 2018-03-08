package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.widget.Toast;


import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.uimodules.R;

import java.util.List;


class ClipboardPresenter {
    static final int RECENT_TABLE_SIZE = 10;
    private static final int PINS_TABLE_SIZE = 30;
    private static final String TAG = ClipboardPresenter.class.getSimpleName();
    private ClipboardContact.ClipboardView ClipboardView;
    private ClipboardContact.ClipboardSQLiteOperate clipboardSQLiteOperate;

    ClipboardPresenter() {
        clipboardSQLiteOperate = ClipboardDataBaseOperateImpl.getInstance();
    }

    List<ClipboardRecentViewAdapter.ClipboardRecentMessage> getclipRecentData() {
        return clipboardSQLiteOperate.getRecentAllContentList();
    }

    List<String> getClipPinsData() {
        return clipboardSQLiteOperate.getPinsAllContentList();
    }


    void setClipboardView(ClipboardContact.ClipboardView clipboardView) {
        this.ClipboardView = clipboardView;
    }

    //实时监听用户点击Recent页面的收藏按钮时的数据操作
    void clipDataOperateSaveToPins(String item) {
        //recent里点击收藏,收藏内容已经有30条，recent页面点击收藏时，收藏里还没有，提示不能再添加
        if (clipboardSQLiteOperate.getPinsSize() == PINS_TABLE_SIZE & clipboardSQLiteOperate.isPinItemExists(item) == 0) {
            Toast.makeText(HSApplication.getContext(), R.string.clipboard_add_pins_up_to_maximum_value_tip, Toast.LENGTH_LONG).show();
            return;
        }
        //recent里点击收藏，收藏里已经有了，则在recent里去除本条，收藏里置顶该条
        if (clipboardSQLiteOperate.isPinItemExists(item) == 1) {
            //int pinsLastPosition = clipboardSQLiteOperate.queryItemInPinsTableReversePosition(item);
            boolean isSuccess = clipboardSQLiteOperate.deleteRecentItemAndSetItemPositionToBottomInPins(item);
            if (ClipboardView == null) {
                return;
            }
            if (isSuccess) {
                HSLog.d(TAG,"presenter pinsLastPosition    "+item);
                ClipboardView.onDeleteRecentAndMovePinToTop(item);
            } else {
                ClipboardView.onDeleteRecentAndMovePinToTopFail(item);
            }
        }//recent 里点击收藏，收藏里还没有，并且收藏内容小于30条，则添加内容并置顶到收藏，并在recent里删除该条。
        else if (clipboardSQLiteOperate.getPinsAllContentList().size() < PINS_TABLE_SIZE & clipboardSQLiteOperate.isPinItemExists(item) == 0) {
            boolean isSuccess = clipboardSQLiteOperate.deleteRecentItemAndAddToPins(item);
            if (ClipboardView == null) {
                return;
            }
            if (isSuccess) {
                ClipboardView.onDeleteRecentAndAddPin();
            } else {
                ClipboardView.onDeleteRecentAndAddPinFail();
            }
        }

    }

    //实时监听用户点击Pins页面的删除按钮时的数据操作
    void clipDataOperateDeletePins(String item) {
        //用户删除PINS数据，recent里没有
        if (!clipboardSQLiteOperate.isRecentItemExists(item)) {
            boolean isSuccess = clipboardSQLiteOperate.deletePinItem(item);
            if (ClipboardView == null) {
                return;
            }
            if (isSuccess) {
                ClipboardView.onDeletePin();
            } else {
                ClipboardView.onDeletePinFail();
            }
        }//用户删除PINS数据，recent里有,标明recent里该项内容已不被收藏
        else if (clipboardSQLiteOperate.isRecentItemExists(item)) {
            boolean isSuccess = clipboardSQLiteOperate.deletePinItemAndUnpinRecentItem(item);
            ClipboardRecentViewAdapter.ClipboardRecentMessage recentItem = clipboardSQLiteOperate.getRecentItem(item);
            if (ClipboardView == null) {
                return;
            }
            if (isSuccess) {
                ClipboardView.onDeletePinAndUnpinRecent(recentItem);
            } else {
                ClipboardView.onDeletePinAndUnpinRecentFail(recentItem);
            }
        }
    }
}