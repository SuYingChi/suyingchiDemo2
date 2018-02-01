package com.ihs.inputmethod.uimodules.utils;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

/**
 * Created by hdd on 16/4/5.
 */
public class TextUtils {
// --Commented out by Inspection START (18/1/11 下午2:41):
//    /**
//     * TextView set span text color
//     * @param textView
//     * @param start if start < 0  reverse order
//     * @param end if end < 0  reverse order & the last index is -1
//     * @param color
//     */
//    public static void setForegroundColorSpan(final TextView textView, int start, int end, int color){
//
//        int length = textView.getText().length();
//
//        if(!(length > 0 && Math.abs(end) < length && Math.abs(start) < length)){
//            return;
//        }
//
//        if(start < 0){
//            start = length + start;
//        }
//
//        if(end < 0){
//            end = length + end + 1;
//        }
//
//        if(start > end){
//            return;
//        }
//
//        SpannableStringBuilder ssb = new SpannableStringBuilder(textView.getText());
//        ssb.setSpan(new ForegroundColorSpan(color), start, end, 0);
//        textView.setText(ssb);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    /**
//     * TextView : set span text onClick
//     * @param textView
//     * @param start if start < 0 reverse order
//     * @param end if end < 0  reverse order & the last index is -1
//     * @param color text color
//     * @param showUnderline
//     * @param spanClick
//     */
//    public static void setClickableSpan(final TextView textView, int start, int end, final int color, final boolean showUnderline, final OnSpanClick spanClick){
//
//        int length = textView.getText().length();
//
//        if(!(length > 0 && Math.abs(end) < length && Math.abs(start) < length)){
//            return;
//        }
//
//        if(start < 0){
//            start = length + start;
//        }
//
//        if(end < 0){
//            end = length + end + 1;
//        }
//
//        if(start > end){
//            return;
//        }
//
//        SpannableString ss = new SpannableString(textView.getText());
//        ss.setSpan(new ClickableSpan() {
//            @Override
//            public void onClick(View widget) {
//                spanClick.onClick();
//            }
//
//            @Override
//            public void updateDrawState(TextPaint ds) {
//                super.updateDrawState(ds);
//                ds.setColor(color);
//                ds.clearShadowLayer();
//                ds.setUnderlineText(showUnderline);
//            }
//        }, start, end, 0);
//        textView.setMovementMethod(LinkMovementMethod.getInstance());
//        textView.setText(ss);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)


    public interface OnSpanClick {

        void onClick();

    }
}
