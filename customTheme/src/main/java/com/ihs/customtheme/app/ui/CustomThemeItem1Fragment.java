package com.ihs.customtheme.app.ui;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ihs.customtheme.R;
import com.ihs.inputmethod.theme.HSCustomThemeItemBase;

import java.util.List;


public class CustomThemeItem1Fragment extends BaseFragment {

    private RecyclerView mViews;
    private CustomThemeItemAdapter mAdapter;
    private List<HSCustomThemeItemBase> mViewItems;
    private OnRecyclerViewItemClickListener mOnViewItemClickListener;
    private int itemViewLayout;

    private String titleCancel;
    private String titleOK;
    private String titleHead;
    private String title1;
    private boolean backButtonVisible;
    private boolean nextButtonVisible;

    private OnTitleClickListener headButtonClickListener;

    public void setItemViewsParams(final int itemViewLayout,
                                   final List<HSCustomThemeItemBase> viewItems,
                                   final OnRecyclerViewItemClickListener onViewItemClickListener) {
        this.itemViewLayout = itemViewLayout;
        mOnViewItemClickListener = onViewItemClickListener;
        mViewItems = viewItems;
    }

    public void setViewParams(final String titleCancel, final String titleHead, final String titleOK,
                              final boolean backButtonVisible, final boolean nextButtonVisible,
                              final String title1, final OnTitleClickListener listener) {
        this.nextButtonVisible = nextButtonVisible;
        this.backButtonVisible = backButtonVisible;
        this.title1 = title1;
        this.titleHead = titleHead;
        this.titleCancel = titleCancel;
        this.titleOK = titleOK;
        this.headButtonClickListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.custom_theme_1_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViews = (RecyclerView) view.findViewById(R.id.custom_theme_item_recycler_view_1);
        final int col = getResources().getInteger(R.integer.custom_theme_background_grid_column_count);
        mViews.setLayoutManager(new GridLayoutManager(getActivity(), col, GridLayoutManager.VERTICAL, false));
        mViews.setAdapter(mAdapter);

        final TextView text1 = (TextView) view.findViewById(R.id.custom_theme_item_title_1);
        text1.setText(title1);

        final HSCommonHeaderView headerView = (HSCommonHeaderView) view.findViewById(R.id.custom_theme_head_common);
        headerView.setText(titleCancel, titleHead, titleOK);
        headerView.setButtonVisibility(backButtonVisible, nextButtonVisible);
        headerView.setHeadButtonClickListener(headButtonClickListener);

        resetRecyclerViewSize(view, mViews);
    }

    private void resetRecyclerViewSize(final View view, final RecyclerView recyclerView) {
        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                GridLayoutManager layoutManager = ((GridLayoutManager) recyclerView.getLayoutManager());
                int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                CustomThemeItemViewHolder viewHolder = (CustomThemeItemViewHolder) recyclerView.findViewHolderForLayoutPosition(firstVisiblePosition);
                ViewGroup.LayoutParams layoutParams = recyclerView.getLayoutParams();
                int groupCount = (int) (Math.floor((layoutManager.getItemCount() - 1) / layoutManager.getSpanCount()) + 1);
                if (viewHolder != null) {
                    layoutParams.height = viewHolder.itemView.getHeight() * groupCount;
                    recyclerView.setLayoutParams(layoutParams);
                    view.removeOnLayoutChangeListener(this);
                }
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new CustomThemeItemAdapter(itemViewLayout, mViewItems);
        mAdapter.setOnItemClickListener(mOnViewItemClickListener);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void scrollToPosition(int position, boolean smooth) {
        if (position < mAdapter.getItemCount()) {
            if (smooth) {
                mViews.smoothScrollToPosition(position);
            } else {
                mViews.scrollToPosition(position);
            }
        }
    }

    /**
     * 刷新数据，iap购买成功会回调
     */
    @Override
    void notifyDataSetChange() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * for custom background camera and album
     * @param type
     */
    public void resetBackgroundCheckState(int type) {
        mAdapter.resetBackgroundCheckState(type);
    }
}
