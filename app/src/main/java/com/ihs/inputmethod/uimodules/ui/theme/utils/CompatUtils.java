package com.ihs.inputmethod.uimodules.ui.theme.utils;

import android.os.Build;
import android.support.v7.widget.CardView;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;

/**
 * Created by jixiang on 16/8/22.
 *
 * 兼容类，用于各版本之间的UI协调
 */
public class CompatUtils {

    /**
     * 获取v21之前版本最大的阴影值
     * @return
     */
    public static int getCommonCardViewMaxElevation() {
        return HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.common_card_view_max_elevation);
    }

    public static int updateGapValueAccordingVersion(int gap) {
        //因CardView在v21之前，会将阴影部分当成控件的大小计算，因此，recyclerView的row和column之间的间隙可以去除这部分，防止item之间间隙太大
        if (Build.VERSION.SDK_INT < 21) {
            return gap - getCommonCardViewMaxElevation();
        }
        return gap;
    }

    public static void setCardViewMaxElevation(CardView cardView){
        //将v21以前的cardView的MaxCardElevation改为2dp，防止其item之间的距离太大
        if (Build.VERSION.SDK_INT < 21) {
            cardView.setMaxCardElevation(getCommonCardViewMaxElevation());
        }
    }
}
