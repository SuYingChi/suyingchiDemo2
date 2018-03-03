package com.ihs.inputmethod.uimodules.settings;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.location.HSLocationManager;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.HSUIInputMethod;
import com.ihs.inputmethod.api.HSUIInputMethodService;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.BaseFunctionBar;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.fonts.common.HSFontSelectPanel;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity;
import com.ihs.inputmethod.uimodules.ui.theme.ui.panel.HSSelectorPanel;
import com.ihs.inputmethod.uimodules.ui.theme.ui.panel.HSThemeSelectPanel;
import com.ihs.inputmethod.uimodules.widget.ViewPagerIndicator;
import com.ihs.panelcontainer.BasePanel;
import com.ihs.panelcontainer.panel.KeyboardPanel;
import com.kc.utils.KCAnalytics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.ihs.keyboardutils.iap.RemoveAdsManager.NOTIFICATION_REMOVEADS_PURCHASED;
import static com.ihs.panelcontainer.KeyboardPanelSwitchContainer.MODE_BACK_PARENT;


public class HSNewSettingsPanel extends BasePanel {
    public final static String BUNDLE_KEY_SHOW_TIP = "bundle_key_show_tip";
    private View settingPanelView;
    int animDuration = 300;
    private Context mContext;
    private ViewItem themeItem;
    private ViewItem selectorItem;
    private List<ViewItem> items;
    private SettingsViewPager settingsViewPager;
    private static boolean isLocationInfoFetching = false;
    private static Handler reverseLocationHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case LOCATION_MESSAGE:
                    removeMessages(LOCATION_MESSAGE);
                    if (isLocationInfoFetching&&tsk!=null){
                        Toast.makeText(HSApplication.getContext(),R.string.request_location_timeout,Toast.LENGTH_SHORT).show();
                        KCAnalytics.logEvent("keyboard_location_sendFailed","reason","time out");
                        tsk.cancel(true);
                        isLocationInfoFetching = false;
                    }
                    break;
            }
        }
    };
    private static AsyncTask<Location, Void, Address> tsk;
    private final int TIMEOUT_MILLIS = 10000;
    private static final int LOCATION_MESSAGE = 1;
    public HSNewSettingsPanel() {
        mContext = HSApplication.getContext();
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public View onCreatePanelView() {
        if (settingPanelView == null) {
            View view = View.inflate(getContext(), R.layout.panel_settings, null);
            settingsViewPager = view.findViewById(R.id.settingsViewPager);
            settingsViewPager.setItems(prepareItems());

            ViewPagerIndicator dotsRadioGroup = view.findViewById(R.id.dots_indicator);
            dotsRadioGroup.setViewPager(settingsViewPager);

            view.setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
            settingPanelView = view;
        }
        HSGlobalNotificationCenter.addObserver(HSInputMethod.HS_NOTIFICATION_SHOW_INPUTMETHOD, notificationObserver);
        HSGlobalNotificationCenter.addObserver(NOTIFICATION_REMOVEADS_PURCHASED, notificationObserver);
        return settingPanelView;
    }

    private List<ViewItem> prepareItems() {
        items = new ArrayList<>();

        themeItem = ViewItemBuilder.getThemesItem(new ViewItem.ViewItemListener() {
            @Override
            public void onItemClick(ViewItem item) {
                Bundle bundle = new Bundle();
                bundle.putBoolean(BUNDLE_KEY_SHOW_TIP, item.isShowingNewMark());
                getPanelActionListener().showChildPanel(HSThemeSelectPanel.class, bundle);

                item.hideNewMark();
                ((BaseFunctionBar) panelActionListener.getBarView()).hideNewMark();
                KCAnalytics.logEvent("keyboard_setting_themes_clicked");
            }
        });
        items.add(themeItem);
        items.add(ViewItemBuilder.getMyThemeItem(new ViewItem.ViewItemListener() {
            @Override
            public void onItemClick(ViewItem item) {
                Bundle bundle = new Bundle();
                String customEntry = "keyboard";
                bundle.putString(CustomThemeActivity.BUNDLE_KEY_CUSTOMIZE_ENTRY, customEntry);
                CustomThemeActivity.startCustomThemeActivity(bundle);
            }
        }));
        items.add(ViewItemBuilder.getFontsItem(new ViewItem.ViewItemListener() {
            @Override
            public void onItemClick(ViewItem item) {
                getPanelActionListener().showChildPanel(HSFontSelectPanel.class, null);
                KCAnalytics.logEvent("keyboard_setting_fonts_clicked");
            }
        }));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            items.add(ViewItemBuilder.getLuckyItem());
        }
        items.add(ViewItemBuilder.getSoundsPositionItem());
        items.add(ViewItemBuilder.getLocationItem(new ViewItem.ViewItemListener() {
            @Override
            public void onItemClick(ViewItem item) {
                HSAnalytics.logEvent("keyboard_location_clicked");
                //保证不重复发起异步请求
                if (isLocationInfoFetching||(!checkLocationPermission((LocationManager) context.getSystemService(Context.LOCATION_SERVICE)))) {
                    Toast.makeText(HSApplication.getContext(), R.string.unable_location, Toast.LENGTH_SHORT).show();
                    KCAnalytics.logEvent("keyboard_location_sendFailed", "reason", "No network detected and no location permission");
                    return;
                }
                isLocationInfoFetching = true;
                reverseLocationHandler.removeMessages(LOCATION_MESSAGE);
                reverseLocationHandler.sendEmptyMessageAtTime(LOCATION_MESSAGE, TIMEOUT_MILLIS);
                Toast.makeText(HSApplication.getContext(), R.string.start_request_location, Toast.LENGTH_SHORT).show();
                HSLocationManager locationManagerDevice = new HSLocationManager(HSApplication.getContext());
                locationManagerDevice.setDeviceLocationTimeout(TIMEOUT_MILLIS);
                locationManagerDevice.fetchLocation(HSLocationManager.LocationSource.DEVICE, new HSLocationManager.HSLocationListener() {
                    @Override
                    public void onLocationFetched(boolean success, HSLocationManager locationManager) {
                        if (!success) {
                            Toast.makeText(HSApplication.getContext(), R.string.request_location_fail, Toast.LENGTH_SHORT).show();
                            KCAnalytics.logEvent("keyboard_location_sendFailed", "reason", "null location");
                            reverseLocationHandler.removeMessages(LOCATION_MESSAGE);
                            isLocationInfoFetching = false;
                        } else {
                            EditorInfo editorInfo = HSUIInputMethodService.getInstance().getCurrentInputEditorInfo();
                            tsk = new LocationReverseAsyncTask(editorInfo, TIMEOUT_MILLIS).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, locationManager.getLocation());
                        }
                    }

                    @Override
                    public void onGeographyInfoFetched(boolean success, HSLocationManager locationManager) {
                        if (!success) {
                            KCAnalytics.logEvent("onGeographyInfoFetched_keyboard_location_sendFail");
                        }else {
                            KCAnalytics.logEvent("onGeographyInfoFetched_keyboard_location_sendSuccess");
                        }
                    }
                });
            }
        }));
        items.add(ViewItemBuilder.getAutoCorrectionItem());
        if (selectorItem == null) {
            selectorItem = ViewItemBuilder.getSelectorItem(new ViewItem.ViewItemListener() {
                @Override
                public void onItemClick(ViewItem item) {
                    KCAnalytics.logEvent("keyboard_selector_clicked");
                    getPanelActionListener().showChildPanel(HSSelectorPanel.class, new Bundle());
                }
            });
        }
        items.add(selectorItem);
        // items.add(ViewItemBuilder.getAutoCapitalizationItem());
        // items.add(ViewItemBuilder.getPredicationItem());
        // items.add(ViewItemBuilder.getSwipeItem());
        items.add(ViewItemBuilder.getLanguageItem(new ViewItem.ViewItemListener() {
            @Override
            public void onItemClick(ViewItem item) {
                HSInputMethod.hideWindow();
                getPanelActionListener().showPanel(KeyboardPanel.class);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        HSUIInputMethod.launchMoreLanguageActivity();
                    }
                }, 100);
                KCAnalytics.logEvent("keyboard_setting_addlanguage_clicked");
            }
        }));
        items.add(ViewItemBuilder.getMoreSettingsItem(new ViewItem.ViewItemListener() {
            @Override
            public void onItemClick(ViewItem item) {
                HSInputMethod.hideWindow();
                getPanelActionListener().showPanel(KeyboardPanel.class);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        HSUIInputMethod.launchSettingsActivity();
                    }
                }, 100);
                KCAnalytics.logEvent("keyboard_setting_more_clicked");
            }
        }));

        return items;
    }

    private boolean checkLocationPermission(LocationManager locationManager) {
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false ;
        }
        return !(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));

    }


    private  static class LocationReverseAsyncTask extends AsyncTask<Location, Void, Address> {
        private String locationText = "";
        EditorInfo editorInfo;
        int timeoutMillis;

        LocationReverseAsyncTask(EditorInfo editorInfo, int timeoutMillis) {
            this.editorInfo = editorInfo;
            this.timeoutMillis = timeoutMillis;
        }

        @Override
        protected Address doInBackground(Location... locations) {
            Location location = locations[0];
            List<Address> addressList = null;
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            try {
                addressList = new Geocoder(HSApplication.getContext(), Locale.getDefault()).getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addressList!=null&&addressList.size() >= 1 ? addressList.get(0) : null;
        }

        @Override
        protected void onPostExecute(Address address) {
            if (address != null) {
                String streetName = address.getAddressLine(0);
                if (TextUtils.isEmpty(streetName)) {
                    Toast.makeText(HSApplication.getContext(), R.string.request_location_fail, Toast.LENGTH_SHORT).show();
                    KCAnalytics.logEvent("GeoCoder_keyboard_location_sendFailed", "reason", "result is not full");
                } else if (!TextUtils.isEmpty(streetName)&&streetName.contains(address.getPostalCode())) {
                        locationText = streetName.substring(0, streetName.length() - address.getPostalCode().length() - 2);
                }else {
                    locationText  =streetName;
                }
                if (editorInfo != null && editorInfo.equals(HSUIInputMethodService.getInstance().getCurrentInputEditorInfo())) {
                    HSInputMethod.inputText(locationText);
                    KCAnalytics.logEvent("GeoCoder_keyboard_location_sendSuccess");
                }

            } else {
                Toast.makeText(HSApplication.getContext(), R.string.request_location_fail, Toast.LENGTH_SHORT).show();
                KCAnalytics.logEvent("GeoCoder_keyboard_location_sendFailed", "reason", "Failed to get the location");
            }
            isLocationInfoFetching = false;
            reverseLocationHandler.removeMessages(LOCATION_MESSAGE);
        }
    }


    private static void setViewHeight(View v, int height) {
        if (v != null && v.getLayoutParams() != null) {
            final ViewGroup.LayoutParams params = v.getLayoutParams();
            params.height = height;
            v.requestLayout();
        }
    }

    private INotificationObserver notificationObserver = new INotificationObserver() {

        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (HSInputMethod.HS_NOTIFICATION_SHOW_INPUTMETHOD.equals(s)) {
                if (themeItem != null) {
                    themeItem.showNewMarkIfNeed();
                }
                if (items != null) {
                    for (ViewItem viewItem : items) {
                        if (viewItem.onItemListener != null) {
                            viewItem.onItemListener.onItemViewInvalidate(viewItem);
                        }
                    }
                }
            }
            if (NOTIFICATION_REMOVEADS_PURCHASED.equals(s)) {
                settingsViewPager.removeAds();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        items = null;
        themeItem = null;
        selectorItem = null;
        settingPanelView = null;
        ViewItemBuilder.release();
        HSGlobalNotificationCenter.removeObserver(notificationObserver);
    }

    @Override
    public Animation getAppearAnimator() {
        return showPanelAnimator(true);
    }

    @Override
    public Animation getDismissAnimator() {
        return showPanelAnimator(false);
    }

    @NonNull
    private Animation showPanelAnimator(final boolean appear) {
        int defaultKeyboardHeight = HSResourceUtils.getDefaultKeyboardHeight(HSApplication.getContext().getResources());
        setViewHeight(settingPanelView, defaultKeyboardHeight);

        TranslateAnimation showOrDismissPanelAnimator = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, appear ? -1 : 0, Animation.RELATIVE_TO_SELF, appear ? 0 : -1);
        showOrDismissPanelAnimator.setDuration(animDuration);
        showOrDismissPanelAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        showOrDismissPanelAnimator.setFillAfter(true);
        showOrDismissPanelAnimator.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                BaseFunctionBar functionBar = (BaseFunctionBar) panelActionListener.getBarView();
                functionBar.setFunctionEnable(false);
                if (onAnimationListener != null) {
                    onAnimationListener.onAnimationStart(animation);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                BaseFunctionBar functionBar = (BaseFunctionBar) panelActionListener.getBarView();
                functionBar.setSettingButtonType(appear ? SettingsButton.SettingButtonType.SETTING : SettingsButton.SettingButtonType.MENU);
                functionBar.setFunctionEnable(true);
                if (onAnimationListener != null) {
                    onAnimationListener.onAnimationEnd(animation);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        settingPanelView.startAnimation(showOrDismissPanelAnimator);
        return showOrDismissPanelAnimator;
    }


    @Override
    protected boolean onShowPanelView(int appearMode) {
        return true;
    }

    @Override
    protected boolean onHidePanelView(int appearMode) {
        switch (appearMode) {
            case MODE_BACK_PARENT:
                return true;
        }

        return false;
    }
}
