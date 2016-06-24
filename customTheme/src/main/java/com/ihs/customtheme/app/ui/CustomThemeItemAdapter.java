package com.ihs.customtheme.app.ui;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import com.ihs.inputmethod.theme.HSCustomThemeItemBase;

import java.util.List;

public final class CustomThemeItemAdapter extends RecyclerView.Adapter<CustomThemeItemViewHolder> {

    private final List<HSCustomThemeItemBase> mItems;
    private OnRecyclerViewItemClickListener mOnItemClickListener;
    private final int itemViewLayout;


    public CustomThemeItemAdapter(final int itemViewLayout, final List<HSCustomThemeItemBase> items) {
        this.itemViewLayout = itemViewLayout;
        mItems = items;
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    @Override
    public CustomThemeItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CustomThemeItemViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(itemViewLayout, parent, false));
    }

    @Override
    public void onBindViewHolder(final CustomThemeItemViewHolder holder, final int position) {
        final HSCustomThemeItemBase item = mItems.get(position);
        final View itemView = holder.itemView;
        itemView.setTag(position);

        holder.bindItem(item);

        itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        doSelectAnimationOnItemViewTouch(v);
                        return true;
                    case MotionEvent.ACTION_UP:
                        doSelectAnimationOnItemViewRelease(v);
                        if (mOnItemClickListener != null) {
                            mOnItemClickListener.onRecyclerViewItemClick(holder);
                        }
                        refreshCheckedState(position);
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        doSelectAnimationOnItemViewRelease(v);
                        return true;
                }
                return false;
            }
        });


    }

    private void refreshCheckedState(int position) {
        for (int i = 0; i < mItems.size(); i++) {
            if(i!=position&&mItems.get(i).isChecked){
                mItems.get(i).isChecked=false;
                notifyItemChanged(i);
            }

            if(i==position){
                mItems.get(i).isChecked=true;
                notifyItemChanged(i);
            }

        }
    }


    @Override
    public int getItemCount() {
        if (mItems == null) {
            return 0;
        }
        return mItems.size();
    }

    private void doSelectAnimationOnItemViewRelease(final View view) {
        final ScaleAnimation bigScale = new ScaleAnimation(0.9f, 1.05f, 0.9f, 1.05f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        bigScale.setDuration(120);

        final ScaleAnimation reset = new ScaleAnimation(1.05f, 1f, 1.05f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        reset.setDuration(80);

        bigScale.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.startAnimation(reset);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        reset.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(bigScale);
    }

    private void doSelectAnimationOnItemViewTouch(final View view) {
        final ScaleAnimation smallScale = new ScaleAnimation(1f, 0.9f, 1f, 0.9f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        smallScale.setFillAfter(true);
        smallScale.setDuration(0);
        view.startAnimation(smallScale);
    }

}
