package com.ihs.inputmethod.uimodules.ui.fonts.homeui;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacter;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacterManager;
import com.ihs.inputmethod.uimodules.R;

import java.util.List;

/**
 * Created by guonan.lv on 17/8/14.
 */

public class FontCardAdapter extends RecyclerView.Adapter<FontCardViewHolder> {

    private List<FontModel> fontModelList;
    private OnFontCardClickListener onFontCardClickListener;
    private int currentSelectPosition = -1;
    public final static int MORE_FONT_COMING_TYPE = 2;
    private String FROM_FRAGMENT_TYPE;

    public FontCardAdapter(List<FontModel> fontModels, OnFontCardClickListener onFontCardClickListener) {
        this.onFontCardClickListener = onFontCardClickListener;
        this.fontModelList = fontModels;
    }

    @Override
    public FontCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        currentSelectPosition = HSSpecialCharacterManager.getCurrentSpecialCharacterIndex();
        return new FontCardViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_font_card, parent, false));
    }

    private void fontSelected(final FontModel fontModel) {
        if(fontModel.isFontDownloaded()) {
            int position = fontModelList.indexOf(fontModel);
            if (currentSelectPosition != position) {
                if (currentSelectPosition != -1) {
                    notifyItemChanged(currentSelectPosition, 0);
                }
                currentSelectPosition = position;
                notifyItemChanged(position, 0);
            }
        }

        if (onFontCardClickListener != null) {
            onFontCardClickListener.onFontCardClick(fontModel);
        }
    }

    public void setFragmentType(String type) {
        FROM_FRAGMENT_TYPE = type;
    }

    @Override
    public void onBindViewHolder(final FontCardViewHolder holder, final int position) {
        if(fontModelList == null) {
            return;
        }

        if(isFromFontHomeType() && getItemViewType(position) == MORE_FONT_COMING_TYPE) {
            holder.moreFontHint.setVisibility(View.VISIBLE);
            holder.fontCardView.setVisibility(View.GONE);
            return;
        }

        final FontModel fontModel = fontModelList.get(position);
        final HSSpecialCharacter hsSpecialCharacter = fontModel.getHsSpecialCharacter();
        holder.fontCardContent.setText(hsSpecialCharacter.example);
        if(!fontModel.isFontDownloaded()) {
            holder.downloadIcon.setImageResource(R.drawable.ic_download_icon);
        } else {
            holder.downloadIcon.setVisibility(View.GONE);
            holder.radioButton.setVisibility(View.VISIBLE);
            holder.radioButton.setChecked(position == currentSelectPosition);
            holder.radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fontSelected(fontModel);
                }
            });
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fontSelected(fontModel);
            }
        });
    }
    
    public int getcurrentSelectPosition() {
        return currentSelectPosition;
    }

    public void setCurrentSelectPosition(int position) {
        currentSelectPosition = position;
    }

    @Override
    public int getItemCount() {
        if (isFromFontHomeType()) {
            return fontModelList.size()+1;
        }
        return  fontModelList.size();
    }

    private boolean isFromFontHomeType() {
        return TextUtils.equals(FROM_FRAGMENT_TYPE, FontHomeFragment.class.getSimpleName());
    }

    @Override
    public int getItemViewType(int position) {
        if (isFromFontHomeType() && position == getItemCount()-1) {
            return MORE_FONT_COMING_TYPE;
        }
        return 0;
    }

    public interface OnFontCardClickListener {
        void onFontCardClick(final FontModel fontModel);
    }
}
