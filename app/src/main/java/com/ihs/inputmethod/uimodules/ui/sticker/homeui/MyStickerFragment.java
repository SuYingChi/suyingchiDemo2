package com.ihs.inputmethod.uimodules.ui.sticker.homeui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.commons.config.HSConfig;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by guonan.lv on 17/8/14.
 */

public class MyStickerFragment extends Fragment {
    private RecyclerView recyclerView;
    private StickerCardAdapter stickerCardAdapter;
    private List<StickerModel> stickerModelList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mysticker, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        initView();
        return view;
    }

    private void initView() {
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);
        loadStickerModel();
        stickerCardAdapter = new StickerCardAdapter(stickerModelList, new StickerCardAdapter.OnStickerCardClickListener() {
            @Override
            public void onCardViewClick(StickerModel stickerModel) {

            }

            @Override
            public void onDownloadButtonClick(StickerModel stickerModel) {

            }
        });
        recyclerView.setAdapter(stickerCardAdapter);

    }

    private void loadStickerModel() {
        List<Map<String, Object>> stickerConfigList = (List<Map<String, Object>>) HSConfig.getList("Application", "StickerGroupList");
        for (Map<String, Object> map : stickerConfigList) {
            String stickerGroupName = (String) map.get("name");
            String stickerGroupDownloadDisplayName = (String) map.get("showName");
            StickerGroup stickerGroup = new StickerGroup(stickerGroupName);
            stickerGroup.setDownloadDisplayName(stickerGroupDownloadDisplayName);
            StickerModel stickerModel = new StickerModel(stickerGroup);
            stickerModel.setIsDownload(true);
            stickerModelList.add(stickerModel);
        }
    }

    @Override
    public void onResume() {
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
