package com.ihs.inputmethod.uimodules.ui.emoji;

import android.content.Context;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.adpanel.KeyboardPanelAdManager;
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
import com.ihs.inputmethod.uimodules.ui.gif.common.control.UIController;
import com.ihs.inputmethod.uimodules.utils.RippleDrawableUtils;
import com.ihs.keyboardutils.giftad.GiftInterstitialHelper;

import java.util.ArrayList;
import java.util.List;


public class HSEmojiPanelView extends FrameLayout implements BaseTabViewAdapter.OnTabChangeListener,
		HSEmojiViewAdapter.OnEmojiClickListener,HSEmojiViewAdapter.OnEmojiLongPressListener ,HSEmojiSkinViewAdapter.OnEmojiClickListener {


    private HSEmojiTabAdapter tabAdapter;
	private RecyclerView emojiView;
	private HSEmojiViewAdapter emojiAdapter;

	private RecyclerView emojiCategoryView;
    private final EmojiCategory emojiCategory;
    private KeyboardPanelAdManager keyboardPanelAdManager;

	//emoji skin support
	HSEmojiSkinViewAdapter skinViewAdapter;
	View skinView;

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
				HSAnalytics.logEvent("Keyboard_EmojiTopLeftTabAd_show");
                imageView.setVisibility(View.VISIBLE);
				keyboardPanelAdManager.hasShowedAd();
				imageView.setBackgroundDrawable(RippleDrawableUtils.getTransparentRippleBackground());
				imageView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						HSAnalytics.logEvent("Keyboard_EmojiTopLeftTabAd_click");
						GiftInterstitialHelper.showInterstitialGiftAd(HSApplication.getContext().getString(R.string.ad_placement_gift_ad));
					}
				});
            }

			emojiCategoryView = (RecyclerView) findViewById(R.id.image_category_tabhost);
			emojiCategoryView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
			emojiCategoryView.setAdapter(tabAdapter);
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
		LinearLayout.LayoutParams flp = (LinearLayout.LayoutParams)emojiView.getLayoutParams();//new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,height)
		flp.height = height;
		
		RecyclerView.ItemAnimator animator = emojiView.getItemAnimator();
		if (animator instanceof SimpleItemAnimator) {
			((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
		}
	}


	@Override
	public void onTabChanged(final String nextTab) {
		HSLog.d("change to tab "+nextTab);
		hiddenSkinView();
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

		HSAnalytics.logEvent("emoji_input", "Value", key.getLabel());
	}

	@Override
	public void onEmojiSkinClick(Emoji key) {
		key.selected();
		emojiAdapter.selectedEmoji(key.getSuperEmoji());
		this.emojiView.invalidate();

		UIController.getInstance().getUIHandler().postDelayed(new Runnable() {
			@Override
			public void run() {
				hiddenSkinView();
			}
		},100);

		emojiCategory.pendingRecentEmoji(key);
		HSInputMethod.inputText(key.getLabel());

		HSAnalytics.logEvent("emoji_input", "Value", key.getLabel());
	}

	@Override
	public void onEmojiLongPress(Emoji emoji,View emojiTextView,int parentViewHeight ) {
		ViewStub skinStub = (ViewStub) findViewById(R.id.emoji_skin_import);
		if (skinStub  != null) {
			skinStub.setVisibility(View.VISIBLE);
		}

		skinView = findViewById(R.id.emoji_skin_layout);//LayoutInflater.from(getContext()).inflate(R.layout.panel_emoji_skin_view,null);

		final Resources res = getResources();
		final int height = HSResourceUtils.getDefaultKeyboardHeight(res)
				- res.getDimensionPixelSize(R.dimen.emoticon_panel_actionbar_height);

		final int width = HSResourceUtils.getDefaultKeyboardWidth(res);
		final int emojiCol = res.getInteger(R.integer.config_emoji_col_count);
		final int emojiWidth = (int) (width/(emojiCol+0.5f));

		RecyclerView emojiSkinView = (RecyclerView) skinView.findViewById(R.id.emoji_skin_container);
		skinView.setOnTouchListener(new OnTouchListener() {
          @Override
          public boolean onTouch(View v, MotionEvent event) {
              if (event.getAction() == MotionEvent.ACTION_DOWN) {

				  UIController.getInstance().getUIHandler().postDelayed(new Runnable() {
					  @Override
					  public void run() {
						  hiddenSkinView();
					  }
				  },100);

                  return true;
              }
              return false;
          }
      });
		skinViewAdapter = new HSEmojiSkinViewAdapter(emojiTextView.getMeasuredHeight() +  HSDisplayUtils.dip2px(5) ,emojiWidth,0.8f,this);
		skinViewAdapter.setHasStableIds(true);

		emojiSkinView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
		emojiSkinView.setAdapter(skinViewAdapter);

		int windowPos[] = calculateViewPositionInParentView(emojiTextView, skinView);
		windowPos[1] -=  HSDisplayUtils.dip2px(15);

		LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams)emojiSkinView.getLayoutParams();
		llp.width = LayoutParams.WRAP_CONTENT;
		llp.height = LayoutParams.WRAP_CONTENT;
		llp.topMargin = windowPos[1] - (parentViewHeight - emojiTextView.getMeasuredHeight()  );
		emojiSkinView.setLayoutParams(llp);
		skinViewAdapter.setData(emoji.getSkinItems());

		Animation animation = createShowAnimation(2,50);
		skinView.setVisibility(View.VISIBLE);
		skinView.setAnimation(animation);

	}

	public void showPanelView() {
		hiddenSkinView();
		setHardwareAcceleratedDrawingEnabled(HSInputMethod.isHardwareAcceleratedDrawingEnabled());
		updateTabsBeforeStart();
	}

	void onDataLoaded(){
		hiddenSkinView();
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
			hiddenSkinView();
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


	private static int[] calculateViewPositionInParentView(final View view , final View parentView) {
		final int resultLoc[] = new int[2];
		final int parentViewLoc[] = new int[2];
		final int viewLoc[] = new int[2];
		// 获取锚点View在屏幕上的左上角坐标位置
		view.getLocationOnScreen(viewLoc);
		parentView.getLocationOnScreen(parentViewLoc);

		resultLoc[0] = viewLoc[0] - parentViewLoc[0] ;
		resultLoc[1] = viewLoc[1] - parentViewLoc[1] ;
		return resultLoc;
	}

	private void hiddenSkinView() {
		if (this.skinView != null && this.skinView.getVisibility() == View.VISIBLE) {

			Animation animation = createHiddenAnimation(2,50);
			animation.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					skinView.setVisibility(View.GONE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}
			});
			skinView.startAnimation(animation);
		}
	}

	private Animation createShowAnimation(final float scaleRation, final int upDuration){

		final Animation scaleUp=new ScaleAnimation(
				1/scaleRation,1.0f,1,1.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1f
		);

		final AnimationSet set=new AnimationSet(false);
		scaleUp.setDuration(upDuration);
		scaleUp.setFillAfter(true);
		set.setDuration(upDuration);
		set.addAnimation(scaleUp);

		AlphaAnimation  alpha = new AlphaAnimation(0.5f, 1.0f);
		alpha.setFillAfter(true);
		alpha.setDuration(upDuration);
		set.addAnimation(alpha);
		return set;
	}

	private Animation createHiddenAnimation(final float scaleRation,   final int downDuration){

		final Animation scaleDown=new ScaleAnimation(
				1.0f,1/scaleRation,1.0f,1,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1.f
		);
		final AnimationSet set=new AnimationSet(false);
		scaleDown.setDuration(downDuration);
		scaleDown.setFillAfter(true);
		set.setDuration( downDuration);
		set.addAnimation(scaleDown);

		AlphaAnimation  alpha = new AlphaAnimation(1.0f, 0.5f);
		alpha.setFillAfter(true);
		alpha.setDuration(downDuration);
		set.addAnimation(alpha);
		return set;
	}
}
