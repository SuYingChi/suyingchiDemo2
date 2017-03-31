package com.mobipioneer.inputmethod.panels.settings.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
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

public class SwitchPreference extends LinearLayout {

    String pTitle = null;
    String tipString = null;
    String preferenceKey = null;
    private boolean mSwitchedOn;
    private Animation anim;
    private Animation inverseAnim;
    private FrameLayout prefButton;
    private ImageView prefSwitchBtn;
    private View prefSwitchLine;
    private final Drawable enabledButton;
    private final Drawable disabledButton;
    private SharedPreferences mPref;
    private final boolean defaultValue;
    private final boolean hasTip;
    private SwitchPreference dependentPref;
    private final static int ANIMATION_DURATION = 150;
    private TextView pTitleView;
    private TextView tipView;
    private boolean animatedOn;
    private ISwitchPreferenceToggleListener switchPreferenceToggleListener;

    @SuppressLint("Recycle")
    public SwitchPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SwitchPreference);
        pTitle = ta.getString(R.styleable.SwitchPreference_titlestring);
        tipString = ta.getString(R.styleable.SwitchPreference_tipstring);
        preferenceKey = ta.getString(R.styleable.SwitchPreference_preferenceKey);
        defaultValue = ta.getBoolean(R.styleable.SwitchPreference_defaultValue, false);
        hasTip = ta.getBoolean(R.styleable.SwitchPreference_hasTip, false);
        ta.recycle();
        enabledButton = context.getResources().getDrawable(R.drawable.pref_switch_btn_enabled);
        disabledButton = context.getResources().getDrawable(R.drawable.pref_switch_btn_disabled);
        init();
    }

    public SwitchPreference(Context context, AttributeSet attrs) {
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
        inflate(getContext(), R.layout.switch_preference, this);
        pTitleView = (TextView) findViewById(R.id.prefs_title);
        pTitleView.setText(pTitle);
        tipView = (TextView) findViewById(R.id.prefs_tip);
        if (!hasTip) {
            tipView.setVisibility(View.GONE);
        }else {
            tipView.setText(tipString);
        }
        prefButton = (FrameLayout) findViewById(R.id.prefs_switch);
        prefSwitchBtn = (ImageView) findViewById(R.id.prefs_switch_btn);
        prefSwitchLine = findViewById(R.id.prefs_switch_line);
        refreshSwitchState();
        prefButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                toggleSwitch();
            }
        });

        this.setOnClickListener(new OnClickListener() {
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

    public void setDependentPreference(SwitchPreference pref) {
        this.dependentPref = pref;
    }
    public void setSwitchPreferenceToggleListener(ISwitchPreferenceToggleListener listener) {
        this.switchPreferenceToggleListener = listener;
    }

    public void toggleSwitchButton() {
        mSwitchedOn = !mSwitchedOn;
        setSwithToggle(mSwitchedOn);
        mPref.edit().putBoolean(preferenceKey, mSwitchedOn).apply();

        if(switchPreferenceToggleListener!=null){
            switchPreferenceToggleListener.onToggleSwitch(mSwitchedOn);
        }
    }

    public boolean isSwitchedOn() {
        return mSwitchedOn;
    }

    public void setSwithToggle(boolean switchOn){
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) prefSwitchBtn.getLayoutParams();
        layoutParams.gravity = Gravity.LEFT;
        prefSwitchBtn.setLayoutParams(layoutParams);
        if (switchOn) {
            prefSwitchBtn.startAnimation(anim);
            animatedOn = true;
            prefSwitchLine.setBackgroundDrawable(getResources().getDrawable(R.drawable.pref_switch_bar_enabled));
            prefSwitchBtn.setImageDrawable(enabledButton);
        } else {
            prefSwitchBtn.startAnimation(inverseAnim);
            animatedOn = false;
            prefSwitchLine.setBackgroundDrawable(getResources().getDrawable(R.drawable.pref_switch_bar_disabled));
            prefSwitchBtn.setImageDrawable(disabledButton);
        }
    }

    public void refreshSwitchState() {
        mSwitchedOn = mPref.getBoolean(preferenceKey, defaultValue);
        prefSwitchBtn.setImageDrawable(mSwitchedOn ? enabledButton : disabledButton);
        prefSwitchLine.setBackgroundDrawable(mSwitchedOn ? getResources().getDrawable(R.drawable.pref_switch_bar_enabled) : getResources().getDrawable(
                R.drawable.pref_switch_bar_disabled));
        if (mSwitchedOn && animatedOn) { //the animatedOn varible is a work around for fixing a triky bug, that is, the button circle disappears everytime after screen lock
            return;
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) prefSwitchBtn.getLayoutParams();
        layoutParams.gravity = mSwitchedOn ? Gravity.RIGHT : Gravity.LEFT;
        prefSwitchBtn.setLayoutParams(layoutParams);
    }

    public interface ISwitchPreferenceToggleListener{
        void onToggleSwitch(boolean isSwitchOn);
    }

}