package com.ihs.inputmethod.uimodules.ui.settings.activities;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ihs.inputmethod.uimodules.R;


public class HeaderPreference extends LinearLayout {
    public interface OnHeaderClickedListener {
        void onHeaderClicked();
    }

    private String headerTitle = null;
    private String subTitle = null;
    private OnHeaderClickedListener listener;
    private TextView subTitleView;
    private TextView pTitleView;

    public HeaderPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.HeaderPreference);
        headerTitle = ta.getString(R.styleable.HeaderPreference_headerString);
        subTitle = ta.getString(R.styleable.HeaderPreference_subtitleString);
        ta.recycle();
        inflate(getContext(), R.layout.header_preference, this);
        pTitleView = this.findViewById(R.id.prefs_header_title);
        pTitleView.setText(headerTitle);
        subTitleView = this.findViewById(R.id.prefs_sub_title);
        subTitleView.setText(subTitle);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (listener != null) {
                    listener.onHeaderClicked();
                }
            }
        });
    }

    public void setOnHeaderClickListener(OnHeaderClickedListener callback) {
        this.listener = callback;
    }

    public HeaderPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void setSubTitle(String subtitle) {
        subTitleView.setText(subtitle);
    }
}
