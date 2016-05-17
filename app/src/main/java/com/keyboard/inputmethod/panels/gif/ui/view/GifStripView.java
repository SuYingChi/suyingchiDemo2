package com.keyboard.inputmethod.panels.gif.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.HSInputMethod;
import com.ihs.inputmethod.api.HSInputMethodPanelStripView;
import com.ihs.inputmethod.api.HSInputMethodSettings;
import com.ihs.inputmethod.api.HSInputMethodTheme;
import com.ihs.inputmethod.base.utils.ResourceUtils;
import com.ihs.inputmethod.theme.HSKeyboardThemeManager;
import com.keyboard.inputmethod.panels.gif.emojisearch.ESManager;
import com.keyboard.inputmethod.panels.gif.emojisearch.ESPageGridView;
import com.keyboard.inputmethod.panels.gif.model.GifItem;
import com.keyboard.rainbow.R;
import com.keyboard.rainbow.app.MyInputMethodService;
import com.keyboard.rainbow.utils.Constants;

/**
 * Created by dsapphire on 16/1/9.
 */
public final class GifStripView extends HSInputMethodPanelStripView implements ESPageGridView.OnEmojiClickListener{
	public final static String TAG="GifStripView";
	public static final String INPUT_FINISHED_EVENT="giftoolbar.inputfinished";
	public static final String BACK_EVENT="giftoolbar.back";
	public static final String EVENT_DATA="giftoolbar.eventdata";
	public static final String TOSEARCH_EVENT = "giftoolbar.tosearch";

	private View back_btn;
	private CustomSearchEditText search_edit;
	private TextView text;
	private TextView emojiText;
	private View emojiIv;
	private View emoji;

	private LinearLayout main;
	private volatile boolean isEditing=false;
	private volatile String result="";
	private volatile boolean settingsDisabled=false;
	private volatile boolean autoCorrection=false;
	private volatile boolean wordPrediction=false;
	private GifPanelView panelView;
	private ImageView closeButton;
	private View strip;
	private View emoji_strip;

	public GifStripView(Context context) {
		this(context,null);
	}

	public GifStripView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public GifStripView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		HSGlobalNotificationCenter.addObserver(MyInputMethodService.HS_NOTIFICATION_DISCONNECT_INSIDE_CONNECTION, disconnect);
		this.setBackgroundColor(HSInputMethodTheme.getThemeMainColor());
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final Resources res = getContext().getResources();
		final int width = ResourceUtils.getDefaultKeyboardWidth(res);
		final int height = (int) res.getDimension(R.dimen.config_suggestions_strip_height);
		setMeasuredDimension(width, height);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		init();
	}

	private void init() {
		strip=findViewById(R.id.panel_strip);
		emoji_strip=findViewById(R.id.emoji_strip);

		back_btn=findViewById(R.id.gif_toolbar_back_button);
		text= (TextView) findViewById(R.id.gif_toolbar_text);
		search_edit= (CustomSearchEditText) findViewById(R.id.gif_toolbar_edit);

		emojiText = (TextView) findViewById(R.id.gif_toolbar_emoji_text);
		emojiIv =  findViewById(R.id.gif_toolbar_emoji_iv);
		emoji = findViewById(R.id.gif_toolbar_emoji);
		final String emojiStr="\ud83d\udca9";
		emojiText.setText(emojiStr);
		emojiText.setVisibility(GONE);

		if(!ESManager.getInstance().shuoldEmojiSearchEabled()){
			emoji.setVisibility(INVISIBLE);
		}

		main= (LinearLayout) findViewById(R.id.gif_toolbar_main);
		ImageView iv2= (ImageView) findViewById(R.id.gif_toolbar_back_button_iv);
		closeButton = (ImageView) findViewById(R.id.close);
		ImageView iv= (ImageView) findViewById(R.id.gif_toolbar_main_search);
		TextView tv_emoji_search_title = (TextView) findViewById(R.id.tv_emoji_search_title);
		tv_emoji_search_title.setTextColor( HSKeyboardThemeManager.getTextColorFromStyleOfCurrentTheme("GifEmojiSearchTitleTextViewStyle"));

		setBackgroundDrawable(main,"keyboard_gif_search_bar_bg");
		setImageDrawable(iv,"keyboard_gif_search_bar");
		setImageDrawable(iv2,"keyboard_gif_left_arrow");
		setImageDrawable(closeButton,"keyboard_gif_emoji_search_close_button");

		initBackAnim();
		bindEvents();


	}



	private void setBackgroundDrawable(View view, String fileName) {
		view.setBackgroundDrawable(HSKeyboardThemeManager.getNinePatchAssetDrawable(getDefaultDrawable(fileName),fileName+".png"));
	}
	private void setImageDrawable(ImageView iv, String fileName) {
		iv.setImageDrawable(HSKeyboardThemeManager.getStyledAssetDrawable(getDefaultDrawable(fileName),fileName+".png"));
	}

	private Drawable getDefaultDrawable(String defaultFileName) {
		return getResources().getDrawable(getResources().getIdentifier(defaultFileName, "drawable", HSApplication.getContext().getPackageName()));
	}

	private void initBackAnim() {
		collapse(back_btn,5);
	}

	private void initToMainAnim() {
		expand(back_btn,0.1f,5);
	}

	private void bindEvents() {
		search_edit.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if(s.toString().endsWith("\n") || s.toString().endsWith("\r")){
					HSBundle bundle=new HSBundle();
					bundle.putString(EVENT_DATA,s.toString().trim());
					HSGlobalNotificationCenter.sendNotificationOnMainThread(INPUT_FINISHED_EVENT, bundle);
					result=s.toString().trim();
					updateStripViewState(StripState.SEARCH_RESULT);
					MyInputMethodService.onFinishInputInside();
				}
			}
		});
		search_edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId==EditorInfo.IME_ACTION_GO||
						actionId==EditorInfo.IME_ACTION_SEARCH||
						actionId==EditorInfo.IME_ACTION_SEND||
						actionId==EditorInfo.IME_ACTION_NEXT||
						actionId==EditorInfo.IME_ACTION_DONE||
						actionId==EditorInfo.IME_ACTION_PREVIOUS){
					String s=search_edit.getText().toString().toLowerCase().trim();
					HSBundle bundle=new HSBundle();
					bundle.putString(EVENT_DATA,s);
					HSGlobalNotificationCenter.sendNotificationOnMainThread(INPUT_FINISHED_EVENT, bundle);
					result=s;
					updateStripViewState(StripState.SEARCH_RESULT);
					MyInputMethodService.onFinishInputInside();
					return true;
				}
				return false;
			}
		});
		text.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(isEditing){
					return true;
				}
				isEditing=true;
				HSGoogleAnalyticsUtils.logKeyboardEvent(Constants.KEYBOARD_GIF_SEARCH_CLICKED);
				updateStripViewState(StripState.SEARCH);
				HSInputMethod.showMainKeyboard();
				HSGlobalNotificationCenter.sendNotificationOnMainThread(TOSEARCH_EVENT);
				return true;
			}
		});
        back_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                backStripViewState();
				HSGlobalNotificationCenter.sendNotificationOnMainThread(BACK_EVENT);

            }
        });

		emoji.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ESManager.getInstance().setListener(GifStripView.this);
				ESManager.getInstance().showEmojiSearchView();
				showEmoji();
				HSGoogleAnalyticsUtils.logKeyboardEvent(Constants.KEYBOARD_GIF_EMOJI_CLICKED);
			}
		});
		closeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ESManager.getInstance().hideEmojiSearchView();
				showStrip();
			}
		});
	}

	private void showStrip() {
		strip.setVisibility(VISIBLE);
		emoji_strip.setVisibility(GONE);
	}

	private void showEmoji(){
		emoji_strip.setVisibility(VISIBLE);
		strip.setVisibility(GONE);
		emoji_strip.setBackgroundColor(HSInputMethodTheme.getThemeMainColor());
	}

	private void toSearch(){
		disableSettings();
		search_edit.setVisibility(VISIBLE);
		text.setVisibility(GONE);
		if(last==StripState.ORIGIN||last==StripState.UNSPECIFIED){
			toMainAnim();
		}

		search_edit.setText("");
		search_edit.requestFocus();
		MyInputMethodService.onStartInputInside(search_edit);
		search_edit.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
		emojiText.setVisibility(GONE);
		emojiIv.setVisibility(VISIBLE);
	}

	private void toMainAnim() {
		expand(back_btn,0.1f,200);
	}

	private void backAnim() {
		collapse(back_btn,200);
	}

	private void toOrigin(){
		search_edit.setVisibility(GONE);
		text.setVisibility(VISIBLE);
		text.setText("");
		emojiText.setVisibility(GONE);
		emojiIv.setVisibility(VISIBLE);
	}

	private void toResult(){
		if(result==null||result.length()==0){
			backStripViewState();
			return;
		}
		search_edit.setVisibility(GONE);
		text.setVisibility(VISIBLE);
		text.setText(result.toUpperCase());
	}

	private void toEmoji() {
		MyInputMethodService.onFinishInputInside();
		if(last==StripState.ORIGIN||last==StripState.UNSPECIFIED){
			initToMainAnim();
		}
		emojiText.setVisibility(VISIBLE);
		emojiIv.setVisibility(GONE);
		search_edit.setVisibility(GONE);
		text.setVisibility(VISIBLE);
		text.setText("");
	}


	private void updateStripViewState(StripState next){
		last=state;
		if (state == next){
			return;
		}
		Log.d(TAG,"gif strip view state updating "+state+"-> to ->"+next);
		state=next;
		switch (next){
			case ORIGIN:
				toOrigin();
				break;
			case SEARCH_RESULT:
				toResult();
				break;
			case Emoji:
				toEmoji();
				break;
			case SEARCH:
				toSearch();
				break;
			default:
				break;
		}
	}

	private void backStripViewState() {
		if(isEditing){
			isEditing=false;
			MyInputMethodService.onFinishInputInside();
		}

		emojiText.setVisibility(GONE);
		emojiIv.setVisibility(VISIBLE);

		last=state;
		state=StripState.ORIGIN;
		search_edit.setVisibility(GONE);
		text.setVisibility(VISIBLE);
		text.setText("");
		backAnim();
	}

	private StripState state= StripState.UNSPECIFIED;
	private StripState last= StripState.UNSPECIFIED;

	private INotificationObserver disconnect=new INotificationObserver() {
		@Override
		public void onReceive(String s, HSBundle hsBundle) {
			if(MyInputMethodService.HS_NOTIFICATION_DISCONNECT_INSIDE_CONNECTION.equals(s)){
				search_edit.setText("");
				isEditing=false;
				resetSettings();
			}
		}
	};

	public void onTabChanged(String tabId) {
		if(state!=StripState.ORIGIN){
			initBackAnim();
		}
		updateStripViewState(StripState.ORIGIN);
	}

	public void setTagOnStripView(final String tag){
		if(tag!=null){
			initToMainAnim();
			result=tag;
			if(result.startsWith("#")){
				result=result.substring(1);
			}
			updateStripViewState(StripState.SEARCH_RESULT);
		}
	}

	@Override
	public void onEmojiClick(GifItem esItem) {
		ESManager.getInstance().hideEmojiSearchView();
		showStrip();
		emojiText.setText(esItem.getId());
		emojiText.setTextColor(HSKeyboardThemeManager.getTextColorFromStyleOfCurrentTheme("GifEmojiSearchSelectedTextViewStyle"));
		panelView.onEmojiClicked(esItem);
		if(state==StripState.SEARCH){
			HSInputMethod.getInputService().showPanel(Constants.PANEL_NAME_GIFS);
		}
		updateStripViewState(StripState.Emoji);
		HSGoogleAnalyticsUtils.logKeyboardEvent(Constants.KEYBOARD_GIF_EMOJI_SEARCH,esItem.getId());
	}

	public void bindPanelView(GifPanelView panelView) {
		this.panelView=panelView;
	}

	private enum StripState{
		ORIGIN,
		SEARCH_RESULT,
		Emoji,
		SEARCH,
		UNSPECIFIED
	}

	@Override
	public void onShowStripView() {
		if(ESManager.getInstance().shuoldEmojiSearchEabled()){
			emoji.setVisibility(VISIBLE);
		}
		showStrip();
	}

	@Override
	public void onDestroyStripView() {
		HSGlobalNotificationCenter.removeObserver(disconnect);
	}

	@Override
	public void onHideStripView() {
		ESManager.getInstance().hideEmojiSearchView();
		if(isEditing){
			MyInputMethodService.onFinishInputInside();
			isEditing=false;
		}
		if(state!=StripState.ORIGIN){
			initBackAnim();
		}
		updateStripViewState(StripState.ORIGIN);
	}

	@Override
	public synchronized boolean shouldKeepStripState() {
		if(!isEditing){
			resetSettings();
		}
		return isEditing;
	}


	private void resetSettings(){
		if(settingsDisabled){
			settingsDisabled=false;
			HSInputMethodSettings.setAutoCorrectionEnabled(autoCorrection);
			HSInputMethodSettings.setWordPredictionEnabled(wordPrediction);
		}
	}

	private void disableSettings(){
		if(!settingsDisabled){
			autoCorrection= HSInputMethodSettings.getAutoCorrectionEnabled();
			wordPrediction= HSInputMethodSettings.getWordPredictionEnabled();
			settingsDisabled=true;
			HSInputMethodSettings.setAutoCorrectionEnabled(false);
			HSInputMethodSettings.setWordPredictionEnabled(false);
		}
	}

	private static void collapse(final View v,final long animDuration) {
		final LayoutParams layoutParams= (LayoutParams) v.getLayoutParams();
		final float weight=layoutParams.weight;
		Animation a = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				if(interpolatedTime == 1){
					layoutParams.weight=0;
					v.requestLayout();
					v.setVisibility(View.INVISIBLE);
				}else{
					layoutParams.weight=weight*(1-interpolatedTime);
					v.requestLayout();
				}
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};
		a.setDuration(animDuration);
		v.startAnimation(a);
	}

	private static void expand(final View v,final float targetWeight,final long animDuration) {
		v.setVisibility(View.VISIBLE);
		final LayoutParams layoutParams= (LayoutParams) v.getLayoutParams();
		Animation a = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				layoutParams.weight=targetWeight*interpolatedTime;
				v.requestLayout();
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};
		a.setDuration(animDuration);
		v.startAnimation(a);
	}
}
