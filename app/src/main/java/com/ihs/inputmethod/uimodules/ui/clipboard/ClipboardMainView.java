package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.alerts.KCAlert;
import com.kc.utils.KCAnalytics;

import java.util.ArrayList;
import java.util.List;



public final class ClipboardMainView extends LinearLayout implements ClipboardActionBar.OnClipboardTabChangeListener, ClipboardContract.ClipboardView, ClipboardRecentViewAdapter.OnRecentItemPinClickedListener, ClipboardPinsViewAdapter.OnPinItemDeletedClickListener, ClipboardMonitor.OnClipboardRecentDataChangeListener {

    private static final String TAG = ClipboardMainView.class.getSimpleName();
    private ClipboardRecentViewAdapter clipboardRecentViewAdapter;
    private ClipboardPinsViewAdapter clipboardPinsViewAdapter;
    private ClipboardActionBar actionBar;
    private RecyclerView clipboardPanelPinsView;
    private RecyclerView clipboardPanelRecentView;
    private ClipboardPresenter clipboardPresenter;
    private FrameLayout recyclerViewGroup;
    private RecyclerView currentView = null;
    private KCAlert deleteAlert;
    List<String> tabNameList = new ArrayList<String>();
    private Handler uiHandler = new Handler();
    static final int KEYBOARD_HEIGHT = HSResourceUtils.getDefaultKeyboardHeight(HSApplication.getContext().getResources());
    final int BACKGROUND_COLOR = HSKeyboardThemeManager.getCurrentTheme().getDominantColor();

    public ClipboardMainView(Context context) {
        super(context);
        clipboardPresenter = new ClipboardPresenter(this);
        setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, KEYBOARD_HEIGHT));
        setOrientation(VERTICAL);
        setBackgroundColor(BACKGROUND_COLOR);
        addRecyclerViewGroup();
        addRecyclerViewToPanelViewGroup();
        currentView = clipboardPanelRecentView;
        addBarView();
        tabNameList.add(ClipboardConstants.PANEL_RECENT);
        tabNameList.add(ClipboardConstants.PANEL_PIN);
        actionBar.setActionBarTabName(tabNameList);
        //初始的时候recent在显示，相应按钮设为被选中
        actionBar.setCurrentClipboardTab((String) currentView.getTag());
        ClipboardMonitor.getInstance().setOnClipboardRecentDataChangeListener(this);
    }


    @Override
    public void onClipboardTabChange(String tabName) {
        if (tabName.equals(ClipboardConstants.PANEL_RECENT)) {
            switchToRecentView();
        } else if (tabName.equals(ClipboardConstants.PANEL_PIN)) {
            switchToPinsView();
        }
    }

    private void showPanelView(RecyclerView recyclerViewToShow) {
            //如果此时选中的view　已经在显示，则不做处理
            if (recyclerViewToShow.equals(currentView)) {
                HSLog.e(ClipboardMainView.class.getSimpleName(), "selected clipboard view has been Showing");
            }else {
                //如果此时选中的view没有在显示但是已经添加，并已有VIEW在显示，则隐藏原来的view 并显示选中的view，设置为currentView
                HSLog.e(ClipboardMainView.class.getSimpleName(), "recyclerViewToShow   "+(String) recyclerViewToShow.getTag()+"currentView "+(String)currentView.getTag());
                currentView.setVisibility(INVISIBLE);
                recyclerViewToShow.setVisibility(VISIBLE);
                currentView = recyclerViewToShow;
            }
    }

    public void showDeletedSuggestionAlert(String selectedPinsItem,int selectPinItemPosition) {
        //如果不把方法接收的参数存为局部变量的话，在方法匿名内部类的onclick里只能接收到第一次传进来的参数
        String pinsItem = selectedPinsItem;
        int pinItemPosition = selectPinItemPosition;
        if (deleteAlert == null) {
            deleteAlert = new KCAlert.Builder(HSApplication.getContext())
                    .setTitle(getResources().getString(R.string.clipboard_delete_title))
                    .setMessage(getResources().getString(R.string.clipboard_delete_message))
                    .setPositiveButton(getResources().getString(R.string.cancel), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            deleteAlert.dismiss();
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.clipboard_delete_pin_confirm), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            clipboardPresenter.deletePinItem(pinsItem,pinItemPosition);
                            KCAnalytics.logEvent("keyboard_clipboard_pin_delete");
                            deleteAlert.dismiss();
                        }
                    }).build();
        }
        deleteAlert.show();

    }

    private void switchToPinsView() {
        showPanelView(clipboardPanelPinsView);
        HSLog.d(TAG,"show clipboardPanelPinsView");
        actionBar.setCurrentClipboardTab((String) currentView.getTag());
    }

    private void switchToRecentView() {
        showPanelView(clipboardPanelRecentView);
        HSLog.d(TAG,"show clipboardPanelRecentView");
        actionBar.setCurrentClipboardTab((String) currentView.getTag());
    }

    private void addRecyclerViewToPanelViewGroup() {

        LinearLayoutManager recentLayoutManager = new LinearLayoutManager(getContext());
        recentLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        clipboardPanelRecentView = new RecyclerView(getContext());
        clipboardPanelRecentView.setLayoutManager(recentLayoutManager);
        clipboardPanelRecentView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        clipboardRecentViewAdapter = new ClipboardRecentViewAdapter(clipboardPresenter.getClipRecentData(), this);
        clipboardPanelRecentView.setTag(ClipboardConstants.PANEL_RECENT);
        clipboardPanelRecentView.setAdapter(clipboardRecentViewAdapter);
        clipboardPanelRecentView.setVisibility(VISIBLE);

        LinearLayoutManager pinsLayoutManager = new LinearLayoutManager(getContext());
        pinsLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        clipboardPanelPinsView = new RecyclerView(getContext());
        clipboardPanelPinsView.setLayoutManager(pinsLayoutManager);
        clipboardPanelPinsView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        clipboardPinsViewAdapter = new ClipboardPinsViewAdapter(clipboardPresenter.getClipPinsData(), this);
        clipboardPanelPinsView.setTag(ClipboardConstants.PANEL_PIN);
        clipboardPanelPinsView.setAdapter(clipboardPinsViewAdapter);
        clipboardPanelPinsView.setVisibility(INVISIBLE);

        recyclerViewGroup.addView(clipboardPanelRecentView, new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        recyclerViewGroup.addView(clipboardPanelPinsView, new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

    }

    private void addRecyclerViewGroup() {
        recyclerViewGroup = new FrameLayout(this.getContext());
        LayoutParams panelViewGroupLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, KEYBOARD_HEIGHT - HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.emoticon_panel_actionbar_height));
        //防止点击事件穿透
        recyclerViewGroup.setClickable(true);
        addView(recyclerViewGroup, panelViewGroupLayoutParams);
    }

    private void addBarView() {
        actionBar = new ClipboardActionBar(getContext());
        actionBar.setOnClipboardTabChangeListener(this);
        addView(actionBar,new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.emoticon_panel_actionbar_height)));
    }


    /**
     * 点击recent的条目的pins按钮时执行的回调
     * @param item 点击的recent Item的文本内容，在UI列表的position
     * @param position 在UI列表的position
     */
    @Override
    public void onSaveRecentItemToPinsBtnClick(String item, int position) {
        clipboardPresenter.saveRecentItemToPins(item,position);
        KCAnalytics.logEvent("keyboard_clipboard_save_recent_to_pins_clicked");

    }

    /**
     * 点击Pins的条目的删除按钮时执行的回调
     * @param pinsContentItem 点击的pin Item的文本内容
     * @param position 在UI列表的position
     */
    @Override
    public void onDeletePinsItemBtnClick(String pinsContentItem, int position) {
        showDeletedSuggestionAlert(pinsContentItem,position);
        KCAnalytics.logEvent("keyboard_clipboard_delete_pin_item_clicked");
    }


    //UI更新接口
    @Override
    public void onNewRecentAddSuccess(ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage) {
        switchToRecentView();
        clipboardRecentViewAdapter.insertDataAndNotifyDataSetChange(clipboardRecentMessage);
        clipboardPanelRecentView.scrollToPosition(0);
    }

    @Override
    public void onDeleteTheLastRecentAndNewRecentAddSuccess(ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage) {
        switchToRecentView();
        clipboardRecentViewAdapter.deleteDataChangeAndNotifyDataSetChange(ClipboardConstants.RECENT_TABLE_SIZE - 1);
        clipboardRecentViewAdapter.insertDataAndNotifyDataSetChange(clipboardRecentMessage);
        clipboardPanelRecentView.scrollToPosition(0);
    }


    @Override
    public void onExistRecentAddSuccess(ClipboardRecentViewAdapter.ClipboardRecentMessage exitsClipboardRecentMessage) {
        switchToRecentView();
        //为防止数据库数据与adapter维护的list数据不一致，从adapter获取position和数据去更新UI
        clipboardRecentViewAdapter.moveRecentItemToTopAndNotifyDataSetChange(exitsClipboardRecentMessage);
        clipboardPanelRecentView.scrollToPosition(0);
    }

    @Override
    public void onDeleteRecentAndAddPinSuccess(String selectedRecentItem, int selectedRecentItemPosition) {
        switchToRecentView();
        clipboardRecentViewAdapter.deleteDataChangeAndNotifyDataSetChange(selectedRecentItemPosition);
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                switchToPinsView();
                clipboardPinsViewAdapter.insertDataAndNotifyDataSetChange(selectedRecentItem);
                clipboardPanelPinsView.scrollToPosition(0);
            }
        }, ClipboardConstants.SHOW_VIEW_DURATION);
    }

    @Override
    public void onDeleteRecentAndMovePinToTopSuccess(String selectedRecentItem, int selectedRecentItemPosition) {
        switchToRecentView();
        clipboardRecentViewAdapter.deleteDataChangeAndNotifyDataSetChange(selectedRecentItemPosition);
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                switchToPinsView();
                //为防止数据库数据与adapter维护的list数据不一致，从adapter获取position和数据去更新UI
               // int pinsLastPosition = clipboardPinsViewAdapter.getPinsDataList().indexOf(selectedRecentItem);
                HSLog.d(TAG, "clipboardMainView  "+"----selectedRecentItem---" + selectedRecentItem);
                clipboardPinsViewAdapter.movePinsItemToTopAndNotifyDataSetChange(selectedRecentItem);
                clipboardPanelPinsView.scrollToPosition(0);
            }
        }, ClipboardConstants.SHOW_VIEW_DURATION);
    }

    @Override
    public void onDeletePinSuccess(int selectRecentPinItemPosition) {
        switchToPinsView();
        clipboardPinsViewAdapter.deleteDataAndNotifyDataSetChange(selectRecentPinItemPosition);
    }

    @Override
    public void onDeletePinAndUnpinRecentSuccess(ClipboardRecentViewAdapter.ClipboardRecentMessage recentItem, int selectPinItemPosition) {
        switchToPinsView();
        clipboardPinsViewAdapter.deleteDataAndNotifyDataSetChange(selectPinItemPosition);
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                switchToRecentView();
                //为防止数据库数据与adapter维护的list数据不一致，从adapter获取position和数据去更新UI,并不对外开放适配器的list
                HSLog.d(TAG,"onDeletePinAndUnpinRecentSuccess  ====  "+"recentItem====="+recentItem);
                clipboardRecentViewAdapter.itemChangedAndNotifyDataSetChange(recentItem);
            }
        }, ClipboardConstants.SHOW_VIEW_DURATION);
    }

    @Override
    public void onNewRecentAddFail(ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage) {
        Toast.makeText(HSApplication.getContext(), R.string.clipboard_operate_fail, Toast.LENGTH_SHORT).show();
        HSLog.d(TAG," Failure to add new recent item to the database.");
    }

    @Override
    public void onExistRecentAddFail(ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage) {
        Toast.makeText(HSApplication.getContext(), R.string.clipboard_operate_fail, Toast.LENGTH_SHORT).show();
        HSLog.d(TAG," Failure to move exist recent item to the bottom of database.");
    }


    @Override
    public void onDeletePinFail(int selectRecentPinItemPosition) {
        Toast.makeText(HSApplication.getContext(), R.string.clipboard_operate_fail, Toast.LENGTH_SHORT).show();
        HSLog.d(TAG," Failure to delete pin item to the database.");
    }

    @Override
    public void onDeletePinAndUnpinRecentFail(ClipboardRecentViewAdapter.ClipboardRecentMessage recentItem,int selectPinItemPosition) {
        Toast.makeText(HSApplication.getContext(), R.string.clipboard_operate_fail, Toast.LENGTH_SHORT).show();
        HSLog.d(TAG," Failure to delete pin item and unpin recent to the database.");
    }

    @Override
    public void onDeleteRecentAndMovePinToTopFail(String lastPinsItem,int exitsPinItemPosition) {
        Toast.makeText(HSApplication.getContext(), R.string.clipboard_operate_fail, Toast.LENGTH_SHORT).show();
        HSLog.d(TAG," Failure to delete recent item and move exist pin To bottom to the database.");
    }

    @Override
    public void onDeleteRecentAndAddPinFail(String selectedRecentItem,int selectedRecentItemPosition) {
        Toast.makeText(HSApplication.getContext(), R.string.clipboard_operate_fail, Toast.LENGTH_SHORT).show();
        HSLog.d(TAG," Failure to delete recent item and add Pin To bottom to the database.");
    }

    @Override
    public void onDeleteTheLastRecentAndNewRecentAddFail(ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage) {
        Toast.makeText(HSApplication.getContext(), R.string.clipboard_operate_fail, Toast.LENGTH_SHORT).show();
        HSLog.d(TAG," Failure to delete the last recent item and add recent To bottom to the database.");
    }
}
