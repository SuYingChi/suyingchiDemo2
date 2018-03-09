package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
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


public class ClipboardPinsViewAdapter extends RecyclerView.Adapter<ClipboardPinsViewAdapter.ViewHolder> {


    private OnPinItemDeletedClickListener onPinItemDeletedClickListener;
    //集合不对外开放，只在数据库的操作完之后的回调来更改数据，防止外界饶过数据库更改了集合导致UI与数据库显示不一致
    private List<String> pinsDataList = new ArrayList<String>();
    private final String TAG = ClipboardPinsViewAdapter.class.getSimpleName();

    ClipboardPinsViewAdapter(List<String> list, OnPinItemDeletedClickListener onPinItemDeletedClickListener) {
        this.onPinItemDeletedClickListener = onPinItemDeletedClickListener;
        this.pinsDataList.addAll(list);
        HSLog.d(TAG, "create ClipboardPinsViewAdapter , pinsDataList  is " + pinsDataList.toString());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.clipboard_pins_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String pinsContent = pinsDataList.get(holder.getAdapterPosition());
        holder.tv.setText(pinsContent);
        holder.tv.setTextColor(HSKeyboardThemeManager.getCurrentTheme().getKeyTextColor(Color.WHITE));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HSInputMethod.inputText(pinsContent);
            }
        });
        holder.img.setImageDrawable(VectorDrawableCompat.create(HSApplication.getContext().getResources(), R.drawable.clipboard_delete_pin_item, null));
        holder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPinItemDeletedClickListener.onDeletePinsItem(pinsContent, holder.getAdapterPosition());
                HSLog.d(TAG, "  delete pins item  " + pinsContent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return pinsDataList.size();
    }


    void insertDataAndNotifyDataSetChange(String clipPinsData) {

        pinsDataList.add(0, clipPinsData);
        notifyItemInserted(0);
    }

    void movePinsItemToTopAndNotifyDataSetChange(String clipPinsItem) {
        int  previousPinsPosition = pinsDataList.indexOf(clipPinsItem);
        deleteDataAndNotifyDataSetChange(previousPinsPosition);
        insertDataAndNotifyDataSetChange(clipPinsItem);
    }

    void deleteDataAndNotifyDataSetChange(int pinItemPosition) {
        pinsDataList.remove(pinItemPosition);
        notifyItemRemoved(pinItemPosition);
        if (pinItemPosition != pinsDataList.size()) { // 如果移除的是最后一个，忽略
            //对于被删掉的元素的后边的view进行重新onBindViewHolder
            notifyItemRangeChanged(pinItemPosition, pinsDataList.size() - pinItemPosition);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv;
        private ImageView img;

        public ViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.pins_content);
            img = (ImageView) itemView.findViewById(R.id.pins_delete);
        }
    }


    public interface OnPinItemDeletedClickListener {
        void onDeletePinsItem(String pinsContentItem, int position);
    }
}