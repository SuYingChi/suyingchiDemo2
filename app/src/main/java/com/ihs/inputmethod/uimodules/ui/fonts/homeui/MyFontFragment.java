package com.ihs.inputmethod.uimodules.ui.fonts.homeui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacter;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacterManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.constants.KeyboardActivationProcessor;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeDownloadActivity;
import com.ihs.inputmethod.uimodules.widget.TrialKeyboardDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guonan.lv on 17/8/14.
 */

public class MyFontFragment extends Fragment implements FontCardAdapter.OnFontCardClickListener {
    private RecyclerView recyclerView;
    private FontCardAdapter fontCardAdapter;
    private List<FontModel> fontModelList = new ArrayList<>();
    private TrialKeyboardDialog trialKeyboardDialog;
    public static final String tabTitle = HSApplication.getContext().getString(R.string.tab_font_my);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_myfont, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        initView();
        return view;
    }

    private void initView() {
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        loadFontModel();
        fontCardAdapter = new FontCardAdapter(fontModelList, this);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {

            @Override
            public int getSpanSize(int position) {
                if (fontCardAdapter.getItemViewType(position) == FontCardAdapter.MORE_FONT_COMING_TYPE) {
                    return 2;
                }
                return 1;
            }
        });
        recyclerView.setAdapter(fontCardAdapter);
        recyclerView.setLayoutManager(layoutManager);
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

    @Override
    public void onFontCardClick(int position) {
        showTrialKeyboardDialog(ThemeDownloadActivity.keyboardActivationFromDownload, fontModelList.get(position));
    }

    private void showTrialKeyboardDialog(final int activationCode, final FontModel fontModel) {
        final int position = fontModelList.indexOf(fontModel);
        final KeyboardActivationProcessor processor =
                new KeyboardActivationProcessor(getActivity().getClass(), new KeyboardActivationProcessor.OnKeyboardActivationChangedListener() {
                    @Override
                    public void activeDialogShowing() {

                    }

                    @Override
                    public void keyboardSelected(int requestCode) {
                        if (requestCode == activationCode) {
                            if (trialKeyboardDialog == null) {
                                trialKeyboardDialog = new TrialKeyboardDialog.Builder(ThemeDownloadActivity.class.getName()).create(getActivity(), (TrialKeyboardDialog.OnTrialKeyboardStateChanged) getActivity());
                            }
                            HSSpecialCharacterManager.selectSpecialCharacter(position);
                            trialKeyboardDialog.show(getActivity(), activationCode, true);
                        }
                    }

                    @Override
                    public void activeDialogCanceled() {

                    }

                    @Override
                    public void activeDialogDismissed() {

                    }
                });
        processor.activateKeyboard(getActivity(), true, activationCode);
    }
}
