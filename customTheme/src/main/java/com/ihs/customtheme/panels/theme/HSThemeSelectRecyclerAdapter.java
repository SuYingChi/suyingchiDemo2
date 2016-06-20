package com.ihs.customtheme.panels.theme;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.app.framework.HSApplication;
import com.ihs.customtheme.R;
import com.ihs.customtheme.app.ui.CustomThemeActivity;
import com.ihs.customtheme.app.utils.Constants;
import com.ihs.inputmethod.api.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.HSInputMethod;
import com.ihs.inputmethod.api.HSInputMethodTheme;
import com.ihs.inputmethod.keyboard.KeyboardTheme;
import com.ihs.inputmethod.theme.HSKeyboardThemeManager;
import com.tonicartos.superslim.GridSLM;

import java.util.List;

public class HSThemeSelectRecyclerAdapter extends RecyclerView.Adapter<HSThemeSelectRecyclerViewHolder> {
    private static final String TAG = HSThemeSelectRecyclerAdapter.class.getSimpleName();

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private static final int VIEW_TYPE_HEADER = 0x01;
    private static final int VIEW_TYPE_CONTENT = 0x00;

    private final List<ThemeSelectViewItem> mItems;
    private int mHeaderDisplay;
    private boolean mMarginsFixed;
    private final Context mContext;
    private OnItemClickListener mOnItemClickListener;
    private Animator mAnimator;
    private String mCurrentThemeName;

    public HSThemeSelectRecyclerAdapter(Context context, int headerMode, List<ThemeSelectViewItem> items) {
        mContext = context;
        mHeaderDisplay = headerMode;
        mItems = items;
    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public boolean isItemHeader(int position) {
        return mItems.get(position).isHeader;
    }

    @Override
    public HSThemeSelectRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        boolean isHeaderView = viewType == VIEW_TYPE_HEADER;
        if (isHeaderView) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.theme_select_recycler_item_header, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.theme_select_recycler_item_cell, parent, false);
        }
        return new HSThemeSelectRecyclerViewHolder(view, isHeaderView);
    }


    @Override
    public void onBindViewHolder(HSThemeSelectRecyclerViewHolder holder, final int position) {
        final ThemeSelectViewItem item = mItems.get(position);
        final View itemView = holder.itemView;

        holder.bindItem(item,getNumOfColums());

        final GridSLM.LayoutParams lp = GridSLM.LayoutParams.from(itemView.getLayoutParams());
        // Overrides xml attrs, could use different layouts too.
        if (item.isHeader) {
            lp.headerDisplay = mHeaderDisplay;
            if (lp.isHeaderInline() || (mMarginsFixed && !lp.isHeaderOverlay())) {
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            } else {
                lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            }

            lp.headerEndMarginIsAuto = !mMarginsFixed;
            lp.headerStartMarginIsAuto = !mMarginsFixed;
        }

        lp.setSlm(GridSLM.ID);
        lp.setNumColumns(getNumOfColums());
        lp.setFirstPosition(item.sectionFirstPosition);
        itemView.setLayoutParams(lp);

        if (!item.isHeader) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (item.isAddTheme) {
                        final Context context = HSApplication.getContext();
                        final Intent intent = new Intent(context, CustomThemeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);
                    } else {
                        mCurrentThemeName = item.themeName;
                        mAnimator = createAnimator(v);
                        mAnimator.start();
                        HSInputMethodTheme.saveKeyboardThemeName(mCurrentThemeName);
                        HSGoogleAnalyticsUtils.logKeyboardEvent(Constants.GA_PARAM_ACTION_THEME_CHOSED, mCurrentThemeName);
                        notifyDataSetChanged();
                    }
                }
            });
        }
    }
    public int getNumOfColums() {
        KeyboardTheme.windowStyleType windowStyleType = HSKeyboardThemeManager.getCurrentTheme().getCurrentWindowTypeStyle();
        //phone：竖屏一行3个，横屏一行5个,pad：竖屏一行4个，横屏一行5个
        int numOfColums = 3;
        switch (windowStyleType) {
            case portrait:
                numOfColums = 3;
                break;
            case sw600dp_portrait:
            case sw768dp_portrait:
                numOfColums = 4;
                break;
            case landscape:
            case sw600dp_landscape:
            case sw768dp_landscape:
                numOfColums = 5;
                break;
        }
        return numOfColums;
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


    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).isHeader ? VIEW_TYPE_HEADER : VIEW_TYPE_CONTENT;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setHeaderDisplay(int headerDisplay) {
        mHeaderDisplay = headerDisplay;
        notifyHeaderChanges();
    }

    public void setMarginsFixed(boolean marginsFixed) {
        mMarginsFixed = marginsFixed;
        notifyHeaderChanges();
    }

    private void notifyHeaderChanges() {
        for (int i = 0; i < mItems.size(); i++) {
            ThemeSelectViewItem item = mItems.get(i);
            if (item.isHeader) {
                notifyItemChanged(i);
            }
        }
    }

    public static class ThemeSelectViewItem {
        public int sectionManager;
        public int sectionFirstPosition;
        public boolean isHeader;
        public boolean isAddTheme;
        public String title;
        public int themeIndex;
        public String themeName;

        public ThemeSelectViewItem(String title, boolean isHeader, boolean isAddTheme, int themeIndex, String themeName,
                                   int sectionManager, int sectionFirstPosition) {
            this.title = title;
            this.isHeader = isHeader;
            this.isAddTheme = isAddTheme;
            this.themeIndex = themeIndex;
            this.themeName = themeName;
            this.sectionManager = sectionManager;
            this.sectionFirstPosition = sectionFirstPosition;
        }
    }
}
