package com.ihs.inputmethod.uimodules.ui.theme.iap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.reward.RewardItem;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.iap.HSIAPManager;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.reward.RewardVideoHelper;
import com.ihs.inputmethod.uimodules.utils.RippleDrawableUtils;
import com.keyboard.core.themes.custom.KCCustomThemeData;
import com.keyboard.core.themes.custom.elements.KCBaseElement;

import org.json.JSONObject;


/**
 * Created by jixiang on 16/5/3.
 */
public class HSThemePromptPurchaseView extends LinearLayout implements View.OnClickListener, HSIAPManager.HSIAPListener, RewardVideoHelper.RewardResultListener {

    View rootView;
    ImageView themeImage;
    TextView themeState;
    TextView themeName;
    TextView themePrice;
    TextView allThemePrice;
    View closeButton;
    View themePriceLayout;
    View watchViewUnlockViewLayout;

    RewardVideoHelper rewardVideoHelper;
    IItemClickListener itemClickListener;
    KCBaseElement currentCustomThemeItem;

    public HSThemePromptPurchaseView(Context context) {
        this(context, null);
    }

    public HSThemePromptPurchaseView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HSThemePromptPurchaseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClickable(true);
        rewardVideoHelper = new RewardVideoHelper((Activity) context, this);
        rootView = View.inflate(context, R.layout.prompt_theme_purchase_view, this);
        initView();
        HSIAPManager.getInstance().addListener(this);
    }

    private void initView() {
        themeState = (TextView) rootView.findViewById(R.id.theme_state);
        themeName = (TextView) rootView.findViewById(R.id.theme_name);
        themeImage = (ImageView) rootView.findViewById(R.id.theme_image);
        themePriceLayout = rootView.findViewById(R.id.theme_price_layout);
        watchViewUnlockViewLayout = rootView.findViewById(R.id.watch_video_unlock_layout);
        themePrice = (TextView) rootView.findViewById(R.id.theme_price);
        allThemePrice = (TextView) rootView.findViewById(R.id.all_theme_price);
        closeButton = rootView.findViewById(R.id.close_button);

        float radius = HSApplication.getContext().getResources().getDimension(R.dimen.common_round_corner);
        themePriceLayout.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(0xffd53bf1, radius));
        allThemePrice.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(0xff931ae8, radius));
        watchViewUnlockViewLayout.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(0xffffffff, 0xffaaaaaa, radius));
        ((TextView) findViewById(R.id.watch_video_unlock_text)).setText(HSConfig.optString(getContext().getResources().getString(R.string.unlock_theme_element_via_watch_video),
                "Application", "ButtonConfig", "CustomizeThemeAlert", "WatchVideo"));

        GradientDrawable background = (GradientDrawable) rootView.findViewById(R.id.purchase_view_layout).getBackground();
        background.setColor(Color.parseColor("#ffffff"));

        watchViewUnlockViewLayout.setOnClickListener(this);
        themePriceLayout.setOnClickListener(this);
        allThemePrice.setOnClickListener(this);
        closeButton.setOnClickListener(this);
    }

    public void setIconBackground(Drawable backgroundDrawable) {
        themeImage.setBackgroundDrawable(backgroundDrawable);
    }

    public void setIcon(KCBaseElement item) {
        if (currentCustomThemeItem == item) {
            return;
        }
        currentCustomThemeItem = item;

        String state = getContext().getString(R.string.custom_theme_background) + " " + getContext().getString(R.string.locked);
        if (item.getTypeName() == "font") {
            state = getContext().getString(R.string.custom_theme_font) + " " + getContext().getString(R.string.locked);
        } else if (item.getTypeName() == "click_sound") {
            state = getContext().getString(R.string.custom_theme_sound) + " " + getContext().getString(R.string.locked);
        }

        setCurrentThemePrice(IAPManager.getManager().getThemePrice(currentCustomThemeItem));
        setAllThemePrice(IAPManager.getManager().getAllPrice(currentCustomThemeItem));
        setThemeState(state);
        setThemeName(item.getName());
        setThemeImage(item.getPreview());
    }

    public void setThemeState(String state) {
        themeState.setText(state);
    }

    public void setThemeName(String name) {
        themeName.setText(name);
    }

    public void setThemeImage(Drawable drawable) {
        if (drawable instanceof RoundedBitmapDrawable) {
            RoundedBitmapDrawable backgroundIconDrawable = (RoundedBitmapDrawable) drawable;
            if (HSApplication.getContext().getResources().getBoolean(R.bool.isTablet)) {
                backgroundIconDrawable.setCornerRadius(10);
            } else {
                backgroundIconDrawable.setCornerRadius(10);
            }
            themeImage.setImageDrawable(backgroundIconDrawable);
        } else {
            themeImage.setImageDrawable(drawable);
        }
    }

    public void setCurrentThemePrice(String price) {
        String currentThemePriceStr = getResources().getString(R.string.current_theme_price);
        currentThemePriceStr = String.format(currentThemePriceStr, price);
        themePrice.setText(currentThemePriceStr);
    }

    public void setAllThemePrice(String price) {
        allThemePrice.setText(price);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.theme_price_layout) {
            purchaseCurrentItemTheme();
        } else if (id == R.id.all_theme_price) {
            purchaseCurrentTypeWholeItemTheme();
        } else if (id == R.id.close_button) {
            if (itemClickListener != null) {
                itemClickListener.onCloseButtonClick();
            }
        } else if (id == R.id.watch_video_unlock_layout) {
            rewardVideoHelper.loadAndShowVideo();
            HSGoogleAnalyticsUtils.getInstance().logAppEvent("iapalert_custom_WatchVideoToUnlock_clicked");
        }
    }


    @Override
    public void onRewardedSuccess() {
        if (currentCustomThemeItem != null) {
            IAPManager.getManager().unlockProductViaWatchVideo(IAPManager.getManager().getProductId(currentCustomThemeItem));
        }
        setVisibility(GONE);
        HSGoogleAnalyticsUtils.getInstance().logAppEvent("iapalert_custom_WatchVideoToUnlock_VideoCompleted");
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
    }

    @Override
    public void onRewardedVideoStartLoad() {
        setVisibility(GONE);
    }

    @Override
    public void onRewardedVideoStart() {
        setVisibility(GONE);
        HSGoogleAnalyticsUtils.getInstance().logAppEvent("iapalert_custom_WatchVideoToUnlock_VideoStarted");
    }

    @Override
    public void onRewardedVideoLoadTimeout() {
        setVisibility(GONE);
    }

    @Override
    public void onRewardedFinish() {
        setVisibility(GONE);
    }

    private void purchaseCurrentItemTheme() {
        String purchaseId = IAPManager.getManager().getProductId(currentCustomThemeItem);
        IAPManager.getManager().purchaseProduct(purchaseId);
        HSGoogleAnalyticsUtils.getInstance().logAppEvent("app_iapalert_custom_unlockone_clicked");
    }

    private void purchaseCurrentTypeWholeItemTheme() {
        String purchaseId = IAPManager.getManager().getCurrentTypeWholeItemProductId(currentCustomThemeItem);
        IAPManager.getManager().purchaseProduct(purchaseId);
        HSGoogleAnalyticsUtils.getInstance().logAppEvent("app_iapalert_custom_unlockall_clicked");
    }

    @Override
    public void onPurchaseSucceeded(String productId) {
    }

    @Override
    public void onPurchaseFailed(String productId, int errorCode) {
    }

    @Override
    public void onVerifySucceeded(String productId, JSONObject jsonObject) {
        //最终购买成功在这边提示
        IAPManager.getManager().onVerifySuccessed(productId, jsonObject);
        Toast.makeText(HSApplication.getContext(), HSApplication.getContext().getString(R.string.purchase_success), Toast.LENGTH_LONG).show();
        setVisibility(GONE);
    }

    @Override
    public void onVerifyFailed(String productId, int errorCode) {
        IAPManager.getManager().onVerifyFailed(productId, errorCode);
    }

    public interface IItemClickListener {
        void onCloseButtonClick();
    }

    public void addProductPurchaseListener(IItemClickListener listener) {
        itemClickListener = listener;
    }

    @Override
    protected void onDetachedFromWindow() {
        HSIAPManager.getInstance().removeListener(this);
        super.onDetachedFromWindow();
    }

}
