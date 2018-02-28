package com.ihs.inputmethod.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ihs.inputmethod.uimodules.R;

/**
 * Created by jixiang on 18/2/12.
 */

public class CustomSettingItem extends LinearLayout {

    private String text;
    private Drawable drawable;

    public CustomSettingItem(Context context) {
        super(context);

        initView();
    }

    public CustomSettingItem(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomSettingItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomSettingItem);
        text = a.getString(R.styleable.CustomSettingItem_csi_text);

        final int id = a.getResourceId(R.styleable.CustomSettingItem_csi_icon, -1);
        if (id != -1) {
            drawable = AppCompatResources.getDrawable(getContext(), id);
        }

        a.recycle();

        initView();
    }

    private void initView() {
        View view = View.inflate(getContext(), R.layout.custom_setting_item, this);

        ImageView imageView = view.findViewById(R.id.setting_icon);
        imageView.setImageDrawable(drawable);

        TextView textView = view.findViewById(R.id.setting_text);
        textView.setText(text);
    }


}
