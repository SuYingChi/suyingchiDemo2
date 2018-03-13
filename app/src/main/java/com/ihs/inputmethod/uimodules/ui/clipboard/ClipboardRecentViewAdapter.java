package com.ihs.inputmethod.uimodules.ui.clipboard;


import android.graphics.Color;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.uimodules.R;

import java.util.ArrayList;
import java.util.List;


public class ClipboardRecentViewAdapter extends RecyclerView.Adapter<ClipboardRecentViewAdapter.ViewHolder> {

    //集合不对外开放，只在数据库的操作完之后的回调来更改数据，防止外界饶过数据库更改了集合导致UI与数据库显示不一致
    private List<ClipboardRecentMessage> clipRecentData = new ArrayList<ClipboardRecentMessage>();
    private OnRecentItemPinClickedListener onRecentItemPinClickedListener;
    private final String TAG = ClipboardRecentViewAdapter.class.getSimpleName();


    ClipboardRecentViewAdapter(List<ClipboardRecentMessage> list, OnRecentItemPinClickedListener onRecentItemPinClickedListener) {
        clipRecentData.addAll(list);
        this.onRecentItemPinClickedListener = onRecentItemPinClickedListener;
        HSLog.d(TAG, "create ClipboardRecentViewAdapter , clipRecentData  is" + clipRecentData.toString());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.clipboard_recent_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ClipboardRecentMessage recentClipItemContent = clipRecentData.get(holder.getAdapterPosition());
        holder.clipContent.setText(recentClipItemContent.recentClipItemContent);
        holder.clipContent.setTextColor(HSKeyboardThemeManager.getCurrentTheme().getKeyTextColor(Color.WHITE));
        if (recentClipItemContent.isPined == 1) {
            holder.saveToPinsImageView.setImageDrawable(VectorDrawableCompat.create(HSApplication.getContext().getResources(), R.drawable.clipboard_save_to_pins_pined, null));
        } else if (recentClipItemContent.isPined == 0) {
            holder.saveToPinsImageView.setImageDrawable(VectorDrawableCompat.create(HSApplication.getContext().getResources(), R.drawable.clipboard_save_to_pins_no_pined, null));
        }
        holder.saveToPinsImageView.setClickable(true);
        holder.saveToPinsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecentItemPinClickedListener.onSaveRecentItemToPinsBtnClick(recentClipItemContent.recentClipItemContent, holder.getAdapterPosition());
                HSLog.d(TAG, "save recentItem content to pins  " + recentClipItemContent.recentClipItemContent);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HSInputMethod.inputText(recentClipItemContent.recentClipItemContent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return clipRecentData.size();
    }


    void deleteDataChangeAndNotifyDataSetChange(int recentItemPosition) {
        clipRecentData.remove(recentItemPosition);
        notifyItemRemoved(recentItemPosition);
        if (recentItemPosition != clipRecentData.size()) { // 如果移除的是最后一个，忽略
            //对于被删掉的元素的后边的view进行重新onBindViewHolder
            notifyItemRangeChanged(recentItemPosition, clipRecentData.size() - recentItemPosition);
        }

    }

    void moveRecentItemToTopAndNotifyDataSetChange(ClipboardRecentMessage clipboardRecentMessage) {
        int recentItemPosition  = clipRecentData.indexOf(clipboardRecentMessage);
        deleteDataChangeAndNotifyDataSetChange(recentItemPosition);
        insertDataAndNotifyDataSetChange(clipboardRecentMessage);

    }

    void itemChangedAndNotifyDataSetChange(ClipboardRecentMessage recentItem) {
        int previousPosition = clipRecentData.indexOf(new ClipboardRecentViewAdapter.ClipboardRecentMessage(recentItem.recentClipItemContent,1));
        clipRecentData.remove(previousPosition);
        clipRecentData.add(previousPosition,recentItem);
        HSLog.d(TAG,"clipRecentData.at "+"---position========="+clipRecentData.get(previousPosition));
        notifyItemChanged(previousPosition);
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView saveToPinsImageView;
        TextView clipContent;

        public ViewHolder(View view) {
            super(view);
            clipContent = (TextView) view.findViewById(R.id.clip_content);
            saveToPinsImageView = (ImageView) view.findViewById(R.id.save_to_pins);
        }
    }

    public interface OnRecentItemPinClickedListener {
        void onSaveRecentItemToPinsBtnClick(String itemRecentContent, int position);
    }

    void insertDataAndNotifyDataSetChange(ClipboardRecentMessage clipRecentMessage) {
        clipRecentData.add(0, clipRecentMessage);
        HSLog.d(TAG, "notifyDataSetChanged  recentAdapter,     current pinsDataList  is   " + clipRecentData.toString());
        notifyItemInserted(0);
    }

    //布尔值改为int值 不然不方便数据库操作
    public static class ClipboardRecentMessage {
        String recentClipItemContent;
        int isPined;

        ClipboardRecentMessage(String recentClipItemContent, int isPined) {
            this.recentClipItemContent = recentClipItemContent;
            this.isPined = isPined;
        }

        @Override
        public String toString() {
            return "  recentClipItemContent  =" + recentClipItemContent + "  isPined =" + isPined;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if ((obj == null) || (obj.getClass() != this.getClass()))
                return false;
            // object must be Test at this point
            ClipboardRecentMessage clipboardRecentMessage = (ClipboardRecentMessage) obj;
            return recentClipItemContent.equals(clipboardRecentMessage.recentClipItemContent) && isPined == clipboardRecentMessage.isPined;
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = result * 31 + recentClipItemContent.hashCode();
            result = result * 31 + isPined;
            return result;
        }
    }

}