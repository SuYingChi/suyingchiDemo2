package com.ihs.inputmethod.fonts.adapter;

import android.app.Activity;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacter;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacterManager;
import com.ihs.inputmethod.base.adapter.BaseListAdapter;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.fonts.homeui.FontModel;

/**
 * Created by jixiang on 18/1/23.
 */

public class FontAdapter extends BaseListAdapter<FontModel> {
    public static final String FROM_HOME = "fromHome";
    public static final String FROM_MY_DOWNLOAD = "fromMyDownload";

    private FontAdapter.OnFontCardClickListener onFontCardClickListener;
    private int currentSelectPosition = -1;
    private String from = FROM_HOME;

    public int getSpanSize(int position) {
        if (getItemViewType(position) == ITEM_TYPE.ITEM_TYPE_MORE.ordinal()) {
            return 2;
        }else {
            return 1;
        }
    }

    public enum ITEM_TYPE {
        ITEM_TYPE_HOME,
        ITEM_TYPE_MY,
        ITEM_TYPE_MORE
    }

    public FontAdapter(Activity activity, FontAdapter.OnFontCardClickListener onFontCardClickListener) {
        super(activity);
        this.onFontCardClickListener = onFontCardClickListener;
    }

    @Override
    public FontAdapter.FontCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == FontAdapter.ITEM_TYPE.ITEM_TYPE_MY.ordinal()) {
            currentSelectPosition = HSSpecialCharacterManager.getCurrentSpecialCharacterIndex();
            return new FontAdapter.MyFontViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_font_card, parent, false));
        } else {
            return new FontAdapter.FontHomeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_font_card, parent, false));
        }
    }

    public void setFrom(String from) {
        this.from = from;
    }

    private void fontSelected(final int position) {
        if (getItemViewType(position) == FontAdapter.ITEM_TYPE.ITEM_TYPE_MY.ordinal()) {
            if (currentSelectPosition != position) {
                if (currentSelectPosition != -1) {
                    notifyItemChanged(currentSelectPosition);
                }
                currentSelectPosition = position;
                notifyItemChanged(position);
            }
        }

        if (onFontCardClickListener != null) {
            onFontCardClickListener.onFontCardClick(dataList.get(position), position);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        if (dataList == null) {
            return;
        }

        FontAdapter.FontCardViewHolder holder = (FontCardViewHolder) viewHolder;

        if (getItemViewType(position) == FontAdapter.ITEM_TYPE.ITEM_TYPE_MORE.ordinal()) {
            holder.fontCardView.setVisibility(View.GONE);
            ((FontAdapter.FontHomeViewHolder) holder).moreFontHint.setVisibility(View.VISIBLE);
            return;
        }
        final FontModel fontModel = dataList.get(position);
        final HSSpecialCharacter hsSpecialCharacter = fontModel.getHsSpecialCharacter();
        holder.fontCardContent.setText(hsSpecialCharacter.example);

        if (getItemViewType(position) == FontAdapter.ITEM_TYPE.ITEM_TYPE_HOME.ordinal()) {
            ((FontAdapter.FontHomeViewHolder) holder).downloadIcon.setImageResource(R.drawable.ic_download_icon);
        } else if (getItemViewType(position) == FontAdapter.ITEM_TYPE.ITEM_TYPE_MY.ordinal()) {
            AppCompatRadioButton appCompatRadioButton = ((FontAdapter.MyFontViewHolder) holder).radioButton;
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
            return dataList.size() + 1;
        }
        return dataList.size();
    }

    private boolean isFromFontHomeType() {
        return FROM_HOME.equals(from);
    }

    @Override
    public int getItemViewType(int position) {
        if (isFromFontHomeType() && position == getItemCount() - 1) {
            return ITEM_TYPE.ITEM_TYPE_MORE.ordinal();
        }

        return isFromFontHomeType() ? ITEM_TYPE.ITEM_TYPE_HOME.ordinal() : ITEM_TYPE.ITEM_TYPE_MY.ordinal();
    }

    public interface OnFontCardClickListener {
        void onFontCardClick(final FontModel fontModel, int position);
    }

    public class FontCardViewHolder extends RecyclerView.ViewHolder {
        View fontCardView;
        TextView fontCardContent;

        public FontCardViewHolder(View itemView) {
            super(itemView);

            fontCardView = itemView.findViewById(R.id.font_card_view);
            fontCardContent = itemView.findViewById(R.id.font_content);
        }
    }

    public class MyFontViewHolder extends FontAdapter.FontCardViewHolder {
        AppCompatRadioButton radioButton;

        public MyFontViewHolder(View itemView) {
            super(itemView);
            radioButton = itemView.findViewById(R.id.font_radio_button);
        }
    }

    public class FontHomeViewHolder extends FontAdapter.FontCardViewHolder {
        ImageView downloadIcon;
        TextView moreFontHint;

        public FontHomeViewHolder(View itemView) {
            super(itemView);
            downloadIcon = itemView.findViewById(R.id.download_icon);
            moreFontHint = itemView.findViewById(R.id.more_font_coming);
        }
    }

}