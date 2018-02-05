package com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.common.Category;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.common.CategoryItemViewProvider;
import com.ihs.inputmethod.uimodules.ui.theme.utils.CompatUtils;
import com.keyboard.core.themes.custom.KCCustomThemeData;
import com.keyboard.core.themes.custom.elements.KCBaseElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.drakeet.multitype.Items;
import me.drakeet.multitype.MultiTypeAdapter;

import static com.ihs.keyboardutils.iap.RemoveAdsManager.NOTIFICATION_REMOVEADS_PURCHASED;


/**
 * Created by chenyuanming on 31/10/2016.
 */

public abstract class BaseThemeFragment extends Fragment implements INotificationObserver {
    public static final int SPAN_COUNT = 6;
    protected Handler handler = new Handler();
    RecyclerView recyclerView;
    Items items;
    KCCustomThemeData customThemeData;
    private MultiTypeAdapter adapter;
    Runnable dataChangeRunnable = new Runnable() {
        @Override
        public void run() {
            notifyAdapterOnMainThread();
        }
    };
    private ThemePageItem pageItem;
    private Map<String, Object> chosedItems = new HashMap<>();
    private List data = new ArrayList();
    private int index = 0;

    public BaseThemeFragment() {
    }

    private CustomThemeActivity getCustomThemeActivity() {
        if (!isDetached()) {
            return (CustomThemeActivity) getActivity();
        } else {
            return null;
        }
    }

    protected void refreshKeyboardView() {
        if (getCustomThemeActivity() != null) {
            getCustomThemeActivity().refreshKeyboardView();
        }
    }

    public void setHeaderNextEnable(boolean enable) {
        if (getCustomThemeActivity() != null) {
            getCustomThemeActivity().setHeaderNextEnable(enable);
        }
    }

    public void refreshHeaderNextButtonState() {
        boolean isNextButtonEnabled = true;
        for (Object item : chosedItems.values()) {
            if (item instanceof KCBaseElement) {
                isNextButtonEnabled &= ((KCBaseElement) item).hasLocalContent();
            }
        }
        setHeaderNextEnable(isNextButtonEnabled);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HSGlobalNotificationCenter.addObserver(NOTIFICATION_REMOVEADS_PURCHASED, this);
        HSGlobalNotificationCenter.addObserver(HSKeyboardThemeManager.HS_NOTIFICATION_CUSTOM_THEME_PREVIEW_DOWNLOAD_FINISHED, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.frg_base_customt_theme, null);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    initRecyclerView(view);
                }
            }
        }, 0);

        return view;
    }

    private ThemePageItem getPageItem() {
        if (pageItem == null) {
            pageItem = initiateThemePageItem();
        }
        return pageItem;
    }

    public KCCustomThemeData getCustomThemeData() {
        return customThemeData;
    }

    public void setCustomThemeData(KCCustomThemeData customThemeData) {
        this.customThemeData = customThemeData;
    }

    public void addChosenItem(KCBaseElement item) {
        chosedItems.put(item.getTypeName(), item);
    }

    protected abstract ThemePageItem initiateThemePageItem();

    private void initRecyclerView(View rootView) {
        recyclerView = rootView.findViewById(R.id.recyclerView);

        HSLog.d("initRecyclerView");
        items = new Items();
        adapter = new MultiTypeAdapter(items);
        adapter.register(Category.class, new CategoryItemViewProvider());


        final GridLayoutManager layoutManager = new GridLayoutManager(getContext(), SPAN_COUNT);
        GridLayoutManager.SpanSizeLookup spanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                Object item = items.get(position);
                return (item instanceof Category) ? SPAN_COUNT : 1;
            }
        };
        layoutManager.setSpanSizeLookup(spanSizeLookup);
        recyclerView.setLayoutManager(layoutManager);
        int gap = (int) HSApplication.getContext().getResources().getDimension(R.dimen.custom_theme_item_margin);
        recyclerView.addItemDecoration(new BaseItemDecoration(gap, gap));
        recyclerView.setAdapter(adapter);
        recyclerView.setPadding(0, 0, 0, HSResourceUtils.getDefaultKeyboardHeight(getResources()));
        loadData();
    }

    private void loadData() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                List<ThemePageItem.CategoryItem<?>> categories = getPageItem().categories;
                if (categories != null) {
                    for (final ThemePageItem.CategoryItem<?> category : categories) {
                        adapter.register(category.itemClazz, category.provider);
                        Category category1 = new Category(category.categoryName);
                        if (!data.contains(category1)) {
                            data.add(category1);
                        }
                        data.addAll(category.data);
                    }
                    items.addAll(data);
                    adapter.notifyDataSetChanged();
                }
            }
        }, 1);
    }

    @Override
    public void onDestroy() {
        HSGlobalNotificationCenter.removeObserver(NOTIFICATION_REMOVEADS_PURCHASED, this);
        HSGlobalNotificationCenter.removeObserver(this);
        super.onDestroy();
    }

    /**
     * 购买成功的回调，用于刷新数据
     *
     * @param s
     * @param hsBundle
     */
    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if (NOTIFICATION_REMOVEADS_PURCHASED.equals(s)) {
            notifyDataSetChange();
        } else if (HSKeyboardThemeManager.HS_NOTIFICATION_CUSTOM_THEME_PREVIEW_DOWNLOAD_FINISHED.equals(s)) {
//            Object object = hsBundle.getObject(KCCustomThemeManager.CUSTOM_THEME_PREVIEW_DOWNLOAD_KEY);
//            if (object != null && object instanceof HSCustomThemeItemBase) {
//                HSCustomThemeItemBase item = (HSCustomThemeItemBase) object;
//                notifyCustomThemePreviewDownloadFinished(item);
//            }
        }
    }

    /**
     * IAP购买成功之后会收到通知,需要刷新数据，子类自己重写，
     */
    protected void notifyDataSetChange() {
        notifyAdapterOnMainThread();
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    /**
//     * 自定义主题的预览图(背景和字体)下载完收到通知，子类自己重写，
//     */
//    protected void notifyCustomThemePreviewDownloadFinished(KCBaseElement item) {
//        notifyAdapterOnMainThread();
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    public void notifyAdapterOnMainThread() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (adapter != null) {
                    if (!recyclerView.isComputingLayout()) {
                        adapter.notifyDataSetChanged();
                    } else {
                        handler.removeCallbacks(dataChangeRunnable);
                        handler.postDelayed(dataChangeRunnable, 5);
                    }
                }
            }
        });
    }


    public static class BaseItemDecoration extends RecyclerView.ItemDecoration {

        private int rowGap;
        private int columnGap;

        public BaseItemDecoration(int rowGap, int columnGap) {
            this.rowGap = CompatUtils.updateGapValueAccordingVersion(rowGap);
            this.columnGap = CompatUtils.updateGapValueAccordingVersion(columnGap);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.set(columnGap, rowGap, columnGap, rowGap);
        }
    }
}
