package com.ihs.inputmethod.uimodules.ui.clipboard;
import android.content.Context;
import android.content.SharedPreferences;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.ui.common.adapter.ClipboardPinsViewAdapter;
import com.ihs.inputmethod.uimodules.ui.common.adapter.ClipboardRecentViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yingchi.su on 2018/1/25.
 */

public class ClipboardPresenter implements ClipboardRecentViewAdapter.SaveRecentItemToPins, ClipboardPinsViewAdapter.DeleteFromPinsToRecenet {

    static ClipboardPresenter clipboardPresenter;
    private final SharedPreferences sp;
    private ClipboardRecentViewAdapter clipboardRecentViewAdapter;
    List<String> recentClipData = new ArrayList<String>();
    ClipboardPanelPinsView clipboardPanelPinsView;
    private ClipboardPinsViewAdapter clipboardPinsViewAdapter;
    private List<String> pinsData= new ArrayList<String>();
    ClipboardPanelRecentView clipboardPanelRecentView;
    ClipDataResult clipDataResult;
    public static ClipboardPresenter getInstance(){
        synchronized (ClipboardPresenter.class){
         if(clipboardPresenter == null){
           clipboardPresenter = new ClipboardPresenter();
         }
        }
        return clipboardPresenter;
    }

   private  ClipboardPresenter(){
       sp = HSApplication.getContext().getSharedPreferences("recentClip", Context.MODE_PRIVATE);
       SharedPreferences.Editor recentClipSpEditor = sp.edit();
       loadArray();
        pinsData = getPinsClipData();
       clipboardRecentViewAdapter = new ClipboardRecentViewAdapter(recentClipData,this);
       clipboardPinsViewAdapter = new ClipboardPinsViewAdapter(pinsData,this);
   }

    private List<String> getPinsClipData() {

        return null;
    }


    public void deleteAndFresh(String pinsContentItem) {
         clipboardRecentViewAdapter.deleteAndFresh(pinsContentItem);
    }

    public   void recentDataOperate(String data) {
        if(recentClipData.size()<10&!recentClipData.contains(data)) {
            recentClipData.add(0, data);
        }else if(recentClipData.size() == 10&!recentClipData.contains(data)){
            recentClipData.remove(recentClipData.size()-1);
            recentClipData.add(0,data);
            deleteAndFresh(data);
        }else if(recentClipData.contains(data)){
            recentClipData.remove(data);
            recentClipData.add(0,data);
        }
    }
    public void addDataAndFresh(String itemPinsContent) {
        clipboardPinsViewAdapter.addDataAndFresh(itemPinsContent);
    }

    public void setClipboardPanelPinsView(ClipboardPanelPinsView clipboardPanelPinsView) {
        this.clipboardPanelPinsView = clipboardPanelPinsView;
        this.clipboardPanelPinsView.setAdapter(clipboardPinsViewAdapter);
    }

    public void setClipboardPanelRecentView(ClipboardPanelRecentView clipboardPanelRecentView) {
        this.clipboardPanelRecentView = clipboardPanelRecentView;
        this.clipboardPanelRecentView.setAdapter(clipboardRecentViewAdapter);
    }

    @Override
    public void saveToPins(String itemPinsContent) {
            addDataAndFresh(itemPinsContent);
    }
    @Override
    public void deletePinsItem(String pinsContentItem) {
        deleteAndFresh(pinsContentItem);
    }


    public  boolean saveArrayToSp(SharedPreferences.Editor editor) {
        editor.putInt("clipSize", recentClipData.size());

        for (int i = 0; i < recentClipData.size(); i++) {
            editor.putString("clipValue" + i, recentClipData.get(i));
        }

        return editor.commit();
    }

    public  void loadArray() {

        recentClipData.clear();
        int size = sp.getInt("clipSize", 0);

        for(int i=0;i<size;i++) {
            recentClipData.add(sp.getString("clipValue" + i, null));
        }
        if(recentClipData.isEmpty()){
            clipDataResult.noData();
        }
    }


    public ClipboardPanelPinsView getClipboardPanelPinsView(){
       return clipboardPanelPinsView;
    }
    public ClipboardPanelRecentView getClipboardPanelRecentView(){
        return clipboardPanelRecentView;
    }

    interface ClipDataResult{
        void noData();
    }
    public void setClipDataResult(ClipDataResult clipDataResult){
        this.clipDataResult = clipDataResult;
    }
}
