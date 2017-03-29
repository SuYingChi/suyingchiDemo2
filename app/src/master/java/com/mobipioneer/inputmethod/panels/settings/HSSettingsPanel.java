package com.mobipioneer.inputmethod.panels.settings;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.fonts.common.HSFontSelectViewAdapter;
import com.ihs.panelcontainer.BasePanel;
import com.mobipioneer.inputmethod.panels.settings.model.ViewItem;
import com.mobipioneer.inputmethod.panels.settings.model.ViewItemBuilder;
import com.mobipioneer.inputmethod.panels.settings.widget.SettingsViewPager;
import com.viewpagerindicator.IconPageIndicator;

import java.util.ArrayList;
import java.util.List;

public class HSSettingsPanel extends BasePanel {


    public static final String PANEL_NAME_THEME = "settings";
    private Context mContext;


    public HSSettingsPanel() {
        super();
        mContext = HSApplication.getContext();
        HSGlobalNotificationCenter.addObserver(HSFontSelectViewAdapter.HS_NOTIFICATION_FONT_CHANGED, observer);
        HSGlobalNotificationCenter.addObserver(HSKeyboardThemeManager.HS_NOTIFICATION_THEME_CHANGED, observer);
//        HSGlobalNotificationCenter.addObserver(HSKeyboardThemeManager.HS_NOTIFICATION_HIDE_WINDOW_EVENT,observer);
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public View onCreatePanelView() {

//        LinearLayout linearLayout = new LinearLayout(getContext());
//        linearLayout.setOrientation(LinearLayout.VERTICAL);
//
//
//        SettingsViewPager pager = new SettingsViewPager(getContext());
//        List<ViewItem> items = prepareItems();
//        pager.setItems(items);
//
//        NativeAdsHelper.getHelper().onCreateAdView();
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
//        layoutParams.weight = 1;
//        pager.setLayoutParams(layoutParams);
//        linearLayout.addView(pager);
//
//        IconPageIndicator iconPageIndicator = new IconPageIndicator(getContext());
//        iconPageIndicator.setViewPager(pager);
//        LinearLayout.LayoutParams indicatorLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        indicatorLayoutParams.topMargin = 15;
//        indicatorLayoutParams.bottomMargin = 15;
//        iconPageIndicator.setLayoutParams(indicatorLayoutParams);
//        linearLayout.addView(iconPageIndicator);
//
//
//        linearLayout.setBackgroundColor(Color.LTGRAY);
//
//        return linearLayout;


//        FrameLayout frameLayout = new FrameLayout(getContext());
//
//        SettingsViewPager pager = new SettingsViewPager(getContext());
//        List<ViewItem> items = prepareItems();
//        pager.setItems(items);
//
//        NativeAdsHelper.getHelper().onCreateAdView();
//        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
//        layoutParams.height = ResourceUtils.getDefaultKeyboardHeight(getContext().getResources());
//        pager.setLayoutParams(layoutParams);
//        frameLayout.addView(pager);
//
//        IconPageIndicator iconPageIndicator = new IconPageIndicator(getContext());
//        iconPageIndicator.setViewPager(pager);
//        FrameLayout.LayoutParams indicatorLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        indicatorLayoutParams.topMargin = 15;
//        indicatorLayoutParams.bottomMargin = 15;
//        indicatorLayoutParams.gravity = Gravity.BOTTOM;
//        iconPageIndicator.setLayoutParams(indicatorLayoutParams);
//        frameLayout.addView(iconPageIndicator);
//
//
//        frameLayout.setBackgroundColor(Color.LTGRAY);
//
//        return frameLayout;

        View view = View.inflate(getContext(), R.layout.panel_settings, null);
        SettingsViewPager settingsViewPager = (SettingsViewPager) view.findViewById(R.id.settingsViewPager);
        IconPageIndicator iconIndicator = (IconPageIndicator) view.findViewById(R.id.iconIndicator);


        List<ViewItem> items = prepareItems();
        settingsViewPager.setItems(items);
        NativeAdsHelper.getHelper().onCreateAdView();

        iconIndicator.setViewPager(settingsViewPager);


        view.setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
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

//    @Override
//    protected Drawable getNormalDrawable() {
//        return getContext().getResources().getDrawable(R.drawable.menu_setting);
//    }
//
//    @Override
//    protected Drawable getPressedDrawable() {
//        return getContext().getResources().getDrawable(R.drawable.menu_setting);
//    }
//
//
//    @Override
//    public void setTabbarBtnState(boolean pressed) {
//        super.setTabbarBtnState(pressed);
//        int animDuration = 300;
//        if (getPanelView() != null) {
//            if (pressed) {
//                expandOrCollapse(getPanelView(), true, animDuration);
//            } else {
//                expandOrCollapse(getPanelView(), false, animDuration);
//            }
//        }
//    }


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
    protected void onDestroy() {
        super.onDestroy();
        HSGlobalNotificationCenter.removeObserver(observer);
    }

//    @Override
//    public void onShowPanelView() {
//        super.onShowPanelView();
//
//        NativeAdsHelper.getHelper().onShowAdView();
//        if (getPanelView() != null) {
//            getPanelView().invalidate();
//        }
//    }


    INotificationObserver observer = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
//            if (HSKeyboardThemeManager.HS_NOTIFICATION_THEME_CHANGED.equals(s)) {
//                mPanelView = null;
//            }else if(HSInputMethod.HS_NOTIFICATION_HIDE_WINDOW_EVENT.equals(s)||HSInputMethod.HS_NOTIFICATION_FONT_CHANGED.equals(s)){
//                MasterKeyboardPluginManager.getInstance().resetActionButtonState();
//            }
        }
    };
}
