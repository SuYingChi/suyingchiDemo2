package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.Toast;

import com.ihs.app.framework.HSApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yingchi.su on 2018/1/25.
 */

public class ClipboardPresenter implements ClipboardRecentViewAdapter.SaveRecentItemToPinsListener, ClipboardPinsViewAdapter.DeleteFromPinsToRecentListener {
    static final int ADD_RECENT = 1;
    private static final int SAVE_TO_PINS = 2;
    private static final int DELETE_PINS = 3;
    private static final int RECENT_SP_DATA = 4;
    private static final int PINS_SP_DATA = 5;
    static final int RECENT = 6;
    static final int PINS = 7;
    static final int RECENT_SIZE = 10;
    private static final int PINS_SIZE = 30;
    private static ClipboardPresenter clipboardPresenter;
    private final SharedPreferences recentSp;
    private final SharedPreferences pinsSp;
    private ClipboardRecentViewAdapter clipboardRecentViewAdapter;
    private List<ClipboardRecentViewAdapter.ClipboardRecentMessage> clipRecentData = new ArrayList<ClipboardRecentViewAdapter.ClipboardRecentMessage>();
    private ClipboardPinsViewAdapter clipboardPinsViewAdapter;
    private List<String> clipPinsData = new ArrayList<String>();
    private boolean isClipRecentDataChange = false;
    private boolean isClipPinsDataChange = false;
    private OnMainViewCreatedListener onMainViewCreatedListener;


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
        final String CLIP_RECENT_SP = "CLIP_RECENT_SP_NAME";
        final String CLIP_PINS_SP = "CLIP_PINS_SP_NAME";
        recentSp = HSApplication.getContext().getSharedPreferences(CLIP_RECENT_SP, Context.MODE_PRIVATE);
        pinsSp = HSApplication.getContext().getSharedPreferences(CLIP_PINS_SP, Context.MODE_PRIVATE);
        //一开始 没本地数据则不创建适配器，有则创建
        initClipboardAdapters();
    }

    private void initClipboardAdapters() {
        loadClipDataFromSp(clipRecentData, recentSp, RECENT_SP_DATA);
        loadClipDataFromSp(clipPinsData, pinsSp, PINS_SP_DATA);
        if (!clipRecentData.isEmpty()) {
            clipboardRecentViewAdapter = new ClipboardRecentViewAdapter(clipRecentData, this);
        }
        if (!clipPinsData.isEmpty()) {
            clipboardPinsViewAdapter = new ClipboardPinsViewAdapter(clipPinsData, this);
        }
    }

    //新增复制内容时,点击收藏，删除收藏内容时的处理数据的接口，并标明是否有数据变化，有变化保存数据到SP里，
    void clipDataOperate(String data, int dataSize, int operateType) {
        List<Boolean> clipRecentDataIsPined = new ArrayList<Boolean>();
        List<String> clipPinsDatatemp = new ArrayList<String>();
        List<String> clipRecentContent = new ArrayList<String>();
        List<ClipboardRecentViewAdapter.ClipboardRecentMessage> clipRecent = new ArrayList<ClipboardRecentViewAdapter.ClipboardRecentMessage>();
        // 实时监听用户复制数据操作
        for (ClipboardRecentViewAdapter.ClipboardRecentMessage recentMessage : clipRecentData) {
            clipRecentContent.add(recentMessage.getRecentClipItemContent());
            clipRecentDataIsPined.add(recentMessage.getPined());
        }
        clipPinsDatatemp.addAll(clipPinsData);
        //实时监听用户点击Recent页面的收藏按钮时的数据操作
        //recent里点击收藏,收藏内容已经有30条，recent页面点击收藏时，收藏里还没有，提示不能再添加
        if (operateType == SAVE_TO_PINS) {
            if (clipPinsDatatemp.size() == dataSize & !clipPinsDatatemp.contains(data)) {
                Toast.makeText(HSApplication.getContext(), "You can add at most 30 message", Toast.LENGTH_LONG).show();
                isClipPinsDataChange = false;
                isClipRecentDataChange = false;
                return;
            }//recent里点击收藏，收藏里已经有了，则在recent里去除本条，收藏里置顶该条
            else if (clipPinsDatatemp.contains(data)) {
                clipPinsDatatemp.remove(clipPinsDatatemp.size() - 1);
                clipPinsDatatemp.add(0, data);
                int index = clipRecentContent.indexOf(data);
                clipRecentContent.remove(data);
                clipRecentDataIsPined.remove(index);
            }//recent 里点击收藏，收藏里还没有，并且收藏内容小于30条，则添加内容到收藏，并在recent里删除该条。
            else if (clipPinsDatatemp.size() < dataSize & !clipPinsDatatemp.contains(data)) {
                clipPinsDatatemp.add(0, data);
                int index = clipRecentContent.indexOf(data);
                clipRecentContent.remove(data);
                clipRecentDataIsPined.remove(index);
            }
            //数据处理完，更新到成员变量和SP里
            for (int i = 0; i < clipRecentContent.size(); i++) {
                String recentContent = clipRecentContent.get(i);
                boolean isPined = clipRecentDataIsPined.get(i);
                clipRecent.add(i, new ClipboardRecentViewAdapter.ClipboardRecentMessage(recentContent, isPined));
            }
            if (!clipRecentData.equals(clipRecent)) {
                clipRecentData.clear();
                clipRecentData.addAll(clipRecent);
                saveClipDataToSp(recentSp.edit(), clipRecentData, RECENT);
                isClipRecentDataChange = true;
            } else {
                isClipRecentDataChange = false;
            }
            if (clipPinsData.equals((clipPinsDatatemp))) {
                clipPinsDatatemp.clear();
                clipPinsData.addAll(clipPinsDatatemp);
                saveClipDataToSp(pinsSp.edit(), clipPinsData, PINS);
                isClipPinsDataChange = true;
            } else {
                isClipPinsDataChange = false;
            }
        }
        //用户新增recent数据，小于最大条数并且内容未与recent重复增加新内容,标明收藏是否存有，并置顶
        else if (operateType == ADD_RECENT) {
            if (clipRecentContent.size() < dataSize & !clipRecentContent.contains(data)) {
                clipRecentContent.add(0, data);
                if (clipPinsDatatemp.contains(data)) {
                    clipRecentDataIsPined.add(0, true);
                } else if (!clipPinsDatatemp.contains(data)) {
                    clipRecentDataIsPined.add(0, false);
                }
            }//用户新增recent数据，recent已经是最大条数，并且未与recent重复,则删除最后一条，标明收藏是否存有，将新内容置顶
            else if (clipRecentContent.size() == dataSize & !clipRecentContent.contains(data)) {
                clipRecentContent.remove(clipRecentContent.size() - 1);
                clipRecentDataIsPined.remove(clipRecentContent.size() - 1);
                clipRecentContent.add(0, data);
                if (clipPinsDatatemp.contains(data)) {
                    clipRecentDataIsPined.add(0, true);
                } else if (!clipPinsDatatemp.contains(data)) {
                    clipRecentDataIsPined.add(0, false);
                }
            }
            //用户新增recent数据，与recent已有内容重复，则置顶重复内容
            else if (clipRecentContent.contains(data)) {
                int index = clipRecentContent.indexOf(data);
                clipRecentContent.remove(data);
                clipRecentContent.add(0, data);
                boolean isPined = clipRecentDataIsPined.get(index);
                clipRecentDataIsPined.remove(index);
                clipRecentDataIsPined.add(0, isPined);

            }
            //数据处理完，更新到成员变量和SP里
            for (int i = 0; i < clipRecentContent.size(); i++) {
                String recentContent = clipRecentContent.get(i);
                boolean isPined = clipRecentDataIsPined.get(i);
                clipRecent.add(i, new ClipboardRecentViewAdapter.ClipboardRecentMessage(recentContent, isPined));
            }
            if (!clipRecentData.equals(clipRecent)) {
                clipRecentData.clear();
                clipRecentData.addAll(clipRecent);
                saveClipDataToSp(recentSp.edit(), clipRecentData, RECENT);
                isClipRecentDataChange = true;
            } else {
                isClipRecentDataChange = false;
            }

        } else if (operateType == DELETE_PINS) {
            //用户删除PINS数据，recent里没有
            if (!clipRecentContent.contains(data)) {
                clipPinsDatatemp.remove(data);
            }//用户删除PINS数据，recent里有,标明recent里该项内容已不被收藏
            else if (clipRecentContent.contains(data)) {
                clipPinsDatatemp.remove(data);
                int index = clipRecentContent.indexOf(data);
                clipRecentDataIsPined.remove(index);
                clipRecentDataIsPined.add(index, false);
            }
            //数据处理完，更新到成员变量和SP里
            for (int i = 0; i < clipRecentContent.size(); i++) {
                String recentContent = clipRecentContent.get(i);
                boolean isPined = clipRecentDataIsPined.get(i);
                clipRecent.add(i, new ClipboardRecentViewAdapter.ClipboardRecentMessage(recentContent, isPined));
            }
            if (!clipRecentData.equals(clipRecent)) {
                clipRecentData.clear();
                clipRecentData.addAll(clipRecent);
                saveClipDataToSp(recentSp.edit(), clipRecentData, RECENT);
                isClipRecentDataChange = true;
            } else {
                isClipRecentDataChange = false;
            }
            if (!clipPinsData.equals(clipPinsDatatemp)) {
                clipPinsData.clear();
                clipPinsData.addAll(clipPinsDatatemp);
                saveClipDataToSp(pinsSp.edit(), clipPinsData, PINS);
                isClipPinsDataChange = true;
            } else {
                isClipPinsDataChange = false;
            }
        }
        //如果数据不为空了，并且适配器还没创建，则创建适配器,并通知对应的recyclerView
        if (!clipRecentData.isEmpty() && clipboardRecentViewAdapter == null&&onMainViewCreatedListener !=null) {
            clipboardRecentViewAdapter = new ClipboardRecentViewAdapter(clipRecentData, this);
            onMainViewCreatedListener.adapterCreated(clipboardRecentViewAdapter, ADD_RECENT);
        }
        if (!clipPinsData.isEmpty() && clipboardPinsViewAdapter == null&&onMainViewCreatedListener !=null) {
            clipboardPinsViewAdapter = new ClipboardPinsViewAdapter(clipPinsData, this);
            onMainViewCreatedListener.adapterCreated(clipboardPinsViewAdapter, SAVE_TO_PINS);
        }
    }
    //点击recent的条目的pins按钮时执行的回调
    @Override
    public void saveToPins(String itemPinsContent, ImageView recentItemImageView, int position) {
        clipDataOperate(itemPinsContent, PINS_SIZE, SAVE_TO_PINS);
        refreshClipboard();

    }

    //点击pins的条目的删除按钮时的回调
    @Override
    public void deletePinsItem(String pinsContentItem, int position) {
        clipDataOperate(pinsContentItem, DELETE_PINS);
        refreshClipboard();

    }

    private void clipDataOperate(String pinsContentItem, int deletePins) {
        clipDataOperate(pinsContentItem, 0, deletePins);
    }


    private void saveClipDataToSp(SharedPreferences.Editor editor, List clipData, int ViewType) {
        editor.clear();
        editor.putInt("clipSize", clipData.size());
        for (int i = 0; i < clipData.size(); i++) {
            if (ViewType == RECENT) {
                ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage = (ClipboardRecentViewAdapter.ClipboardRecentMessage) clipData.get(i);
                String recentClipItemContent = clipboardRecentMessage.getRecentClipItemContent();
                boolean pined = clipboardRecentMessage.getPined();
                editor.putString("clipValue" + i, recentClipItemContent);
                editor.putBoolean("clipValuePined" + i, pined);
            } else if (ViewType == PINS) {
                editor.putString("clipValue" + i, (String) clipData.get(i));
            }
        }

        editor.commit();
    }

    private void loadClipDataFromSp(List clipData, SharedPreferences sp, int ViewType) {

        clipData.clear();
        int size = sp.getInt("clipSize", 0);

        for (int i = 0; i < size; i++) {
            if (ViewType == RECENT) {
                ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage = new ClipboardRecentViewAdapter.ClipboardRecentMessage();
                clipboardRecentMessage.setRecentClipItemContent(sp.getString("clipValue" + i, null));
                clipboardRecentMessage.setPined(sp.getBoolean("clipValuePined" + i, false));
                clipData.add(clipboardRecentMessage);
            } else if (ViewType == PINS) {
                clipData.add(sp.getString("clipValue" + i, null));
            }
        }
    }
    void refreshClipboard() {
        if (isClipRecentDataChange && onMainViewCreatedListener != null && clipboardRecentViewAdapter != null) {
            clipboardRecentViewAdapter.dataChangeAndRefresh(clipRecentData);
            isClipRecentDataChange = false;
        }
        if (isClipPinsDataChange && onMainViewCreatedListener != null && clipboardPinsViewAdapter != null) {
            clipboardPinsViewAdapter.dataChangeAndRefresh(clipPinsData);
            isClipPinsDataChange = false;
        }
    }

    //adapter在程序一开启就创建，MainView创建后presenter获得该接口实例，将adapter创建完成后回传给MainView，mainView再将adapter贴上去
    public interface OnMainViewCreatedListener {
        void adapterCreated(RecyclerView.Adapter adapter, int adapterType);
    }

    void setOnMainViewCreatedListener(OnMainViewCreatedListener onMainViewCreatedListener) {
        this.onMainViewCreatedListener = onMainViewCreatedListener;
    }

    StateListDrawable getClipActionBarBtnViewBackgroundDrawable() {
        StateListDrawable background = new StateListDrawable();
        Drawable bg = new ColorDrawable(Color.TRANSPARENT);
        Drawable pressedBg = new ColorDrawable(Color.parseColor("#1AFFFFFF"));

        background.addState(new int[]{android.R.attr.state_focused}, pressedBg);
        background.addState(new int[]{android.R.attr.state_pressed}, pressedBg);
        background.addState(new int[]{android.R.attr.state_selected}, pressedBg);
        background.addState(new int[]{}, bg);
        return background;
    }

    public Drawable pinedBackground() {
        return new ColorDrawable(Color.parseColor("#1AFFFFFF"));
    }

    public Drawable noPinedBackground() {
        return new ColorDrawable(Color.TRANSPARENT);
    }
}
