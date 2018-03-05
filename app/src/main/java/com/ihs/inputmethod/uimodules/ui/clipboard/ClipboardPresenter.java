package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.widget.Toast;


import com.ihs.app.framework.HSApplication;

import java.util.List;


public class ClipboardPresenter implements OnClipboardDataBaseOperateFinishListener {
    static final int RECENT_TABLE_SIZE = 10;
    static final int PINS_TABLE_SIZE = 30;
    private ClipboardMainViewListener clipboardMainViewListener;
    ClipboardSQLiteOperate clipboardSQLiteOperate;

    ClipboardPresenter() {
        clipboardSQLiteOperate = ClipboardSQLiteDao.getInstance();
        clipboardSQLiteOperate.setOnDataBaseOperateFinishListener(this);
    }


    private void updateRecentView() {
        if (clipboardMainViewListener != null) {
            clipboardMainViewListener.notifyRecentDataSetChange();
        }
    }

    private void updatePinsView() {
        if (clipboardMainViewListener != null) {
            clipboardMainViewListener.notifyPinsDataSetChange();
        }
    }


    @Override
    public void addRecentItemSuccess() {
        updateRecentView();
        clipboardMainViewListener.changeToShowRecentView();
    }


    @Override
    public void setRecentItemToTopSuccess() {

        updateRecentView();
        clipboardMainViewListener.changeToShowRecentView();
    }


    @Override
    public void deletePinsItemSuccess() {
        updatePinsView();
    }


    @Override
    public void deleteRecentItemAndSetItemPositionToBottomInPins() {
        updateRecentView();
        updatePinsView();
        clipboardMainViewListener.changeToShowPinsView();
    }

    @Override
    public void deleteRecentItemAndAddToPins() {
        updateRecentView();
        updatePinsView();
        clipboardMainViewListener.changeToShowPinsView();
    }

    @Override
    public void deletePinsItemAndUpdateRecentItemNoPined() {
        updateRecentView();
        updatePinsView();
        clipboardMainViewListener.changeToShowRecentView();
    }

    List<ClipboardRecentViewAdapter.ClipboardRecentMessage> getclipRecentData() {
        return ClipboardSQLiteDao.getInstance().getRecentAllContentFromTable();
    }

    List<String> getClipPinsData() {
        return ClipboardSQLiteDao.getInstance().getPinsAllContentFromTable();
    }


    void setClipboardMainViewListener(ClipboardMainViewListener clipboardMainViewListener) {
        this.clipboardMainViewListener = clipboardMainViewListener;
    }

    //实时监听用户点击Recent页面的收藏按钮时的数据操作
    void clipDataOperateSaveToPins(String item) {
        //recent里点击收藏,收藏内容已经有30条，recent页面点击收藏时，收藏里还没有，提示不能再添加
        if (ClipboardSQLiteDao.getInstance().getPinsAllContentFromTable().size() == PINS_TABLE_SIZE & !ClipboardSQLiteDao.getInstance().queryItemExistsInPinsTable(item)) {
            Toast.makeText(HSApplication.getContext(), "You can add at most 30 message", Toast.LENGTH_LONG).show();
            return;
        }
        //recent里点击收藏，收藏里已经有了，则在recent里去除本条，收藏里置顶该条
        if (ClipboardSQLiteDao.getInstance().queryItemExistsInPinsTable(item)) {
            ClipboardSQLiteDao.getInstance().deleteRecentItemAndSetItemPositionToBottomInPins(item);
        }//recent 里点击收藏，收藏里还没有，并且收藏内容小于30条，则添加内容并置顶到收藏，并在recent里删除该条。
        else if (ClipboardSQLiteDao.getInstance().getPinsAllContentFromTable().size() < PINS_TABLE_SIZE & !ClipboardSQLiteDao.getInstance().queryItemExistsInPinsTable(item)) {
            ClipboardSQLiteDao.getInstance().deleteRecentItemAndAddToPins(item);
        }

    }

    //实时监听用户点击Pins页面的删除按钮时的数据操作
    void clipDataOperateDeletePins(String item) {
        //用户删除PINS数据，recent里没有
        if (!ClipboardSQLiteDao.getInstance().queryItemExistsInRecentTable(item)) {
            ClipboardSQLiteDao.getInstance().deleteItemInPinsTable(item);
        }//用户删除PINS数据，recent里有,标明recent里该项内容已不被收藏
        else if (ClipboardSQLiteDao.getInstance().queryItemExistsInRecentTable(item)) {
            ClipboardSQLiteDao.getInstance().deletePinsItemAndUpdateRecentItemNoPined(item);
        }
    }
}