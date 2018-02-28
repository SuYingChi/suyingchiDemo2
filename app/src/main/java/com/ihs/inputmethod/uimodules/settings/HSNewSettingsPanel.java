package com.ihs.inputmethod.uimodules.settings;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.AppOpsManagerCompat;
import android.text.TextUtils;
import android.util.Log;
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
import com.ihs.commons.utils.HSLog;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;
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
                HSAnalytics.logEvent("keyboard_location_clicked ");
                Toast.makeText(HSApplication.getContext(), "Locating....", Toast.LENGTH_SHORT).show();
                boolean isLocServiceEnable = isLocServiceEnable();
                boolean isLocationNetworkEnable = isLocationNetworkEnable();
                if (!isLocServiceEnable & !isLocationNetworkEnable) {
                    Toast.makeText(HSApplication.getContext(), R.string.no_location_permission, Toast.LENGTH_SHORT).show();
                    KCAnalytics.logEvent("keyboard_location_sendFailed", "unable  location feature ");
                    Toast.makeText(HSApplication.getContext(), R.string.network_not_available, Toast.LENGTH_LONG).show();
                    KCAnalytics.logEvent("keyboard_location_sendFailed", " network no available");
                    return;
                }
                int timeoutMillis = 10000;
                EditorInfo editorInfo = HSUIInputMethodService.getInstance().getCurrentInputEditorInfo();
                HSLocationManager locationManager_device = new HSLocationManager(HSApplication.getContext());
                locationManager_device.setDeviceLocationTimeout(timeoutMillis);
                long startTime = System.currentTimeMillis();
                locationManager_device.fetchLocation(HSLocationManager.LocationSource.DEVICE, new HSLocationManager.HSLocationListener() {
                    volatile boolean isGeoCoderFetchFinish = false;
                    volatile boolean isGeographyFetchFinish = false;
                    String locationText="";
                    @SuppressLint("StaticFieldLeak")
                    @Override
                    public void onLocationFetched(boolean success, HSLocationManager locationManager) {
                        if(locationManager.getLocation()==null){
                            Toast.makeText(HSApplication.getContext(), R.string.request_null_LaLongitude_fail, Toast.LENGTH_LONG).show();
                            KCAnalytics.logEvent("keyboard_location_sendFailed", "device nonsupport location");
                            return;
                        }
                        new AsyncTask<Location, Void, Address>() {
                            @Override
                            protected Address doInBackground(Location... locations) {
                                Location location = locations[0];
                                List<Address> addressList = new ArrayList<Address>();
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                try {
                                    addressList = new Geocoder(HSApplication.getContext(), getCurrentLocale()).getFromLocation(latitude, longitude, 1);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                return addressList.get(0);
                            }

                            @Override
                            protected void onPostExecute(Address address) {
                                if (isGeographyFetchFinish) {
                                    HSLog.d("suyingchi","onLocationFetched---------"+"isGeographyFetchFinish=="+isGeographyFetchFinish+"------------182");
                                    return;
                                }
                                if (address != null) {
                                    //国家
                                    String country = address.getCountryName();
                                    //省份
                                    String adminArea = address.getAdminArea();
                                    //城市名
                                    String locality = address.getLocality();
                                    //街名路牌号,streetName取不到的时候使用该地址
                                    String featureName = address.getFeatureName();
                                    //城区
                                    String subLocality = address.getSubLocality();
                                    //优先使用该地址
                                    String streetName = address.getAddressLine(0);
                                    if (TextUtils.isEmpty(country) || TextUtils.isEmpty(adminArea) || TextUtils.isEmpty(locality) || (TextUtils.isEmpty(featureName) && TextUtils.isEmpty(streetName))) {
                                        if(!isGeographyFetchFinish || !isGeoCoderFetchFinish){
                                            HSLog.d("suyingchi","onLocationFetched---------"+"isGeographyFetchFinish=="+isGeographyFetchFinish+"--------isGeoCoderFetchFinish===="+isGeoCoderFetchFinish+"------------199");
                                            return;
                                        }
                                        long endTime = System.currentTimeMillis();
                                        if (endTime - startTime >= timeoutMillis) {
                                            Toast.makeText(HSApplication.getContext(), R.string.request_location_timeout, Toast.LENGTH_LONG).show();
                                            KCAnalytics.logEvent("keyboard_location_sendFailed", "request timeout");
                                        } else {
                                            Toast.makeText(HSApplication.getContext(), R.string.request_location_fail, Toast.LENGTH_LONG).show();
                                            KCAnalytics.logEvent("keyboard_location_sendFailed", "device nonsupport location");
                                        }
                                        isGeoCoderFetchFinish = true;
                                        HSLog.d("suyingchi","onLocationFetched---------"+"isGeographyFetchFinish=="+isGeographyFetchFinish+"--------isGeoCoderFetchFinish===="+isGeoCoderFetchFinish+"------------211");
                                        return;
                                        //有些低版本手机取不到完整的街道名，这种情况下使用featureName
                                    }else if (!TextUtils.isEmpty(streetName)&&streetName.contains(subLocality)) {
                                        //去除结果文本中的邮编
                                        locationText = streetName.substring(0,streetName.length()-8);;
                                    }else if(!TextUtils.isEmpty(featureName)){
                                        locationText = featureName+","+subLocality+","+locality+","+adminArea+","+country;
                                    }

                                    if (!isGeoCoderFetchFinish&&!isGeographyFetchFinish && editorInfo != null && editorInfo.equals(HSUIInputMethodService.getInstance().getCurrentInputEditorInfo())) {
                                        HSInputMethod.inputText(locationText);
                                        KCAnalytics.logEvent("keyboard_location_sendSuccess");
                                        isGeoCoderFetchFinish = true;
                                        HSLog.d("suyingchi","onLocationFetched---------"+"isGeographyFetchFinish=="+isGeographyFetchFinish+"--------isGeoCoderFetchFinish===="+isGeoCoderFetchFinish+"------------225");
                                    }
                                }else {
                                    if(!isGeographyFetchFinish || !isGeoCoderFetchFinish){
                                        HSLog.d("suyingchi","onLocationFetched---------"+"isGeographyFetchFinish=="+isGeographyFetchFinish+"--------isGeoCoderFetchFinish===="+isGeoCoderFetchFinish+"------------244");
                                        return;
                                    }
                                    long endTime = System.currentTimeMillis();
                                    if (endTime - startTime >= timeoutMillis) {
                                        Toast.makeText(HSApplication.getContext(), R.string.request_location_timeout, Toast.LENGTH_LONG).show();
                                        KCAnalytics.logEvent("keyboard_location_sendFailed", "request timeout");
                                    } else {
                                        Toast.makeText(HSApplication.getContext(), R.string.request_location_fail, Toast.LENGTH_LONG).show();
                                        KCAnalytics.logEvent("keyboard_location_sendFailed", "device nonsupport location");
                                    }
                                    isGeoCoderFetchFinish = true;
                                    HSLog.d("suyingchi","onLocationFetched---------"+"isGeographyFetchFinish=="+isGeographyFetchFinish+"--------isGeoCoderFetchFinish===="+isGeoCoderFetchFinish+"------------256");
                                }

                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, locationManager.getLocation());

                    }

                    @Override
                    public void onGeographyInfoFetched(boolean success, HSLocationManager locationManager) {
                        if (isGeoCoderFetchFinish) {
                            HSLog.d("suyingchi","onGeographyInfoFetched---------"+"isGeographyFetchFinish=="+isGeographyFetchFinish+"--------isGeoCoderFetchFinish===="+isGeoCoderFetchFinish+"------------267");
                            return;
                        }
                        if (success) {
                            String city = String.valueOf(locationManager.getCity());
                            String subLocality = locationManager.getSublocality();
                            String Neighborhood = locationManager.getNeighborhood();
                            String country = locationManager.getCountry();
                            if (TextUtils.isEmpty(city) || TextUtils.isEmpty(subLocality) || TextUtils.isEmpty(Neighborhood) || TextUtils.isEmpty(country)) {
                                if(!isGeographyFetchFinish || !isGeoCoderFetchFinish){
                                    return;
                                }
                                long endTime = System.currentTimeMillis();
                                if (endTime - startTime >= timeoutMillis) {
                                    Toast.makeText(HSApplication.getContext(), R.string.request_location_timeout, Toast.LENGTH_LONG).show();
                                    KCAnalytics.logEvent("keyboard_location_sendFailed", "request timeout");
                                } else {
                                    Toast.makeText(HSApplication.getContext(), R.string.request_location_fail, Toast.LENGTH_LONG).show();
                                    KCAnalytics.logEvent("keyboard_location_sendFailed", "device nonsupport location");
                                }
                                isGeographyFetchFinish = true;
                                HSLog.d("suyingchi","onGeographyInfoFetched---------"+"isGeographyFetchFinish=="+isGeographyFetchFinish+"--------isGeoCoderFetchFinish===="+isGeoCoderFetchFinish+"------------288");
                                return;
                            }
                            if (!isGeographyFetchFinish&&!isGeoCoderFetchFinish && editorInfo != null && editorInfo.equals(HSUIInputMethodService.getInstance().getCurrentInputEditorInfo())) {
                                HSInputMethod.inputText(Neighborhood + "," + subLocality + "," + city + "," + country);
                                KCAnalytics.logEvent("keyboard_location_sendSuccess");
                                isGeographyFetchFinish = true;
                                HSLog.d("suyingchi","onGeographyInfoFetched---------"+"isGeographyFetchFinish=="+isGeographyFetchFinish+"--------isGeoCoderFetchFinish===="+isGeoCoderFetchFinish+"------------295");
                            }
                        } else {
                            if(!isGeographyFetchFinish || !isGeoCoderFetchFinish){
                                HSLog.d("suyingchi","onGeographyInfoFetched---------"+"isGeographyFetchFinish=="+isGeographyFetchFinish+"--------isGeoCoderFetchFinish===="+isGeoCoderFetchFinish+"------------313");
                                return;
                            }
                            long endTime = System.currentTimeMillis();
                            if (endTime - startTime >= timeoutMillis) {
                                Toast.makeText(HSApplication.getContext(), R.string.request_location_timeout, Toast.LENGTH_LONG).show();
                                KCAnalytics.logEvent("keyboard_location_sendFailed", "request timeout");
                            } else {
                                Toast.makeText(HSApplication.getContext(), R.string.request_location_fail, Toast.LENGTH_LONG).show();
                                KCAnalytics.logEvent("keyboard_location_sendFailed", "device nonsupport location");
                            }
                            isGeographyFetchFinish = true;
                            HSLog.d("suyingchi","onGeographyInfoFetched---------"+"isGeographyFetchFinish=="+isGeographyFetchFinish+"--------isGeoCoderFetchFinish===="+isGeoCoderFetchFinish+"------------325");
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

   private Locale getCurrentLocale(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return context.getResources().getConfiguration().getLocales().get(0);
        } else{
            //noinspection deprecation
            return context.getResources().getConfiguration().locale;
        }
    }


    //判断定位服务与权限
    private boolean isLocServiceEnable() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            final int AppOpsManager_OP_GPS = 2;
            final int AppOpsManager_OP_FINE_LOCATION = 1;
            int checkResult = checkOp(context, AppOpsManager_OP_GPS);
            int checkResult2 = checkOp(context, AppOpsManager_OP_FINE_LOCATION);
            if ((AppOpsManagerCompat.MODE_IGNORED != checkResult && AppOpsManagerCompat.MODE_IGNORED != checkResult2) == false) {
                return false;
            }
        }
        if ((locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) == false) {
            return false;
        }
        return true;
    }

    //判断网络状态
    private boolean isLocationNetworkEnable() {
        ConnectivityManager manager = (ConnectivityManager) context
                .getApplicationContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);

        if (manager == null) {
            return false;
        }

        NetworkInfo networkinfo = manager.getActiveNetworkInfo();
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean network = false;
        if (locationManager != null) {
            network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }
        if ((networkinfo != null && networkinfo.isAvailable() && network) == false) {
            return false;
        }
        return true;
    }

    //检查权限列表
    private static int checkOp(Context context, int op) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= Build.VERSION_CODES.KITKAT) {
            Object object = context.getSystemService(Context.APP_OPS_SERVICE);
            Class c = null;
            if (object != null) {
                c = object.getClass();
            }
            try {
                Class[] cArg = new Class[3];
                cArg[0] = int.class;
                cArg[1] = int.class;
                cArg[2] = String.class;
                Method lMethod = null;
                if (c != null)
                    lMethod = c.getDeclaredMethod("checkOp", cArg);
                if (lMethod != null) {
                    return (Integer) lMethod.invoke(object, op, Binder.getCallingUid(), context.getPackageName());
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return AppOpsManagerCompat.noteOp(context, AppOpsManager.OPSTR_FINE_LOCATION, context.getApplicationInfo().uid,
                            context.getPackageName());
                }

            }
        }
        return -1;
    }

    public static void setViewHeight(View v, int height) {
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
