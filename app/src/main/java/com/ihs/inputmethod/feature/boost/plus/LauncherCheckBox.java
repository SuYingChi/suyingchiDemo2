package com.ihs.inputmethod.feature.boost.plus;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CompoundButton;

import java.lang.reflect.Field;

/**
 * This class differs from {@link android.support.v7.widget.AppCompatCheckBox} in that it provides a
 * {@link #setChecked(boolean, boolean)} method to toggle the check box without firing
 * {@link OnCheckedChangeListener} callback.
 */
public class LauncherCheckBox extends android.support.v7.widget.AppCompatCheckBox {

    private Field mBroadcastingField;

    public LauncherCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);

        Field broadcastingField = null;
        try {
            broadcastingField = CompoundButton.class.getDeclaredField("mBroadcasting");
            broadcastingField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        // Cache the field
        mBroadcastingField = broadcastingField;
    }

    /**
     * @param notify Whether to notify {@link OnCheckedChangeListener} callback about
     *               this change.
     */
    public void setChecked(boolean checked, boolean notify) {
        if (notify || mBroadcastingField == null) {
            // Super implementation of setCheck() is equivalent to a setChecked(checked, true) call
            super.setChecked(checked);
            return;
        }

        try {
            // Temporarily suppress the callback by setting a mBroadcasting field to true
            mBroadcastingField.setBoolean(this, true);
            super.setChecked(checked);
            mBroadcastingField.setBoolean(this, false);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
