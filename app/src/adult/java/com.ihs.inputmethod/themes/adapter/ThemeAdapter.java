package com.ihs.inputmethod.themes.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.artw.lockscreen.LockerEnableDialog;
import com.artw.lockscreen.LockerSettings;
import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.keyboard.HSKeyboardTheme;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.theme.HSThemeNewTipController;
import com.ihs.inputmethod.api.utils.HSToastUtils;
import com.ihs.inputmethod.common.adapter.CommonAdapter;
import com.ihs.inputmethod.home.adapter.HomeTitleAdapterDelegate;
import com.ihs.inputmethod.home.model.HomeModel;
import com.ihs.inputmethod.theme.ThemeLockerBgUtil;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegatesManager;
import com.ihs.inputmethod.uimodules.widget.TrialKeyboardDialog;

import java.util.List;

/**
 * Created by jixiang on 18/1/18.
 */

public class ThemeAdapter<T> extends CommonAdapter<HomeModel> {
    private Activity activity;
    protected AdapterDelegatesManager<List<HomeModel>> delegatesManager;
    private HSKeyboardTheme keyboardThemeOnKeyboardActivation;

    public interface ThemeCardItemClickListener {
        void onCardClick(HSKeyboardTheme keyboardTheme);

        void onMenuShareClick(HSKeyboardTheme keyboardTheme);

        void onMenuDownloadClick(HSKeyboardTheme keyboardTheme);

        void onMenuDeleteClick(HSKeyboardTheme keyboardTheme);

        void onMenuAppliedClick(HSKeyboardTheme keyboardTheme);

        void onKeyboardActivationStart();
    }


    public ThemeAdapter(Activity activity) {
        super(activity);
        delegatesManager = new AdapterDelegatesManager<>();
        delegatesManager.addDelegate(new ThemeCardAdapterDelegate());
        delegatesManager.addDelegate(new HomeTitleAdapterDelegate());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return delegatesManager.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        delegatesManager.onBindViewHolder(dataList, position, holder);
    }

    @Override
    public int getItemViewType(int position) {
        return delegatesManager.getItemViewType(dataList, position);
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        delegatesManager.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        delegatesManager.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        delegatesManager.onViewRecycled(holder);
    }

    @Override
    public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
        return delegatesManager.onFailedToRecycleView(holder);
    }

    public int getSpanSize(int position) {
        return delegatesManager.getSpanSize(dataList, position);
    }


    public void finishKeyboardActivation(boolean success) {
        if (success && keyboardThemeOnKeyboardActivation != null) {
            HSKeyboardTheme keyboardTheme = keyboardThemeOnKeyboardActivation;
            keyboardThemeOnKeyboardActivation = null;
            if (!HSKeyboardThemeManager.setKeyboardTheme(keyboardTheme.mThemeName)) {
                String failedString = HSApplication.getContext().getResources().getString(R.string.theme_apply_failed);
                HSToastUtils.toastCenterLong(String.format(failedString, keyboardTheme.getThemeShowName()));
                return;
            }else {
                if (LockerSettings.isLockerEnableShowSatisfied()) {
                    LockerEnableDialog.showLockerEnableDialog(activity,
                            ThemeLockerBgUtil.getInstance().getThemeBgUrl(HSKeyboardThemeManager.getCurrentThemeName()),
                            activity.getString(R.string.locker_enable_title_has_text),
                            this::showTryKeyboardDialog);
                } else {
                    showTryKeyboardDialog();
                }
            }
        }
    }

    private void showTryKeyboardDialog() {
        TrialKeyboardDialog trialKeyboardDialog = new TrialKeyboardDialog.Builder(activity).create();
        trialKeyboardDialog.show(true);
    }

    private void setThemeNotNew(HSKeyboardTheme keyboardTheme) {
        HSThemeNewTipController.getInstance().setThemeNotNew(keyboardTheme.mThemeName);
    }
}
