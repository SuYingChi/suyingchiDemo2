package com.ihs.inputmethod.uimodules.ui.sticker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.panelcontainer.BasePanel;
import com.ihs.panelcontainer.panel.KeyboardPanel;

public class StickerPanel extends BasePanel {

	private StickerPalettesView view;
	@Override
	public View onCreatePanelView() {
		//init sticker
		StickerManager.init();

		LayoutInflater inflater = (LayoutInflater) HSApplication.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = (StickerPalettesView) inflater.inflate(R.layout.joy_sticker_palettes_view, null);
		view.setOnStickerAlphabetKeyClickListener(new StickerPalettesView.OnStickerAlphabetKeyClickListener() {
			@Override
			public void onAlphabetClick() {
				panelActionListener.showPanel(KeyboardPanel.class);
				HSInputMethod.backToAlphabetKeyboardFromOtherView();
			}
		});
		return view;
	}


	@Override
	protected boolean onShowPanelView(int appearMode) {
		view.startStickerPalettes();
		return false;
	}
}
