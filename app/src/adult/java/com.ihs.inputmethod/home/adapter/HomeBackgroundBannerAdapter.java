package com.ihs.inputmethod.home.adapter;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.home.HomeActivity;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity;
import com.ihs.inputmethod.widget.HomeBackgroundBannerView;
import com.keyboard.core.mediacontroller.listeners.DownloadStatusListener;
import com.keyboard.core.themes.custom.KCCustomThemeManager;
import com.keyboard.core.themes.custom.elements.KCBackgroundElement;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HomeBackgroundBannerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {
    private final static int AUTO_SCROLL_DELAY_DEFAULT = 6000;
    private final static int MSG_WHAT_START = 1;
    private final static int MSG_WHAT_LOOP = 2;
    private static int AUTO_SCROLL_DELAY;

    private ViewPager viewPager;
    private List<KCBackgroundElement> backgroundList = new ArrayList<>();
    private boolean isStartLoop = false;
    private boolean isLoop = true;
    private boolean isInfinite = true;
    private boolean hasInit = false;
    private long lastScrollTime = 0;
    private final static int loopMultiple = 500;

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

    private final INotificationObserver notificationObserver = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (HSKeyboardThemeManager.HS_NOTIFICATION_THEME_LIST_CHANGED.equals(s)) {
                updateData();
                notifyDataSetChanged();
            } else if (ThemeHomeFragment.NOTIFICATION_THEME_HOME_STOP.equals(s)) {
                stopAutoScroll();
            }
        }
    };

    public void updateData() {
        setData(KCCustomThemeManager.getInstance().getBackgroundHomeElements().subList(0,3));
    }

    public void initData() {
        setData(KCCustomThemeManager.getInstance().getBackgroundHomeElements().subList(0,3));
    }

    public void setData(List<KCBackgroundElement> backgroundElementList) {
        this.backgroundList = backgroundElementList;
        if (backgroundList != null) {
            if (backgroundList.size() == 0) {
                viewPager.setVisibility(View.GONE);
                setLoop(false);
            } else {
                setLoop(true);
            }
        }
    }

    public HomeBackgroundBannerAdapter() {
        AUTO_SCROLL_DELAY = HSConfig.optInteger(AUTO_SCROLL_DELAY_DEFAULT, "Application", "KeyboardTheme", "ThemeContents", "themeConfig", "bannerAutoScrollDelay");
        HSGlobalNotificationCenter.addObserver(HomeActivity.NOTIFICATION_HOME_DESTROY, notificationObserver);
        HSGlobalNotificationCenter.addObserver(HSKeyboardThemeManager.HS_NOTIFICATION_THEME_LIST_CHANGED, notificationObserver);
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
        if (backgroundList == null || backgroundList.size() == 0) {
            return 0;
        }
        return backgroundList.size();
    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        int newPosition = position % getRealCount();
        HomeBackgroundBannerView view = (HomeBackgroundBannerView) View.inflate(container.getContext(), R.layout.item_home_background_banner, null);

        if (backgroundList.size() > 0) {
            final KCBackgroundElement kcBackgroundElement = backgroundList.get(newPosition);
            final ImageView imageView = view.findViewById(R.id.theme_banner_image);

            imageView.setImageResource(R.drawable.image_placeholder);
            if (kcBackgroundElement.hasLocalContent()) {
                Glide.with(HSApplication.getContext()).load(kcBackgroundElement.getKeyboardImageContentPath()).into(imageView);
            }else {
                KCCustomThemeManager.getInstance().downloadElementResource(kcBackgroundElement, new DownloadStatusListener() {
                    @Override
                    public void onDownloadProgress(File file, float percent) {

                    }

                    @Override
                    public void onDownloadSucceeded(File file) {
                        Glide.with(HSApplication.getContext()).load(kcBackgroundElement.getKeyboardImageContentPath()).into(imageView);
                    }

                    @Override
                    public void onDownloadFailed(File file) {

                    }
                }, false /* isPreview */);
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    String customEntry = "store_bg";
                    String backgroundItemName = kcBackgroundElement.getName();
                    bundle.putString(CustomThemeActivity.BUNDLE_KEY_BACKGROUND_NAME, backgroundItemName);
                    bundle.putString(CustomThemeActivity.BUNDLE_KEY_CUSTOMIZE_ENTRY, customEntry);
                    Intent intent = new Intent(HSApplication.getContext(), CustomThemeActivity.class);
                    intent.putExtras(bundle);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    HSApplication.getContext().startActivity(intent);
                }
            });
        }
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
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
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    /**
     * @param isLoop the is InfiniteLoop to set
     */
    public void setLoop(boolean isLoop) {
        this.isLoop = isLoop;
    }

    public void recycle() {
        stopAutoScroll();
        handler.removeCallbacksAndMessages(null);

        viewPager.removeAllViews();

        HSGlobalNotificationCenter.removeObserver(notificationObserver);
    }
}
