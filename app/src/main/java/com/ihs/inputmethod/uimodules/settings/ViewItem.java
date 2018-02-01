package com.ihs.inputmethod.uimodules.settings;


import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.theme.HSThemeNewTipController;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;

/**
 * Created by chenyuanming on 16/9/22.
 */

final class ViewItem {
    public String name;
    public boolean isSelected;
    public ViewItemListener onItemListener;
    public Drawable drawable;

    public TextView textView;
    public ImageView imageView;
    public TextView newTipView;
    public ViewGroup viewContainer;
    // --Commented out by Inspection (18/1/11 下午2:41):boolean isClickedManual = false;
    boolean isShowNewTip = false;

    ViewItem(String name, Drawable drawable, ViewItemListener onItemClickListener, boolean isSelected) {
        this.name = name;
        this.drawable = drawable;
        this.onItemListener = onItemClickListener;
        this.isSelected = isSelected;
    }


    View createView(Context context) {
        View view = View.inflate(context, R.layout.panel_settings_item, null);

        textView = view.findViewById(R.id.tv_settings_item);
        imageView = view.findViewById(R.id.iv_settings_item);
        newTipView = view.findViewById(R.id.new_tip_view);

        textView.setText(name);
        textView.setTextColor(HSKeyboardThemeManager.getCurrentTheme().getStyledTextColor());

        if (HSKeyboardThemeManager.getCurrentTheme().isDarkBg()) {
            imageView.setBackgroundResource(R.drawable.settings_key_common_background_selector);
        } else {
            imageView.setBackgroundResource(R.drawable.settings_key_common_background_selector_light);
        }
        if (HSApplication.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            int padding_portrait = DisplayUtils.dip2px(20);
            if(name.equals(HSApplication.getContext().getResources().getString(R.string.lucky_game_title))){
                padding_portrait = DisplayUtils.dip2px(6);
            }
            imageView.setPadding(padding_portrait, padding_portrait, padding_portrait, padding_portrait);
        } else {
            int padding_land = DisplayUtils.dip2px(12);
            if(name.equals(HSApplication.getContext().getResources().getString(R.string.lucky_game_title))){
                padding_land = DisplayUtils.dip2px(3);
            }
            imageView.setPadding(padding_land, padding_land, padding_land, padding_land);
        }
        imageView.setImageDrawable(drawable);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemListener != null) {
                    onItemListener.onItemClick(ViewItem.this);
                }
            }
        });

        if (onItemListener != null) {
            onItemListener.onItemViewCreated(this);
        }

        viewContainer = (ViewGroup) view;
        return view;
    }

    void showNewMarkIfNeed() {
        if (HSApplication.getContext().getResources().getString(R.string.setting_item_themes).equals(name)) {
            if (HSThemeNewTipController.getInstance().hasNewTipNow()) {
                GradientDrawable newTipDrawable = new GradientDrawable();
                newTipDrawable.setColor(Color.parseColor("#ff3341"));
                newTipDrawable.setShape(GradientDrawable.RECTANGLE);
                newTipDrawable.setCornerRadius(HSDisplayUtils.dip2px(HSApplication.getContext(), 3));
                newTipView.setBackgroundDrawable(newTipDrawable);
                newTipView.setVisibility(View.VISIBLE);
                isShowNewTip = true;
            } else {
                hideNewMark();
            }
        }
    }

    void hideNewMark() {
        if (newTipView != null) {
            newTipView.setVisibility(View.GONE);
        }
        isShowNewTip = false;
    }

    boolean isShowingNewMark() {
        return isShowNewTip;
    }


    public void setSelected(boolean selected) {
        updateSelectedStatus(selected);
        showToast(!selected ? name + " " + HSApplication.getContext().getString(R.string.enabled) : name + " " + HSApplication.getContext().getString(R.string.disabled));
    }

    private void showToast(String msg) {
        Toast.makeText(HSApplication.getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    void updateSelectedStatus(boolean selected) {
        isSelected = selected;
        imageView.setSelected(selected);
    }


    static abstract class ViewItemListener {
        public abstract void onItemClick(ViewItem item);

        public void onItemViewCreated(ViewItem item) {
            item.updateSelectedStatus(item.isSelected);
        }

        void onItemViewInvalidate(ViewItem item) {

        }
    }


}