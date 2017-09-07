package com.ihs.inputmethod.uimodules.ui.theme.utils;

import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.view.View;
import android.widget.Toast;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.keyboard.HSKeyboardTheme;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSFileUtils;
import com.ihs.inputmethod.api.utils.HSThreadUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.alerts.HSAlertDialog;
import com.keyboard.core.themes.custom.KCCustomThemeHelper;

import java.io.File;

/**
 * Created by jixiang on 16/8/26.
 */
public class ThemeMenuUtils {

    public static final int keyboardActivationFromAdapter = 10;

    public static PopupMenu createPopMenu(final View v, final HSKeyboardTheme keyboardTheme) {


        final PopupMenu popup = new PopupMenu(v.getContext(), v);
        switch (keyboardTheme.getThemeType()) {
            case NEED_DOWNLOAD:
                break;
            case DOWNLOADED:
            case BUILD_IN:
                if (HSKeyboardThemeManager.getCurrentTheme() == keyboardTheme) {
                    popup.getMenu().add(HSApplication.getContext().getString(R.string.theme_card_menu_applied));
                } else {
                    popup.getMenu().add(HSApplication.getContext().getString(R.string.theme_card_menu_apply));
                }
                popup.getMenu().add(HSApplication.getContext().getString(R.string.theme_card_menu_share));
                break;
            case CUSTOM:
                if (HSKeyboardThemeManager.getCurrentTheme() == keyboardTheme) {
                    popup.getMenu().add(HSApplication.getContext().getString(R.string.theme_card_menu_applied));
                } else {
                    popup.getMenu().add(HSApplication.getContext().getString(R.string.theme_card_menu_apply));
                }
                popup.getMenu().add(HSApplication.getContext().getString(R.string.theme_card_menu_share));
                popup.getMenu().add(HSApplication.getContext().getString(R.string.theme_card_menu_delete));
                break;
        }

        popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                v.setSelected(false);
            }
        });
        return popup;
    }

    public static void shareTheme(final Activity activity, final HSKeyboardTheme keyboardTheme) {
        final String shareActionTitle = "Choose Share";
        final String title = "";
        final String content;
        final String shareImagePath;
        switch (keyboardTheme.getThemeType()) {
            case CUSTOM:
                content = HSApplication.getContext().getResources().getString(R.string.theme_share_text_content_for_custom_theme, HSApplication.getContext().getResources().getString(R.string.app_name));
                shareImagePath = getCustomShareFile(keyboardTheme);
                File file = new File(shareImagePath);
                if (!file.exists() || file.length() == 0){
                    final AlertDialog loadingDialog = HSAlertDialog.build(activity).setView(R.layout.dialog_loading).setCancelable(false).create();
                    loadingDialog.show();
                    HSThreadUtils.execute(new Runnable() {
                        @Override
                        public void run() {
                            KCCustomThemeHelper.createAndSaveShareImage(keyboardTheme,shareImagePath);
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    loadingDialog.dismiss();
                                    ShareUtils.shareImageFilterBlackList(activity, shareActionTitle, title, content, shareImagePath);
                                }
                            });
                        }
                    });
                }else {
                    ShareUtils.shareImageFilterBlackList(activity, shareActionTitle, title, content, shareImagePath);
                }
                break;
            default:
                content = HSApplication.getContext().getResources().getString(R.string.theme_share_text_content, HSApplication.getContext().getResources().getString(R.string.app_name));
                shareImagePath = getDefaultShareFile();
                ShareUtils.shareImageFilterBlackList(activity, shareActionTitle, title, content, shareImagePath);
                break;
        }

    }


    public static String getShareFileDir() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return Environment.getExternalStorageDirectory() + File.separator + HSApplication.getContext().getPackageName() + File.separator + "shareTheme";
        }
        Toast.makeText(HSApplication.getContext(), HSApplication.getContext().getString(R.string.sd_card_unavailable_tip), Toast.LENGTH_SHORT).show();
        return null;
    }

    public static String getCustomShareFile(HSKeyboardTheme keyboardTheme) {
        if (keyboardTheme.mThemeName != null && getShareFileDir() != null) {
            String themeName = keyboardTheme.mThemeName;
            if (themeName.length() >= 16) {
                themeName = themeName.substring(0, 16);
            }
            File shareTempFile = new File(getShareFileDir(), themeName + "_share_app.png");
            if (!shareTempFile.exists() || !shareTempFile.isFile()) {
                HSFileUtils.copyFile(HSKeyboardThemeManager.getCustomThemeShareFile(keyboardTheme.mThemeName), shareTempFile.getAbsolutePath());
            }
            return shareTempFile.getAbsolutePath();
        }
        return null;
    }

    public static String getDefaultShareFile() {
        if (getShareFileDir() == null) {
            return null;
        }
        File file = new File(getShareFileDir(), "share_app.jpg");
        if (!file.exists() || file.length() == 0) {
            file.getParentFile().mkdirs();
            HSFileUtils.copyFile(HSApplication.getContext().getResources().openRawResource(R.raw.share_app), file);
        }
        return file.getAbsolutePath();
    }

}
