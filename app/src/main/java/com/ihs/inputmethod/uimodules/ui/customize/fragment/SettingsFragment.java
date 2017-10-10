package com.ihs.inputmethod.uimodules.ui.customize.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.settings.activities.SettingsActivity;

/**
 * Created by guonan.lv on 17/9/14.
 */

public class SettingsFragment extends Fragment {

    private static final String GENERAL_HOME_PREF = "GENERAL_HOME_PREF";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_tab, container, false);
        initSettings();
        return view;
    }

    private void initSettings() {
        FragmentManager fragmentManager = getActivity().getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        SettingsActivity.GeneralHomePreferenceFragment generalHomePreferenceFragment = (SettingsActivity.GeneralHomePreferenceFragment) fragmentManager.findFragmentByTag(GENERAL_HOME_PREF);
        if (generalHomePreferenceFragment == null) {
            generalHomePreferenceFragment = new SettingsActivity.GeneralHomePreferenceFragment();
            fragmentTransaction.add(R.id.settings_content, generalHomePreferenceFragment, GENERAL_HOME_PREF);
        }
        fragmentTransaction.show(generalHomePreferenceFragment).commit();
    }
}
