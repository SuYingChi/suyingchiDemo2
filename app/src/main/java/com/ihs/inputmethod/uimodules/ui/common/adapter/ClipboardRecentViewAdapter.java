package com.ihs.inputmethod.uimodules.ui.common.adapter;

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

public class ClipboardRecentViewAdapter extends RecyclerView.Adapter<ClipboardRecentViewAdapter.ViewHolder> {

    public static final String clipSpName = "Clip_Content";
    private List<String> recentClipContent = new ArrayList<String>();
    private SaveRecentItemToPins saveToPins;
    //注意处理完list再传进来
    public ClipboardRecentViewAdapter(List<String> list, SaveRecentItemToPins saveToPins){
        recentClipContent = list;
        this.saveToPins = saveToPins;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.recentclip_item_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String RecentClipItemContent = recentClipContent.get(position);
        holder.clipContent.setText(RecentClipItemContent);
        holder.saveToPins.setImageDrawable(HSApplication.getContext().getResources().getDrawable(R.drawable.launch_page_icon));
        holder.saveToPins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //添加到pins页面
                saveToPins.saveToPins(RecentClipItemContent);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HSInputMethod.inputText(RecentClipItemContent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return recentClipContent.size();
    }

    public void deleteAndFresh(String pinsContentItem) {
        if(recentClipContent.contains(pinsContentItem)) {
            recentClipContent.remove(pinsContentItem);
        }
        notifyDataSetChanged();
    }


    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView saveToPins;
        TextView clipContent;

        public ViewHolder(View view) {
            super(view);
            clipContent = (TextView) view.findViewById(R.id.clip_content);
            saveToPins = (ImageView) view.findViewById(R.id.save_to_pins);
        }
    }
    public interface SaveRecentItemToPins {
       void saveToPins(String itemPinsContent);
    }
}
