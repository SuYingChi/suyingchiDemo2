package com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.ThemeHomeModel;
import com.ihs.inputmethod.uimodules.utils.ViewConvertor;
import com.ihs.keyboardutils.nativeads.NativeAdParams;
import com.ihs.keyboardutils.nativeads.NativeAdView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wenbinduan on 2016/12/22.
 */

public class ThemeAdAdapterDelegate extends AdapterDelegate<List<ThemeHomeModel>> {

	protected Map<String, View> nativeAdViewCached;
	protected List<View> nativeAdAlreadyLoadedList;

	protected List<Map<String, Object>> themeAdInfos;

	protected int width;
	protected Handler handler = new Handler() {
		/**
		 * Subclasses must implement this to receive messages.
		 *
		 * @param msg
		 */
		@Override
		public void handleMessage(Message msg) {
            NativeAdView nativeAdView = (NativeAdView) msg.obj;
            if (null != nativeAdView) {
                TextView adButtonView = (TextView) nativeAdView.findViewById(R.id.ad_call_to_action);
				adButtonView.getBackground().setColorFilter(HSApplication.getContext().getResources().getColor(R.color.ad_button_green_state), PorterDuff.Mode.SRC_ATOP);
            } else {
                // do nothing
            }
        }
    };

	private final INotificationObserver notificationObserver = new INotificationObserver() {
		@Override
		public void onReceive(String s, HSBundle hsBundle) {
			if (ThemeHomeFragment.NOTIFICATION_THEME_HOME_DESTROY.equals(s)) {
				release();
				HSGlobalNotificationCenter.removeObserver(notificationObserver);
			}
		}
	};

	public void release() {
		for(Map.Entry<String, View> entry : nativeAdViewCached.entrySet()) {
			if(entry.getValue() instanceof NativeAdView) {
				((NativeAdView)entry.getValue()).release();
			}
		}
	}

	public ThemeAdAdapterDelegate() {
		themeAdInfos = (List<Map<String, Object>>) HSConfig.getList("Application", "NativeAds", "NativeAdPosition", "ThemeAd");
		nativeAdViewCached = new HashMap<>();
		nativeAdAlreadyLoadedList = new ArrayList<>();
		HSGlobalNotificationCenter.addObserver(ThemeHomeFragment.NOTIFICATION_THEME_HOME_DESTROY, notificationObserver);
	}

	@Override
	protected boolean isForViewType(@NonNull List<ThemeHomeModel> items, int position) {
		return items.get(position).span == 2 && items.get(position).isAd;
	}

	@NonNull
	@Override
	protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
		width = parent.getMeasuredWidth() - parent.getPaddingLeft() - parent.getPaddingRight();
		CardView cardView = ViewConvertor.getCardView();
		FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) cardView.getLayoutParams();
		int margin = HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.theme_card_recycler_view_card_margin);
		layoutParams.setMargins(margin, margin, margin, margin);
		cardView.setLayoutParams(layoutParams);
		HSLog.e("CardWidth:" + cardView.getWidth());
		return new ThemeAdViewHolder(cardView);
	}

	@Override
	protected void onBindViewHolder(@NonNull List<ThemeHomeModel> items, final int position, @NonNull RecyclerView.ViewHolder holder) {

		removeNativeAdViewFromHolder(holder);

		CardView cardView = (CardView) holder.itemView;
		String nativeAd = getNativeAd(position);
		if(nativeAdViewCached.get(nativeAd) == null) {
			View view = LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ad_style_2, null);
			LinearLayout loadingView = (LinearLayout) LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ad_loading_3, null);
			LinearLayout.LayoutParams loadingLP = new LinearLayout.LayoutParams(width, (int) (width / 1.9f) + HSDisplayUtils.dip2px(65));
			loadingView.setLayoutParams(loadingLP);
			loadingView.setGravity(Gravity.CENTER);
			NativeAdView nativeAdView = new NativeAdView(HSApplication.getContext(), view, loadingView);
			nativeAdView.setTag("nativeadview");
			nativeAdView.setOnAdLoadedListener(new NativeAdView.OnAdLoadedListener() {
				@Override
				public void onAdLoaded(NativeAdView nativeAdView) {
					if (!nativeAdAlreadyLoadedList.contains(nativeAdView)) {
						nativeAdAlreadyLoadedList.add(nativeAdView);
					} else {
						// do nothing
					}
					Message message = Message.obtain();
					message.what = position;
					message.obj = nativeAdView;
					handler.sendMessageDelayed(message, 1500);
				}
			});
			nativeAdView.configParams(new NativeAdParams(nativeAd, width, 1.9f));
			cardView.addView(nativeAdView);

			nativeAdViewCached.put(nativeAd, nativeAdView);
		} else {
			ViewGroup parent = ((ViewGroup) nativeAdViewCached.get(nativeAd).getParent());
			if (parent != null) {
				parent.removeView(nativeAdViewCached.get(nativeAd));
			}
			cardView.addView(nativeAdViewCached.get(nativeAd));
		}
	}

	@Override
	protected void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {

		if (nativeAdViewCached.containsKey(getNativeAd(holder.getAdapterPosition()))) {
			NativeAdView nativeAdView = (NativeAdView) nativeAdViewCached.get(getNativeAd(holder.getAdapterPosition()));
			if (nativeAdAlreadyLoadedList.contains(nativeAdView)) {
                TextView adButtonView = (TextView) nativeAdView.findViewById(R.id.ad_call_to_action);
				adButtonView.getBackground().setColorFilter(HSApplication.getContext().getResources().getColor(R.color.ad_button_blue), PorterDuff.Mode.SRC_ATOP);
				Message message = Message.obtain();
				message.what = holder.getAdapterPosition();
				message.obj = nativeAdView;
				handler.sendMessageDelayed(message, 1500);
			} else {
				// do nothing
			}
		} else {
			// do nothing
		}
		super.onViewAttachedToWindow(holder);
	}

	@Override
	protected void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
		handler.removeMessages(holder.getAdapterPosition());
		super.onViewDetachedFromWindow(holder);
	}

	private void removeNativeAdViewFromHolder(final RecyclerView.ViewHolder holder) {
		View nativeAdView = holder.itemView.findViewWithTag("nativeadview");
		if(nativeAdView != null) {
			((ViewGroup)holder.itemView).removeView(nativeAdView);
		}
	}

	@Override
	public int getSpanSize(List<ThemeHomeModel> items, int position) {
		return 2;
	}

	protected String getNativeAd(int itemPosition) {
		for(Map<String, Object> item : themeAdInfos) {
			if((int)item.get("Position") == itemPosition - 4) {
				return (String) item.get("NativeAd");
			}
		}
		for(Map<String, Object> item : themeAdInfos) {
			if((int)item.get("Position") == 10000) {
				return (String) item.get("NativeAd");
			}
		}

		return null;
	}
}
