package com.keyboard.inputmethod.panels.gif.emojisearch;

import android.content.res.Resources;
import android.view.LayoutInflater;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.HSInputMethod;
import com.ihs.inputmethod.framework.HSKeyboardSwitcher;
import com.ihs.inputmethod.keyboard.internal.CodesArrayParser;
import com.keyboard.inputmethod.panels.gif.control.GifCategory;
import com.keyboard.inputmethod.panels.gif.control.GifManager;
import com.keyboard.inputmethod.panels.gif.model.GifItem;
import com.keyboard.inputmethod.panels.gif.net.callback.UICallback;
import com.keyboard.inputmethod.panels.gif.net.request.BaseRequest;
import com.keyboard.inputmethod.panels.gif.net.request.TagRequest;
import com.keyboard.colorkeyboard.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public final class ESManager {

	private static final int SIZE_TO_EABLE_EMOJI_SEARCH =20;
	private List<GifItem> mEmojis=new ArrayList<>();
	private Set<String> supportEmoji=new HashSet<>();
	private boolean mESShow;
	private ESPalettesView mESPalettesView;

	private int mMaxPageItemCount;
	private boolean supportEmojiLoaded=false;
	private static ESManager mInstance;
	private UICallback callback=new UICallback() {
		@Override
		public void onFetchRemote() {

		}

		@Override
		public void onFail() {

		}

		@Override
		public void onComplete(List<?> data, BaseRequest request) {
			if(request.offset==mEmojis.size()&&data!=null&&data.size()>0){
				for(Object item:data){
					try {
						GifItem emoji= (GifItem) item;
						if(emoji!=null){
							if(supportEmoji.contains(emoji.getId())&&!mEmojis.contains(emoji)){
								mEmojis.add(emoji);
							}
						}
					}catch (Exception e){
						e.printStackTrace();
					}
				}
			}
		}
	};

	private ESManager() {
		new Thread() {
			@Override
			public void run() {
				mInstance.loadEmojis();
			}
		}.start();
	}

	public static ESManager getInstance() {
		if (mInstance == null) {
			init();
		}
		return mInstance;
	}

	public List<GifItem> getDataByPagePosition(final int position) {
		final List<GifItem> data = new ArrayList<>();
		final int start = mMaxPageItemCount * position;
		int end = start + mMaxPageItemCount;
		end = mEmojis.size() > end ? end : mEmojis.size();

		for (int i = start; i < end; i++) {
			data.add(mEmojis.get(i));
		}

		return data;
	}

	public int getPageCount() {
		if (mEmojis == null) {
			return 0;
		}

		if (mMaxPageItemCount <= 0) {
			return 0;
		}

		return (int) (Math.ceil(mEmojis.size() / (double) mMaxPageItemCount));
	}

	public static void init() {
		if (mInstance == null) {
			synchronized (ESManager.class){
				if(mInstance==null){
					mInstance = new ESManager();
				}
			}
		}

		// we must renew one to match port/land
		mInstance.initEmojiSearchView();
	}

	private void initEmojiSearchView() {
		if (mESPalettesView != null) {
			mESPalettesView.destroy();
		}
		mESShow=false;
		mESPalettesView = (ESPalettesView) LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.emoji_search_palettes_view, null);
	}

	private void loadEmojis() {
		readSupportedEmojis();
		TagRequest emoji=new TagRequest(callback, GifCategory.TAB_EMOJI,50);
		emoji.offset=mEmojis.size();
		GifManager.getInstance().sendRequest(emoji);
	}

	public void onFinishInputView() {
		hideEmojiSearchView();
	}

	public void onConfigurationChanged() {
		hideEmojiSearchView();
	}

	public void setMaxPageItemCount(final int count) {
		mMaxPageItemCount = count;
	}

	public void setListener(final ESPageGridView.OnEmojiClickListener listener) {
		if(mESPalettesView==null)
			initEmojiSearchView();
		mESPalettesView.setListener(listener);
	}

	public void showEmojiSearchView() {
		if(!mESShow){
			mESPalettesView.prepare();

			final HSKeyboardSwitcher switcher= HSInputMethod.getInputService().getKeyboardSwitcher();
			switcher.addTopView(mESPalettesView);
			mESShow = true;
		}
	}

	public synchronized void hideEmojiSearchView() {
		if (mESShow) {
			final HSKeyboardSwitcher switcher= HSInputMethod.getInputService().getKeyboardSwitcher();
			switcher.removeTopView(mESPalettesView);
			mESShow = false;
		}
		new Thread(){
			@Override
			public void run() {
				mInstance.loadEmojis();
			}
		}.start();
	}

	public boolean shuoldEmojiSearchEabled(){
		final boolean should= mEmojis.size()> SIZE_TO_EABLE_EMOJI_SEARCH;
		if(!should){
			new Thread(){
				@Override
				public void run() {
					mInstance.loadEmojis();
				}
			}.start();
		}
		return should;
	}

	private void readSupportedEmojis() {
		if(supportEmojiLoaded){
			return;
		}
		supportEmojiLoaded=true;
		final Resources res = HSApplication.getContext().getResources();
		readEmojiByCategory(res, R.array.emoji_nature);
		readEmojiByCategory(res, R.array.emoji_symbols);
		readEmojiByCategory(res, R.array.emoji_faces);
		readEmojiByCategory(res, R.array.emoji_objects);
		readEmojiByCategory(res, R.array.emoji_places);
		if(res.getBoolean(R.bool.emoji_update_with_6_0_1)){
			readEmojiByCategory(res, R.array.emoji_eight_activity);
			readEmojiByCategory(res, R.array.emoji_eight_animals_nature);
			readEmojiByCategory(res, R.array.emoji_eight_flags);
			readEmojiByCategory(res, R.array.emoji_eight_objects);
			readEmojiByCategory(res, R.array.emoji_eight_food_drink);
			readEmojiByCategory(res, R.array.emoji_eight_smiley_people);
			readEmojiByCategory(res, R.array.emoji_eight_smiley_people_boring);
			readEmojiByCategory(res, R.array.emoji_eight_symbols);
			readEmojiByCategory(res, R.array.emoji_eight_travel_places);
		}
	}

	private void readEmojiByCategory(final Resources res, final int arrayId) {
		try {
			final String[] emojis = res.getStringArray(arrayId);
			for (String emoji : emojis) {
				supportEmoji.add(CodesArrayParser.parseLabel(emoji));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}