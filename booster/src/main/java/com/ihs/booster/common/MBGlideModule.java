package com.ihs.booster.common;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.module.GlideModule;
import com.bumptech.glide.request.target.ViewTarget;
import com.ihs.booster.R;
import com.ihs.booster.utils.L;

/**
 * Created by sharp on 16/4/25.
 */
public class MBGlideModule implements GlideModule {
    public void applyOptions(Context context, GlideBuilder builder) {
        L.l("MBGlideModule:" + context + " builder:" + builder.toString());
        ViewTarget.setTagId(R.id.glide_tag_id);
    }


    public void registerComponents(Context context, Glide glide) {

    }
}
