package com.ihs.inputmethod.uimodules.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.uimodules.R;

public final class CustomFontTextView extends TextView{
    private static final String FONT_FuturaStd_Heavy_="fonts/FuturaStd-Heavy.otf";
    private static final String FONT_PhosphateSolid_="fonts/PhosphateSolid.ttf";
    private static final String FONT_Roboto_Medium_="fonts/Roboto-Medium.ttf";
    private static final String FONT_Roboto_Light_="fonts/Roboto-Light.ttf";
    
    public static final String FONT_FuturaStd_Heavy="FuturaStd_Heavy";
    public static final String FONT_PhosphateSolid="PhosphateSolid";
    public static final String FONT_Roboto_Medium="Roboto_Medium";
    public static final String FONT_Roboto_Light="Roboto_Light";
    public CustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        changeTypeFace(context, attrs);
    }
    public CustomFontTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        changeTypeFace(context, attrs);
    }
    private void changeTypeFace(Context context,AttributeSet attrs){
        if (attrs != null){
            TypedArray a=context.obtainStyledAttributes(attrs, R.styleable.CustomFontTextView);
            final int n=a.getIndexCount();
            for(int i=0;i<n;i++){
                int attr=a.getIndex(i);
                if (attr == R.styleable.CustomFontTextView_fancyfont) {
                    String fonts = a.getString(attr);
                    changeFont(context, fonts);
                } else {
                    HSLog.e("Error, no such font.");
                    break;
                }
            }
            a.recycle();
        }  
    }
    private void changeFont(Context context,String font) {
       if(font.equals(FONT_FuturaStd_Heavy)){
           Typeface mtf = Typeface.createFromAsset(context.getAssets(),  
                   FONT_FuturaStd_Heavy_);  
           super.setTypeface(mtf);  
       }else if(font.equals(FONT_PhosphateSolid)){
           Typeface mtf = Typeface.createFromAsset(context.getAssets(),  
                   FONT_PhosphateSolid_);
           super.setTypeface(mtf); 
       }else if(font.equals(FONT_Roboto_Medium)){
           Typeface mtf = Typeface.createFromAsset(context.getAssets(),  
                   FONT_Roboto_Medium_);
           super.setTypeface(mtf); 
       }else if(font.equals(FONT_Roboto_Light)){
           Typeface mtf = Typeface.createFromAsset(context.getAssets(),  
                   FONT_Roboto_Light_);
           super.setTypeface(mtf); 
       }else{
           HSLog.e("No such fonts-->"+font);
       }
    }
    
    public void changeFacyFont(String font){
        changeFont(getContext(), font);
    }
}
