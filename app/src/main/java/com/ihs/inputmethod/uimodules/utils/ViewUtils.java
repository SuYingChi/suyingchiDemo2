package com.ihs.inputmethod.uimodules.utils;

import android.view.View;
import android.view.ViewGroup;

import com.ihs.commons.utils.HSLog;

/**
 * Created by jixiang on 16/4/8.
 */
public class ViewUtils {
    // --Commented out by Inspection (18/1/11 下午2:41):public final static int PIC_WIDTH = 1080;
    // --Commented out by Inspection (18/1/11 下午2:41):public final static int PIC_HEIGHT = 1920;
    public final static int FLAG_LEFTMARGIN = 1;
    public final static int FLAG_TOPMARGIN = 2;
    public final static int FLAG_RIGHTMARGIN = 4;
    public final static int FLAG_BOTTOMMARGIN = 8;

// --Commented out by Inspection START (18/1/11 下午2:41):
//    /**
//     * 重新设置view的宽高，保持图片的长宽比，依照控件的实际宽度，来设置控件的实际高度。长宽比通过后面的宽高算出
//     *
//     * @param view
//     * @param width     控件的实际宽度
//     * @param picWidth  预期效果的宽度
//     * @param picHeight 预期效果的高度
//     */
//    public static void resetViewSize(View view, int width, int picWidth, int picHeight) {
//        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
//        layoutParams.width = width;
//        layoutParams.height = (int) (picHeight * 1.0f * width / picWidth);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public static void setViewMarginValue(View view, int flag, int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
//        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
//        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
//            ViewGroup.MarginLayoutParams realLayoutParmas = (ViewGroup.MarginLayoutParams) layoutParams;
//            setMargin(flag, leftMargin, topMargin, rightMargin, bottomMargin, realLayoutParmas);
//        } else {
//            throw new RuntimeException("ViewUtils.setViewMarginValue error,because the view's layoutparams should be either LinearLayout.LayoutParams or FrameLayout.LayoutParams");
//        }
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    private static void setMargin(int flag, int leftMargin, int topMargin, int rightMargin, int bottomMargin, ViewGroup.MarginLayoutParams realLayoutParmas) {
        if ((flag & FLAG_LEFTMARGIN) == FLAG_LEFTMARGIN) {
            realLayoutParmas.leftMargin = leftMargin;
            HSLog.d("AAAAA leftMargin:" + leftMargin);
        }
        if ((flag & FLAG_TOPMARGIN) == FLAG_TOPMARGIN) {
            realLayoutParmas.topMargin = topMargin;
            HSLog.d("AAAAA topMargin:" + topMargin);
        }
        if ((flag & FLAG_RIGHTMARGIN) == FLAG_RIGHTMARGIN) {
            realLayoutParmas.rightMargin = rightMargin;
            HSLog.d("AAAAA rightMargin:" + rightMargin);
        }
        if ((flag & FLAG_BOTTOMMARGIN) == FLAG_BOTTOMMARGIN) {
            realLayoutParmas.bottomMargin = bottomMargin;
            HSLog.d("AAAAA bottomMargin:" + bottomMargin);
        }
    }

}
