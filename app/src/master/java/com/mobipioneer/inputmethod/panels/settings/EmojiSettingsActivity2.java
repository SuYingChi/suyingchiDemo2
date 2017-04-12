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

package com.mobipioneer.inputmethod.panels.settings;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.chargingscreen.utils.ChargingPrefsUtil;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.charging.ChargingConfigManager;
import com.ihs.inputmethod.framework.RichInputMethodManager;
import com.ihs.inputmethod.language.api.HSImeSubtypeManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.settings.activities.MoreLanguageActivity2;
import com.mobipioneer.lockerkeyboard.utils.Constants;

import java.util.List;

public final class EmojiSettingsActivity2 extends AppCompatPreferenceActivity {
    private int adItem;
    private final static String GENERAL_PREFERENCE_FRAGMENT_TAG = "general_preference_fragment_tag";
    private final static String FUNCTION_PREFERENCE_FRAGMENT_TAG = "function_preference_fragment_tag";

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
        adItem = getIntent().getIntExtra("ad_item", -1);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (adItem != -1) {
            Bundle bundle = new Bundle();
            bundle.putInt("ad_item", adItem);
            FunctionPreferenceFragment functionPreferenceFragment = new FunctionPreferenceFragment();
            functionPreferenceFragment.setArguments(bundle);
            transaction.add(android.R.id.content, functionPreferenceFragment, FUNCTION_PREFERENCE_FRAGMENT_TAG).commit();
        } else {
            String jumpTo = getIntent().getStringExtra("jumpTo");
            if (!TextUtils.isEmpty(jumpTo) && jumpTo.equals(getString(R.string.key_uninstall))) {
                UninstallPreferenceFragment uninstallPreferenceFragment = new UninstallPreferenceFragment();
                transaction.add(android.R.id.content, uninstallPreferenceFragment, FUNCTION_PREFERENCE_FRAGMENT_TAG).commit();
            } else {
                GeneralPreferenceFragment generalPreferenceFragment = new GeneralPreferenceFragment();
                transaction.add(android.R.id.content, generalPreferenceFragment, GENERAL_PREFERENCE_FRAGMENT_TAG).commit();
            }
        }
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Setting");
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
        private RichInputMethodManager mRichImm;
        private boolean showChargeSetting;//是否显示charge的设置选项，默认不显示,locker项目需要显示
        private boolean isSettingChargingClicked;//是否点击过charging的设置

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mRichImm = RichInputMethodManager.getInstance();
            showChargeSetting = getResources().getBoolean(R.bool.config_boost_charge_setting_enable);
            addPreferencesFromResource(R.xml.emoji_setting_layout);
            setHasOptionsMenu(true);
            setEnabledLanguage();
            initPreference();
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
            final InputMethodInfo imi = mRichImm.getInputMethodInfoOfThisIme();
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

        private void initPreference() {
            findPreference(getString(R.string.custom_uninstall_preference_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showUninstallFragment();
                    return true;
                }
            });
            setCharging();
        }

        private void setCharging() {

            CheckBoxPreference chargingPreference = (CheckBoxPreference) findPreference(getResources().getString(R.string.config_charge_switchpreference_key));

            int chargingEnableStates = ChargingPrefsUtil.getInstance().getChargingEnableStates();
            if (chargingEnableStates == ChargingPrefsUtil.CHARGING_MUTED) {
                getPreferenceScreen().removePreference(chargingPreference);
                return;
            }

            boolean chargingEnabled = chargingEnableStates == ChargingPrefsUtil.CHARGING_DEFAULT_ACTIVE;
            chargingPreference.setChecked(chargingEnabled);
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
                        HSGoogleAnalyticsUtils.getInstance().logAppEvent(Constants.GA_PARAM_ACTION_APP_SETTING_CHARGING_FIRSTTIME_CLICKED, switchOn);
                        HSAnalytics.logEvent(Constants.GA_PARAM_ACTION_APP_SETTING_CHARGING_FIRSTTIME_CLICKED, "switchOn", switchOn);
                    }

                    if (isSwitchOn) {
                         ChargingManagerUtil.enableCharging(false);
                    } else {
                        ChargingManagerUtil.disableCharging();
                    }
                    return true;
                }
            });
//            }
        }

        private void showFunctionFragment(int postion) {
            Fragment generalFragment = getFragmentManager().findFragmentByTag(GENERAL_PREFERENCE_FRAGMENT_TAG);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            if (generalFragment != null) {
                transaction.hide(generalFragment);
            }
            Bundle bundle = new Bundle();
            bundle.putInt("ad_item", postion);
            FunctionPreferenceFragment functionPreferenceFragment = new FunctionPreferenceFragment();
            functionPreferenceFragment.setArguments(bundle);
            transaction.add(android.R.id.content, functionPreferenceFragment, FUNCTION_PREFERENCE_FRAGMENT_TAG).commit();
        }

        private void showUninstallFragment() {
            Fragment generalFragment = getFragmentManager().findFragmentByTag(GENERAL_PREFERENCE_FRAGMENT_TAG);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            if (generalFragment != null) {
                transaction.hide(generalFragment);
            }
            UninstallPreferenceFragment uninstallPreferenceFragment = new UninstallPreferenceFragment();
            transaction.add(android.R.id.content, uninstallPreferenceFragment, FUNCTION_PREFERENCE_FRAGMENT_TAG).commit();
        }

        @Override
        public void onResume() {
            super.onResume();
            CheckBoxPreference chargingPreference = (CheckBoxPreference) findPreference(getResources().getString(R.string.config_charge_switchpreference_key));
        }
    }


    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class FunctionPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.emoji_function_setting_layout);
            setHasOptionsMenu(true);
            Bundle arguments = getArguments();
            int ad_item = arguments.getInt("ad_item", 0);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                Fragment generalFragment = getFragmentManager().findFragmentByTag(GENERAL_PREFERENCE_FRAGMENT_TAG);
                Fragment functionFragment = getFragmentManager().findFragmentByTag(FUNCTION_PREFERENCE_FRAGMENT_TAG);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                if (generalFragment == null) {
                    getActivity().finish();
                } else {
                    if (functionFragment != null) {
                        transaction.hide(functionFragment);
                    }
                    transaction.show(generalFragment).commit();
                }
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }


    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class UninstallPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.emoji_uninstall_setting_layout);
            setHasOptionsMenu(true);

            Preference preference0 = findPreference(getResources().getString(R.string.custom_uninstall_preference_key));
            preference0.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("settings_uninstall_turn" + ((boolean) newValue ? " On" : "Off"));
                    PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).edit().putBoolean(getString(R.string.key_uninstall), (boolean) newValue).apply();
                    return true;
                }
            });

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                Fragment generalFragment = getFragmentManager().findFragmentByTag(GENERAL_PREFERENCE_FRAGMENT_TAG);
                Fragment functionFragment = getFragmentManager().findFragmentByTag(FUNCTION_PREFERENCE_FRAGMENT_TAG);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                if (generalFragment == null) {
                    getActivity().finish();
                } else {
                    if (functionFragment != null) {
                        transaction.hide(functionFragment);
                    }
                    transaction.show(generalFragment).commit();
                }
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        Fragment generalFragment = getFragmentManager().findFragmentByTag(GENERAL_PREFERENCE_FRAGMENT_TAG);
        if (generalFragment == null /** 直接从广告的设置界面进入到二级界面,则按返回键直接退出 */
                || generalFragment.isVisible() /** 已经在一级界面,则按返回键直接退出 */) {
            finish();
        } else {
            Fragment functionFragment = getFragmentManager().findFragmentByTag(FUNCTION_PREFERENCE_FRAGMENT_TAG);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            if (functionFragment != null) {
                transaction.hide(functionFragment);
            }
            transaction.show(generalFragment);
            transaction.commit();
        }
    }


}
