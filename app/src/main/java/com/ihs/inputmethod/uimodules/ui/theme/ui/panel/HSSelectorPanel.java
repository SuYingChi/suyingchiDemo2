package com.ihs.inputmethod.uimodules.ui.theme.ui.panel;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputConnection;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.framework.HSInputMethodService;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSDrawableUtils;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.BaseFunctionBar;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.settings.SettingsButton;
import com.ihs.panelcontainer.BasePanel;

import java.lang.ref.WeakReference;

/**
 * Created by yanxia on 2017/11/28.
 */

public class HSSelectorPanel extends BasePanel implements View.OnClickListener, View.OnTouchListener {
    private static final String TAG = "HSSelectorPanel";

    private final static int SELECTOR_KEY_ARROW = R.drawable.ic_selector_arrow_top;
    private final static int SELECTOR_KEY_SELECTOR = R.drawable.ic_selector;
    private final static int SELECTOR_KEY_SELECT_ALL = R.drawable.ic_selector_select_all;
    private final static int SELECTOR_KEY_CUT = R.drawable.ic_selector_cut;
    private final static int SELECTOR_KEY_COPY = R.drawable.ic_selector_copy;
    private final static int SELECTOR_KEY_PASTE = R.drawable.ic_selector_paste;
    private final static int SELECTOR_KEY_DELETE = R.drawable.ic_selector_delete;

    private ImageView selectorDirectionUp;
    private ImageView selectorDirectionDown;
    private ImageView selectorDirectionLeft;
    private ImageView selectorDirectionRight;
    private ImageView selectorDirectionSelectButton;
    private ImageView selectorSelectAllOrCutButton;
    private ImageView selectorCopy;
    private ImageView selectorPaste;
    private ImageView selectorDelete;
    private TextView selectorSelectAllOrCutTextView;

    private long lastDownTime;
    private static final int WHAT_SET_STATE = 1;

    private class MyHandler extends Handler {
        private final WeakReference<HSSelectorPanel> panelWeakReference;

        MyHandler(HSSelectorPanel panel) {
            panelWeakReference = new WeakReference<HSSelectorPanel>(panel);
        }

        /**
         * Subclasses must implement this to receive messages.
         *
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            HSSelectorPanel panel = panelWeakReference.get();
            if (panel != null) {
                int what = msg.what;
                switch (what) {
                    case R.id.selector_direction_up:
                        sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_UP);
                        break;
                    case R.id.selector_direction_down:
                        sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN);
                        break;
                    case R.id.selector_direction_left:
                        sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT);
                        break;
                    case R.id.selector_direction_right:
                        sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT);
                        break;
                    case WHAT_SET_STATE:
                        updateButtonStates();
                        break;
                    default:
                        HSLog.w("unknown what.");
                }
            }
        }
    }

    private final MyHandler handler = new MyHandler(this);

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v == selectorDirectionSelectButton) {
            if (selectorDirectionSelectButton.isSelected()) {
                selectorDirectionSelectButton.setSelected(false);
                //结束选择文本
                releaseShiftKey();
                cancelSelectText();
            } else {
                if (!isEditTextEmpty()) {
                    selectorDirectionSelectButton.setSelected(true);
                    //开始选择文本
                    pressDownShiftKey();
                } else {
                    HSLog.d("edit text is empty");
                }
            }
        } else if (v == selectorSelectAllOrCutButton) {
            if (selectorSelectAllOrCutButton.isSelected()) { // 选中了
                //cut
                performContextMenuAction(android.R.id.cut);
            } else { //未选中
                //select all
                performContextMenuAction(android.R.id.selectAll);
            }
        } else if (v == selectorCopy) {
            performContextMenuAction(android.R.id.copy);
            cancelSelectText();
        } else if (v == selectorPaste) {
            performContextMenuAction(android.R.id.paste);
        } else if (v == selectorDelete) {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
            releaseShiftKey();
        }
    }

    private void sendDownUpKeyEvents(int keyCode) {
        if (hasSelection() && selectorDirectionSelectButton.isSelected()) {
            pressDownShiftKey();
        }
        HSInputMethodService.getInstance().sendDownUpKeyEvents(keyCode);
        handler.removeMessages(WHAT_SET_STATE);
        handler.sendEmptyMessageDelayed(WHAT_SET_STATE, 50);
    }

    private void performContextMenuAction(int id) {
        releaseShiftKey();
        HSInputMethodService.getInstance().getCurrentInputConnection().performContextMenuAction(id);
        updateButtonStates();
    }


    /**
     * Called when a touch event is dispatched to a view. This allows listeners to
     * get a chance to respond before the target view.
     *
     * @param v     The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about
     *              the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int eventAction = event.getAction();
        if (eventAction == MotionEvent.ACTION_UP || eventAction == MotionEvent.ACTION_CANCEL) {
            v.performClick();
            lastDownTime = 0;
            v.setPressed(false);
            handler.removeMessages(v.getId());
        } else {
            v.setPressed(true);
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastDownTime > 100) {
                handler.sendEmptyMessageDelayed(v.getId(), 40);
                lastDownTime = currentTime;
            }
        }
        return true;
    }

    private void cancelSelectText() {
        int start = HSInputMethodService.getInstance().getInputLogic().mConnection.getExpectedSelectionStart();
        HSInputMethodService.getInstance().getCurrentInputConnection().setSelection(start, start);
        updateButtonStates();
    }

    private void pressDownShiftKey() {
        HSInputMethodService.getInstance().getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT));
    }

    private void releaseShiftKey() {
        HSInputMethodService.getInstance().getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT));
    }

    private boolean hasSelection() {
        InputConnection currentInputConnection = HSInputMethodService.getInstance().getCurrentInputConnection();
        return currentInputConnection != null && !TextUtils.isEmpty(currentInputConnection.getSelectedText(0));
    }

    private void updateButtonStates() {
        if (hasSelection()) {
            selectorDirectionSelectButton.setSelected(true);
            selectorSelectAllOrCutButton.setSelected(true);
            selectorSelectAllOrCutTextView.setText(R.string.setting_item_selector_cut);
        } else {
            selectorDirectionSelectButton.setSelected(false);
            selectorSelectAllOrCutButton.setSelected(false);
            selectorSelectAllOrCutTextView.setText(R.string.setting_item_selector_select_all);
        }
    }

    private boolean isEditTextEmpty() {
        return TextUtils.isEmpty(HSInputMethodService.getInstance().getInputLogic().mConnection.getAllText());
    }

    private Drawable getSelectAllOrCutImageDrawable() {
        StateListDrawable stateListDrawable = new StateListDrawable();
        Drawable defNormalDrawable = getStyledDrawableFromResources(SELECTOR_KEY_SELECT_ALL);
        Drawable defPressedDrawable = getStyledDrawableFromResources(SELECTOR_KEY_CUT);
        if (HSKeyboardThemeManager.getCurrentTheme().isDarkBg()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                defNormalDrawable.setColorFilter(ContextCompat.getColor(HSApplication.getContext(), R.color.selector_button_ic_dark_normal), PorterDuff.Mode.SRC_IN);
                defPressedDrawable.setColorFilter(ContextCompat.getColor(HSApplication.getContext(), R.color.selector_button_ic_dark_normal), PorterDuff.Mode.SRC_IN);
            } else {
                DrawableCompat.setTint(defNormalDrawable, ContextCompat.getColor(HSApplication.getContext(), R.color.selector_button_ic_dark_normal));
                DrawableCompat.setTint(defPressedDrawable, ContextCompat.getColor(HSApplication.getContext(), R.color.selector_button_ic_dark_normal));
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                defNormalDrawable.setColorFilter(ContextCompat.getColor(HSApplication.getContext(), R.color.selector_button_ic_light_normal), PorterDuff.Mode.SRC_IN);
                defPressedDrawable.setColorFilter(ContextCompat.getColor(HSApplication.getContext(), R.color.selector_button_ic_light_press), PorterDuff.Mode.SRC_IN);
            } else {
                DrawableCompat.setTint(defNormalDrawable, ContextCompat.getColor(HSApplication.getContext(), R.color.selector_button_ic_light_normal));
                DrawableCompat.setTint(defPressedDrawable, ContextCompat.getColor(HSApplication.getContext(), R.color.selector_button_ic_light_press));
            }
        }
        stateListDrawable.addState(new int[]{android.R.attr.state_selected}, defPressedDrawable);
        stateListDrawable.addState(new int[]{}, defNormalDrawable);
        return stateListDrawable;
    }

    private Drawable getSelectButtonBackgroundDrawable() {
        StateListDrawable stateListDrawable = new StateListDrawable();
        GradientDrawable defNormalDrawable = new GradientDrawable();
        defNormalDrawable.setShape(GradientDrawable.RECTANGLE);
        defNormalDrawable.setCornerRadius(DisplayUtils.dip2px(5));
        GradientDrawable defPressedDrawable = new GradientDrawable();
        defPressedDrawable.setShape(GradientDrawable.RECTANGLE);
        defPressedDrawable.setCornerRadius(DisplayUtils.dip2px(5));
        if (HSKeyboardThemeManager.getCurrentTheme().isDarkBg()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                defNormalDrawable.setColorFilter(ContextCompat.getColor(HSApplication.getContext(), R.color.selector_button_bg_dark_normal), PorterDuff.Mode.SRC_IN);
                defPressedDrawable.setColorFilter(ContextCompat.getColor(HSApplication.getContext(), R.color.selector_button_bg_dark_press), PorterDuff.Mode.SRC_IN);
            } else {
                DrawableCompat.setTint(defNormalDrawable, ContextCompat.getColor(HSApplication.getContext(), R.color.selector_button_bg_dark_normal));
                DrawableCompat.setTint(defPressedDrawable, ContextCompat.getColor(HSApplication.getContext(), R.color.selector_button_bg_dark_press));
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                defNormalDrawable.setColorFilter(ContextCompat.getColor(HSApplication.getContext(), R.color.selector_button_bg_light_normal), PorterDuff.Mode.SRC_IN);
                defPressedDrawable.setColorFilter(ContextCompat.getColor(HSApplication.getContext(), R.color.selector_button_bg_light_press), PorterDuff.Mode.SRC_IN);
            } else {
                DrawableCompat.setTint(defNormalDrawable, ContextCompat.getColor(HSApplication.getContext(), R.color.selector_button_bg_light_normal));
                DrawableCompat.setTint(defPressedDrawable, ContextCompat.getColor(HSApplication.getContext(), R.color.selector_button_bg_light_press));
            }
        }
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, defPressedDrawable);
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
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, HSResourceUtils.getDefaultKeyboardHeight(HSApplication.getContext().getResources()));
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
        selectorDirectionUp.setImageDrawable(getStateListDrawable(SELECTOR_KEY_ARROW, SELECTOR_KEY_ARROW));
        selectorDirectionUp.setOnTouchListener(this);

        selectorDirectionDown = selectorView.findViewById(R.id.selector_direction_down);
        selectorDirectionDown.setImageDrawable(getStateListDrawable(SELECTOR_KEY_ARROW, SELECTOR_KEY_ARROW));
        selectorDirectionDown.setOnTouchListener(this);

        selectorDirectionLeft = selectorView.findViewById(R.id.selector_direction_left);
        selectorDirectionLeft.setImageDrawable(getStateListDrawable(SELECTOR_KEY_ARROW, SELECTOR_KEY_ARROW));
        selectorDirectionLeft.setOnTouchListener(this);

        selectorDirectionRight = selectorView.findViewById(R.id.selector_direction_right);
        selectorDirectionRight.setImageDrawable(getStateListDrawable(SELECTOR_KEY_ARROW, SELECTOR_KEY_ARROW));
        selectorDirectionRight.setOnTouchListener(this);

        selectorDirectionSelectButton = selectorView.findViewById(R.id.selector_select);
        selectorDirectionSelectButton.setImageDrawable(getStateListDrawable(SELECTOR_KEY_SELECTOR, SELECTOR_KEY_SELECTOR));
        selectorDirectionSelectButton.setOnClickListener(this);

        selectorSelectAllOrCutButton = selectorView.findViewById(R.id.selector_select_all_or_cut_image);
        selectorSelectAllOrCutButton.setImageDrawable(getSelectAllOrCutImageDrawable());
        selectorSelectAllOrCutButton.setOnClickListener(this);

        selectorCopy = selectorView.findViewById(R.id.selector_copy_image);
        selectorCopy.setImageDrawable(getStateListDrawable(SELECTOR_KEY_COPY, SELECTOR_KEY_COPY));
        selectorCopy.setOnClickListener(this);

        selectorPaste = selectorView.findViewById(R.id.selector_paste_image);
        selectorPaste.setImageDrawable(getStateListDrawable(SELECTOR_KEY_PASTE, SELECTOR_KEY_PASTE));
        selectorPaste.setOnClickListener(this);

        selectorDelete = selectorView.findViewById(R.id.selector_delete_image);
        selectorDelete.setImageDrawable(getStateListDrawable(SELECTOR_KEY_DELETE, SELECTOR_KEY_DELETE));
        selectorDelete.setOnClickListener(this);

        selectorSelectAllOrCutTextView = selectorView.findViewById(R.id.selector_select_all_and_cut_text);
        TextView selectorCopyTextView = selectorView.findViewById(R.id.selector_copy_text);
        TextView selectorPasteTextView = selectorView.findViewById(R.id.selector_paste_text);
        TextView selectorDeleteTextView = selectorView.findViewById(R.id.selector_delete_text);
        //selectorDirectionSelectButton.setBackgroundDrawable(getSelectButtonBackgroundDrawable());

        if (HSKeyboardThemeManager.getCurrentTheme().isDarkBg()) {
            selectorDirectionSelectButton.setBackgroundResource(R.drawable.selector_select_button_background_dark);
            selectorDirectionUp.setBackgroundResource(R.drawable.selector_button_backgroud_dark);
            selectorDirectionDown.setBackgroundResource(R.drawable.selector_button_backgroud_dark);
            selectorDirectionLeft.setBackgroundResource(R.drawable.selector_button_backgroud_dark);
            selectorDirectionRight.setBackgroundResource(R.drawable.selector_button_backgroud_dark);
            selectorSelectAllOrCutButton.setBackgroundResource(R.drawable.selector_button_backgroud_dark);
            selectorCopy.setBackgroundResource(R.drawable.selector_button_backgroud_dark);
            selectorPaste.setBackgroundResource(R.drawable.selector_button_backgroud_dark);
            selectorDelete.setBackgroundResource(R.drawable.selector_button_backgroud_dark);

            selectorSelectAllOrCutTextView.setTextColor(Color.WHITE);
            selectorCopyTextView.setTextColor(Color.WHITE);
            selectorPasteTextView.setTextColor(Color.WHITE);
            selectorDeleteTextView.setTextColor(Color.WHITE);
        } else {
            selectorDirectionSelectButton.setBackgroundResource(R.drawable.selector_select_button_background_light);
            selectorDirectionUp.setBackgroundResource(R.drawable.selector_button_background_light);
            selectorDirectionDown.setBackgroundResource(R.drawable.selector_button_background_light);
            selectorDirectionLeft.setBackgroundResource(R.drawable.selector_button_background_light);
            selectorDirectionRight.setBackgroundResource(R.drawable.selector_button_background_light);
            selectorSelectAllOrCutButton.setBackgroundResource(R.drawable.selector_button_background_light);
            selectorCopy.setBackgroundResource(R.drawable.selector_button_background_light);
            selectorPaste.setBackgroundResource(R.drawable.selector_button_background_light);
            selectorDelete.setBackgroundResource(R.drawable.selector_button_background_light);

            int color = ContextCompat.getColor(HSApplication.getContext(), R.color.selector_button_ic_light_normal);
            selectorSelectAllOrCutTextView.setTextColor(color);
            selectorCopyTextView.setTextColor(color);
            selectorPasteTextView.setTextColor(color);
            selectorDeleteTextView.setTextColor(color);
        }
    }

    private static StateListDrawable getStateListDrawable(int normalImageName, int pressedImageName) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        Drawable defNormalDrawable = getStyledDrawableFromResources(normalImageName);
        if (HSKeyboardThemeManager.getCurrentTheme().isDarkBg()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                defNormalDrawable.setColorFilter(ContextCompat.getColor(HSApplication.getContext(), R.color.selector_button_ic_dark_normal), PorterDuff.Mode.SRC_IN);
            } else {
                DrawableCompat.setTint(defNormalDrawable, ContextCompat.getColor(HSApplication.getContext(), R.color.selector_button_ic_dark_normal));
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                defNormalDrawable.setColorFilter(ContextCompat.getColor(HSApplication.getContext(), R.color.selector_button_ic_light_normal), PorterDuff.Mode.SRC_IN);
            } else {
                DrawableCompat.setTint(defNormalDrawable, ContextCompat.getColor(HSApplication.getContext(), R.color.selector_button_ic_light_normal));
            }
        }

        Drawable defPressedDrawable = getStyledDrawableFromResources(pressedImageName);
        if (HSKeyboardThemeManager.getCurrentTheme().isDarkBg()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                defPressedDrawable.setColorFilter(ContextCompat.getColor(HSApplication.getContext(), R.color.selector_button_ic_dark_press), PorterDuff.Mode.SRC_IN);
            } else {
                DrawableCompat.setTint(defPressedDrawable, ContextCompat.getColor(HSApplication.getContext(), R.color.selector_button_ic_dark_press));
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                defPressedDrawable.setColorFilter(ContextCompat.getColor(HSApplication.getContext(), R.color.selector_button_ic_light_press), PorterDuff.Mode.SRC_IN);
            } else {
                DrawableCompat.setTint(defPressedDrawable, ContextCompat.getColor(HSApplication.getContext(), R.color.selector_button_ic_light_press));
            }
        }

        stateListDrawable.addState(new int[]{android.R.attr.state_focused}, defPressedDrawable);
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, defPressedDrawable);
        stateListDrawable.addState(new int[]{android.R.attr.state_selected}, defPressedDrawable);
        stateListDrawable.addState(new int[]{}, defNormalDrawable);
        return stateListDrawable;
    }

    private static Drawable getStyledDrawableFromResources(int resId) {
        if (resId != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return ContextCompat.getDrawable(HSApplication.getContext(), resId);
            } else {
                return VectorDrawableCompat.create(HSApplication.getContext().getResources(), resId, null);
            }
        } else {
            HSLog.e(TAG, "getStyledDrawableFromResources called with wrong resId = [" + resId + "]");
            return HSDrawableUtils.getTransparentBitmapDrawable();
        }
    }

    public HSSelectorPanel() {
        super();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
