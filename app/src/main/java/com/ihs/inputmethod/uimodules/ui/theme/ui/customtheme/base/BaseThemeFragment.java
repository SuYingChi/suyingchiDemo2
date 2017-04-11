package com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.animator.AlphaInAnimator;
import com.ihs.inputmethod.uimodules.ui.theme.iap.IAPManager;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.CustomThemeActivity;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base.ThemePageItem.CategoryItem;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.common.Category;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.common.CategoryItemViewProvider;
import com.keyboard.core.themes.custom.KCCustomThemeData;
import com.keyboard.core.themes.custom.elements.KCBaseElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.drakeet.multitype.Items;
import me.drakeet.multitype.MultiTypeAdapter;


/**
 * Created by chenyuanming on 31/10/2016.
 */

public abstract class BaseThemeFragment extends Fragment implements INotificationObserver {
    public static final int SPAN_COUNT = 6;
    RecyclerView recyclerView;
    private MultiTypeAdapter adapter;
    private AlphaInAnimator animator;
    Items items;
    private ThemePageItem pageItem;
    private Map<String, Object> chosedItems = new HashMap<>();


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

    protected void showKeyboard() {
        if (getCustomThemeActivity() != null) {
            getCustomThemeActivity().showKeyboard();
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
        HSGlobalNotificationCenter.addObserver(IAPManager.NOTIFICATION_IAP_PURCHASE_SUCCESS, this);
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
    KCCustomThemeData customThemeData;

    public void setCustomThemeData(KCCustomThemeData customThemeData) {
        this.customThemeData = customThemeData;
    }

    public KCCustomThemeData getCustomThemeData() {
        return customThemeData;
    }

    public void addChosenItem(KCBaseElement item) {
        chosedItems.put(item.getTypeName(), item);
    }

    public Collection<Object> getChosenItems() {
        return chosedItems.values();
    }

    protected abstract ThemePageItem initiateThemePageItem();

    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    protected Handler handler = new Handler();

    private void initRecyclerView(View rootView) {
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

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
        recyclerView.setAdapter(adapter);
        animator = new AlphaInAnimator();
        animator.setAddDuration(80);
        recyclerView.setItemAnimator(animator);
        recyclerView.setPadding(0, 0, 0, HSResourceUtils.getDefaultKeyboardHeight(getResources()));
        loadData();
    }

    private List data = new ArrayList();

    private void loadData() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                List<CategoryItem<?>> categories = getPageItem().categories;
                if (categories != null) {
                    for (final CategoryItem<?> category : categories) {
                        adapter.register(category.itemClazz, category.provider);
                        Category category1 = new Category(category.categoryName);
                        if (!data.contains(category1)) {
                            data.add(category1);
                        }
                        data.addAll(category.data);
                    }
                    loadItem();
                }
            }
        }, 1);
    }


    private int index = 0;

    private void loadItem() {
        if (index < data.size()) {
            items.add(data.get(index));
            adapter.notifyItemInserted(index + 1);
            index = index + 1;
            loadItem();
        }
    }

    @Override
    public void onDestroy() {
        HSGlobalNotificationCenter.removeObserver(IAPManager.NOTIFICATION_IAP_PURCHASE_SUCCESS, this);
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
        if (IAPManager.NOTIFICATION_IAP_PURCHASE_SUCCESS.equals(s)) {
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


    /**
     * 自定义主题的预览图(背景和字体)下载完收到通知，子类自己重写，
     */
    protected void notifyCustomThemePreviewDownloadFinished(KCBaseElement item) {
        notifyAdapterOnMainThread();
    }

    Runnable dataChangeRunnable = new Runnable() {
        @Override
        public void run() {
            notifyAdapterOnMainThread();
        }
    };

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

    public <I extends KCBaseElement> void showPromptPurchaseView(I item) {
        if (getCustomThemeActivity() != null) {
            getCustomThemeActivity().showPromptPurchaseView(item);
        }
    }


}
