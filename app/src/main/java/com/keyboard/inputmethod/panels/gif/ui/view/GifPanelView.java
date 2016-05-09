package com.keyboard.inputmethod.panels.gif.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.HSInputMethod;
import com.ihs.inputmethod.api.HSInputMethodTheme;
import com.ihs.inputmethod.base.utils.ResourceUtils;
import com.ihs.inputmethod.framework.AudioAndHapticFeedbackManager;
import com.ihs.inputmethod.framework.Constants;
import com.ihs.inputmethod.keyboard.KeyboardActionListener;
import com.ihs.inputmethod.theme.HSKeyboardThemeManager;
import com.keyboard.inputmethod.panels.gif.control.DataManager;
import com.keyboard.inputmethod.panels.gif.control.GifCategory;
import com.keyboard.inputmethod.panels.gif.control.GifManager;
import com.keyboard.inputmethod.panels.gif.model.GifItem;
import com.keyboard.inputmethod.panels.gif.net.callback.UICallback;
import com.keyboard.inputmethod.panels.gif.net.download.GifDownloadTask;
import com.keyboard.inputmethod.panels.gif.net.request.BaseRequest;
import com.keyboard.inputmethod.panels.gif.net.request.SearchRequest;
import com.keyboard.inputmethod.panels.gif.net.request.TagRequest;
import com.keyboard.inputmethod.panels.gif.net.request.TrendRequest;
import com.keyboard.inputmethod.panels.gif.ui.adapter.GifHorizontalScrollViewAdapter;
import com.keyboard.inputmethod.panels.gif.ui.adapter.TabViewAdapter;
import com.keyboard.inputmethod.panels.gif.ui.panel.GifLayoutParams;
import com.keyboard.inputmethod.panels.gif.utils.ShareUtils;
import com.keyboard.inputmethod.panels.utils.DeleteKeyOnTouchListener;
import com.keyboard.inputmethod.panels.utils.NetworkChangeReceiver;
import com.keyboard.rainbow.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class GifPanelView extends RelativeLayout implements TabViewAdapter.OnTabChangeListener,UICallback,GifView.OnGifClickListener{

    private final static String TAG="GifPanelView";

    private GifLayoutParams mGifLayoutParams;
	private TabViewAdapter tabViewAdapter=null;

    private GifHorizontalScrollView mGifHSView;
    private GifHorizontalScrollViewAdapter mGifHSViewAdapter;

    private final GifCategory mGifCategory;

    private GifLoadingView mGifLoadingView;

    private boolean mLoadingRemote;

    // We should restore hot data
    private int mLastHotPosition;
    private int mLastHotOffset;
    private int mLastHotItemCount;
    private boolean mShouldRestoreHot;

    private final DeleteKeyOnTouchListener mDeleteKeyOnTouchListener;

    private GifStripView mStripView=null;
	private volatile boolean isPerformActionBack=false;
    private volatile boolean isPerformActionSearch=false;

	//background to hide recycler view to avoid flashing when enter gif panel
	private View mGifPanelBg;//background view with specific background color
	private volatile boolean isPaintingBg;

	private String mp4PackageName = HSInputMethod.getCurrentAppName();
	private String shareUrl="";
	private GifDownloadTask.Callback mp4DownloadCallback =new GifDownloadTask.Callback() {
		@Override
		public void onDownloadProgress(final File file, View view, float percent) {
			if (!mGifLoadingView.isShowing()) {
				mGifLoadingView.setBackgroundColor(0xcc000000);
				mGifLoadingView.show();
			}
		}

		@Override
		public void onDownloadSucceeded(final File file, View view) {
			mGifLoadingView.hide();
			try {
				ShareUtils.shareImageByIntent(Uri.fromFile(file), ShareUtils.MIME_MP4, mp4PackageName);
			} catch (Exception e) {
				HSInputMethod.sendText(shareUrl+"");
			}
		}

		@Override
		public void onDownloadFailed(View view) {
			mGifLoadingView.hide();
		}
	};

	private BaseRequest lastRequest=null;

	public GifPanelView(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.stickerPalettesViewStyle);
    }

    public GifPanelView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        final Resources res = context.getResources();
        mGifLayoutParams = new GifLayoutParams(res);
        mGifCategory = new GifCategory();
        mDeleteKeyOnTouchListener = new DeleteKeyOnTouchListener(context);
	    this.setBackgroundColor(HSInputMethodTheme.getThemeMainColor());
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final Resources res = getContext().getResources();
        final int width = ResourceUtils.getDefaultKeyboardWidth(res)
                + getPaddingLeft() + getPaddingRight();
        final int height = ResourceUtils.getDefaultKeyboardHeight(res)
                + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
		if(tabViewAdapter==null){
			List<String> tabs=new ArrayList<>();
			tabs.addAll(mGifCategory.getShownCategories());
			tabViewAdapter=new TabViewAdapter(tabs, HSApplication.getContext(),this);
			RecyclerView mTabHost = (RecyclerView) findViewById(R.id.image_category_tabhost);
			mTabHost.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
			mTabHost.setAdapter(tabViewAdapter);
		}

        mGifHSViewAdapter = new GifHorizontalScrollViewAdapter(this, mGifLayoutParams.getViewWidth(),mGifLayoutParams.getViewHeight());
        mGifHSViewAdapter.setHasStableIds(true);

        mGifHSView = (GifHorizontalScrollView)findViewById(R.id.gif_horizontal_scroll_view);
        mGifHSView.setLayoutManager(new StaggeredGridLayoutManager(mGifLayoutParams.getGridRowNumber(), StaggeredGridLayoutManager.HORIZONTAL));
        mGifHSView.setAdapter(mGifHSViewAdapter);
        mGifHSView.addItemDecoration(new GridSpacingItemDecoration(mGifLayoutParams.getGridSpacing(), 0));
        mGifLayoutParams.setGifBgProperties(mGifHSView);
        GifHorizontalScrollView.LoadScrollListener listener = new GifHorizontalScrollView.LoadScrollListener();
        mGifHSView.addOnChildAttachStateChangeListener(listener);
        mGifLoadingView = (GifLoadingView) findViewById(R.id.gif_loading_view);
		mGifPanelBg=findViewById(R.id.gif_panel_view_alpha);
	    mGifPanelBg.setBackgroundColor(HSInputMethodTheme.getThemeMainColor());
	    mGifLayoutParams.setGifBgProperties(mGifPanelBg);// set height for bg to avoid background view higher than gif panel recycler view
	    ImageButton mDeleteKey = (ImageButton) findViewById(R.id.delete_button);
        mDeleteKey.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDeleteKey.setTag(Constants.CODE_DELETE);
	    final StateListDrawable deleteKeyDrawable = new StateListDrawable();
//		deleteKeyDrawable.addState(new int[] { android.R.attr.state_pressed }, HSKeyboardThemeManager.getStyledAssetDrawable(null, "tabbar_gif_delete_pressed.png"));
//		deleteKeyDrawable.addState(new int[] {}, HSKeyboardThemeManager.getStyledAssetDrawable(null, "tabbar_gif_delete.png"));
		deleteKeyDrawable.addState(new int[] { android.R.attr.state_pressed }, getResources().getDrawable(R.drawable.tabbar_gif_delete_pressed));
		deleteKeyDrawable.addState(new int[] {}, getResources().getDrawable(R.drawable.tabbar_gif_delete));
        mDeleteKey.setImageDrawable(deleteKeyDrawable);
        mDeleteKey.setOnTouchListener(mDeleteKeyOnTouchListener);
	    findViewById(R.id.favorite_added_iv_anim).setVisibility(INVISIBLE);
    }

    @Override
    public void onTabChanged(final String tabId) {
	    final String last=GifCategory.getCurrentExtendedCategoryId();
	    if(last.equals(tabId)&&mGifHSViewAdapter.getItemCount()>0){
		    return;
	    }
	    lastClickedView=null;
	    clear();
	    // request data
	    mGifCategory.setCurrentCategoryId(tabId);
	    try {
		    lastRequest=getRequest(tabId);
		    GifManager.getInstance().sendRequest(lastRequest);
	    } catch (Exception e) {
		    e.printStackTrace();
		    onFail();
	    }

	    AudioAndHapticFeedbackManager.getInstance().performHapticAndAudioFeedback(Constants.CODE_UNSPECIFIED, this);
        if(mStripView!=null){
            mStripView.onTabChanged(tabId);
        }
	    HSGoogleAnalyticsUtils.logKeyboardEvent(com.keyboard.rainbow.utils.Constants.KEYBOARD_GIF_TAB_SWITCHED, mGifCategory.getCurrentLogCategoryId());
    }

	private BaseRequest getRequest(String tabId) {
		if(tabId.equals(GifCategory.TAB_EXPLORE)
				||tabId.equals(GifCategory.TAB_REACTIONS)){
			return new TagRequest(this,tabId,20);
		}
		//only trend
		return new TrendRequest(this,tabId,20);
	}

	private INotificationObserver networkObserver=new INotificationObserver() {
		@Override
		public void onReceive(String s, HSBundle hsBundle) {
			if(s.equals(NetworkChangeReceiver.HS_NOTIFICATION_NETWORK_AVAILABLE)
					&& needRefreshUI
					&&mGifHSViewAdapter.getItemCount()==0
					&&lastRequest!=null
					&&lastRequest.categoryName.equals(GifCategory.getCurrentExtendedCategoryId())){
				GifManager.getInstance().sendRequest(lastRequest);
			}
			needRefreshUI=false;
		}
	};
	private volatile boolean needRefreshUI=false;
    public void onShowPanelView(){
	    HSGlobalNotificationCenter.addObserver(NetworkChangeReceiver.HS_NOTIFICATION_NETWORK_AVAILABLE,networkObserver);
	    if(isPerformActionSearch||isPerformActionBack){
		    return;
	    }

	    if(mGifHSViewAdapter.getItemCount()==0){
		    tabViewAdapter.setCurrentTab(GifCategory.TAB_REACTIONS,GifCategory.TAB_EXPLORE);
	    }else if(mGifHSViewAdapter.getItemCount()>0&&!GifCategory.getCurrentExtendedCategoryId().equals(GifCategory.TAB_REACTIONS)){
		    //enter hot , show background to hide recycler view
		    isPaintingBg = true;
		    mGifPanelBg.setVisibility(VISIBLE);
		    tabViewAdapter.setCurrentTab(GifCategory.TAB_REACTIONS,GifCategory.TAB_EXPLORE);
	    }
    }



    private void setCurrentGifPositionIfNeeded(final BaseRequest request) {
        if (request == null) {
            return;
        }
        // if we back from a tag rank
        if (GifCategory.isTagTab(request.categoryName)) {
            if (mShouldRestoreHot) {
                setCurrentGifPosition(mLastHotPosition, mLastHotOffset);
                mLastHotPosition = 0;
                mLastHotOffset = 0;
                mShouldRestoreHot = false;
	            return;
            }
        }
        // otherwise we always back to front
        if (request.offset == 0) {
            setCurrentGifPosition(0, 0);
        }
    }

    private void setCurrentGifPosition(final int position, final int offset) {
        ((StaggeredGridLayoutManager)mGifHSView.getLayoutManager()).scrollToPositionWithOffset(position, offset);
    }

    public void setKeyboardActionListener(final KeyboardActionListener listener) {
        mDeleteKeyOnTouchListener.setKeyboardActionListener(listener);
    }

    private void loadMoreData() {
        Log.d(TAG, "loadMoreDataNow...");

	    final String categoryId = GifCategory.getCurrentExtendedCategoryId();
	    try {
		    lastRequest.offset=mGifHSViewAdapter.getItemCount();

		    if(lastRequest.getParams().size()>0){
			    lastRequest.addParams("pos",DataManager.getInstance().getNextPos(categoryId));
		    }else{
			    lastRequest.addParamsToUrl("pos",DataManager.getInstance().getNextPos(categoryId));
		    }

		    GifManager.getInstance().sendRequest(lastRequest);
	    } catch (Exception e) {
		    onFail();
	    }

    }

    public void onGifClicked(GifItem item) {
	    HSGoogleAnalyticsUtils.logKeyboardEvent(com.keyboard.rainbow.utils.Constants.KEYBOARD_GIF_CLICKED, mGifCategory.getCurrentLogCategoryId());
	    mp4PackageName= HSInputMethod.getCurrentAppName();
	    shareUrl=item.getUrl();
	    GifManager.getInstance().notifyImageClicked(item, mp4PackageName,mp4DownloadCallback);
    }

    private void onTagClicked(GifItem tagGifItem) {
        recordCurrentPosition();
	    String tag=tagGifItem.getId();
	    if(tag.startsWith("#")){
		    tag=tag.substring(1);
	    }
	    HSGoogleAnalyticsUtils.logKeyboardEvent(com.keyboard.rainbow.utils.Constants.KEYBOARD_GIF_TAG_CLICKED,tag);
	    mGifCategory.setCurrentExtendedCategoryId(tag);

	    clear();
		mStripView.setTagOnStripView(tag);
	    try {
		    lastRequest=new SearchRequest(this,tagGifItem.getUrl(),tag);
		    GifManager.getInstance().sendRequest(lastRequest);
	    } catch (Exception e) {
		    onFail();
	    }
    }

	public void onEmojiClicked(GifItem tagGifItem) {
		isPerformActionSearch=true;
		mGifCategory.setCurrentLogCategoryId("emojiSearch");
		mGifCategory.setCurrentExtendedCategoryId(tagGifItem.getId());
		clear();
		try {
			lastRequest=new SearchRequest(this,tagGifItem.getUrl(),tagGifItem.getId());
			GifManager.getInstance().sendRequest(lastRequest);
		} catch (Exception e) {
			onFail();
		}
	}

    private void clear(){
        if (mGifLoadingView.isShowing()) {
                mGifLoadingView.hide();
        }
        mGifHSViewAdapter.clear();
    }

    public void performActionSearch(final String keyWord) {

	    mGifPanelBg.setVisibility(VISIBLE);
		isPaintingBg = true;

        isPerformActionSearch=true;
        clear();
	    mGifCategory.setCurrentLogCategoryId("search");
        mGifCategory.setCurrentExtendedCategoryId(keyWord);
	    try {
		     lastRequest = new SearchRequest(this,keyWord,20);
		    GifManager.getInstance().sendRequest(lastRequest);
	    } catch (Exception e) {
		    onFail();
	    }

    }

    public void performActionBack() {
	    // get last extended category id
	    final String lastExtendedCategoryId = GifCategory.getCurrentExtendedCategoryId();

	    // back to old category
	    final String categoryId = mGifCategory.getCurrentCategoryId();
	    mGifCategory.setCurrentExtendedCategoryId(categoryId);
	    if (!lastExtendedCategoryId .equals(categoryId)) {

		    // if we back to a tag
		    if (GifCategory.isTagTab(categoryId)) {
			    clear();
			    // restore last hot data
			    try {
				    int count=mLastHotItemCount;
				    if(count==0){
					    count=20;
				    }
				    lastRequest = new TagRequest(this,categoryId,count);

				    GifManager.getInstance().sendRequest(lastRequest);
			    } catch (Exception e) {
				    e.printStackTrace();
				    onFail();
			    }

		    } else {
			    clear();
			    // request data
			    try {
				    lastRequest=new TrendRequest(this,categoryId,20);
				    GifManager.getInstance().sendRequest(lastRequest);
			    } catch (Exception e) {
				    onFail();
			    }
		    }
	    }
        setPerformActionBack(false);
    }

    private void recordCurrentPosition() {
        mShouldRestoreHot = true;

        final StaggeredGridLayoutManager sglm = (StaggeredGridLayoutManager)mGifHSView.getLayoutManager();

        final int pos[] = new int[2];
        sglm.findFirstVisibleItemPositions(pos);
        mLastHotPosition = pos[0];

        mLastHotOffset = 0;
        final View firstVisibleItem = sglm.findViewByPosition(mLastHotPosition);
        if (firstVisibleItem != null) {
            mLastHotOffset = firstVisibleItem.getLeft();
        }

        mLastHotItemCount = mGifHSViewAdapter.getItemCount();
    }


    public void bindStripView(GifStripView stripView) {
        this.mStripView=stripView;
    }

	public synchronized void setPerformActionBack(boolean performActionBack) {
		isPerformActionBack = performActionBack;
	}


	@Override
	public void onFetchRemote() {
		if (mGifHSViewAdapter.getItemCount()==0) {
			mGifLoadingView.show();
			mLoadingRemote=true;
		}
	}

	@Override
	public void onFail() {
		if(mGifLoadingView.isShowing()){
			mGifLoadingView.hide();
			mLoadingRemote=false;
		}
		isPerformActionSearch=false;
		isPaintingBg = false;
		mGifPanelBg.postDelayed(new Runnable() {
			@Override
			public void run() {
				mGifPanelBg.setVisibility(INVISIBLE);
			}
		},200);

		if(mGifHSViewAdapter.getItemCount()==0){
			if(GifCategory.getCurrentExtendedCategoryId().equals(GifCategory.TAB_RECENT)){
				mGifLoadingView.showResult("No Recents.");
				return;
			}

			if(GifCategory.getCurrentExtendedCategoryId().equals(GifCategory.TAB_FAVORITE)){
				mGifLoadingView.showResult("No Favorites.");
				return;
			}

			mGifLoadingView.showResult("Please check your network and try again.");
			needRefreshUI=true;
		}
	}

	@Override
	public synchronized void onComplete(List<?> list, final BaseRequest request) {
		needRefreshUI=false;
		isPerformActionSearch=false;
		Log.d(TAG, "notifyDataCompleted "+"->"+request.categoryName);
		if (mLoadingRemote) {
			mLoadingRemote = false;
		}

		if(mGifLoadingView.isShowing()){
			mGifLoadingView.hide();
		}

		if(request.categoryName.equals(GifCategory.getCurrentExtendedCategoryId())){
			if(request.offset==mGifHSViewAdapter.getItemCount()){
				if (list != null && !list.isEmpty()) {
					List<GifItem> data= (List<GifItem>) list;
					mGifHSViewAdapter.addData(data);
					mGifHSViewAdapter.notifyItemRangeInserted(mGifHSViewAdapter.getItemCount(),data.size());
				}
			}
		}
		//if background is visible, set it invisible
		if(isPaintingBg){
			isPaintingBg = false;
			mGifPanelBg.postDelayed(new Runnable() {
				@Override
				public void run() {
					mGifPanelBg.setVisibility(INVISIBLE);
				}
			},200);
		}
		if(mGifHSViewAdapter.getItemCount()==0){
			if(GifCategory.getCurrentExtendedCategoryId().equals(GifCategory.TAB_RECENT)){
				mGifLoadingView.showResult("No Recents.");
			}else if(GifCategory.getCurrentExtendedCategoryId().equals(GifCategory.TAB_FAVORITE)){
				mGifLoadingView.showResult("No Favorites.");
			}else{
				mGifLoadingView.showResult("No GIFs or stickers found.");
				needRefreshUI=true;
			}
		}

		// set current gif position
		if(request.categoryName.equals(GifCategory.getCurrentExtendedCategoryId())){
			this.post(new Runnable() {
				@Override
				public void run() {
					setCurrentGifPositionIfNeeded(request);
				}
			});
		}

	}

	//service destroy
	public void onServiceDestroy() {
		resetGifPanelView();
	}

	public void onDestroyPanelView() {
		HSGlobalNotificationCenter.removeObserver(networkObserver);
	}
	
	private void resetGifPanelView(){
		post(new Runnable() {
			@Override
			public void run() {
				mGifCategory.setCurrentExtendedCategoryId("");
				isPaintingBg = true;
				mGifPanelBg.setVisibility(VISIBLE);
				mGifHSViewAdapter.clear();
			}
		});
	}

	//language switched
	public void switchLanguage(){
		resetGifPanelView();
	}


	GifView lastClickedView;

	@Override
	public void onGifClick(GifItem item,GifView view) {

		if(lastClickedView!=view&&lastClickedView!=null){
			lastClickedView.hideFavoriteView();
		}
		lastClickedView=view;

		if(item.isTag()){
			onTagClicked(item);
			return;
		}
		onGifClicked(item);
	}

	@Override
	public void onGifLongClick(final GifItem item, final GifView view) {
		if(lastClickedView!=view&&lastClickedView!=null){
			lastClickedView.hideFavoriteView();
		}
		lastClickedView=view;

		if(GifCategory.isTagTab(GifCategory.getCurrentExtendedCategoryId())){
			return;
		}
		if(GifCategory.getCurrentExtendedCategoryId().equals(GifCategory.TAB_FAVORITE)){
			view.showFavoriteDeleteView();
			return;
		}
		final boolean isVisible=view.isFavoriteViewVisible();
		DataManager.getInstance().addFavorite(item);
		view.showFavoriteAddView(true);

		if(isVisible){
			return;
		}

		showFavoriteAddAnim(view);
	}

	private void showFavoriteAddAnim(final GifView view) {
		View favorite=view.getFavoriteIv();
		float fromX=view.getX()+favorite.getX();
		float fromY=view.getY()+favorite.getY();
		final View favoriteICon=findViewById(R.id.favorite_added_iv_anim);
		if(tabViewAdapter.getTabY(GifCategory.TAB_FAVORITE)<0){
			return;
		}
		float toY=mGifHSView.getHeight()+tabViewAdapter.getTabY(GifCategory.TAB_FAVORITE);
		float toX=tabViewAdapter.getTabX(GifCategory.TAB_FAVORITE);

		float jumpX;
		float jumpDistance=Math.abs(toX-fromX)/2;
		if(jumpDistance>favorite.getX()/3){
			jumpDistance=favorite.getX()/3;
		}
		if(toX>fromX){
			jumpX=fromX+jumpDistance;
		}else{
			jumpX=fromX-jumpDistance;
		}

		float jumpY=fromY-favorite.getY()/3;

		Animation upAni=new TranslateAnimation(
				Animation.ABSOLUTE,fromX,
				Animation.ABSOLUTE,jumpX,
				Animation.ABSOLUTE,fromY,
				Animation.ABSOLUTE,jumpY
				);
		upAni.setDuration(200);
		upAni.setInterpolator(new LinearInterpolator());
		final Animation downAni=new TranslateAnimation(
				Animation.ABSOLUTE,jumpX,
				Animation.ABSOLUTE,toX,
				Animation.ABSOLUTE,jumpY,
				Animation.ABSOLUTE,toY
		);
		final int screenWith=ResourceUtils.getDefaultKeyboardWidth(HSApplication.getContext().getResources());
		double durationRatio= Math.abs(1.0*(fromX-toX)/screenWith);
		if(durationRatio<0.3){
			durationRatio=  0.3;
		}
		downAni.setDuration((long) (600*durationRatio));
		downAni.setInterpolator(new AccelerateInterpolator());

		upAni.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				favoriteICon.setVisibility(VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				favoriteICon.startAnimation(downAni);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
		downAni.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				favoriteICon.setVisibility(INVISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});

		favoriteICon.startAnimation(upAni);
	}

	@Override
	public void onFavoriteIconClick(GifItem item, GifView view) {
		if(lastClickedView!=view&&lastClickedView!=null){
			lastClickedView.hideFavoriteView();
		}
		lastClickedView=view;
		final boolean isAdded= DataManager.getInstance().isAddedToFavorite(item);
		if(isAdded){
			DataManager.getInstance().removeFavorite(item);
		}else{
			DataManager.getInstance().addFavorite(item);
		}
		view.showFavoriteAddView(!isAdded);
	}

	@Override
	public void onDeleteIconClick(GifItem item, GifView view) {
		if(lastClickedView!=view&&lastClickedView!=null){
			lastClickedView.hideFavoriteView();
		}
		lastClickedView=null;
		DataManager.getInstance().removeFavorite(item);
		mGifHSViewAdapter.removeItem(item);
		if(mGifHSViewAdapter.getItemCount()==0){
			onFail();
		}
	}

	public void onLastImageShowing() {
		if(GifCategory.isTagTab(GifCategory.getCurrentExtendedCategoryId())||mGifHSViewAdapter.getItemCount()==0){
			return;
		}
		loadMoreData();
	}

	public void onHidePanelView() {
		HSGlobalNotificationCenter.removeObserver(networkObserver);
	}
}

