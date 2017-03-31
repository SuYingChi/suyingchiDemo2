package com.ihs.inputmethod.uimodules;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.constants.Constants;
import com.ihs.inputmethod.uimodules.constants.Notification;
import com.ihs.panelcontainer.BasePanel;

import java.util.HashMap;
import java.util.Map;

public class ControlPanelView extends BaseControlPanelView implements View.OnClickListener {



	private static final int INDICATOR_DOT_ANIMATION_DURATION = 100;
	private static final float BUTTON_STRIP_LAYOUT_MARGIN_TOP_RATIO = 0.2f;
	private static final float BUTTON_STRIP_LAYOUT_MARGIN_BOTTOM_RATIO = 0.3f;
	private static int BUTTON_STRIP_LAYOUT_BUTTON_MARGIN_LEFT;

	// buttons strip
    private LinearLayout mButtonStrip;

	// supplementary buttons strip
	private LinearLayout mSupplementaryButtonStrip;

	private Map<View, String> mAddedTabButtonMap;
	private Map<View, String> mAddedSupplementaryButtonMap;
	private Map<String, View> mAddedExtensionView;
	private Map<String, BasePanel> mAddedPanelMap;

	private int mIndicatorDotCurrentX;
	private int mIndicatorDotDefaultX;
	private int mIndicatorDotRadius;
	private View mIndicatorDot;

	private int mTotalHeight;
	private int mButtonWidth;
	private int mButtonHeight;


	public ControlPanelView(Context context) {
		this(context, null);
	}

	public ControlPanelView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ControlPanelView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mAddedTabButtonMap = new HashMap<>();
		mAddedExtensionView = new HashMap<>();
		mAddedPanelMap = new HashMap<>();
		mAddedSupplementaryButtonMap=new HashMap<>();
		HSGlobalNotificationCenter.addObserver(Notification.SHOW_CONTROL_PANEL_VIEW, mImeActionObserver);
		HSGlobalNotificationCenter.addObserver(Notification.SERVICE_START_INPUT_VIEW, mImeActionObserver);
		calcLayoutDimension();
	}

	public void addButton(final String panelName, final View view) {
		if (view == null) {
			return;
		}

		// Button layout params
		final LayoutParams llp = new LayoutParams(mButtonWidth, mButtonHeight);
		llp.leftMargin = BUTTON_STRIP_LAYOUT_BUTTON_MARGIN_LEFT;
		llp.rightMargin = BUTTON_STRIP_LAYOUT_BUTTON_MARGIN_LEFT;
		llp.topMargin = (int) (mTotalHeight * BUTTON_STRIP_LAYOUT_MARGIN_TOP_RATIO);

		// Scale ABC button
		if (panelName.equals(Constants.PANEL_NAME_KEYBOARD)) {
			final float r = ((Float)(view.getTag())).floatValue();
			llp.width = (int) (mButtonHeight * r * 0.5f);
			llp.height = (int) (mButtonHeight * 0.5f);
			llp.topMargin = llp.topMargin + mButtonHeight / 4;

			// Layout dot by ABC button width
			mIndicatorDotDefaultX = BUTTON_STRIP_LAYOUT_BUTTON_MARGIN_LEFT + llp.width / 2 - mIndicatorDotRadius;
			HSLog.d(mIndicatorDotDefaultX + "/" + BUTTON_STRIP_LAYOUT_BUTTON_MARGIN_LEFT + "/" + llp.width / 2 + "/" + mIndicatorDotRadius);

			final RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mIndicatorDot.getLayoutParams();
			lp.leftMargin = mIndicatorDotDefaultX;
			mIndicatorDotCurrentX = mIndicatorDotDefaultX;
			mIndicatorDot.requestLayout();
		}

		// Listener and layout
		view.setLayoutParams(llp);

		// Wrap parent layout as button region
		final LinearLayout buttonLayout = new LinearLayout(HSApplication.getContext());
		final LayoutParams btnLayoutParam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		buttonLayout.setLayoutParams(btnLayoutParam);
		buttonLayout.addView(view);
		buttonLayout.setOnClickListener(this);
		buttonLayout.setSoundEffectsEnabled(false);

		// Add button to strip
		mButtonStrip.addView(buttonLayout);

		// Add button to map
		mAddedTabButtonMap.put(buttonLayout, panelName);
	}


	public void onPanelShow(final BasePanel panel){

		/**
		 * Double check if dot is on right position.
		 * Such as back to ABC by other paths
		 */
//		doIndicatorDotAnimation(panel);
	}


	public void addSupplementaryButton(final String panelName, final View view) {
		if (view == null) {
			return;
		}

		view.setOnClickListener(this);
		view.setSoundEffectsEnabled(false);
		mAddedSupplementaryButtonMap.put(view, panelName);
		mAddedExtensionView.put(panelName, view);
		mSupplementaryButtonStrip.addView(view);
	}

	private void calcLayoutDimension() {
		// Margin left of buttons
		BUTTON_STRIP_LAYOUT_BUTTON_MARGIN_LEFT = HSApplication.getContext().getResources().getDimensionPixelOffset(R.dimen.control_button_strip_button_margin_left);

		// Total height
		mTotalHeight = getResources().getDimensionPixelSize(R.dimen.config_suggestions_strip_height);

		// Default button w/h
		final int BUTTON_HEIGHT = (int) (mTotalHeight * (1 - BUTTON_STRIP_LAYOUT_MARGIN_TOP_RATIO - BUTTON_STRIP_LAYOUT_MARGIN_BOTTOM_RATIO));
		mButtonWidth = BUTTON_HEIGHT;
		mButtonHeight = BUTTON_HEIGHT;

		// Dot radius
		mIndicatorDotRadius = (int) (mTotalHeight * BUTTON_STRIP_LAYOUT_MARGIN_BOTTOM_RATIO) / 6;
	}


	public void doIndicatorDotAnimation(final View button,final int duration) {
		if (button.getMeasuredWidth() == 0) {
			return;
		}

		final int currentDotX = mIndicatorDotCurrentX;
        final int targetDotX = (int) button.getX() + button.getMeasuredWidth() / 2 - mIndicatorDotRadius;

		HSLog.d(currentDotX + " to " + targetDotX + ", duration: " + duration);
		HSLog.d(button.getMeasuredWidth() + "/" + button.getWidth());

		if (currentDotX == targetDotX) {
			HSLog.d("No need to do animation");
			return;
		}

        final Animation.AnimationListener listener = new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
				mIndicatorDotCurrentX = targetDotX;
                HSLog.d(mIndicatorDotCurrentX + "/");
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        };

        final Animation anim = new TranslateAnimation(currentDotX - mIndicatorDotDefaultX, targetDotX - mIndicatorDotDefaultX, 0, 0);
        anim.setDuration(duration);
        anim.setFillAfter(true);
		anim.setAnimationListener(listener);
		post(new Runnable() {
			@Override
			public void run() {
				mIndicatorDot.startAnimation(anim);
			}
		});
    }

	private View getTabBtn(final String panelName){
		for(final View btn:mAddedTabButtonMap.keySet()){
			if(mAddedTabButtonMap.get(btn)!=null&&panelName.equals(mAddedTabButtonMap.get(btn))){
				return btn;
			}
		}
		return null;
	}

	private BasePanel getPanel(final String panelName) {
		return mAddedPanelMap.get(panelName);
	}

	private void logEvent() {
//		BasePanel panel = KeyboardPluginManager.getInstance().getPanel("gif");
//		if(panel!=null){
//			View panelView=panel.getPanelView();
//			if(panelView!=null){
//				Object tag=panelView.getTag();
//				if(tag!=null&&!tag.equals("")&&!tag.equals("GIF")){
//					HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("keyboard_gif_entry_suggestion_clicked",tag+"");
//					HSLogManager.saveSuggestionHint(tag.toString(),1,0);
//				}else{
//					HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("keyboard_gif_entry_clicked");
//				}
//			}
//		}
	}

	private void moveIndicatorDotToButton(final View button) {
		if (button == null) {
			return;
		}

		mIndicatorDotCurrentX = (int) button.getX() + button.getMeasuredWidth() / 2 - mIndicatorDotRadius;
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final Resources res = getResources();
		final int width = HSResourceUtils.getDefaultKeyboardWidth(res);
		final int height =  res.getDimensionPixelSize(R.dimen.config_suggestions_strip_height);
		setMeasuredDimension(width, height);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		// buttons strip
		mButtonStrip = (LinearLayout) findViewById(R.id.control_panel_button_strip);

		// dot
		mIndicatorDot = findViewById(R.id.control_panel_button_indicator_dot);
		final RelativeLayout.LayoutParams dotLp = (RelativeLayout.LayoutParams) mIndicatorDot.getLayoutParams();
		dotLp.bottomMargin = mIndicatorDotRadius * 2;
		dotLp.width = mIndicatorDotRadius * 2;
		dotLp.height = mIndicatorDotRadius * 2;

		// supplementary buttons strip
		mSupplementaryButtonStrip = (LinearLayout) findViewById(R.id.control_panel_supplementary_button_strip);
	}

	@Override
	public void onClick(View view) {
//		// We click internal image button, find parent layout
//		if (view instanceof ImageButton) {
//			for (View v : mAddedTabButtonMap.keySet()) {
//				if (((LinearLayout) v).getChildAt(0) == view) {
//					view = v;
//					break;
//				}
//			}
//		}
//
//		String panelName = mAddedTabButtonMap.get(view);
//		if (panelName == null) {
//			panelName = mAddedSupplementaryButtonMap.get(view);
//		}
//
//		if (panelName != null) {
//			showPanelByButton(panelName, view);
//
//			if (shouldLogKeyboardTabChosedEvent(panelName)) {
//				HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(HSGoogleAnalyticsConstants.GA_PARAM_ACTION_KEYBOARD_TAB_CHOSED, panelName);
//			}
//		}
	}

	@Override
	public void onStartInputView() {
		final View btn = getTabBtn(Constants.PANEL_NAME_KEYBOARD);

		if (btn != null) {
			doIndicatorDotAnimation(btn, 0);
		}
	}


	private void measureIndicatorDot(final int stripHeight) {
		// Dot radius
		mIndicatorDotRadius = (int)(stripHeight * BUTTON_STRIP_LAYOUT_MARGIN_BOTTOM_RATIO) / 6;

		// Dot layout params
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mIndicatorDot.getLayoutParams();
		lp.bottomMargin = mIndicatorDotRadius * 2;
		lp.width = mIndicatorDotRadius * 2;
		lp.height = mIndicatorDotRadius * 2;

		if (mButtonStrip.getChildCount() > 0) {
			mIndicatorDotDefaultX = BUTTON_STRIP_LAYOUT_BUTTON_MARGIN_LEFT + mButtonStrip.getChildAt(0).getLayoutParams().width / 2 - mIndicatorDotRadius;
			lp = (RelativeLayout.LayoutParams) mIndicatorDot.getLayoutParams();
			lp.leftMargin = mIndicatorDotDefaultX;
			HSLog.d("mIndicatorDotDefaultX " + lp.leftMargin);
			mIndicatorDot.requestLayout();

			for (int i = 0; i < mButtonStrip.getChildCount(); ++i) {
				int w = mButtonStrip.getChildAt(i).getMeasuredWidth();
				HSLog.d("button width: " + w + "/"+mButtonStrip.getChildAt(i).getLayoutParams().width);
			}
		}
	}

	private void measureButtonStrip(final int stripHeight) {
		// Button strip layout margin top/bottom
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)mButtonStrip.getLayoutParams();
		lp.topMargin = (int) (stripHeight * BUTTON_STRIP_LAYOUT_MARGIN_TOP_RATIO);
		lp.bottomMargin = (int) (stripHeight * BUTTON_STRIP_LAYOUT_MARGIN_BOTTOM_RATIO);

		// Default button w/h
		final int BUTTON_HEIGHT = (int) (stripHeight * (1 - BUTTON_STRIP_LAYOUT_MARGIN_TOP_RATIO - BUTTON_STRIP_LAYOUT_MARGIN_BOTTOM_RATIO));
		int buttonWidth = BUTTON_HEIGHT;
		int buttonHeight = BUTTON_HEIGHT;

		for (int i = 0; i < mButtonStrip.getChildCount(); ++i) {
			final View button = mButtonStrip.getChildAt(i);

			if (i == 0) {
				if (button.getTag() != null) {
					final Object obj = button.getTag();
					if (obj instanceof Float) {
						final float r = ((Float) obj).floatValue();
						buttonWidth = (int) (BUTTON_HEIGHT * r * 0.5f);
						buttonHeight = (int) (BUTTON_HEIGHT * 0.5f);
					}
				}
			} else {
				buttonWidth = BUTTON_HEIGHT;
				buttonHeight = BUTTON_HEIGHT;
			}

			final LayoutParams llp = new LayoutParams(buttonWidth, buttonHeight);
			llp.leftMargin = BUTTON_STRIP_LAYOUT_BUTTON_MARGIN_LEFT;

			if (i == 0) {
				llp.gravity = Gravity.CENTER_VERTICAL;
			}

			button.setLayoutParams(llp);
		}
	}


	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		HSGlobalNotificationCenter.removeObserver(mImeActionObserver);
	}

	public void onDestoryService() {
		HSGlobalNotificationCenter.removeObserver(mImeActionObserver);
	}

	private void showExtensionView(final String panelName) {
		// Hide all first
		for (View view : mAddedExtensionView.values()) {
			view.setVisibility(View.GONE);
		}

		// Show target
		final View extensionView = mAddedExtensionView.get(panelName);
		if (extensionView != null) {
			extensionView.setVisibility(View.VISIBLE);
		}
	}

	private boolean shouldLogKeyboardTabChosedEvent(final String panelName) {
		return true;
	}

	private boolean shouldShowControlStripView(final BasePanel panel) {

		return false;
	}

	private boolean shouldDoIndicatorDotAnimation(final BasePanel panel) {
		return true;
	}
}