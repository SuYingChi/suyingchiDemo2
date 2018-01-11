package com.ihs.inputmethod.uimodules.ui.common.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.model.Emoji;

import java.util.List;

/**
 * Created by wenbinduan on 2016/11/22.
 */

public final class HSEmojiViewAdapter extends RecyclerView.Adapter<HSEmojiViewAdapter.ViewHolder> implements View.OnClickListener,View.OnLongClickListener{

	public interface OnEmojiClickListener {
		void onEmojiClick(Emoji emoji);
	}

	public interface OnEmojiLongPressListener {
		void onEmojiLongPress(Emoji emoji,View emojiView,int parentViewHeight);
	}

	private final int childViewHeight;
	private final int childViewWidth;
	private final int emojiSize;
	private List<Emoji> emojiList;
	private final OnEmojiClickListener listener;


	private OnEmojiLongPressListener longPressListener;

	public HSEmojiViewAdapter(int childViewHeight, int childViewWidth, float scaleRatio, OnEmojiClickListener listener) {
		this.childViewHeight = childViewHeight;
		this.childViewWidth = childViewWidth;
		emojiSize= (int) (Math.min(childViewWidth,childViewHeight)*scaleRatio);
		this.listener=listener;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.common_emoji_view,parent,false));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		if(emojiList==null){
			return;
		}
		final Emoji emoji=emojiList.get(position);
		TextView textView=holder.tv;
		textView.setText(emoji.getLabel());
		if(!HSKeyboardThemeManager.getCurrentTheme().isDarkBg()){
			textView.setTextColor(Color.BLACK);
		}
		if(emoji.isText()){
			textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, emojiSize/1.5f);
		}else{
			textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, emojiSize);
		}
		textView.setHorizontallyScrolling(false);//must

		RecyclerView.LayoutParams lp= (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
		lp.width=childViewWidth*emoji.getColumn();
		lp.height=childViewHeight;
		if(emoji.isDivider()){
			lp.width=HSDisplayUtils.dip2px(20);
		}
		holder.itemView.setLayoutParams(lp);

		if(emoji.getLabel().trim().length()>0){
			holder.itemView.setClickable(true);
			textView.setTag(emoji);
			holder.itemView.setOnClickListener(this);
			textView.setSoundEffectsEnabled(false);
			holder.itemView.setTag(emoji);
		}else{
			textView.setTag(null);
			holder.itemView.setOnClickListener(null);
			holder.itemView.setTag(null);
			holder.itemView.setClickable(false);
		}
		if (emoji.supportSkin()) {

			FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams)holder.iv.getLayoutParams();
			flp.width= childViewWidth / 5;
			flp.height= childViewHeight / 5;
			holder.iv.setLayoutParams(flp);

			holder.iv.setVisibility(View.VISIBLE);
			holder.itemView.setOnLongClickListener(this);
		}else {
			holder.itemView.setOnLongClickListener(null);
			holder.iv.setVisibility(View.GONE);
		}
	}


	@Override
	public int getItemCount() {
		if(emojiList!=null){
			return emojiList.size();
		}
		return 0;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setData(List<Emoji> emojis) {
		this.emojiList=emojis;
		notifyDataSetChanged();
	}


	@Override
	public void onClick(View v) {
		final Object tag=v.getTag();
		if(tag instanceof Emoji && listener!=null){
			View textView = v.findViewById(R.id.emoji_tv);
			listener.onEmojiClick((Emoji) tag);
//			String unicode = ((Emoji) tag).getUnicodeStr();
//			Log.d("emoji",  ((Emoji) tag).getLabel() + " ---->unicode:"+unicode);
			Animation set=createClickAnimation(1.4f,80,80);
			textView.startAnimation(set);
		}
	}

	@Override
	public  boolean onLongClick(View v) {
		final Object tag= v.getTag();
		if(tag instanceof Emoji && longPressListener!=null){
			View textView = v.findViewById(R.id.emoji_tv);
			longPressListener.onEmojiLongPress((Emoji) tag,textView,this.childViewHeight);

			Animation set=createClickAnimation(1.4f,80,80);
//			String unicode = ((Emoji) tag).getUnicodeStr();
//			Log.d("emoji",  ((Emoji) tag).getLabel() + " ---->unicode:"+unicode);
			textView.startAnimation(set);
			return true;
		}

		return false;
	}


	public OnEmojiLongPressListener getLongPressListener() {
		return longPressListener;
	}

	public void setLongPressListener(OnEmojiLongPressListener longPressListener) {
		this.longPressListener = longPressListener;
	}

	private Animation createClickAnimation(final float scaleRation, final int upDuration, final int downDuration){

		final Animation scaleUp=new ScaleAnimation(
				1.0f,scaleRation,1.0f,scaleRation,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
		);
		final Animation scaleDown=new ScaleAnimation(
				1.0f,1/scaleRation,1.0f,1/scaleRation,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
		);
		final AnimationSet set=new AnimationSet(false);
		scaleUp.setDuration(upDuration);
		scaleUp.setFillAfter(true);
		scaleDown.setDuration(downDuration);
		scaleDown.setStartOffset(upDuration);
		scaleDown.setFillAfter(true);
		set.setDuration(upDuration+downDuration);

		set.addAnimation(scaleUp);
		set.addAnimation(scaleDown);

		return set;
	}

	class ViewHolder extends RecyclerView.ViewHolder{
		TextView tv;
		ImageView iv;
		public ViewHolder(View itemView) {
			super(itemView);
			tv = itemView.findViewById(R.id.emoji_tv);
			iv = itemView.findViewById(R.id.emoji_iv);
		}
	}

	public void selectedEmoji(Emoji emoji) {
		if (this.emojiList == null) {
			return;
		}
		int index = this.emojiList.indexOf(emoji);
		if (index != -1) {
			this.notifyItemChanged(index);
		}
	}
}
