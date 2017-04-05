package com.ihs.inputmethod.uimodules.ui.sticker;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;

import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.listeners.DeleteKeyOnTouchListener;
import com.ihs.inputmethod.uimodules.ui.sticker.bean.BaseStickerItem;

public class StickerPalettesView extends LinearLayout implements OnTabChangeListener, ViewPager.OnPageChangeListener,
		StickerPageGridView.OnStickerClickListener,View.OnClickListener {


	private Drawable mAlphabetKeyLeftBackgroundDrawable;
	private Drawable mAlphabetKeyRightBackgroundDrawable;
	private Drawable mDeleteKeyBackgroundDrawable;
	private Drawable mSpacebarBackgroundDrawable;
	private boolean mCategoryIndicatorEnabled;
	private int mCategoryPageIndicatorColor;
	private int mCategoryPageIndicatorBackground;

	private final DeleteKeyOnTouchListener mDeleteKeyOnTouchListener;
	private ImageButton mDeleteKey;
	private TextView mAlphabetKeyLeft;
	private TextView mAlphabetKeyRight;
	private View mSpacebar;
	private View mSpacebarIcon;
	private StickerCategoryPageIndicatorView mStickerCategoryPageIndicatorView;
	private TabHost mTabHost;
	private ViewPager mImagePager;

	private StickerLayoutParams mStickerLayoutParams;
	private StickerPalettesAdapter mStickerPalettesAdapter;

	private int mCurrentPagerPosition = 0;
	private StickerCategory mStickerCategory;


	public StickerPalettesView(Context context, AttributeSet attrs) {
		this(context, attrs,R.attr.stickerPalettesViewStyle);
	}
	
	public StickerPalettesView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Drawable transparentDrawable = new ColorDrawable(Color.TRANSPARENT);
		TypedArray keyboardViewAttr = context.obtainStyledAttributes(attrs, R.styleable.KeyboardView, defStyle,R.style.KeyboardView);

		StateListDrawable abcKeyDrawable = new StateListDrawable();
		abcKeyDrawable.addState(new int[] { android.R.attr.state_pressed },
				HSKeyboardThemeManager.getNinePatchAssetDrawable(transparentDrawable, HSKeyboardThemeManager.IMG_EMOJI_BG_ABC_HL));
		abcKeyDrawable.addState(new int[] {}, HSKeyboardThemeManager.getNinePatchAssetDrawable(transparentDrawable, HSKeyboardThemeManager.IMG_EMOJI_BG_ABC));
		mAlphabetKeyLeftBackgroundDrawable = HSKeyboardThemeManager.getCurrentTheme().getEmojiABCKeyBackground();
		mAlphabetKeyRightBackgroundDrawable = HSKeyboardThemeManager.getCurrentTheme().getEmojiABCKeyBackground();

		StateListDrawable deleteKeyDrawable = new StateListDrawable();
		deleteKeyDrawable.addState(new int[] { android.R.attr.state_pressed },
				HSKeyboardThemeManager.getStyledDrawable(transparentDrawable, HSKeyboardThemeManager.IMG_EMOJI_KEY_DELETE_FG_HL));
		deleteKeyDrawable.addState(new int[] {}, HSKeyboardThemeManager.getStyledDrawable(transparentDrawable, HSKeyboardThemeManager.IMG_EMOJI_KEY_DELETE_FG));
		mDeleteKeyBackgroundDrawable = deleteKeyDrawable;
		mSpacebarBackgroundDrawable = HSKeyboardThemeManager.getCurrentTheme().getEmojiSpaceKeyBackground();
		keyboardViewAttr.recycle();

		Resources res = context.getResources();
		mStickerLayoutParams = new StickerLayoutParams(res);
		TypedArray imagePalettesViewAttr = context.obtainStyledAttributes(attrs, R.styleable.StickerPalettesView, defStyle, R.style.StickerPalettesView_DARK);

		mStickerCategory = new StickerCategory(PreferenceManager.getDefaultSharedPreferences(context));

		mCategoryIndicatorEnabled = imagePalettesViewAttr.getBoolean(R.styleable.StickerPalettesView_categoryIndicatorEnabled, true);
		mCategoryPageIndicatorColor = imagePalettesViewAttr.getColor(R.styleable.StickerPalettesView_categoryPageIndicatorColor, 0);
		mCategoryPageIndicatorBackground = imagePalettesViewAttr.getColor(R.styleable.StickerPalettesView_categoryPageIndicatorBackground, 0);
		mDeleteKeyOnTouchListener = new DeleteKeyOnTouchListener(context);
		imagePalettesViewAttr.recycle();
		this.setBackgroundColor(context.getResources().getColor(R.color.panel_background));
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		Resources res = getContext().getResources();
		int width = HSResourceUtils.getDefaultKeyboardWidth(res)
				+ getPaddingLeft() + getPaddingRight();
		int height = HSResourceUtils.getDefaultKeyboardHeight(res)
				+ res.getDimensionPixelSize(R.dimen.config_suggestions_strip_height)
				+ getPaddingTop() + getPaddingBottom();
		setMeasuredDimension(width, height);
	}
	
	private void addTab(TabHost host, int categoryId) {
		String tabId = mStickerCategory.getCategoryName(categoryId,0);
		TabHost.TabSpec tspec = host.newTabSpec(tabId);
		tspec.setContent(R.id.sticker_keyboard_dummy);
		ImageView iconView = (ImageView)LayoutInflater.from(getContext()).inflate(R.layout.joy_sticker_panel_tab_icon, null);
//		iconView.setImageResource(mStickerCategory.getCategoryTabIcon(categoryId));
//		String iconName = mStickerCategory.getCategoryTabIconName(categoryId);
		String iconName = StickerManager.getInstance().getCategoryTabIconName(categoryId);
		iconView.setImageDrawable(HSKeyboardThemeManager.getTabbarCategoryIconDrawable(iconName));
		tspec.setIndicator(iconView);
		host.addTab(tspec);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mTabHost = (TabHost)findViewById(R.id.sticker_category_tabhost);
		mTabHost.setup();
		for (StickerCategory.CategoryProperties properties : mStickerCategory.getShownCategories()) {
			addTab(mTabHost, properties.mCategoryId);
		}
		mTabHost.setOnTabChangedListener(this);
        TabWidget tabWidget = mTabHost.getTabWidget();
        tabWidget.setStripEnabled(mCategoryIndicatorEnabled);
        tabWidget.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		mStickerPalettesAdapter = new StickerPalettesAdapter(mStickerCategory, this, mStickerLayoutParams);
		mImagePager = (ViewPager)findViewById(R.id.sticker_keyboard_pager);
		mImagePager.setAdapter(mStickerPalettesAdapter);
		mImagePager.addOnPageChangeListener(this);
		mImagePager.setOffscreenPageLimit(0);
		mImagePager.setPersistentDrawingCache(PERSISTENT_NO_CACHE);
		mStickerLayoutParams.setPagerProperties(mImagePager);


		mStickerCategoryPageIndicatorView = (StickerCategoryPageIndicatorView) findViewById(R.id.sticker_category_page_id_view);
		mStickerCategoryPageIndicatorView.setColors(mCategoryPageIndicatorColor, mCategoryPageIndicatorBackground);
		mStickerLayoutParams.setCategoryPageIdViewProperties(mStickerCategoryPageIndicatorView);
		LinearLayout actionBar = (LinearLayout)findViewById(R.id.sticker_action_bar);
		mStickerLayoutParams.setActionBarProperties(actionBar);


		mAlphabetKeyLeft = (TextView) findViewById(R.id.sticker_keyboard_alphabet_left);
		mAlphabetKeyLeft.setBackgroundDrawable(mAlphabetKeyLeftBackgroundDrawable);
		mAlphabetKeyLeft.setOnClickListener(this);
		mAlphabetKeyRight = (TextView) findViewById(R.id.sticker_keyboard_alphabet_right);
		mAlphabetKeyRight.setBackgroundDrawable(mAlphabetKeyRightBackgroundDrawable);
		mAlphabetKeyRight.setOnClickListener(this);
		mSpacebar = findViewById(R.id.sticker_keyboard_space);
		mSpacebar.setBackgroundDrawable(mSpacebarBackgroundDrawable);
		mSpacebar.setOnClickListener(this);
		mStickerLayoutParams.setKeyProperties(mSpacebar);
		mSpacebarIcon = findViewById(R.id.sticker_keyboard_space_icon);

		// deleteKey depends only on OnTouchListener.
		mDeleteKey = (ImageButton) findViewById(R.id.sticker_keyboard_delete);
		mDeleteKey.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		mDeleteKey.setImageDrawable(mDeleteKeyBackgroundDrawable);
		mDeleteKey.setOnTouchListener(mDeleteKeyOnTouchListener);
		setCurrentCategoryId(mStickerCategory.getCurrentCategoryId(), true /* force */);
	}
	

	@Override
	public void onTabChanged(String tabId) {
		int categoryId = mStickerCategory.getCategoryId(tabId);
		setCurrentCategoryId(categoryId,false);
		uploadStickerCategoryPageIdView();
	}

	private void uploadStickerCategoryPageIdView() {
		if (mStickerCategoryPageIndicatorView == null) {
			return;
		}
		mStickerCategoryPageIndicatorView.setCategoryPageId(mStickerCategory.getCurrentCategoryPageSize(), mStickerCategory.getCurrentCategoryPageId(), 0.0f );
	}
	
	@Override
	public void onPageSelected(int position) {
		final Pair<Integer, Integer> newPos = mStickerCategory.getCategoryIdAndPageIdFromPagePosition(position);
		setCurrentCategoryId(newPos.first /* categoryId */, false /* force */);
		mStickerCategory.setCurrentCategoryPageId(newPos.second /* categoryPageId */);
		uploadStickerCategoryPageIdView();
		mCurrentPagerPosition = position;
	}
	
	@Override
	public void onPageScrollStateChanged(int state) {

	}
	
	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		Pair<Integer, Integer> newPos = mStickerCategory.getCategoryIdAndPageIdFromPagePosition(position);
		int newCategoryId = newPos.first;
		int newCategorySize = mStickerCategory.getCategoryPageSize(newCategoryId);
		int currentCategoryId = mStickerCategory.getCurrentCategoryId();
		int currentCategoryPageId = mStickerCategory.getCurrentCategoryPageId();
		int currentCategorySize = mStickerCategory.getCurrentCategoryPageSize();
		if (newCategoryId == currentCategoryId) {
			mStickerCategoryPageIndicatorView.setCategoryPageId(
					newCategorySize, newPos.second, positionOffset);
		} else if (newCategoryId > currentCategoryId) {
			mStickerCategoryPageIndicatorView.setCategoryPageId(
					currentCategorySize, currentCategoryPageId, positionOffset);
		} else if (newCategoryId < currentCategoryId) {
			mStickerCategoryPageIndicatorView.setCategoryPageId(
					currentCategorySize, currentCategoryPageId, positionOffset - 1);
		}
	}

	private void setCurrentCategoryId(int categoryId, boolean force) {
//		if(categoryId==StickerCategory.ID_RECENTS){
//			mStickerPalettesAdapter.flushPendingRecentStickers();
//		}
		int oldCategoryId=mStickerCategory.getCurrentCategoryId();
		if (oldCategoryId == categoryId && !force) {
			return;
		}
		mStickerCategory.setCurrentCategoryId(categoryId);

		int newTabId=mStickerCategory.getTabIdFromCategoryId(categoryId);
		final int newCategoryPageId = mStickerCategory.getPageIdFromCategoryId(categoryId);
		if (force || mStickerCategory.getCategoryIdAndPageIdFromPagePosition(mImagePager.getCurrentItem()).first != categoryId) {
			mImagePager.setCurrentItem(newCategoryPageId, false /* smoothScroll */);
		}
		if (force || mTabHost.getCurrentTab() != newTabId) {
			mTabHost.setCurrentTab(newTabId);
		}
	}

	@Override
	public void onStickerClicked(BaseStickerItem item) {
		mStickerPalettesAdapter.addRecentSticker(item);
		if(getCurrentCategoryId()==StickerCategory.ID_RECENTS){
			mStickerPalettesAdapter.flushPendingRecentStickers();
		}
		StickerManager.shareImage(Uri.parse(item.url),item.url);
	}

	public int getCurrentCategoryId() {
		return mStickerCategory.getCurrentCategoryId();
	}

	public int getCurrentPagerPosition() {
		return mCurrentPagerPosition;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.sticker_keyboard_alphabet_left || v.getId() == R.id.sticker_keyboard_alphabet_right) {
			if(onStickerAlphabetKeyClickListener !=null){
				onStickerAlphabetKeyClickListener.onAlphabetClick();
			}
		} else if (v.getId() == R.id.sticker_keyboard_space) {
			HSInputMethod.inputSpace();
		}
	}

	public void startStickerPalettes() {
		setupAlphabetKey();
	}

	private void setupAlphabetKey(){
		setupAlphabetKey(mAlphabetKeyLeft, HSInputMethod.getSwitchToAlphaKeyLabel());
		setupAlphabetKey(mAlphabetKeyRight, HSInputMethod.getSwitchToAlphaKeyLabel());
	}

	private static void setupAlphabetKey(final TextView alphabetKey, final String label) {
		alphabetKey.setText(label);
		alphabetKey.setTextColor(HSKeyboardThemeManager.getCurrentTheme().getFuncKeyTextColor());
		alphabetKey.setTextSize(TypedValue.COMPLEX_UNIT_PX, HSKeyboardThemeManager.getCurrentTheme().getFuncKeyLabelSize());
		alphabetKey.setTypeface(HSKeyboardThemeManager.getCurrentTheme().getTextTypeface());
	}


	private OnStickerAlphabetKeyClickListener onStickerAlphabetKeyClickListener;

	public void setOnStickerAlphabetKeyClickListener(OnStickerAlphabetKeyClickListener onStickerAlphabetKeyClickListener) {
		this.onStickerAlphabetKeyClickListener = onStickerAlphabetKeyClickListener;
	}

	public interface OnStickerAlphabetKeyClickListener {
		void onAlphabetClick();
	}

//	@Override
//	public void save() {
//		if (enableRecoverable) {
//			if (mStickerPalettesAdapter != null) {
//				mStickerPalettesAdapter.save();
//
//                recoverableState = State.Saved;
//			}
//		}
//	}
//
//	@Override
//	public void restore() {
//		if (enableRecoverable) {
//			if (mStickerPalettesAdapter != null) {
//				mStickerPalettesAdapter.restore();
//
//                recoverableState = State.Restored;
//			}
//		}
//	}
//
//	@Override
//	public void release()
//	{
//		if (enableRecoverable) {
//			if (mStickerPalettesAdapter != null) {
//				mStickerPalettesAdapter.release();
//
//				ImagePipeline imagePipeline = Fresco.getImagePipeline();
//				imagePipeline.clearMemoryCaches();
//
//                recoverableState = State.Released;
//			}
//		}
//	}
//
//    @Override
//    public Recoverable.State currentState() {
//        return recoverableState;
//    }
//
//    private Recoverable.State recoverableState = State.Initialized;
//
//	private final static boolean enableRecoverable = true;
}