package com.ihs.inputmethod.uimodules.ui.theme.ui.panel;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v7.widget.GridLayoutManager;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.keyboard.HSKeyboardTheme;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.theme.HSThemeNewTipController;
import com.ihs.inputmethod.theme.download.ApkUtils;
import com.ihs.inputmethod.uimodules.BaseFunctionBar;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.settings.HSNewSettingsPanel;
import com.ihs.inputmethod.uimodules.settings.SettingsButton;
import com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.ThemePanelAdapter;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.ThemePanelModel;
import com.ihs.inputmethod.uimodules.widget.NewThemePromptView;
import com.ihs.panelcontainer.BasePanel;
import com.keyboard.core.themes.custom.KCCustomThemeManager;

import java.util.ArrayList;
import java.util.List;

public final class HSThemeSelectPanel extends BasePanel {

	private FrameLayout panelView;
	private NewThemePromptView newThemePromptView;
	private HSThemeSelectRecycler recycler;
	private ThemePanelAdapter panelAdapter;
	private List<ThemePanelModel> panelModels;
	private int createThemeItemPosition = -1;
	private int moreThemeItemPosition = -1;
	private ThemePanelModel customTitle;
	private ThemePanelModel defaultTitle;
	private ThemePanelModel create;
	private ThemePanelModel more;
	private boolean isCustomThemeInEditMode=false;
	private INotificationObserver notificationObserver = new INotificationObserver() {
		@Override
		public void onReceive(String eventName, HSBundle hsBundle) {
			if (HSKeyboardThemeManager.HS_NOTIFICATION_THEME_LIST_CHANGED.equals(eventName)) {
				if (panelAdapter != null) {
					reloadThemeItems();
					panelAdapter.notifyDataSetChanged();
				}
			}
		}
	};

	public HSThemeSelectPanel() {
	}

	@Override
	public View onCreatePanelView() {
		//set functionBar setting button type
		BaseFunctionBar functionBar = (BaseFunctionBar) panelActionListener.getBarView();
		functionBar.setSettingButtonType(SettingsButton.SettingButtonType.BACK);

		panelView = new FrameLayout(HSApplication.getContext());

		Context mThemeContext = new ContextThemeWrapper(HSApplication.getContext(), HSKeyboardThemeManager.getCurrentTheme().mStyleId);
		LayoutInflater inflater = LayoutInflater.from(mThemeContext);

		panelModels = new ArrayList<>();

		customTitle = new ThemePanelModel();
		customTitle.isTitle = true;
		customTitle.isCustomThemeTitle = true;
		customTitle.customTitleOnClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				isCustomThemeInEditMode=!isCustomThemeInEditMode;
				for (ThemePanelModel theme : panelModels) {
					if(theme.isCustomTheme||theme.isMoreButton||theme.isCreateButton){
						theme.isCustomThemeInEditMode = isCustomThemeInEditMode;
						panelAdapter.notifyItemChanged(panelModels.indexOf(theme));
					}
				}
			}
		};
		customTitle.title = HSApplication.getContext().getResources().getString(R.string.customized_themes);

		defaultTitle = new ThemePanelModel();
		defaultTitle.isTitle = true;
		defaultTitle.title = HSApplication.getContext().getResources().getString(R.string.default_themes);

		create = new ThemePanelModel();
		create.isCreateButton = true;

		more = new ThemePanelModel();
		more.isMoreButton = true;

		reloadThemeItems();

		recycler = (HSThemeSelectRecycler) inflater.inflate(R.layout.theme_select_recycler, null);
		final int span = HSKeyboardThemeManager.getThemeNumOfColumns();
		panelAdapter = new ThemePanelAdapter(span);
		panelAdapter.setItems(panelModels);

		GridLayoutManager gridLayoutManager = new GridLayoutManager(HSApplication.getContext(), span);
		gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
			@Override
			public int getSpanSize(int position) {
				return panelAdapter.getSpanSize(position);
			}
		});
		recycler.setLayoutManager(gridLayoutManager);

		recycler.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (recycler.getMeasuredWidth() > 0 && recycler.getMeasuredHeight() > 0) {
					recycler.setAdapter(panelAdapter);
					recycler.postDelayed(new Runnable() {
						@Override
						public void run() {
							final int currentShowTipType = HSThemeNewTipController.getInstance().getCurrentShowTipType();
							if (getBundle() != null && getBundle().getBoolean(HSNewSettingsPanel.BUNDLE_KEY_SHOW_TIP) && currentShowTipType != HSThemeNewTipController.ThemeTipType.NEW_TIP_NONE) {
								showTipView(currentShowTipType);
							}
						}
					},500);
					recycler.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
			}
		});


		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		panelView.addView(recycler, layoutParams);

		HSGlobalNotificationCenter.addObserver(HSKeyboardThemeManager.HS_NOTIFICATION_THEME_LIST_CHANGED, notificationObserver);
		return panelView;
	}

	public void reloadThemeItems() {
		panelModels.clear();

		//Built in Themes And Plugin Themes only
		if (!HSKeyboardThemeManager.isCustomThemeEnabled()) {
			List<HSKeyboardTheme> allKeyboardThemeList = HSKeyboardThemeManager.getAllKeyboardThemeList();
			for (int i = 0; i < allKeyboardThemeList.size(); i++) {
				ThemePanelModel theme = new ThemePanelModel();
				theme.themeName = HSKeyboardThemeManager.getThemeNameByIndex(i);
				theme.themeShowName = allKeyboardThemeList.get(i).getThemeShowName();
				panelModels.add(theme);
			}
			return;
		}


		//With custom Themes
		if (KCCustomThemeManager.getInstance().getAllCustomThemes().size() > 0) {

			panelModels.add(customTitle);
			// Add Theme
			panelModels.add(create);
			createThemeItemPosition = panelModels.indexOf(create);

			// Custom Themes
			List<HSKeyboardTheme> customKeyboardThemeList = KCCustomThemeManager.getInstance().getAllCustomThemes();
			for (int i = 0; i < customKeyboardThemeList.size(); i++) {
				ThemePanelModel theme = new ThemePanelModel();
				theme.isCustomTheme = true;
				theme.isCustomThemeInEditMode=isCustomThemeInEditMode;
				theme.themeName = customKeyboardThemeList.get(i).getThemeId();
				theme.themeShowName = customKeyboardThemeList.get(i).getThemeShowName();
				panelModels.add(theme);
			}

			// BuiltIn Themes
			panelModels.add(defaultTitle);

			// More Theme
			panelModels.add(more);

			moreThemeItemPosition = panelModels.indexOf(more);

			// BuildIn Themes And Downloaded Themes
			List<HSKeyboardTheme> keyboardThemeList = new ArrayList<>();
			keyboardThemeList.addAll(HSKeyboardThemeManager.getBuiltInThemeList());
			keyboardThemeList.addAll(getDownloadedTheme());
			for (int i = 0; i < keyboardThemeList.size(); i++) {
				ThemePanelModel theme = new ThemePanelModel();
				theme.themeName = keyboardThemeList.get(i).mThemeName;
				theme.themeShowName = keyboardThemeList.get(i).getThemeShowName();
				panelModels.add(theme);
			}

		} else {
			//reset
			isCustomThemeInEditMode=false;
			create.isCustomThemeInEditMode=false;
			customTitle.isCustomThemeInEditMode=false;
			more.isCustomThemeInEditMode=false;

			// Add Theme
			panelModels.add(create);
			createThemeItemPosition = panelModels.indexOf(create);

			// BuildIn Themes And Downloaded Themes
			List<HSKeyboardTheme> keyboardThemeList = new ArrayList<>();
			keyboardThemeList.addAll(HSKeyboardThemeManager.getBuiltInThemeList());
			keyboardThemeList.addAll(getDownloadedTheme());
			for (int i = 0; i < keyboardThemeList.size(); i++) {
				ThemePanelModel theme = new ThemePanelModel();
				theme.themeName = keyboardThemeList.get(i).mThemeName;
				theme.themeShowName = keyboardThemeList.get(i).getThemeShowName();
				panelModels.add(theme);
			}
		}
	}

	private List<HSKeyboardTheme> getDownloadedTheme() {
		List<HSKeyboardTheme> result = new ArrayList<>();
		PackageManager packageManager = HSApplication.getContext().getPackageManager();
		List<HSKeyboardTheme> downloadedThemeList = HSKeyboardThemeManager.getDownloadedThemeList();
		for(HSKeyboardTheme hsKeyboardTheme : downloadedThemeList){
			if(ApkUtils.isPackageInstalled(hsKeyboardTheme.getThemePkName(),packageManager)){
				if (hsKeyboardTheme.isDownloadThemeReady()) {
					result.add(hsKeyboardTheme);
				}
			}
		}
		return result;
	}

	private void showTipView(int currentShowTipType) {
		int tipViewUpViewPosition = 0;
		String tipMessage = "";
		switch (currentShowTipType) {
			case HSThemeNewTipController.ThemeTipType.NEW_TIP_BACKGROUND:
				tipMessage = HSApplication.getContext().getString(R.string.new_background_tip);
				tipViewUpViewPosition = createThemeItemPosition;
				break;
			case HSThemeNewTipController.ThemeTipType.NEW_TIP_FONT:
				tipMessage = HSApplication.getContext().getString(R.string.new_font_tip);
				tipViewUpViewPosition = createThemeItemPosition;
				break;
			case HSThemeNewTipController.ThemeTipType.NEW_TIP_EFFECT:
				tipMessage = HSApplication.getContext().getString(R.string.new_tap_effect_tip);
				tipViewUpViewPosition = createThemeItemPosition;
				break;
			case HSThemeNewTipController.ThemeTipType.NEW_TIP_SOUND:
				tipMessage = HSApplication.getContext().getString(R.string.new_click_sound_tip);
				tipViewUpViewPosition = createThemeItemPosition;
				break;
			case HSThemeNewTipController.ThemeTipType.NEW_TIP_THEME:
				tipMessage = HSApplication.getContext().getString(R.string.new_theme_tip);
				// if has not custom theme , tip view will show under create button ,otherwise it will show under more button
				if (KCCustomThemeManager.getInstance().getAllCustomThemes().size() > 0) {
					tipViewUpViewPosition = moreThemeItemPosition;
				} else {
					tipViewUpViewPosition = createThemeItemPosition;
					create.isAddButtonClickToThemeHome = true;
					panelAdapter.notifyItemChanged(createThemeItemPosition);
				}
				break;
		}

		if (tipViewUpViewPosition == -1
				|| recycler.getLayoutManager().findViewByPosition(tipViewUpViewPosition) == null
				) {
			return;
		}
		if (newThemePromptView == null) {
			newThemePromptView = new NewThemePromptView(HSApplication.getContext(), this);
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			panelView.addView(newThemePromptView, layoutParams);
		}
		newThemePromptView.setPromptText(tipMessage);
		newThemePromptView.prepareShowBelow(recycler, tipViewUpViewPosition);
		HSThemeNewTipController.getInstance().setTypeViewed(currentShowTipType);
	}

	@Override
	protected boolean onShowPanelView(int appearMode) {
		return super.onShowPanelView(appearMode);
	}

	@Override
	protected boolean onHidePanelView(int appearMode) {
		if (newThemePromptView != null) {
			newThemePromptView.hideTip();
			newThemePromptView = null;
		}
		return super.onHidePanelView(appearMode);
	}


	@Override
	public void onDestroy() {
		if (newThemePromptView != null) {
			newThemePromptView.release();
			newThemePromptView = null;
		}
		HSGlobalNotificationCenter.removeObserver(notificationObserver);
		super.onDestroy();
	}
}
