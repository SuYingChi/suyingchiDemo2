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

public class CustomThemeItem2Fragment extends BaseFragment {

    private RecyclerView mViews1;
    private RecyclerView mViews2;
    private CustomThemeItemAdapter mAdapter1;
    private CustomThemeItemAdapter mAdapter2;
    private List<HSCustomThemeItemBase> mViewItems1;
    private List<HSCustomThemeItemBase> mViewItems2;
    private OnItemClickListener mOnViewItem1ClickListener;
    private OnItemClickListener mOnViewItem2ClickListener;
    private int itemViewLayout1;
    private int itemViewLayout2;

    private String titleCancel;
    private String titleOK;
    private String titleHead;
    private String title1;
    private String title2;
    private boolean backButtonVisible;
    private boolean nextButtonVisible;

    private OnHeadButtonClickListener headButtonClickListener;

    public void setItemViewsParams(final int itemViewLayout1,
                                   final int itemViewLayout2,
                                   final List<HSCustomThemeItemBase> viewItems1,
                                   final List<HSCustomThemeItemBase> viewItems2,
                                   final OnItemClickListener onViewItemClickListener1,
                                   final OnItemClickListener onViewItemClickListener2
    ) {
        this.itemViewLayout1 = itemViewLayout1;
        this.itemViewLayout2 = itemViewLayout2;
        mOnViewItem1ClickListener = onViewItemClickListener1;
        mOnViewItem2ClickListener = onViewItemClickListener2;
        mViewItems1 = viewItems1;
        mViewItems2 = viewItems2;
    }

    public void setViewParams(final String titleCancel, final String titleHead, final String titleOK,
                              final boolean backButtonVisible, final boolean nextButtonVisible,
                              final String title1, final String title2, final OnHeadButtonClickListener listener) {
        this.nextButtonVisible = nextButtonVisible;
        this.backButtonVisible = backButtonVisible;
        this.title1 = title1;
        this.title2 = title2;
        this.titleHead = titleHead;
        this.titleCancel = titleCancel;
        this.titleOK = titleOK;
        this.headButtonClickListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.custom_theme_2_fragment, container, false);
    }

    private static final String TAG = "CustomThemeItem2Fragment";

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViews1 = (RecyclerView) view.findViewById(R.id.custom_theme_item_recycler_view_1);
        final int col = getResources().getInteger(R.integer.custom_theme_background_grid_column_count);
        mViews1.setLayoutManager(new GridLayoutManager(getActivity(), col, GridLayoutManager.VERTICAL, false));
        mViews1.setAdapter(mAdapter1);


        mViews2 = (RecyclerView) view.findViewById(R.id.custom_theme_item_recycler_view_2);
        mViews2.setLayoutManager(new GridLayoutManager(getActivity(), col, GridLayoutManager.VERTICAL, false));
        mViews2.setAdapter(mAdapter2);

        final TextView text1 = (TextView) view.findViewById(R.id.custom_theme_item_title_1);
        text1.setText(title1);
        final TextView text2 = (TextView) view.findViewById(R.id.custom_theme_item_title_2);
        text2.setText(title2);

        final HSCommonHeaderView headerView = (HSCommonHeaderView) view.findViewById(R.id.custom_theme_head_common);
        headerView.setText(titleCancel, titleHead, titleOK);
        headerView.setButtonVisibility(backButtonVisible, nextButtonVisible);
        headerView.setHeadButtonClickListener(headButtonClickListener);
        resetRecyclerViewSize(view, mViews1);
        resetRecyclerViewSize(view, mViews2);
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
                layoutParams.height = viewHolder.itemView.getHeight() * groupCount;
                recyclerView.setLayoutParams(layoutParams);
                view.removeOnLayoutChangeListener(this);
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter1 = new CustomThemeItemAdapter(itemViewLayout1, mViewItems1);
        mAdapter1.setOnItemClickListener(mOnViewItem1ClickListener);
        mAdapter2 = new CustomThemeItemAdapter(itemViewLayout2, mViewItems2);
        mAdapter2.setOnItemClickListener(mOnViewItem2ClickListener);
    }

    /**
     * 刷新数据，iap购买成功会通知
     */
    @Override
    void notifyDataSetChange() {
        if (mAdapter1 != null) {
            mAdapter1.notifyDataSetChanged();
        }
        if (mAdapter2 != null) {
            mAdapter2.notifyDataSetChanged();
        }
    }
}
