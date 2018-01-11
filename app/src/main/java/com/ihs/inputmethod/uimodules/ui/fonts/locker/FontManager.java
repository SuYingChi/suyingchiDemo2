package com.ihs.inputmethod.uimodules.ui.fonts.locker;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacterManager;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.R;

public class FontManager {

	private boolean mShowing;
	private View mFontView;
	private FontSelectViewAdapter mAdapter;
	private static FontManager mInstance;

	private FontManager() {}

	private INotificationObserver loadDataObserver = new INotificationObserver() {
		@Override
		public void onReceive(String eventName, HSBundle notificaiton) {
			if (eventName.equals(HSSpecialCharacterManager.HS_NOTIFICATION_SPECIAL_CHARACTERS_LOAD_FINISHED)) {
				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}
			}
		}
	};

	private void addObservers() {
		HSGlobalNotificationCenter.addObserver(HSSpecialCharacterManager.HS_NOTIFICATION_SPECIAL_CHARACTERS_LOAD_FINISHED, loadDataObserver);
	}

	public static FontManager getInstance() {
		if (mInstance == null) {
			init();
		}
		return mInstance;
	}

	public void hideFontView() {
		if (mShowing) {
			final FrameLayout inputArea = HSInputMethod.getInputArea();
			if (inputArea != null) {
				inputArea.removeView(mFontView);
				mShowing = false;
				HSGlobalNotificationCenter.removeObserver(loadDataObserver);
			}
		}
	}

	public static void init() {
		if (mInstance == null) {
			mInstance = new FontManager();
		}

		// we must renew one to match port/land
		mInstance.initFontView();

		// add observer
	}

	private void initFontView() {
		final Context mThemeContext = new ContextThemeWrapper(HSApplication.getContext(), HSKeyboardThemeManager.getCurrentTheme().mStyleId);
		final LayoutInflater inflater = LayoutInflater.from(mThemeContext);
		mFontView = inflater.inflate(R.layout.locker_font_select_layout, null);
		final View closeButton = mFontView.findViewById(R.id.close);
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideFontView();
			}
		});

		final FontSelectView fontSelectView= mFontView.findViewById(R.id.font_select_listview);
		mAdapter = new FontSelectViewAdapter(HSApplication.getContext(), fontSelectView, null);
		fontSelectView.setAdapter(mAdapter);

		mFontView.findViewById(R.id.sticker_keyboard_top_divider).setBackgroundDrawable(HSKeyboardThemeManager.getStyledDrawable(null, HSKeyboardThemeManager.IMG_EMOJI_KEYBOARD_DIVIDER));
		mFontView.findViewById(R.id.font_ad_tell).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String ad = HSConfig.getString("Application", "ShareContents", "Keyboard", "ShareTexts", "ForFonts");
				if (ad.length() > 2)
					HSInputMethod.inputText(ad);
			}
		});
	}

// --Commented out by Inspection START (18/1/11 下午2:41):
//	public void onFinishInputView() {
//		hideFontView();
//	}
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//	public void onConfigurationChanged() {
//		hideFontView();
//	}
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//	public void onDestroyPanelView() {
//		HSGlobalNotificationCenter.removeObserver(loadDataObserver);
//	}
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//	public void showFontView() {
//		if (mShowing) {
//			return;
//		}
//		mInstance.addObservers();
//		mAdapter.notifyDataSetChanged();
//
//		final FrameLayout inputArea = HSInputMethod.getInputArea();
//		if (inputArea != null) {
//			final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
//			params.gravity = Gravity.BOTTOM;
//			inputArea.addView(mFontView, params);
//			mShowing = true;
//		}
//	}
// --Commented out by Inspection STOP (18/1/11 下午2:41)
}