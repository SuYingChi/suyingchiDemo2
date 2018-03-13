package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.widget.Toast;


import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.uimodules.R;

import java.util.List;


class ClipboardPresenter implements ClipboardContract.Presenter {

    private static final String TAG = ClipboardPresenter.class.getSimpleName();
    private ClipboardContract.ClipboardView clipboardView;
    private ClipboardContract.ClipboardSQLiteOperate clipboardSQLiteOperate;

    ClipboardPresenter(ClipboardContract.ClipboardView clipboardView) {
        clipboardSQLiteOperate = ClipboardDataBaseOperateImpl.getInstance();
        this.clipboardView = clipboardView;
    }

    //实时监听用户点击Recent页面的收藏按钮时的数据操作
    @Override
    public void saveRecentItemToPins(String item, int position) {
        //recent里点击收藏,收藏内容已经有30条，recent页面点击收藏时，收藏里还没有，提示不能再添加
        if (clipboardSQLiteOperate.getPinsSize() == ClipboardConstants.PINS_TABLE_SIZE && !clipboardSQLiteOperate.isPinItemExists(item)) {
            Toast.makeText(HSApplication.getContext(), R.string.clipboard_add_pins_up_to_maximum_value_tip, Toast.LENGTH_LONG).show();
            return;
        }
        //recent里点击收藏，收藏里已经有了，则在recent里去除本条，收藏里置顶该条
        if (clipboardSQLiteOperate.isPinItemExists(item)) {
            boolean isSuccess = clipboardSQLiteOperate.deleteRecentItemAndMovePinedItemToBottom(item);
            if (clipboardView == null) {
                return;
            }
            if (isSuccess) {
                clipboardView.onDeleteRecentAndMovePinToTopSuccess(item, position);
            } else {
                clipboardView.onDeleteRecentAndMovePinToTopFail(item, position);
            }
        }//recent 里点击收藏，收藏里还没有，并且收藏内容小于30条，则添加内容并置顶到收藏，并在recent里删除该条。
        else if (clipboardSQLiteOperate.getPinsAllContentList().size() < ClipboardConstants.PINS_TABLE_SIZE && !clipboardSQLiteOperate.isPinItemExists(item)) {
            boolean isSuccess = clipboardSQLiteOperate.deleteRecentItemAndAddPinItemToBottom(item);
            if (clipboardView == null) {
                return;
            }
            if (isSuccess) {
                clipboardView.onDeleteRecentAndAddPinSuccess(item, position);
            } else {
                clipboardView.onDeleteRecentAndAddPinFail(item, position);
            }
        }

    }

    //实时监听用户点击Pins页面的删除按钮时的数据操作
    @Override
    public void deletePinItem(String item, int position) {
        //用户删除PINS数据，recent里没有
        if (!clipboardSQLiteOperate.isRecentItemExists(item)) {
            boolean isSuccess = clipboardSQLiteOperate.deletePinItem(item);
            if (clipboardView == null) {
                return;
            }
            if (isSuccess) {
                clipboardView.onDeletePinSuccess(position);
            } else {
                clipboardView.onDeletePinFail(position);
            }
        }//用户删除PINS数据，recent里有,标明recent里该项内容已不被收藏
        else if (clipboardSQLiteOperate.isRecentItemExists(item)) {
            boolean isSuccess = clipboardSQLiteOperate.deletePinItemAndUnpinRecentItem(item);
            if (clipboardView == null) {
                return;
            }
            ClipboardRecentViewAdapter.ClipboardRecentMessage recentItem = new ClipboardRecentViewAdapter.ClipboardRecentMessage(item, 0);
            if (isSuccess) {
                HSLog.d(TAG, "onDeletePinAndUnpinRecentSuccess  ============== " + recentItem);
                clipboardView.onDeletePinAndUnpinRecentSuccess(recentItem, position);
            } else {
                clipboardView.onDeletePinAndUnpinRecentFail(recentItem, position);
            }
        }
    }

    List<ClipboardRecentViewAdapter.ClipboardRecentMessage> getClipRecentData() {
        return clipboardSQLiteOperate.getRecentAllContentList();
    }

    List<String> getClipPinsData() {
        return clipboardSQLiteOperate.getPinsAllContentList();
    }
}