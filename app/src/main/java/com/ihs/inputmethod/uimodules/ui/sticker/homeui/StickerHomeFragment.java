package com.ihs.inputmethod.uimodules.ui.sticker.homeui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerDataManager;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerDownloadManager;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerGroup;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerUtils;
import com.ihs.inputmethod.utils.DownloadUtils;
import com.ihs.keyboardutils.adbuffer.AdLoadingView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.ihs.inputmethod.uimodules.ui.sticker.StickerDataManager.STICKER_GROUP_DOWNLOAD_SUCCESS_NOTIFICATION;
import static com.ihs.inputmethod.uimodules.ui.sticker.StickerDataManager.STICKER_GROUP_ORIGINAL;
import static com.ihs.inputmethod.uimodules.ui.sticker.StickerUtils.STICKER_DOWNLOAD_ZIP_SUFFIX;

/**
 * Created by guonan.lv on 17/8/10.
 */

public class StickerHomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private StickerCardAdapter stickerCardAdapter;
    private List<StickerModel> stickerModelList = new ArrayList<>();
    public static final String tabTitle = HSApplication.getContext().getString(R.string.tab_sticker);

    private INotificationObserver observer = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (STICKER_GROUP_DOWNLOAD_SUCCESS_NOTIFICATION.equals(s)) {
                StickerGroup stickerGroup = (StickerGroup) hsBundle.getObject(STICKER_GROUP_ORIGINAL);
                StickerModel stickerModel = new StickerModel(stickerGroup);
                int position = stickerModelList.indexOf(stickerModel);
                stickerModelList.remove(position);
                removeStickerFromView(position);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sticker, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        initView();
        HSGlobalNotificationCenter.addObserver(STICKER_GROUP_DOWNLOAD_SUCCESS_NOTIFICATION, observer);
        return view;
    }

    private void initView() {
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);

        loadStickerGroup();
        stickerCardAdapter = new StickerCardAdapter(stickerModelList, new StickerCardAdapter.OnStickerCardClickListener() {
            @Override
            public void onCardViewClick(StickerModel stickerModel, Drawable drawable) {
                HSAnalytics.logEvent(stickerModel.getStickerGroup().getStickerGroupName(), "sticker_download_clicked");
                onDownloadButtonClick(stickerModel, drawable);
            }

            @Override
            public void onDownloadButtonClick(final StickerModel stickerModel, Drawable drawable) {
                final StickerGroup stickerGroup = stickerModel.getStickerGroup();
                final String stickerGroupName = stickerModel.getStickerGroup().getStickerGroupName();
                final String stickerGroupDownloadedFilePath = StickerUtils.getStickerFolderPath(stickerGroupName) + STICKER_DOWNLOAD_ZIP_SUFFIX;

                // 移除点击过的new角标
                StickerDataManager.getInstance().removeNewTipOfStickerGroup(stickerModel);
                stickerCardAdapter.notifyItemChanged(stickerModelList.indexOf(stickerModel));


                DownloadUtils.getInstance().startForegroundDownloading(HSApplication.getContext(), stickerGroupName,
                        stickerGroupDownloadedFilePath, stickerGroup.getStickerGroupDownloadUri(),
                        drawable, new AdLoadingView.OnAdBufferingListener() {
                            @Override
                            public void onDismiss(boolean success) {
                                if (success) {
                                    HSAnalytics.logEvent("sticker_download_succeed", stickerGroupName);
                                    StickerDownloadManager.getInstance().unzipStickerGroup(stickerGroupDownloadedFilePath, stickerGroup);
                                }
                            }

                        });
            }

        });
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {

            @Override
            public int getSpanSize(int position) {
                if (stickerCardAdapter.getItemViewType(position) == StickerCardAdapter.ITEM_TYPE.ITEM_TYPE_MORE.ordinal()) {
                    return 2;
                }
                return 1;
            }
        });
        stickerCardAdapter.setFragmentType(StickerHomeFragment.class.getSimpleName());
        recyclerView.setAdapter(stickerCardAdapter);
        recyclerView.setLayoutManager(layoutManager);

    }

    private void loadStickerGroup() {
        List<StickerGroup> stickerGroupList = StickerDataManager.getInstance().getStickerGroupList();

        for (StickerGroup stickerGroup : stickerGroupList) {
            if (!stickerGroup.isStickerGroupDownloaded()) {
                stickerModelList.add(new StickerModel(stickerGroup));
            }
        }
    }

    private void reloadStickerGroup() {
        Iterator<StickerModel> iterator = stickerModelList.iterator();
        while (iterator.hasNext()) {
            StickerModel stickerModel = iterator.next();
            if (StickerDataManager.getInstance().isStickerGroupDownloaded(stickerModel.getStickerGroup().getStickerGroupName())) {
                int position = stickerModelList.indexOf(stickerModel);
                iterator.remove();
                removeStickerFromView(position);
            }
        }
    }

    private void removeStickerFromView(int position) {
        stickerCardAdapter.notifyItemRemoved(position);
        stickerCardAdapter.notifyItemRangeChanged(position, stickerModelList.size());
    }

    @Override
    public void onResume() {
        reloadStickerGroup();
        super.onResume();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        HSGlobalNotificationCenter.removeObserver(STICKER_GROUP_DOWNLOAD_SUCCESS_NOTIFICATION, observer);
    }

}
