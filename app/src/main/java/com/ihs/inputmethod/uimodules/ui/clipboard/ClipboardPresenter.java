package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.widget.Toast;


import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.kc.utils.KCAnalytics;


import java.util.ArrayList;
import java.util.List;



public class ClipboardPresenter implements ClipboardRecentViewAdapter.SaveRecentItemToPinsListener, ClipboardPinsViewAdapter.DeleteFromPinsToRecentListener {
    static final int OPERATE_ADD_RECENT = 1;
    private static final int OPERATE_SAVE_TO_PINS = 2;
    static final int OPERATE__DELETE_PINS = 3;

    static final int RECENT_VIEW = 6;
    static final int PINS_VIEW = 7;
    static final int RECENT_TABLE_SIZE = 10;
    static final int PINS_TABLE_SIZE = 30;
    private  volatile static ClipboardPresenter clipboardPresenter;
    private List<ClipboardRecentViewAdapter.ClipboardRecentMessage> clipRecentData = new ArrayList<ClipboardRecentViewAdapter.ClipboardRecentMessage>();
    private List<String> clipPinsData = new ArrayList<String>();
    private boolean isNeedClipRecentReDraw = false;
    private boolean isNeedClipPinsReDraw = false;
    private OnMainViewCreatedListener onMainViewCreatedListener;
    private final String TAG = ClipboardPresenter.class.getSimpleName();
    private String pinsContentItem;

    public static ClipboardPresenter getInstance() {
        if (clipboardPresenter == null) {
            synchronized (ClipboardPresenter.class) {
                if (clipboardPresenter == null) {
                    clipboardPresenter = new ClipboardPresenter();
                }
            }
        }
        return clipboardPresenter;
    }

    private ClipboardPresenter() {
        clipRecentData = ClipboardSQLiteDao.getInstance().getRecentAllContentFromTable();
        clipPinsData = ClipboardSQLiteDao.getInstance().getPinsAllContentFromTable();

    }

    //新增复制内容时,点击收藏，删除收藏内容时的处理数据的接口，保存数据到数据库里，
    void clipDataOperateAndSaveToDatabase(String item, int operateType) {
        if (operateType == OPERATE_SAVE_TO_PINS) {
            //recent里点击收藏,收藏内容已经有30条，recent页面点击收藏时，收藏里还没有，提示不能再添加
            if (clipRecentData.size() == PINS_TABLE_SIZE & !clipPinsData.contains(item)) {
                Toast.makeText(HSApplication.getContext(), "You can add at most 30 message", Toast.LENGTH_LONG).show();
                isNeedClipPinsReDraw = false;
                isNeedClipRecentReDraw = false;
                return;
            }
            clipDataOperateSaveToPins(item);
        } else if (operateType == OPERATE_ADD_RECENT) {
            clipDataOperateAddRecent(item);
        } else if (operateType == OPERATE__DELETE_PINS) {
            clipDataOperateDeletePins(item);
        }
        //同步数据库数据到应用并标明是否需要刷新页面
        List<ClipboardRecentViewAdapter.ClipboardRecentMessage> recentListTemp = ClipboardSQLiteDao.getInstance().getRecentAllContentFromTable();
        List<String> pinsListTemp = ClipboardSQLiteDao.getInstance().getPinsAllContentFromTable();
        if (!clipRecentData.equals(recentListTemp)) {
            clipRecentData.clear();
            clipRecentData.addAll(recentListTemp);
            isNeedClipRecentReDraw = true;
            HSLog.d(TAG, "  clipRecentData synchronized from database , " + "   clipRecentData  is" + recentListTemp.toString() + ", need to update display");
        } else {
            isNeedClipRecentReDraw = false;
            HSLog.d(TAG, " clipRecentData is  the newest,no need to update");
        }
        if (!clipPinsData.equals(pinsListTemp)) {
            clipPinsData.clear();
            clipPinsData.addAll(pinsListTemp);
            isNeedClipPinsReDraw = true;
            HSLog.d(TAG, "clipPinsData synchronized from database     " + "   clipPinsData is" + clipPinsData.toString() + ",  need to update display");
        } else {
            isNeedClipPinsReDraw = false;
            HSLog.d(TAG, " clipPinsData is  the newest,no need to update");
        }
    }

    //实时监听用户点击Recent页面的收藏按钮时的数据操作
    private void clipDataOperateSaveToPins(String item) {
        //recent里点击收藏，收藏里已经有了，则在recent里去除本条，收藏里置顶该条
        if (clipPinsData.contains(item)) {
            ClipboardSQLiteDao.getInstance().deleteItemInTable(item, RECENT_VIEW);
            ClipboardSQLiteDao.getInstance().setItemPositionToBottomInTable(item, PINS_VIEW);
        }//recent 里点击收藏，收藏里还没有，并且收藏内容小于30条，则添加内容并置顶到收藏，并在recent里删除该条。
        else if (clipPinsData.size() < PINS_TABLE_SIZE & !clipPinsData.contains(item)) {
            ClipboardSQLiteDao.getInstance().deleteItemInTable(item, RECENT_VIEW);
            ClipboardSQLiteDao.getInstance().addItemToBottomInTable(item, PINS_VIEW);
        }

    }

    //实时监听用户点击Pins页面的删除按钮时的数据操作
    private void clipDataOperateDeletePins(String item) {
        //用户删除PINS数据，recent里没有
        if (!clipRecentData.contains(item)) {
            ClipboardSQLiteDao.getInstance().deleteItemInTable(item, PINS_VIEW);
        }//用户删除PINS数据，recent里有,标明recent里该项内容已不被收藏
        else if (clipRecentData.contains(item)) {
            ClipboardSQLiteDao.getInstance().deleteItemInTable(item, PINS_VIEW);
            ClipboardSQLiteDao.getInstance().updateRecentItemNoPinedInPinsTable(item);
        }
    }

    //实时监听用户新增复制内容时的操作
    private void clipDataOperateAddRecent(String item) {
        //用户新增recent数据，小于最大条数并且内容未与recent重复增加新内容,添加并置顶
        if (clipRecentData.size() <= RECENT_TABLE_SIZE & !clipRecentData.contains(item)) {
            ClipboardSQLiteDao.getInstance().addItemToBottomInTable(item, RECENT_VIEW);
        }
        //用户新增recent数据，与recent已有内容重复，则置顶重复内容
        else if (clipRecentData.contains(item)) {
            ClipboardSQLiteDao.getInstance().setItemPositionToBottomInTable(item, RECENT_VIEW);
        }
    }

    //点击recent的条目的pins按钮时执行的回调
    @Override
    public void saveToPins(String itemPinsContent) {
        clipDataOperateAndSaveToDatabase(itemPinsContent, OPERATE_SAVE_TO_PINS);
        if (isNeedClipRecentReDraw) {
            notifyRecentDataChange();
        }
        if (isNeedClipPinsReDraw) {
            notifyPinsDataChange();
        }
        onMainViewCreatedListener.showPinsView();
        KCAnalytics.logEvent("keyboard_clipboard_pin_clicked");

    }

    //点击pins的条目的删除按钮时的回调
    @Override
    public void deletePinsItem(String pinsContentItem) {

        HSLog.d(TAG,"deletePinsItem  "+pinsContentItem);
        onMainViewCreatedListener.showDeletedSuggestionAlert();
        this.pinsContentItem = pinsContentItem;
        KCAnalytics.logEvent("keyboard_clipboard_OneDeleted");


    }

    public void notifyRecentDataChange() {
        if (onMainViewCreatedListener != null) {
            onMainViewCreatedListener.notifyRecentChange();
        }
    }

    public void notifyPinsDataChange() {
        if (onMainViewCreatedListener != null) {
            onMainViewCreatedListener.notifyPinsChange();
        }
    }


    public void deletePin() {
        clipDataOperateAndSaveToDatabase(pinsContentItem, OPERATE__DELETE_PINS);
        if (isNeedClipRecentReDraw) {
            notifyRecentDataChange();
        }
        if (isNeedClipPinsReDraw) {
            notifyPinsDataChange();
        }
    }

    //MainView创建后presenter获得该接口实例，将adapter创建完成后回传给MainView，mainView再将adapter贴上去
    public interface OnMainViewCreatedListener {
        void notifyPinsChange();

        void notifyRecentChange();

        void showDeletedSuggestionAlert();

        void showPinsView();
    }

    void setOnMainViewCreatedListener(OnMainViewCreatedListener onMainViewCreatedListener) {
        this.onMainViewCreatedListener = onMainViewCreatedListener;
    }


    List<ClipboardRecentViewAdapter.ClipboardRecentMessage> getClipRecentData() {
        return clipRecentData;
    }

    List<String> getClipPinsData() {
        return clipPinsData;
    }

    boolean isNeedClipRecentReDraw() {
        return isNeedClipRecentReDraw;
    }

    void clipRecentNoNeedReDraw() {
        isNeedClipRecentReDraw = false;
    }

    void clipPinsNoNeedReDraw() {
        isNeedClipPinsReDraw = false;
    }
}