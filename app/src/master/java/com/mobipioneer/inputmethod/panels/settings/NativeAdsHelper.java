package com.mobipioneer.inputmethod.panels.settings;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.ads.NativeAd;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeAppInstallAdView;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSNetworkConnectionUtils;
import com.ihs.inputmethod.uimodules.R;
import com.mobipioneer.inputmethod.panels.settings.model.ViewItemBuilder;
import com.mobipioneer.lockerkeyboard.ads.AdmobNativeAdsManager;
import com.mobipioneer.lockerkeyboard.ads.AdsConstants;
import com.mobipioneer.lockerkeyboard.ads.FacebookNativeAdsManager;
import com.mobipioneer.lockerkeyboard.ads.HomeKeyWatcher;
import com.mobipioneer.lockerkeyboard.ads.NativeAdUtils;
import com.mobipioneer.lockerkeyboard.app.MyInputMethodApplication;
import com.mobipioneer.lockerkeyboard.utils.Constants;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

import static com.ihs.booster.common.recyclerview.animator.impl.ItemMoveAnimationManager.TAG;


/**
 * Created by chenyuanming on 26/09/2016.
 */

public class NativeAdsHelper {
    private boolean isShowingProgressbar = false;
    private Dialog dialog;
    private HomeKeyWatcher mHomeWatcher;

    private Context mContext;

    private ImageView btnAd;
    private TextView textAd;

    private NativeAppInstallAdView appInstallAdView;
    private ImageView appInstallAdImageView;
    private FrameLayout facebookAdView;
    private ImageView facebookAdImageView;
    private TextView appInstallAdTextView;
    private TextView facebookAdTextView;
    private LinearLayout nativeAdContainer;
    private LayoutInflater inflater;
    private ProgressBar progressbar;
    private ImageView progressImageView;
    private RelativeLayout progressbarRoot;
    private static NativeAdsHelper helper = new NativeAdsHelper();

    private AdmobNativeAdsManager admobNativeAdsManager;
    private FacebookNativeAdsManager facebookNativeAdsManager;

    private NativeAdsHelper() {
        mContext = HSApplication.getContext();
        inflater = LayoutInflater.from(mContext);

        initAds();
    }

    public static NativeAdsHelper getHelper() {
        return helper;
    }

    private class BitmapCompressor implements BitmapProcessor {
        private int desWidth;
        private int desHeight;

        public BitmapCompressor(int desWidth, int desHeight) {
            this.desWidth = desWidth;
            this.desHeight = desHeight;
        }

        @Override
        public Bitmap process(Bitmap bitmap) {
            int imgWidth = bitmap.getWidth();
            int imgHeight = bitmap.getHeight();
            int width = desWidth > bitmap.getWidth() ? desWidth : imgWidth;
            int height = desHeight > bitmap.getHeight() ? desHeight : imgHeight;

            Matrix matrix = new Matrix();
            matrix.setScale((float) (desWidth * 0.5 / width), (float) (desHeight * 0.5 / height));
            Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, imgWidth, imgHeight, matrix, true);

            bitmap.recycle();
            bitmap = null;

            Bitmap resultBitMap = ((BitmapDrawable) NativeAdUtils.getRoundedCornerDrawable(new BitmapDrawable(mContext.getResources(), newBitmap), 10, (btnAd.getDrawable().getIntrinsicWidth()), (btnAd.getDrawable().getIntrinsicHeight()))).getBitmap();

            newBitmap.recycle();
            newBitmap = null;

            return resultBitMap;
        }

    }


    private INotificationObserver loadDataObserver = new INotificationObserver() {
        @Override
        public void onReceive(String eventName, HSBundle notificaiton) {
            Log.d(TAG, "onReceive() called with: eventName = [" + eventName + "], notificaiton = [" + notificaiton + "]");

            if (notificaiton != null) {
                if ((notificaiton.getInt(AdsConstants.KEY_ADS_OWNER) != AdsConstants.ADS_OWNER_SETTINGS)) {
                    return;
                }
            }

            if (eventName.equals(AdmobNativeAdsManager.NOTIFICATION_APP_INSTALL_AD_CLICKED) || eventName.equals(FacebookNativeAdsManager.NOTIFICATION_FACEBOOK_AD_CLICKED)) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
            if (!isShowingProgressbar) {
                return;
            }
            hideProgressbar();
            if (eventName.equals(AdmobNativeAdsManager.NOTIFICATION_APP_INSTALL_AD_LOADED)) {
                if (facebookNativeAdsManager.getFacebookNativeAd() != null && !facebookNativeAdsManager.isCachedAdConsumed()) {
                    refreshFacebookNativeAdView();
                    return;
                }
                refreshAppInstallAdView();
            }
            if (eventName.equals(FacebookNativeAdsManager.NOTIFICATION_FACEBOOK_AD_LOADED)) {
                if (admobNativeAdsManager.getNativeAppInstallAd() != null && !admobNativeAdsManager.isCachedAdConsumed()) {
                    refreshAppInstallAdView();
                    return;
                }
                refreshFacebookNativeAdView();
            }
        }
    };


    public void onCreateAdView() {
        nativeAdContainer = ViewItemBuilder.getAdsItem().viewContainer;
        btnAd = ViewItemBuilder.getAdsItem().imageView;
        textAd = ViewItemBuilder.getAdsItem().textView;

        HSGlobalNotificationCenter.addObserver(AdmobNativeAdsManager.NOTIFICATION_APP_INSTALL_AD_LOADED, loadDataObserver);
        HSGlobalNotificationCenter.addObserver(AdmobNativeAdsManager.NOTIFICATION_CONTENT_AD_LOADED, loadDataObserver);
        HSGlobalNotificationCenter.addObserver(FacebookNativeAdsManager.NOTIFICATION_FACEBOOK_AD_LOADED, loadDataObserver);
        HSGlobalNotificationCenter.addObserver(AdmobNativeAdsManager.NOTIFICATION_APP_INSTALL_AD_CLICKED, loadDataObserver);
        HSGlobalNotificationCenter.addObserver(FacebookNativeAdsManager.NOTIFICATION_FACEBOOK_AD_CLICKED, loadDataObserver);


        HomeKeyWatcher mHomeWatcher = new HomeKeyWatcher(HSApplication.getContext());
        mHomeWatcher.setOnHomePressedListener(new HomeKeyWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        mHomeWatcher.startWatch();

    }


    public void onShowAdView() {
        isShowingProgressbar = false;

        int adLoadMode = HSConfig.optInteger(2, "Application", AdsConstants.CONFIG_NODE_NATIVE_ADS, AdsConstants.CONFIG_NODE_ADS_LOAD_MODEL);

        if (!HSNetworkConnectionUtils.isNetworkConnected()) {
            nativeAdContainer.removeAllViews();
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_KEYBOARD_SETTINGS_AD_NO_REQUEST, "network");
            return;
        }

        boolean shouldShowAds = HSConfig.optBoolean(false, "Application", AdsConstants.CONFIG_NODE_NATIVE_ADS, AdsConstants.CONFIG_NODE_SHOULD_SHOW_NATIVE_ADS);
        if (!shouldShowAds) {
            nativeAdContainer.removeAllViews();
            return;
        }

        if ((!MyInputMethodApplication.isGooglePlayInstalled() && adLoadMode == 0)) {
            nativeAdContainer.removeAllViews();
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_KEYBOARD_SETTINGS_AD_NO_REQUEST, "play");
            return;
        }


        if ((!MyInputMethodApplication.isFacebookAppInstalled() && adLoadMode != 1)) {
            nativeAdContainer.removeAllViews();
            HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(Constants.GA_PARAM_ACTION_KEYBOARD_SETTINGS_AD_NO_REQUEST, "facebook");
            return;
        }

        if ((facebookNativeAdsManager.getFacebookNativeAd() == null || !facebookNativeAdsManager.getFacebookNativeAd().isAdLoaded()) && admobNativeAdsManager.getNativeAppInstallAd() == null) {
            HSLog.d("should spin progress bar here");
            showProgressbar();
            facebookNativeAdsManager.fetchAds();
            admobNativeAdsManager.fetchAds();
            return;
        }


        if ((facebookNativeAdsManager.getFacebookNativeAd() != null && facebookNativeAdsManager.getFacebookNativeAd().isAdLoaded()) && admobNativeAdsManager.getNativeAppInstallAd() != null) {
            if (!facebookNativeAdsManager.isCachedAdConsumed()) {
                refreshFacebookNativeAdView();
                return;
            }
            if (admobNativeAdsManager.isCachedAdConsumed()) {
                refreshFacebookNativeAdView();
                return;
            }
            refreshAppInstallAdView();
            return;
        }

        if (facebookNativeAdsManager.getFacebookNativeAd() == null || !facebookNativeAdsManager.getFacebookNativeAd().isAdLoaded()) {
            facebookNativeAdsManager.fetchAds();
            refreshAppInstallAdView();
            return;
        }

        if (admobNativeAdsManager.getNativeAppInstallAd() == null) {
            admobNativeAdsManager.fetchAds();
            refreshFacebookNativeAdView();
            return;
        }

    }


    public void onDestroyAdView() {
        HSGlobalNotificationCenter.removeObserver(loadDataObserver);

        if (mHomeWatcher != null) {
            mHomeWatcher.stopWatch();
        }
    }

    private void showProgressbar() {
        nativeAdContainer.removeAllViews();
        progressbarRoot = (RelativeLayout) inflater.inflate(R.layout.progressbar_layout, nativeAdContainer, false);
        progressbar = (ProgressBar) progressbarRoot.findViewById(R.id.progressBar);
        progressImageView = (ImageView) progressbarRoot.findViewById(R.id.progress_imgview);
        progressImageView.setBackgroundDrawable(HSKeyboardThemeManager.getStyledDrawable(null, "settings_ad_bg.png"));

        TextView tv_loading= (TextView) progressbarRoot.findViewById(R.id.tv_loading);
        tv_loading.setTextColor(HSKeyboardThemeManager.getCurrentTheme().getStyledTextColor());

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.width = btnAd.getDrawable().getIntrinsicWidth();
        lp.height = btnAd.getDrawable().getIntrinsicHeight();
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        progressbar.setLayoutParams(lp);

        if (isShowingProgressbar) {
            return;
        }
        nativeAdContainer.addView(progressbarRoot);
        progressbarRoot.setVisibility(View.VISIBLE);
        isShowingProgressbar = true;
    }

    private void hideProgressbar() {
        progressbarRoot.setVisibility(View.GONE);
        nativeAdContainer.removeAllViews();
        isShowingProgressbar = false;
    }

    private void initAds() {
        admobNativeAdsManager = new AdmobNativeAdsManager(AdsConstants.ADS_OWNER_SETTINGS, HSConfig.getString("Application", AdsConstants.CONFIG_NODE_NATIVE_ADS, AdsConstants.CONFIG_NODE_SETTINGS_ADS, AdsConstants.CONFIG_NODE_GOOGLE_NATIVE_ADS_ID));
        facebookNativeAdsManager = new FacebookNativeAdsManager(AdsConstants.ADS_OWNER_SETTINGS, HSConfig.getString("Application", AdsConstants.CONFIG_NODE_NATIVE_ADS, AdsConstants.CONFIG_NODE_SETTINGS_ADS, AdsConstants.CONFIG_NODE_FACEBOOK_NATIVE_ADS_ID));
    }

    private void refreshAppInstallAdView() {
        admobNativeAdsManager.next();
        com.google.android.gms.ads.formats.NativeAd nativeAd = admobNativeAdsManager.getNativeAppInstallAd();
        if (nativeAd == null) {
            return;
        }
        if (nativeAdContainer == null) {
            return;
        }

        HSLog.d("refreshing install app ads");
        nativeAdContainer.removeAllViews();
        LinearLayout adView = (LinearLayout) inflater.inflate(R.layout.admob_native_adview, nativeAdContainer, false);
        appInstallAdImageView = (ImageView) adView.findViewById(R.id.app_install_image_view);
        appInstallAdTextView = (TextView) adView.findViewById(R.id.install_app_ad_label);
        appInstallAdTextView.setTextColor(HSKeyboardThemeManager.getCurrentTheme().getStyledTextColor());

        appInstallAdImageView.setBackgroundDrawable(new ColorDrawable(0));
        FrameLayout.LayoutParams fp = (FrameLayout.LayoutParams) appInstallAdImageView.getLayoutParams();
        fp.width = (btnAd.getDrawable().getIntrinsicWidth());
        fp.height = (btnAd.getDrawable().getIntrinsicHeight());


        appInstallAdImageView.setLayoutParams(fp);

        if (nativeAd instanceof NativeAppInstallAd) {
            ImageLoader.getInstance().displayImage(((NativeAppInstallAd) nativeAd).getIcon().getUri().toString(), appInstallAdImageView, new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true)
                    .postProcessor(new BitmapCompressor(appInstallAdImageView.getLayoutParams().width, appInstallAdImageView.getLayoutParams().height)).build());
            appInstallAdTextView.setText(((NativeAppInstallAd) nativeAd).getHeadline());
        } else {
            if (((NativeContentAd) nativeAd).getLogo() != null) {
                ImageLoader.getInstance().displayImage(((NativeContentAd) nativeAd).getLogo().getUri().toString(), appInstallAdImageView, new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true)
                        .postProcessor(new BitmapCompressor(appInstallAdImageView.getLayoutParams().width, appInstallAdImageView.getLayoutParams().height)).build());
            } else {
                ImageLoader.getInstance().displayImage(((NativeContentAd) nativeAd).getImages().get(0).getUri().toString(), appInstallAdImageView, new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true)
                        .postProcessor(new BitmapCompressor(appInstallAdImageView.getLayoutParams().width, appInstallAdImageView.getLayoutParams().height)).build());
            }
            appInstallAdTextView.setText(((NativeContentAd) nativeAd).getHeadline());
        }

        nativeAdContainer.addView(adView);
        admobNativeAdsManager.logAdShowingTime();

        admobNativeAdsManager.setStartShowingTime(System.currentTimeMillis());
        appInstallAdImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                admobNativeAdsManager.logAdClick();
                showAdmobAdAlert();
            }
        });
        admobNativeAdsManager.fetchAds();
    }


    private void refreshFacebookNativeAdView() {
        facebookNativeAdsManager.next();
        NativeAd facebookNativeAd = facebookNativeAdsManager.getFacebookNativeAd();
        if (facebookNativeAd == null) {
            return;
        }
        if (nativeAdContainer == null) {
            return;
        }

        HSLog.d("refreshing facebook ads");
        nativeAdContainer.removeAllViews();
        LinearLayout adView = (LinearLayout) inflater.inflate(R.layout.facebook_native_adview, nativeAdContainer, false);
        facebookAdView = (FrameLayout) adView.findViewById(R.id.facebook_ad_view);

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) facebookAdView.getLayoutParams();
        lp.width = btnAd.getDrawable().getIntrinsicWidth();
        lp.height = btnAd.getDrawable().getIntrinsicHeight();
        facebookAdView.setLayoutParams(lp);
        facebookAdImageView = (ImageView) adView.findViewById(R.id.facebook_ad_image_view);

        FrameLayout.LayoutParams fp = (FrameLayout.LayoutParams) facebookAdImageView.getLayoutParams();
        fp.width = (int) (btnAd.getDrawable().getIntrinsicWidth() * 0.98);
        fp.height = (int) (btnAd.getDrawable().getIntrinsicHeight() * 0.98);
        facebookAdImageView.setLayoutParams(fp);
        facebookAdImageView.setScaleType(ImageView.ScaleType.FIT_XY);

        facebookAdTextView = (TextView) adView.findViewById(R.id.facebook_ad_label);
        ImageLoader.getInstance().displayImage(facebookNativeAd.getAdIcon().getUrl(), facebookAdImageView, new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).postProcessor(new BitmapProcessor() {
            @Override
            public Bitmap process(Bitmap bitmap) {
                Bitmap newBitmap = ((BitmapDrawable) NativeAdUtils.getRoundedCornerDrawable(bitmap, 15)).getBitmap();
                bitmap.recycle();
                bitmap = null;
                return newBitmap;
            }
        }).build());

        facebookAdTextView.setText(facebookNativeAd.getAdTitle());
        facebookAdTextView.setTextColor(HSKeyboardThemeManager.getCurrentTheme().getStyledTextColor());
        nativeAdContainer.addView(adView);
        facebookNativeAdsManager.logAdShowingTime();

        facebookNativeAdsManager.setStartShowingTime(System.currentTimeMillis());
        boolean shouldShowFacebookAlert = HSConfig.optBoolean(true, "Application", AdsConstants.CONFIG_NODE_NATIVE_ADS, AdsConstants.CONFIG_NODE_SHOW_FACEBOOK_ALERT);
        if (shouldShowFacebookAlert) {
            facebookAdImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    facebookNativeAdsManager.logAdClick();
                    showFacebookAdAlert();
                }
            });
        } else {
            facebookNativeAd.registerViewForInteraction(adView);
        }
        facebookNativeAdsManager.fetchNextAds();

    }

    private boolean shouldRoundCorner(final float bitmapRatio, final WindowManager.LayoutParams parent, final LinearLayout.LayoutParams child, final ImageView view) {
        boolean showRoundCorner = true;
        child.height = (int) (parent.width / bitmapRatio);
        if (child.height > (int) (parent.height * 0.6)) {
            showRoundCorner = false;
            child.height = (int) (parent.height * 0.6);
            child.width = (int) ((float) child.height * bitmapRatio);
        }
        view.post(new Runnable() {
            @Override
            public void run() {
                view.setLayoutParams(child);
            }
        });
        return showRoundCorner;
    }

    private void showAdmobAdAlert() {
        final com.google.android.gms.ads.formats.NativeAd nativeAd = admobNativeAdsManager.getNativeAppInstallAd();
        if (nativeAd == null) {
            return;
        }

        dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.native_ad_alert_admob);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        final WindowManager.LayoutParams param = new WindowManager.LayoutParams();
        param.copyFrom(dialog.getWindow().getAttributes());
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int orientation = display.getRotation();
        if (orientation == Surface.ROTATION_90
                || orientation == Surface.ROTATION_270) {
            param.width = (int) (size.x * 0.48);
            param.height = (int) (size.y * 0.81);
        } else {
            param.width = (int) (size.x * 0.81);
            param.height = (int) (size.y * 0.48);
        }
        dialog.getWindow().setAttributes(param);

        ImageView bgImage = (ImageView) dialog.findViewById(R.id.ad_alert_bg_img);
        bgImage.setBackgroundDrawable(HSApplication.getContext().getResources().getDrawable(R.drawable.admobad_alert_bg));

        final ImageView image = (ImageView) dialog.findViewById(R.id.ad_alert_big_img);
        final LinearLayout.LayoutParams bigImgParam = (LinearLayout.LayoutParams) image.getLayoutParams();
        bigImgParam.width = param.width;
        ImageView icon = (ImageView) dialog.findViewById(R.id.ad_alert_icon);
        TextView bodyText = (TextView) dialog.findViewById(R.id.ad_alert_body_text);
        TextView titleText = (TextView) dialog.findViewById(R.id.ad_alert_title);

        LinearLayout.LayoutParams iconParam = (LinearLayout.LayoutParams) icon.getLayoutParams();
        iconParam.width = (int) (param.width * 0.2);
        iconParam.height = iconParam.width;
        icon.setLayoutParams(iconParam);
        RelativeLayout dialogButton = (RelativeLayout) dialog.findViewById(R.id.ad_alert_button);

        LinearLayout.LayoutParams btnParam = (LinearLayout.LayoutParams) dialogButton.getLayoutParams();
        btnParam.height = (int) (param.height * 0.125);
        dialogButton.setLayoutParams(btnParam);
        TextView btnText = (TextView) dialogButton.findViewById(R.id.ad_alert_btn_text);

        if (nativeAd instanceof NativeAppInstallAd) {
            ImageLoader.getInstance().displayImage(((NativeAppInstallAd) nativeAd).getImages().get(0).getUri().toString(), image, new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).postProcessor(new BitmapProcessor() {
                @Override
                public Bitmap process(Bitmap bitmap) {
                    shouldRoundCorner(((float) bitmap.getWidth()) / bitmap.getHeight(), param, bigImgParam, image);

                    return bitmap;
                }
            }).build());
            ImageLoader.getInstance().displayImage(((NativeAppInstallAd) nativeAd).getIcon().getUri().toString(), icon, new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true)
                    .postProcessor(new BitmapCompressor(icon.getLayoutParams().width, icon.getLayoutParams().height)).build());
            bodyText.setText(((NativeAppInstallAd) nativeAd).getBody());
            titleText.setText(((NativeAppInstallAd) nativeAd).getHeadline());
            btnText.setText(((NativeAppInstallAd) nativeAd).getCallToAction().toString().trim());
        } else {

            ImageLoader.getInstance().displayImage(((NativeContentAd) nativeAd).getImages().get(0).getUri().toString(), image, new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).postProcessor(new BitmapProcessor() {
                @Override
                public Bitmap process(Bitmap bitmap) {
                    shouldRoundCorner(((float) bitmap.getWidth()) / bitmap.getHeight(), param, bigImgParam, image);

                    return bitmap;
                }
            }).build());

            bodyText.setText(((NativeContentAd) nativeAd).getBody());
            titleText.setText(((NativeContentAd) nativeAd).getHeadline());
            btnText.setText(((NativeContentAd) nativeAd).getCallToAction().toString().trim());

            if (((NativeContentAd) nativeAd).getLogo() != null) {
                ImageLoader.getInstance().displayImage(((NativeContentAd) nativeAd).getLogo().getUri().toString(), icon, new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true)
                        .postProcessor(new BitmapCompressor(icon.getLayoutParams().width, icon.getLayoutParams().height)).build());
            } else {
                icon.setVisibility(View.GONE);
            }
        }

        appInstallAdView = (NativeAppInstallAdView) dialog.findViewById(R.id.ad_alert_root);
        appInstallAdView.setNativeAd(nativeAd);
        appInstallAdView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        appInstallAdView.setCallToActionView(dialogButton);
        appInstallAdView.setImageView(image);
        appInstallAdView.setIconView(icon);
        appInstallAdView.setBodyView(bodyText);
        appInstallAdView.setHeadlineView(titleText);
        dialog.setCancelable(true);
        dialog.show();
    }


    private void showFacebookAdAlert() {

        final NativeAd facebookAd = facebookNativeAdsManager.getFacebookNativeAd();
        if (facebookAd == null) {
            return;
        }

        dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.native_ad_alert_facebook);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        final WindowManager.LayoutParams param = new WindowManager.LayoutParams();
        param.copyFrom(dialog.getWindow().getAttributes());
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int orientation = display.getRotation();
        if (orientation == Surface.ROTATION_90
                || orientation == Surface.ROTATION_270) {
            param.width = (int) (size.x * 0.48);
            param.height = (int) (size.y * 0.81);
        } else {
            param.width = (int) (size.x * 0.81);
            param.height = (int) (size.y * 0.48);
        }

        dialog.getWindow().setAttributes(param);

        ImageView bgImage = (ImageView) dialog.findViewById(R.id.facebook_ad_alert_bg_img);
        bgImage.setBackgroundDrawable(NativeAdUtils.getRoundedCornerDrawable(HSApplication.getContext().getResources().getDrawable(R.drawable.ad_alert_bg), 20));
        final ImageView image = (ImageView) dialog.findViewById(R.id.facebook_ad_alert_big_img);
        final LinearLayout.LayoutParams bigImgParam = (LinearLayout.LayoutParams) image.getLayoutParams();
        bigImgParam.width = param.width;
        bigImgParam.height = (int) (param.width * (float) facebookAd.getAdCoverImage().getHeight() / facebookAd.getAdCoverImage().getWidth());
        image.setLayoutParams(bigImgParam);

        ImageView icon = (ImageView) dialog.findViewById(R.id.facebook_ad_alert_icon);
        TextView bodyText = (TextView) dialog.findViewById(R.id.facebook_ad_alert_body_text);
        TextView titleText = (TextView) dialog.findViewById(R.id.facebook_ad_alert_title);

        LinearLayout.LayoutParams iconParam = (LinearLayout.LayoutParams) icon.getLayoutParams();
        iconParam.width = (int) (param.width * 0.2);
        iconParam.height = (int) (param.height * 0.2);
        icon.setLayoutParams(iconParam);

        ImageLoader.getInstance().displayImage(facebookAd.getAdCoverImage().getUrl(), image, new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).postProcessor(new BitmapProcessor() {
            @Override
            public Bitmap process(Bitmap bitmap) {
                boolean shouldRoundCorner = shouldRoundCorner(((float) bitmap.getWidth()) / bitmap.getHeight(), param, bigImgParam, image);

                return ((BitmapDrawable) NativeAdUtils.getRoundedTopCornerDrawable(bitmap, shouldRoundCorner ? 15 : 0)).getBitmap();
            }
        }).build());
        ImageLoader.getInstance().displayImage(facebookAd.getAdIcon().getUrl(), icon, new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).postProcessor(new BitmapProcessor() {
            @Override
            public Bitmap process(Bitmap bitmap) {
                Bitmap newBitmap = ((BitmapDrawable) NativeAdUtils.getRoundedCornerDrawable(bitmap, 15)).getBitmap();
                bitmap.recycle();
                bitmap = null;
                return newBitmap;
            }
        }).build());

        bodyText.setText(facebookAd.getAdBody());
        titleText.setText(facebookAd.getAdTitle());
        RelativeLayout dialogButton = (RelativeLayout) dialog.findViewById(R.id.facebook_ad_alert_button);
        LinearLayout.LayoutParams btnParam = (LinearLayout.LayoutParams) dialogButton.getLayoutParams();
        btnParam.height = (int) (param.height * 0.125);
        dialogButton.setLayoutParams(btnParam);

        TextView btnText = (TextView) dialogButton.findViewById(R.id.facebook_ad_alert_btn_text);
        if (TextUtils.isEmpty(facebookAd.getAdCallToAction())) {
            btnText.setText("Install Now");
        } else {
            btnText.setText(facebookAd.getAdCallToAction().toString().trim());
        }

        FrameLayout adView = (FrameLayout) dialog.findViewById(R.id.facebook_native_ad_root);
        facebookAd.registerViewForInteraction(adView);
        dialog.setCancelable(true);
        dialog.show();
    }


}
