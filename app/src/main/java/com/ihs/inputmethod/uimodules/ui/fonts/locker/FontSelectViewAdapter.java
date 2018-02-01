package com.ihs.inputmethod.uimodules.ui.fonts.locker;

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

import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacter;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacterManager;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.fonts.common.HSFontDownloadManager;

import java.util.List;

//ListView adapter.
public class FontSelectViewAdapter extends BaseAdapter {

    private final static String FONT_SELECTED_TICK = "keyboard_font_selected_tick.png";
    private LayoutInflater mInflater;
    private List<HSSpecialCharacter> mFonts;
    private FontSelectView mParentView;

    private Drawable mItemDefaultBackground;
    private Drawable mItemSelectedBackground;

    private Animator mAnimator;
    private int mFadeinAnimatorResId;
    private OnFontClickListener onFontClickListener;
    public FontSelectViewAdapter(final Context context, final View parentView,OnFontClickListener listener) {
        mInflater = LayoutInflater.from(context);
        mParentView = (FontSelectView) parentView;
        mFadeinAnimatorResId = R.animator.font_picked_icon_fadein;
        mItemDefaultBackground = mParentView.getItemDefaultBackground();
        mItemSelectedBackground = mParentView.getItemSelectedBackground();
        // Set data
        HSFontDownloadManager.getInstance(); // read fonts from file
        mFonts = HSSpecialCharacterManager.getSpecialCharacterList();
        onFontClickListener = listener;
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
            convertView = mInflater.inflate(R.layout.common_font_select_listview_item, null);

            holder = new ViewHolder();

            holder.fontClickRegionLeft = convertView.findViewById(R.id.rl_font_left);
            holder.fontClickRegionRight = convertView.findViewById(R.id.rl_font_right);
            holder.fontNameLeft = convertView.findViewById(R.id.tv_font_left);
            holder.fontNameRight = convertView.findViewById(R.id.tv_font_right);
            holder.fontPickIconLeft = convertView.findViewById(R.id.iv_font_pick_left);
            holder.fontPickIconLeft.setImageDrawable(HSKeyboardThemeManager.getStyledDrawable(convertView.getResources().getDrawable(R.drawable.keyboard_font_selected_tick),
                    FONT_SELECTED_TICK));
            holder.fontPickIconRight = convertView.findViewById(R.id.iv_font_pick_right);
            holder.fontPickIconRight.setImageDrawable(HSKeyboardThemeManager.getStyledDrawable(convertView.getResources().getDrawable(R.drawable.keyboard_font_selected_tick),
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
        final int currentFont = HSSpecialCharacterManager.getCurrentSpecialCharacterIndex();

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
        target.clearAnimation();
        mAnimator = null;

        // Close font palettes view
        FontManager.getInstance().hideFontView();

        // Open main keyboard panel
        if(onFontClickListener!=null){
            onFontClickListener.onFontClick();
        }
    }

    private void switchFont(final int fontIndex) {
        HSSpecialCharacterManager.selectSpecialCharacter(fontIndex);
    }

    private void updateViews(final int font, final View pickIcon, final View backgrond) {
        final int currentFont = HSSpecialCharacterManager.getCurrentSpecialCharacterIndex();
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

    public interface OnFontClickListener{
        void onFontClick();
    }
}
