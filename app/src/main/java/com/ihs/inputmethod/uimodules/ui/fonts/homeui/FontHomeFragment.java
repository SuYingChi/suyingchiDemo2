package com.ihs.inputmethod.uimodules.ui.fonts.homeui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacter;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacterManager;
import com.ihs.inputmethod.uimodules.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guonan.lv on 17/8/14.
 */

public class FontHomeFragment extends Fragment {
    
    private RecyclerView recyclerView;
    private FontCardAdapter fontCardAdapter;
    private List<FontModel> fontModelList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_font, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        initView();
        return view;
    }

    private void initView() {
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);
        loadFontModel();
        fontCardAdapter = new FontCardAdapter(fontModelList, new FontCardAdapter.OnFontCardClickListener() {

            @Override
            public void onFontCardClick() {
                Toast.makeText(getContext(), "fontCardClicked", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(fontCardAdapter);
    }

    private void loadFontModel() {
        List<HSSpecialCharacter> hsSpecialCharacterList = HSSpecialCharacterManager.getSpecialCharacterList();
        for(HSSpecialCharacter hsSpecialCharacter : hsSpecialCharacterList) {
            fontModelList.add(new FontModel(hsSpecialCharacter));
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
