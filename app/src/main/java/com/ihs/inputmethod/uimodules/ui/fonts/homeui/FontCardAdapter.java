package com.ihs.inputmethod.uimodules.ui.fonts.homeui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacter;
import com.ihs.inputmethod.uimodules.R;

import java.util.List;

/**
 * Created by guonan.lv on 17/8/14.
 */

public class FontCardAdapter extends RecyclerView.Adapter<FontCardViewHolder> {

    private List<FontModel> fontModelList;
    private OnFontCardClickListener onFontCardClickListener;

    public FontCardAdapter(List<FontModel> fontModels, OnFontCardClickListener onFontCardClickListener) {
        this.onFontCardClickListener = onFontCardClickListener;
        this.fontModelList = fontModels;
    }

    @Override
    public FontCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FontCardViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_font_card, parent, false));
    }

    @Override
    public void onBindViewHolder(FontCardViewHolder holder, int position) {
        if(fontModelList == null) {
            return;
        }

        FontModel fontModel = fontModelList.get(position);
        HSSpecialCharacter hsSpecialCharacter = fontModel.getHsSpecialCharacter();
        holder.fontCardContent.setText(hsSpecialCharacter.example);
        holder.downloadIcon.setImageResource(R.drawable.ic_download_icon);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onFontCardClickListener != null) {
                    onFontCardClickListener.onFontCardClick();
                }
            }
        });
        if(fontModel.getNeedDownload()) {
            holder.radioButton.setVisibility(View.GONE);
        } else {
            holder.radioButton.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return fontModelList.size();
    }

    public interface OnFontCardClickListener {
        void onFontCardClick();
    }
}
