package com.ihs.inputmethod.uimodules.ui.emoticon;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kc.utils.KCAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.framework.HSInputMethodService;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.framework.Constants;
import com.ihs.inputmethod.uimodules.BuildConfig;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.listeners.DeleteKeyOnTouchListener;
import com.ihs.inputmethod.uimodules.ui.emoji.HSEmojiPanel;
import com.ihs.inputmethod.uimodules.ui.emoticon.bean.ActionbarTab;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.ui.GifPanel;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerPanel;
import com.ihs.inputmethod.uimodules.ui.textart.HSTextPanel;
import com.ihs.panelcontainer.BasePanel;
import com.ihs.panelcontainer.panel.KeyboardPanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ihs.keyboardutils.iap.RemoveAdsManager.NOTIFICATION_REMOVEADS_PURCHASED;

/**
 * Created by wenbinduan on 2016/11/21.
 */

public final class HSEmoticonActionBar extends LinearLayout implements View.OnClickListener {

    public final static String PANEL_EMOJI = "emoji";
    public final static String PANEL_STICKER = "sticker";
    public final static String PANEL_FACEEMOJI = "facemoji";
    public final static String PANEL_GIF = "gif";
    public final static String PANEL_TEXT = "text";

    private BasePanel.OnPanelActionListener containerListener;
    private BasePanel.OnPanelActionListener keyboardActionListener;
    private TextView alphabet_left;
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

    public HSEmoticonActionBar(Context context) {
        this(context, null);
    }

    public HSEmoticonActionBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HSEmoticonActionBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        isCurrentThemeDarkBg = HSKeyboardThemeManager.getCurrentTheme().isDarkBg();
        HSGlobalNotificationCenter.addObserver(NOTIFICATION_REMOVEADS_PURCHASED, notificationObserver);
    }

    public void release() {
        HSGlobalNotificationCenter.removeObserver(notificationObserver);
        containerListener = null;
    }

    public static void saveLastPanelName(String panelName) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext());
        sp.edit().putString("emoticon_last_show_panel_name", panelName).apply();
    }

    public static String getLastPanelName() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext());
        return sp.getString("emoticon_last_show_panel_name", PANEL_EMOJI);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        List<ActionbarTab> actionbarTabs = new ArrayList<>();
        actionbarTabs.add(new ActionbarTab(PANEL_EMOJI, HSEmojiPanel.class, R.drawable.ic_emoji_panel_tab));
        actionbarTabs.add(new ActionbarTab(PANEL_STICKER, StickerPanel.class, R.drawable.ic_sticker_panel_tab));
        if (BuildConfig.ENABLE_FACEMOJI) {
            try {
                actionbarTabs.add(new ActionbarTab(PANEL_FACEEMOJI,Class.forName("com.ihs.inputmethod.uimodules.ui.facemoji.HSFacemojiPanel"), R.drawable.ic_facemoji_panel_tab));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("com.ihs.inputmethod.uimodules.ui.facemoji.HSFacemojiPanel not find!");
            }
        }
        actionbarTabs.add(new ActionbarTab(PANEL_GIF, GifPanel.class, R.drawable.ic_gif_panel_tab));
        actionbarTabs.add(new ActionbarTab(PANEL_TEXT, HSTextPanel.class, R.drawable.ic_text_panel_tab));

        final int height = getResources().getDimensionPixelSize(R.dimen.emoticon_panel_actionbar_height);
        final int actionBarAmount = actionbarTabs.size();
        for (int i = 0; i < actionBarAmount; i++) {
            final String panelName = actionbarTabs.get(i).panelName;
            final Class<?> clazz = actionbarTabs.get(i).panelClass;
            final ImageView btn = getBtnImage(actionbarTabs.get(i).iconResId);
            final LayoutParams params = new LayoutParams(0, height, 1.0f);
            final int actionBarWidth = (getContext().getResources().getDisplayMetrics().widthPixels - HSDisplayUtils.dip2px(56) * 2) / actionBarAmount;

            FrameLayout wrapLayout = new FrameLayout(getContext());
            params.gravity = Gravity.CENTER_VERTICAL;
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, height);
            layoutParams.gravity = Gravity.CENTER;
            wrapLayout.addView(btn, layoutParams);
            btn.setTag(panelName);
            btn.setOnClickListener(this);

            if (TextUtils.equals(panelName, "sticker") &&
                    HSInputMethodService.isShowNewMask()) {
                View newTipView = new View(getContext());
                GradientDrawable redPointDrawable = new GradientDrawable();
                redPointDrawable.setColor(Color.RED);
                redPointDrawable.setShape(GradientDrawable.OVAL);
                newTipView.setBackgroundDrawable(redPointDrawable);

                int width = HSDisplayUtils.dip2px(5);
                int dotHeight = HSDisplayUtils.dip2px(5);
                layoutParams = new FrameLayout.LayoutParams(width, dotHeight);
                layoutParams.topMargin = height * 4 / 15;
                layoutParams.leftMargin = actionBarWidth / 2 + height / 5;
                wrapLayout.addView(newTipView, layoutParams);
            }

            addView(wrapLayout, params);

            panels.put(panelName, clazz);
            btnMap.put(clazz, btn);
        }

        alphabet_left = (TextView) findViewById(R.id.emoji_keyboard_alphabet_left);
        alphabet_left.setOnClickListener(this);
        alphabet_left.setTextColor(HSKeyboardThemeManager.getCurrentTheme().getFuncKeyTextColor());
        alphabet_left.setTextSize(TypedValue.COMPLEX_UNIT_PX, HSKeyboardThemeManager.getCurrentTheme().getFuncKeyLabelSize());
        alphabet_left.setText(HSInputMethod.getSwitchToAlphaKeyLabel());
        alphabet_left.setBackgroundDrawable(getBackgroundDrawable());
        initDeleteKey(height);
    }

    private void initDeleteKey(final int height) {
        final ImageView deleteKey = new ImageView(getContext());
        deleteKey.setScaleType(ImageView.ScaleType.CENTER);
        deleteKey.setTag(Constants.CODE_DELETE);
        deleteKey.setImageDrawable(getTabDrawable(R.drawable.ic_delete_panel_tab));
        deleteKey.setOnTouchListener(new DeleteKeyOnTouchListener(getContext()));
        // 56dp fixed
        final LayoutParams params = new LayoutParams(HSDisplayUtils.dip2px(56), height);
        params.gravity = Gravity.CENTER_VERTICAL;
        deleteKey.setSoundEffectsEnabled(false);
        addView(deleteKey, params);
    }

    private ImageView getBtnImage(int iconResId) {
        Drawable tabbarBtnDrawable = getTabDrawable(iconResId);
        ImageView tabbarBtn = new ImageView(HSApplication.getContext());
        tabbarBtn.setScaleType(ImageView.ScaleType.CENTER);
        tabbarBtn.setImageDrawable(tabbarBtnDrawable);

        tabbarBtn.setBackgroundDrawable(getBackgroundDrawable());
        tabbarBtn.setSoundEffectsEnabled(false);
        return tabbarBtn;
    }

    @NonNull
    private Drawable getTabDrawable(int resId) {
        Resources resources = HSApplication.getContext().getResources();
        int pressColor = isCurrentThemeDarkBg ? resources.getColor(R.color.emoji_panel_tab_selected_color_when_theme_dark_bg) : resources.getColor(R.color.emoji_panel_tab_selected_color);
        int normalColor = isCurrentThemeDarkBg ? resources.getColor(R.color.emoji_panel_tab_normal_color_when_theme_dark_bg) : resources.getColor(R.color.emoji_panel_tab_normal_color);

        Drawable tabbarBtnDrawable = VectorDrawableCompat.create(resources,resId, null);
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
                KCAnalytics.logEvent("keyboard_emoji_tab_switch", "tagContent", tag.toString());
            }
        } else {
            if (v.getId() == alphabet_left.getId() && keyboardActionListener != null) {
                keyboardActionListener.showPanel(KeyboardPanel.class);
                keyboardActionListener.setBarVisibility(VISIBLE);
                keyboardActionListener = null;
            } else if (tag instanceof Integer && (Integer) tag == Constants.CODE_DELETE) {
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
