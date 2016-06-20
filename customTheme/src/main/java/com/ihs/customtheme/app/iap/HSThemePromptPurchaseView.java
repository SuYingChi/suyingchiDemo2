package com.ihs.customtheme.app.iap;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.customtheme.R;
import com.ihs.iap.HSIAPManager;
import com.ihs.inputmethod.theme.HSCustomThemeItemBase;

import org.json.JSONObject;


/**
 * Created by jixiang on 16/5/3.
 */
public class HSThemePromptPurchaseView extends LinearLayout implements View.OnClickListener, HSIAPManager.HSIAPListener {

    View rootView;
    ImageView themeImage;
    TextView themeState;
    TextView themeName;
    TextView themePrice;
    TextView allThemePrice;
    ImageView closeButton;

    HSCustomThemeItemBase currentCustomThemeItem;
    IItemClickListener itemClickListener;

    public HSThemePromptPurchaseView(Context context) {
        super(context);
    }

    public HSThemePromptPurchaseView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HSThemePromptPurchaseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClickable(true);
        rootView = View.inflate(context, R.layout.prompt_theme_purchase_view, this);
        initView();
        HSIAPManager.getInstance().addListener(this);
    }

    private void initView() {
        themeState = (TextView) rootView.findViewById(R.id.theme_state);
        themeName = (TextView) rootView.findViewById(R.id.theme_name);
        themeImage = (ImageView) rootView.findViewById(R.id.theme_image);
        themePrice = (TextView) rootView.findViewById(R.id.theme_price);
        allThemePrice = (TextView) rootView.findViewById(R.id.all_theme_price);
        closeButton = (ImageView) rootView.findViewById(R.id.close_button);

        StateListDrawable currentPriceDrawable = getStateListDrawable("#d53bf1", "#711f80");
        themePrice.setBackgroundDrawable(currentPriceDrawable);
        StateListDrawable forAllPriceDrawable = getStateListDrawable("#931ae8", "#4e0d7b");
        allThemePrice.setBackgroundDrawable(forAllPriceDrawable);

        GradientDrawable background = (GradientDrawable)rootView.findViewById(R.id.purchase_view_layout).getBackground();
        background.setColor(Color.parseColor("#ffffff"));

        themePrice.setOnClickListener(this);
        allThemePrice.setOnClickListener(this);
        closeButton.setOnClickListener(this);
    }

    private StateListDrawable getStateListDrawable(String normalColor, String pressColor) {
        int strokeWidth = 0;
        int roundRadius = getContext().getResources().getDimensionPixelSize(R.dimen.shape_corners_radius);
        int strokeColor = Color.parseColor("#FFFFFF");//边框颜色
        int fillColor = Color.parseColor(normalColor);//内部填充颜色
        GradientDrawable normalBg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[0]);//创建drawable
        normalBg.setColor(fillColor);
        normalBg.setCornerRadius(roundRadius);
        normalBg.setStroke(strokeWidth, strokeColor);

        fillColor = Color.parseColor(pressColor);
        GradientDrawable pressBg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[0]);//创建drawable
        pressBg.setColor(fillColor);
        pressBg.setCornerRadius(roundRadius);
        pressBg.setStroke(strokeWidth, strokeColor);

        final StateListDrawable drawable=new StateListDrawable();
        drawable.addState(new int[] { android.R.attr.state_focused },pressBg);
        drawable.addState(new int[] { android.R.attr.state_pressed }, pressBg);
        drawable.addState(new int[] { android.R.attr.state_selected }, pressBg);
        drawable.addState(new int[] {}, normalBg);

        return drawable;
    }

    public void swapData(HSCustomThemeItemBase item){
        if(currentCustomThemeItem == item){
            return;
        }
        currentCustomThemeItem = item;
        if(item.getItemBackgroundDrawable()!=null){
            themeImage.setBackgroundDrawable(item.getItemBackgroundDrawable());
        }else {
            themeImage.setBackgroundDrawable(null);
        }

        String state = "Background Locked";
        if(item.getItemType() == HSCustomThemeItemBase.ItemType.FONT){
            state = "Font Locked";
        }

        setCurrentThemePrice(IAPManager.getManager().getThemePrice(currentCustomThemeItem));
        setAllThemePrice(IAPManager.getManager().getAllPrice(currentCustomThemeItem));
        setThemeState(state);
        setThemeName(item.getItemName());
        setThemeImage(item.getDrawable());
    }

    public void setThemeState(String state){
        themeState.setText(state);
    }

    public void setThemeName(String name){
        themeName.setText(name);
    }

    public void setThemeImage(Drawable drawable){
        if(drawable instanceof RoundedBitmapDrawable) {
            RoundedBitmapDrawable backgroundIconDrawable = (RoundedBitmapDrawable) drawable;
            if (HSApplication.getContext().getResources().getBoolean(R.bool.isTablet)) {
                backgroundIconDrawable.setCornerRadius(10);
            } else {
                backgroundIconDrawable.setCornerRadius(10);
            }
            themeImage.setImageDrawable(backgroundIconDrawable);
        }else {
            themeImage.setImageDrawable(drawable);
        }
    }

    public void setCurrentThemePrice(String price){
        String currentThemePriceStr = getResources().getString(R.string.current_theme_price);
        currentThemePriceStr = String.format(currentThemePriceStr,price);
        themePrice.setText(currentThemePriceStr);
    }

    public void setAllThemePrice(String price){
        String allThemePriceStr = getResources().getString(R.string.all_theme_price);
        allThemePriceStr = String.format(allThemePriceStr,price);
        allThemePrice.setText(allThemePriceStr);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.theme_price){
            Toast.makeText(getContext(),"invoke pay sdk to buy current theme",Toast.LENGTH_SHORT).show();
            purchaseCurrentItemTheme();
        }else if(id == R.id.all_theme_price){
            Toast.makeText(getContext(),"invoke pay sdk to buy all theme",Toast.LENGTH_SHORT).show();
            purchaseCurrentTypeWholeItemTheme();
        }else if(id == R.id.close_button){
            if(itemClickListener !=null){
                itemClickListener.onCloseButtonClick();
            }
        }
    }

    private void purchaseCurrentItemTheme(){
        String purchaseId = IAPManager.getManager().getProductId(currentCustomThemeItem);
        HSIAPManager.getInstance().purchaseIAPProduct(purchaseId);
    }

    private void purchaseCurrentTypeWholeItemTheme(){
        String purchaseId = IAPManager.getManager().getCurrentTypeWholeItemProductId(currentCustomThemeItem);
        HSIAPManager.getInstance().purchaseIAPProduct(purchaseId);
    }

    @Override
    public void onPurchaseSucceeded(String productId) {
        HSLog.d("onIAPProductPurchaseSucceeded:productId:"+productId);
        Log.d("jx","onIAPProductPurchaseSucceeded:productId:"+productId);
    }

    @Override
    public void onPurchaseFailed(String productId, int errorCode) {
        HSLog.d("onIAPProductPurchaseFailed:productId:"+productId+",errorCode:"+errorCode);
        Log.d("jx","onIAPProductPurchaseFailed:productId:"+productId+",errorCode:"+errorCode);
    }

    @Override
    public void onVerifySucceeded(String productId, JSONObject jsonObject) {
        HSLog.d("onIAPProductVerifySucceeded:productId:"+productId);
        Log.d("jx","onIAPProductVerifySucceeded:productId:"+productId);
        //最终购买成功在这边提示
        IAPManager.getManager().onVerifySuccessed(productId,jsonObject);
        Toast.makeText(HSApplication.getContext(),"purchase success ",Toast.LENGTH_LONG).show();
        setVisibility(GONE);
    }

    @Override
    public void onVerifyFailed(String productId, int errorCode) {
        HSLog.d("onVerifyFailed:errorCode:"+errorCode);
        Log.d("jx","onVerifyFailed:errorCode:"+errorCode);
        IAPManager.getManager().onVerifyFailed(productId,errorCode);
    }


    public interface IItemClickListener{
        void onCloseButtonClick();
    }

    public void addProductPurchaseListener(IItemClickListener listener){
        itemClickListener = listener;
    }

    @Override
    protected void onDetachedFromWindow() {
        HSIAPManager.getInstance().removeListener(this);
        super.onDetachedFromWindow();
    }

}
