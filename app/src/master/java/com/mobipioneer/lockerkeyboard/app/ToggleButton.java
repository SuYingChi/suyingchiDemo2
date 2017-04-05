package com.mobipioneer.lockerkeyboard.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;

public class ToggleButton extends LinearLayout {

    String pTitle = null;
    String tipString = null;
    String preferenceKey = null;
    private boolean mSwitchedOn;
    private Animation anim;
    private Animation inverseAnim;
    private FrameLayout prefButton;
    private ImageView prefSwitchBtn;
//    private final Drawable enabledButton;
//    private final Drawable disabledButton;
    private SharedPreferences mPref;
    private final boolean defaultValue;
    private ToggleButton dependentPref;
    private final static int ANIMATION_DURATION = 150;
    private TextView pTitleView;
    private TextView tipView;
    private boolean animatedOn;
    private IToggleButtonSwitchListener toggleButtonSwitchListener;

    @SuppressLint("Recycle")
    public ToggleButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ButtonSwitch);
        pTitle = ta.getString(R.styleable.ButtonSwitch_button_switch_title);
        tipString = ta.getString(R.styleable.ButtonSwitch_button_switch_tip);
        preferenceKey = ta.getString(R.styleable.ButtonSwitch_button_switch_preferencekey);
        defaultValue = ta.getBoolean(R.styleable.ButtonSwitch_button_switch_defaultValue, false);
        ta.recycle();
//        enabledButton = context.getResources().getDrawable(R.drawable.pref_switch_btn_enabled);
//        disabledButton = context.getResources().getDrawable(R.drawable.pref_switch_btn_disabled);
        init();
    }

    public ToggleButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private void init() {
        mPref = PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext());
        anim = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, 0.0f, TranslateAnimation.RELATIVE_TO_SELF, 1f, TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
        anim.setDuration(ANIMATION_DURATION);
        anim.setFillAfter(true);
        inverseAnim = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, 1f, TranslateAnimation.RELATIVE_TO_SELF, 0f, TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
        inverseAnim.setDuration(ANIMATION_DURATION);
        inverseAnim.setFillAfter(true);
        inflate(getContext(), R.layout.toggle_button, this);
        pTitleView = (TextView) findViewById(R.id.prefs_title);
        pTitleView.setText(pTitle);
        tipView = (TextView) findViewById(R.id.prefs_tip);
        if (tipString==null) {
            tipView.setVisibility(View.GONE);
        }else {
            tipView.setText(tipString);
        }
        prefButton = (FrameLayout) findViewById(R.id.prefs_switch);
        prefSwitchBtn = (ImageView) findViewById(R.id.prefs_switch_btn);
        refreshSwitchState();
        prefButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                toggleSwitch();
            }
        });
    }

    private void toggleSwitch() {
        toggleSwitchButton();
        if (dependentPref == null) {
            return;
        }
        dependentPref.setState(mSwitchedOn);
        if (mSwitchedOn != dependentPref.isSwitchedOn()) {
            dependentPref.toggleSwitchButton();
        }
    }

    public void setState(boolean active) {
        if (!active) {
            this.setClickable(false);
            prefButton.setClickable(false);
            pTitleView.setAlpha(0.6f);
        } else {
            this.setClickable(true);
            prefButton.setClickable(true);
            pTitleView.setAlpha(1f);
        }
    }

    public void setToggleSwitchListener(IToggleButtonSwitchListener listener) {
        this.toggleButtonSwitchListener = listener;
    }

    private void toggleSwitchButton() {
        mSwitchedOn = !mSwitchedOn;
        setSwithToggle(mSwitchedOn);
        mPref.edit().putBoolean(preferenceKey, mSwitchedOn).apply();

        if(toggleButtonSwitchListener!=null){
            toggleButtonSwitchListener.onToggleSwitch(mSwitchedOn);
        }
    }

    public void setSwithToggle(boolean switchOn){
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) prefSwitchBtn.getLayoutParams();
        layoutParams.gravity = Gravity.LEFT|Gravity.CENTER_VERTICAL;
        prefSwitchBtn.setLayoutParams(layoutParams);
        if (!switchOn) {
            prefSwitchBtn.startAnimation(inverseAnim);
            animatedOn = false;
            prefButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.toggle_off_bg));
//            prefSwitchBtn.setImageDrawable(disabledButton);
        } else {
            prefSwitchBtn.startAnimation(anim);
            animatedOn = true;
            prefButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.toggle_on_bg));
//            prefSwitchBtn.setImageDrawable(enabledButton);
        }
    }

    public boolean isSwitchedOn() {
        return mSwitchedOn;
    }

    public void refreshSwitchState() {
        mSwitchedOn = mPref.getBoolean(preferenceKey, defaultValue);
//        prefSwitchBtn.setImageDrawable(mSwitchedOn ? enabledButton : disabledButton);
        prefButton.setBackgroundDrawable(mSwitchedOn ? getResources().getDrawable(R.drawable.toggle_on_bg) : getResources().getDrawable(
                R.drawable.toggle_off_bg));
        if (mSwitchedOn && animatedOn) { //the animatedOn varible is a work around for fixing a triky bug, that is, the button circle disappears everytime after screen lock 
            return;
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) prefSwitchBtn.getLayoutParams();
        layoutParams.gravity = mSwitchedOn ? Gravity.RIGHT|Gravity.CENTER_VERTICAL : Gravity.LEFT|Gravity.CENTER_VERTICAL;
        prefSwitchBtn.setLayoutParams(layoutParams);
    }

    public interface IToggleButtonSwitchListener {
        void onToggleSwitch(boolean isSwitchOn);
    }

}
