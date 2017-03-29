package com.mobipioneer.inputmethod.panels.settings.model;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.R;

/**
 * Created by chenyuanming on 16/9/22.
 */

public class ViewItem {
    public String name;
    public boolean isSelected;
    public ViewItemListener onItemClickListener;
    public Drawable drawable;

    public TextView textView;
    public ImageView imageView;
    public LinearLayout viewContainer;
    boolean isClickedManual = false;

    public ViewItem(String name, Drawable drawable, ViewItemListener onItemClickListener) {
        this.name = name;
        this.drawable = drawable;
        this.onItemClickListener = onItemClickListener;

    }


    public View createView(Context context) {
        View view = View.inflate(context, R.layout.panel_settings_item, null);

        textView = (TextView) view.findViewById(R.id.tv_settings_item);
        imageView = (ImageView) view.findViewById(R.id.iv_settings_item);

        textView.setText(name);
        textView.setTextColor(HSKeyboardThemeManager.getCurrentTheme().getStyledTextColor());


        imageView.setImageDrawable(drawable);


        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    isClickedManual = true;
                    onItemClickListener.onItemClick(ViewItem.this);
                }
            }
        });

        if (onItemClickListener != null) {
            isClickedManual = false;
            onItemClickListener.onItemViewCreated(this);
        }
        viewContainer = (LinearLayout) view;
        return view;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
        imageView.setSelected(selected);
        if (isClickedManual) {
            showToast(selected ? name + " Enabled" : name + " Disabled");
        }
    }

    private void showToast(String msg) {
        Toast.makeText(HSApplication.getContext(), msg, Toast.LENGTH_SHORT).show();
    }


    public static abstract class ViewItemListener {
        public abstract void onItemClick(ViewItem item);

        public void onItemViewCreated(ViewItem item) {

        }
    }


}