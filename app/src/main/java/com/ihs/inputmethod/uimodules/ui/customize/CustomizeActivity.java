package com.ihs.inputmethod.uimodules.ui.customize;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.acb.call.HomeKeyWatcher;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.inputmethod.feature.common.ViewUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.customize.util.BottomNavigationViewHelper;
import com.ihs.inputmethod.uimodules.ui.customize.view.CustomizeContentView;
import com.ihs.inputmethod.uimodules.ui.customize.view.LayoutWrapper;
import com.ihs.inputmethod.uimodules.ui.customize.view.OnlineWallpaperPage;
import com.ihs.keyboardutils.utils.CommonUtils;

/**
 * Created by guonan.lv on 17/9/1.
 */

public class CustomizeActivity extends BaseCustomizeActivity implements INotificationObserver, BottomNavigationView.OnNavigationItemSelectedListener {

    public static final int REQUEST_CODE_SYSTEM_THEME_ALERT = 1;
    public static final int REQUEST_CODE_PICK_WALLPAPER = 2;
    public static final int REQUEST_CODE_APPLY_3D_WALLPAPER = 3;

    public static final String NOTIFICATION_CUSTOMIZE_ACTIVITY_DESTROY = "notification_customize_activity_onDestroy";
    public static final String PREF_KEY_WALLPAPER_LAUNCHED_FROM_SHORTCUT = "wallpaper_launched_from_shortcut";
    public static final String PREF_KEY_THEME_LAUNCHED_FROM_SHORTCUT = "theme_launched_from_shortcut";

    private static final SparseIntArray ITEMS_INDEX_MAP = new SparseIntArray(5);
    private static final SparseArray<String> ITEMS_FLURRY_NAME_MAP = new SparseArray<>(5);

    public static final int TAB_INDEX_THEME = 0;
    public static final int TAB_INDEX_WALLPAPER = 0;
    public static final int TAB_INDEX_KEYBOARD = 2;
    public static final int TAB_INDEX_LOCKER = 1;
    public static final int TAB_INDEX_LOCAL = 4;

    static {
//        ITEMS_INDEX_MAP.put(R.id.customize_bottom_bar_themes, TAB_INDEX_THEME);
        ITEMS_INDEX_MAP.put(R.id.customize_bottom_bar_wallpapers, TAB_INDEX_WALLPAPER);
        ITEMS_INDEX_MAP.put(R.id.customize_bottom_bar_locker, TAB_INDEX_LOCKER);

//        ITEMS_FLURRY_NAME_MAP.put(R.id.customize_bottom_bar_themes, "Theme");
        ITEMS_FLURRY_NAME_MAP.put(R.id.customize_bottom_bar_wallpapers, "Wallpaper");
        ITEMS_FLURRY_NAME_MAP.put(R.id.customize_bottom_bar_locker, "Locker");
    }

    private CustomizeContentView mContent;
    private BottomNavigationView mBottomBar;

    private LayoutWrapper mLayoutWrapper;

    private int mViewIndex;
    public int mThemeTabIndex;
    public int mWallpaperTabIndex;
    private HomeKeyWatcher mHomeKeyWatcher;

    public static void bindScrollListener(Context context, RecyclerView recyclerView, boolean hasBottom) {
        if (context instanceof CustomizeActivity) {
            ((CustomizeActivity) context).getLayoutWrapper().attachToRecyclerView(recyclerView, hasBottom);
        }
    }

    public LayoutWrapper getLayoutWrapper() {
        return mLayoutWrapper;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize);
        mContent = ViewUtils.findViewById(this, R.id.customize_content);
        mContent.setChildSelected(0);
        mBottomBar = ViewUtils.findViewById(this, R.id.bottom_bar);
        BottomNavigationViewHelper.disableShiftMode(mBottomBar);
//        BottomNavigationViewHelper.setTypeface(mBottomBar, FontUtils.getTypeface(FontUtils.Font.PROXIMA_NOVA_REGULAR));
        mLayoutWrapper = new LayoutWrapper(mBottomBar, getResources().getDimensionPixelSize(R.dimen.bottom_bar_default_height), CommonUtils.pxFromDp(3.3f));

        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");

        mHomeKeyWatcher = new HomeKeyWatcher(this);
        mHomeKeyWatcher.setOnHomePressedListener(new HomeKeyWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {

            }

            @Override
            public void onRecentPressed() {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mLayoutWrapper != null) {
            mLayoutWrapper.show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mHomeKeyWatcher.startWatch();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (mLayoutWrapper != null) {
            mLayoutWrapper.show();
        }
    }

    public ViewGroup getBottomBar() {
        return mBottomBar;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_CUSTOMIZE_ACTIVITY_DESTROY);
        HSGlobalNotificationCenter.removeObserver(this);

        for (int index = 0; index < mContent.getChildCount(); index++) {
            View view = mContent.getChildAt(index);
            if (view instanceof OnlineWallpaperPage) {
                ((OnlineWallpaperPage) view).onDestroy();
            }
        }

        mHomeKeyWatcher.destroy();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        int index = ITEMS_INDEX_MAP.get(itemId);
        boolean viewIndexUpdated = false;
        if (mViewIndex != index) {
            mViewIndex = index;
            viewIndexUpdated = true;
        }
        mContent.setChildSelected(index);
        // reset icon to origins
        Menu menu = mBottomBar.getMenu();
        setMenuItemIconDrawable(menu, R.id.customize_bottom_bar_wallpapers, R.drawable.customize_wallpaper);
//        setMenuItemIconDrawable(menu, R.id.customize_bottom_bar_themes, R.drawable.customize_theme);
        setMenuItemIconDrawable(menu, R.id.customize_bottom_bar_locker, R.drawable.customize_locker);

        switch (item.getItemId()) {
            case R.id.customize_bottom_bar_wallpapers:
                item.setIcon(R.drawable.customize_wallpaper_h);
                break;
//            case R.id.customize_bottom_bar_themes:
//                item.setIcon(R.drawable.customize_theme_h);
//                break;
            case R.id.customize_bottom_bar_locker:
                item.setIcon(R.drawable.customize_locker_h);
                break;
        }
        return true;
    }

    private void setMenuItemIconDrawable(Menu menu, @IdRes int itemId, @DrawableRes int drawableId) {
        MenuItem item = menu.findItem(itemId);
        if (item != null) {
            item.setIcon(drawableId);
        }
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {

    }
}
