package com.ihs.inputmethod.uimodules.listeners;

import android.content.Context;
import android.content.res.Resources;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;

import com.ihs.inputmethod.api.framework.HSInputMethod;

import java.util.concurrent.TimeUnit;

public class DeleteKeyOnTouchListener implements View.OnTouchListener {

	static final long MAX_REPEAT_COUNT_TIME = TimeUnit.SECONDS.toMillis(30);
	final long mKeyRepeatStartTimeout;
	final long mKeyRepeatInterval;

	public DeleteKeyOnTouchListener(Context context) {
		final Resources res = context.getResources();
		mKeyRepeatStartTimeout = res.getInteger(com.ihs.inputmethod.R.integer.config_key_repeat_start_timeout);
		mKeyRepeatInterval = res.getInteger(com.ihs.inputmethod.R.integer.config_key_repeat_interval);
		mTimer = new CountDownTimer(MAX_REPEAT_COUNT_TIME, mKeyRepeatInterval) {
			@Override
			public void onTick(long millisUntilFinished) {
				final long elapsed = MAX_REPEAT_COUNT_TIME - millisUntilFinished;
				if (elapsed < mKeyRepeatStartTimeout) {
					return;
				}
				onKeyRepeat();
			}
			@Override
			public void onFinish() {
				onKeyRepeat();
			}
		};
	}

	/** Key-repeat state. */
	private static final int KEY_REPEAT_STATE_INITIALIZED = 0;
	// The key is touched but auto key-repeat is not started yet.
	private static final int KEY_REPEAT_STATE_KEY_DOWN = 1;
	// At least one key-repeat event has already been triggered and the key is not released.
	private static final int KEY_REPEAT_STATE_KEY_REPEAT = 2;

	private final CountDownTimer mTimer;
	private int mState = KEY_REPEAT_STATE_INITIALIZED;
	private int mRepeatCount = 0;

	@Override
	public boolean onTouch(final View v, final MotionEvent event) {
		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				onTouchDown(v);
				return true;
			case MotionEvent.ACTION_MOVE:
				final float x = event.getX();
				final float y = event.getY();
				if (x < 0.0f || v.getWidth() < x || y < 0.0f || v.getHeight() < y) {
					// Stop generating key events once the finger moves away from the view area.
					onTouchCanceled(v);
					return false;
				}
				return true;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				onTouchUp(v);
				return true;
		}
		return false;
	}

	private void handleKeyDown() {}

	private void handleKeyUp() {
		HSInputMethod.deleteBackward();
		++mRepeatCount;
	}

	private void onTouchDown(final View v) {
		mTimer.cancel();
		mRepeatCount = 0;
		handleKeyDown();
		v.setPressed(true /* pressed */);
		mState = KEY_REPEAT_STATE_KEY_DOWN;
		mTimer.start();
	}

	private void onTouchUp(final View v) {
		mTimer.cancel();
		if (mState == KEY_REPEAT_STATE_KEY_DOWN) {
			handleKeyUp();
		}
		v.setPressed(false /* pressed */);
		mState = KEY_REPEAT_STATE_INITIALIZED;
	}

	private void onTouchCanceled(final View v) {
		mTimer.cancel();
		v.setPressed(false /* pressed */);
		mState = KEY_REPEAT_STATE_INITIALIZED;
	}

	// Called by {@link #mTimer} in the UI thread as an auto key-repeat signal.
	void onKeyRepeat() {
		switch (mState) {
			case KEY_REPEAT_STATE_INITIALIZED:
				// Basically this should not happen.
				break;
			case KEY_REPEAT_STATE_KEY_DOWN:
				// Do not call {@link #handleKeyDown} here because it has already been called
				// in {@link #onTouchDown}.
				handleKeyUp();
				mState = KEY_REPEAT_STATE_KEY_REPEAT;
				break;
			case KEY_REPEAT_STATE_KEY_REPEAT:
				handleKeyDown();
				handleKeyUp();
				break;
		}
	}
}