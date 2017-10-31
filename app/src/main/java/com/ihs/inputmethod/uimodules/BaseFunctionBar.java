package com.ihs.inputmethod.uimodules;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.theme.HSThemeNewTipController;
import com.ihs.inputmethod.api.utils.HSColorUtils;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.settings.SettingsButton;
import com.ihs.inputmethod.uimodules.stickerplus.PlusButton;
import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiManager;
import com.ihs.inputmethod.uimodules.ui.fonts.common.HSFontSelectViewAdapter;
import com.ihs.inputmethod.uimodules.utils.RippleDrawableUtils;
import com.ihs.inputmethod.uimodules.widget.ClothButton;
import com.ihs.panelcontainer.KeyboardPanelSwitchContainer;

import pl.droidsonroids.gif.GifImageView;

import static com.ihs.inputmethod.uimodules.utils.RippleDrawableUtils.getTransparentRippleBackground;


public final class BaseFunctionBar extends LinearLayout implements View.OnClickListener {
    private LinearLayout functionLayout;
    private SettingsButton settingsButton;
    private OnFunctionBarItemClickListener onFunctionBarClickListener;
    private BaseFunction baseFunction;
    private GifImageView facemojiView;
    private View makeFacemojiTip;
    private PlusButton plusButton;

    private final static int MSG_DISMISS_MAKE_FACEMOJI_TIP = 1;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DISMISS_MAKE_FACEMOJI_TIP:
                    dismissMakeFacemojiTip();
                    break;
            }
        }
    };

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
        functionLayout = (LinearLayout) findViewById(R.id.function_layout);
        initFunctionBar();
    }


    // TODO: 17/4/7 需求要求隐藏web搜索，但是没有去掉。如果不再用，需要完整去除。
//    private BaseFunction webSeachView;
    private BaseFunction clothView;


    private void initFunctionBar() {
        settingsButton = new SettingsButton(getContext());
        baseFunction = new BaseFunction(HSApplication.getContext());
        baseFunction.setId(R.id.func_setting_button);
        baseFunction.setFunctionView(settingsButton);
        baseFunction.setOnClickListener(this);
        baseFunction.setNewTipStatueChangeListener(settingsButton);
        updateFunctionAndSettingButtonSize();
        functionLayout.addView(baseFunction);

        ClothButton clothButton = new ClothButton(getContext());
        clothView = new BaseFunction(getContext());
        clothView.setId(R.id.func_cloth_button);
        clothView.setFunctionView(clothButton);
        clothView.setOnClickListener(this);

        this.setBackgroundDrawable(getTransparentRippleBackground());

        functionLayout.addView(clothView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

        //站位View
        View emptyView = new View(HSApplication.getContext());
        LayoutParams emptyViewLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0);
        emptyViewLayoutParams.weight = 1;
        functionLayout.addView(emptyView, emptyViewLayoutParams);

        if (BuildConfig.ENABLE_FACEMOJI) {
            //FaceMojiView
            facemojiView = new GifImageView(getContext());
            facemojiView.setImageURI(Uri.parse("android.resource://" + HSApplication.getContext().getPackageName() + "/" + R.raw.keyboard_facemoji));
            facemojiView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            facemojiView.setId(R.id.func_facemoji_button);
            facemojiView.setOnClickListener(this);
            facemojiView.setBackgroundDrawable(RippleDrawableUtils.getTransparentRippleBackground());
            functionLayout.addView(facemojiView, new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.config_suggestions_strip_height), LinearLayout.LayoutParams.MATCH_PARENT));
        }

        plusButton = new PlusButton(getContext());
        functionLayout.addView(plusButton, new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.config_suggestions_strip_height), LinearLayout.LayoutParams.MATCH_PARENT));
        plusButton.setVisibility(GONE);
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

    public void showFacemojiGifView() {
        if (facemojiView != null) {
            facemojiView.setVisibility(VISIBLE);
        }
    }

    public void hideFacemojiGifView() {
        if (facemojiView != null) {
            facemojiView.setVisibility(GONE);
        }
    }

    public void showMakeFacemojiTipIfNeed(final KeyboardPanelSwitchContainer keyboardPanelSwitchContainer) {
        if (shouldShowMakeFacmojiTip()) {
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (facemojiView.getWidth() > 0 && facemojiView.getHeight() > 0) {
                        getViewTreeObserver().removeGlobalOnLayoutListener(this);

                        makeFacemojiTip = LayoutInflater.from(getContext()).inflate(R.layout.layout_make_facemoji_tip, BaseFunctionBar.this, false);
                        makeFacemojiTip.measure(0, 0);
                        int[] location = new int[2];
                        facemojiView.getLocationInWindow(location);

                        View triangleView = makeFacemojiTip.findViewById(R.id.triangle_view);
                        LinearLayout.LayoutParams layoutParams = (LayoutParams) triangleView.getLayoutParams();
                        layoutParams.rightMargin = keyboardPanelSwitchContainer.getWidth() - (location[0] + facemojiView.getWidth() / 2) - triangleView.getMeasuredWidth() / 2;

                        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(0, location[1] - makeFacemojiTip.getMeasuredHeight(), 0, 0);
                        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

                        keyboardPanelSwitchContainer.addView(makeFacemojiTip, lp);

                        handler.removeMessages(MSG_DISMISS_MAKE_FACEMOJI_TIP);
                        handler.sendEmptyMessageDelayed(MSG_DISMISS_MAKE_FACEMOJI_TIP, 5000);
                    }
                }
            });
        }
    }

    private boolean shouldShowMakeFacmojiTip() {
        if (BuildConfig.ENABLE_FACEMOJI && FacemojiManager.getDefaultFacePicUri() == null && (System.currentTimeMillis() - PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).getLong("lastShowMakeFacemojiTipTime", 0) > 24 * 60 * 60 * 1000)) {
            PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()).edit().putLong("lastShowMakeFacemojiTipTime", System.currentTimeMillis()).apply();
            return true;
        }
        return false;
    }

    public void dismissMakeFacemojiTip() {
        if (makeFacemojiTip != null && makeFacemojiTip.getParent() != null) {
            ((ViewGroup) makeFacemojiTip.getParent()).removeView(makeFacemojiTip);
            makeFacemojiTip = null;
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
}