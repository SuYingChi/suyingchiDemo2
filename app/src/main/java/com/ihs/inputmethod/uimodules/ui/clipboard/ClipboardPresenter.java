package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.widget.Toast;


import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;

import java.util.List;


public class ClipboardPresenter  {
    static final int RECENT_TABLE_SIZE = 10;
    static final int PINS_TABLE_SIZE = 30;
    private ClipboardContact.ClipboardView ClipboardView;
    ClipboardContact.ClipboardSQLiteOperate clipboardSQLiteOperate;

    ClipboardPresenter() {
        clipboardSQLiteOperate = ClipboardDataBaseOperateImpl.getInstance();
    }

    List<ClipboardRecentViewAdapter.ClipboardRecentMessage> getclipRecentData() {
        return clipboardSQLiteOperate.getRecentAllContentFromTable();
    }

    List<String> getClipPinsData() {
        return clipboardSQLiteOperate.getPinsAllContentFromTable();
    }


    void setClipboardView(ClipboardContact.ClipboardView clipboardView) {
        this.ClipboardView = clipboardView;
    }

    //实时监听用户点击Recent页面的收藏按钮时的数据操作
    void clipDataOperateSaveToPins(String item) {
        //recent里点击收藏,收藏内容已经有30条，recent页面点击收藏时，收藏里还没有，提示不能再添加
        if (clipboardSQLiteOperate.getPinsAllContentFromTable().size() == PINS_TABLE_SIZE & clipboardSQLiteOperate.queryItemExistsInPinsTable(item)==0) {
            Toast.makeText(HSApplication.getContext(), R.string.clipboard_add_pins_most, Toast.LENGTH_LONG).show();
            return;
        }
        //recent里点击收藏，收藏里已经有了，则在recent里去除本条，收藏里置顶该条
        if (clipboardSQLiteOperate.queryItemExistsInPinsTable(item)==1) {
             boolean isSuccess = clipboardSQLiteOperate.deleteRecentItemAndSetItemPositionToBottomInPins(item);
            int pinsLastPosition= clipboardSQLiteOperate.queryItemInPinsTableReversePosition(item);
            if (ClipboardView ==null){
                return;
            }
            if(isSuccess){
                ClipboardView.notifyDeleteRecentAndSetPinsItemToTopDataSetChange(pinsLastPosition);
            }else {
                ClipboardView.deleteRecentAndSetPinsItemToTopFail(pinsLastPosition);
            }
        }//recent 里点击收藏，收藏里还没有，并且收藏内容小于30条，则添加内容并置顶到收藏，并在recent里删除该条。
        else if (clipboardSQLiteOperate.getPinsAllContentFromTable().size() < PINS_TABLE_SIZE & clipboardSQLiteOperate.queryItemExistsInPinsTable(item)==0) {
            boolean isSuccess= clipboardSQLiteOperate.deleteRecentItemAndAddToPins(item);
            if (ClipboardView ==null){
                return;
            }
            if(isSuccess){
                ClipboardView.notifyDeleteRecentAndAddPinsDataItemToTopChange();
            }else {
                ClipboardView.deleteRecentAndAddPinsDataItemToTopFail();
            }
        }

    }

    //实时监听用户点击Pins页面的删除按钮时的数据操作
    void clipDataOperateDeletePins(String item) {
        //用户删除PINS数据，recent里没有
        if (!clipboardSQLiteOperate.queryItemExistsInRecentTable(item)) {
           boolean isSuccess = clipboardSQLiteOperate.deleteItemInPinsTable(item);
           if (ClipboardView ==null){
               return;
           }
           if(isSuccess){
               ClipboardView.notifyDeletePinsDataItem();
           }else {
               ClipboardView.deletePinsDataItemFail();
           }
        }//用户删除PINS数据，recent里有,标明recent里该项内容已不被收藏
        else if (clipboardSQLiteOperate.queryItemExistsInRecentTable(item)) {
            boolean isSuccess = clipboardSQLiteOperate.deletePinsItemAndUpdateRecentItemNoPined(item);
            int recentPosition= clipboardSQLiteOperate.queryItemInRecentTableReversePosition(item);
            if (ClipboardView ==null){
                return;
            }
            if(isSuccess){
                ClipboardView.notifyDeletePinsItemUpdateRecentNoPined(recentPosition);
            }else {
                ClipboardView.deletePinsItemUpdateRecentNoPinedFail(recentPosition);
            }
        }
    }
}