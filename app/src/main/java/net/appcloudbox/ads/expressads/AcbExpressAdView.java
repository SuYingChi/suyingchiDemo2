package net.appcloudbox.ads.expressads;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Arthur on 2018/1/31.
 */

public class AcbExpressAdView extends View {
    public AcbExpressAdView(Context context) {
        super(context);
    }

    public AcbExpressAdView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AcbExpressAdView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public interface AcbExpressAdViewListener {
        void onAdShown(AcbExpressAdView var1);

        void onAdClicked(AcbExpressAdView var1);
    }
}
