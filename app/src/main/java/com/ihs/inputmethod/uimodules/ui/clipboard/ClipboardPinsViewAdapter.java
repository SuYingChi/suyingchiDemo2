package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.uimodules.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yingchi.su on 2018/1/25.
 */

public class ClipboardPinsViewAdapter extends RecyclerView.Adapter<ClipboardPinsViewAdapter.ViewHolder> {


    private DeleteFromPinsToRecentListener deleteFromPinsToRecentListener;
    private List<String> pinsDataList = new ArrayList<String>();
    private int EMPTY_DATA = -1;

    public ClipboardPinsViewAdapter(List<String> list, DeleteFromPinsToRecentListener deleteFromPinsToRecentListener){
        this.deleteFromPinsToRecentListener = deleteFromPinsToRecentListener;
        this.pinsDataList.addAll(list);

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.pins_clip_item_layout,parent,false));

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.img.setImageDrawable(HSApplication.getContext().getResources().getDrawable(R.drawable.acb_phone_sms_close));
        String pinsContent = pinsDataList.get(position);
        holder.tv.setText(pinsContent);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HSInputMethod.inputText(pinsContent);
            }
        });
        holder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFromPinsToRecentListener.deletePinsItem(pinsContent,position);
                pinsDataList.remove(pinsContent);
                notifyDataSetChanged();
            }
        });

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public void dataChangeAndRefresh(List<String> clipPinsData) {
        pinsDataList.clear();
        pinsDataList.addAll(clipPinsData);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private  TextView tv;
        private  ImageView img;

        public ViewHolder(View itemView) {
            super(itemView);
            tv = (TextView)itemView.findViewById(R.id.pins_content);
            img = (ImageView)itemView.findViewById(R.id.pins_delete);
        }
    }


    public interface DeleteFromPinsToRecentListener {
        void deletePinsItem(String pinsContentItem,int position);
    }
}
