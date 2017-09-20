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
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.Browser;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.ListView;
import android.widget.Toast;

import com.acb.call.CPSettings;
import com.acb.nativeads.AcbNativeAdManager;
import com.artw.lockscreen.LockerSettings;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.ChargingAnalytics;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.chargingscreen.utils.ChargingPrefsUtil;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.HSUIInputMethod;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.charging.ChargingConfigManager;
import com.ihs.inputmethod.feature.apkupdate.ApkUtils;
import com.ihs.inputmethod.language.api.HSImeSubtypeManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.iap.RemoveAdsManager;
import com.ihs.keyboardutils.utils.KCAnalyticUtil;

import java.util.List;

import static com.ihs.keyboardutils.iap.RemoveAdsManager.NOTIFICATION_REMOVEADS_PURCHASED;

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

    public void setupActionBar(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(title);
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
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || GeneralMorePreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_settings);
            setHasOptionsMenu(true);
            setMore();
            setEnabledLanguage();
        }

        private void setMore() {
            Preference preference = findPreference(getResources().getString(R.string.setting_key_more));
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(android.R.id.content, new GeneralMorePreferenceFragment());
                    fragmentTransaction.addToBackStack(null).commit();
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

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralMorePreferenceFragment extends PreferenceFragment {
        private boolean isSettingChargingClicked;//是否点击过charging的设置

        public GeneralMorePreferenceFragment() {
            super();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_settings_more);
            setHasOptionsMenu(true);
            setCharging();
            setLocker();
            setBoost();
            setCallAssistant();
            setupActionBar(getString(R.string.setting_item_more_settings));
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            setupActionBar(getString(R.string.settings));
        }

        private void setupActionBar(String title) {
            ActionBar actionBar = ((HSAppCompatPreferenceActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                // Show the Up button in the action bar.
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle(title);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    getActivity().onBackPressed();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }

        private void setCallAssistant() {
            SwitchPreference preference = (SwitchPreference) findPreference(getResources().getString(R.string.setting_key_call_assistant));
            boolean screenFlashSetting = CPSettings.isScreenFlashModuleEnabled();
            preference.setChecked(screenFlashSetting);
            CPSettings.setScreenFlashModuleEnabled(screenFlashSetting);
            preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean isSwitchOn = (boolean) newValue;
                    CPSettings.setScreenFlashModuleEnabled(isSwitchOn);
                    return true;
                }
            });
        }

        private void setBoost() {
            SwitchPreference boostPreference = (SwitchPreference) findPreference(getResources().getString(R.string.boost_notification_key));
            if (Build.VERSION.SDK_INT < 16) {
                getPreferenceScreen().removePreference(boostPreference);
            } else {
                boostPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        boolean isSwitchOn = (boolean) newValue;
                        if (isSwitchOn) {
                            if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
                                AcbNativeAdManager.sharedInstance().activePlacementInProcess(getString(R.string.ad_placement_result_page));
                            }
                            KCAnalyticUtil.logEvent("phoneboost_enabled");
                        } else {
                            AcbNativeAdManager.sharedInstance().deactivePlacementInProcess(getString(R.string.ad_placement_result_page));
                            KCAnalyticUtil.logEvent("phoneboost_disabled");
                        }
                        return true;
                    }
                });
            }
        }

        private void setLocker() {
            String locker = getString(R.string.locker_switcher);
            if (LockerSettings.getLockerEnableStates() == LockerSettings.LOCKER_MUTED) {
                getPreferenceScreen().removePreference(findPreference(locker));
            } else {
                SwitchPreference lockerSwitcher = (SwitchPreference) findPreference(locker);
                int lockerEnableStates = LockerSettings.getLockerEnableStates();
                switch (lockerEnableStates) {
                    case LockerSettings.LOCKER_DEFAULT_DISABLED:
                        lockerSwitcher.setChecked(false);
                        break;
                    case LockerSettings.LOCKER_DEFAULT_ACTIVE:
                        lockerSwitcher.setChecked(true);
                        break;
                }

                lockerSwitcher.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        LockerSettings.setLockerEnabled((Boolean) newValue);
                        if (!((Boolean) newValue)) {
                            LockerSettings.recordLockerDisableOnce();
                        }
                        return true;
                    }
                });
            }
        }

        private void setCharging() {
            SwitchPreference chargingPreference = (SwitchPreference) findPreference(getResources().getString(R.string.config_charge_switchpreference_key));

            int chargingEnableStates = ChargingPrefsUtil.getInstance().getChargingEnableStates();
            if (chargingEnableStates == ChargingPrefsUtil.CHARGING_MUTED) {
                getPreferenceScreen().removePreference(chargingPreference);
                return;
            }

            boolean chargingEnabled = chargingEnableStates == ChargingPrefsUtil.CHARGING_DEFAULT_ACTIVE;

            chargingPreference.setChecked(chargingEnabled);
//            if (!showChargeSetting || !ChargingConfigManager.getManager().enableChargingFunction()) {
//                getPreferenceScreen().removePreference(chargingPreference);
            chargingPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean isSwitchOn = (boolean) newValue;
                    if (!isSettingChargingClicked) {
                        isSettingChargingClicked = true;
                        ChargingConfigManager.getManager().setUserChangeChargingToggle();
                        String switchOn = isSwitchOn ? "true" : "false";
                        HSAnalytics.logEvent(GA_PARAM_ACTION_APP_SETTING_CHARGING_FIRSTTIME_CLICKED, "switchOn", switchOn);
                    }

                    if (isSwitchOn) {
                        ChargingManagerUtil.enableCharging(false);
                    } else {
                        ChargingManagerUtil.disableCharging();
                        ChargingAnalytics.getInstance().recordChargingDisableOnce();
                    }
                    return true;
                }
            });
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralHomePreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

        private OnUpdateClickListener onUpdateClickListener;
        private Preference updatePreference;
        private PreferenceCategory preferenceCategoryMore;

        private INotificationObserver observer = new INotificationObserver() {
            @Override
            public void onReceive(String s, HSBundle hsBundle) {
                if (NOTIFICATION_REMOVEADS_PURCHASED.equals(s)) {
                    Toast.makeText(HSApplication.getContext(), HSApplication.getContext().getString(R.string.purchase_success), Toast.LENGTH_LONG).show();
                    if (preferenceCategoryMore != null) {
                        preferenceCategoryMore.removePreference(findPreference("removeAd"));
                    }
                }
            }
        };

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            //set divider color
            View rootView = getView();
            ListView list = (ListView) rootView.findViewById(android.R.id.list);
            list.setDivider(new ColorDrawable(0xef000000));
            list.setDividerHeight(1);
        }


        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            if (context instanceof OnUpdateClickListener) {
                onUpdateClickListener = (OnUpdateClickListener) context;
            }
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_settings_home);
            setLanguage();
            setup();
            HSGlobalNotificationCenter.addObserver(NOTIFICATION_REMOVEADS_PURCHASED, observer);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            HSGlobalNotificationCenter.removeObserver(observer);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            return super.onOptionsItemSelected(item);
        }

        private void setLanguage() {
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
            findPreference("choose_language").setOnPreferenceClickListener(this);
        }

        private void setup() {
            preferenceCategoryMore = (PreferenceCategory) findPreference("more");
            updatePreference = findPreference("update");
            if (!ApkUtils.isUpdateEnabled()) {
                preferenceCategoryMore.removePreference(updatePreference);
            } else {
                preferenceCategoryMore.addPreference(updatePreference);
            }
            findPreference("keyboard_settings").setOnPreferenceClickListener(this);
            findPreference("removeAd").setOnPreferenceClickListener(this);
            findPreference("privacy_policy").setOnPreferenceClickListener(this);
            updatePreference.setOnPreferenceClickListener(this);
        }

        private void setPrivacy() {
            Uri uri = Uri.parse(HSConfig.optString("", "Application", "Policy", "PrivacyPolicy"));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.putExtra(Browser.EXTRA_APPLICATION_ID, getActivity().getPackageName());
            try {
                getActivity().startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.w("URLSpan", "Activity was not found for intent, " + intent.toString());
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (preference == findPreference("choose_language")) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), MoreLanguageActivity2.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            } else if (preference == findPreference("keyboard_settings")) {
                HSUIInputMethod.launchSettingsActivity();
                HSAnalytics.logEvent("sidebar_settings_clicked");
                return true;
            } else if (preference == findPreference("removeAd")) {
                HSAnalytics.logEvent("sidebar_removeAds_clicked");
                RemoveAdsManager.getInstance().purchaseRemoveAds();
                return true;
            } else if (preference == findPreference("privacy_policy")) {
                setPrivacy();
                return true;
            } else if (preference == updatePreference) {
                if (onUpdateClickListener != null) {
                    onUpdateClickListener.updateClick();
                }
            }
            return false;
        }

        public interface OnUpdateClickListener {
            void updateClick();
        }
    }
}
