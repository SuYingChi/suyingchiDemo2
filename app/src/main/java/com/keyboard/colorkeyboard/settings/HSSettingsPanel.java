package com.keyboard.colorkeyboard.settings;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.panel.HSInputMethodPanel;
import com.ihs.inputmethod.uimodules.ui.fonts.common.HSFontSelectViewAdapter;
import com.ihs.inputmethod.uimodules.ui.keyboard.KeyboardPanel;
import com.keyboard.colorkeyboard.KeyboardPanelManager;
import com.keyboard.colorkeyboard.MasterKeyboardPluginManager;
import com.keyboard.colorkeyboard.R;
import com.viewpagerindicator.IconPageIndicator;

import java.util.ArrayList;
import java.util.List;

public class HSSettingsPanel extends HSInputMethodPanel {
    private View settingPanelView;
    int animDuration = 300;

    public static final String PANEL_NAME_THEME = "settings";
    private Context mContext;


    public HSSettingsPanel() {
        super(PANEL_NAME_THEME);
        mContext = HSApplication.getContext();
        HSGlobalNotificationCenter.addObserver(HSFontSelectViewAdapter.HS_NOTIFICATION_FONT_CHANGED, observer);
        HSGlobalNotificationCenter.addObserver(HSKeyboardThemeManager.HS_NOTIFICATION_THEME_CHANGED, observer);
        HSGlobalNotificationCenter.addObserver(HSInputMethod.HS_NOTIFICATION_HIDE_WINDOW,observer);
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public View onCreatePanelView() {

        View view = View.inflate(getContext(), R.layout.panel_settings, null);
        SettingsViewPager settingsViewPager = (SettingsViewPager) view.findViewById(R.id.settingsViewPager);
        IconPageIndicator iconIndicator = (IconPageIndicator) view.findViewById(R.id.iconIndicator);


        List<ViewItem> items = prepareItems();
        settingsViewPager.setItems(items);
        //TODO:add NativeAds
//        NativeAdsHelper.getHelper().onCreateAdView();

        iconIndicator.setViewPager(settingsViewPager);


        view.setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
        settingPanelView = view;
        return view;
    }

    private List<ViewItem> prepareItems() {
        List<ViewItem> items = new ArrayList<>();

        items.add(ViewItemBuilder.getThemesItem());
        items.add(ViewItemBuilder.getFontsItem());
        items.add(ViewItemBuilder.getSoundsItem());
        items.add(ViewItemBuilder.getAutoCorrectionItem());
        items.add(ViewItemBuilder.getAutoCapitalizationItem());
        items.add(ViewItemBuilder.getPredicationItem());
        items.add(ViewItemBuilder.getSwipeItem());
        items.add(ViewItemBuilder.getAdsItem());
        items.add(ViewItemBuilder.getLanguageItem());
        items.add(ViewItemBuilder.getMoreSettingsItem());

        return items;
    }

    @Override
    protected Drawable getNormalDrawable() {
        return getContext().getResources().getDrawable(R.drawable.menu_setting);
    }

    @Override
    protected Drawable getPressedDrawable() {
        return getContext().getResources().getDrawable(R.drawable.menu_setting);
    }


    @Override
    public void setTabbarBtnState(boolean pressed) {
        super.setTabbarBtnState(pressed);
        int animDuration = 300;
        if (getPanelView() != null) {
            if (pressed) {
                expandOrCollapse(settingPanelView, true, animDuration);
            } else {
                expandOrCollapse(settingPanelView, false, animDuration);
            }
        }
    }


    private void expandOrCollapse(final View v, final boolean isExpand, final long animDuration) {
        if (v == null) {
            return;
        }
        float keyboardHeight = HSResourceUtils.getDefaultKeyboardHeight(HSApplication.getContext().getResources());
        ValueAnimator animator = isExpand ? ValueAnimator.ofFloat(0, keyboardHeight) : ValueAnimator.ofFloat(keyboardHeight, 0);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(animDuration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                float val = (float) animation.getAnimatedValue();
                setViewHeight(v, (int) val);
            }
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isExpand) {
                    v.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    public static void setViewHeight(View v, int height) {
        if (v != null && v.getLayoutParams() != null) {
            final ViewGroup.LayoutParams params = v.getLayoutParams();
            params.height = height;
            v.requestLayout();
        }
    }

    @Override
    public void onDestroyPanelView() {
        super.onDestroyPanelView();

        HSGlobalNotificationCenter.removeObserver(observer);

    }

    @Override
    public void onShowPanelView() {
        super.onShowPanelView();

//        NativeAdsHelper.getHelper().onShowAdView();
        if (getPanelView() != null) {
            getPanelView().invalidate();
        }
    }


    INotificationObserver observer = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (HSKeyboardThemeManager.HS_NOTIFICATION_THEME_CHANGED.equals(s)) {
//                mPanelView = null;
                MasterKeyboardPluginManager.getInstance().resetActionButtonState();
                KeyboardPanelManager.getInstance().getKeyboardPanelSwitchContainer().showPanel(KeyboardPanel.class);
            }else if(HSInputMethod.HS_NOTIFICATION_HIDE_WINDOW.equals(s) || HSFontSelectViewAdapter.HS_NOTIFICATION_FONT_CHANGED.equals(s)){
                MasterKeyboardPluginManager.getInstance().resetActionButtonState();
            }
        }
    };

    @Override
    public Animator getAppearAnimator() {
        return showPanelAnimator(true);
    }

    @Override
    public Animator getDismissAnimator() {
        return  showPanelAnimator(false);
    }

    @NonNull
    private ValueAnimator showPanelAnimator(final boolean appear) {
        float keyboardHeight = HSResourceUtils.getDefaultKeyboardHeight(HSApplication.getContext().getResources());
        ValueAnimator animator = appear ? ValueAnimator.ofFloat(0, keyboardHeight) : ValueAnimator.ofFloat(keyboardHeight, 0);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(animDuration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                float val = (float) animation.getAnimatedValue();
                setViewHeight(settingPanelView, (int) val);
            }
        });

//        animator.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//                getPanelView().setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                if (!appear) {
//                    getPanelView().setVisibility(View.GONE);
//                }
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        });
        return animator;
    }
}