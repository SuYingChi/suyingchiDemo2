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
import android.support.v7.widget.RecyclerView;
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
import com.ihs.inputmethod.uimodules.ui.emoticon.bean.ActionbarTab;

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
    private RecyclerView recentRecyclerView = null;
    private RecyclerView pinsRecyclerView = null;
    private Map<RecyclerView, View> btnMap = new HashMap<>();
    private Map<String, RecyclerView> clipboardViews = new HashMap<>();
    boolean isCurrentThemeDarkBg = HSKeyboardThemeManager.getCurrentTheme().isDarkBg();
    private ClipboardTabChangeListener clipboardTabChangeListener;

    public ClipBoardActionBar(Context context) {
        this(context, null);
    }

    public ClipBoardActionBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClipBoardActionBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        isCurrentThemeDarkBg = HSKeyboardThemeManager.getCurrentTheme().isDarkBg();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        List<ClipboardActionbarTab> actionbarTabs = new ArrayList<>();
        actionbarTabs.add(new ClipboardActionbarTab(PANEL_RECENT, recentRecyclerView, R.drawable.ic_emoji_panel_tab));
        actionbarTabs.add(new ClipboardActionbarTab(PANEL_PIN, pinsRecyclerView, R.drawable.ic_text_panel_tab));
        final int height = getResources().getDimensionPixelSize(R.dimen.emoticon_panel_actionbar_height);
        final int actionBarAmount = actionbarTabs.size();
        for (int i = 0; i < actionBarAmount; i++) {
            final String viewName = actionbarTabs.get(i).viewName;
            final RecyclerView recyclerView = actionbarTabs.get(i).view;
            final LayoutParams params = new LayoutParams(0, height, 1.0f);

            RelativeLayout clipActionBarBtnView = (RelativeLayout) inflate(getContext(), R.layout.clipboard_bar_button, null);
            setBtnImage(clipActionBarBtnView.findViewById(R.id.iv_icon), actionbarTabs.get(i).iconResId);
            ((TextView) clipActionBarBtnView.findViewById(R.id.tv_title)).setText(viewName);
            clipActionBarBtnView.setTag(viewName);
            clipActionBarBtnView.setOnClickListener(this);
            clipActionBarBtnView.setBackgroundDrawable(ClipboardPresenter.getInstance().getClipActionBarBtnViewBackgroundDrawable());

            addView(clipActionBarBtnView, params);
            clipboardViews.put(viewName, recyclerView);
            btnMap.put(recyclerView, clipActionBarBtnView);
        }
    }


    private void setBtnImage(ImageView btnImageView, int iconResId) {
        Drawable tabBarBtnDrawable = getTabDrawable(iconResId);
        btnImageView.setScaleType(ImageView.ScaleType.CENTER);
        btnImageView.setImageDrawable(tabBarBtnDrawable);
        btnImageView.setSoundEffectsEnabled(false);
    }

    @NonNull
    private Drawable getTabDrawable(int resId) {
        Resources resources = HSApplication.getContext().getResources();
        int pressColor = isCurrentThemeDarkBg ? resources.getColor(R.color.emoji_panel_tab_selected_color_when_theme_dark_bg) : resources.getColor(R.color.emoji_panel_tab_selected_color);
        int normalColor = isCurrentThemeDarkBg ? resources.getColor(R.color.emoji_panel_tab_normal_color_when_theme_dark_bg) : resources.getColor(R.color.emoji_panel_tab_normal_color);

        Drawable tabBarBtnDrawable = VectorDrawableCompat.create(resources, resId, null);
        DrawableCompat.setTintList(tabBarBtnDrawable, new ColorStateList(
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
        return tabBarBtnDrawable;
    }

    void setClipboardTabChangeListener(ClipboardTabChangeListener clipboardTabChangeListener) {
        this.clipboardTabChangeListener = clipboardTabChangeListener;
    }

    interface ClipboardTabChangeListener {

        void showView(RecyclerView selectedView);
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
            //Class panel = panels.get(tag);
            RecyclerView view = clipboardViews.get(tag);

            if (/*containerListener != null && panel != null*/view != null) {
                //containerListener.showPanel(panel);
                clipboardTabChangeListener.showView(view);
                HSAnalytics.logEvent("keyboard_emoji_tab_switch", "tagContent", tag.toString());
            }
        } else {
            if (tag instanceof Integer && (Integer) tag == Constants.CODE_DELETE) {
                HSInputMethod.deleteBackward();
            }
        }
    }

    void selectedViewBtn(RecyclerView recyclerView) {
        for (RecyclerView recyclerViewValue : clipboardViews.values()) {
            View view = btnMap.get(recyclerViewValue);
            if (view != null) {
                view.setSelected(false);
            }
        }

        View view = btnMap.get(recyclerView);
        if (view != null) {
            view.setSelected(true);
        }

    }

    public void setClipboardRecentRecyclerView(RecyclerView recentRecyclerView) {
        this.recentRecyclerView = recentRecyclerView;
    }

    public void setClipboardPinsRecyclerView(RecyclerView pinsRecyclerView) {
        this.pinsRecyclerView = pinsRecyclerView;
    }

    private class ClipboardActionbarTab {
        public int iconResId;
        public String viewName;
        public RecyclerView view;

        public ClipboardActionbarTab(String viewName, RecyclerView view, int iconResId) {
            this.viewName = viewName;
            this.iconResId = iconResId;
            this.view = view;
        }
    }
}
