package com.keyboard.inputmethod.panels.gif.ui.adapter;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.ihs.app.framework.HSApplication;
import com.keyboard.inputmethod.panels.gif.control.DownloadManager;
import com.keyboard.inputmethod.panels.gif.model.GifItem;
import com.keyboard.inputmethod.panels.gif.net.download.DownloadTask;
import com.keyboard.inputmethod.panels.gif.net.download.GifDownloadTask;
import com.keyboard.inputmethod.panels.gif.ui.view.CustomProgressDrawable;
import com.keyboard.inputmethod.panels.gif.ui.view.GifHorizontalScrollView;
import com.keyboard.inputmethod.panels.gif.ui.view.GifPanelView;
import com.keyboard.inputmethod.panels.gif.ui.view.GifView;
import com.keyboard.rainbow.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class GifHorizontalScrollViewAdapter extends RecyclerView.Adapter<GifHorizontalScrollViewAdapter.ViewHolder> implements GifDownloadTask.Callback {

    private final LayoutInflater mInflater;
    private List<GifItem> mData = new ArrayList<>();
    private final int mViewWidth;
    private final int mViewHeight;

	private boolean isLoadingMore=false;
	private GifPanelView listener;


	public GifHorizontalScrollViewAdapter(final GifPanelView listener, final int width, final int height) {
        mInflater = LayoutInflater.from(HSApplication.getContext());
        mViewWidth = width;
        mViewHeight = height;
		this.listener=listener;
    }

    public void addData(final List<GifItem> data) {
	    isLoadingMore=false;
	    if(data==null){
		    return;
	    }
        mData.addAll(data);
    }

	public void removeItem(final GifItem data) {
		isLoadingMore=false;
		final int pos=mData.indexOf(data);
		if(pos>-1&&pos<mData.size()){
			mData.remove(pos);
			notifyItemRemoved(pos);
		}
	}

    public void clear() {
	    isLoadingMore=false;
        mData.clear();
	    notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final GifView gifView = (GifView) mInflater.inflate(R.layout.gif_view, parent,false);
        gifView.setLayoutParams(new GifHorizontalScrollView.LayoutParams(mViewWidth, mViewHeight));
	    return new ViewHolder(gifView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder,final int position) {

    }

	@Override
	public void onViewAttachedToWindow(ViewHolder holder) {
		final int position  = (int) holder.getItemId();
		onMyBindViewHolder(holder, position);
		super.onViewAttachedToWindow(holder);
	}

	private void onMyBindViewHolder(ViewHolder holder, int position) {
		if (mData.isEmpty()) return;

		final GifItem data = mData.get(position);

		final GifView gifView = (GifView)holder.itemView;
		gifView.setGifEnabled(false);
		gifView.setOnImageEventInfo(data,listener);

		// part: tag
		if (data.isTag()) {
			holder.tag.setVisibility(View.VISIBLE);
			final String tag=data.id.toUpperCase();
			holder.tag.setText(tag);
			// clickable
		} else {
			holder.tag.setVisibility(View.GONE);
		}

		// bind view with mediaId
		holder.view.setTag(data.id);

		final DownloadTask task = DownloadManager.getInstance().loadImageTask(data.id, data.getGifUrl(),holder.itemView,this);
		if (task != null&&!task.isDone()) {
			holder.progress.getDrawable().setLevel(0);
			holder.progress.setVisibility(View.VISIBLE);
			holder.view.setVisibility(View.INVISIBLE);
		}
		gifView.setDownloadTask(task);
		// load more
		if (position == getItemCount() - 1&&!isLoadingMore) {
			isLoadingMore=true;
			listener.onLastImageShowing();
		}
	}

	@Override
    public void onDownloadProgress(final File file, final View view, final float percent) {
        GifView gv = (GifView)view;

        View sdv = gv.findViewById(R.id.view);
        String mediaId = (String)(sdv.getTag());
        if(file!=null&&mediaId!=null){
	        if (mediaId.equals(file.getName().split("\\.")[0])) {
		        final ImageView progressBar = (ImageView) view.findViewById(R.id.progress);
		        progressBar.getDrawable().setLevel((int) (percent * 100));
	        }
        }
    }

    @Override
    public void onDownloadSucceeded(final File file,final View view) {
        final GifView gv = (GifView)view;
	    final View sdv = gv.findViewById(R.id.view);
        final String mediaId = (String)(sdv.getTag());
        if(file!=null&&file.exists()&&mediaId!=null){
	        if (mediaId.equals(file.getName().split("\\.")[0])) {
		        // reset progress
		        final ImageView progress = (ImageView) gv.findViewById(R.id.progress);
		        progress.getDrawable().setLevel(0);
		        progress.setVisibility(View.GONE);
		        gv.setGifEnabled(true);
		        loadGif((SimpleDraweeView) sdv, Uri.fromFile(file));
	        }
        }
    }

    @Override
    public void onDownloadFailed(final View view) {
        final ImageView progress = (ImageView) view.findViewById(R.id.progress);
        progress.getDrawable().setLevel(0);
        progress.setVisibility(View.GONE);
        GifView gv = (GifView)view;
        DownloadTask task=gv.getTask();
        if(task!=null){
            task.setDone(false);
            task.setRunning(false);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    class ViewHolder extends RecyclerView.ViewHolder{
        SimpleDraweeView view;
        ImageView progress;
        TextView tag;

        public ViewHolder(View itemView) {
            super(itemView);
            // view
            view = (SimpleDraweeView) itemView.findViewById(R.id.view);
            final ViewGroup.LayoutParams lp = view.getLayoutParams();
            lp.width  = mViewWidth;
            lp.height = mViewHeight;
            view.setLayoutParams(lp);

            // progress
            progress = (ImageView) itemView.findViewById(R.id.progress);
            progress.setImageDrawable(new CustomProgressDrawable());

            // tag
            tag = (TextView) itemView.findViewById(R.id.tag);

	        final View favorite = itemView.findViewById(R.id.favorite);
	        final ViewGroup.LayoutParams lpp = favorite.getLayoutParams();
	        lpp.width  = mViewWidth;
	        lpp.height = mViewHeight;
	        favorite.setLayoutParams(lpp);
        }
    }

    private void loadGif(final SimpleDraweeView view, final Uri uri) {

        final DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setAutoPlayAnimations(true)
                .setOldController(view.getController())
                .build();

        final GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(HSApplication.getContext().getResources())
                .setActualImageScaleType(ScalingUtils.ScaleType.FIT_XY)
                .setFadeDuration(0)
                .build();

        view.setHierarchy(hierarchy);
        view.setController(draweeController);
	    if(view.getController()!=null)
            view.getController().onAttach();
        view.setVisibility(View.VISIBLE);
    }

}