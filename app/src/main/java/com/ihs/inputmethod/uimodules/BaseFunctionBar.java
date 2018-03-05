package com.ihs.inputmethod.uimodules;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.theme.HSThemeNewTipController;
import com.ihs.inputmethod.api.utils.HSColorUtils;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.settings.SettingsButton;
import com.ihs.inputmethod.uimodules.softgame.SoftGameButton;
import com.ihs.inputmethod.uimodules.stickerplus.PlusButton;
import com.ihs.inputmethod.uimodules.ui.fonts.common.HSFontSelectViewAdapter;
import com.ihs.inputmethod.uimodules.widget.ClothButton;

import static com.ihs.inputmethod.uimodules.utils.RippleDrawableUtils.getTransparentRippleBackground;


public final class BaseFunctionBar extends LinearLayout implements View.OnClickListener {
    private LinearLayout functionLayout;
    private SettingsButton settingsButton;
    private OnFunctionBarItemClickListener onFunctionBarClickListener;
    private BaseFunction baseFunction;
    private PlusButton plusButton;

    private SoftGameButton softGameButton;

    public BaseFunctionBar(Context context) {
        this(context, null);
    }

    public BaseFunctionBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseFunctionBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        HSGlobalNotificationCenter.addObserver(HSKeyboardThemeManager.HS_NOTIFICATION_THEME_CHANGED, observer);
        HSGlobalNotificationCenter.addObserver(HSFontSelectViewAdapter.HS_NOTIFICATION_FONT_CHANGED, observer);
        HSGlobalNotificationCenter.addObserver(HSInputMethod.HS_NOTIFICATION_HIDE_WINDOW, observer);

        setGravity(Gravity.CENTER_VERTICAL);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        functionLayout = findViewById(R.id.function_layout);
        initFunctionBar();
    }


    // TODO: 17/4/7 需求要求隐藏web搜索，但是没有去掉。如果不再用，需要完整去除。
    private BaseFunction webSeachButton;
    private BaseFunction clothView;


    private void initFunctionBar() {
        this.setBackgroundDrawable(getTransparentRippleBackground());

        settingsButton = new SettingsButton(getContext());
        baseFunction = new BaseFunction(HSApplication.getContext());
        baseFunction.setId(R.id.func_setting_button);
        baseFunction.setFunctionView(settingsButton);
        baseFunction.setOnClickListener(this);
        baseFunction.setNewTipStatueChangeListener(settingsButton);
        updateFunctionAndSettingButtonSize();
        functionLayout.addView(baseFunction);

        if (!BuildConfig.BASS_PRODUCT) {
            ClothButton clothButton = new ClothButton(getContext());
            clothView = new BaseFunction(getContext());
            clothView.setId(R.id.func_cloth_button);
            clothView.setFunctionView(clothButton);
            clothView.setOnClickListener(this);
            functionLayout.addView(clothView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

            //站位View
            View emptyView = new View(HSApplication.getContext());
            LayoutParams emptyViewLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0);
            emptyViewLayoutParams.weight = 1;
            functionLayout.addView(emptyView, emptyViewLayoutParams);

            //search view
            webSeachButton = new BaseFunction(getContext());
            ImageView webIcon = new ImageView(getContext());
            refreshDrawable(webIcon, "menu_search.png", R.drawable.web_search_icon_funcbar);
            webIcon.setScaleType(ImageView.ScaleType.CENTER);
            webSeachButton.setFunctionView(webIcon);
            webSeachButton.setId(R.id.web_search_icon);
            webSeachButton.setOnClickListener(this);

            LayoutParams param = new LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT, 1.0f);
            functionLayout.addView(new View(getContext()), param);

            if (HSDisplayUtils.getRotation(getContext()) == Surface.ROTATION_0) {
                functionLayout.addView(webSeachButton, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }


            softGameButton = new SoftGameButton(getContext());
            functionLayout.addView(softGameButton, new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.config_suggestions_strip_height), LinearLayout.LayoutParams.MATCH_PARENT));
        }

        plusButton = new PlusButton(getContext());
        functionLayout.addView(plusButton, new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.config_suggestions_strip_height), LinearLayout.LayoutParams.MATCH_PARENT));
        plusButton.setVisibility(GONE);
    }

    public SoftGameButton getSoftGameButton() {
        return softGameButton;
    }

    public void checkNewGame() {
        softGameButton.checkNewGame();
    }

    public void setOnFunctionBarClickListener(OnFunctionBarItemClickListener onFunctionBarClickListener) {
        this.onFunctionBarClickListener = onFunctionBarClickListener;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final Resources res = getResources();
        final int width = HSResourceUtils.getDefaultKeyboardWidth(res);
        final int height = res.getDimensionPixelSize(R.dimen.config_suggestions_strip_height);
        setMeasuredDimension(width, height);
    }

    private void updateFunctionAndSettingButtonSize() {
        final FrameLayout.LayoutParams llp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llp.gravity = Gravity.CENTER;
        settingsButton.setLayoutParams(llp);

        LayoutParams functionLayoutParam = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, getResources().getDimensionPixelSize(R.dimen.config_suggestions_strip_height));

        baseFunction.setLayoutParams(functionLayoutParam);
    }

    INotificationObserver observer = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (HSKeyboardThemeManager.HS_NOTIFICATION_THEME_CHANGED.equals(s)) {
                resetSettingButtonType();
            } else if (HSFontSelectViewAdapter.HS_NOTIFICATION_FONT_CHANGED.equals(s)) {
                resetSettingButtonType();
            }
        }
    };

    private void resetSettingButtonType() {
        setSettingButtonType(SettingsButton.SettingButtonType.MENU);
    }

    @Override
    public void onClick(View view) {
        if (onFunctionBarClickListener != null) {
            onFunctionBarClickListener.onFunctionBarItemClick(view);
        }
    }

    public void setFunctionEnable(boolean enabled) {
        baseFunction.setEnabled(enabled);
    }

    public void setSettingButtonType(int type) {
        settingsButton.setButtonType(type);
        updateFunctionAndSettingButtonSize();
    }

    public SettingsButton getSettingsButton() {
        return settingsButton;
    }

    public void onDestroy() {
        HSGlobalNotificationCenter.removeObserver(observer);
    }

    public void showNewMarkIfNeed() {
        boolean shouldShowNewTip = HSThemeNewTipController.getInstance().hasNewTipNow();
        if (shouldShowNewTip) {
            baseFunction.showNewTip();
        } else {
            baseFunction.hideNewTip();
        }
    }

    public void hideNewMark() {
        baseFunction.hideNewTip();
    }

    public void showMenuButton(View view) {
        if (view != null) {
            view.setVisibility(VISIBLE);
        }
    }

    public void hideMenuButton(View view) {
        if (view != null) {
            view.setVisibility(GONE);
        }
    }

    public interface OnFunctionBarItemClickListener {
        void onFunctionBarItemClick(View v);
    }


    public static Drawable getFuncButtonDrawable(Drawable drawable) {
        Drawable compatDrawable = DrawableCompat.wrap(drawable);
        int defaultValue = Color.TRANSPARENT;
        int normalColor = HSKeyboardThemeManager.getCurrentTheme().getFuncBarButtonColor(HSKeyboardThemeManager.getCurrentTheme().getKeyTextColor(defaultValue));
        int pressedColor = HSColorUtils.darkerColor(normalColor);
        ColorStateList colorStateList = new ColorStateList(
                new int[][]
                        {
                                new int[]{android.R.attr.state_pressed},
                                new int[]{android.R.attr.state_focused},
                                new int[]{android.R.attr.state_activated},
                                new int[]{}
                        },
                new int[]
                        {
                                pressedColor,
                                pressedColor,
                                pressedColor,
                                normalColor
                        }
        );
        DrawableCompat.setTintList(compatDrawable, colorStateList);
        return compatDrawable;
    }


    private void refreshDrawable(ImageView funcButton, String drawableName, int defaultDrawableId) {

        Drawable drawable = HSKeyboardThemeManager.getThemeSettingMenuDrawable(drawableName, null);
        if (drawable == null) {
            drawable = VectorDrawableCompat.create(getResources(), defaultDrawableId, null);
            drawable = getFuncButtonDrawable(drawable);
        }
        funcButton.setImageDrawable(drawable);
    }

    public PlusButton getPLusButton() {
        return plusButton;
    }

    public BaseFunction getWebSeachButton() {
        return webSeachButton;
    }
}