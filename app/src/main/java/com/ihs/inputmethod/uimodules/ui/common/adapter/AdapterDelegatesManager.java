package com.ihs.inputmethod.uimodules.ui.common.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * Created by wenbinduan on 2016/12/21.
 */

public final class AdapterDelegatesManager<T> {

	private static final int FALLBACK_DELEGATE_VIEW_TYPE = Integer.MAX_VALUE - 1;

	private SparseArrayCompat<AdapterDelegate<T>> delegates = new SparseArrayCompat<>();
	private AdapterDelegate<T> fallbackDelegate;


	public AdapterDelegatesManager<T> addDelegate(@NonNull AdapterDelegate<T> delegate) {
		int viewType = delegates.size();
		while (delegates.get(viewType) != null) {
			viewType++;
			if (viewType == FALLBACK_DELEGATE_VIEW_TYPE) {
				throw new IllegalArgumentException(
						"Oops, we are very close to Integer.MAX_VALUE. It seems that there are no more free and unused view type integers left to add another AdapterDelegate.");
			}
		}
		return addDelegate(viewType, false, delegate);
	}


	public AdapterDelegatesManager<T> addDelegate(int viewType,@NonNull AdapterDelegate<T> delegate) {
		return addDelegate(viewType, false, delegate);
	}


	public AdapterDelegatesManager<T> addDelegate(int viewType, boolean allowReplacingDelegate,
	                                              @NonNull AdapterDelegate<T> delegate) {

		if (viewType == FALLBACK_DELEGATE_VIEW_TYPE) {
			throw new IllegalArgumentException("The view type = "
					+ FALLBACK_DELEGATE_VIEW_TYPE
					+ " is reserved for fallback adapter delegate (see setFallbackDelegate() ). Please use another view type.");
		}

		if (!allowReplacingDelegate && delegates.get(viewType) != null) {
			throw new IllegalArgumentException(
					"An AdapterDelegate is already registered for the viewType = "
							+ viewType
							+ ". Already registered AdapterDelegate is "
							+ delegates.get(viewType));
		}

		delegates.put(viewType, delegate);

		return this;
	}


	public AdapterDelegatesManager<T> removeDelegate(@NonNull AdapterDelegate<T> delegate) {

		int indexToRemove = delegates.indexOfValue(delegate);

		if (indexToRemove >= 0) {
			delegates.removeAt(indexToRemove);
		}
		return this;
	}

	public AdapterDelegatesManager<T> removeDelegate(int viewType) {
		delegates.remove(viewType);
		return this;
	}


	public int getItemViewType(@NonNull T items, int position) {

		int delegatesCount = delegates.size();
		for (int i = 0; i < delegatesCount; i++) {
			AdapterDelegate<T> delegate = delegates.valueAt(i);
			if (delegate.isForViewType(items, position)) {
				return delegates.keyAt(i);
			}
		}

		if (fallbackDelegate != null) {
			return FALLBACK_DELEGATE_VIEW_TYPE;
		}

		throw new NullPointerException(
				"No AdapterDelegate added that matches position=" + position + " in data source");
	}


	@NonNull
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		AdapterDelegate<T> delegate = getDelegateForViewType(viewType);
		if (delegate == null) {
			throw new NullPointerException("No AdapterDelegate added for ViewType " + viewType);
		}

		return delegate.onCreateViewHolder(parent);
	}

	public void onBindViewHolder(@NonNull T items, int position, @NonNull RecyclerView.ViewHolder viewHolder) {
		AdapterDelegate<T> delegate = getDelegateForViewType(viewHolder.getItemViewType());
		if (delegate == null) {
			throw new NullPointerException("No delegate found for item at position = "
					+ position
					+ " for viewType = "
					+ viewHolder.getItemViewType());
		}
		delegate.onBindViewHolder(items, position, viewHolder);
	}


	public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
		AdapterDelegate<T> delegate = getDelegateForViewType(viewHolder.getItemViewType());
		if (delegate == null) {
			throw new NullPointerException("No delegate found for "
					+ viewHolder
					+ " for item at position = "
					+ viewHolder.getAdapterPosition()
					+ " for viewType = "
					+ viewHolder.getItemViewType());
		}
		delegate.onViewRecycled(viewHolder);
	}

	public boolean onFailedToRecycleView(@NonNull RecyclerView.ViewHolder viewHolder) {
		AdapterDelegate<T> delegate = getDelegateForViewType(viewHolder.getItemViewType());
		if (delegate == null) {
			throw new NullPointerException("No delegate found for "
					+ viewHolder
					+ " for item at position = "
					+ viewHolder.getAdapterPosition()
					+ " for viewType = "
					+ viewHolder.getItemViewType());
		}
		return delegate.onFailedToRecycleView(viewHolder);
	}


	public void onViewAttachedToWindow(RecyclerView.ViewHolder viewHolder) {
		AdapterDelegate<T> delegate = getDelegateForViewType(viewHolder.getItemViewType());
		if (delegate == null) {
			throw new NullPointerException("No delegate found for "
					+ viewHolder
					+ " for item at position = "
					+ viewHolder.getAdapterPosition()
					+ " for viewType = "
					+ viewHolder.getItemViewType());
		}
		delegate.onViewAttachedToWindow(viewHolder);
	}

	public void onViewDetachedFromWindow(RecyclerView.ViewHolder viewHolder) {
		AdapterDelegate<T> delegate = getDelegateForViewType(viewHolder.getItemViewType());
		if (delegate == null) {
			throw new NullPointerException("No delegate found for "
					+ viewHolder
					+ " for item at position = "
					+ viewHolder.getAdapterPosition()
					+ " for viewType = "
					+ viewHolder.getItemViewType());
		}
		delegate.onViewDetachedFromWindow(viewHolder);
	}

	public AdapterDelegatesManager<T> setFallbackDelegate(
			@Nullable AdapterDelegate<T> fallbackDelegate) {
		this.fallbackDelegate = fallbackDelegate;
		return this;
	}

	public int getViewType(@NonNull AdapterDelegate<T> delegate) {

		int index = delegates.indexOfValue(delegate);
		if (index == -1) {
			return -1;
		}
		return delegates.keyAt(index);
	}

	@Nullable
	public AdapterDelegate<T> getDelegateForViewType(int viewType) {
		AdapterDelegate<T> delegate = delegates.get(viewType);
		if (delegate == null) {
			if (fallbackDelegate == null) {
				return null;
			} else {
				return fallbackDelegate;
			}
		}

		return delegate;
	}

	@Nullable
	public AdapterDelegate<T> getFallbackDelegate() {
		return fallbackDelegate;
	}

	public int getSpanSize(T items,int position) {

		AdapterDelegate<T> delegate=null;
		int delegatesCount = delegates.size();
		for (int i = 0; i < delegatesCount; i++) {
			AdapterDelegate<T> temp = delegates.valueAt(i);
			if (temp.isForViewType(items, position)) {
				delegate=temp;
				break;
			}
		}

		if (delegate == null && fallbackDelegate != null) {
			delegate= fallbackDelegate;
		}

		if (delegate == null) {
			throw new NullPointerException("No AdapterDelegate added for item at position " + position);
		}

		return delegate.getSpanSize(items,position);
	}

}

