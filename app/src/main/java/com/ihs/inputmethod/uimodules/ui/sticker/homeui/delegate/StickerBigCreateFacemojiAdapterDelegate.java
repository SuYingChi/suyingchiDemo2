package com.ihs.inputmethod.uimodules.ui.sticker.homeui.delegate;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.StickerHomeModel;
import com.ihs.inputmethod.uimodules.utils.RippleDrawableUtils;

import java.util.List;

public class StickerBigCreateFacemojiAdapterDelegate extends AdapterDelegate<List<StickerHomeModel>> {

    @Override
    protected boolean isForViewType(@NonNull List<StickerHomeModel> items, int position) {
        return items.get(position).isBigCreateFacemoji;
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        StickerBigCreateFacemojiViewHolder stickerBigCreateFacemojiViewHolder = new StickerBigCreateFacemojiViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_big_create_facemoji, parent, false));

        int width = parent.getResources().getDisplayMetrics().widthPixels;
        int halfWidth = width / 2;
        int height = (int) (width / 2 * 0.8);

        //整个图片占据空间屏幕宽的一半，让其左右边距是屏幕宽一半的10%，控件大小为屏幕宽的一半的80%
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) stickerBigCreateFacemojiViewHolder.faceImage.getLayoutParams();
        layoutParams.leftMargin = (int) (halfWidth * 0.1);
//        layoutParams.rightMargin = (int) (halfWidth * 0.1);
        layoutParams.width = (int) (halfWidth * 0.8);
        layoutParams.height = layoutParams.width;

        layoutParams = (RelativeLayout.LayoutParams) stickerBigCreateFacemojiViewHolder.tipText.getLayoutParams();
        layoutParams.topMargin = (int) (height * 0.15);

        layoutParams = (RelativeLayout.LayoutParams) stickerBigCreateFacemojiViewHolder.createBtn.getLayoutParams();
        layoutParams.bottomMargin = (int) (height * 0.15);

        stickerBigCreateFacemojiViewHolder.createBtn.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(0xffffe011,HSApplication.getContext().getResources().getDimension(R.dimen.corner_radius)));

        return stickerBigCreateFacemojiViewHolder;
    }

    @Override
    protected void onBindViewHolder(@NonNull List<StickerHomeModel> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        StickerBigCreateFacemojiViewHolder h = (StickerBigCreateFacemojiViewHolder) holder;
        h.createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HSAnalytics.logEvent("app_facemoji_creste_clicked_banner");
                try {
                    Intent i = new Intent(HSApplication.getContext(), Class.forName("com.ihs.inputmethod.uimodules.ui.facemoji.ui.CameraActivity"));
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    HSApplication.getContext().startActivity(i);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    throw new RuntimeException("com.ihs.inputmethod.uimodules.ui.facemoji.ui.CameraActivity not find");
                }
            }
        });
    }

    @Override
    public int getSpanSize(List<StickerHomeModel> items, int position) {
        return 6;
    }


    public final class StickerBigCreateFacemojiViewHolder extends RecyclerView.ViewHolder {
        ImageView faceImage;
        TextView tipText;
        TextView createBtn;

        public StickerBigCreateFacemojiViewHolder(View itemView) {
            super(itemView);
            tipText = (TextView) itemView.findViewById(R.id.facemoji_text);
            createBtn = (TextView) itemView.findViewById(R.id.facemoji_create);
            faceImage = (ImageView) itemView.findViewById(R.id.face_arrow);
        }
    }
}
