package com.ihs.customtheme.panels.theme;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.customtheme.R;
import com.ihs.inputmethod.api.HSInputMethodTheme;
import com.ihs.inputmethod.theme.HSKeyboardThemeManager;



public class HSThemeSelectRecyclerViewHolder extends RecyclerView.ViewHolder {
    private View mView;
    private TextView mTitleTextView;
    private ImageView mCheckImageView;
    private ImageView mContentImageView;

    private HSThemeSelectRecyclerAdapter.ThemeSelectViewItem mItem;
    private int numOfColums;

    public HSThemeSelectRecyclerViewHolder(View view, boolean isHeaderView) {
        super(view);
        mView = view;


        if (isHeaderView) {
            mTitleTextView = (TextView) mView.findViewById(R.id.theme_select_recycler_item_header_title_text_view);
        } else {
            mContentImageView = (ImageView) mView.findViewById(R.id.theme_select_recycler_preview);
            mCheckImageView = (ImageView) mView.findViewById(R.id.theme_select_recycler_selected);
        }
    }

    public void bindItem(HSThemeSelectRecyclerAdapter.ThemeSelectViewItem item, int numOfColums) {
        mItem = item;
        this.numOfColums = numOfColums;

        if (item.isHeader) {
            mTitleTextView.setText(item.title);
        } else {
            if (item.isAddTheme) {
                Drawable addThemeDrawable = HSApplication.getContext().getResources().getDrawable(R.drawable.preview_keyboard_theme_create);
                mContentImageView.setImageDrawable(HSKeyboardThemeManager.getDimmedDrawable(addThemeDrawable));
                mCheckImageView.setVisibility(View.GONE);
            } else {
                mContentImageView.setImageDrawable(HSInputMethodTheme.getThemePreviewDrawable(mItem.themeIndex));
                mCheckImageView.setVisibility(HSInputMethodTheme.getCurrentThemeIndex() == mItem.themeIndex ? View.VISIBLE : View.GONE);
            }
            resetLayoutParams(new LayoutParam(mCheckImageView, 280f, 150), new LayoutParam(mContentImageView, 260f, 130));
        }
    }

    class LayoutParam {
        public ImageView iv;
        public float width;
        public float height;

        public LayoutParam(ImageView iv, float width, float height) {
            this.iv = iv;
            this.width = width;
            this.height = height;
        }
    }

    /**
     * reset iv2's Layout base on iv1
     *
     * @param param1
     * @param param2
     */
    private void resetLayoutParams(LayoutParam param1, LayoutParam param2) {
        ImageView iv1 = param1.iv, iv2 = param2.iv;
        float width1 = param1.width, width2 = param2.width, height1 = param1.height, height2 = param2.height;
        DisplayMetrics displayMetrics = iv1.getContext().getResources().getDisplayMetrics();
        int viewWidth = displayMetrics.widthPixels / numOfColums;


        iv1.measure(-1, -1);
        ViewGroup.LayoutParams layoutParams = iv1.getLayoutParams();
        layoutParams.width = viewWidth;
        layoutParams.height = (int) (viewWidth * height1 / width1);
        iv1.setLayoutParams(layoutParams);

        mView.setLayoutParams(layoutParams);


        ViewGroup.LayoutParams params = iv2.getLayoutParams();
        params.width = (int) (layoutParams.width * width2 / width1);
        params.height = (int) (layoutParams.height * height2 / height1);
        iv2.setLayoutParams(params);
    }

}
