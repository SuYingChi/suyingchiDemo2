package com.keyboard.inputmethod.panels.gif.emojisearch;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.ihs.inputmethod.api.HSInputMethodTheme;
import com.keyboard.inputmethod.panels.gif.model.GifItem;
import com.keyboard.rainbow.R;

public final class ESPalettesView extends LinearLayout implements ViewPager.OnPageChangeListener, ESPageGridView.OnEmojiClickListener {

    private ViewPager mViewPager;
    private ESLayoutParams mESLayoutParams;
    private ESPageIndicatorView mESPageIndicatorView;
    private ESPageGridView.OnEmojiClickListener mListener;

    public ESPalettesView(Context context) {
        this(context,null);
    }

    public ESPalettesView(Context context, AttributeSet attrs) {
        this(context, attrs,R.attr.emojiPalettesViewStyle);
    }

    public ESPalettesView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, R.attr.emojiPalettesViewStyle);
        init(context);
    }

    public void destroy() {
        mListener = null;
    }

    private void init(Context context) {
        mESLayoutParams = new ESLayoutParams(context.getResources());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // view pager
        mViewPager = (ViewPager) findViewById(R.id.emoji_viewpager);
        mViewPager.setAdapter(new ESPalettesViewAdapter(this, mESLayoutParams));
        mViewPager.setOnPageChangeListener(this);
        mESLayoutParams.setPagerProperties(mViewPager);

        // page id view
        mESPageIndicatorView = (ESPageIndicatorView) findViewById(R.id.emoji_search_page_id_view);
        mESPageIndicatorView.setColors(0xff1ea0cd, 0x00000000);
    }

    @Override
    public void onPageSelected(int position) {
        mESPageIndicatorView.setPageId(ESManager.getInstance().getPageCount(), position, 0.0f);
    }

    @Override
    public void onPageScrollStateChanged(int state) {}

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mESPageIndicatorView.setPageId(ESManager.getInstance().getPageCount(), position, positionOffset);
    }

    @Override
    public void onEmojiClick(final GifItem emoji) {
        if (mListener != null) {
            mListener.onEmojiClick(emoji);
        }
    }

    public void prepare() {
        this.setBackgroundColor(HSInputMethodTheme.getThemeMainColor());
        mViewPager.getAdapter().notifyDataSetChanged();
        mViewPager.setCurrentItem(0);
    }

    public void setListener(final ESPageGridView.OnEmojiClickListener listener) {
        mListener = listener;
    }
}