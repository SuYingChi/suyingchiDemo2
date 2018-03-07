package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import static com.ihs.inputmethod.uimodules.ui.clipboard.ClipboardPresenter.RECENT_TABLE_SIZE;


public class ClipboardMonitor {

    private static ClipboardMonitor instance = null;
    private ClipboardDataBaseOperateImpl clipboardDataBaseOperateImpl;
    private OnClipboardDataBaseOperateRecentFinish onClipboardDataBaseOperateRecentFinish;
    public static ClipboardMonitor getInstance() {
        if (instance == null) {
            instance = new ClipboardMonitor();
        }
        return instance;
    }

    private ClipboardMonitor() {
        clipboardDataBaseOperateImpl = ClipboardDataBaseOperateImpl.getInstance();
    }

    public void registerClipboardMonitor() {
        final ClipboardManager clipboard = (ClipboardManager) HSApplication.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
                public void onPrimaryClipChanged() {
                    CharSequence text = clipboard.getText();
                    if (!TextUtils.isEmpty(text)) {
                        String item = text.toString();
                        int currentRecentSize = clipboardDataBaseOperateImpl.getRecentAllContentFromTable().size();
                        HSLog.d(ClipboardMonitor.class.getSimpleName(), "     ClipboardMonitor    add  new data      " + item);
                        //用户新增recent数据，与recent不重复，则添加并置顶
                        if (currentRecentSize <= RECENT_TABLE_SIZE & !clipboardDataBaseOperateImpl.queryItemExistsInRecentTable(item)) {
                           boolean isSuccess =  clipboardDataBaseOperateImpl.addItemToBottomInRecentTable(item,clipboardDataBaseOperateImpl.queryItemExistsInPinsTable(item),currentRecentSize);
                            if(onClipboardDataBaseOperateRecentFinish==null){
                                return;
                            }
                           if (isSuccess){
                               ClipboardRecentViewAdapter.ClipboardRecentMessage  addRecent = clipboardDataBaseOperateImpl.getRecentItemFromTable(item);
                                   onClipboardDataBaseOperateRecentFinish.addRecentItemToTopSuccess(addRecent);
                           }else {
                                   int isPined = clipboardDataBaseOperateImpl.queryItemExistsInPinsTable(item);
                                   onClipboardDataBaseOperateRecentFinish.addItemToRecentTopFail(new ClipboardRecentViewAdapter.ClipboardRecentMessage(item, isPined));
                           }
                        }
                        //用户新增recent数据，与recent已有内容重复，则置顶重复内容
                        else if (clipboardDataBaseOperateImpl.queryItemExistsInRecentTable(item)) {
                            int lastPosition =  clipboardDataBaseOperateImpl.queryItemInRecentTableReversePosition(item);
                            ClipboardRecentViewAdapter.ClipboardRecentMessage  lastRecent = clipboardDataBaseOperateImpl.getRecentItemFromTable(item);
                            if(lastPosition>=0&&lastRecent!=null){
                                boolean isSuccess = clipboardDataBaseOperateImpl.setItemPositionToBottomInRecentTable(item,clipboardDataBaseOperateImpl.queryItemExistsInPinsTable(item));
                               if(onClipboardDataBaseOperateRecentFinish==null){
                                   return;
                               }
                                if(isSuccess){
                                        onClipboardDataBaseOperateRecentFinish.setRecentItemToTopSuccess(lastRecent, lastPosition);
                                }else {
                                        onClipboardDataBaseOperateRecentFinish.setRecentItemToTopFail(lastRecent, lastPosition);
                                }
                            }
                        }
                    }
                }
            });
        }
    }
   void setOnClipboardDataBaseOperateRecentFinish(OnClipboardDataBaseOperateRecentFinish onClipboardDataBaseOperateRecentFinish){
       this.onClipboardDataBaseOperateRecentFinish = onClipboardDataBaseOperateRecentFinish;
   }
   public  interface  OnClipboardDataBaseOperateRecentFinish{
       void addRecentItemToTopSuccess(ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage);
       void setRecentItemToTopSuccess(ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage, int position);
       void addItemToRecentTopFail(ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage);
       void setRecentItemToTopFail(ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage, int position);
   }
}
