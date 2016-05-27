/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.keyboard.inputmethod.panels.gif.ui.panel;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.View;
import android.widget.FrameLayout;

import com.ihs.inputmethod.base.utils.ResourceUtils;
import com.keyboard.inputmethod.panels.gif.ui.view.GifHorizontalScrollView;
import com.keyboard.colorkeyboard.R;

final public class GifLayoutParams {

    private int mWindowHeight;
    private int mGridSpacing;

    private int mGridViewRowNumber;
    private int mViewWidth;
    private int mViewHeight;
    private int mTabbarHeight;

    public GifLayoutParams(final Resources res) {
        final int defaultKeyboardHeight = ResourceUtils.getDefaultKeyboardHeight(res);

        mGridSpacing  = res.getDimensionPixelSize(R.dimen.config_gif_grid_spacing);
        mTabbarHeight = res.getDimensionPixelSize(R.dimen.config_suggestions_strip_height);
        mWindowHeight = defaultKeyboardHeight - mTabbarHeight;

        if (res.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mGridViewRowNumber = 1;
        } else {
            mGridViewRowNumber = 2;
        }

        mViewHeight = mWindowHeight / mGridViewRowNumber;
        mViewWidth  = 4 * mViewHeight / 3;
    }

    public void setGifBgProperties(final GifHorizontalScrollView view) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) view.getLayoutParams();
        lp.height = mWindowHeight;
        lp.bottomMargin = 0;
        view.setLayoutParams(lp);
    }

    public void setGifBgProperties(final View view) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) view.getLayoutParams();
        lp.height = mWindowHeight;
        lp.bottomMargin = 0;
        view.setLayoutParams(lp);
    }

    public int getGridSpacing() {
        return mGridSpacing;
    }

    public int getViewWidth() {
        return mViewWidth;
    }
    
    public int getViewHeight() {
        return mViewHeight;
    }

    public int getGridRowNumber() {
        return mGridViewRowNumber;
    }
}