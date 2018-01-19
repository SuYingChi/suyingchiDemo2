package com.ihs.inputmethod.view;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.analytics.ThemeAnalyticsReporter;

public class HomeBackgroundBannerView extends CardView {

	private Rect screenRect = new Rect();
	private int paddingLeft=-1;
	private int paddingRight=-1;
	{
		WindowManager wm = (WindowManager) HSApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
		Point p = new Point();
		wm.getDefaultDisplay().getSize(p);
		screenRect = new Rect(0, 0, p.x, p.y);
	}

	private final ViewTreeObserver.OnScrollChangedListener onScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
		@Override
		public void onScrollChanged() {
			onViewPositionChanged(getScreenVisibleRect());
		}
	};
	private boolean inScreen=false;

	public HomeBackgroundBannerView(Context context) {
		this(context,null);
	}

	public HomeBackgroundBannerView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public HomeBackgroundBannerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void setUpOnScrollChangedListener(){
		getViewTreeObserver().addOnScrollChangedListener(onScrollChangedListener);
	}

	public void shutDownOnScrollChangedListener(){
		getViewTreeObserver().removeOnScrollChangedListener(onScrollChangedListener);
	}

	private void onViewPositionChanged(Rect rectInScreen) {
		if(getTag()==null){
			return;
		}
		final boolean shown=!rectInScreen.isEmpty() && screenRect.contains(rectInScreen);
		if (!inScreen && shown){
			ThemeAnalyticsReporter.getInstance().recordBannerThemeShown(getTag().toString());
			shutDownOnScrollChangedListener();
		}
		inScreen=shown;
	}

	private Rect getScreenVisibleRect() {
		if(paddingLeft<0||paddingRight<0){
			paddingLeft=getResources().getDimensionPixelSize(R.dimen.theme_store_viewpager_padding_left);
			paddingRight=getResources().getDimensionPixelSize(R.dimen.theme_store_viewpager_padding_right);
		}
		int[] out = new int[2];
		getLocationOnScreen(out);
		int left = out[0] > 0 ? out[0] : 0;
		int top = out[1] > 0 ? out[1] : 0;
		int right = getMeasuredWidth() + out[0];
		right = right >= screenRect.right ? screenRect.right : right;
		int bottom = getMeasuredHeight() + out[1];
		bottom = bottom >= screenRect.bottom ? screenRect.bottom : bottom;

		if(right<=paddingLeft){
			right=0;
		}
		if(left<=paddingLeft){
			left=0;
		}

		if(left>=screenRect.right-paddingRight){
			left=screenRect.right;
		}
		if(right>=screenRect.right-paddingRight){
			right=screenRect.right;
		}
		return new Rect(left, top, right, bottom);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		shutDownOnScrollChangedListener();
	}
}
