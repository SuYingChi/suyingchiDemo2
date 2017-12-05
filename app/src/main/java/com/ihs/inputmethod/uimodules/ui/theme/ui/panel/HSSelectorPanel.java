package com.ihs.inputmethod.uimodules.ui.theme.ui.panel;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.framework.HSInputMethodService;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.BaseFunctionBar;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.settings.SettingsButton;
import com.ihs.inputmethod.uimodules.settings.ViewItemBuilder;
import com.ihs.panelcontainer.BasePanel;

/**
 * Created by yanxia on 2017/11/28.
 */

public class HSSelectorPanel extends BasePanel {

    private final static String SELECTOR_KEY_ARROW = "ic_selector_arrow_top";
    private final static String SELECTOR_KEY_SELECTOR = "ic_selector";
    private final static String SELECTOR_KEY_SELECT_ALL = "ic_selector_select_all";
    private final static String SELECTOR_KEY_CUT = "ic_selector_cut";
    private final static String SELECTOR_KEY_COPY = "ic_selector_copy";
    private final static String SELECTOR_KEY_PASTE = "ic_selector_copy";
    private final static String SELECTOR_KEY_DELETE = "ic_selector_delete";

    private ImageView selectorDirectionUp;
    private ImageView selectorDirectionDown;
    private ImageView selectorDirectionLeft;
    private ImageView selectorDirectionRight;
    private ImageView selectorDirectionSelectButton;
    private ImageView selectorSelectAllOrCut;
    private ImageView selectorCopy;
    private ImageView selectorPaste;
    private ImageView selectorDelete;
    private TextView selectorSelectAllOrCutTextView;

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(this);
            setState();
        }
    };

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == selectorDirectionUp) {
                if (HSInputMethodService.getInstance().getInputLogic().mConnection.hasSelection() && selectorDirectionSelectButton.isSelected()) {
                    pressDownShiftKey();
                }
                HSInputMethodService.getInstance().sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_UP);
                setState();
            } else if (v == selectorDirectionDown) {
                if (HSInputMethodService.getInstance().getInputLogic().mConnection.hasSelection() && selectorDirectionSelectButton.isSelected()) {
                    pressDownShiftKey();
                }
                HSInputMethodService.getInstance().sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN);
                setState();
            } else if (v == selectorDirectionLeft) {
                if (HSInputMethodService.getInstance().getInputLogic().mConnection.hasSelection() && selectorDirectionSelectButton.isSelected()) {
                    pressDownShiftKey();
                }
                HSInputMethodService.getInstance().sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT);
                setState();
            } else if (v == selectorDirectionRight) {
                if (HSInputMethodService.getInstance().getInputLogic().mConnection.hasSelection() && selectorDirectionSelectButton.isSelected()) {
                    pressDownShiftKey();
                }
                HSInputMethodService.getInstance().sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT);
                setState();
            } else if (v == selectorDirectionSelectButton) {
                if (selectorDirectionSelectButton.isSelected()) {
                    selectorDirectionSelectButton.setSelected(false);
                    //结束选择文本
                    releaseShiftKey();
                } else {
                    if (!isEditTextEmpty()) {
                        selectorDirectionSelectButton.setSelected(true);
                        //开始选择文本
                        pressDownShiftKey();
                    } else {
                        HSLog.d("edit text is empty");
                    }
                }
            } else if (v == selectorSelectAllOrCut) {
                if (selectorSelectAllOrCut.isSelected()) { // 选中了
                    //cut
                    releaseShiftKey();
                    HSInputMethodService.getInstance().getCurrentInputConnection().performContextMenuAction(android.R.id.cut);
                } else { //未选中
                    //select all
                    HSInputMethodService.getInstance().getCurrentInputConnection().performContextMenuAction(android.R.id.selectAll);
                }
                setState();
            } else if (v == selectorCopy) {
                releaseShiftKey();
                HSInputMethodService.getInstance().getCurrentInputConnection().performContextMenuAction(android.R.id.copy);
                setState();
            } else if (v == selectorPaste) {
                releaseShiftKey();
                HSInputMethodService.getInstance().getCurrentInputConnection().performContextMenuAction(android.R.id.paste);
                setState();
            } else if (v == selectorDelete) {
                releaseShiftKey();
                HSInputMethodService.getInstance().sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
                handler.postDelayed(runnable, 100);
            }
        }
    };

    private void pressDownShiftKey() {
        HSInputMethodService.getInstance().getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT));
    }

    private void releaseShiftKey() {
        HSInputMethodService.getInstance().getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT));
    }

    private boolean hasSelection() {
        return !TextUtils.isEmpty(HSInputMethodService.getInstance().getCurrentInputConnection().getSelectedText(0));
    }

    private void setState() {
        if (hasSelection()) {
            selectorDirectionSelectButton.setSelected(true);
            selectorSelectAllOrCut.setSelected(true);
            selectorSelectAllOrCutTextView.setText(R.string.setting_item_selector_cut);
        } else {
            selectorDirectionSelectButton.setSelected(false);
            selectorSelectAllOrCut.setSelected(false);
            selectorSelectAllOrCutTextView.setText(R.string.setting_item_selector_select_all);
        }
    }

    private boolean isEditTextEmpty() {
        return TextUtils.isEmpty(HSInputMethodService.getInstance().getInputLogic().mConnection.getAllText());
    }

    private int getCursorPosition() {
        InputConnection ic = HSInputMethodService.getInstance().getCurrentInputConnection();
        ExtractedText et = ic.getExtractedText(new ExtractedTextRequest(), 0);
        return et == null ? 0 : et.selectionStart;
    }

    private Drawable getSelectAllOrCutImageDrawable() {
        StateListDrawable stateListDrawable = new StateListDrawable();
        Drawable defNormalDrawable = ViewItemBuilder.getStyledDrawableFromResources(SELECTOR_KEY_SELECT_ALL);
        Drawable defPressedDrawable = ViewItemBuilder.getStyledDrawableFromResources(SELECTOR_KEY_CUT);
        if (HSKeyboardThemeManager.getCurrentTheme().isDarkBg()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                defNormalDrawable.setColorFilter(ContextCompat.getColor(HSApplication.getContext(), R.color.settings_button_dark_normal), PorterDuff.Mode.SRC_IN);
                defPressedDrawable.setColorFilter(ContextCompat.getColor(HSApplication.getContext(), R.color.settings_button_dark_normal), PorterDuff.Mode.SRC_IN);
            } else {
                DrawableCompat.setTint(defNormalDrawable, ContextCompat.getColor(HSApplication.getContext(), R.color.settings_button_dark_normal));
                DrawableCompat.setTint(defPressedDrawable, ContextCompat.getColor(HSApplication.getContext(), R.color.settings_button_dark_normal));
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                defNormalDrawable.setColorFilter(ContextCompat.getColor(HSApplication.getContext(), R.color.settings_button_light_icon), PorterDuff.Mode.SRC_IN);
                defPressedDrawable.setColorFilter(ContextCompat.getColor(HSApplication.getContext(), R.color.settings_button_light_icon), PorterDuff.Mode.SRC_IN);
            } else {
                DrawableCompat.setTint(defNormalDrawable, ContextCompat.getColor(HSApplication.getContext(), R.color.settings_button_light_icon));
                DrawableCompat.setTint(defPressedDrawable, ContextCompat.getColor(HSApplication.getContext(), R.color.settings_button_light_icon));
            }
        }
        stateListDrawable.addState(new int[]{android.R.attr.state_selected}, defPressedDrawable);
        stateListDrawable.addState(new int[]{}, defNormalDrawable);
        return stateListDrawable;
    }

    @Override
    protected View onCreatePanelView() {
        //set functionBar setting button type
        FrameLayout panelView = new FrameLayout(HSApplication.getContext());
        BaseFunctionBar functionBar = (BaseFunctionBar) panelActionListener.getBarView();
        functionBar.setSettingButtonType(SettingsButton.SettingButtonType.BACK);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.settings_selector_layout, null);
        initView(view);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) HSApplication.getContext().getResources().getDimension(R.dimen.config_default_keyboard_height));
        panelView.addView(view, layoutParams);
        return panelView;
    }

    private void initView(View selectorView) {
        selectorView.setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
        selectorView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        selectorDirectionUp = selectorView.findViewById(R.id.selector_direction_up);
        selectorDirectionUp.setImageDrawable(ViewItemBuilder.getStateListDrawable(SELECTOR_KEY_ARROW, SELECTOR_KEY_ARROW));
        selectorDirectionUp.setOnClickListener(clickListener);

        selectorDirectionDown = selectorView.findViewById(R.id.selector_direction_down);
        selectorDirectionDown.setImageDrawable(ViewItemBuilder.getStateListDrawable(SELECTOR_KEY_ARROW, SELECTOR_KEY_ARROW));
        selectorDirectionDown.setOnClickListener(clickListener);

        selectorDirectionLeft = selectorView.findViewById(R.id.selector_direction_left);
        selectorDirectionLeft.setImageDrawable(ViewItemBuilder.getStateListDrawable(SELECTOR_KEY_ARROW, SELECTOR_KEY_ARROW));
        selectorDirectionLeft.setOnClickListener(clickListener);

        selectorDirectionRight = selectorView.findViewById(R.id.selector_direction_right);
        selectorDirectionRight.setImageDrawable(ViewItemBuilder.getStateListDrawable(SELECTOR_KEY_ARROW, SELECTOR_KEY_ARROW));
        selectorDirectionRight.setOnClickListener(clickListener);

        selectorDirectionSelectButton = selectorView.findViewById(R.id.selector_select);
        selectorDirectionSelectButton.setImageDrawable(ViewItemBuilder.getStateListDrawable(SELECTOR_KEY_SELECTOR, SELECTOR_KEY_SELECTOR));
        selectorDirectionSelectButton.setOnClickListener(clickListener);

        selectorSelectAllOrCut = selectorView.findViewById(R.id.selector_select_all_or_cut_image);
        selectorSelectAllOrCut.setImageDrawable(getSelectAllOrCutImageDrawable());
        selectorSelectAllOrCut.setOnClickListener(clickListener);

        selectorCopy = selectorView.findViewById(R.id.selector_copy_image);
        selectorCopy.setImageDrawable(ViewItemBuilder.getStateListDrawable(SELECTOR_KEY_COPY, SELECTOR_KEY_COPY));
        selectorCopy.setOnClickListener(clickListener);

        selectorPaste = selectorView.findViewById(R.id.selector_paste_image);
        selectorPaste.setImageDrawable(ViewItemBuilder.getStateListDrawable(SELECTOR_KEY_PASTE, SELECTOR_KEY_PASTE));
        selectorPaste.setOnClickListener(clickListener);

        selectorDelete = selectorView.findViewById(R.id.selector_delete_image);
        selectorDelete.setImageDrawable(ViewItemBuilder.getStateListDrawable(SELECTOR_KEY_DELETE, SELECTOR_KEY_DELETE));
        selectorDelete.setOnClickListener(clickListener);

        selectorSelectAllOrCutTextView = selectorView.findViewById(R.id.selector_select_all_and_cut_text);
        TextView selectorCopyTextView = selectorView.findViewById(R.id.selector_copy_text);
        TextView selectorPasteTextView = selectorView.findViewById(R.id.selector_paste_text);
        TextView selectorDeleteTextView = selectorView.findViewById(R.id.selector_delete_text);

        if (HSKeyboardThemeManager.getCurrentTheme().isDarkBg()) {
            selectorDirectionUp.setBackgroundResource(R.drawable.settings_key_common_background_selector);
            selectorDirectionDown.setBackgroundResource(R.drawable.settings_key_common_background_selector);
            selectorDirectionLeft.setBackgroundResource(R.drawable.settings_key_common_background_selector);
            selectorDirectionRight.setBackgroundResource(R.drawable.settings_key_common_background_selector);
            selectorDirectionSelectButton.setBackgroundResource(R.drawable.settings_key_common_background_selector);
            selectorSelectAllOrCut.setBackgroundResource(R.drawable.selector_button_backgroud);
            selectorCopy.setBackgroundResource(R.drawable.settings_key_common_background_selector);
            selectorPaste.setBackgroundResource(R.drawable.settings_key_common_background_selector);
            selectorDelete.setBackgroundResource(R.drawable.settings_key_common_background_selector);
            selectorSelectAllOrCutTextView.setTextColor(Color.WHITE);
            selectorCopyTextView.setTextColor(Color.WHITE);
            selectorPasteTextView.setTextColor(Color.WHITE);
            selectorDeleteTextView.setTextColor(Color.WHITE);
        } else {
            selectorDirectionUp.setBackgroundResource(R.drawable.settings_key_common_background_selector_light);
            selectorDirectionDown.setBackgroundResource(R.drawable.settings_key_common_background_selector_light);
            selectorDirectionLeft.setBackgroundResource(R.drawable.settings_key_common_background_selector_light);
            selectorDirectionRight.setBackgroundResource(R.drawable.settings_key_common_background_selector_light);
            selectorDirectionSelectButton.setBackgroundResource(R.drawable.settings_key_common_background_selector_light);
            selectorSelectAllOrCut.setBackgroundResource(R.drawable.selector_button_background_light);
            selectorCopy.setBackgroundResource(R.drawable.settings_key_common_background_selector_light);
            selectorPaste.setBackgroundResource(R.drawable.settings_key_common_background_selector_light);
            selectorDelete.setBackgroundResource(R.drawable.settings_key_common_background_selector_light);
            int color = ContextCompat.getColor(HSApplication.getContext(), R.color.settings_button_light_icon);
            selectorSelectAllOrCutTextView.setTextColor(color);
            selectorCopyTextView.setTextColor(color);
            selectorPasteTextView.setTextColor(color);
            selectorDeleteTextView.setTextColor(color);
        }
    }

    public HSSelectorPanel() {
        super();
    }

    @Override
    protected boolean onHidePanelView(int appearMode) {
        return super.onHidePanelView(appearMode);
    }

    @Override
    protected boolean onShowPanelView(int appearMode) {
        return super.onShowPanelView(appearMode);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View getPanelView() {
        return super.getPanelView();
    }

    @Override
    public void setPanelView(View rootView) {
        super.setPanelView(rootView);
    }

    @Override
    public void showChildPanel(Class panelClass, Bundle bundle) {
        super.showChildPanel(panelClass, bundle);
    }

    @Override
    protected Bundle getBundle() {
        return super.getBundle();
    }

    @Override
    public void setBarVisibility(int visibility) {
        super.setBarVisibility(visibility);
    }

    @Override
    public View getKeyboardView() {
        return super.getKeyboardView();
    }

    @Override
    public Animation getAppearAnimator() {
        return super.getAppearAnimator();
    }

    @Override
    public Animation getDismissAnimator() {
        return super.getDismissAnimator();
    }

    @Override
    public void setOnAnimationListener(OnAnimationListener onAnimationListener) {
        super.setOnAnimationListener(onAnimationListener);
    }

    @Override
    public void setPanelActionListener(OnPanelActionListener onPanelActionListener) {
        super.setPanelActionListener(onPanelActionListener);
    }

    @Override
    public OnPanelActionListener getPanelActionListener() {
        return super.getPanelActionListener();
    }

    @Override
    public void backToParentPanel(boolean keepSelf) {
        super.backToParentPanel(keepSelf);
    }
}
