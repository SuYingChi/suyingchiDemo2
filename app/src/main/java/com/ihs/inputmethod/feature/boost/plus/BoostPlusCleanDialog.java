package com.ihs.inputmethod.feature.boost.plus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.honeycomb.launcher.BuildConfig;
import com.honeycomb.launcher.R;
import com.honeycomb.launcher.animation.LauncherAnimUtils;
import com.honeycomb.launcher.animation.LauncherAnimationUtils;
import com.honeycomb.launcher.boost.animation.BoostAnimationManager;
import com.honeycomb.launcher.boost.animation.DynamicRotateAnimation;
import com.honeycomb.launcher.customize.view.ProgressWheel;
import com.honeycomb.launcher.dialog.FloatWindowManager;
import com.honeycomb.launcher.dialog.FullScreenDialog;
import com.honeycomb.launcher.dialog.LauncherFloatWindowManager;
import com.honeycomb.launcher.dialog.SafeWindowManager;
import com.honeycomb.launcher.model.LauncherFiles;
import com.honeycomb.launcher.resultpage.ResultPageActivity;
import com.honeycomb.launcher.util.CommonUtils;
import com.honeycomb.launcher.util.NotificationCenter;
import com.honeycomb.launcher.util.PreferenceHelper;
import com.honeycomb.launcher.util.Thunk;
import com.honeycomb.launcher.util.Utils;
import com.honeycomb.launcher.util.VectorCompat;
import com.honeycomb.launcher.util.ViewUtils;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.device.clean.accessibility.HSAccTaskManager;
import com.ihs.device.clean.memory.HSAppMemory;
import com.ihs.device.clean.memory.HSAppMemoryManager;
import com.ihs.permission.HSPermissionRequestCallback;
import com.ihs.permission.HSPermissionRequestMgr;
import com.ihs.permission.HSPermissionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public class BoostPlusCleanDialog extends FullScreenDialog {

    public static final String TAG = BoostPlusCleanDialog.class.getSimpleName();

    private static final String PREF_KEY_BOOST_PLUS_CLEAN_DIALOG_SHOW_COUNT = "boost_plus_clean_dialog_show_count";

    public static final int CLEAN_TYPE_NON_ROOT = 0;
    public static final int CLEAN_TYPE_ROOT = 1;
    public static final int CLEAN_TYPE_NON_ROOT_DIRECTLY = 2;
    public static final int CLEAN_TYPE_NON_ROOT_ACCESSIBILITY_OPEN = 3;
    public static final int CLEAN_TYPE_NORMAL = 4;

    private static final String PREF_KEY_NOTIFICATION_REQUEST_TIME = "pref_key_notification_request_time";
    private static final String PREF_KEY_USAGE_REQUEST_TIME = "pref_key_usage_request_time";

    private static final int DEFAULT_ACC_CLEAN_TIMEOUT_SECONDS = 5;

    public static final long FRAME = 35; // 30 fps
    private static final long FRAME_DOTS = 40;

    // Circle
    private static final long DURATION_CIRCLE_IN_ALPHA_ADD = 28 * FRAME;
    private static final long DURATION_CIRCLE_IN_ALPHA_REDUCE = 25 * FRAME;

    public static final long START_OFF_CIRCLE_ROTATE_MAIN = 0;
    private static final long DURATION_ROTATE_MAIN = 110 * FRAME;

    // Rocket
    private static final long DURATION_ROCKET_ALPHA_ADD = 9 * FRAME;
    private static final long DURATION_ROCKET_ALPHA_NO_CHANGE = 10 * FRAME;
    private static final long DURATION_ROCKET_ALPHA_REDUCE = 7 * FRAME;

    private static final long DURATION_ROCKET_TRANSLATE = 25 * FRAME;
    private static final int POSITION_ROCKET_TRANSLATE = 2000;

    // Smoke
    private static final long DURATION_SMOKE_ALPHA_ADD = 15 * FRAME;
    private static final long DURATION_SMOKE_ALPHA_REDUCE = 9 * FRAME;
    private static final long DURATION_SMOKE_SCALE = 20 * FRAME;

    private static final float SMOKE_FROM_X = 0.8f;
    private static final float SMOKE_TO_X = 0.1f;
    private static final float SMOKE_FROM_Y = 0;
    private static final float SMOKE_TO_Y = 1.5f;

    // Clean Size
    private static final long DURATION_MEMORY_USED_ALPHA_REDUCE = 7 * FRAME;
    private static final long DURATION_CLEAN_ITEM_APP = 10 * FRAME;
    private static final long DURATION_CLEAN_ITEM_APP_END_LAST = 5 * FRAME;

    public static final int DEVICE_SCREEN_HEIGHT_TAG = 1920;

    // Cleaning running apps
    private static final long DURATION_CLEANING_TEXT_START_OFF = START_OFF_CIRCLE_ROTATE_MAIN + 10 * FRAME;

    private static final long TIMEOUT_CLEAN = 2 * 60 * 1000;
    private static final long TIMEOUT_GET_PERMISSION = 60 * 1000;

    // Dots
    private static final int DOTS_COUNT = 80; // one dots 200ms

    // Boosted Circle
    private static final float CIRCLE_ROTATE_FACTOR = 1.5f;

    // Background
    private static final long DURATION_BACKGROUND_SINGLE_CHANGED = 50 * FRAME;
    private static final long DURATION_BACKGROUND_END_CHANGED = 20 * FRAME;

    private static final int END_SIZE_LAST_PERCENT = 3;
    private static final long DURATION_END_SIZE_DECELERATE_FACTOR = 3;
    private static final long START_OFF_EXIT_CLEAN = 1000;
    private static final long TIMEOUT_EXITING_DIALOG = 12000;
    private static final long DELAY_IMG_CURVE_ANIMATION = 500;

    @Thunk RelativeLayout mCleanMainRl;
    @Thunk RelativeLayout mBoostIconContainer;
    private RelativeLayout mExitingRl;
    @Thunk LinearLayout mBoostingTextLl;
    @Thunk View mContainerV;
    private View mStopDialogV;

    @Thunk ImageView mCircleInIV;
    @Thunk ImageView mCircleMiddleIv;
    @Thunk ImageView mCircleOutIv;
    @Thunk ImageView mDotPositionTagIv;
    private ImageView mRocketIv;
    private ImageView mSmokeIv;
    private ImageView mBoostCenterIv;
    private ImageView mIconOneV;
    private ImageView mIconTwoV;
    private ImageView mIconThreeV;
    private ImageView mIconFourV;
    private ImageView mIconFiveV;
    private ImageView mIconSixV;
    private ImageView mIconSevenV;
    @Thunk
    AppCompatImageView mDotTagIv;

    private TextView mTipTitleTv;
    @Thunk TextView mCleaningTv;
    @Thunk TextView mCleaningAppNumberTv;
    @Thunk TextView mTipTv;
    private BoostTextView mMemoryUsedNumberTv;

    private ProgressWheel mExitingProgressWheel;

    @Thunk Handler mHandler = new Handler();
    @Thunk Handler mDotsHandler = new Handler();

    private Runnable mCleanTimeOutRunnable;
    private Runnable mGetPermissionTimeOutRunnable;

    @Thunk boolean mIsResultViewShow;
    private boolean mIsStartGetPermission;
    @Thunk boolean mIsPermissionGetting;
    private boolean mIsStartForceStopCancel;
    private boolean mIsStartRootCancel;
    @Thunk boolean mIsBackDisabled;
    @Thunk boolean mIsRootCleaning;
    private boolean mIsFlurryLogResultShow;

    private int mScreenHeight;
    @Thunk int mDotsAnimationCount = 0;
    private long mCurrentLastCleanSize;
    @Thunk long mStartCircleAnimationTime;

    @Thunk DynamicRotateAnimation mCircleInDynamicRotateAnimation;
    @Thunk DynamicRotateAnimation mCircleMiddleDynamicRotateAnimation;
    @Thunk DynamicRotateAnimation mCircleOutDynamicRotateAnimation;

    private ValueAnimator mBgColorAnimator;

    @Thunk ArrayList<HSAppMemory> mSelectedAppList;
    @Thunk List<String> mCleanedAppPackageNameList = new ArrayList<>();
    @Thunk CleanResult mCleanResult = CleanResult.CLEAN_CANCEL;

    @Thunk BoostPlusContracts.HomePage mHome;

    private enum CleanResult {
        CLEAN_SUCCESS,
        CLEAN_FAILED,
        CLEAN_CANCEL,
        PERMISSION_SUCCESS,
        PERMISSION_FAILED,
        PERMISSION_CANCEL
    }

    public static void showBoostPlusCleanDialog(Context context, int type) {
        if (CLEAN_TYPE_NORMAL == type && !BoostPlusUtils.isNormalCleanToasted()) {
            Toast.makeText(context, context.getString(R.string.boost_plus_normal_toast), Toast.LENGTH_LONG).show();
            BoostPlusUtils.setNormalCleanToasted();
        }
        LauncherFloatWindowManager.getInstance().showDialog(context,
                LauncherFloatWindowManager.Type.BOOST_PLUS_CLEAN, type);
    }

    public BoostPlusCleanDialog(Context context, int type) {
        this(context);
        type = initCleanType(type);

        if (null != context && context instanceof BoostPlusContracts.HomePage) {
            mHome = (BoostPlusContracts.HomePage) context;
        }
        getCleanData();

        mScreenHeight = CommonUtils.getPhoneHeight(context);

        mContainerV = ViewUtils.findViewById(mContentView, R.id.view_container);
        mCleanMainRl = ViewUtils.findViewById(mContentView, R.id.clean_main_rl);
        // init dots animation view tag
        mCleanMainRl.setTag(false);
        // init material content view top margin
        ViewUtils.setMargins(mCleanMainRl, 0, Utils.getStatusBarHeight(getContext()), 0, 0);

        mBoostIconContainer = (RelativeLayout) findViewById(R.id.boost_icon);
        mCircleInIV = ViewUtils.findViewById(mContentView, R.id.circle_in_iv);
        mCircleMiddleIv = ViewUtils.findViewById(mContentView, R.id.circle_middle_iv);
        mCircleOutIv = ViewUtils.findViewById(mContentView, R.id.circle_out_iv);
        mMemoryUsedNumberTv = ViewUtils.findViewById(mContentView, R.id.memory_used_number_tv);
        mBoostingTextLl = ViewUtils.findViewById(mContentView, R.id.boosting_text_ll);

        mRocketIv = ViewUtils.findViewById(mContentView, R.id.rocket_iv);
        mSmokeIv = ViewUtils.findViewById(mContentView, R.id.smoke_iv);
        mBoostCenterIv = ViewUtils.findViewById(mContentView, R.id.boost_center_iv);

        mDotTagIv = ViewUtils.findViewById(mContentView, R.id.dot_anchor_tag_iv);
        mDotPositionTagIv = ViewUtils.findViewById(mContentView, R.id.dot_normal_anchor_iv);

        mCleaningTv = ViewUtils.findViewById(mContentView, R.id.cleaning_running_apps_tv);
        mCleaningAppNumberTv = ViewUtils.findViewById(mContentView, R.id.cleaning_running_apps_number_tv);
        mTipTv = ViewUtils.findViewById(mContentView, R.id.tip_tv);
        mTipTitleTv = ViewUtils.findViewById(mContentView, R.id.tip_title_tv);

        mExitingRl = ViewUtils.findViewById(mContentView, R.id.exiting_rl);
        mExitingProgressWheel = ViewUtils.findViewById(mContentView, R.id.exiting_progress_wheel);

        initTip(context);
        initStopDialog();

        mIconOneV = ViewUtils.findViewById(mContentView, R.id.boost_icon_1_iv);
        mIconTwoV = ViewUtils.findViewById(mContentView, R.id.boost_icon_2_iv);
        mIconThreeV = ViewUtils.findViewById(mContentView, R.id.boost_icon_3_iv);
        mIconFourV = ViewUtils.findViewById(mContentView, R.id.boost_icon_4_iv);
        mIconFiveV = ViewUtils.findViewById(mContentView, R.id.boost_icon_5_iv);
        mIconSixV = ViewUtils.findViewById(mContentView, R.id.boost_icon_6_iv);
        mIconSevenV = ViewUtils.findViewById(mContentView, R.id.boost_icon_7_iv);

        startBoostAnimation();
        startClean(type);
        FloatWindowManager.isRemoveDialogFrozen = true;
    }

    public BoostPlusCleanDialog(Context context) {
        super(context);
    }

    public BoostPlusCleanDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BoostPlusCleanDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private int initCleanType(int type) {
        if (type == CLEAN_TYPE_NON_ROOT_DIRECTLY) {
            type = CLEAN_TYPE_NON_ROOT;
            HSAnalytics.logEvent("BoostPlus_Animation_Start", "Type", "Directly");
        } else if (type == CLEAN_TYPE_NON_ROOT_ACCESSIBILITY_OPEN) {
            type = CLEAN_TYPE_NON_ROOT;
            HSAnalytics.logEvent("BoostPlus_Animation_Start", "Type", "Accessibility Open");
        } else {
            HSAnalytics.logEvent("BoostPlus_Animation_Start", "Type", "Root Open");
        }
        return type;
    }

    @SuppressLint("SetTextI18n")
    private void initTip(Context context) {
        // Text
        String[] tips = context.getResources().getStringArray(R.array.boost_plus_clean_tips);
        if (tips.length == 0) {
            return;
        }
        int showCount = PreferenceHelper.get(LauncherFiles.BOOST_PREFS)
                .incrementAndGetInt(PREF_KEY_BOOST_PLUS_CLEAN_DIALOG_SHOW_COUNT);
        if (showCount == 1) {
            // First show
            mTipTv.setText(tips[0]);
            return;
        }
        if (tips.length == 1) {
            return;
        }
        mTipTv.setText(tips[(showCount - 2) % (tips.length - 1) + 1]);
    }

    @Override
    public WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.type = WindowManager.LayoutParams.TYPE_PHONE;
        lp.format = PixelFormat.RGBA_8888;
        lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        lp.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        this.setLayoutParams(lp);
        return lp;
    }

    @Override
    protected int getResId() {
        return R.layout.boost_plus_clean_fullscreen_dialog;
    }

    @Override
    protected boolean IsInitStatusBarPadding() {
        return false;
    }

    @Override
    public void onAddedToWindow(SafeWindowManager windowManager) {
        initAnimationLocation();

        Toolbar toolbar = (Toolbar) mContentView.findViewById(R.id.action_bar);
        toolbar.setTitle(getContext().getString(R.string.launcher_widget_boost_plus_title));
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsBackDisabled) {
                    onBackClicked();
                }
            }
        });
    }

    private void initAnimationLocation() {
        mDotTagIv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mDotTagIv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Rect location = ViewUtils.getLocationRect(mDotTagIv);
                int top = location.top - Utils.getStatusBarHeight(getContext());
                int left = location.left;
                ViewUtils.setMargins(mDotPositionTagIv, left, top, 0, 0);
            }
        });
    }

    private void initStopDialog() {
        mStopDialogV = ViewUtils.findViewById(mContentView, R.id.stop_dialog_view);
        // Stop Dialog title content
        TextView stopDialogTitleTv = (TextView) findViewById(R.id.custom_alert_title);
        TextView stopDialogBodyTv = (TextView) findViewById(R.id.custom_alert_body);
        stopDialogTitleTv.setText(getContext().getString(R.string.boost_plus_stop_clean_title));
        stopDialogBodyTv.setText(getContext().getString(R.string.boost_plus_stop_clean_content));
        // Stop Dialog button
        Button stopDialogCancelBtn = (Button) findViewById(R.id.custom_alert_cancel_btn);
        stopDialogCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissStopDialog();
            }
        });
        Button stopDialogOkBtn = (Button) findViewById(R.id.custom_alert_ok_btn);
        stopDialogOkBtn.setText(getContext().getString(R.string.boost_plus_stop_sure));
        stopDialogOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cancelClean()) {
                    dismissStopDialog();
                    showExitingDialog();
                } else {
                    dismissStopDialog();
                    dismissDialog();
                }
            }
        });
    }

    private void showExitingDialog() {
        mIsBackDisabled = true;
        if (null != mExitingRl) {
            mExitingRl.setVisibility(View.VISIBLE);
        }
        spinProgressWheel();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(getContext(), "Exiting Time Out...", Toast.LENGTH_SHORT).show();
                }
                dismissExitingDialog();
                onCancelExitClean(true, getCleanRemainingAppList());
            }
        }, TIMEOUT_EXITING_DIALOG);
    }

    private void dismissExitingDialog() {
        mIsBackDisabled = false;
        if (null != mExitingRl) {
            mExitingRl.setVisibility(View.GONE);
        }
        stopProgressWheel();
    }

    private void onCancelExitClean(final boolean isCanceled, final List<HSAppMemory> cleanRemainingApps) {
        mIsStartForceStopCancel = false;
        mIsStartRootCancel = false;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismissExitingDialog();
                if (isCanceled) {
                    onCancelDialogDismiss(cleanRemainingApps);
                } else {
                    dismissDialog();
                }
            }
        }, START_OFF_EXIT_CLEAN);
    }

    public void spinProgressWheel() {
        if (null == mExitingProgressWheel) {
            return;
        }
        mExitingProgressWheel.setFinishSpeed(500f / 360f);
        mExitingProgressWheel.setSpinSpeed(125f / 360f);
        mExitingProgressWheel.setBarSpinCycleTime(START_OFF_EXIT_CLEAN + 150);
        mExitingProgressWheel.setVisibility(VISIBLE);
        mExitingProgressWheel.spin();
    }

    private void stopProgressWheel() {
        if (null == mExitingProgressWheel) {
            return;
        }
        mExitingProgressWheel.stopSpinning();
    }

    public void showStopDialog() {
        if (null != mStopDialogV) {
            mStopDialogV.setVisibility(View.VISIBLE);
        }
    }

    private boolean isStopDialogShowing() {
        boolean isShowing = false;
        if (null != mStopDialogV) {
            isShowing = (mStopDialogV.getVisibility() == View.VISIBLE);
        }
        return isShowing;
    }

    private void dismissStopDialog() {
        if (null != mStopDialogV) {
            mStopDialogV.setVisibility(View.GONE);
        }
    }

    private void getCleanData() {
        if (null != mHome) {
            mSelectedAppList = new ArrayList<>(mHome.getAppsToClean());
        }
    }

    private int getSelectedSize() {
        return null == mSelectedAppList ? 0 : mSelectedAppList.size();
    }

    @Override
    protected FloatWindowManager.Type getType() {
        return FloatWindowManager.Type.BOOST_PLUS_CLEAN;
    }

    public void dismissDialog() {
        dismissDialog(0L);
    }

    public void dismissDialog(long actualDismissDelay) {
        onDialogDismiss();
        Runnable dismissRunnable = new Runnable() {
            @Override
            public void run() {
                if (null != mHome) {
                    mHome.dismissBoostPlusCleanDialog();
                }
            }
        };
        if (actualDismissDelay > 0) {
            mHandler.postDelayed(dismissRunnable, actualDismissDelay);
        } else {
            dismissRunnable.run();
        }
    }

    public void onCancelDialogDismiss(List<HSAppMemory> cleanRemainingApps) {
        onDialogDismiss();
        if (null != mHome) {
            mHome.onReturnFromCleanCancelled(cleanRemainingApps);
        }
    }

    private boolean cancelClean() {
        boolean cancelStart = false;
        if (HSAccTaskManager.getInstance().isRunning()) {
            HSLog.d(TAG, "non root cancelClean");
            cancelStart = true;
            mIsStartForceStopCancel = true;
            HSAccTaskManager.getInstance().cancel();
        } else if (mIsRootCleaning) {
            HSLog.d(TAG, "root cancelClean");
            cancelStart = true;
            mIsStartRootCancel = true;
            HSAppMemoryManager.getInstance().stopClean();
        } else {
            if (mIsStartGetPermission && mCleanResult != CleanResult.PERMISSION_SUCCESS) {
                HSLog.d(TAG, "cancelPermission");
                cancelStart = true;
                HSPermissionRequestMgr.getInstance().cancelRequest();
            }
        }
        return cancelStart;
    }

    private void onDialogDismiss() {
        HSLog.d(TAG, "onDialogDismiss mCleanResult = " + mCleanResult + " mIsStartGetPermission = " + mIsStartGetPermission);

        // Notify underlying result page to start its animations
        HSBundle resultPageAction = new HSBundle();
        NotificationCenter.sendStickyNotification(ResultPageActivity.NOTIFICATION_VISIBLE_TO_USER, resultPageAction);

        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (null != mDotsHandler) {
            mDotsHandler.removeCallbacksAndMessages(null);
        }
        if (null != mBgColorAnimator) {
            mBgColorAnimator.cancel();
        }
        if (null != mCircleInIV) {
            mCircleInIV.clearAnimation();
        }
        if (null != mCircleMiddleIv) {
            mCircleMiddleIv.clearAnimation();
        }
        if (null != mCircleOutIv) {
            mCircleOutIv.clearAnimation();
        }
    }

    private void startBoostAnimation() {
        HSLog.d(TAG, "startBoostAnimation ***");
        mIsFlurryLogResultShow = false;
        startCircleRotateAnimation();
        startDotsAnimation();
        startBackgroundChangedAnimation(ContextCompat.getColor(getContext(), R.color.boost_plus_red),
                ContextCompat.getColor(getContext(), R.color.boost_plus_yellow), DURATION_BACKGROUND_SINGLE_CHANGED);

        mCleanTimeOutRunnable = new Runnable() {
            @Override
            public void run() {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(getContext(), "Clean Time Out...", Toast.LENGTH_SHORT).show();
                }
                startDecelerateResultAnimation();
                mIsRootCleaning = false;
            }
        };
        mHandler.postDelayed(mCleanTimeOutRunnable, TIMEOUT_CLEAN);
    }

    private void cancelCleanTimeOut() {
        HSLog.d(TAG, "cancelCleanTimeOut ***");
        if (null != mCleanTimeOutRunnable) {
            mHandler.removeCallbacks(mCleanTimeOutRunnable);
        }
    }

    private void cancelGetPermissionTimeOut() {
        HSLog.d(TAG, "cancelGetPermissionTimeOut ***");
        if (null != mGetPermissionTimeOutRunnable) {
            mHandler.removeCallbacks(mGetPermissionTimeOutRunnable);
        }
    }

    private long getBackgroundCenterDuration() {
        int selectedSize = getSelectedSize();
        int factor = selectedSize - 1;
        if (factor < 1) {
            factor = 1;
        }
        if (factor > 5) {
            factor = 5;
        }
        return DURATION_BACKGROUND_SINGLE_CHANGED * factor;
    }

    @Thunk void startResultAnimation() {
        if (isStopDialogShowing()) {
            dismissStopDialog();
        }
        startRocketAnimation();
        startSmokeAnimation();
        startMemoryUsedDisappearAnimation();
        startCleanRunningDisappearAnimation();
        stopDotsAnimation();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ResultPageActivity.isAttached()) {
                    dismissDialog();
                } else {
                    HSGlobalNotificationCenter.addObserver(ResultPageActivity.NOTIFICATION_RESULT_PAGE_ATTACHED, new INotificationObserver() {
                        @Override
                        public void onReceive(String s, HSBundle hsBundle) {
                            HSGlobalNotificationCenter.removeObserver(this);
                            dismissDialog(500L); // Delay 500 ms
                        }
                    });
                }
            }
        }, 25 * FRAME);

        if (!mIsFlurryLogResultShow) {
            mIsFlurryLogResultShow = true;
            cleanFinished();
        }
        mIsResultViewShow = true;
        LauncherFloatWindowManager.isRemoveDialogFrozen = false;
    }

    private void cleanFinished() {
        if (null != mHome) {
            mHome.onCleanFinished();
        }
    }

    private void startCircleRotateAnimation() {
        mCircleInDynamicRotateAnimation = new DynamicRotateAnimation(CIRCLE_ROTATE_FACTOR);
        mCircleInDynamicRotateAnimation.setAnimationListener(new LauncherAnimationUtils.AnimationListenerAdapter() {
            @Override
            public void onAnimationStart(Animation animation) {
                super.onAnimationStart(animation);
                mStartCircleAnimationTime = System.currentTimeMillis();
                LauncherAnimUtils.startAlphaAppearAnimation(mCircleInIV, DURATION_CIRCLE_IN_ALPHA_ADD);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                mCircleInIV.clearAnimation();
                mCircleInIV.setVisibility(View.INVISIBLE);
            }
        });
        mCircleInIV.clearAnimation();
        mCircleInIV.startAnimation(mCircleInDynamicRotateAnimation);

        mCircleMiddleDynamicRotateAnimation = new DynamicRotateAnimation(CIRCLE_ROTATE_FACTOR);
        mCircleMiddleDynamicRotateAnimation.setAnimationListener(new LauncherAnimationUtils.AnimationListenerAdapter() {
            @Override
            public void onAnimationStart(Animation animation) {
                super.onAnimationStart(animation);
                LauncherAnimUtils.startAlphaAppearAnimation(mCircleMiddleIv, DURATION_CIRCLE_IN_ALPHA_ADD);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                mCircleMiddleIv.clearAnimation();
                mCircleMiddleIv.setVisibility(View.INVISIBLE);
            }
        });
        mCircleMiddleIv.clearAnimation();
        mCircleMiddleIv.startAnimation(mCircleMiddleDynamicRotateAnimation);

        mCircleOutDynamicRotateAnimation = new DynamicRotateAnimation(CIRCLE_ROTATE_FACTOR);
        mCircleOutDynamicRotateAnimation.setAnimationListener(new LauncherAnimationUtils.AnimationListenerAdapter() {
            @Override
            public void onAnimationStart(Animation animation) {
                super.onAnimationStart(animation);
                LauncherAnimUtils.startAlphaAppearAnimation(mCircleOutIv, DURATION_CIRCLE_IN_ALPHA_ADD);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                mCircleOutIv.clearAnimation();
                mCircleOutIv.setVisibility(View.INVISIBLE);
            }
        });
        mCircleOutIv.clearAnimation();
        mCircleOutIv.startAnimation(mCircleOutDynamicRotateAnimation);
    }

    private void startRocketAnimation() {
        int rocketToYDelta = mScreenHeight * POSITION_ROCKET_TRANSLATE / DEVICE_SCREEN_HEIGHT_TAG;
        Animation rocketAlphaAppearAnimation = LauncherAnimationUtils.getAlphaAppearAnimation(
                DURATION_ROCKET_ALPHA_ADD, START_OFF_CIRCLE_ROTATE_MAIN);
        Animation rocketAlphaNoChangeAnimation = LauncherAnimationUtils.getAlphaAppearNoChangeAnimation(
                DURATION_ROCKET_ALPHA_NO_CHANGE, DURATION_ROCKET_ALPHA_ADD + START_OFF_CIRCLE_ROTATE_MAIN);
        Animation rocketAlphaDisAppearAnimation = LauncherAnimationUtils.getAlphaDisAppearAnimation(
                DURATION_ROCKET_ALPHA_REDUCE,
                DURATION_ROCKET_ALPHA_ADD + DURATION_ROCKET_ALPHA_NO_CHANGE + START_OFF_CIRCLE_ROTATE_MAIN);
        Animation rocketTranslateAnimation = LauncherAnimationUtils.getTranslateYAnimation(0, -rocketToYDelta,
                DURATION_ROCKET_TRANSLATE, START_OFF_CIRCLE_ROTATE_MAIN, true, new AccelerateInterpolator(2.0f));
        LauncherAnimationUtils.startSetAnimation(mRocketIv, true, rocketAlphaAppearAnimation, rocketAlphaNoChangeAnimation,
                rocketAlphaDisAppearAnimation, rocketTranslateAnimation);
    }

    private void startSmokeAnimation() {
        int rocketToYDelta = mScreenHeight * POSITION_ROCKET_TRANSLATE / DEVICE_SCREEN_HEIGHT_TAG;
        Animation smokeAlphaAppearAnimation = LauncherAnimationUtils.getAlphaAppearAnimation(
                DURATION_SMOKE_ALPHA_ADD, START_OFF_CIRCLE_ROTATE_MAIN);
        Animation smokeAlphaDisAppearAnimation = LauncherAnimationUtils.getAlphaDisAppearAnimation(
                DURATION_SMOKE_ALPHA_REDUCE, DURATION_SMOKE_ALPHA_ADD + START_OFF_CIRCLE_ROTATE_MAIN);
        Animation smokeScaleYAnimation = LauncherAnimationUtils.getScaleYAnimation(
                SMOKE_FROM_Y, SMOKE_TO_Y, DURATION_SMOKE_SCALE, START_OFF_CIRCLE_ROTATE_MAIN);
        Animation smokeScaleXAnimation = LauncherAnimationUtils.getScaleXAnimation(
                SMOKE_FROM_X, SMOKE_TO_X, DURATION_SMOKE_SCALE, START_OFF_CIRCLE_ROTATE_MAIN);
        Animation smokeTranslateAnimation = LauncherAnimationUtils.getTranslateYAnimation(0, -rocketToYDelta,
                DURATION_ROCKET_TRANSLATE, START_OFF_CIRCLE_ROTATE_MAIN, true, new AccelerateInterpolator(2.0f));
        LauncherAnimationUtils.startSetAnimation(mSmokeIv, true, smokeAlphaAppearAnimation, smokeAlphaDisAppearAnimation,
                smokeScaleYAnimation, smokeScaleXAnimation, smokeTranslateAnimation);
    }

    private int getAppTotalSizeMbs() {
        int totalMbs = 0;
        if (null != mSelectedAppList) {
            long totalBytes = 0;
            for (int i = 0; i < mSelectedAppList.size(); i++) {
                HSAppMemory hsAppMemory = mSelectedAppList.get(i);
                if (null != hsAppMemory) {
                    long hsAppMemoryBytes = hsAppMemory.getSize();
                    totalBytes += hsAppMemoryBytes;
                }
            }
            totalMbs = (int) (totalBytes / 1024 / 1024);
        }
        return totalMbs;
    }

    private long getCleanAppSize(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return 0;
        }
        if (null != mSelectedAppList) {
            for (int i = 0; i < mSelectedAppList.size(); i++) {
                HSAppMemory hsAppMemory = mSelectedAppList.get(i);
                if (null != hsAppMemory && packageName.equals(hsAppMemory.getPackageName())) {
                    return hsAppMemory.getSize() / 1024 / 1024;
                }
            }
        }
        return 0;
    }

    private void startMemoryUsedAnimation(long duration, long startNumber, long endNumber) {
        mBoostingTextLl.setVisibility(View.VISIBLE);
        mMemoryUsedNumberTv.startAnimation(duration, startNumber, endNumber);
    }

    private void startMemoryUsedDisappearAnimation() {
        Animation memoryUsedAlphaDisAppearAnimation = LauncherAnimationUtils.getAlphaDisAppearAnimation(
                DURATION_MEMORY_USED_ALPHA_REDUCE, START_OFF_CIRCLE_ROTATE_MAIN);
        mBoostingTextLl.setTag(true);
        LauncherAnimationUtils.startAnimation(mBoostingTextLl, memoryUsedAlphaDisAppearAnimation,
                new LauncherAnimationUtils.AnimationListenerAdapter(){
            @Override
            public void onAnimationStart(Animation animation) {
                super.onAnimationStart(animation);
                // Use tag to record animation start status. When screen off then start animation in some phones,
                // it will be onAnimationEnd callback before onAnimationStart.
                Object tag = mBoostingTextLl.getTag();
                if (null != tag && tag instanceof Boolean && (Boolean) tag) {
                    mBoostingTextLl.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                mBoostingTextLl.setVisibility(View.INVISIBLE);
                mBoostingTextLl.setTag(false);
            }
        });
    }

    private void startCleanRunningDisappearAnimation() {
        startViewDisappearAnimation(mCleaningTv);
        startViewDisappearAnimation(mCleaningAppNumberTv);
        startViewDisappearAnimation(mTipTv);
        startViewDisappearAnimation(mTipTitleTv);
    }

    private void startViewDisappearAnimation(final View textView) {
        Animation cleaningAlphaDisAppearAnimation = LauncherAnimationUtils.getAlphaDisAppearAnimation(
                DURATION_ROCKET_ALPHA_REDUCE, DURATION_CLEANING_TEXT_START_OFF);
        textView.setTag(true);
        LauncherAnimationUtils.startAnimation(textView, cleaningAlphaDisAppearAnimation, new LauncherAnimationUtils.AnimationListenerAdapter() {
            @Override
            public void onAnimationStart(Animation animation) {
                super.onAnimationStart(animation);
                // Use tag to record animation start status. When screen off then start animation in some phones,
                // it will be onAnimationEnd callback before onAnimationStart.
                Object tag = textView.getTag();
                if (null != tag && tag instanceof Boolean && (Boolean) tag) {
                    textView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                textView.setVisibility(View.INVISIBLE);
                textView.setTag(false);
            }
        });
    }

    private void startClean(final int cleanType) {
        HSLog.d(TAG, "startClean cleanType = " + cleanType);
        if (null != mSelectedAppList && mSelectedAppList.size() > 0) {
            Collections.sort(mSelectedAppList, new Comparator<HSAppMemory>() {
                @Override
                public int compare(HSAppMemory o1, HSAppMemory o2) {
                    return (int) (o2.getSize() - o1.getSize());
                }
            });

            List<String> selectedPackageList = new ArrayList<>();
            for (HSAppMemory hsAppMemory : mSelectedAppList) {
                if (null != hsAppMemory) {
                    String packageName = hsAppMemory.getPackageName();
                    if (!TextUtils.isEmpty(packageName)) {
                        selectedPackageList.add(packageName);
                    }
                }
            }
            final int totalSizeMbs = getAppTotalSizeMbs();
            mCurrentLastCleanSize = totalSizeMbs;

            if (cleanType == CLEAN_TYPE_NON_ROOT) {
                int timeout = HSConfig.optInteger(DEFAULT_ACC_CLEAN_TIMEOUT_SECONDS,
                        "Application", "BoostPlus", "AccCleanTimeoutSeconds");
                HSAccTaskManager.getInstance().startForceStop(selectedPackageList, timeout, new HSAccTaskManager.AccTaskListener() {
                    @Override
                    public void onStarted() {
                        mCleanedAppPackageNameList.clear();
                        onCleanStarted(totalSizeMbs, true);
                    }

                    @Override
                    public void onProgressUpdated(int processedCount, int total, String packageName) {
                        mCleanedAppPackageNameList.add(packageName);
                        onCleanProgressUpdated(processedCount, total, packageName, true);
                    }

                    @Override
                    public void onSucceeded() {
                        cancelCleanTimeOut();
                        onCleanSucceeded(CLEAN_TYPE_NON_ROOT);
                    }

                    @Override
                    public void onFailed(int code, String failMsg) {
                        cancelCleanTimeOut();
                        onCleanFailed(CLEAN_TYPE_NON_ROOT, code, failMsg);
                    }
                });
            } else {
                HSLog.d(TAG, "startClean root or normal ***** startClean ***** cleanType = " + cleanType);
                if (null != mSelectedAppList) {
                    int total = mSelectedAppList.size();
                    startNormalCleanImgCurveAnimation(0, total, totalSizeMbs);
                }

                HSAppMemoryManager.getInstance().startClean(mSelectedAppList, false, new HSAppMemoryManager.MemoryTaskListener() {
                    @Override
                    public void onStarted() {
                    }

                    @Override
                    public void onProgressUpdated(int processedCount, int total, HSAppMemory hsAppMemory) {
                    }

                    @Override
                    public void onSucceeded(List<HSAppMemory> list, long size) {
                    }

                    @Override
                    public void onFailed(int code, String failMsg) {
                    }
                });
            }
        }
    }

    // Region Clean Task Handling Methods
    @Thunk void onCleanStarted(long totalSizeMB, boolean shouldStartImgCurveAnimation) {
        if (mSelectedAppList != null && !mSelectedAppList.isEmpty()) {
            HSAppMemory hSAppMemory = mSelectedAppList.get(0);
            if (null != hSAppMemory) {
                String startPackageName = hSAppMemory.getPackageName();
                long endNumber = (mSelectedAppList.size() == 1) ? 0 : (totalSizeMB - getCleanAppSize(startPackageName));
                boolean isEnd = false;
                if (endNumber == 0) {
                    endNumber = mCurrentLastCleanSize / END_SIZE_LAST_PERCENT;
                    isEnd = true;
                }
                startMemoryUsedAnimation(isEnd ?
                                DURATION_END_SIZE_DECELERATE_FACTOR * DURATION_CLEAN_ITEM_APP
                                : DURATION_CLEAN_ITEM_APP,
                        totalSizeMB, endNumber);
                if (shouldStartImgCurveAnimation) {
                    startImgCurveAnimation(BoostAnimationManager.Boost.ICON_ONE, startPackageName);
                }
                mCleaningAppNumberTv.setText(1 + "/" + mSelectedAppList.size());
                mCurrentLastCleanSize = endNumber;
            }
        }
    }

    @Thunk void onCleanProgressUpdated(int processedCount, int total, String packageName, boolean shouldStartImgCurveAnimation) {
        startBackgroundChangedAnimation(ContextCompat.getColor(getContext(), R.color.boost_plus_yellow),
                ContextCompat.getColor(getContext(), R.color.boost_plus_clean_green), getBackgroundCenterDuration());
        String animationPackageName = "";
        if (0 <= processedCount && processedCount < getSelectedSize()) {
            HSAppMemory hSAppMemory = mSelectedAppList.get(processedCount);
            if (null != hSAppMemory) {
                animationPackageName = hSAppMemory.getPackageName();
            }
        }
        HSLog.d(TAG, "onProgressUpdated Clean progressCount = " + processedCount
                + " total = " + total + " packageName = " + packageName + " animationPackageName = " + animationPackageName);

        if (!TextUtils.isEmpty(animationPackageName) || processedCount < total) {
            long endNumber = (processedCount == total - 1) ? 0 : (mCurrentLastCleanSize - getCleanAppSize(animationPackageName));
            boolean isEnd = false;
            if (endNumber == 0) {
                endNumber = mCurrentLastCleanSize / END_SIZE_LAST_PERCENT;
                isEnd = true;
            }
            startMemoryUsedAnimation(isEnd ?
                            DURATION_END_SIZE_DECELERATE_FACTOR * DURATION_CLEAN_ITEM_APP
                            : DURATION_CLEAN_ITEM_APP,
                    mCurrentLastCleanSize, endNumber);
            if (shouldStartImgCurveAnimation) {
                startImgCurveAnimation(processedCount, animationPackageName);
            }
            mCleaningAppNumberTv.setText((processedCount + 1) + "/" + total);
            mCurrentLastCleanSize = endNumber;
        }
    }

    @Thunk void onCleanSucceeded(int cleanType) {
        HSLog.d(TAG, "onSucceeded cleanType = " + cleanType + " mIsStartForceStopCancel = "
                + mIsStartForceStopCancel + " mIsStartRootCancel = " + mIsStartRootCancel);
        mCleanResult = BoostPlusCleanDialog.CleanResult.CLEAN_SUCCESS;

        if (cleanType == CLEAN_TYPE_NON_ROOT && mIsStartForceStopCancel
                || (cleanType == CLEAN_TYPE_ROOT && mIsStartRootCancel)) {
            HSLog.d(TAG, "onSucceeded ****** force stop ****** onSucceeded onCancelExitClean");
            onCancelExitClean(true, mSelectedAppList);
        } else {
            startDecelerateAndGetPermission(cleanType);
        }
    }

    @Thunk void onCleanFailed(int cleanType, int code, String failMsg) {
        HSLog.d(TAG, "onCleanFailed cleanType = " + cleanType + " code = "
                + code + " failMsg = " + failMsg + " mIsStartRootCancel = " + mIsStartRootCancel);
        mCleanResult = BoostPlusCleanDialog.CleanResult.CLEAN_FAILED;

        if (cleanType == CLEAN_TYPE_NON_ROOT && code == HSAccTaskManager.FAIL_CANCEL
                || (cleanType == CLEAN_TYPE_ROOT && code == HSAppMemoryManager.FAIL_CANCEL)
                || (cleanType == CLEAN_TYPE_ROOT && mIsStartRootCancel)) {
            HSLog.d(TAG, "onCleanFailed ****** onCancelExitClean");
            onCancelExitClean(true, getCleanRemainingAppList());
        } else {
            startDecelerateAndGetPermission(cleanType);
        }
    }

    private List<HSAppMemory> getCleanRemainingAppList() {
        if (null == mSelectedAppList) {
            return null;
        }

        List<HSAppMemory> hsAppMemoryList = new ArrayList<>();
        hsAppMemoryList.addAll(mSelectedAppList);
        if (mCleanedAppPackageNameList.size() > 0) {
            for (String packageName : mCleanedAppPackageNameList) {
                if (!TextUtils.isEmpty(packageName)) {
                    for (HSAppMemory hsAppMemory : mSelectedAppList) {
                        if (null != hsAppMemory && hsAppMemory.getPackageName().equals(packageName)) {
                            hsAppMemoryList.remove(hsAppMemory);
                        }
                    }
                }
            }
        }
        return hsAppMemoryList;
    }

    public boolean isCleanResultViewShow() {
        return mIsResultViewShow;
    }

    private void startNormalCleanImgCurveAnimation(int processCount, int total, long totalSizeMbs) {
        HSLog.d(TAG, "startNormalCleanImgCurveAnimation ***** processCount = " + processCount + " total = " + total + " totalSizeMbs = " + totalSizeMbs);
        if (null == mSelectedAppList) {
            return;
        }

        if (processCount >= mSelectedAppList.size()) {
            return;
        }

        HSAppMemory hsAppMemory = mSelectedAppList.get(processCount);
        boolean isCleanEnd = (processCount == total - 1);

        if (null != hsAppMemory) {
            String packageName = hsAppMemory.getPackageName();
            if (!TextUtils.isEmpty(packageName)) {
                if (processCount == 0) {
                    HSLog.d(TAG, "startClean root onStarted totalSizeMbs = " + totalSizeMbs);
                    mIsRootCleaning = true;
                    mCleanedAppPackageNameList.clear();
                    onCleanStarted(totalSizeMbs, false);
                }

                HSLog.d(TAG, "startClean root onProgressUpdated processedCount = " + processCount + " total = " + total);
                mCleanedAppPackageNameList.add(packageName);
                onCleanProgressUpdated(processCount + 1, total, packageName, false);
                startImgCurveAnimation(processCount + 1, packageName);
            }

            if (isCleanEnd) {
                HSLog.d(TAG, "startClean root onSucceeded");
                mCleanedAppPackageNameList.add(packageName);
                onCleanProgressUpdated(processCount + 1, total, packageName, false);

                mHandler.postDelayed(() -> {
                    mIsRootCleaning = false;
                    cancelCleanTimeOut();
                    onCleanSucceeded(CLEAN_TYPE_ROOT);
                }, DELAY_IMG_CURVE_ANIMATION * 4);
                return;
            }
        }

        mHandler.postDelayed(() -> startNormalCleanImgCurveAnimation(processCount + 1, total, totalSizeMbs), DELAY_IMG_CURVE_ANIMATION + (long) (Math.random() * DELAY_IMG_CURVE_ANIMATION));
    }

    private void startDecelerateAndGetPermission(int cleanType) {
        dismissExitingDialog();
        boolean isNotificationListeningGranted = com.ihs.permission.Utils.isNotificationListeningGranted();
        boolean isUsageAccessGranted = com.ihs.permission.Utils.isUsageAccessGranted();
        boolean isRoot = (cleanType == CLEAN_TYPE_ROOT);

        HSLog.i(TAG, "startDecelerateAndGetPermission isRoot = " + isRoot
                + " isNotificationListeningGranted = " + isNotificationListeningGranted
                + " isUsageAccessGranted = " + isUsageAccessGranted);
        if (isRoot) {
            startDecelerateResultAnimation();
            return;
        }

        if (HSConfig.optBoolean(false, "Application", "BoostPlus", "PermissionAcquireFeatureEnabled")) {
            if (!isUsageAccessGranted && HSPreferenceHelper.getDefault().getInt(PREF_KEY_USAGE_REQUEST_TIME, 0) < 2) {
                startGetPermission(HSPermissionType.TYPE_USAGE_ACCESS);
            } else if (!isNotificationListeningGranted
                    && HSPreferenceHelper.getDefault().getInt(PREF_KEY_NOTIFICATION_REQUEST_TIME, 0) < 2) {
                startGetPermission(HSPermissionType.TYPE_NOTIFICATION_LISTENING);
            } else {
                startDecelerateResultAnimation();
            }
        } else {
            startDecelerateResultAnimation();
        }
    }

    private void startGetPermission(final HSPermissionType type) {
        mIsStartGetPermission = true;
        mIsPermissionGetting = true;
        if (BuildConfig.DEBUG) {
            Toast.makeText(getContext(), "Start get permission...", Toast.LENGTH_SHORT).show();
        }

        if (type.equals(HSPermissionType.TYPE_NOTIFICATION_LISTENING)) {
            int notificationRequestTime = HSPreferenceHelper.getDefault().getInt(PREF_KEY_NOTIFICATION_REQUEST_TIME, 0);
            HSPreferenceHelper.getDefault().putInt(PREF_KEY_NOTIFICATION_REQUEST_TIME, ++notificationRequestTime);
        } else if (type.equals(HSPermissionType.TYPE_USAGE_ACCESS)) {
            int usageRequestTime = HSPreferenceHelper.getDefault().getInt(PREF_KEY_USAGE_REQUEST_TIME, 0);
            HSPreferenceHelper.getDefault().putInt(PREF_KEY_USAGE_REQUEST_TIME, ++usageRequestTime);
        }

        HSLog.i(TAG, "startGetPermission type = " + type);
        HSPermissionRequestMgr.getInstance().startRequest(EnumSet.of(type), new HSPermissionRequestCallback.Stub() {
            @Override
            public void onFinished(int succeedCount, int totalCount) {
                HSLog.i(TAG, "permission request finished, succeeded " + succeedCount + " , total " + totalCount);
                dismissExitingDialog();
                cancelGetPermissionTimeOut();
                mCleanResult = CleanResult.PERMISSION_SUCCESS;
                startDecelerateResultAnimation();
                mIsPermissionGetting = false;
            }

            @Override
            public void onSinglePermissionStarted(int index) {
                HSLog.i(TAG, "permission request index " + index + " started");
            }

            @Override
            public void onSinglePermissionFinished(int index, boolean isSucceed) {
                HSLog.i(TAG, "permission request index " + index + " finished, result " + isSucceed);
            }

            @Override
            public void onCancelled() {
                super.onCancelled();
                HSLog.d(TAG, "onCancelled ****** get permission ****** dismissExitingDialog");
                cancelGetPermissionTimeOut();
                mIsPermissionGetting = false;
                onCancelExitClean(false, mSelectedAppList);
            }
        });

        mGetPermissionTimeOutRunnable = new Runnable() {
            @Override
            public void run() {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(getContext(), "Get permission Time Out...", Toast.LENGTH_SHORT).show();
                }
                startDecelerateResultAnimation();
                mIsPermissionGetting = false;
            }
        };
        mHandler.postDelayed(mGetPermissionTimeOutRunnable, TIMEOUT_GET_PERMISSION);
    }

    private void startImgCurveAnimation(int processIndex, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return;
        }
        HSLog.d(TAG, "startImgCurveAnimation processIndex = " + processIndex + " packageName = " + packageName);

        Drawable currentDrawable = Utils.getAppIcon(packageName);

        int animationIndex;
        if (processIndex < 0) {
            animationIndex = 0;
        } else if (processIndex < BoostAnimationManager.COUNT_ICON) {
            animationIndex = processIndex;
        } else {
            animationIndex = processIndex % BoostAnimationManager.COUNT_ICON;
        }

        int[] location = new int[2];
        mBoostCenterIv.getLocationOnScreen(location);
        float endX = location[0];
        float endY = location[1];
        BoostAnimationManager boostAnimationManager = new BoostAnimationManager(endX, endY, true);

        if (null != currentDrawable) {
            ImageView animationIv = getCleanImageView(animationIndex);
            if (null != animationIv) {
                animationIv.setImageDrawable(currentDrawable);
                boostAnimationManager.startIconAnimation(animationIv, animationIndex);
            }
        }
    }

    private ImageView getCleanImageView(int index) {
        switch (index) {
            case BoostAnimationManager.Boost.ICON_ONE:
                return mIconOneV;
            case BoostAnimationManager.Boost.ICON_TWO:
                return mIconTwoV;
            case BoostAnimationManager.Boost.ICON_THREE:
                return mIconThreeV;
            case BoostAnimationManager.Boost.ICON_FOUR:
                return mIconFourV;
            case BoostAnimationManager.Boost.ICON_FIVE:
                return mIconFiveV;
            case BoostAnimationManager.Boost.ICON_SIX:
                return mIconSixV;
            case BoostAnimationManager.Boost.ICON_SEVEN:
                return mIconSevenV;
            default:
                return mIconOneV;
        }
    }


    private void startDotsAnimation() {
        // dots
        mDotsAnimationCount = 0;
        for (int i = 0; i < DOTS_COUNT; i++) {
            mDotsHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDotsAnimationCount++;
                    if (mCleanMainRl.getTag() != null && mCleanMainRl.getTag() instanceof Boolean && (Boolean) mCleanMainRl.getTag()) {
                        mDotsHandler.removeCallbacksAndMessages(null);
                        return;
                    }
                    startDotAnimation();
                    if (mDotsAnimationCount == DOTS_COUNT - 1) {
                        mDotsHandler.removeCallbacksAndMessages(null);
                        startDotsAnimation();
                    }
                }
            }, (i + 1) * 5 * FRAME_DOTS);
        }
    }

    private void stopDotsAnimation() {
        if (null != mCleanMainRl) {
            mCleanMainRl.setTag(true);
        }
    }

    private void startDotAnimation() {
        final AppCompatImageView dotView = new AppCompatImageView(getContext());
        VectorCompat.setImageViewVectorResource(getContext(), dotView, R.drawable.boost_plus_light_dot_svg);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_LEFT, R.id.dot_normal_anchor_iv);
        params.addRule(RelativeLayout.ALIGN_TOP, R.id.dot_normal_anchor_iv);
        Random random = new Random();
        int radius = random.nextInt(CommonUtils.pxFromDp(50)) + CommonUtils.pxFromDp(100);
        double radians = random.nextDouble() * 2 * Math.PI;
        int leftMargin = (int) (radius * Math.sin(radians));
        int topMargin = (int) (radius * Math.cos(radians));
        params.leftMargin = leftMargin;
        params.topMargin = topMargin;
        mBoostIconContainer.addView(dotView, params);

        ObjectAnimator dotAnimation = ObjectAnimator.ofPropertyValuesHolder(dotView,
                PropertyValuesHolder.ofFloat(View.ALPHA, 0.2f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0),
                PropertyValuesHolder.ofFloat(View.TRANSLATION_X, -leftMargin),
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, -topMargin));
        dotAnimation.setDuration(8 * FRAME_DOTS);
        dotAnimation.setInterpolator(new AccelerateInterpolator(2));
        dotAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mBoostIconContainer.removeView(dotView);
            }
        });
        dotAnimation.start();
    }

    @Thunk void startDecelerateResultAnimation() {
        NotificationCenter.clearNotification(ResultPageActivity.NOTIFICATION_VISIBLE_TO_USER);
        ResultPageActivity.startForBoostPlus((Activity) getContext(), getAppTotalSizeMbs());

        startBackgroundChangedAnimation(ContextCompat.getColor(getContext(), R.color.boost_plus_clean_green),
                ContextCompat.getColor(getContext(), R.color.boost_plus_clean_bg), DURATION_BACKGROUND_END_CHANGED);
        startMemoryUsedAnimation(DURATION_CLEAN_ITEM_APP_END_LAST, mCurrentLastCleanSize, 0);
        long startOffset;
        long duration = System.currentTimeMillis() - mStartCircleAnimationTime;
        if (duration >= DURATION_ROTATE_MAIN) {
            startOffset = 0;
        } else {
            startOffset = DURATION_ROTATE_MAIN - duration;
        }
        HSLog.d(TAG, "startDecelerateResultAnimation startOffset = " + startOffset);

        Runnable decelerateRunnable = new Runnable() {
            @Override
            public void run() {
                if (null != mHome) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (null != mCircleInDynamicRotateAnimation) {
                                mCircleInDynamicRotateAnimation.startDecelerateMode();
                            }
                            if (null != mCircleMiddleDynamicRotateAnimation) {
                                mCircleMiddleDynamicRotateAnimation.startDecelerateMode();
                            }
                            if (null != mCircleOutDynamicRotateAnimation) {
                                mCircleOutDynamicRotateAnimation.startDecelerateMode();
                            }

                            LauncherAnimUtils.startAlphaDisappearAnimation(mCircleInIV, DURATION_CIRCLE_IN_ALPHA_REDUCE);
                            LauncherAnimUtils.startAlphaDisappearAnimation(mCircleMiddleIv, DURATION_CIRCLE_IN_ALPHA_REDUCE);
                            LauncherAnimUtils.startAlphaDisappearAnimation(mCircleOutIv, DURATION_CIRCLE_IN_ALPHA_REDUCE);
                        }
                    });
                }
            }
        };
        mHandler.postDelayed(decelerateRunnable, startOffset);

        Runnable tickRunnable = new Runnable() {
            @Override
            public void run() {
                startResultAnimation();
            }
        };
        mHandler.postDelayed(tickRunnable, startOffset);
    }

    private void startBackgroundChangedAnimation(int colorFrom, int colorEnd, long duration) {
        if (null != mBgColorAnimator && mBgColorAnimator.isRunning()) {
            mBgColorAnimator.cancel();
        }
        int backgroundColor = ViewUtils.getBackgroundColor(mContainerV);
        if (backgroundColor != 0) {
            colorFrom = backgroundColor;
        }
        mBgColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorEnd);
        mBgColorAnimator.setDuration(duration);
        mBgColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mContainerV.setBackgroundColor((int) animator.getAnimatedValue());
            }
        });
        mBgColorAnimator.start();
    }

    private void onBackClicked() {
        boolean isStopDialogShowing = isStopDialogShowing();
        HSLog.d(TAG, "onBackClicked ******* isStopDialogShowing = " + isStopDialogShowing
                + " mIsResultViewShow = " + mIsResultViewShow);
        if (isStopDialogShowing) {
            dismissStopDialog();
            return;
        }

        boolean isNonRootCleaning = HSAccTaskManager.getInstance().isRunning();
        HSLog.d(TAG, "onBackClicked ******* isNonRootCleaning = " + isNonRootCleaning + " mIsRootCleaning = "
                + mIsRootCleaning + " mIsPermissionGetting = " + mIsPermissionGetting);
        if (isNonRootCleaning || mIsRootCleaning || mIsPermissionGetting) {
            HSAnalytics.logEvent("BoostPlus_Animation_BackAlert_Show", "Type", "Btn Back");
            showStopDialog();
        } else {
            if (mIsResultViewShow) {
                HSAnalytics.logEvent("BoostPlus_ResultPage_Back", "Type", "Btn Back");
            }
            dismissDialog();
        }
    }
}
