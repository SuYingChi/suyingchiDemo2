package com.ihs.inputmethod.uimodules.ui.sticker;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.R;

/**
 * Created by yanxia on 2017/6/9.
 */

public class StickerDownloadView extends LinearLayout {

    public StickerGroup stickerGroup;

    public StickerDownloadView(Context context) {
        super(context);
    }

    public StickerDownloadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StickerDownloadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Finalize inflating a view from XML.  This is called as the last phase
     * of inflation, after all child views have been added.
     * <p>
     * <p>Even if the subclass overrides onFinishInflate, they should always be
     * sure to call the super method, so that we get called.
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        TextView textViewShowName = findViewById(R.id.sticker_download_show_name);
        TextView textViewDescription = findViewById(R.id.sticker_download_description);
        if (!HSKeyboardThemeManager.getCurrentTheme().isDarkBg()) {
            textViewShowName.setTextColor(Color.parseColor("#37474f"));
            textViewDescription.setTextColor(Color.parseColor("#37474f"));
        } else {
            textViewShowName.setTextColor(Color.WHITE);
            textViewDescription.setTextColor(Color.WHITE);
        }
    }

    public void setStickerGroup(StickerGroup stickerGroup) {
        this.stickerGroup = stickerGroup;
    }

    public StickerGroup getStickerGroup() {
        return stickerGroup;
    }
}
