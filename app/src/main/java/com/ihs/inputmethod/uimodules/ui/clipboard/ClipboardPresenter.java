package com.ihs.inputmethod.uimodules.ui.clipboard;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;

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
    private final SharedPreferences recentSp;
    private final SharedPreferences pinsSp;
    private final String recentClip= "recentClip";
    private final String pinsClip = "pinsClip";
    private ClipboardRecentViewAdapter clipboardRecentViewAdapter;
    List<String> recentClipData = new ArrayList<String>();
    RecyclerView clipboardPanelPinsView;
    private ClipboardPinsViewAdapter clipboardPinsViewAdapter;
    private List<String> pinsData= new ArrayList<String>();
    RecyclerView clipboardPanelRecentView;
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
       recentSp = HSApplication.getContext().getSharedPreferences("recentClip", Context.MODE_PRIVATE);
       pinsSp = HSApplication.getContext().getSharedPreferences("pinsClip", Context.MODE_PRIVATE);
       loadArray(recentClipData,recentSp,recentClip);
       loadArray(pinsData,pinsSp,pinsClip);
        pinsData = getPinsClipData();
       clipboardRecentViewAdapter = new ClipboardRecentViewAdapter(recentClipData,this);
       clipboardPinsViewAdapter = new ClipboardPinsViewAdapter(pinsData,this);
   }
    void init(){

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

    public void setClipboardPanelPinsView(RecyclerView clipboardPanelPinsView) {
        this.clipboardPanelPinsView = clipboardPanelPinsView;

    }

    public void setClipboardPanelRecentView(RecyclerView clipboardPanelRecentView) {
        this.clipboardPanelRecentView = clipboardPanelRecentView;
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

    public  void loadArray(List<String> recentClipData,SharedPreferences sp,String spName) {

        recentClipData.clear();
        int size = sp.getInt("clipSize", 0);

        for(int i=0;i<size;i++) {
            recentClipData.add(sp.getString("clipValue" + i, null));
        }
        if(recentClipData.isEmpty()){
            clipDataResult.noData(spName);
        }
    }


    public RecyclerView getClipboardPanelPinsView(){
       return clipboardPanelPinsView;
    }
    public RecyclerView getClipboardPanelRecentView(){
        return clipboardPanelRecentView;
    }

    interface ClipDataResult{
        void noData(String spName);
    }
    public void setClipDataResult(ClipDataResult clipDataResult){
        this.clipDataResult = clipDataResult;
    }
    public ClipboardRecentViewAdapter getClipboardRecentViewAdapter(){
        return clipboardRecentViewAdapter;
    }
    public ClipboardPinsViewAdapter getClipboardPinsViewAdapter(){
        return clipboardPinsViewAdapter;
    }
}
