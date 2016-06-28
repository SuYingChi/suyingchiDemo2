package com.keyboard.inputmethod.panels.gif.ui.panel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.HSInputMethod;
import com.ihs.inputmethod.api.HSInputMethodPanel;
import com.ihs.inputmethod.api.HSInputMethodPanelStripView;
import com.ihs.inputmethod.framework.HSKeyboardPanel;
import com.ihs.inputmethod.framework.HSKeyboardSwitcher;
import com.ihs.inputmethod.uimodules.KeyboardPluginManager;
import com.keyboard.inputmethod.panels.gif.control.DataManager;
import com.keyboard.inputmethod.panels.gif.ui.view.GifPanelView;
import com.keyboard.inputmethod.panels.gif.ui.view.GifStripView;
import com.keyboard.rainbow.R;
import com.ihs.inputmethod.latin.LatinIME;
import com.keyboard.rainbow.utils.Constants;

public final class GifPanel extends HSInputMethodPanel {
	private GifPanelView mGifPanelView;
	private GifStripView stripView=null;

	public GifPanel() {
		super(Constants.PANEL_NAME_GIFS);

		HSGlobalNotificationCenter.addObserver(GifStripView.INPUT_FINISHED_EVENT, imeActionObserver);
		HSGlobalNotificationCenter.addObserver(GifStripView.BACK_EVENT, imeActionObserver);
		HSGlobalNotificationCenter.addObserver(GifStripView.TOSEARCH_EVENT, imeActionObserver);
		HSGlobalNotificationCenter.addObserver(DataManager.HS_NOTIFICATION_SWITCH_LANGUAGE, imeActionObserver);
		HSGlobalNotificationCenter.addObserver(LatinIME.HS_NOTIFICATION_SERVICE_DESTROY, imeActionObserver);

		createGifStripView();
	}


	private INotificationObserver imeActionObserver = new INotificationObserver() {
		@Override
		public void onReceive(String eventName, HSBundle notificaiton) {
			if(mGifPanelView==null){
				return;
			}
			if(eventName==null){
				return;
			}

			if(GifStripView.TOSEARCH_EVENT.equals(eventName)){
				setTabbarBtnState(true);
				HSKeyboardSwitcher switcher=HSInputMethod.getInputService().getKeyboardSwitcher();
				if(switcher!=null){
					HSKeyboardPanel main= KeyboardPluginManager.getInstance().getPanel(HSKeyboardPanel.KEYBOARD_PANEL_KEYBOARD_NAME);
					if(main!=null){
						main.setTabbarBtnState(false);
						main.setTabbarStateEnabled(false);
					}
				}
				return;
			}

			if(DataManager.HS_NOTIFICATION_SWITCH_LANGUAGE.equals(eventName)){
				switchLanguage();
				return;
			}

			if (eventName.equals(GifStripView.INPUT_FINISHED_EVENT)) {
				final String keyWord = notificaiton.getString(GifStripView.EVENT_DATA);
				if(keyWord!=null&&keyWord.trim().length()>0){
					HSGoogleAnalyticsUtils.logKeyboardEvent(Constants.KEYBOARD_GIF_SEARCH_BEGIN,keyWord);
					mGifPanelView.performActionSearch(keyWord);
					KeyboardPluginManager.getInstance().showPanel(getPanelName());
				}else{
					mGifPanelView.setPerformActionBack(true);
					KeyboardPluginManager.getInstance().showPanel(getPanelName());
					mGifPanelView.performActionBack();
				}
				return;
			}

			if (eventName.equals(GifStripView.BACK_EVENT)) {
				mGifPanelView.setPerformActionBack(true);
				KeyboardPluginManager.getInstance().showPanel(getPanelName());
				mGifPanelView.performActionBack();
				return;
			}

			if(LatinIME.HS_NOTIFICATION_SERVICE_DESTROY.equals(eventName)){
				mGifPanelView.onServiceDestroy();
			}
		}
	};

    @Override
    public View onCreatePanelView() {
        LayoutInflater inflater = (LayoutInflater) HSApplication.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mGifPanelView = (GifPanelView)inflater.inflate(R.layout.gif_panel_view, null);
        mGifPanelView.setKeyboardActionListener(HSInputMethod.getInputService());
	    createGifStripView();
        return mGifPanelView;
    }

    @Override
    public void onDestroyPanelView() {
        super.onDestroyPanelView();
        HSGlobalNotificationCenter.removeObserver(imeActionObserver);
	    if(stripView!=null){
		    stripView.onDestroyStripView();
	    }
	    if(mGifPanelView!=null){
		    mGifPanelView.onDestroyPanelView();
	    }
    }

    @Override
    public void onShowPanelView() {
	    createGifStripView();
        mGifPanelView.onShowPanelView();
    }

	@Override
	public void onHidePanelView() {
		super.onHidePanelView();
		if(mGifPanelView!=null){
			mGifPanelView.onHidePanelView();
		}
	}

	@Override
	public HSInputMethodPanelStripView getPanelStripView() {
		createGifStripView();
		return stripView;
	}

	private synchronized void createGifStripView(){
		if(stripView==null){
			stripView= (GifStripView) LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.gif_strip_view,null);
		}
		if(mGifPanelView!=null){
			mGifPanelView.bindStripView(stripView);
			stripView.bindPanelView(mGifPanelView);
		}
	}

	private void switchLanguage(){
		if(mGifPanelView!=null){
			mGifPanelView.switchLanguage();
		}
	}

}
