package com.ihs.inputmethod.uimodules.ui.clipboard;
import android.content.Context;
import android.content.SharedPreferences;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.ui.common.adapter.PinsClipPanelViewAdapter;
import com.ihs.inputmethod.uimodules.ui.common.adapter.RecentClipboardAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yingchi.su on 2018/1/25.
 */

public class ClipboardPresenter implements RecentClipboardAdapter.SaveRecentItemToPins, PinsClipPanelViewAdapter.DeleteFromPinsToRecenet {

    static ClipboardPresenter clipboardPresenter;
    private final SharedPreferences sp;
    RecentClipboardAdapter recentClipboardAdapter;
    List<String> recentClipData = new ArrayList<String>();
    PinClipPanelView pinClipPanelView;
    private PinsClipPanelViewAdapter pinsClipPanelViewAdapter;
    private List<String> pinsData= new ArrayList<String>();
    RecentClipPanelView recentClipPanelView;


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
       recentClipboardAdapter = new RecentClipboardAdapter(recentClipData,this);
        pinsClipPanelViewAdapter= new PinsClipPanelViewAdapter(pinsData,this);
        pinsData = getPinsClipData();
   }

    private List<String> getPinsClipData() {

        return null;
    }


    public void deleteAndFresh(String pinsContentItem) {
         recentClipboardAdapter.deleteAndFresh(pinsContentItem);
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
        pinsClipPanelViewAdapter.addDataAndFresh(itemPinsContent);
    }

    public void setPinClipPanelView(PinClipPanelView pinClipPanelView) {
        this.pinClipPanelView = pinClipPanelView;
    }

    public void setRecentClipPanelView(RecentClipPanelView recentClipPanelView) {
        this.recentClipPanelView = recentClipPanelView;
    }

    @Override
    public void saveToPins(String itemPinsContent) {
            addDataAndFresh(itemPinsContent);
    }
    @Override
    public void deletePinsItem(String pinsContentItem) {
        deleteAndFresh(pinsContentItem);
    }

    public void setRecentClipData(List<String> recentClipData) {
        this.recentClipData = recentClipData;
    }

    public void setPinsData(List<String> pinsData) {
        this.pinsData = pinsData;
    }

    public List<String> getPinsData() {
        return pinsData;
    }

    public List<String> getRecentClipData() {
        return recentClipData;
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
    }

    public PinsClipPanelViewAdapter getPinsClipPanelViewAdapter() {
        return pinsClipPanelViewAdapter;
    }
}
