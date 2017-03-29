package com.ihs.inputmethod.uimodules.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.inputmethod.uimodules.R;


public final class CustomProgressDialog extends Dialog {
	private RotateAnimation animation;
	private ImageView iv;

	public CustomProgressDialog(Context context, int themeResId) {
		super(context, themeResId);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custom_progressdialog);
		if (iv == null) {
			iv = (ImageView) findViewById(R.id.custom_dialog_circle);
		}
		if (animation == null) {
			animation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			LinearInterpolator lin = new LinearInterpolator();
			animation.setDuration(500);
			animation.setRepeatCount(Animation.INFINITE);
			animation.setRepeatMode(Animation.RESTART);
			animation.setInterpolator(lin);
		}
		if (animation != null && iv != null) {
			iv.startAnimation(animation);
		}

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

	}
	public void setMessage(CharSequence message) {
		if (message != null && message.length() > 0) {
			TextView txt = (TextView) findViewById(R.id.custom_dialog_text);
			txt.setText(message);
			txt.invalidate();
		}
	}
	public static CustomProgressDialog show(Context context,String message){
		CustomProgressDialog dialog=new CustomProgressDialog(context,R.style.CustomProgressDialog);
		dialog.setTitle("");
		if(message!=null)
			dialog.setMessage(message);
		dialog.setCancelable(false);
		WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
		lp.gravity= Gravity.CENTER;
		dialog.getWindow().setAttributes(lp);
		dialog.show();
		return dialog;
	}
}