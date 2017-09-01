package com.ihs.inputmethod.uimodules.ui.theme.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.api.keyboard.HSKeyboardTheme;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.theme.HSThemeNewTipController;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.analytics.ThemeAnalyticsReporter;
import com.ihs.inputmethod.uimodules.utils.ViewConvertor;
import com.ihs.keyboardutils.iap.RemoveAdsManager;
import com.ihs.keyboardutils.nativeads.KCNativeAdView;
import com.keyboard.core.themes.custom.KCCustomThemeManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static com.ihs.keyboardutils.iap.RemoveAdsManager.NOTIFICATION_REMOVEADS_PURCHASED;

/**
 * Created by jixiang on 16/8/22.
 */
public class ThemeBannerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {
    private final static int AUTO_SCROLL_DELAY_DEFAULT = 6000;
    private final static int MSG_WHAT_START = 1;
    private final static int MSG_WHAT_LOOP = 2;
    private static int AUTO_SCROLL_DELAY;
    private final int bannerWidth;
    private final int bannerHeight;

    private ViewPager viewPager;
    private Activity activity;
    private List<HSKeyboardTheme> keyboardThemeList;
    private boolean isStartLoop = false;
    private boolean isLoop = true;
    private boolean isInfinite = true;
    private boolean hasInit = false;
    private long lastScrollTime = 0;
    private final static int loopMultiple = 500;

    private CardView adCardView;
    private KCNativeAdView nativeAdView;

    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .displayer(new RoundedBitmapDisplayer(HSDisplayUtils.dip2px(2)))
            .cacheInMemory(true).cacheOnDisk(true)
            .imageScaleType(ImageScaleType.EXACTLY)
            .build();
    private boolean themeAnalyticsEnabled = false;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_START:
                    if (isLoop) {
                        handler.sendEmptyMessageDelayed(MSG_WHAT_LOOP, AUTO_SCROLL_DELAY > 0 ? AUTO_SCROLL_DELAY : AUTO_SCROLL_DELAY_DEFAULT);
                    }
                    break;
                case MSG_WHAT_LOOP:
                    long castTimeUtilLastScrollTime = System.currentTimeMillis() - lastScrollTime;
                    if (castTimeUtilLastScrollTime >= AUTO_SCROLL_DELAY) {
                        int position = viewPager.getCurrentItem() + 1;
                        if (position >= getCount()) {
                            position = getInitItem();
                        }
                        int delayTime = AUTO_SCROLL_DELAY > 0 ? AUTO_SCROLL_DELAY : AUTO_SCROLL_DELAY_DEFAULT;
                        if (adCardView != null && keyboardThemeList.get(position % getRealCount()) == null) {
                            delayTime = HSConfig.optInteger(AUTO_SCROLL_DELAY_DEFAULT, "Application", "ThemeContents", "themeConfig", "BannerNativeAdAutoScrollDelay");
                        }
                        viewPager.setCurrentItem(position);
                        if (isLoop) {
                            handler.sendEmptyMessageDelayed(MSG_WHAT_LOOP, delayTime);
                        }
                    } else {
                        if (isLoop) {
                            handler.sendEmptyMessageDelayed(MSG_WHAT_LOOP, (AUTO_SCROLL_DELAY > 0 ? AUTO_SCROLL_DELAY : AUTO_SCROLL_DELAY_DEFAULT) - castTimeUtilLastScrollTime);
                        }
                    }
                    break;
            }
        }
    };


    public void setViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
        viewPager.addOnPageChangeListener(this);
    }

    public void startToFetchNativeAd() {
        if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            if (adCardView == null) {
                View view = LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ad_style_1, null);
                nativeAdView = new KCNativeAdView(HSApplication.getContext());
                nativeAdView.setAdLayoutView(view);
                nativeAdView.setOnAdLoadedListener(new KCNativeAdView.OnAdLoadedListener() {
                    @Override
                    public void onAdLoaded(KCNativeAdView nativeAdView) {
                        nativeAdView.setTag("NativeAd");
                        adCardView = ViewConvertor.toCardView(nativeAdView);
                        adCardView.setCardBackgroundColor(0xfff6f6f6);
                        scrollToPreAdPosition();
                    }
                });
                nativeAdView.load(HSApplication.getContext().getString(R.string.ad_placement_cardad));
            }
        }
    }

    private void removeNativeAdView() {
        if (adCardView != null) {
            nativeAdView.release();
            adCardView.removeAllViews();
            adCardView = null;

            int count = getRealCount();
            int currentItem = viewPager.getCurrentItem();
            keyboardThemeList.remove(null);
            notifyDataSetChanged();
            int newCurrent = currentItem + (currentItem % count - currentItem % (count - 1));
            viewPager.setCurrentItem(newCurrent, false);
        }
    }

    private final INotificationObserver notificationObserver = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (HSKeyboardThemeManager.HS_NOTIFICATION_THEME_LIST_CHANGED.equals(s)) {
                updateData();
                notifyDataSetChanged();
            } else if (ThemeHomeFragment.NOTIFICATION_THEME_HOME_DESTROY.equals(s)) {
                recycle();
            } else if (ThemeHomeFragment.NOTIFICATION_THEME_HOME_STOP.equals(s)) {
                stopAutoScroll();
            } else if (NOTIFICATION_REMOVEADS_PURCHASED.equals(s)) {
                // removeNativeAdView();
            }

        }
    };

    public void updateData() {
        LinkedList<HSKeyboardTheme> keyboardThemeArrayList = new LinkedList<>();
        // 获取所有的非自定义主题
        List<HSKeyboardTheme> allKeyboardThemesExceptCustomTheme = new ArrayList<>();
        allKeyboardThemesExceptCustomTheme.addAll(HSKeyboardThemeManager.getAllKeyboardThemeList());
        allKeyboardThemesExceptCustomTheme.removeAll(KCCustomThemeManager.getInstance().getAllCustomThemes());

        // 获取banner主题
        List<String> bannerThemeNames = (List<String>) HSConfig.getList("Application", "ThemeContents", "BannerList");
        for (String bannerThemeName : bannerThemeNames) {
            for (HSKeyboardTheme keyboardTheme : allKeyboardThemesExceptCustomTheme) {
                if (keyboardTheme.mThemeName.equals(bannerThemeName)) {
                    keyboardThemeArrayList.add(keyboardTheme);
                }
            }
        }

        LinkedList<HSKeyboardTheme> unDownloadBannerThemes = new LinkedList<>();

        Iterator<HSKeyboardTheme> iterator = keyboardThemeArrayList.iterator();
        while(iterator.hasNext()) {
            HSKeyboardTheme theme = iterator.next();
            if(!HSKeyboardThemeManager.getDownloadedThemeList().contains(theme)) {
                unDownloadBannerThemes.add(theme);
            }
        }

        int count = keyboardThemeList.size();
        if(unDownloadBannerThemes.size() > 0) {
            for(HSKeyboardTheme keyboardTheme : unDownloadBannerThemes) {
                if(!keyboardThemeList.contains(keyboardTheme)) {
                    keyboardThemeList.add(keyboardTheme);
                    break;
                }
            }

            Iterator<HSKeyboardTheme> iterator1 = keyboardThemeList.iterator();
            while(iterator1.hasNext() && keyboardThemeList.size() > count) {
                HSKeyboardTheme theme = iterator1.next();
                if(HSKeyboardThemeManager.getDownloadedThemeList().contains(theme)) {
                    iterator1.remove();
                }
            }
        }

        setData(keyboardThemeList.subList(0, count));
    }

    public void initData() {
        LinkedList<HSKeyboardTheme> keyboardThemeArrayList = new LinkedList<>();
        // 获取所有的非自定义主题
        List<HSKeyboardTheme> allKeyboardThemesExceptCustomTheme = new ArrayList<>();
        allKeyboardThemesExceptCustomTheme.addAll(HSKeyboardThemeManager.getAllKeyboardThemeList());
        allKeyboardThemesExceptCustomTheme.removeAll(KCCustomThemeManager.getInstance().getAllCustomThemes());

        // 获取banner主题
        List<String> bannerThemeNames = (List<String>) HSConfig.getList("Application", "ThemeContents", "BannerList");
        for (String bannerThemeName : bannerThemeNames) {
            for (HSKeyboardTheme keyboardTheme : allKeyboardThemesExceptCustomTheme) {
                if (keyboardTheme.mThemeName.equals(bannerThemeName)) {
                    keyboardThemeArrayList.add(keyboardTheme);
                }
            }
        }

        List<HSKeyboardTheme> result = new ArrayList<>();

        LinkedList<HSKeyboardTheme> unDownloadBannerThemes = new LinkedList<>();
        LinkedList<HSKeyboardTheme> downloadBannerThemes = new LinkedList<>();

        Iterator<HSKeyboardTheme> iterator = keyboardThemeArrayList.iterator();
        while(iterator.hasNext()) {
            HSKeyboardTheme theme = iterator.next();
            if(!HSKeyboardThemeManager.getDownloadedThemeList().contains(theme)) {
                unDownloadBannerThemes.add(theme);
            }
            else {
                downloadBannerThemes.add(theme);
            }
        }

        if(unDownloadBannerThemes.size() == 5) {
            result.addAll(unDownloadBannerThemes);
        }
        else if(unDownloadBannerThemes.size() < 5) {
            result.addAll(unDownloadBannerThemes);
            result.addAll(downloadBannerThemes.subList(0, 5 - result.size()));
        }
        else {
            List<String> indexShowCount = new ArrayList<>();
            int count = unDownloadBannerThemes.size();
            for (int i = 0; i < count; i++) {
                int showCount = HSPreferenceHelper.getDefault().getInt(unDownloadBannerThemes.get(i).mThemeName + "_show_count", 0);
                indexShowCount.add((showCount > 5 ? 5 : showCount) + "_" + i);
            }

            for (int i = 0; i < indexShowCount.size(); i++) {
                if (!indexShowCount.get(i).startsWith("5") && !indexShowCount.get(i).startsWith("0") && result.size() < 5) {
                    result.add(unDownloadBannerThemes.get(Integer.valueOf(indexShowCount.get(i).split("_")[1])));
                }
                if (result.size() == 5) {
                    break;
                }
            }

            if (result.size() < 5) {
                for (int i = 0; i < indexShowCount.size(); i++) {
                    if (indexShowCount.get(i).startsWith("0") && result.size() < 5) {
                        result.add(unDownloadBannerThemes.get(Integer.valueOf(indexShowCount.get(i).split("_")[1])));
                    }
                    if (result.size() == 5) {
                        break;
                    }
                }
            }

            if (result.size() < 5) {
                for (int i = 0; i < indexShowCount.size(); i++) {
                    if (indexShowCount.get(i).startsWith("5") && result.size() < 5) {
                        result.add(unDownloadBannerThemes.get(Integer.valueOf(indexShowCount.get(i).split("_")[1])));
                    }
                    if (result.size() == 5) {
                        break;
                    }
                }
            }
        }
        boolean allUnDownloadBannerThemeHasShowed = true;

        for(int i = 0; i < unDownloadBannerThemes.size(); i++) {
            if(HSPreferenceHelper.getDefault().getInt(unDownloadBannerThemes.get(i).mThemeName + "_show_count", 0) < 5) {
                allUnDownloadBannerThemeHasShowed = false;
                break;
            }
        }

        if(allUnDownloadBannerThemeHasShowed) {
            for (int i = 0; i < unDownloadBannerThemes.size(); i++) {
                HSKeyboardTheme theme = unDownloadBannerThemes.get(i);
                HSPreferenceHelper.getDefault().putInt(theme.mThemeName + "_show_count", 0);
            }
        }


        for(int i = 0; i < downloadBannerThemes.size(); i++) {
            HSKeyboardTheme theme = downloadBannerThemes.get(i);
            HSPreferenceHelper.getDefault().putInt(theme.mThemeName + "_show_count", 0);
        }

        setData(result);
    }

    public void setData(List<HSKeyboardTheme> keyboardThemeList) {
        this.keyboardThemeList = keyboardThemeList;
        if (keyboardThemeList != null) {
            if (keyboardThemeList.size() == 0) {
                viewPager.setVisibility(View.GONE);
                setLoop(false);
            } else {
                setLoop(true);
            }
        }
    }

    public ThemeBannerAdapter(Activity activity, int bannerWidth, int bannerHeight) {
        this.activity = activity;
        this.bannerWidth = bannerWidth;
        this.bannerHeight = bannerHeight;
        AUTO_SCROLL_DELAY = HSConfig.optInteger(AUTO_SCROLL_DELAY_DEFAULT, "Application", "ThemeContents", "themeConfig", "bannerAutoScrollDelay");
        HSGlobalNotificationCenter.addObserver(ThemeHomeFragment.NOTIFICATION_THEME_HOME_DESTROY, notificationObserver);
        HSGlobalNotificationCenter.addObserver(ThemeHomeFragment.NOTIFICATION_THEME_HOME_STOP, notificationObserver);
        HSGlobalNotificationCenter.addObserver(HSKeyboardThemeManager.HS_NOTIFICATION_THEME_LIST_CHANGED, notificationObserver);
        HSGlobalNotificationCenter.addObserver(NOTIFICATION_REMOVEADS_PURCHASED, notificationObserver);
    }

    private int getInitItem() {
        if (isInfinite && getRealCount() > 1) {
            return getRealCount() * loopMultiple / 2 -
                    (getRealCount() * loopMultiple / 2 % getRealCount());
        } else {
            return 0;
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return isLoop ? getRealCount() * loopMultiple : getRealCount();
    }

    public int getRealCount() {
        if (keyboardThemeList == null || keyboardThemeList.size() == 0) {
            return 0;
        }
        return keyboardThemeList.size();
    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // 如果当前是广告位,并且广告存在
        if (adCardView != null && keyboardThemeList.get(position % getRealCount()) == null) {
            if (adCardView.getParent() == container) {
                container.removeView(adCardView);
            }
            container.addView(adCardView);
            return adCardView;
        }

        int newPosition = position % getRealCount();
        ThemeBannerView view = (ThemeBannerView) View.inflate(container.getContext(), R.layout.item_theme_banner, null);

        if (keyboardThemeList.size() > 0) {
            final HSKeyboardTheme keyboardTheme = keyboardThemeList.get(newPosition);
            if (themeAnalyticsEnabled && !ThemeAnalyticsReporter.getInstance().isThemeReported(keyboardTheme.mThemeName)) {
                view.setUpOnScrollChangedListener();
                view.setTag(keyboardTheme.mThemeName);
            } else {
                view.shutDownOnScrollChangedListener();
            }
            final ImageView imageView = (ImageView) view.findViewById(R.id.theme_banner_image);
            if (keyboardTheme.getThemeBannerImgUrl() != null) {
                imageView.setImageResource(R.drawable.image_placeholder);
                ImageSize imageSize = new ImageSize(bannerWidth,bannerHeight);
                ImageLoader.getInstance().displayImage(keyboardTheme.getThemeBannerImgUrl(), new ImageViewAware(imageView), displayImageOptions,imageSize,null,null);

            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, ThemeDetailActivity.class);
                    intent.putExtra(ThemeDetailActivity.INTENT_KEY_THEME_NAME, keyboardTheme.mThemeName);
                    activity.startActivity(intent);
                    HSAnalytics.logEvent("store_banner_clicked", "themeName", keyboardTheme.mThemeName);
                    HSThemeNewTipController.getInstance().setThemeNotNew(keyboardTheme.mThemeName);
                    if (ThemeAnalyticsReporter.getInstance().isThemeAnalyticsEnabled()) {
                        ThemeAnalyticsReporter.getInstance().recordBannerThemeClick(keyboardTheme.mThemeName);
                    }
                }
            });
        }
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (object != adCardView) {
            container.removeView((View) object);
        }
    }


    public void startAutoScroll() {
        if (!isStartLoop && getRealCount() > 1 && isLoop) {
            isStartLoop = true;
            handler.removeMessages(MSG_WHAT_START);
            handler.sendEmptyMessage(MSG_WHAT_START);
            if (!hasInit) {
                hasInit = true;
                int initItem = getInitItem();
                viewPager.setCurrentItem(initItem);
            }
        }
    }

    public void stopAutoScroll() {
        if (isStartLoop) {
            isStartLoop = false;
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        lastScrollTime = System.currentTimeMillis();
    }

    @Override
    public void onPageSelected(int position) {
        // 记录当前item显示次数
        int newPosition = position % getRealCount();
        if (keyboardThemeList.get(newPosition) == null) { //当前位置为广告位
            return;
        }
        String themeName = keyboardThemeList.get(newPosition).mThemeName;
        int currentShowCount = HSPreferenceHelper.getDefault().getInt(themeName + "_show_count", 0);
        ++currentShowCount;
        HSPreferenceHelper.getDefault().putInt(themeName + "_show_count", currentShowCount);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    /**
     * @return the is Loop
     */
    public boolean isLoop() {
        return isLoop;
    }

    /**
     * @param isLoop the is InfiniteLoop to set
     */
    public void setLoop(boolean isLoop) {
        this.isLoop = isLoop;
    }

    public void recycle() {
        if (adCardView != null) {
            ((KCNativeAdView) adCardView.getChildAt(0)).release();
        }

        stopAutoScroll();
        handler.removeCallbacksAndMessages(null);

        viewPager.removeAllViews();

        HSGlobalNotificationCenter.removeObserver(notificationObserver);
    }

    public void scrollToPreAdPosition() {
        if (adCardView != null) {
            if (viewPager != null) {
                int count = getRealCount();
                int currentItem = viewPager.getCurrentItem();
                keyboardThemeList.add(currentItem % getRealCount() + 1, null);
                notifyDataSetChanged();
                int newCurrent = currentItem + (currentItem % count - currentItem % (count + 1));
                viewPager.setCurrentItem(newCurrent, false);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);

                    }
                }, 800);

            }
        }
    }

    public void setThemeAnalyticsEnabled(boolean isThemeAnalyticsEnabled) {
        this.themeAnalyticsEnabled = isThemeAnalyticsEnabled;
    }
}
