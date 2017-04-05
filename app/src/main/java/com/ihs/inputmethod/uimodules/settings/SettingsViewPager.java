package com.ihs.inputmethod.uimodules.settings;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Space;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.viewpagerindicator.IconPagerAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by chenyuanming on 22/09/2016.
 */

public class SettingsViewPager extends ViewPager {
    int rowCount = 2;
    int colCount = 4;
    List<View> pageViews = new ArrayList<>();

    List<ViewItem> items;

    public SettingsViewPager(Context context) {
        super(context);
    }


    public SettingsViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    private List<View> createViewGroupsFor(int rowCount, int colCount, List<ViewItem> datas) {
        List<View> views = new ArrayList<>();
        Context context = getContext();

        int pageCount = (int) Math.ceil(1.0 * datas.size() / (rowCount * colCount));

        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
            LinearLayout outerLayout = new LinearLayout(context);
            outerLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            outerLayout.setOrientation(LinearLayout.VERTICAL);
            outerLayout.setGravity(Gravity.CENTER);
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                LinearLayout innerLayout = new LinearLayout(context);
                innerLayout.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams innerLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 15);
                innerLayoutParams.gravity = Gravity.CENTER;
                innerLayout.setLayoutParams(innerLayoutParams);

                for (int colIndex = 0; colIndex < colCount; colIndex++) {
                    int position = pageIndex * rowCount * colCount + rowIndex * colCount + colIndex;
                    View view;
                    if (position < datas.size()) {
                        ViewItem item = datas.get(position);
                        view = item.createView(context);
                    } else {
                        view = new Space(context);
                    }

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    params.weight = 1;
//                    params.topMargin = HSYamlUtils.dp2px(10);
                    params.gravity = Gravity.TOP;
                    view.setLayoutParams(params);
                    innerLayout.addView(view);

                }

                outerLayout.addView(innerLayout);
                if (rowIndex % 2 == 0) { //中间插入一个空白的View
                    View blankView = new View(HSApplication.getContext());
                    outerLayout.addView(blankView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
                }
            }
            views.add(outerLayout);
        }

        return views;
    }

    public int getPageCount() {
        return pageViews == null ? 0 : pageViews.size();
    }

    public void setItems(List<ViewItem> items) {
        this.items = items;
        pageViews = createViewGroupsFor(rowCount, colCount, items);

        setAdapter(new SettingsPagerAdapter(pageViews));
    }

    public void removeAds() {
        this.items.remove(ViewItemBuilder.getAdsItem());
        setItems(this.items);
    }

    public static class SettingsPagerAdapter extends PagerAdapter implements IconPagerAdapter {

        List<View> views;

        public SettingsPagerAdapter(List<View> views) {
            this.views = views;
        }

        @Override
        public int getIconResId(int index) {
            String drawableName = "settings_indicator_selector";
            if (!HSKeyboardThemeManager.getCurrentTheme().isDarkBg()) {
                drawableName += "_light";
            }
            return HSApplication.getContext().getResources().getIdentifier(drawableName, "drawable", HSApplication.getContext().getPackageName());
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            (container).removeView(views.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            (container).addView(views.get(position));
            return views.get(position);
        }
    }
}
