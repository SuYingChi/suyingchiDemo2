package com.ihs.booster.common.sizeformat;


import com.ihs.app.framework.HSApplication;
import com.ihs.booster.R;

/**
 * Created by sharp on 15/8/28.
 */
public class FormatSizeBuilder {
    private static final long KB_IN_BYTES = 1024;
    private static final long MB_IN_BYTES = KB_IN_BYTES * 1024;
    private static final long GB_IN_BYTES = MB_IN_BYTES * 1024;
    private static final long TB_IN_BYTES = GB_IN_BYTES * 1024;
    private static final long PB_IN_BYTES = TB_IN_BYTES * 1024;

    public String size, unit, sizeUnit;

    public FormatSizeBuilder(long sizeBytes) {
        int unitResid = R.string.byteShort;
        float result = sizeBytes;
        if (result > 900) {
            unitResid = R.string.kilobyteShort;
            result = result / 1024;
        }
        if (result > 900) {
            unitResid = R.string.megabyteShort;
            result = result / 1024;
        }
        if (result > 900) {
            unitResid = R.string.gigabyteShort;
            result = result / 1024;
        }
        if (result > 900) {
            unitResid = R.string.terabyteShort;
            result = result / 1024;
        }
        if (result > 900) {
            unitResid = R.string.petabyteShort;
            result = result / 1024;
        }
        size = "0";
        if (result < 10) {
            size = String.format("%.2f", result);
        } else if (result < 100) {
            size = String.format("%.1f", result);
        } else {
            size = String.valueOf(Float.valueOf(result).intValue());
        }

        this.unit = HSApplication.getContext().getString(unitResid);
        this.sizeUnit = size + " " + unit;
    }
}
