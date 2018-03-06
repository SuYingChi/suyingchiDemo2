package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.widget.Toast;


import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import java.util.List;


public class ClipboardPresenter implements OnClipboardDataBaseOperateFinishListener {
    static final int RECENT_TABLE_SIZE = 10;
    static final int PINS_TABLE_SIZE = 30;
    private ClipboardMainViewProxy clipboardMainViewProxy;
    ClipboardSQLiteOperate clipboardSQLiteOperate;

    ClipboardPresenter() {
        clipboardSQLiteOperate = ClipboardDataBaseOperateImpl.getInstance();
        clipboardSQLiteOperate.setOnDataBaseOperateFinishListener(this);
    }

    @Override
    public void addRecentItemSuccess() {
        if (clipboardMainViewProxy != null) {
            clipboardMainViewProxy.notifyAddRecentDataItemToTopChange();
        }
    }


    @Override
    public void setRecentItemToTopSuccess(ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage,int position) {
        if (clipboardMainViewProxy != null) {
            clipboardMainViewProxy.notifySetRecentItemToTopDataSetChange(clipboardRecentMessage,position);
        }
    }


    @Override
    public void deletePinsItemSuccess() {
        if (clipboardMainViewProxy != null) {
            clipboardMainViewProxy.notifyDeletePinsDataItem();
        }
    }



    @Override
    public void deleteRecentItemAndSetItemPositionToBottomInPins(int lastPosition) {
        if (clipboardMainViewProxy != null) {
            clipboardMainViewProxy.notifyDeleteRecentAndSetPinsItemToTopDataSetChange(lastPosition);
        }
    }



    @Override
    public void deleteRecentItemAndAddToPins() {
        if (clipboardMainViewProxy != null) {
            clipboardMainViewProxy.notifyDeleteRecentAndAddPinsDataItemToTopChange();
        }
    }
    @Override
    public void deletePinsItemAndUpdateRecentItemNoPined(int position) {
        if (clipboardMainViewProxy != null) {

            clipboardMainViewProxy.notifyUpdateRecentNoPined(position);
        }
    }

    @Override
    public void clipboardDataBaseOperateFail() {
        clipboardMainViewProxy.clipboardDataBaseOperateFail();
    }


    List<ClipboardRecentViewAdapter.ClipboardRecentMessage> getclipRecentData() {
        return clipboardSQLiteOperate.getRecentAllContentFromTable();
    }

    List<String> getClipPinsData() {
        return clipboardSQLiteOperate.getPinsAllContentFromTable();
    }


    void setClipboardMainViewProxy(ClipboardMainViewProxy clipboardMainViewProxy) {
        this.clipboardMainViewProxy = clipboardMainViewProxy;
    }

    //实时监听用户点击Recent页面的收藏按钮时的数据操作
    void clipDataOperateSaveToPins(String item) {
        //recent里点击收藏,收藏内容已经有30条，recent页面点击收藏时，收藏里还没有，提示不能再添加
        if (clipboardSQLiteOperate.getPinsAllContentFromTable().size() == PINS_TABLE_SIZE & !clipboardSQLiteOperate.queryItemExistsInPinsTable(item)) {
            Toast.makeText(HSApplication.getContext(), "You can add at most 30 message", Toast.LENGTH_LONG).show();
            return;
        }
        //recent里点击收藏，收藏里已经有了，则在recent里去除本条，收藏里置顶该条
        if (clipboardSQLiteOperate.queryItemExistsInPinsTable(item)) {
            clipboardSQLiteOperate.deleteRecentItemAndSetItemPositionToBottomInPins(item);
        }//recent 里点击收藏，收藏里还没有，并且收藏内容小于30条，则添加内容并置顶到收藏，并在recent里删除该条。
        else if (clipboardSQLiteOperate.getPinsAllContentFromTable().size() < PINS_TABLE_SIZE & !clipboardSQLiteOperate.queryItemExistsInPinsTable(item)) {
            clipboardSQLiteOperate.deleteRecentItemAndAddToPins(item);
        }

    }

    //实时监听用户点击Pins页面的删除按钮时的数据操作
    void clipDataOperateDeletePins(String item) {
        //用户删除PINS数据，recent里没有
        if (!clipboardSQLiteOperate.queryItemExistsInRecentTable(item)) {
            clipboardSQLiteOperate.deleteItemInPinsTable(item);
        }//用户删除PINS数据，recent里有,标明recent里该项内容已不被收藏
        else if (clipboardSQLiteOperate.queryItemExistsInRecentTable(item)) {
            clipboardSQLiteOperate.deletePinsItemAndUpdateRecentItemNoPined(item);
        }
    }
}