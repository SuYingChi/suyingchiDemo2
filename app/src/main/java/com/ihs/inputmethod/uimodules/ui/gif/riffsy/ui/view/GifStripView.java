package com.ihs.inputmethod.uimodules.ui.gif.riffsy.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.HSUIInputMethodService;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacterManager;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.suggestions.CustomSearchEditText;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.emojisearch.ESEmojiLoader;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.emojisearch.EmojiSearchView;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.model.GifItem;

/**
 * Created by dsapphire on 16/1/9.
 */
public final class GifStripView extends FrameLayout implements EmojiSearchView.OnEmojiSearchItemClickListener,View.OnClickListener{

	private View back_btn;
	private CustomSearchEditText search_edit;
	private TextView text;
	private TextView emojiText;
	private View emojiIv;
	private View emoji;

	private volatile String result="";
	private GifPanelView panelView;
	private ImageView closeButton;
	private View strip;
	private View emoji_strip;

	private EmojiSearchView emojiSearchView;
	private ESEmojiLoader emojiLoader;
	
	private StripState state= StripState.UNSPECIFIED;
	
	private enum StripState{
		SEARCH_RESULT,
		Emoji,
		SEARCH,
		UNSPECIFIED
	}

	public GifStripView(Context context) {
		this(context,null);
	}

	public GifStripView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public GifStripView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
	}
	

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		emojiLoader =new ESEmojiLoader();
		
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
		
		closeButton = (ImageView) findViewById(R.id.close);
		View main = findViewById(R.id.gif_toolbar_main);
		if(!HSKeyboardThemeManager.getCurrentTheme().isDarkBg()){
			Resources res=getResources();
			main.getBackground().setColorFilter(res.getColor(R.color.gif_panel_search_bar_background_light), PorterDuff.Mode.SRC_IN);
			((ImageView)findViewById(R.id.gif_toolbar_back_button_iv)).getDrawable().setColorFilter(res.getColor(R.color.gif_panel_search_button_color_light),PorterDuff.Mode.SRC_IN);
			closeButton.getDrawable().setColorFilter(res.getColor(R.color.gif_panel_search_button_color_light),PorterDuff.Mode.SRC_IN);
			((ImageView)findViewById(R.id.strip_search_button)).getDrawable().setColorFilter(res.getColor(R.color.gif_panel_search_button_color_light),PorterDuff.Mode.SRC_IN);
			((TextView)findViewById(R.id.emoji_search_title)).setTextColor(res.getColor(R.color.gif_panel_result_text_color_light));
			text.setTextColor(res.getColor(R.color.gif_panel_result_text_color_light));
			text.setHintTextColor(res.getColor(R.color.gif_panel_hint_text_color_light));
			search_edit.setTextColor(res.getColor(R.color.gif_panel_result_text_color_light));
			search_edit.setHintTextColor(res.getColor(R.color.gif_panel_hint_text_color_light));
			emojiText.setTextColor(res.getColor(R.color.gif_panel_result_text_color_light));
		}else{
			Resources res=getResources();
			main.getBackground().setColorFilter(res.getColor(R.color.gif_panel_search_bar_background), PorterDuff.Mode.SRC_IN);
			((ImageView)findViewById(R.id.gif_toolbar_back_button_iv)).getDrawable().setColorFilter(res.getColor(R.color.gif_panel_search_button_color),PorterDuff.Mode.SRC_IN);
			closeButton.getDrawable().setColorFilter(res.getColor(R.color.gif_panel_search_button_color),PorterDuff.Mode.SRC_IN);
			((ImageView)findViewById(R.id.strip_search_button)).getDrawable().setColorFilter(res.getColor(R.color.gif_panel_search_button_color),PorterDuff.Mode.SRC_IN);
			((TextView)findViewById(R.id.emoji_search_title)).setTextColor(res.getColor(R.color.gif_panel_result_text_color));
			text.setTextColor(res.getColor(R.color.gif_panel_result_text_color));
			text.setHintTextColor(res.getColor(R.color.gif_panel_hint_text_color));
			search_edit.setTextColor(res.getColor(R.color.gif_panel_result_text_color));
			search_edit.setHintTextColor(res.getColor(R.color.gif_panel_hint_text_color));
			emojiText.setTextColor(res.getColor(R.color.gif_panel_result_text_color));
		}
		back_btn.setBackgroundDrawable(getBackgroudDrawableForButton());
		bindEvents();
	}
	

	private Drawable getBackgroudDrawableForButton(){
		StateListDrawable stateListDrawable = new StateListDrawable();
		Drawable normalDrawable = new ColorDrawable(Color.TRANSPARENT);
		Drawable pressedDrawable = new ColorDrawable(Color.parseColor("#20000000"));

		stateListDrawable.addState(new int[]{android.R.attr.state_focused}, pressedDrawable);
		stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
		stateListDrawable.addState(new int[]{android.R.attr.state_selected}, pressedDrawable);
		stateListDrawable.addState(new int[]{}, normalDrawable);

		return stateListDrawable;
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
				if(s.toString().endsWith("\n") || s.toString().endsWith("\r")&&s.toString().trim().length()>0){
					result=s.toString().trim();
					final String input = HSSpecialCharacterManager.processConvertSpecialCharacterToNormalEvent(result).toString();
					panelView.performActionSearch(input.toLowerCase());
					updateStripViewState(StripState.SEARCH_RESULT);
					notifyFinishInputInside();
				}
			}
		});
		search_edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId==EditorInfo.IME_ACTION_SEARCH){
					result=search_edit.getText().toString().trim();
					if(result.length()==0){
						return false;
					}
					final String input = HSSpecialCharacterManager.processConvertSpecialCharacterToNormalEvent(result).toString();
					panelView.performActionSearch(input.toLowerCase());
					updateStripViewState(StripState.SEARCH_RESULT);
					notifyFinishInputInside();
					return true;
				}
				return false;
			}
		});
		text.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					updateStripViewState(StripState.SEARCH);
					return true;
				}
				return false;
			}
		});
		back_btn.setOnClickListener(this);
		closeButton.setOnClickListener(this);
	}

	private void showPanelStripView() {
		strip.setVisibility(VISIBLE);
		emoji_strip.setVisibility(GONE);
	}

	private void showEmojiStripView(){
		emoji_strip.setVisibility(VISIBLE);
		strip.setVisibility(GONE);
	}

	private void toSearch(){
		search_edit.setVisibility(VISIBLE);
		text.setVisibility(GONE);
		search_edit.setText("");
		search_edit.requestFocus();
		search_edit.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE
				| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
				| InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
		notifyStartInputInside(search_edit);
		emojiText.setVisibility(GONE);
		emojiIv.setVisibility(VISIBLE);
		panelView.showKeyboardAsDropDownView();
	}

	private void notifyStartInputInside(CustomSearchEditText customSearchEditText) {
		// TODO: should call some method instead of sending a notification
		final HSBundle bundle = new HSBundle();
		bundle.putObject(HSUIInputMethodService.HS_NOTIFICATION_START_INPUT_INSIDE_CUSTOM_SEARCH_EDIT_TEXT, customSearchEditText);
		HSGlobalNotificationCenter.sendNotification(HSInputMethod.HS_NOTIFICATION_START_INPUT_INSIDE,bundle);
	}

	private void notifyFinishInputInside() {
		HSGlobalNotificationCenter.sendNotification(HSInputMethod.HS_NOTIFICATION_FINISH_INPUT_INSIDE);
	}

	private void toResult(){
		search_edit.setVisibility(GONE);
		text.setVisibility(VISIBLE);
		text.setText(result.toUpperCase());
	}

	private void toEmoji() {
		notifyFinishInputInside();
		
		emojiText.setVisibility(VISIBLE);
		emojiIv.setVisibility(GONE);
		search_edit.setVisibility(GONE);
		text.setVisibility(VISIBLE);
		text.setText("");
	}


	private void updateStripViewState(StripState next){
		HSLog.d("gif strip view state updating from "+state+" to "+next);
		if(state==StripState.SEARCH){
			panelView.closeKeyboardDropDownView();
		}
		state=next;
		switch (next){
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
		if(state==StripState.SEARCH){
			panelView.closeKeyboardDropDownView();
			notifyFinishInputInside();
			state=StripState.UNSPECIFIED;
		}

		emojiText.setVisibility(GONE);
		emojiIv.setVisibility(VISIBLE);

		search_edit.setVisibility(GONE);
		text.setVisibility(VISIBLE);
		text.setText("");
		panelView.reloadCurrentTab();
	}

	public void setTagOnStripView(final String tag){
		showStripView();
		if(tag!=null){
			result=tag;
			if(result.startsWith("#")){
				result=result.substring(1);
			}
			updateStripViewState(StripState.SEARCH_RESULT);
		}
	}

	@Override
	public void onEmojiSearchItemClick(GifItem esItem) {
		hideEmojiSearchView();
		showPanelStripView();
		emojiText.setText(esItem.getId());
		panelView.onEmojiSearchItemClicked(esItem);
		updateStripViewState(StripState.Emoji);
	}

	public void bindPanelView(GifPanelView panelView) {
		this.panelView=panelView;
	}

	@Override
	public void onClick(View v) {
		final int id=v.getId();
		if(id==emoji.getId()){
			onEmojiSearchButtonClick();
		}else if(id==closeButton.getId()){
			onCloseButtonClick();
		}else if(id==back_btn.getId()){
			onBackButtonClick();
		}
	}

	private void onBackButtonClick() {
		backStripViewState();
	}

	private void onCloseButtonClick() {
		hideEmojiSearchView();
		showPanelStripView();
	}

	private void onEmojiSearchButtonClick() {
		if(emojiSearchView !=null){
			emojiSearchView.destroy();
			emojiSearchView =null;
		}
		emojiSearchView = (EmojiSearchView) LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.riffsy_emoji_search_view, null);
		emojiSearchView.setListener(this);
		emojiSearchView.setEmojiData(emojiLoader.getEmojiList());
		showEmojiSearchView();
		showEmojiStripView();
	}

	private void showEmojiSearchView(){
		emojiSearchView.prepare();
		panelView.addDropDownView(emojiSearchView);
	}

	public void hideEmojiSearchView(){
		if(emojiSearchView !=null){
			panelView.removeDropDownView(emojiSearchView);
			emojiSearchView.destroy();
			emojiSearchView =null;
		}
	}

	private void showStripView() {
		if(emojiLoader.shuoldEmojiSearchEabled()){
			emoji.setVisibility(VISIBLE);
			emoji.setOnClickListener(this);
		}
		showPanelStripView();
	}
	
	public void showStripViewToSearch(){
		showStripView();
		updateStripViewState(StripState.SEARCH);
	}

}