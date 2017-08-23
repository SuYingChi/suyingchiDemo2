package com.ihs.inputmethod.uimodules.ui.emoji;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.adpanel.KeyboardPanelAdManager;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsConstants;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.framework.HSEmojiSuggestionManager;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.BaseTabViewAdapter;
import com.ihs.inputmethod.uimodules.ui.common.adapter.HSEmojiTabAdapter;
import com.ihs.inputmethod.uimodules.ui.common.adapter.HSEmojiViewAdapter;
import com.ihs.inputmethod.uimodules.ui.common.model.Emoji;
import com.ihs.inputmethod.uimodules.ui.emoji.Skin.HSEmojiSkinViewAdapter;
import com.ihs.inputmethod.uimodules.utils.RippleDrawableUtils;
import com.ihs.keyboardutils.giftad.GiftInterstitialHelper;
import com.ihs.keyboardutils.utils.KCAnalyticUtil;

import java.util.ArrayList;
import java.util.List;


public class HSEmojiPanelView extends LinearLayout implements BaseTabViewAdapter.OnTabChangeListener,
		HSEmojiViewAdapter.OnEmojiClickListener,HSEmojiViewAdapter.OnEmojiLongPressListener ,HSEmojiSkinViewAdapter.OnEmojiClickListener {


    private HSEmojiTabAdapter tabAdapter;
	private RecyclerView emojiView;
	private HSEmojiViewAdapter emojiAdapter;

    private final EmojiCategory emojiCategory;
    private KeyboardPanelAdManager keyboardPanelAdManager;

	//emoji skin support
	HSEmojiSkinViewAdapter skinViewAdapter;

	public HSEmojiPanelView(Context context) {
		this(context,null);
	}

	public HSEmojiPanelView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HSEmojiPanelView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);

		final Resources res = context.getResources();

		emojiCategory = new EmojiCategory(PreferenceManager.getDefaultSharedPreferences(context), res,this);

		this.setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
		final int height = HSResourceUtils.getDefaultKeyboardHeight(res)
				+ res.getDimensionPixelSize(R.dimen.config_suggestions_strip_height)
				- res.getDimensionPixelSize(R.dimen.emoticon_panel_actionbar_height);
		setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
		keyboardPanelAdManager = new KeyboardPanelAdManager("EmojiTopLeftTabAd");
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		if(tabAdapter==null){
			List<String> tabs=new ArrayList<>();
			tabs.addAll(emojiCategory.getTabs());
			tabAdapter=new HSEmojiTabAdapter(tabs,this);
			ImageView imageView = (ImageView) findViewById(R.id.emoji_ad_container);
            if (keyboardPanelAdManager.isShowAdConditionSatisfied()) {
				KCAnalyticUtil.logEvent("Keyboard_EmojiTopLeftTabAd_show");
                imageView.setVisibility(View.VISIBLE);
				keyboardPanelAdManager.hasShowedAd();
				imageView.setBackgroundDrawable(RippleDrawableUtils.getTransparentRippleBackground());
				imageView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						KCAnalyticUtil.logEvent("Keyboard_EmojiTopLeftTabAd_click");
						GiftInterstitialHelper.showInterstitialGiftAd(HSApplication.getContext().getString(R.string.ad_placement_gift_ad));
					}
				});
            }

			RecyclerView mTabHost = (RecyclerView) findViewById(R.id.image_category_tabhost);
			mTabHost.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
			mTabHost.setAdapter(tabAdapter);
		}

		final Resources res=getResources();
		final int height = HSResourceUtils.getDefaultKeyboardHeight(res)
				- res.getDimensionPixelSize(R.dimen.emoticon_panel_actionbar_height);

		final int width=HSResourceUtils.getDefaultKeyboardWidth(res);
		final int emojiCol=res.getInteger(R.integer.config_emoji_col_count);
		final int emojiRow=res.getInteger(R.integer.config_emoji_row_count);
		final int emojiHeight=height/emojiRow;
		final int emojiWidth= (int) (width/(emojiCol+0.5f));

		emojiView = (RecyclerView) findViewById(R.id.emoji_keyboard_pager);
		emojiAdapter =new HSEmojiViewAdapter(emojiHeight,emojiWidth,0.6f,this);
		emojiAdapter.setLongPressListener(this);

		emojiAdapter.setHasStableIds(true);
		emojiView.setLayoutManager(new StaggeredGridLayoutManager(emojiRow,StaggeredGridLayoutManager.HORIZONTAL));
		emojiView.setAdapter(emojiAdapter);
		emojiView.addOnScrollListener(new ScrollListener());
		emojiView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,height));
		
		RecyclerView.ItemAnimator animator = emojiView.getItemAnimator();
		if (animator instanceof SimpleItemAnimator) {
			((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
		}
	}


	@Override
	public void onTabChanged(final String nextTab) {
		HSLog.d("change to tab "+nextTab);
		emojiCategory.setCurrentTabName(nextTab);
		if (emojiCategory.isRecentTab(nextTab)&&emojiCategory.hasPendingRecent()) {
			emojiCategory.flushPendingRecentEmoji();
			emojiAdapter.setData(emojiCategory.getSortEmoji());
			setCurrentItemPosition(0,0);
		}else{
			Pair<Integer,Integer> position=emojiCategory.getLastShownItemPositionForTab(nextTab);
			setCurrentItemPosition(position.first,position.second);
		}
	}

	@Override
	public void onEmojiClick(final Emoji key) {
		emojiCategory.pendingRecentEmoji(key);
		HSInputMethod.inputText(key.getLabel());

		HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent(HSGoogleAnalyticsConstants.GA_PARAM_ACTION_EMOJI_INPUT, key.getLabel());
		HSAnalytics.logEvent("emoji_input", "Value", key.getLabel());
	}

	@Override
	public void onEmojiLongPress(Emoji emoji,View emojiTextView){

		View contentView = LayoutInflater.from(getContext()).inflate(R.layout.panel_emoji_skin_view,
				null);
		final Resources res = getResources();
		final int height = HSResourceUtils.getDefaultKeyboardHeight(res)
				- res.getDimensionPixelSize(R.dimen.emoticon_panel_actionbar_height);

		final int width = HSResourceUtils.getDefaultKeyboardWidth(res);
		final int emojiCol = res.getInteger(R.integer.config_emoji_col_count);
		final int emojiRow = res.getInteger(R.integer.config_emoji_row_count);
		final int emojiHeight = height/emojiRow;
		final int emojiWidth = (int) (width/(emojiCol+0.5f));

		RecyclerView emojiSkinView = (RecyclerView) contentView.findViewById(R.id.emoji_skin_container);

		skinViewAdapter = new HSEmojiSkinViewAdapter(emojiTextView.getMeasuredHeight(),emojiWidth,0.8f,this);
		skinViewAdapter.setHasStableIds(true);
		emojiSkinView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
		emojiSkinView.setAdapter(skinViewAdapter);

		emojiSkinView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));

		int skinPanelWidth = emojiWidth *  emoji.getSkinItems().size()  ;
		PopupWindow mPopBottom = new  PopupWindow(contentView,
				skinPanelWidth, ViewGroup.LayoutParams.WRAP_CONTENT, true);
//      mPopBottom.setTouchInterceptor(new OnTouchListener() {
//          @Override
//          public boolean onTouch(View v, MotionEvent event) {
//              if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
//                  mPopBottom.dismiss();
//                  return true;
//              }
//              return false;
//          }
//      });
		mPopBottom.setBackgroundDrawable(new ColorDrawable());


		mPopBottom.setWidth(skinPanelWidth);
		mPopBottom.setHeight(emojiTextView.getMeasuredHeight() );
		mPopBottom.setTouchable(true);
		mPopBottom.setFocusable(true);
		mPopBottom.setOutsideTouchable(true);
		int windowPos[] = calculatePopWindowPos(emojiTextView, contentView);
		windowPos[1] -=  HSDisplayUtils.dip2px(15);
		mPopBottom.showAtLocation(emojiTextView,  Gravity.TOP | Gravity.START, windowPos[0], windowPos[1]);

		skinViewAdapter.setData(emoji.getSkinItems());

	}



	public void showPanelView() {
		setHardwareAcceleratedDrawingEnabled(HSInputMethod.isHardwareAcceleratedDrawingEnabled());
		updateTabsBeforeStart();
//		emojiAdapter.setData(emojiCategory.getSortEmoji());
//		tabAdapter.setCurrentTab(emojiCategory.getCurrentTabName(),emojiCategory.getDefaultTab());
	}

	void onDataLoaded(){
		emojiAdapter.setData(emojiCategory.getSortEmoji());
		tabAdapter.setCurrentTab(emojiCategory.getCurrentTabName(),emojiCategory.getDefaultTab());
	}

	public void saveRecent() {
		emojiCategory.saveRecent();
	}


	private void setHardwareAcceleratedDrawingEnabled(final boolean enabled) {
		if (!enabled)
			return;
		// TODO: Should use LAYER_TYPE_SOFTWARE when hardware acceleration is off?
		setLayerType(LAYER_TYPE_HARDWARE, null);
	}

	private void setCurrentItemPosition(final int position, final int offset) {
		if(emojiView==null){
			return;
		}
		((StaggeredGridLayoutManager) emojiView.getLayoutManager()).scrollToPositionWithOffset(position, offset);
	}

	/**
	 * Add or remove suggestion category on start
	 */
	private void updateTabsBeforeStart() {

		final boolean hasSuggestion = HSEmojiSuggestionManager.hasFollowEmojiForTypedWords();

		if (hasSuggestion) {
			addSuggestionTab();
			emojiCategory.setCurrentTabName(EmojiCategory.SUGGESTION);
		}

		//because panel are new created every time
		emojiCategory.loadDataAsync();
	}

	private void addSuggestionTab() {
		emojiCategory.addSuggestionTab();
		tabAdapter.addTab(1,EmojiCategory.SUGGESTION);
	}

	private final class ScrollListener extends OnScrollListener{
		@Override
		public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
			super.onScrolled(recyclerView, dx, dy);
			if(dx==0){
				return;
			}
			StaggeredGridLayoutManager layoutManager= (StaggeredGridLayoutManager) emojiView.getLayoutManager();
			int[] into=new int[getResources().getInteger(R.integer.config_emoji_row_count)];
			layoutManager.findFirstCompletelyVisibleItemPositions(into);
			String tab = emojiCategory.getTabNameForPosition(into[0]);
			if (emojiCategory.getSortEmoji().get(into[0]).isDivider()) {
				int emojiRow = getResources().getInteger(R.integer.config_emoji_row_count);
				tab = emojiCategory.getTabNameForPosition(into[0] + emojiRow);
			}
			if(!tab.equals(emojiCategory.getCurrentTabName())){
				emojiCategory.setCurrentTabName(tab);
				tabAdapter.setTabSelected(tab);
				if(emojiCategory.isRecentTab(tab)){
					emojiCategory.flushPendingRecentEmoji();
					emojiAdapter.setData(emojiCategory.getSortEmoji());
				}
			}
		}

		@Override
		public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
			super.onScrollStateChanged(recyclerView, newState);
			if(newState==RecyclerView.SCROLL_STATE_IDLE){
//				StaggeredGridLayoutManager layoutManager= (StaggeredGridLayoutManager) emojiView.getLayoutManager();
//				int[] into=new int[getResources().getInteger(R.integer.config_emoji_row_count)];
//				layoutManager.findFirstVisibleItemPositions(into);
//				final int position=into[0];
//				final int offset=layoutManager.findViewByPosition(position).getLeft();
//				emojiCategory.saveLastShownItemPosition(position,offset);

				if(!emojiView.canScrollHorizontally(-1)
						&& !emojiCategory.isRecentTab(emojiCategory.getCurrentTabName())
						&& !emojiCategory.isRecentEmpty()){

					emojiCategory.flushPendingRecentEmoji();
					emojiAdapter.setData(emojiCategory.getSortEmoji());
					Pair<Integer,Integer> itemPosition=emojiCategory.getLastShownItemPositionForTab(emojiCategory.getCurrentTabName());
					setCurrentItemPosition(itemPosition.first,itemPosition.second);
				}
			}
		}
	}


	/**
	 * 计算出来的位置，y方向就在anchorView的上面和下面对齐显示，x方向就是与屏幕右边对齐显示
	 * 如果anchorView的位置有变化，就可以适当自己额外加入偏移来修正
	 * @param anchorView  呼出window的view
	 * @param contentView   window的内容布局
	 * @return window显示的左上角的xOff,yOff坐标
	 */
	private static int[] calculatePopWindowPos(final View anchorView, final View contentView) {
		final int windowPos[] = new int[2];
		final int anchorLoc[] = new int[2];
		// 获取锚点View在屏幕上的左上角坐标位置
		anchorView.getLocationOnScreen(anchorLoc);

		final int anchorHeight = anchorView.getMeasuredHeight();
		// 获取屏幕的高宽
		final int screenHeight =  getScreenHeight(anchorView.getContext());
		final int screenWidth =  getScreenWidth(anchorView.getContext());
		contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		// 计算contentView的高宽
		final int windowHeight = contentView.getMeasuredHeight();
		final int windowWidth = contentView.getMeasuredWidth();

		windowPos[0] = anchorLoc[0];
		windowPos[1] = anchorLoc[1]  - windowHeight - anchorView.getMeasuredHeight();
		return windowPos;
	}

	/**
	 * 获取屏幕高度(px)
	 */
	public static int getScreenHeight(Context context) {
		return context.getResources().getDisplayMetrics().heightPixels;
	}
	/**
	 * 获取屏幕宽度(px)
	 */
	public static int getScreenWidth(Context context) {
		return context.getResources().getDisplayMetrics().widthPixels;
	}
}
