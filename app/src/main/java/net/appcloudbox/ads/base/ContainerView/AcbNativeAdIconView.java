package net.appcloudbox.ads.base.ContainerView;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Arthur on 2018/1/31.
 */

public class AcbNativeAdIconView  extends View {
    private ImageView.ScaleType imageViewScaleType;

    public AcbNativeAdIconView(Context context) {
        super(context);
    }

    public void setImageViewScaleType(ImageView.ScaleType imageViewScaleType) {
        this.imageViewScaleType = imageViewScaleType;
    }
}
