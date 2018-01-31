package com.ihs.inputmethod.mydownload.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.inputmethod.api.framework.HSInputMethodListManager;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacter;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacterManager;
import com.ihs.inputmethod.fonts.adapter.FontAdapter;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.fonts.homeui.FontModel;
import com.ihs.inputmethod.uimodules.widget.TrialKeyboardDialog;
import com.keyboard.common.KeyboardActivationGuideActivity;

import java.util.ArrayList;
import java.util.List;

public class MyFontFragment extends Fragment implements FontAdapter.OnFontCardClickListener {
    private RecyclerView recyclerView;
    private FontAdapter fontAdapter;
    private List<FontModel> fontModelList = new ArrayList<>();
    private TrialKeyboardDialog trialKeyboardDialog;
    private static final int REQUEST_CODE_ACTIVATION = 1;
    private int fontIndexWhenActivating;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_myfont, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        initView();
        return view;
    }

    private void initView() {
        loadFontModel();
        fontAdapter = new FontAdapter(getActivity(), this);
        fontAdapter.setDataList(fontModelList);
        fontAdapter.setFrom(FontAdapter.FROM_MY_DOWNLOAD);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(fontAdapter);
    }

    private void loadFontModel() {
        List<HSSpecialCharacter> hsSpecialCharacterList = HSSpecialCharacterManager.getSpecialCharacterList();
        for (HSSpecialCharacter hsSpecialCharacter : hsSpecialCharacterList) {
            FontModel fontModel = new FontModel(hsSpecialCharacter);
            fontModel.setNeedDownload(false);
            fontModelList.add(fontModel);
        }
    }


    @Override
    public void onFontCardClick(FontModel fontModel, int position) {
        showTrialKeyboardDialog(position);
    }

    private void showTrialKeyboardDialog(int fontIndex) {
        fontIndexWhenActivating = fontIndex;
        if (HSInputMethodListManager.isMyInputMethodSelected()) {
            HSSpecialCharacterManager.selectSpecialCharacter(fontIndex);
            if (trialKeyboardDialog == null) {
                trialKeyboardDialog = new TrialKeyboardDialog.Builder(getActivity()).create();
            }
            trialKeyboardDialog.show(true);
        } else {
            Intent intent = new Intent(getActivity(), KeyboardActivationGuideActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ACTIVATION);

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ACTIVATION) {
            if (resultCode == Activity.RESULT_OK) {
                showTrialKeyboardDialog(fontIndexWhenActivating);
            }
        }
    }

}
