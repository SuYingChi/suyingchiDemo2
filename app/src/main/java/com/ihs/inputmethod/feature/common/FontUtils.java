package com.ihs.inputmethod.feature.common;

import android.graphics.Typeface;
import android.util.SparseArray;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility for text fonts.
 */
public class FontUtils {

    public enum Font {
        ROBOTO_LIGHT(0),
        ROBOTO_REGULAR(1),
        ROBOTO_MEDIUM(2),
        ROBOTO_THIN(3),
        ROBOTO_CONDENSED(4),
        DS_DIGIB(5),
        AKROBAT_LIGHT(6),
        PROXIMA_NOVA_REGULAR(7),
        PROXIMA_NOVA_LIGHT(8),
        PROXIMA_NOVA_THIN(9),
        PROXIMA_NOVA_SEMIBOLD(11),
        PROXIMA_NOVA_REGULAR_CONDENSED(12);

        private int mValue;

        Font(int value) {
            mValue = value;
        }

        int getResId() {
            switch (mValue) {
                case 0:
                    return R.string.roboto_light;
                case 1:
                    return R.string.roboto_regular;
                case 2:
                    return R.string.roboto_medium;
                case 3:
                    return R.string.roboto_thin;
                case 4:
                    return R.string.roboto_condensed;
                case 5:
                    return R.string.ds_digib;
                case 6:
                    return R.string.akrobat_light;
                case 7:
                    return R.string.proxima_nova_regular;
                case 8:
                    return R.string.proxima_nova_light;
                case 9:
                    return R.string.proxima_nova_thin;
                case 11:
                    return R.string.proxima_nova_semibold;
                case 12:
                    return R.string.proxima_nova_regular_condensed;
            }
            return R.string.roboto_regular;
        }

        public static Font ofFontResId(int resId) {
            switch (resId) {
                case R.string.roboto_light:
                    return ROBOTO_LIGHT;
                case R.string.roboto_regular:
                    return ROBOTO_REGULAR;
                case R.string.roboto_medium:
                    return ROBOTO_MEDIUM;
                case R.string.roboto_thin:
                    return ROBOTO_THIN;
                case R.string.roboto_condensed:
                    return ROBOTO_CONDENSED;
                case R.string.ds_digib:
                    return DS_DIGIB;
                case R.string.akrobat_light:
                    return AKROBAT_LIGHT;
                case R.string.proxima_nova_regular:
                    return PROXIMA_NOVA_REGULAR;
                case R.string.proxima_nova_light:
                    return PROXIMA_NOVA_LIGHT;
                case R.string.proxima_nova_thin:
                    return PROXIMA_NOVA_THIN;
                case R.string.proxima_nova_semibold:
                    return PROXIMA_NOVA_SEMIBOLD;
                case R.string.proxima_nova_regular_condensed:
                    return PROXIMA_NOVA_REGULAR_CONDENSED;

            }
            return null;
        }
    }

    private static SparseArray<Typeface> sFontCache = new SparseArray<>(5);

    private static List<Integer> sCustomFontsResIds = new ArrayList<>(8);
    static {
        sCustomFontsResIds.add(R.string.ds_digib);
        sCustomFontsResIds.add(R.string.akrobat_light);
        sCustomFontsResIds.add(R.string.proxima_nova_regular);
        sCustomFontsResIds.add(R.string.proxima_nova_light);
        sCustomFontsResIds.add(R.string.proxima_nova_thin);
        sCustomFontsResIds.add(R.string.proxima_nova_semibold);
        sCustomFontsResIds.add(R.string.proxima_nova_regular_condensed);
    }

    public static Typeface getTypeface(Font font) {
        return getTypeface(font, Typeface.NORMAL);
    }

    public static Typeface getTypeface(Font font, int style) {
        if (font != null) {
            int fontResId = font.getResId();
            Typeface typeface = sFontCache.get(fontResId);
            if (sCustomFontsResIds.contains(fontResId)) {
                if (typeface != null) {
                    return typeface;
                }
                try {
                    typeface = Typeface.createFromAsset(HSApplication.getContext().getAssets(),
                            "fonts/" + HSApplication.getContext().getString(fontResId) + ".ttf");
                } catch (RuntimeException e) {
                    try {
                        typeface = Typeface.createFromAsset(HSApplication.getContext().getAssets(),
                                "fonts/" + HSApplication.getContext().getString(fontResId) + ".otf");
                    } catch (RuntimeException ingored) {
                        return null;
                    }
                }
                sFontCache.put(fontResId, typeface);
            } else {
                // Already cached by framework.
                typeface = Typeface.create(HSApplication.getContext().getString(fontResId), style);
            }
            return typeface;
        }
        return null;
    }
}
