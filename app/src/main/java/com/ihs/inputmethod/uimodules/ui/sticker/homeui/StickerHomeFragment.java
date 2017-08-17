package com.ihs.inputmethod.uimodules.ui.sticker.homeui;

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
import com.ihs.commons.config.HSConfig;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerDataManager;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerDownloadManager;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerGroup;
import com.ihs.keyboardutils.adbuffer.AdLoadingView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by guonan.lv on 17/8/10.
 */

public class StickerHomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private StickerCardAdapter stickerCardAdapter;
    private List<StickerModel> stickerModelList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sticker, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        initView();
        return view;
    }

    private void initView() {
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);
        loadStickerGroup();
        stickerCardAdapter = new StickerCardAdapter(stickerModelList, new StickerCardAdapter.OnStickerCardClickListener() {
            @Override
            public void onCardViewClick(StickerModel stickerModel) {
                HSAnalytics.logEvent(stickerModel.getStickerGroup().getStickerGroupName(), "sticker_download_clicked");
                onDownloadButtonClick(stickerModel);
            }

            @Override
            public void onDownloadButtonClick(final StickerModel stickerModel) {

                StickerDownloadManager.getInstance().startForegroundDownloading(HSApplication.getContext(), stickerModel.getStickerGroup(), null, new AdLoadingView.OnAdBufferingListener() {
                    @Override
                    public void onDismiss(boolean success) {
                        if(success) {
                            showTrialStickerKeyboard(stickerModel.getStickerGroup());
                            int position = stickerModelList.indexOf(stickerModel);
                            stickerModelList.remove(position);
                            removeStickerFromView(position);
                        }
                    }
                });
            }
        });
        recyclerView.setAdapter(stickerCardAdapter);

    }

    private void showTrialStickerKeyboard(StickerGroup stickerGroup) {

    }

    private void loadStickerGroup() {
        List<Map<String, Object>> stickerConfigList = (List<Map<String, Object>>) HSConfig.getList("Application", "StickerGroupList");
        for (Map<String, Object> map : stickerConfigList) {
            String stickerGroupName = (String) map.get("name");
            StickerGroup stickerGroup = new StickerGroup(stickerGroupName);
            if(StickerDataManager.getInstance().isStickerGroupDownloaded(stickerGroupName)) {
                continue;
            }
            String stickerTag = (String) map.get("tagName");
            String stickerGroupDownloadDisplayName = (String) map.get("showName");
            stickerGroup.setDownloadDisplayName(stickerGroupDownloadDisplayName);
            StickerModel stickerModel = new StickerModel(stickerGroup);
            if(stickerTag != null) {
                stickerModel.setStickTag(stickerTag);
            }
            stickerModelList.add(stickerModel);
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
    }

}
