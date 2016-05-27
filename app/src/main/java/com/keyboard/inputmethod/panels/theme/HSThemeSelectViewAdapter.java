package com.keyboard.inputmethod.panels.theme;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ihs.inputmethod.api.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.HSInputMethod;
import com.ihs.inputmethod.api.HSInputMethodTheme;
import com.keyboard.colorkeyboard.R;
import com.keyboard.colorkeyboard.utils.Constants;

public class HSThemeSelectViewAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private String[] mThemes;
    private HSThemeSelectView mParentView;
    private Drawable mItemDefaultBackground;
    private int mCurrentThemeId;
    private Animator mAnimator;

    public HSThemeSelectViewAdapter(final Context context, final View parentView) {
        mInflater = LayoutInflater.from(context);
        mParentView = (HSThemeSelectView) parentView;
        // Set data
        mThemes = HSInputMethodTheme.getThemeNames();
        mItemDefaultBackground = mParentView.getItemDefaultBackground();
        mCurrentThemeId = HSInputMethodTheme.getCurrentThemeId();
    }

    @Override
    public int getCount() {
        if (mThemes.length > 0) {
            return (int) (Math.ceil(mThemes.length / 2.0f));
        }

        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.theme_select_listview_item, null);

            holder = new ViewHolder();

            holder.themeRegionLeft = (FrameLayout) convertView.findViewById(R.id.rl_theme_left);
            holder.themeRegionRight = (FrameLayout) convertView.findViewById(R.id.rl_theme_right);
            holder.themePreviewLeft = (ImageView) convertView.findViewById(R.id.iv_theme_preview_left);
            holder.themePreviewRight = (ImageView) convertView.findViewById(R.id.iv_theme_preview_right);
            holder.themePreviewLeftPick = (ImageView) convertView.findViewById(R.id.iv_theme_preview_left_selected);
            holder.themePreviewRightPick = (ImageView) convertView.findViewById(R.id.iv_theme_preview_right_selected);
            holder.themeRegionLeft.setBackgroundDrawable(mItemDefaultBackground);
            holder.themeRegionRight.setBackgroundDrawable(mItemDefaultBackground);
            convertView.setBackgroundColor(mParentView.getItemDividerColor());
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // left font
        final int fontLeftIndex = position * 2;
        holder.themePreviewLeft.setImageDrawable(HSInputMethodTheme.getThemePreviewDrawable(fontLeftIndex));
        holder.themeRegionLeft.setOnClickListener(new FrameLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickTheme(fontLeftIndex, holder.themePreviewLeftPick);
            }
        });


        // right font
        final int fontRightIndex = fontLeftIndex + 1;
        if (fontRightIndex < mThemes.length) {
            holder.themePreviewRight.setImageDrawable(HSInputMethodTheme.getThemePreviewDrawable(fontRightIndex));
            holder.themeRegionRight.setOnClickListener(new FrameLayout.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickTheme(fontRightIndex, holder.themePreviewRightPick);
                }
            });
        } else {
            holder.themePreviewRight.setImageDrawable(null);
            holder.themePreviewRightPick.setVisibility(View.GONE);
            holder.themeRegionRight.setOnClickListener(null);
        }

//        if(position==0) {
//            LinearLayout.LayoutParams lpLeft = ((LinearLayout.LayoutParams) (holder.themeRegionLeft.getLayoutParams()));
//            lpLeft.setMargins(0, 1, 0, 0);
//            LinearLayout.LayoutParams lpRight = ((LinearLayout.LayoutParams) (holder.themeRegionRight.getLayoutParams()));
//            lpRight.setMargins(0, 1, 0, 0);
//            holder.themeRegionLeft.setLayoutParams(lpLeft);
//            holder.themeRegionRight.setLayoutParams(lpRight);
//        }
        updateViews(fontLeftIndex, holder.themePreviewLeftPick);
        updateViews(fontRightIndex, holder.themePreviewRightPick);
        return convertView;
    }

    private void onClickTheme(final int index, final View view) {
        if (mAnimator != null) {
            return;
        }
        mCurrentThemeId = index;
        view.setVisibility(View.VISIBLE);
        mAnimator = createAnimator(view);
        mAnimator.start();
        HSInputMethodTheme.saveKeyboardThemeId(String.valueOf(index));
        HSGoogleAnalyticsUtils.logKeyboardEvent(Constants.GA_PARAM_ACTION_THEME_CHOSED, HSInputMethodTheme.getThemeNames(index));
        notifyDataSetChanged();
    }

    private Animator createAnimator(final View view) {
        final Animator animator;

        animator = createDelayedAnimator();

        animator.setTarget(view);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animator) {
                HSInputMethod.updateKeyboardTheme();
                view.clearAnimation();
                mAnimator = null;
            }
        });

        return animator;
    }

    private Animator createDelayedAnimator() {
        ValueAnimator localValueAnimator = ValueAnimator.ofFloat(1.0f, 1.0f);
        localValueAnimator.setDuration(10L);
        return localValueAnimator;
    }

    private void updateViews(final int index, final View selectedBg) {
        if (index >= mThemes.length) {
            return;
        }
        final String currentTheme = HSInputMethodTheme.getCurrentThemeName();
        if (!mThemes[index].equals(currentTheme)) {
            selectedBg.setVisibility(View.GONE);
        } else {
            selectedBg.setVisibility(View.VISIBLE);
        }
    }

    /* 存放控件 */
    final class ViewHolder {
        public FrameLayout themeRegionLeft;
        public ImageView themePreviewLeft;
        public ImageView themePreviewLeftPick;
        public FrameLayout themeRegionRight;
        public ImageView themePreviewRight;
        public ImageView themePreviewRightPick;
    }

    public void cancelAnimation() {
        if (mAnimator != null) {
            mAnimator.removeAllListeners();
            mAnimator.cancel();
            mAnimator = null;
        }
    }
}
