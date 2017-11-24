package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.NonNull;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.framework.Constants;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.emoji.HSEmojiPanel;
import com.ihs.inputmethod.uimodules.ui.emoticon.bean.ActionbarTab;
import com.ihs.inputmethod.uimodules.ui.textart.HSTextPanel;
import com.ihs.panelcontainer.BasePanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ihs.keyboardutils.iap.RemoveAdsManager.NOTIFICATION_REMOVEADS_PURCHASED;

/**
 * Created by wenbinduan on 2016/11/21.
 */

public final class ClipBoardActionBar extends LinearLayout implements View.OnClickListener {

    public final static String PANEL_RECENT = "Recent";
    public final static String PANEL_PIN = "Pins";

    private BasePanel.OnPanelActionListener containerListener;
    private BasePanel.OnPanelActionListener keyboardActionListener;
    private Map<Class, View> btnMap = new HashMap<>();
    private Map<String, Class> panels = new HashMap<>();
    boolean isCurrentThemeDarkBg = HSKeyboardThemeManager.getCurrentTheme().isDarkBg();
    private INotificationObserver notificationObserver = new INotificationObserver() {

        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (NOTIFICATION_REMOVEADS_PURCHASED.equals(s)) {
                View adContainer = findViewWithTag("NativeAd");
                removeView(adContainer);
            }
        }
    };

    public ClipBoardActionBar(Context context) {
        this(context, null);
    }

    public ClipBoardActionBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClipBoardActionBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        isCurrentThemeDarkBg = HSKeyboardThemeManager.getCurrentTheme().isDarkBg();
        HSGlobalNotificationCenter.addObserver(NOTIFICATION_REMOVEADS_PURCHASED, notificationObserver);
    }

    public void release() {
        HSGlobalNotificationCenter.removeObserver(notificationObserver);
        containerListener = null;
    }

//    public static void saveLastPanelName(String panelName) {
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext());
//        sp.edit().putString("emoticon_last_show_panel_name", panelName).apply();
//    }
//
//    public static String getLastPanelName() {
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext());
//        return sp.getString("emoticon_last_show_panel_name", PANEL_RECENT);
//    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        List<ActionbarTab> actionbarTabs = new ArrayList<>();
        actionbarTabs.add(new ActionbarTab(PANEL_RECENT, HSEmojiPanel.class, R.drawable.ic_emoji_panel_tab));
        actionbarTabs.add(new ActionbarTab(PANEL_PIN, HSTextPanel.class, R.drawable.ic_text_panel_tab));

        final int height = getResources().getDimensionPixelSize(R.dimen.emoticon_panel_actionbar_height);
        final int actionBarAmount = actionbarTabs.size();
        for (int i = 0; i < actionBarAmount; i++) {
            final String panelName = actionbarTabs.get(i).panelName;
            final Class<?> clazz = actionbarTabs.get(i).panelClass;
            final LayoutParams params = new LayoutParams(0, height, 1.0f);

            RelativeLayout wrapView = (RelativeLayout) inflate(getContext(), R.layout.clipboard_bar_button,null);
            setBtnImage(wrapView.findViewById(R.id.iv_icon),actionbarTabs.get(i).iconResId);
            ((TextView)wrapView.findViewById(R.id.tv_title)).setText(panelName);
            wrapView.setTag(panelName);
            wrapView.setOnClickListener(this);
            wrapView.setBackgroundDrawable(getBackgroundDrawable());

            addView(wrapView, params);
            panels.put(panelName, clazz);
            btnMap.put(clazz, wrapView);
        }
    }


    private void setBtnImage(ImageView btn, int iconResId) {
        Drawable tabbarBtnDrawable = getTabDrawable(iconResId);
        btn.setScaleType(ImageView.ScaleType.CENTER);
        btn.setImageDrawable(tabbarBtnDrawable);
        btn.setSoundEffectsEnabled(false);
    }

    @NonNull
    private Drawable getTabDrawable(int resId) {
        Resources resources = HSApplication.getContext().getResources();
        int pressColor = isCurrentThemeDarkBg ? resources.getColor(R.color.emoji_panel_tab_selected_color_when_theme_dark_bg) : resources.getColor(R.color.emoji_panel_tab_selected_color);
        int normalColor = isCurrentThemeDarkBg ? resources.getColor(R.color.emoji_panel_tab_normal_color_when_theme_dark_bg) : resources.getColor(R.color.emoji_panel_tab_normal_color);

        Drawable tabbarBtnDrawable = VectorDrawableCompat.create(resources, resId, null);
        DrawableCompat.setTintList(tabbarBtnDrawable, new ColorStateList(
                new int[][]
                        {
                                new int[]{android.R.attr.state_focused},
                                new int[]{android.R.attr.state_pressed},
                                new int[]{android.R.attr.state_selected},
                                new int[]{}
                        },
                new int[]
                        {
                                pressColor,
                                pressColor,
                                pressColor,
                                normalColor,
                        }
        ));
        return tabbarBtnDrawable;
    }

    @NonNull
    private StateListDrawable getBackgroundDrawable() {
        StateListDrawable background = new StateListDrawable();
        Drawable bg = new ColorDrawable(Color.TRANSPARENT);
        Drawable pressedBg = new ColorDrawable(Color.parseColor("#1AFFFFFF"));

        background.addState(new int[]{android.R.attr.state_focused}, pressedBg);
        background.addState(new int[]{android.R.attr.state_pressed}, pressedBg);
        background.addState(new int[]{android.R.attr.state_selected}, pressedBg);
        background.addState(new int[]{}, bg);
        return background;
    }

    void setContainerListener(BasePanel.OnPanelActionListener onStateChangedListener) {
        this.containerListener = onStateChangedListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final Resources res = getContext().getResources();
        final int width = res.getDisplayMetrics().widthPixels;
        final int height = res.getDimensionPixelSize(R.dimen.emoticon_panel_actionbar_height);
        setMeasuredDimension(width, height);
    }

    @Override
    public void onClick(View v) {
        final Object tag = v.getTag();
        if (tag != null && tag instanceof String) {
            Class panel = panels.get(tag);
            if (containerListener != null && panel != null) {
                containerListener.showPanel(panel);
                HSAnalytics.logEvent("keyboard_emoji_tab_switch", "tagContent", tag.toString());
            }
        } else {
            if (tag instanceof Integer && (Integer) tag == Constants.CODE_DELETE) {
                HSInputMethod.deleteBackward();
            }
        }
    }

    void selectPanelBtn(Class clazz) {
        for (Class claz : panels.values()) {
            View view = btnMap.get(claz);
            if (view != null) {
                view.setSelected(false);
            }
        }

        View view = btnMap.get(clazz);
        if (view != null) {
            view.setSelected(true);
        }
    }

    Class<?> getPanelClass(final String panel) {
        return panels.get(panel);
    }

    public void setKeyboardPanelActionListener(BasePanel.OnPanelActionListener panelActionListener) {
        this.keyboardActionListener = panelActionListener;
    }


}
