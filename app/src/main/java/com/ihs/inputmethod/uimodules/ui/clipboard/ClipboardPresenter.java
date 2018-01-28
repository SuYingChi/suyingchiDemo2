package com.ihs.inputmethod.uimodules.ui.clipboard;
import com.ihs.inputmethod.uimodules.ui.common.adapter.PinsClipPanelViewAdapter;
import com.ihs.inputmethod.uimodules.ui.common.adapter.RecentClipboardAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yingchi.su on 2018/1/25.
 */

public class ClipboardPresenter implements RecentClipboardAdapter.SaveRecentItemToPins, PinsClipPanelViewAdapter.DeleteFromPinsToRecenet {

    static ClipboardPresenter clipboardPresenter;
    RecentClipboardAdapter recentClipboardAdapter;
    List<String> recentClipData = new ArrayList<String>();
    private  PinClipPanelView pinClipPanelView;
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
       recentClipData.addAll(ClipboardMonitor.getInstance().getRecentList());
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

    public   void recentDataOperate(List<String> list,String data) {
        if(list.size()<10&!list.contains(data)) {
            list.add(0, data);
        }else if(list.size() == 10&!list.contains(data)){
            list.remove(list.size()-1);
            list.add(0,data);
            recentClipboardAdapter.deleteAndFresh(data);
        }else if(list.contains(data)){
            list.remove(data);
            list.add(0,data);
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

    public PinClipPanelView getPinClipPanelView() {
        return pinClipPanelView;
    }

    public RecentClipPanelView getRecentClipPanelView() {
        return recentClipPanelView;
    }
    @Override
    public void saveToPins(String itemPinsContent) {
            addDataAndFresh(itemPinsContent);
    }
    @Override
    public void deletePinsItem(String pinsContentItem) {
        deleteAndFresh(pinsContentItem);
    }
}
