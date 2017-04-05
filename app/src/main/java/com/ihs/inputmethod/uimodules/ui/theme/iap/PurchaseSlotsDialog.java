package com.ihs.inputmethod.uimodules.ui.theme.iap;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.utils.RippleDrawableUtils;
import com.keyboard.core.themes.custom.KCCustomThemeManager;


/**
 * Created by jixiang on 16/12/22.
 */

public class PurchaseSlotsDialog extends AlertDialog implements View.OnClickListener {

    private OnItemClickListener onItemclickListener;

    public PurchaseSlotsDialog(@NonNull Context context, OnItemClickListener onItemclickListener) {
        super(context, R.style.AppCompactTransparentDialogStyle);
        this.onItemclickListener = onItemclickListener;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        setContentView(R.layout.dialog_free_slots);

        float radius = HSApplication.getContext().getResources().getDimension(R.dimen.common_round_corner);
        Resources resources = getContext().getResources();

        TextView freeSlotsTip = (TextView) findViewById(R.id.text_free_slots_tip);
        freeSlotsTip.setText(String.format(HSApplication.getContext().getString(R.string.free_slots_tip), KCCustomThemeManager.getInstance().getAllCustomThemes().size()));

        TextView btnUnlockAllSlots = (TextView) findViewById(R.id.btn_unlock_all_slots);
        btnUnlockAllSlots.setText(String.format(HSApplication.getContext().getString(R.string.free_slots_price), IAPManager.getManager().getUnlimitedSlotsProductPrice()));
        btnUnlockAllSlots.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(0xffd04bee, radius));
        btnUnlockAllSlots.setOnClickListener(this);

        View watchVideoUnlockLayout = findViewById(R.id.watch_video_unlock_layout);
        watchVideoUnlockLayout.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(0xffffffff, 0xffaaaaaa, radius));
        watchVideoUnlockLayout.setOnClickListener(this);

        TextView WatchVideoUnlockText = (TextView) findViewById(R.id.watch_video_unlock_text);
        WatchVideoUnlockText.setText(HSConfig.optString(resources.getString(R.string.free_slots_via_watch_video),
                "Application", "ButtonConfig", "UnlimitedSlotsAlert", "WatchVideo"));

        findViewById(R.id.btn_close_dialog).setOnClickListener(this);

        int widthPixels = resources.getDisplayMetrics().widthPixels;
        int expectedWidth = resources.getDimensionPixelSize(R.dimen.purchase_slot_dialog_width) + resources.getDimensionPixelSize(R.dimen.purchase_slot_dialog_margin) * 2;
        if (widthPixels < expectedWidth) {
            findViewById(R.id.root_view).getLayoutParams().width = widthPixels - resources.getDimensionPixelSize(R.dimen.purchase_slot_dialog_margin) * 2;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_unlock_all_slots) {
            if (onItemclickListener != null) {
                onItemclickListener.onUnlockAllSlotsClick();
            }
        } else if (id == R.id.watch_video_unlock_layout) {
            if (onItemclickListener != null) {
                onItemclickListener.onUnlockViaWatchVideo();
            }
        } else if (id == R.id.btn_close_dialog) {
            if (onItemclickListener != null) {
                onItemclickListener.onCloseButtonClick();
            }
        }
    }

    public interface OnItemClickListener {
        void onUnlockAllSlotsClick();

        void onUnlockViaWatchVideo();

        void onCloseButtonClick();
    }
}
