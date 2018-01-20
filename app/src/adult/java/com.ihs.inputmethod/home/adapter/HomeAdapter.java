package com.ihs.inputmethod.home.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.artw.lockscreen.LockerEnableDialog;
import com.artw.lockscreen.LockerSettings;
import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.keyboard.HSKeyboardTheme;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.theme.HSThemeNewTipController;
import com.ihs.inputmethod.api.utils.HSToastUtils;
import com.ihs.inputmethod.home.model.HomeModel;
import com.ihs.inputmethod.theme.ThemeLockerBgUtil;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegatesManager;
import com.ihs.inputmethod.uimodules.widget.TrialKeyboardDialog;

import java.util.List;

/**
 * Created by jixiang on 18/1/18.
 */

public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    public interface ThemeCardItemClickListener {
        void onCardClick(HSKeyboardTheme keyboardTheme);

        void onMenuShareClick(HSKeyboardTheme keyboardTheme);

        void onMenuDownloadClick(HSKeyboardTheme keyboardTheme);

        void onMenuDeleteClick(HSKeyboardTheme keyboardTheme);

        void onMenuAppliedClick(HSKeyboardTheme keyboardTheme);

        void onKeyboardActivationStart();
    }


    private Activity activity;
    private ThemeCardItemClickListener themeCardItemClickListener;
    protected AdapterDelegatesManager<List<HomeModel>> delegatesManager;
    private List<HomeModel> items;
    private HSKeyboardTheme keyboardThemeOnKeyboardActivation;

    public HomeAdapter(Activity activity, ThemeCardItemClickListener themeCardItemClickListener, HomeStickerCardAdapterDelegate.OnStickerClickListener onStickerClickListener, boolean themeAnalyticsEnabled) {
        this.activity = activity;
        this.themeCardItemClickListener = themeCardItemClickListener;
        delegatesManager = new AdapterDelegatesManager<>();
        delegatesManager.addDelegate(new HomeBackgroundBannerAdapterDelegate(activity, themeAnalyticsEnabled))
                .addDelegate(new HomeMenuAdapterDelegate())
                .addDelegate(new HomeTitleAdapterDelegate())
                .addDelegate(new HomeThemeBannerAdapterDelegate(activity, themeAnalyticsEnabled))
                .addDelegate(new HomeStickerCardAdapterDelegate(onStickerClickListener));
    }

    public void setItems(List<HomeModel> items) {
        this.items = items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return delegatesManager.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        delegatesManager.onBindViewHolder(items, position, holder);
    }

    @Override
    public int getItemViewType(int position) {
        return delegatesManager.getItemViewType(items, position);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
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
        return delegatesManager.getSpanSize(items, position);
    }

    @Override
    public void onClick(final View v) {
//        final HomeModel model = (HomeModel) v.getTag();
//        final HSKeyboardTheme keyboardTheme = (HSKeyboardTheme) model.item;
//        final int key = (int) v.getTag(R.id.theme_card_view_tag_key_action);
//        final int position = (int) v.getTag(R.id.theme_card_view_tag_key_position);
//
//        switch (key) {
//            case ThemeCardAdapterDelegate.TAG_DOWNLOAD:
//                if (keyboardTheme.getThemePkName() != null) {
//                    Runnable runnable = new Runnable() {
//                        @Override
//                        public void run() {
//                            //直接下载主题zip包
//                            boolean downloadThemeZip = HSConfig.optBoolean(false, "Application", "KeyboardTheme", "DownloadThemeZip", "DownloadInApp");
//                            if (downloadThemeZip) {
//                                if (HSKeyboardThemeManager.isThemeZipFileDownloadAndUnzipSuccess(keyboardTheme.mThemeName)) {
//                                    return;
//                                }
//                                String from = "store";
//                                ThemeZipDownloadUtils.startDownloadThemeZip(v.getContext(), from, model.keyboardTheme.mThemeName, keyboardTheme.getSmallPreivewImgUrl(), new AdLoadingView.OnAdBufferingListener() {
//                                    @Override
//                                    public void onDismiss(boolean success, boolean manually) {
//                                        if (success) {
//                                            ThemeZipDownloadUtils.logDownloadSuccessEvent(keyboardTheme.mThemeName, from);
//                                            if (HSKeyboardThemeManager.isThemeZipFileDownloadAndUnzipSuccess(keyboardTheme.mThemeName)) {
//                                                HSKeyboardThemeManager.moveNeedDownloadThemeToDownloadedList(keyboardTheme.mThemeName, true);
//                                                if (themeCardItemClickListener != null) {
//                                                    themeCardItemClickListener.onKeyboardActivationStart();
//                                                }
//                                                keyboardThemeOnKeyboardActivation = keyboardTheme;
//                                            }
//                                        }
//                                    }
//                                });
//                                ThemeZipDownloadUtils.logDownloadClickEvent(keyboardTheme.mThemeName, from);
//                            } else {
//                                boolean shouldDownloadThemeAPK = ThemeDownloadManager.getInstance().downloadTheme(keyboardTheme);
//                                if (shouldDownloadThemeAPK) {
//                                    Toast.makeText(HSApplication.getContext(), HSApplication.getContext().getString(R.string.theme_card_downloading_tip), Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                            setThemeNotNew(keyboardTheme);
//                        }
//                    };
//
//                    if (LockedCardActionUtils.shouldLock(model)) {
//                        LockedCardActionUtils.handleLockAction(v.getContext(), LockedCardActionUtils.LOCKED_CARD_FROM_THEME, model, runnable);
//                    } else {
//                        runnable.run();
//                    }
//                }
//                if (themeCardItemClickListener != null) {
//                    themeCardItemClickListener.onMenuDownloadClick(keyboardTheme);
//                }
//                break;
//            case ThemeCardAdapterDelegate.TAG_DELETE:
//                KCCustomThemeManager.getInstance().removeCustomTheme(keyboardTheme.getThemeId());
//                break;
//            case ThemeCardAdapterDelegate.TAG_MENU:
//                v.setSelected(true);
//                PopupMenu popMenu = ThemeMenuUtils.createPopMenu(v, keyboardTheme);
//
//                popMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                    @Override
//                    public boolean onMenuItemClick(MenuItem item) {
//                        setThemeNotNew(keyboardTheme);
//                        notifyItemChanged(position);
//
//                        final CharSequence title = item.getTitle();
//                        if (TextUtils.isEmpty(title)) {
//                            return false;
//                        }
//                        if (HSApplication.getContext().getString(R.string.theme_card_menu_download).equals(title)) {
//                            if (keyboardTheme.getThemePkName() != null) {
//                                item.setTitle(R.string.theme_card_menu_downloading);
//                                item.setEnabled(false);
//                                boolean shouldDownloadThemeAPK = ThemeDownloadManager.getInstance().downloadTheme(keyboardTheme);
//                                if (shouldDownloadThemeAPK) {
//                                    Toast.makeText(HSApplication.getContext(), HSApplication.getContext().getString(R.string.theme_card_downloading_tip), Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                            if (themeCardItemClickListener != null) {
//                                themeCardItemClickListener.onMenuDownloadClick(keyboardTheme);
//                            }
//                        } else if (HSApplication.getContext().getString(R.string.theme_card_menu_delete).equals(title)) {
//                            KCCustomThemeManager.getInstance().removeCustomTheme(keyboardTheme.getThemeId());
//                            if (themeCardItemClickListener != null) {
//                                themeCardItemClickListener.onMenuDeleteClick(keyboardTheme);
//                            }
//                        } else if (HSApplication.getContext().getString(R.string.theme_card_menu_share).equals(title)) {
//                            ThemeMenuUtils.shareTheme(activity, keyboardTheme);
//                            if (themeCardItemClickListener != null) {
//                                themeCardItemClickListener.onMenuShareClick(keyboardTheme);
//                            }
//                        } else if (HSApplication.getContext().getString(R.string.theme_card_menu_apply).equals(title)) {
//                            File file = new File(ThemeDownloadManager.getThemeDownloadLocalFile(keyboardTheme.mThemeName));
//                            if (keyboardTheme.getThemeType() == HSKeyboardTheme.ThemeType.DOWNLOADED && !HSInstallationUtils.isAppInstalled(keyboardTheme.getThemePkName()) && file.exists() && file.length() > 0) {
//                                ApkUtils.startInstall(HSApplication.getContext(), Uri.fromFile(file));
//                            } else {
//                                if (themeCardItemClickListener != null) {
//                                    themeCardItemClickListener.onKeyboardActivationStart();
//                                }
//                                keyboardThemeOnKeyboardActivation = keyboardTheme;
//                            }
//
//                        } else if (HSApplication.getContext().getString(R.string.theme_card_menu_applied).equals(title)) {
//                            if (themeCardItemClickListener != null) {
//                                themeCardItemClickListener.onMenuAppliedClick(keyboardTheme);
//                            }
//                        }
//                        v.setSelected(false);
//                        return true;
//                    }
//                });
//                popMenu.show();
//                break;
//            case ThemeCardAdapterDelegate.TAG_CARD:
//                if (model.deleteEnable) {
//                    break;
//                }
//                Intent intent = new Intent(activity, ThemeDetailActivity.class);
//                intent.putExtra(ThemeDetailActivity.INTENT_KEY_THEME_NAME, keyboardTheme.mThemeName);
//                activity.startActivity(intent);
//                if (themeCardItemClickListener != null) {
//                    themeCardItemClickListener.onCardClick(keyboardTheme);
//                }
//                setThemeNotNew(keyboardTheme);
//                notifyItemChanged(position);
//                break;
//            default:
//                break;
//        }
    }

    public void finishKeyboardActivation(boolean success) {
        if (success && keyboardThemeOnKeyboardActivation != null) {
            HSKeyboardTheme keyboardTheme = keyboardThemeOnKeyboardActivation;
            keyboardThemeOnKeyboardActivation = null;
            if (!HSKeyboardThemeManager.setKeyboardTheme(keyboardTheme.mThemeName)) {
                String failedString = HSApplication.getContext().getResources().getString(R.string.theme_apply_failed);
                HSToastUtils.toastCenterLong(String.format(failedString, keyboardTheme.getThemeShowName()));
                return;
            } else {
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
