package com.ihs.inputmethod.uimodules.ui.fonts.homeui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacter;
import com.ihs.inputmethod.settings.RadioButtonPreference;
import com.ihs.inputmethod.uimodules.R;

import java.util.List;

/**
 * Created by guonan.lv on 17/8/14.
 */

public class FontCardAdapter extends RecyclerView.Adapter<FontCardViewHolder> {

    private List<FontModel> fontModelList;
    private OnFontCardClickListener onFontCardClickListener;
    private RadioButton selectRadioButton = null;

    public FontCardAdapter(List<FontModel> fontModels, OnFontCardClickListener onFontCardClickListener) {
        this.onFontCardClickListener = onFontCardClickListener;
        this.fontModelList = fontModels;
    }

    @Override
    public FontCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FontCardViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_font_card, parent, false));
    }

    @Override
    public void onBindViewHolder(final FontCardViewHolder holder, final int position) {
        if(fontModelList == null) {
            return;
        }

        FontModel fontModel = fontModelList.get(position);
        HSSpecialCharacter hsSpecialCharacter = fontModel.getHsSpecialCharacter();
        holder.fontCardContent.setText(hsSpecialCharacter.example);
        if(fontModel.getNeedDownload()) {
            holder.downloadIcon.setImageResource(R.drawable.ic_download_icon);
        } else {
            holder.downloadIcon.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onFontCardClickListener != null) {
                    if(selectRadioButton != null)  {
                        selectRadioButton.setChecked(false);
                    }
                    selectRadioButton = holder.radioButton;
                    selectRadioButton.setChecked(true);
                }
                onFontCardClickListener.onFontCardClick();
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
