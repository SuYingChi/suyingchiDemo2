package com.ihs.inputmethod.feature.boost.plus;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.ColorRes;
import android.support.annotation.MenuRes;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.honeycomb.launcher.BuildConfig;
import com.honeycomb.launcher.R;
import com.honeycomb.launcher.animation.AnimatorListenerAdapter;
import com.honeycomb.launcher.animation.LauncherAnimUtils;
import com.honeycomb.launcher.animation.SpringInterpolator;
import com.honeycomb.launcher.boost.BoostTipUtils;
import com.honeycomb.launcher.customize.view.ProgressFrameLayout;
import com.honeycomb.launcher.dialog.BoostPlusAccessibilityDialog;
import com.honeycomb.launcher.dialog.FloatWindowDialog;
import com.honeycomb.launcher.dialog.FloatWindowManager;
import com.honeycomb.launcher.dialog.LauncherFloatWindowManager;
import com.honeycomb.launcher.dialog.LauncherTipManager;
import com.honeycomb.launcher.ihs.BasePermissionActivity;
import com.honeycomb.launcher.util.ActivityUtils;
import com.honeycomb.launcher.util.FormatSizeBuilder;
import com.honeycomb.launcher.util.LauncherPackageManager;
import com.honeycomb.launcher.util.PermissionUtils;
import com.honeycomb.launcher.util.StringUtils;
import com.honeycomb.launcher.util.Thunk;
import com.honeycomb.launcher.util.ToastUtils;
import com.honeycomb.launcher.util.Utils;
import com.honeycomb.launcher.util.ViewUtils;
import com.honeycomb.launcher.view.RecyclerViewAnimator;
import com.honeycomb.launcher.view.recyclerview.SafeLinearLayoutManager;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.device.clean.accessibility.HSAccTaskManager;
import com.ihs.device.clean.memory.HSAppMemory;
import com.ihs.device.clean.memory.HSAppMemoryManager;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import hugo.weaving.DebugLog;

public class BoostPlusActivity extends BasePermissionActivity
        implements BoostPlusContracts.HomePage, BoostPlusContracts.View,
        View.OnClickListener, View.OnLongClickListener, CompoundButton.OnCheckedChangeListener, INotificationObserver {

    public static final String TAG = BoostPlusActivity.class.getSimpleName();

    /**
     * Keys for data sent over startActivity intent that is used after a clean process has been cancelled.
     */
    private static final String INTENT_KEY_LAST_SELECTION_CANCELLED = "last_selection_cancelled";
    private static final String BUNDLE_KEY_CANCEL_CLEAN = "cancel_clean";
    private static final String BUNDLE_KEY_CANCEL_CLEAN_RESULT_APPS = "cancel_clean_result_apps";
    private static final String BUNDLE_KEY_CANCEL_CLEAN_SELECTED_APPS = "cancel_clean_selected_apps";

    /**
     * Key for data saved with onSaveInstanceState during a clean process that is used for re-scan.
     */
    private static final String BUNDLE_KEY_LAST_SELECTION_CLEANED = "last_selection_cleaned";

    /**
     * Sent when clean animation and result page is done and user is returning to this activity.
     */
    public static final String NOTIFICATION_RETURN_FROM_CLEAN = "return_from_clean";

    @SuppressWarnings("PointlessBooleanExpression")
    private static final boolean DEBUG_NO_RUNNING_APPS = false && BuildConfig.DEBUG;

    /** Time constant (in ms) used to add some delay and fading-in effects to hide the lag in animation at the beginning. */
    private static final long ANIMATION_OPT_TIME = 800;

    private BoostPlusContracts.Presenter mPresenter;

    @Thunk ScanResultFilter mScanResultFilter;
    private List<String> mThirdPartyAllowList;

    private boolean mScanAfterClean;
    private ScanListener mScanListener;

    private ArrayList<HSAppMemory> mRunningApps = new ArrayList<>(16);
    private ArrayList<HSAppMemory> mSelectedApps = new ArrayList<>(8);
    private ArrayList<HSAppMemory> mSelectSuggestions = new ArrayList<>(8);
    private AppsSelection mLastSelection = new AppsSelection();
    private long mTotalSize;
    private RunningAppsAdapter mRunningAppsAdapter;

    private BoostPlusPresenter.UserChoices mUserChoices;

    private BannerBackground mBannerBg;
    private View mListBanner;
    private ProgressFrameLayout mProgressBanner;
    private TextView mTotalSizeText;
    private TextView mTotalSizeUnitText;
    private TextView mSelectedSizeText;
    private TextView mRunningAppsCountText;
    private LauncherCheckBox mSelectAllCheckBox;
    private RecyclerView mRunningAppsView;
    @Thunk Button mBoostActionBtn;

    // Action button translate animation
    private ViewPropertyAnimator mActionBtnAnim;
    private boolean mIsActionBtnAnimating;
    private int mTotalTranslation;
    private int mCurrentTargetTranslation;

    private Handler mHandler = new Handler();
    private AnimationThrottler mAnimationThrottler = new AnimationThrottler();
    private long mColorChangeStartTime;
    private boolean mScanning;
    private static boolean sDestroyed;
    public static boolean mIsAccessibilityOpenSuccess;
    public static boolean mIsAccessibilitySettingsOpened;
    private boolean mIsCleanFinishedNeedReScan;
    public static boolean mScanFinished;
    private boolean mIsAccessibilityGranted;

    //region Activity Lifecycle

    @SuppressWarnings("unchecked")
    @Override
    @DebugLog
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boost_plus);
        sDestroyed = false;
        mIsAccessibilityOpenSuccess = false;
        mIsAccessibilitySettingsOpened = false;
        mScanFinished = false;
        mIsAccessibilityGranted = PermissionUtils.isAccessibilityGranted();

        mBannerBg = new BannerBackground(this, R.id.boost_plus_banner_background_container);
        ViewGroup container = ViewUtils.findViewById(this, R.id.container_view);
        mListBanner = ViewUtils.findViewById(container, R.id.app_list_banner);
        mProgressBanner = ViewUtils.findViewById(container, R.id.scan_progress_banner);
        mTotalSizeText = ViewUtils.findViewById(container, R.id.total_size_text);
        mTotalSizeUnitText = ViewUtils.findViewById(container, R.id.total_size_unit_text);
        mSelectedSizeText = ViewUtils.findViewById(container, R.id.selected_size_text);
        mRunningAppsCountText = ViewUtils.findViewById(container, R.id.running_apps_count);
        mSelectAllCheckBox = ViewUtils.findViewById(container, R.id.running_apps_select_all_check_box);
        mRunningAppsView = ViewUtils.findViewById(container, R.id.running_apps_view);
        mBoostActionBtn = ViewUtils.findViewById(container, R.id.boost_action_btn);

        mPresenter = new BoostPlusPresenter(this);

        mTotalTranslation = getResources().getDimensionPixelOffset(R.dimen.boost_plus_action_btn_anim_translation);

        mScanResultFilter = new ScanResultFilter();
        mThirdPartyAllowList = (List<String>) HSConfig.getList("Application", "BoostPlus", "ThirdPartyAppsAllowList");

        RunningAppsAdapter appsAdapter = new RunningAppsAdapter();
        mRunningAppsView.setLayoutManager(new SafeLinearLayoutManager(this));
        mRunningAppsView.setItemAnimator(new RecyclerViewAnimator());
        mRunningAppsView.setAdapter(appsAdapter);
        mRunningAppsAdapter = appsAdapter;

        Intent intent = getIntent();
        boolean isCancelClean = (null != intent) && intent.getBooleanExtra(BUNDLE_KEY_CANCEL_CLEAN, false);
        HSLog.d(TAG, "onCreate *** isCancelClean = " + isCancelClean);

        if (isCancelClean) {
            Bundle cancelCleanResultAppsData = intent.getBundleExtra(INTENT_KEY_LAST_SELECTION_CANCELLED);
            if (cancelCleanResultAppsData != null) {
                ArrayList<HSAppMemory> cancelCleaResultApps = cancelCleanResultAppsData.getParcelableArrayList(BUNDLE_KEY_CANCEL_CLEAN_RESULT_APPS);
                ArrayList<HSAppMemory> cancelCleanSelectedApps = cancelCleanResultAppsData.getParcelableArrayList(BUNDLE_KEY_CANCEL_CLEAN_SELECTED_APPS);
                mRunningApps.clear();
                mRunningApps.addAll(cancelCleaResultApps);
                mSelectedApps.clear();
                mSelectedApps.addAll(cancelCleanSelectedApps);
                onCancelCleanRefresh();
            }
        } else {
            startScan(false);
        }

        mSelectAllCheckBox.setOnCheckedChangeListener(this);
        mBoostActionBtn.setOnClickListener(this);

        BoostTipUtils.setLastOpenBoostPlusTime();

        HSGlobalNotificationCenter.addObserver(NOTIFICATION_RETURN_FROM_CLEAN, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        HSLog.d(TAG, "onResume mIsAccessibilitySettingsOpened = " + mIsAccessibilitySettingsOpened
                + " mIsAccessibilityOpenSuccess = " + mIsAccessibilityOpenSuccess + " mIsCleanFinishedNeedReScan = " + mIsCleanFinishedNeedReScan);
        if (mIsAccessibilitySettingsOpened && !mIsAccessibilityOpenSuccess) {
            if (mIsHomeKeyClicked) {
                mIsCleanFinishedNeedReScan = true;
            } else {
                showCleanAnimationDialog(BoostPlusCleanDialog.CLEAN_TYPE_NORMAL);
            }
            mIsAccessibilitySettingsOpened = false;
        }

        HSLog.d(TAG, "onResume mIsHomeKeyClicked = " + mIsHomeKeyClicked + " mIsCleanFinishedNeedReScan = " + mIsCleanFinishedNeedReScan);
        if (mIsHomeKeyClicked && mIsCleanFinishedNeedReScan) {
            startScan(true);
            mIsCleanFinishedNeedReScan = false;
        }
        mIsHomeKeyClicked = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        HSLog.d(TAG, "onNewIntent ***");
        sDestroyed = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        HSLog.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        if (mIsCleanFinishedNeedReScan) {
            HSLog.d(TAG, "onSaveInstanceState saved last selection: " + mLastSelection);
            outState.putParcelable(BUNDLE_KEY_LAST_SELECTION_CLEANED, mLastSelection);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        HSLog.d(TAG, "onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
        AppsSelection lastSelection = savedInstanceState.getParcelable(BUNDLE_KEY_LAST_SELECTION_CLEANED);
        if (lastSelection != null) {
            HSLog.d(TAG, "onRestoreInstanceState restored last selection: " + lastSelection);
            mLastSelection = lastSelection;
            mScanResultFilter.setLastSelection(mLastSelection);
            mScanResultFilter.setLastCleanTime(SystemClock.elapsedRealtime());

            startScan(true);
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return mPresenter.createOptionsMenu(menu);
    }

    @Override
    public void inflateOptionsMenu(@MenuRes int menuRes, Menu menu) {
        getMenuInflater().inflate(menuRes, menu);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        Utils.setupTransparentSystemBarsForLmp(this);
        ActivityUtils.setNavigationBarColor(this, Color.BLACK);
        ActivityUtils.configSimpleAppBar(this, getString(R.string.launcher_widget_boost_plus_title), Color.TRANSPARENT, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HSLog.d(TAG, "onDestroy ***");
        cancelScan();
        HSGlobalNotificationCenter.removeObserver(this);
        mAnimationThrottler.removeCallbacksAndMessages(null);
        sDestroyed = true;
        mIsAccessibilityOpenSuccess = false;
        mIsAccessibilitySettingsOpened = false;
    }

    //endregion

    //region Callbacks

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if (NOTIFICATION_RETURN_FROM_CLEAN.equals(s)) {
            onReturnFromClean();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_bar_refresh) {
            mPresenter.startSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v == mBoostActionBtn) {
            onClickBoostButton();
            mPresenter.commitUserChoices(mSelectedApps, mSelectSuggestions);
        } else if (v.getId() == R.id.boost_plus_running_app_item) {
            CheckBox checkBox = ViewUtils.findViewById(v, R.id.boost_plus_item_check_box);
            boolean isChecked = !checkBox.isChecked();
            checkBox.setChecked(isChecked);
        } else if (v.getId() == R.id.boost_plus_item_warning_sign) {
            ToastUtils.showToast(R.string.boost_plus_warning_toast, Toast.LENGTH_LONG);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == R.id.boost_plus_running_app_item) {
            Object tag = ViewUtils.findViewById(v, R.id.boost_plus_item_check_box).getTag();
            if (!(tag instanceof Integer)) {
                return false;
            }
            HSAppMemory app = mRunningApps.get((Integer) tag);
            ToastUtils.showToast(app.getPackageName());
        }
        return false;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (mScanning) {
            return;
        }

        if (buttonView == mSelectAllCheckBox) {
            // "Select all" check box
            mSelectedApps.clear();
            if (isChecked) {
                mSelectedApps.addAll(mRunningApps);
            }

            try {
                mRunningAppsAdapter.notifyDataSetChanged();
            } catch (IllegalStateException ignored) {
            }
        } else {
            // Single item check box
            Object tag = buttonView.getTag();
            if (!(tag instanceof Integer)) {
                return;
            }
            int position = (int) tag;
            HSAppMemory app = mRunningApps.get(position);
            mSelectedApps.remove(app);
            if (isChecked) {
                mSelectedApps.add(app);
            }

            if (mSelectedApps.size() == mRunningApps.size()) {
                // Auto-select "select all" check box when all items are selected
                mSelectAllCheckBox.setChecked(true, false);
            } else if (mSelectedApps.isEmpty()) {
                // Auto deselect "select all" check box when all items are deselected
                mSelectAllCheckBox.setChecked(false, false);
            }
            try {
                mRunningAppsAdapter.notifyItemChanged(position);
            } catch (IllegalStateException ignored) {
            }
        }
        refreshSelectedSize(true);
    }

    @Override
    public void onBackPressed() {
        boolean isPermissionGuideVisible = LauncherFloatWindowManager.getInstance().isPermissionGuideShowing();
        boolean isFloatButtonVisible = LauncherFloatWindowManager.getInstance().isFloatButtonShowing();
        boolean isBoostCleanVisible = FloatWindowManager.getInstance().isDialogShowing(FloatWindowManager.Type.BOOST_PLUS_CLEAN);
        boolean isCleaning = HSAccTaskManager.getInstance().isRunning();

        HSLog.d(BoostPlusCleanDialog.TAG, "BoostPlusActivity onBackPressed isPermissionGuideVisible = " + isPermissionGuideVisible + " isFloatButtonVisible = " + isFloatButtonVisible
            + " isBoostCleanVisible = " + isBoostCleanVisible + " isCleaning = " + isCleaning);

        if (isBoostCleanVisible) {
            FloatWindowDialog floatWindowDialog  = FloatWindowManager.getInstance().getDialog(FloatWindowManager.Type.BOOST_PLUS_CLEAN);
            boolean isCleanResultViewShow = true;
            if (null != floatWindowDialog && floatWindowDialog instanceof BoostPlusCleanDialog){
                isCleanResultViewShow = ((BoostPlusCleanDialog) floatWindowDialog).isCleanResultViewShow();
            }

            HSLog.d(BoostPlusCleanDialog.TAG, "BoostPlusActivity onBackPressed isCleanResultViewShow = " + isCleanResultViewShow + " isCleaning = " + isCleaning);
            if (!isCleaning && isCleanResultViewShow) {
                HSAnalytics.logEvent("BoostPlus_ResultPage_Back", "Type", "Back");
                dismissBoostPlusCleanDialog();
            }

            if (isPermissionGuideVisible) {
                LauncherFloatWindowManager.getInstance().removePermissionGuide(false);
            }
            if (isFloatButtonVisible) {
                LauncherFloatWindowManager.getInstance().removeFloatButton();
            }
        } else {
            boolean isRemoveDialog = false;
            if (isPermissionGuideVisible) {
                LauncherFloatWindowManager.getInstance().removePermissionGuide(false);
                isRemoveDialog = true;
            }
            if (isFloatButtonVisible) {
                LauncherFloatWindowManager.getInstance().removeFloatButton();
                isRemoveDialog = true;
            }
            if (!isRemoveDialog) {
                super.onBackPressed();
            }
        }
    }

    //endregion

    //region Implementations of BoostPlusContracts.HomePage

    @Override
    public ArrayList<HSAppMemory> getAppsToClean() {
        return mSelectedApps;
    }

    @Override
    public void onCleanFinished() {
        HSLog.d(TAG, "onCleanFinished sDestroyed = " + sDestroyed);
        mIsCleanFinishedNeedReScan = true;
    }

    public void onReturnFromClean() {
        HSLog.d(TAG, "onReturnFromClean");

        // Exclude cleaned apps in next scan result
        mLastSelection.clear();
        mLastSelection.selectedApps.addAll(mSelectedApps);
        mLastSelection.unselectedApps.addAll(getLastUnselectedApps());
        mScanResultFilter.setLastSelection(mLastSelection);
        mScanResultFilter.setLastCleanTime(SystemClock.elapsedRealtime());
        mScanFinished = true;

        startScan(true);
        mIsCleanFinishedNeedReScan = false;
    }

    @Override
    public void onReturnFromCleanCancelled(List<HSAppMemory> cleanRemainingApps) {
        HSLog.d(TAG, "onReturnFromCleanCancelled sDestroyed = " + sDestroyed + " size = "
                + String.valueOf((null == cleanRemainingApps) ? 0 : cleanRemainingApps.size()));
        dismissBoostPlusCleanDialog();

        if (cleanRemainingApps == null || cleanRemainingApps.size() == 0) {
            mRunningApps.clear();
            mSelectedApps.clear();
            mSelectSuggestions.clear();
            setupViews(true);
        } else {
            ArrayList<HSAppMemory> unselectedApps = getLastUnselectedApps();
            mRunningApps.clear();
            mRunningApps.addAll(cleanRemainingApps);
            mRunningApps.addAll(unselectedApps);
            mSelectedApps.clear();
            mSelectedApps.addAll(cleanRemainingApps);
            mSelectSuggestions.clear();
            mSelectSuggestions.addAll(cleanRemainingApps);

            if (sDestroyed) {
                Intent intent = new Intent(this, BoostPlusActivity.class);
                Bundle data = new Bundle();
                data.putBoolean(BUNDLE_KEY_CANCEL_CLEAN, true);
                data.putParcelableArrayList(BUNDLE_KEY_CANCEL_CLEAN_RESULT_APPS, mRunningApps);
                data.putParcelableArrayList(BUNDLE_KEY_CANCEL_CLEAN_SELECTED_APPS, mSelectedApps);
                intent.putExtra(INTENT_KEY_LAST_SELECTION_CANCELLED, data);
                startActivity(intent);
            } else {
                cancelScan();
                onCancelCleanRefresh();
            }
        }
    }

    private void onCancelCleanRefresh() {
        // Refresh recycler view
        refreshRunningAppsView();

        // refresh total size and banner color, running apps number
        mTotalSize = 0;
        for (HSAppMemory hSAppMemory : mRunningApps) {
            if (null != hSAppMemory) {
                mTotalSize += hSAppMemory.getSize();
            }
        }
        refreshTextAndBannerColor(false);
    }

    public void dismissBoostPlusCleanDialog() {
        FloatWindowManager.isRemoveDialogFrozen = false;
        FloatWindowManager.getInstance().removeDialog(FloatWindowManager.Type.BOOST_PLUS_CLEAN);
    }

    private ArrayList<HSAppMemory> getLastUnselectedApps() {
        ArrayList<HSAppMemory> unselectedApps = new ArrayList<>(mRunningApps);
        unselectedApps.removeAll(mSelectedApps);
        return unselectedApps;
    }

    //endregion

    private void startScan(boolean scanAfterClean) {
        if (mScanning) {
            HSLog.w(TAG, "Already scanning, skip new scan");
            return;
        }
        mScanAfterClean = scanAfterClean;
        mUserChoices = mPresenter.loadUserChoices();

        mScanning = true;
        mScanListener = new ScanListener();

        mScanResultFilter.initSelectedAppsMap();
        //noinspection SuspiciousMethodCalls
        HSAppMemoryManager.getInstance().startScanWithCompletedProgress(mScanListener);

        mRunningApps.clear();
        mSelectedApps.clear();
        mSelectSuggestions.clear();
        mTotalSize = 0;

        mSelectAllCheckBox.setChecked(false);
        refreshTextAndBannerColor(false);
        refreshRunningAppsView();

        mListBanner.setVisibility(View.INVISIBLE);
        mProgressBanner.reset();
        final View wheel = findViewById(R.id.progressWheel);
        assert wheel != null;
        wheel.setAlpha(0f);
        wheel.setVisibility(View.VISIBLE);
        wheel.animate().alpha(1f)
                .setDuration(ANIMATION_OPT_TIME)
                .start();

        mColorChangeStartTime = SystemClock.uptimeMillis() + ANIMATION_OPT_TIME;
        HSLog.d(TAG + ".Banner", "Start scan, color change suppressed until " + ANIMATION_OPT_TIME + " ms later");
    }

    private void cancelScan() {
        mScanning = false;
        HSAppMemoryManager.getInstance().stopScan(mScanListener);
        mScanListener = null;
    }

    @Thunk
    void bindScannedApp(HSAppMemory app) {
        Message.obtain(mAnimationThrottler, AnimationThrottler.MESSAGE_PRODUCE_INSERT_APP, app)
                .sendToTarget();
    }

    @Thunk
    void bindScannedAppThrottled(HSAppMemory app) {
        mRunningApps.add(0, app);
        mTotalSize += app.getSize();

        if (shouldCheck(app.getPackageName())) {
            mSelectedApps.add(app);
            mSelectSuggestions.add(app);
        }

        refreshTextAndBannerColor(true);
        try {
            mRunningAppsAdapter.notifyItemInserted(0);
        } catch (IllegalStateException ignored) {
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mRunningAppsView.smoothScrollToPosition(0);
            }
        });
    }

    private boolean shouldCheck(String packageName) {
        boolean checkedByUser = mUserChoices.additions.contains(packageName);
        boolean uncheckedByUser = mUserChoices.removals.contains(packageName);
        boolean inThirdPartyAllowList = mThirdPartyAllowList.contains(packageName);

        boolean shouldCheck = checkedByUser || (!inThirdPartyAllowList && !uncheckedByUser);
        HSLog.d(TAG + ".Check", (shouldCheck ? "[✓] " : "[ ] ") + "checkedByUser: " + checkedByUser
                + ", uncheckedByUser: " + uncheckedByUser + ", inThirdPartyAllowList: " + inThirdPartyAllowList);
        return shouldCheck;
    }

    @Thunk
    void finishScan() {
        HSLog.d(TAG, "finishScan ***");
        Message.obtain(mAnimationThrottler, AnimationThrottler.MESSAGE_PRODUCE_DONE)
                .sendToTarget();
    }

    @Thunk
    void finishScanThrottled() {
        HSLog.d(TAG, "finishScanThrottled ***");
        boolean noRunningApps = mRunningApps.isEmpty();

        // Hide app list and selection banner, show an empty view when no app is running
        setupViews(noRunningApps);

        if (mSelectedApps.size() == mRunningApps.size()) {
            // Auto-select "select all" check box when all items are selected
            mSelectAllCheckBox.setChecked(true, false);
        }

        mProgressBanner.finish(new Runnable() {
            @Override
            public void run() {
                final View wheel = findViewById(R.id.progressWheel);
                assert wheel != null;
                wheel.animate().alpha(0f)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                wheel.setVisibility(View.INVISIBLE);
                            }
                        })
                        .start();
            }
        });
        mListBanner.setAlpha(0f);
        mListBanner.setVisibility(View.VISIBLE);
        mListBanner.animate()
                .alpha(1f)
                .start();

        if (!noRunningApps) {
            Collections.sort(mRunningApps, new Comparator<HSAppMemory>() {
                @Override
                public int compare(HSAppMemory o1, HSAppMemory o2) {
                    int selected1 = mSelectedApps.contains(o1) ? 1 : 0;
                    int selected2 = mSelectedApps.contains(o2) ? 1 : 0;
                    if (selected1 != selected2) {
                        return selected2 - selected1;
                    }
                    return (int) (o2.getSize() - o1.getSize());
                }
            });
            refreshRunningAppsView();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                }
            });
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    refreshTextAndBannerColor(true);
                    showAccessibilityDialog();
                }
            }, 500);
        }

        if (!mScanAfterClean) {
            logScanResult();
        }
    }

    private void logScanResult() {
        HashMap<String, String> data = new HashMap<>(2);

        int sizeMb = (int) (mTotalSize / (1024 * 1024));
        String sizeString;
        if (sizeMb == 0) {
            sizeString = "0";
        } else if (sizeMb < 30) {
            sizeString = "0-30";
        } else if (sizeMb < 100) {
            sizeString = "30-100";
        } else if (sizeMb < 200) {
            sizeString = "100-200";
        } else if (sizeMb < 300) {
            sizeString = "200-300";
        } else if (sizeMb < 400) {
            sizeString = "300-400";
        } else if (sizeMb < 500) {
            sizeString = "400-500";
        } else if (sizeMb < 600) {
            sizeString = "500-600";
        } else if (sizeMb < 700) {
            sizeString = "600-700";
        } else if (sizeMb < 800) {
            sizeString = "700-800";
        } else if (sizeMb < 900) {
            sizeString = "800-900";
        } else if (sizeMb < 1000) {
            sizeString = "900-1000";
        } else {
            sizeString = "More Than 1000";
        }
        data.put("Memory", sizeString);

        int runningAppCount = mRunningApps.size();
        String countString;
        if (runningAppCount <= 10) {
            countString = String.valueOf(runningAppCount);
        } else {
            countString = "Other";
        }
        data.put("Number", countString);

        HSAnalytics.logEvent("BoostPlus_Homepage_ScanResult", data);
    }

    @SuppressLint("HandlerLeak")
    private class AnimationThrottler extends Handler {
        private static final long THROTTLE_INTERVAL_MILLIS = 200;

        static final int MESSAGE_PRODUCE_INSERT_APP = 0;
        static final int MESSAGE_PRODUCE_DONE = 1;
        static final int MESSAGE_CONSUME = 2;

        private Queue<Object> mTasks = new ArrayDeque<>();
        private long mLastConsumeTime;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_PRODUCE_INSERT_APP:
                    mTasks.offer(msg.obj);
                    scheduleConsumeNext();
                    break;
                case MESSAGE_PRODUCE_DONE:
                    mTasks.offer(new Object());
                    scheduleConsumeNext();
                    break;

                case MESSAGE_CONSUME:
                    Object task = mTasks.poll();
                    if (task instanceof HSAppMemory) {
                        mLastConsumeTime = SystemClock.elapsedRealtime();
                        bindScannedAppThrottled((HSAppMemory) task);
                        sendEmptyMessageDelayed(MESSAGE_CONSUME, THROTTLE_INTERVAL_MILLIS);
                    } else if (task != null) {
                        finishScanThrottled();
                    }
                    break;
            }
        }

        private void scheduleConsumeNext() {
            if (!hasMessages(MESSAGE_CONSUME)) {
                long timeSinceLastConsumption = SystemClock.elapsedRealtime() - mLastConsumeTime;
                if (timeSinceLastConsumption >= THROTTLE_INTERVAL_MILLIS) {
                    sendEmptyMessage(MESSAGE_CONSUME);
                } else {
                    sendEmptyMessageDelayed(MESSAGE_CONSUME, THROTTLE_INTERVAL_MILLIS - timeSinceLastConsumption);
                }
            }
        }
    }

    private void setupViews(boolean noRunningApps) {
        View container = findViewById(R.id.linear_fixed_container);
        assert container != null;
        View divider = container.findViewById(R.id.selection_banner_divider);

        View emptyView = findViewById(R.id.empty_view);
        if (noRunningApps) {
            mProgressBanner.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
            mRunningAppsView.setVisibility(View.GONE);

            if (emptyView == null) {
                ViewStub emptyViewStub = (ViewStub) findViewById(R.id.empty_view_stub);
                assert emptyViewStub != null;
                emptyView = emptyViewStub.inflate();
            }
            emptyView.setVisibility(View.VISIBLE);
        } else {
            mProgressBanner.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
            mRunningAppsView.setVisibility(View.VISIBLE);

            if (emptyView != null) {
                emptyView.setVisibility(View.GONE);
            }
        }
    }

    private void refreshTextAndBannerColor(boolean animated) {
        FormatSizeBuilder sizeBuilder = new FormatSizeBuilder(mTotalSize);
        mTotalSizeText.setText(sizeBuilder.size);
        mTotalSizeUnitText.setText(sizeBuilder.unit);
        mRunningAppsCountText.setText(String.valueOf(mRunningApps.size()));

        refreshBannerColor(mTotalSize, animated);
        refreshSelectedSize(animated);
    }

    private void refreshBannerColor(final long totalSizeBytes, final boolean animated) {
        long now = SystemClock.uptimeMillis();
        if (now < mColorChangeStartTime) {
            HSLog.d(TAG + ".Banner", "Post refresh banner color to " + (mColorChangeStartTime - now) + " ms in the future");
            mHandler.postAtTime(new Runnable() {
                @Override
                public void run() {
                    mPresenter.refreshBannerColor(totalSizeBytes, animated);
                }
            }, mColorChangeStartTime);
            mColorChangeStartTime += 100L; // Add 100 ms to this time to keep calls in order
        } else {
            mPresenter.refreshBannerColor(totalSizeBytes, animated);
        }
    }

    @Override
    public void setBannerColor(@ColorRes int resId, boolean animated) {
        mBannerBg.setBannerColor(resId, animated);
    }

    private void refreshSelectedSize(boolean animated) {
        long selectedSize = 0;
        for (HSAppMemory app : mSelectedApps) {
            selectedSize += app.getSize();
        }
        final FormatSizeBuilder sizeBuilder = new FormatSizeBuilder(selectedSize);
        String fullText = getString(R.string.boost_plus_selected_size_text, sizeBuilder.sizeUnit);
        mSelectedSizeText.setText(StringUtils.getTextWithBoldSpan(fullText, sizeBuilder.size));
        Runnable changeButtonTextRunnable = new Runnable() {
            @Override
            public void run() {
                mBoostActionBtn.setText(getString(R.string.boost_plus_action_button_text, sizeBuilder.sizeUnit));
            }
        };
        if (selectedSize > 0 && !mScanning) {
            changeButtonTextRunnable.run();
            setActionButtonTranslation(0, animated, null); // Appear
        } else {
            setActionButtonTranslation(mTotalTranslation, animated, changeButtonTextRunnable); // Disappear
        }
    }

    private void setActionButtonTranslation(final int toTranslation, boolean animated, final Runnable endAction) {
        if (mIsActionBtnAnimating) {
            if (toTranslation == mCurrentTargetTranslation) {
                return;
            } else {
                mActionBtnAnim.cancel();
            }
        }
        float current = mBoostActionBtn.getTranslationY();
        float actualTranslationAbs = Math.abs(toTranslation - current);
        if (actualTranslationAbs < 1f) {
            return;
        }
        boolean downward = (toTranslation > current);
        if (animated) {
            mActionBtnAnim = mBoostActionBtn.animate()
                    .translationY(toTranslation)
                    .setDuration((long) (LauncherAnimUtils.getShortAnimDuration() * (downward ? 1 : 6)
                            * (actualTranslationAbs / mTotalTranslation)))
                    .setInterpolator(downward ? LauncherAnimUtils.ACCELERATE_QUAD : new SpringInterpolator(0.3f))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            mIsActionBtnAnimating = true;
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mIsActionBtnAnimating = false;
                            if (!mCancelled && endAction != null) {
                                endAction.run();
                            }
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            super.onAnimationCancel(animation);
                            mIsActionBtnAnimating = false;
                        }
                    });
            mCurrentTargetTranslation = toTranslation;
            mActionBtnAnim.start();
        } else {
            mCurrentTargetTranslation = toTranslation;
            mBoostActionBtn.setTranslationY(toTranslation);
            if (endAction != null) {
                endAction.run();
            }
        }
    }

    private void refreshRunningAppsView() {
        try {
            mRunningAppsAdapter.notifyDataSetChanged();
        } catch (IllegalStateException ignored) {
        }
    }

    private void onClickBoostButton() {
        if (mSelectedApps.isEmpty()) {
            ToastUtils.showToast("No app selected");
            return;
        }
        long sizeToClean = 0L;
        Map<String, String> selectedAppsMap = new HashMap<>();
        for (HSAppMemory selectedApp : mSelectedApps) {
            sizeToClean += selectedApp.getSize();
            selectedAppsMap.put(selectedApp.getPackageName(), String.valueOf(System.currentTimeMillis()));
        }
        BoostPlusUtils.setCleanedAppsMap(selectedAppsMap);
        mPresenter.startBoost(sizeToClean);
    }

    @Override
    public void showAuthorizeDialog(long sizeToClean) {
        if (BoostPlusUtils.hasTurnOnAccessibilityDialogShowed()) {
            showCleanAnimationDialog(BoostPlusCleanDialog.CLEAN_TYPE_NORMAL);
            return;
        }
        BoostPlusUtils.setTurnOnAccessibilityDialogShowed();
        LauncherTipManager.getInstance().showTip(this, LauncherTipManager.TipType.BOOST_PLUS_AUTHORIZE, sizeToClean);
    }

    @Override
    public void showCleanAnimationDialog(int type) {
        BoostPlusCleanDialog.showBoostPlusCleanDialog(BoostPlusActivity.this, type);
    }

    private class RunningAppsAdapter extends RecyclerView.Adapter<RunningAppHolder> {
        private static final long EXCLAMATION_MARK_SHOW_MEMORY_THRESHOLD = 100 * 1024 * 1024;

        @Override
        public int getItemCount() {
            return mRunningApps.size();
        }

        @Override
        public RunningAppHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View item = getLayoutInflater().inflate(R.layout.boost_plus_running_app_item, parent, false);
            RunningAppHolder holder = new RunningAppHolder(item);
            holder.checkBox.setOnCheckedChangeListener(BoostPlusActivity.this);
            return holder;
        }

        @Override
        public void onBindViewHolder(RunningAppHolder holder, int position) {
            HSAppMemory app = mRunningApps.get(position);
            LauncherPackageManager pm = LauncherPackageManager.getInstance();
            long size = app.getSize();

            boolean isChecked = mSelectedApps.contains(app);
            boolean boldSizeText = (size > EXCLAMATION_MARK_SHOW_MEMORY_THRESHOLD && !isChecked);
            boolean showWarningSign = (mThirdPartyAllowList.contains(app.getPackageName()) && isChecked);
            long launchTime = app.getMainProcessLaunchTime();

            holder.icon.setImageDrawable(pm.getApplicationIcon(app.getPackageName()));
            holder.title.setText(app.getAppName());
            FormatSizeBuilder sizeBuilder = new FormatSizeBuilder(size);
            CharSequence sizeText = boldSizeText ?
                    StringUtils.getTextWithBoldSpan(sizeBuilder.sizeUnit, sizeBuilder.sizeUnit) : sizeBuilder.sizeUnit;
            holder.memory.setText(sizeText);
            if (mIsAccessibilityGranted && launchTime > 0) {
                String timeString = getString(R.string.boost_plus_running_time,
                        getDurationString(launchTime, SystemClock.elapsedRealtime()));
                holder.runningTime.setText(timeString);
                holder.runningTime.setVisibility(View.VISIBLE);
            } else {
                holder.runningTime.setVisibility(View.GONE);
            }
            holder.warningSign.setVisibility(showWarningSign ? View.VISIBLE : View.INVISIBLE);
            holder.checkBox.setTag(position);
            holder.checkBox.setChecked(isChecked, false);

            holder.itemView.setOnClickListener(BoostPlusActivity.this);
            if (BuildConfig.DEBUG) {
                holder.itemView.setOnLongClickListener(BoostPlusActivity.this);
            }
            holder.warningSign.setOnClickListener(BoostPlusActivity.this);
        }

        /**
         * Get duration string like "8 h 23 min".
         *
         * For duration < 120s, returns "1 min".
         * For duration [120s, 180s), returns "2 min".
         * ...
         */
        private String getDurationString(long start, long end) {
            long durationMillis = end - start;
            int minutes = (int) (durationMillis / (60 * 1000));
            if (minutes < 1) {
                minutes = 1;
            }
            StringBuilder durationString = new StringBuilder();
            if (minutes >= 60) {
                durationString.append(minutes / 60).append(" h ");
            }
            durationString.append(minutes % 60).append(" min");
            return durationString.toString();
        }
    }

    private static class RunningAppHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;
        TextView runningTime;
        TextView memory;
        View warningSign;
        LauncherCheckBox checkBox;

        RunningAppHolder(View itemView) {
            super(itemView);
            icon = ViewUtils.findViewById(itemView, R.id.boost_plus_item_icon);
            title = ViewUtils.findViewById(itemView, R.id.boost_plus_item_title);
            runningTime = ViewUtils.findViewById(itemView, R.id.boost_plus_item_running_time);
            memory = ViewUtils.findViewById(itemView, R.id.boost_plus_item_memory_size);
            warningSign = ViewUtils.findViewById(itemView, R.id.boost_plus_item_warning_sign);
            checkBox = ViewUtils.findViewById(itemView, R.id.boost_plus_item_check_box);
        }
    }

    private class ScanListener implements HSAppMemoryManager.MemoryTaskListener {
        private int mInvalidCount;

        @Override
        public void onStarted() {
            HSLog.d(TAG, "Start scan");
        }

        @Override
        public void onProgressUpdated(int processedCount, int total, HSAppMemory hsAppMemory) {
            HSLog.d(TAG, "Scan progress " + processedCount + " / " + total + ", " + hsAppMemory.getPackageName() + " takes "
                    + hsAppMemory.getSize() + " bytes");
            if (!mScanResultFilter.filter(BoostPlusActivity.this, hsAppMemory)) {
                HSLog.w(TAG, "Invalid running app scan result, skip");
                mInvalidCount++;
            } else if (!DEBUG_NO_RUNNING_APPS) {
                bindScannedApp(hsAppMemory);
            }
        }

        @Override
        public void onSucceeded(List<HSAppMemory> list, long l) {
            HSLog.d(TAG, "Scan succeeded, " + (list.size() - mInvalidCount) + " apps takes " + l + " bytes");
            finishScan();
        }

        @Override
        public void onFailed(int i, String s) {
            HSLog.w(TAG, "Clean failed: " + i + ", error: " + s);
            finishScan();
        }
    }

    static class AppsSelection implements Parcelable {
        ArrayList<HSAppMemory> selectedApps = new ArrayList<>();
        ArrayList<HSAppMemory> unselectedApps = new ArrayList<>();

        AppsSelection() {
        }

        AppsSelection(Parcel in) {
            selectedApps = in.createTypedArrayList(HSAppMemory.CREATOR);
            unselectedApps = in.createTypedArrayList(HSAppMemory.CREATOR);
        }

        public static final Creator<AppsSelection> CREATOR = new Creator<AppsSelection>() {
            @Override
            public AppsSelection createFromParcel(Parcel in) {
                return new AppsSelection(in);
            }

            @Override
            public AppsSelection[] newArray(int size) {
                return new AppsSelection[size];
            }
        };

        void clear() {
            selectedApps.clear();
            unselectedApps.clear();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeTypedList(selectedApps);
            dest.writeTypedList(unselectedApps);
        }

        @Override
        public String toString() {
            return "selectedApps: " + selectedApps + ", unselectedApps: " + unselectedApps;
        }
    }

    @Override
    protected boolean registerCloseSystemDialogsReceiver() {
        return true;
    }

    private void showAccessibilityDialog() {
        if (mScanAfterClean) {
            return;
        }
        int runningAppSize = mRunningApps.size();
        HSLog.d(TAG, "showAccessibilityDialog *** runningAppSize = " + runningAppSize);
        if (BoostPlusUtils.shouldShowAccessibilityNoticeDialog(runningAppSize)) {
            BoostPlusAccessibilityDialog.Data data = new BoostPlusAccessibilityDialog.Data();
            data.selectNumber = runningAppSize;
            LauncherTipManager.getInstance().showTip(this, LauncherTipManager.TipType.BOOST_PLUS_ACCESSIBILITY_TIP, data);
            BoostPlusUtils.setAccessibilityNoticeDialogShowed();
            HSAnalytics.logEvent("BoostPlus_DetectedAlert_Show");
        }
    }

}
