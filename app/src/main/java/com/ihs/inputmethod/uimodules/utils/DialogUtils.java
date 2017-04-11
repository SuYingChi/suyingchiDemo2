package com.ihs.inputmethod.uimodules.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import com.ihs.actiontrigger.model.AppInfoBean;
import com.ihs.actiontrigger.utils.FormatSizeBuilder;
import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.HSUIInputMethod;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.utils.HSActivityUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.nativeads.NativeAdParams;
import com.ihs.keyboardutils.nativeads.NativeAdView;
import com.ihs.keyboardutils.nativeads.NativeAdView.OnAdLoadedListener;

import static android.view.View.VISIBLE;
import static com.ihs.app.framework.HSApplication.getContext;

/**
 * Created by chenyuanming on 07/02/2017.
 */

public class DialogUtils {
    private static long adShowTime;
    private static NativeAdView nativeAdView;

    public static boolean showUninstallAlert(AppInfoBean appInfo) {
        boolean isShowAlert = PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).getBoolean(HSApplication.getContext().getString(R.string.key_uninstall), true);
        if (!isShowAlert) {
            return false;
        }

        // Create custom dialog object
        final AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.uninstallDialog)).create();

        // hide to default title for Dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // inflate the layout dialog_layout.xml and set it as contentView
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.apk_uninstall_alert, null, false);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setView(view);

        initView(appInfo, dialog, view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            dialog.getWindow().setType(LayoutParams.TYPE_TOAST);
        } else {
            dialog.getWindow().setType(LayoutParams.TYPE_SYSTEM_ALERT);
        }
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (adShowTime != 0) {
                    long duration = System.currentTimeMillis() - adShowTime;
                    HSGoogleAnalyticsUtils.getInstance().logAppEvent("NativeAd_Master_A(NativeAds)UninstallAd_DisplayTime", duration / 1000f + "s");
                }
                if (nativeAdView != null) {
                    nativeAdView.release();
                }
            }
        });

        return true;
    }

    private static void initView(AppInfoBean appInfo, final AlertDialog dialog, View view) {
        view.findViewById(R.id.uninstall_setting).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("jumpTo", HSApplication.getContext().getString(R.string.key_uninstall));
                intent.setAction(HSApplication.getContext().getPackageName() + "." + HSUIInputMethod.HS_LAUNCH_SETTINGS);
                if (HSActivityUtils.isAppActivityOnTop()) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                } else {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                }
                HSApplication.getContext().startActivity(intent);
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.uninstall_close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        TextView appTitle = (TextView) view.findViewById(R.id.uninstall_app_info);
        TextView appStorage = (TextView) view.findViewById(R.id.uninstall_app_storage);

        appTitle.setText(HSApplication.getContext().getResources().getString(R.string.app_uninstalled_name, appInfo.getAppName()));
        FormatSizeBuilder builder = new FormatSizeBuilder(appInfo.getAppSize());
        String storageWithColor = "<font color='#0C4DAD'>" + builder.size + builder.unit + "</font>";
        String freeStorage = HSApplication.getContext().getResources().getString(R.string.app_free_storage, storageWithColor);
        appStorage.setText(Html.fromHtml(freeStorage));


        initAdView(view, dialog);
    }

    private static void initAdView(View rootView, final AlertDialog dialog) {
        final ViewGroup layoutAdRecommend = (ViewGroup) rootView.findViewById(R.id.ad_recommend_layout);
        layoutAdRecommend.setVisibility(View.GONE);
        ViewGroup adContainer = (ViewGroup) rootView.findViewById(R.id.ad_container);

        View inflate = View.inflate(getContext(), R.layout.ad_style_uninstall, null);
        NativeAdView nativeAdView = new NativeAdView(getContext(), inflate);
        HSGoogleAnalyticsUtils.getInstance().logAppEvent("NativeAd_Master_A(NativeAds)UninstallAd_Load");
        nativeAdView.setOnAdClickedListener(new NativeAdView.OnAdClickedListener() {
            @Override
            public void onAdClicked(NativeAdView nativeAdView) {
                dialog.dismiss();
                HSGoogleAnalyticsUtils.getInstance().logAppEvent("NativeAd_Master_A(NativeAds)UninstallAd_Click");
            }
        });
        adShowTime = 0;
        nativeAdView.setOnAdLoadedListener(new OnAdLoadedListener() {
            @Override
            public void onAdLoaded(NativeAdView nativeAdView) {
                layoutAdRecommend.setVisibility(VISIBLE);
                HSGoogleAnalyticsUtils.getInstance().logAppEvent("NativeAd_Master_A(NativeAds)UninstallAd_Show");
                adShowTime = System.currentTimeMillis();
            }
        });
        nativeAdView.configParams(new NativeAdParams(HSApplication.getContext().getResources().getString(R.string.ad_placement_uninstall)));


        adContainer.addView(nativeAdView);
    }

}
