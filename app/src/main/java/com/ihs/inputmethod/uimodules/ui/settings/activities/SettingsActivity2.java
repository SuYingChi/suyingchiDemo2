/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ihs.inputmethod.uimodules.ui.settings.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.chargingscreen.utils.ChargingPrefsUtil;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.charging.ChargingConfigManager;
import com.ihs.inputmethod.language.api.HSImeSubtypeManager;
import com.ihs.inputmethod.uimodules.R;

import java.util.List;

public final class SettingsActivity2 extends HSAppCompatPreferenceActivity {
    private static final String GA_PARAM_ACTION_APP_SETTING_CHARGING_FIRSTTIME_CLICKED = "app_setting_charging_firsttime_clicked";

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        getFragmentManager().beginTransaction().replace(android.R.id.content, new GeneralPreferenceFragment()).commit();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.settings));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        private boolean isSettingChargingClicked;//是否点击过charging的设置

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_settings);
            setHasOptionsMenu(true);
            setEnabledLanguage();
            setCharging();
        }

        private void setCharging() {
            SwitchPreference chargingPreference = (SwitchPreference) findPreference(getResources().getString(R.string.config_charge_switchpreference_key));
            chargingPreference.setChecked(ChargingPrefsUtil.getInstance().isChargingEnabled());
//            if (!showChargeSetting || !ChargingConfigManager.getManager().enableChargingFunction()) {
//                getPreferenceScreen().removePreference(chargingPreference);
//            } else {
            chargingPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean isSwitchOn = (boolean) newValue;
                    if (!isSettingChargingClicked) {
                        isSettingChargingClicked = true;
                        ChargingConfigManager.getManager().setUserChangeChargingToggle();
                        String switchOn = isSwitchOn ? "true" : "false";
                        HSGoogleAnalyticsUtils.getInstance().logAppEvent(GA_PARAM_ACTION_APP_SETTING_CHARGING_FIRSTTIME_CLICKED, switchOn);
                        HSAnalytics.logEvent(GA_PARAM_ACTION_APP_SETTING_CHARGING_FIRSTTIME_CLICKED, "switchOn", switchOn);
                    }

                    if (isSwitchOn) {
                        ChargingManagerUtil.enableCharging(false);
                    } else {
                        ChargingManagerUtil.disableCharging();
                    }
                    return true;
                }
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                getActivity().finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private void setEnabledLanguage() {
            StringBuilder languageSb = new StringBuilder();
            final InputMethodInfo imi = HSInputMethod.getInputMethodInfoOfThisIme();
            List<InputMethodSubtype> enabledList = HSImeSubtypeManager.getInputMethodSubtypeList(true);
            CharSequence subtypeLabel;
            for (int i = 0; i < enabledList.size(); i++) {
                subtypeLabel = enabledList.get(i).getDisplayName(HSApplication.getContext(), imi.getPackageName(), imi.getServiceInfo().applicationInfo);
                if (i < enabledList.size() - 1) {
                    languageSb.append(subtypeLabel).append(", ");
                } else {
                    languageSb.append(subtypeLabel);
                    if (i > 0) {
                        languageSb.append(".");
                    }
                }
            }
            Preference languagePreference = findPreference("choose_language");
            languagePreference.setSummary(languageSb.toString());
            languagePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), MoreLanguageActivity2.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                }
            });
        }
    }
}
