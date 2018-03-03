package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.widget.Toast;


import com.ihs.app.framework.HSApplication;


public class ClipboardPresenter implements ClipboardSQLiteDao.OnDataBaseOperateFinishListener {
    static final int RECENT_TABLE_SIZE = 10;
    static final int PINS_TABLE_SIZE = 30;
    private ClipboardMainViewListener clipboardMainViewListener;

    public ClipboardPresenter() {
        ClipboardSQLiteDao.getInstance().setOnDataBaseOperateFinishListener(this);
    }


    public void notifyRecentDataChange() {
        if (clipboardMainViewListener != null) {
            clipboardMainViewListener.notifyRecentChange();
        }
    }

    public void notifyPinsDataChange() {
        if (clipboardMainViewListener != null) {
            clipboardMainViewListener.notifyPinsChange();
        }
    }



    @Override
    public void addRecentItemSuccess() {

        notifyRecentDataChange();
    }



    @Override
    public void setRecentItemToTopSuccess() {

        notifyRecentDataChange();
    }



    @Override
    public void deletePinsItemSuccess() {
        notifyPinsDataChange();
    }



    @Override
    public void deleteRecentItemAndSetItemPositionToBottomInPins() {
        notifyRecentDataChange();
        notifyPinsDataChange();
    }

    @Override
    public void deleteRecentItemAndAddToPins() {
        notifyRecentDataChange();
        notifyPinsDataChange();
    }

    @Override
    public void deletePinsItemAndUpdateRecentItemNoPined() {
        notifyRecentDataChange();
        notifyPinsDataChange();
    }


    //MainView创建后presenter获得该接口实例，将adapter创建完成后回传给MainView，mainView再将adapter贴上去
    public interface ClipboardMainViewListener {
        void notifyPinsChange();

        void notifyRecentChange();
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