package com.ihs.inputmethod.uimodules.ui.fonts.homeui;

import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacter;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacterManager;
import com.ihs.inputmethod.uimodules.R;

import java.util.List;

/**
 * Created by guonan.lv on 17/8/14.
 */

public class FontCardAdapter extends RecyclerView.Adapter<FontCardAdapter.FontCardViewHolder> {

    private List<FontModel> fontModelList;
    private OnFontCardClickListener onFontCardClickListener;
    private int currentSelectPosition = -1;
    private String fromFragmentType;

    public enum ITEM_TYPE {
        ITEM_TYPE_HOME,
        ITEM_TYPE_MY,
        ITEM_TYPE_MORE
    }

    public FontCardAdapter(List<FontModel> fontModels, OnFontCardClickListener onFontCardClickListener) {
        this.onFontCardClickListener = onFontCardClickListener;
        this.fontModelList = fontModels;
    }

    @Override
    public FontCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE.ITEM_TYPE_MY.ordinal()) {
            currentSelectPosition = HSSpecialCharacterManager.getCurrentSpecialCharacterIndex();
            return new MyFontViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_font_card, parent, false));
        } else {
            return new FontHomeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_font_card, parent, false));
        }
    }

    private void fontSelected(final int position) {
        if (getItemViewType(position) == ITEM_TYPE.ITEM_TYPE_MY.ordinal()) {
            if (currentSelectPosition != position) {
                if (currentSelectPosition != -1) {
                    notifyItemChanged(currentSelectPosition);
                }
                currentSelectPosition = position;
                notifyItemChanged(position);
            }
        }

        if (onFontCardClickListener != null) {
            onFontCardClickListener.onFontCardClick(position);
        }
    }

    public void setFragmentType(String type) {
        fromFragmentType = type;
    }

    @Override
    public void onBindViewHolder(final FontCardViewHolder holder, final int position) {
        if (fontModelList == null) {
            return;
        }
        if (getItemViewType(position) == ITEM_TYPE.ITEM_TYPE_MORE.ordinal()) {
            holder.fontCardView.setVisibility(View.GONE);
            ((FontHomeViewHolder) holder).moreFontHint.setVisibility(View.VISIBLE);
            return;
        }
        final FontModel fontModel = fontModelList.get(position);
        final HSSpecialCharacter hsSpecialCharacter = fontModel.getHsSpecialCharacter();
        holder.fontCardContent.setText(hsSpecialCharacter.example);

        if (getItemViewType(position) == ITEM_TYPE.ITEM_TYPE_HOME.ordinal()) {
            ((FontHomeViewHolder) holder).downloadIcon.setVisibility(View.VISIBLE);
            ((FontHomeViewHolder) holder).downloadIcon.setImageResource(R.drawable.ic_download_icon);
        } else if (getItemViewType(position) == ITEM_TYPE.ITEM_TYPE_MY.ordinal()) {
            AppCompatRadioButton appCompatRadioButton = ((MyFontViewHolder) holder).radioButton;
            appCompatRadioButton.setVisibility(View.VISIBLE);
            appCompatRadioButton.setChecked(position == currentSelectPosition);
            appCompatRadioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fontSelected(position);
                }
            });
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fontSelected(position);
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
            return fontModelList.size() + 1;
        }
        return fontModelList.size();
    }

    private boolean isFromFontHomeType() {
        return TextUtils.equals(fromFragmentType, FontHomeFragment.class.getSimpleName());
    }

    @Override
    public int getItemViewType(int position) {
        if (isFromFontHomeType() && position == getItemCount()-1) {
            return ITEM_TYPE.ITEM_TYPE_MORE.ordinal();
        }

        return isFromFontHomeType() ? ITEM_TYPE.ITEM_TYPE_HOME.ordinal() : ITEM_TYPE.ITEM_TYPE_MY.ordinal();
    }

    public interface OnFontCardClickListener {
        void onFontCardClick(final int position);
    }

    public class FontCardViewHolder extends RecyclerView.ViewHolder {
        View fontCardView;
        TextView fontCardContent;

        public FontCardViewHolder(View itemView) {
            super(itemView);

            fontCardView = itemView.findViewById(R.id.font_card_view);
            fontCardContent = (TextView) itemView.findViewById(R.id.font_content);
        }
    }

    public class MyFontViewHolder extends FontCardViewHolder {

        AppCompatRadioButton radioButton;

        public MyFontViewHolder(View itemView) {
            super(itemView);
            radioButton = (AppCompatRadioButton) itemView.findViewById(R.id.font_radio_button);
        }
    }

    public class FontHomeViewHolder extends FontCardViewHolder {
        ImageView downloadIcon;
        TextView moreFontHint;

        public FontHomeViewHolder(View itemView) {
            super(itemView);
            downloadIcon = (ImageView) itemView.findViewById(R.id.download_icon);
            moreFontHint = (TextView) itemView.findViewById(R.id.more_font_coming);
        }
    }

}
