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

import static com.ihs.inputmethod.uimodules.ui.clipboard.ClipboardPresenter.RECENT_TABLE_SIZE;


public final class ClipboardMainView extends LinearLayout implements ClipboardActionBar.ClipboardTabChangeListener, ClipboardContact.ClipboardView, ClipboardRecentViewAdapter.SaveRecentItemToPinsListener, ClipboardPinsViewAdapter.DeleteFromPinsToRecentListener, ClipboardMonitor.OnClipboardRecentDataChangeListener {

    private static final String TAG = ClipboardMainView.class.getSimpleName();
    private ClipboardRecentViewAdapter clipboardRecentViewAdapter;
    private ClipboardPinsViewAdapter clipboardPinsViewAdapter;
    private ClipboardActionBar actionBar;
    private RecyclerView clipboardPanelPinsView;
    private RecyclerView clipboardPanelRecentView;
    private ClipboardPresenter clipboardPresenter;
    private FrameLayout recyclerViewGroup;
    private RecyclerView currentView = null;
    int keyboardHeight = HSResourceUtils.getDefaultKeyboardHeight(getResources());
    private KCAlert deleteAlert;
    public final static String PANEL_RECENT = "Recent";
    public final static String PANEL_PIN = "Pins";
    private final static int SHOW_VIEW_DURATION = 600;
    List<String> tabNameList = new ArrayList<String>();
    private int selectRecentPinItemPosition;
    private int selectedRecentItemPosition;
    private Handler uiHandler = new Handler();
    private String selectedRecentItem;
    private String selectedPinsItem;

    public ClipboardMainView(Context context) {
        super(context);
        clipboardPresenter = new ClipboardPresenter();
        setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
        setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, keyboardHeight));
        setOrientation(VERTICAL);
        //填充frameLayout(填充RecyclerView)到此LinearLayout
        addRecyclerViewGroup();
        //填充RecyclerView到frameLayout
        addRecyclerViewToPanelViewGroup();
        //填充底部actionbar到此LinearLayout
        addBarView();
        //recyclerView与actionbar建立关联
        tabNameList.add(PANEL_RECENT);
        tabNameList.add(PANEL_PIN);
        actionBar.relateToActionBar(tabNameList);
        //初始的时候recent在显示，相应按钮设为被选中
        actionBar.selectedViewBtn((String) currentView.getTag());
        clipboardPresenter.setClipboardView(this);
        ClipboardMonitor.getInstance().setOnClipboardRecentDataChangeListener(this);
    }


    @Override
    public void onTabChange(String tabName) {
        if (tabName.equals(PANEL_RECENT)) {
            switchToRecentView();
        } else if (tabName.equals(PANEL_PIN)) {
            switchToPinsView();
        }
    }

    private void showPanelView(RecyclerView recyclerViewToShow) {
            //如果此时选中的view　已经在显示，则不做处理
            if (recyclerViewToShow.equals(currentView)) {
                HSLog.e(ClipboardMainView.class.getSimpleName(), "selected clipboard view has been Showing");
            }else {
                //如果此时选中的view没有在显示但是已经添加，并已有VIEW在显示，则隐藏原来的view 并显示选中的view，设置为currentView
                currentView.setVisibility(INVISIBLE);
                recyclerViewToShow.setVisibility(VISIBLE);
                currentView = recyclerViewToShow;
            }
    }

    public void showDeletedSuggestionAlert() {
        if (deleteAlert == null) {
            deleteAlert = new KCAlert.Builder(HSApplication.getContext())
                    .setTitle(getResources().getString(R.string.clipboard_delete_title))
                    .setMessage(getResources().getString(R.string.clipboard_delete_message))
                    .setPositiveButton(getResources().getString(R.string.cancel), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            deleteAlert.dismiss();
                            KCAnalytics.logEvent("keyboard_clipboard_pin_cancelled");
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.clipboard_delete_pin_confirm), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            clipboardPresenter.clipDataOperateDeletePins(selectedPinsItem);
                            deleteAlert.dismiss();
                        }
                    }).build();
        }
        deleteAlert.show();

    }

    private void switchToPinsView() {
        showPanelView(clipboardPanelPinsView);
        actionBar.selectedViewBtn((String) currentView.getTag());
    }

    private void switchToRecentView() {
        showPanelView(clipboardPanelRecentView);
        actionBar.selectedViewBtn((String) currentView.getTag());
    }

    private void addRecyclerViewToPanelViewGroup() {
        LinearLayoutManager recentLayoutManager = new LinearLayoutManager(getContext());
        recentLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        LinearLayoutManager pinsLayoutManager = new LinearLayoutManager(getContext());
        pinsLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        clipboardPanelRecentView = new RecyclerView(getContext());
        clipboardPanelPinsView = new RecyclerView(getContext());
        clipboardPanelRecentView.setLayoutManager(recentLayoutManager);
        clipboardPanelPinsView.setLayoutManager(pinsLayoutManager);
        clipboardPanelRecentView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        clipboardPanelPinsView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        recyclerViewGroup.addView(clipboardPanelRecentView, new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        recyclerViewGroup.addView(clipboardPanelPinsView, new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        HSLog.d(ClipboardMainView.class.getSimpleName(), "clipboard mainView created  clipboardRecentData = " + clipboardPresenter.getclipRecentData() + "    clipboardPinsData  = " + clipboardPresenter.getClipPinsData().toString());
        clipboardRecentViewAdapter = new ClipboardRecentViewAdapter(clipboardPresenter.getclipRecentData(), this);
        clipboardPinsViewAdapter = new ClipboardPinsViewAdapter(clipboardPresenter.getClipPinsData(), this);
        clipboardPanelRecentView.setAdapter(clipboardRecentViewAdapter);
        clipboardPanelPinsView.setAdapter(clipboardPinsViewAdapter);
        clipboardPanelRecentView.setTag(PANEL_RECENT);
        clipboardPanelPinsView.setTag(PANEL_PIN);
        clipboardPanelRecentView.setVisibility(VISIBLE);
        currentView = clipboardPanelRecentView;
        clipboardPanelPinsView.setVisibility(INVISIBLE);

    }


    private void addBarView() {
        actionBar = (ClipboardActionBar) View.inflate(HSApplication.getContext(), R.layout.clipboard_action_bar, null);
        actionBar.setClipboardTabChangeListener(this);
        LinearLayout.LayoutParams actionBarLayoutParams = (LinearLayout.LayoutParams) actionBar.getLayoutParams();
        if (actionBarLayoutParams == null) {
            actionBarLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        }
        addView(actionBar, actionBarLayoutParams);
    }

    private void addRecyclerViewGroup() {
        recyclerViewGroup = new FrameLayout(this.getContext());
        LinearLayout.LayoutParams panelViewGroupLayoutParams = (LinearLayout.LayoutParams) recyclerViewGroup.getLayoutParams();
        if (panelViewGroupLayoutParams == null) {
            panelViewGroupLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, keyboardHeight - getResources().getDimensionPixelSize(R.dimen.emoticon_panel_actionbar_height));

        }
        //防止点击事件穿透
        recyclerViewGroup.setClickable(true);
        addView(recyclerViewGroup, panelViewGroupLayoutParams);
    }
    //点击操作接口

    //点击recent的条目的pins按钮时执行的回调
    @Override
    public void saveToPins(String item, int position) {
        selectedRecentItemPosition = position;
        selectedRecentItem = item;
        clipboardPresenter.clipDataOperateSaveToPins(item);
        KCAnalytics.logEvent("keyboard_clipboard_pin_clicked");

    }

    //点击pins的条目的删除按钮时的回调
    @Override
    public void deletePinsItem(String pinsContentItem, int position) {
        selectRecentPinItemPosition = position;
        selectedPinsItem = pinsContentItem;
        showDeletedSuggestionAlert();
        KCAnalytics.logEvent("keyboard_clipboard_OneDeleted");
    }


    //UI更新接口
    @Override
    public void onNewRecentAdd(ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage) {
        switchToRecentView();
        clipboardRecentViewAdapter.insertDataChangeAndRefresh(clipboardRecentMessage);
        clipboardPanelRecentView.scrollToPosition(0);
    }

    @Override
    public void onDeleteTheLastRecentAndNewRecentAdd(ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage) {
        switchToRecentView();
        clipboardRecentViewAdapter.deleteDataChangeAndRefresh(RECENT_TABLE_SIZE - 1);
        clipboardRecentViewAdapter.insertDataChangeAndRefresh(clipboardRecentMessage);
        clipboardPanelRecentView.scrollToPosition(0);
    }


    @Override
    public void onExistRecentAdd(ClipboardRecentViewAdapter.ClipboardRecentMessage lastClipboardRecentMessage) {
        switchToRecentView();
        int lastPosition = clipboardRecentViewAdapter.getClipRecentData().indexOf(lastClipboardRecentMessage);
        clipboardRecentViewAdapter.setRecentItemToTopAndRefresh(lastClipboardRecentMessage, lastPosition);
        clipboardPanelRecentView.scrollToPosition(0);
    }

    @Override
    public void onDeleteRecentAndAddPin() {
        switchToRecentView();
        clipboardRecentViewAdapter.deleteDataChangeAndRefresh(selectedRecentItemPosition);
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                switchToPinsView();
                clipboardPinsViewAdapter.insertDataChangeAndRefresh(selectedRecentItem);
                clipboardPanelPinsView.scrollToPosition(0);
            }
        }, SHOW_VIEW_DURATION);
    }

    @Override
    public void onDeleteRecentAndMovePinToTop(String lastPinsItem) {
        switchToRecentView();
        clipboardRecentViewAdapter.deleteDataChangeAndRefresh(selectedRecentItemPosition);
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                switchToPinsView();
                int pinsLastPosition = clipboardPinsViewAdapter.getPinsDataList().indexOf(lastPinsItem);
                HSLog.d(TAG, "clipboardMainView pinsLastPosition-----" + pinsLastPosition + "----selectedRecentItem---" + selectedRecentItem);
                clipboardPinsViewAdapter.setPinsItemToTopAndRefresh(lastPinsItem, pinsLastPosition);
                clipboardPanelPinsView.scrollToPosition(0);
            }
        }, SHOW_VIEW_DURATION);
    }

    @Override
    public void onDeletePin() {
        switchToPinsView();
        clipboardPinsViewAdapter.deleteDataChangeAndRefresh(selectRecentPinItemPosition);
    }

    @Override
    public void onDeletePinAndUnpinRecent(ClipboardRecentViewAdapter.ClipboardRecentMessage recentItem) {
        switchToPinsView();
        clipboardPinsViewAdapter.deleteDataChangeAndRefresh(selectRecentPinItemPosition);
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                switchToRecentView();
                int recentPosition = clipboardRecentViewAdapter.getClipRecentData().indexOf(new ClipboardRecentViewAdapter.ClipboardRecentMessage(recentItem.recentClipItemContent,1));
                HSLog.d(TAG,"onDeletePinAndUnpinRecent  ====  "+"recentItem====="+recentItem+"---------recentPosition===="+recentPosition);
                clipboardRecentViewAdapter.notifyItemChangedAndRefresh(recentItem,recentPosition);
            }
        }, SHOW_VIEW_DURATION);
    }

    @Override
    public void onNewRecentAddFail(ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage) {
        Toast.makeText(HSApplication.getContext(), R.string.clipboard_operate_fail, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onExistRecentAddFail(ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage) {
        Toast.makeText(HSApplication.getContext(), R.string.clipboard_operate_fail, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onDeletePinFail() {
        Toast.makeText(HSApplication.getContext(), R.string.clipboard_operate_fail, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeletePinAndUnpinRecentFail(ClipboardRecentViewAdapter.ClipboardRecentMessage recentItem) {
        Toast.makeText(HSApplication.getContext(), R.string.clipboard_operate_fail, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteRecentAndMovePinToTopFail(String lastPinsItem) {
        Toast.makeText(HSApplication.getContext(), R.string.clipboard_operate_fail, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteRecentAndAddPinFail() {
        Toast.makeText(HSApplication.getContext(), R.string.clipboard_operate_fail, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteTheLastRecentAndNewRecentAddFail(ClipboardRecentViewAdapter.ClipboardRecentMessage clipboardRecentMessage) {
        Toast.makeText(HSApplication.getContext(), R.string.clipboard_operate_fail, Toast.LENGTH_SHORT).show();
    }
}
