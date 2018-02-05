package com.ihs.inputmethod.uimodules.ui.customize.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.ihs.inputmethod.feature.common.CommonUtils;
import com.ihs.inputmethod.feature.common.ViewUtils;
import com.ihs.inputmethod.uimodules.R;

/**
 * Created by guonan.lv on 17/9/5.
 */

public class NewsLoadFootView extends LoadFootView {

    private View mProgressBar;
    private TextView mLoadHint;
    private TextView mRetryHint;

    private int mHeight = CommonUtils.pxFromDp(60);

    public NewsLoadFootView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getVisibility() == View.VISIBLE) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            setMeasuredDimension(0, 0);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mProgressBar = ViewUtils.findViewById(this, R.id.progress_bar);
        mLoadHint = ViewUtils.findViewById(this, R.id.loading_hint);
        mRetryHint = ViewUtils.findViewById(this, R.id.retry_hint);
    }


    @Override
    public void reset() {
        mProgressBar.setVisibility(VISIBLE);
        mLoadHint.setVisibility(VISIBLE);
        mRetryHint.setVisibility(GONE);
    }
}
