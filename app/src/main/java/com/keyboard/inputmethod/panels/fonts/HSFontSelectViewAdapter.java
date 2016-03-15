package com.keyboard.inputmethod.panels.fonts;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ihs.inputmethod.api.HSFontManager;
import com.ihs.inputmethod.api.HSInputMethod;
import com.ihs.inputmethod.api.HSInputMethodTheme;
import com.ihs.inputmethod.api.HSLetterFont;
import com.keyboard.rainbow.R;

import java.util.List;

//ListView adapter.
public class HSFontSelectViewAdapter extends BaseAdapter {

    private final static String FONT_SELECTED_TICK = "keyboard_font_selected_tick.png";
    private LayoutInflater mInflater;
    private List<HSLetterFont> mFonts;
    private HSFontSelectView mParentView;

    private Drawable mItemDefaultBackground;
    private Drawable mItemSelectedBackground;

    private static final String ANIMATOR_FADEIN_RES_NAME = "font_picked_icon_fadein";
    private Animator mAnimator;
    private int mFadeinAnimatorResId;

    public HSFontSelectViewAdapter(final Context context, final View parentView) {
        mInflater = LayoutInflater.from(context);
        mParentView = (HSFontSelectView) parentView;
        mFadeinAnimatorResId = context.getResources().getIdentifier(ANIMATOR_FADEIN_RES_NAME, "anim", context.getPackageName());
        mItemDefaultBackground = mParentView.getItemDefaultBackground();
        mItemSelectedBackground = mParentView.getItemSelectedBackground();
        // Set data
        mFonts = HSFontManager.getInstance().getFontList();
    }

    @Override
    public int getCount() {
        if (mFonts != null) {
            return (int) (Math.ceil(mFonts.size() / 2.0f));
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
            convertView = mInflater.inflate(R.layout.font_select_listview_item, null);

            holder = new ViewHolder();

            holder.fontClickRegionLeft = (RelativeLayout) convertView.findViewById(R.id.rl_font_left);
            holder.fontClickRegionRight = (RelativeLayout) convertView.findViewById(R.id.rl_font_right);
            holder.fontNameLeft = (TextView) convertView.findViewById(R.id.tv_font_left);
            holder.fontNameRight = (TextView) convertView.findViewById(R.id.tv_font_right);
            holder.fontPickIconLeft = (ImageView) convertView.findViewById(R.id.iv_font_pick_left);
            holder.fontPickIconLeft.setImageDrawable(HSInputMethodTheme.getStyledAssetDrawable(convertView.getResources().getDrawable(R.drawable.keyboard_font_selected_tick),
                    FONT_SELECTED_TICK));
            holder.fontPickIconRight = (ImageView) convertView.findViewById(R.id.iv_font_pick_right);
            holder.fontPickIconRight.setImageDrawable(HSInputMethodTheme.getStyledAssetDrawable(convertView.getResources().getDrawable(R.drawable.keyboard_font_selected_tick),
                    FONT_SELECTED_TICK));
            holder.fontNameLeft.setTextColor(mParentView.getItemTextColor());
            holder.fontNameRight.setTextColor(mParentView.getItemTextColor());
            convertView.setBackgroundColor(mParentView.getItemDividerColor());
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // left font
        final int fontLeftIndex = position * 2;
        holder.fontNameLeft.setText(mFonts.get(fontLeftIndex).example);
        holder.fontClickRegionLeft.setOnClickListener(new RelativeLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickFont(fontLeftIndex, holder.fontPickIconLeft);
            }
        });

        // right font
        final int fontRightIndex = fontLeftIndex + 1;
        if (fontRightIndex < mFonts.size()) {
            holder.fontNameRight.setText(mFonts.get(fontRightIndex).example);
            holder.fontClickRegionRight.setOnClickListener(new RelativeLayout.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickFont(fontRightIndex, holder.fontPickIconRight);
                }
            });
        } else {
            holder.fontNameRight.setText("");
            holder.fontPickIconRight.setVisibility(View.GONE);
            holder.fontClickRegionRight.setOnClickListener(null);
        }

//        if(position==0) {
//            LinearLayout.LayoutParams lpLeft = ((LinearLayout.LayoutParams) (holder.fontClickRegionLeft.getLayoutParams()));
//            lpLeft.setMargins(0, 1, 0, 0);
//            LinearLayout.LayoutParams lpRight = ((LinearLayout.LayoutParams) (holder.fontClickRegionRight.getLayoutParams()));
//            lpRight.setMargins(0, 1, 0, 0);
//            holder.fontClickRegionLeft.setLayoutParams(lpLeft);
//            holder.fontClickRegionRight.setLayoutParams(lpRight);
//        }
        updateViews(fontLeftIndex, holder.fontPickIconLeft, holder.fontClickRegionLeft);
        updateViews(fontRightIndex, holder.fontPickIconRight, holder.fontClickRegionRight);

        return convertView;
    }

    public void cancelAnimation() {
        if (mAnimator != null) {
            mAnimator.removeAllListeners();
            mAnimator.cancel();
            mAnimator = null;
        }
    }

    private Animator createAnimator(final int font, final View view) {
        final Animator animator;
        final int currentFont = HSFontManager.getInstance().getCurrentFontIndex();

        if (font == currentFont) {
            animator = createDelayedAnimator();
        } else {
            animator = createFadeinAnimator();
        }

        animator.setTarget(view);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animator) {
                onFadeinAnimationEnd(view);
            }
        });

        return animator;
    }

    private Animator createDelayedAnimator() {
        ValueAnimator localValueAnimator = ValueAnimator.ofFloat(1.0f, 1.0f);
        localValueAnimator.setDuration(500L);
        return localValueAnimator;
    }

    private Animator createFadeinAnimator() {
        return AnimatorInflater.loadAnimator(mParentView.getContext(), mFadeinAnimatorResId);
    }

    private void onClickFont(final int font, final View view) {
        if (mAnimator != null) {
            return;
        }

        view.setVisibility(View.VISIBLE);
        mAnimator = createAnimator(font, view);
        mAnimator.start();
        switchFont(font);
        notifyDataSetChanged();
    }

    private void onFadeinAnimationEnd(final View target) {
        HSInputMethod.showMainKeyboard();
        target.clearAnimation();
        mAnimator = null;
    }

    private void switchFont(final int fontIndex) {
        HSInputMethod.selectFont(fontIndex);
    }

    private void updateViews(final int font, final View pickIcon, final View backgrond) {
        final int currentFont = HSFontManager.getInstance().getCurrentFontIndex();
        if (font != currentFont) {
            pickIcon.setVisibility(View.GONE);
            backgrond.setBackgroundDrawable(mItemDefaultBackground);
        } else {
            pickIcon.setVisibility(View.VISIBLE);
            backgrond.setBackgroundDrawable(mItemSelectedBackground);
        }
    }

    /* 存放控件 */
    final class ViewHolder {
        public RelativeLayout fontClickRegionLeft;
        public TextView fontNameLeft;
        public ImageView fontPickIconLeft;
        public RelativeLayout fontClickRegionRight;
        public TextView fontNameRight;
        public ImageView fontPickIconRight;
    }
}
