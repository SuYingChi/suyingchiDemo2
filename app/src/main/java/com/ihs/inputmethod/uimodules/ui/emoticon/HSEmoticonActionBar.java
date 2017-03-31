package com.ihs.inputmethod.uimodules.ui.emoticon;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.framework.Constants;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.listeners.DeleteKeyOnTouchListener;
import com.ihs.inputmethod.uimodules.ui.emoji.HSEmojiPanel;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.ui.GifPanel;
import com.ihs.inputmethod.uimodules.ui.textart.HSTextPanel;
import com.ihs.inputmethod.uimodules.ui.theme.iap.IAPManager;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeFragment;
import com.ihs.keyboardutils.nativeads.NativeAdParams;
import com.ihs.keyboardutils.nativeads.NativeAdView;
import com.ihs.panelcontainer.BasePanel;
import com.ihs.panelcontainer.panel.KeyboardPanel;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wenbinduan on 2016/11/21.
 */

public final class HSEmoticonActionBar extends LinearLayout implements View.OnClickListener {

    private BasePanel.OnPanelActionListener containerListener;
    private BasePanel.OnPanelActionListener keyboardActionListener;
    private TextView alphabet_left;
    private Map<Class, View> btnMap = new HashMap<>();
    private Map<String, Class> panels = new HashMap<>();
    private NativeAdView adView;

    public HSEmoticonActionBar(Context context) {
        this(context, null);
    }

    public HSEmoticonActionBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HSEmoticonActionBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        HSGlobalNotificationCenter.addObserver(ThemeHomeFragment.NOTIFICATION_REMOVEADS_PURCHASED, notificationObserver);
    }
    private INotificationObserver notificationObserver = new INotificationObserver() {

        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if(ThemeHomeFragment.NOTIFICATION_REMOVEADS_PURCHASED.equals(s)) {
                View adContainer = findViewWithTag("NativeAd");
                removeView(adContainer);
            }
        }
    };

    public void release() {
        if (adView != null) {
            HSGlobalNotificationCenter.removeObserver(notificationObserver);
            adView.release();
            adView = null;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        final String[] panelNames = {
                "emoji",
                "gif",
                "text"
        };
        final Class[] panelClassNames = {
                HSEmojiPanel.class,
                GifPanel.class,
                HSTextPanel.class

        };
        final int height = getResources().getDimensionPixelSize(R.dimen.emoticon_panel_actionbar_height);
        for (int i = 0; i < panelNames.length; i++) {
            final String panelName = panelNames[i];
            final Class<?> clazz = panelClassNames[i];
            final ImageView btn = getBtnImage(panelName);
            final LayoutParams params = new LayoutParams(0, height, 1.0f);
            params.gravity = Gravity.CENTER_VERTICAL;
            addView(btn, params);
            btn.setTag(panelName);
            btn.setOnClickListener(this);
            panels.put(panelName, clazz);
            btnMap.put(clazz, btn);
        }


        if(!IAPManager.getManager().hasPurchaseNoAds()) {
            boolean showIconAd = HSConfig.getBoolean("Application", "NativeAds", "ShowIconAd");
            View adLayoutView = View.inflate(getContext(), R.layout.ad_icon_style, null);
            if(!showIconAd) {
                adLayoutView.findViewById(R.id.ad_call_to_action).setVisibility(GONE);
            }
            View adLoadingView = View.inflate(getContext(), R.layout.ad_icon_style_loading, null);
            if(!showIconAd) {
                adLoadingView.findViewById(R.id.ad_call_to_action).setVisibility(GONE);
            }
            ImageView loadingImageView = (ImageView) adLoadingView.findViewById(R.id.ad_loading_image);
            Drawable drawable = HSKeyboardThemeManager.getCurrentTheme().getStyledDrawableFromResources("ic_gift");
            loadingImageView.setImageDrawable(drawable);
            final NativeAdView adView = new NativeAdView(getContext(), adLayoutView, adLoadingView);
            adView.setNativeAdType(NativeAdView.NativeAdType.ICON);
            adView.configParams(new NativeAdParams(getContext().getString(R.string.ad_placement_keyboardemojiad)));
            final LayoutParams params = new LayoutParams(0, height, 1.0f);
            params.gravity = Gravity.CENTER_VERTICAL;
            RelativeLayout adContainer = new RelativeLayout(getContext());
            adContainer.setTag("NativeAd");
            RelativeLayout.LayoutParams adLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            adLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            adContainer.addView(adView, adLayoutParams);
            addView(adContainer, params);
        }

        alphabet_left = (TextView) findViewById(R.id.emoji_keyboard_alphabet_left);
        alphabet_left.setOnClickListener(this);
        alphabet_left.setTextColor(HSKeyboardThemeManager.getCurrentTheme().getFuncKeyTextColor());
        alphabet_left.setTextSize(TypedValue.COMPLEX_UNIT_PX, HSKeyboardThemeManager.getCurrentTheme().getFuncKeyLabelSize());
//        alphabet_left.setTypeface(HSKeyboardThemeManager.getCurrentTheme().getTextTypeface());
        alphabet_left.setText(HSInputMethod.getSwitchToAlphaKeyLabel());
        alphabet_left.setBackgroundDrawable(getBackgroundDrawable());
        initDeleteKey(height);
    }

    private void initDeleteKey(final int height) {
        final StateListDrawable deleteKeyDrawable = new StateListDrawable();
        Drawable deleteDrawable = HSKeyboardThemeManager.getCurrentTheme().getStyledDrawableFromResources("emoji_delete");
        Drawable deleteHL = HSKeyboardThemeManager.getCurrentTheme().getStyledDrawableFromResources("emoji_delete_hl");
        deleteKeyDrawable.addState(new int[]{android.R.attr.state_pressed}, deleteHL);
        deleteKeyDrawable.addState(new int[]{android.R.attr.state_focused}, deleteHL);
        deleteKeyDrawable.addState(new int[]{android.R.attr.state_selected}, deleteHL);
        deleteKeyDrawable.addState(new int[]{}, deleteDrawable);

        final ImageView deleteKey = new ImageView(getContext());
        deleteKey.setScaleType(ImageView.ScaleType.CENTER);
        deleteKey.setTag(Constants.CODE_DELETE);
        deleteKey.setImageDrawable(deleteKeyDrawable);
        deleteKey.setOnTouchListener(new DeleteKeyOnTouchListener(getContext()));
        // 56dp fixed
        final LayoutParams params = new LayoutParams(HSDisplayUtils.dip2px(56), height);
        params.gravity = Gravity.CENTER_VERTICAL;
        deleteKey.setSoundEffectsEnabled(false);
        addView(deleteKey, params);
    }

    private ImageView getBtnImage(final String panelName) {
        final StateListDrawable tabbarBtnDrawable = new StateListDrawable();
        final Drawable drawable = HSKeyboardThemeManager.getCurrentTheme().getStyledDrawableFromResources("ic_compound_panel_" + panelName + "_button_unselected");
        final Drawable pressedDrawable = HSKeyboardThemeManager.getCurrentTheme().getStyledDrawableFromResources("ic_compound_panel_" + panelName + "_button_selected");


        tabbarBtnDrawable.addState(new int[]{android.R.attr.state_focused}, pressedDrawable);
        tabbarBtnDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
        tabbarBtnDrawable.addState(new int[]{android.R.attr.state_selected}, pressedDrawable);
        tabbarBtnDrawable.addState(new int[]{}, drawable);

        ImageView tabbarBtn = new ImageView(HSApplication.getContext());
        tabbarBtn.setScaleType(ImageView.ScaleType.CENTER);
        tabbarBtn.setImageDrawable(tabbarBtnDrawable);

        tabbarBtn.setBackgroundDrawable(getBackgroundDrawable());
        tabbarBtn.setSoundEffectsEnabled(false);
        return tabbarBtn;
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
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
                sp.edit().putString("emoticon_last_show_panel_name", tag.toString()).apply();
                containerListener.showPanel(panel);
                HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_emoji_tab_switch", tag.toString());
            }
        } else {
            if (v.getId() == alphabet_left.getId() && keyboardActionListener != null) {
                keyboardActionListener.showPanel(KeyboardPanel.class);
                keyboardActionListener.setBarVisibility(VISIBLE);
                keyboardActionListener = null;
                HSGlobalNotificationCenter.sendNotification(com.ihs.inputmethod.uimodules.constants.Constants.HS_NOTIFICATION_RESET_EDIT_INFO);
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
