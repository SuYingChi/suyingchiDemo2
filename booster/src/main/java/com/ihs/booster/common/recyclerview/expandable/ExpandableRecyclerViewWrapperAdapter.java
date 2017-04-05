
package com.ihs.booster.common.recyclerview.expandable;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.ViewGroup;

import com.ihs.booster.common.recyclerview.utils.BaseWrapperAdapter;
import com.ihs.booster.common.recyclerview.utils.WrapperAdapterUtils;

import java.util.List;

class ExpandableRecyclerViewWrapperAdapter
        extends BaseWrapperAdapter<ViewHolder> {


    // NOTE: Make accessible with short name
    private interface Constants extends ExpandableItemConstants {
    }

    private static final int VIEW_TYPE_FLAG_IS_GROUP = ExpandableAdapterHelper.VIEW_TYPE_FLAG_IS_GROUP;

    private static final int STATE_FLAG_INITIAL_VALUE = -1;

    private ExpandableItemAdapter mExpandableItemAdapter;
    private RecyclerViewExpandableItemManager mExpandableListManager;
    private ExpandablePositionTranslator mPositionTranslator;

    private RecyclerViewExpandableItemManager.OnGroupExpandListener mOnGroupExpandListener;
    private RecyclerViewExpandableItemManager.OnGroupCollapseListener mOnGroupCollapseListener;

    public ExpandableRecyclerViewWrapperAdapter(RecyclerViewExpandableItemManager manager, RecyclerView.Adapter<RecyclerView.ViewHolder> adapter, int[] expandedItemsSavedState) {
        super(adapter);

        mExpandableItemAdapter = getExpandableItemAdapter(adapter);
        if (mExpandableItemAdapter == null) {
            throw new IllegalArgumentException("adapter does not implement RecyclerViewExpandableListManager");
        }

        if (manager == null) {
            throw new IllegalArgumentException("manager cannot be null");
        }

        mExpandableListManager = manager;

        mPositionTranslator = new ExpandablePositionTranslator();
        mPositionTranslator.build(mExpandableItemAdapter, false);

        if (expandedItemsSavedState != null) {
            // NOTE: do not call hook routines and listener methods
            mPositionTranslator.restoreExpandedGroupItems(expandedItemsSavedState, null, null, null);
        }
    }

    @Override
    protected void onRelease() {
        super.onRelease();

        mExpandableItemAdapter = null;
        mExpandableListManager = null;
        mOnGroupExpandListener = null;
        mOnGroupCollapseListener = null;
    }

    @Override
    public int getItemCount() {
        return mPositionTranslator.getItemCount();
    }

    @Override
    public long getItemId(int position) {
        if (mExpandableItemAdapter == null) {
            return RecyclerView.NO_ID;
        }

        final long expandablePosition = mPositionTranslator.getExpandablePosition(position);
        final int groupPosition = ExpandableAdapterHelper.getPackedPositionGroup(expandablePosition);
        final int childPosition = ExpandableAdapterHelper.getPackedPositionChild(expandablePosition);

        if (childPosition == RecyclerView.NO_POSITION) {
            final long groupId = mExpandableItemAdapter.getGroupId(groupPosition);
            return ExpandableAdapterHelper.getCombinedGroupId(groupId);
        } else {
            final long groupId = mExpandableItemAdapter.getGroupId(groupPosition);
            final long childId = mExpandableItemAdapter.getChildId(groupPosition, childPosition);
            return ExpandableAdapterHelper.getCombinedChildId(groupId, childId);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mExpandableItemAdapter == null) {
            return 0;
        }

        final long expandablePosition = mPositionTranslator.getExpandablePosition(position);
        final int groupPosition = ExpandableAdapterHelper.getPackedPositionGroup(expandablePosition);
        final int childPosition = ExpandableAdapterHelper.getPackedPositionChild(expandablePosition);

        final int type;

        if (childPosition == RecyclerView.NO_POSITION) {
            type = mExpandableItemAdapter.getGroupItemViewType(groupPosition);
        } else {
            type = mExpandableItemAdapter.getChildItemViewType(groupPosition, childPosition);
        }

        if ((type & VIEW_TYPE_FLAG_IS_GROUP) != 0) {
            throw new IllegalStateException("Illegal view type (type = " + Integer.toHexString(type) + ")");
        }

        return (childPosition == RecyclerView.NO_POSITION) ? (type | VIEW_TYPE_FLAG_IS_GROUP) : (type);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mExpandableItemAdapter == null) {
            return null;
        }

        final int maskedViewType = (viewType & (~VIEW_TYPE_FLAG_IS_GROUP));

        final RecyclerView.ViewHolder holder;

        if ((viewType & VIEW_TYPE_FLAG_IS_GROUP) != 0) {
            holder = mExpandableItemAdapter.onCreateGroupViewHolder(parent, maskedViewType);
        } else {
            holder = mExpandableItemAdapter.onCreateChildViewHolder(parent, maskedViewType);
        }

        if (holder instanceof ExpandableItemViewHolder) {
            ((ExpandableItemViewHolder) holder).setExpandStateFlags(STATE_FLAG_INITIAL_VALUE);
        }

        return holder;

    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
        if (mExpandableItemAdapter == null) {
            return;
        }

        final long expandablePosition = mPositionTranslator.getExpandablePosition(position);
        final int groupPosition = ExpandableAdapterHelper.getPackedPositionGroup(expandablePosition);
        final int childPosition = ExpandableAdapterHelper.getPackedPositionChild(expandablePosition);
        final int viewType = (holder.getItemViewType() & (~VIEW_TYPE_FLAG_IS_GROUP));

        // update flags
        int flags = 0;

        if (childPosition == RecyclerView.NO_POSITION) {
            flags |= Constants.STATE_FLAG_IS_GROUP;
        } else {
            flags |= Constants.STATE_FLAG_IS_CHILD;
        }

        if (mPositionTranslator.isGroupExpanded(groupPosition)) {
            flags |= Constants.STATE_FLAG_IS_EXPANDED;
        }

        safeUpdateExpandStateFlags(holder, flags);

//        correctItemDragStateFlags(holder, groupPosition, childPosition);

        if (childPosition == RecyclerView.NO_POSITION) {
            mExpandableItemAdapter.onBindGroupViewHolder(holder, groupPosition, viewType);
        } else {
            mExpandableItemAdapter.onBindChildViewHolder(holder, groupPosition, childPosition, viewType);
        }
    }

    private void rebuildPositionTranslator() {
        if (mPositionTranslator != null) {
            int[] savedState = mPositionTranslator.getSavedStateArray();
            mPositionTranslator.build(mExpandableItemAdapter, false);

            // NOTE: do not call hook routines and listener methods
            mPositionTranslator.restoreExpandedGroupItems(savedState, null, null, null);
        }
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        if (holder instanceof ExpandableItemViewHolder) {
            ((ExpandableItemViewHolder) holder).setExpandStateFlags(STATE_FLAG_INITIAL_VALUE);
        }

        super.onViewRecycled(holder);
    }

    @Override
    protected void onHandleWrappedAdapterChanged() {
        rebuildPositionTranslator();
        super.onHandleWrappedAdapterChanged();
    }

    @Override
    protected void onHandleWrappedAdapterItemRangeChanged(int positionStart, int itemCount) {
        super.onHandleWrappedAdapterItemRangeChanged(positionStart, itemCount);
    }

    @Override
    protected void onHandleWrappedAdapterItemRangeInserted(int positionStart, int itemCount) {
        rebuildPositionTranslator();
        super.onHandleWrappedAdapterItemRangeInserted(positionStart, itemCount);
    }

    @Override
    protected void onHandleWrappedAdapterItemRangeRemoved(int positionStart, int itemCount) {
        if (itemCount == 1) {
            final long expandablePosition = mPositionTranslator.getExpandablePosition(positionStart);
            final int groupPosition = ExpandableAdapterHelper.getPackedPositionGroup(expandablePosition);
            final int childPosition = ExpandableAdapterHelper.getPackedPositionChild(expandablePosition);

            if (childPosition == RecyclerView.NO_POSITION) {
                mPositionTranslator.removeGroupItem(groupPosition);
            } else {
                mPositionTranslator.removeChildItem(groupPosition, childPosition);
            }
        } else {
            rebuildPositionTranslator();
        }

        super.onHandleWrappedAdapterItemRangeRemoved(positionStart, itemCount);
    }

    @Override
    protected void onHandleWrappedAdapterRangeMoved(int fromPosition, int toPosition, int itemCount) {
        rebuildPositionTranslator();
        super.onHandleWrappedAdapterRangeMoved(fromPosition, toPosition, itemCount);
    }


    // NOTE: This method is called from RecyclerViewExpandableItemManager

    @SuppressWarnings("unchecked")
    boolean onTapItem(RecyclerView.ViewHolder holder, int position, int x, int y) {
        if (mExpandableItemAdapter == null) {
            return false;
        }

        final int flatPosition = position;
        final long expandablePosition = mPositionTranslator.getExpandablePosition(flatPosition);
        final int groupPosition = ExpandableAdapterHelper.getPackedPositionGroup(expandablePosition);
        final int childPosition = ExpandableAdapterHelper.getPackedPositionChild(expandablePosition);

        if (childPosition != RecyclerView.NO_POSITION) {
            return false;
        }

        final boolean expand = !(mPositionTranslator.isGroupExpanded(groupPosition));

        boolean result = mExpandableItemAdapter.onCheckCanExpandOrCollapseGroup(holder, groupPosition, x, y, expand);

        if (!result) {
            return false;
        }

        if (expand) {
            expandGroup(groupPosition, true);
        } else {
            collapseGroup(groupPosition, true);
        }

        return true;
    }

    void expandAll() {
        if (!mPositionTranslator.isAllExpanded()) {
            mPositionTranslator.build(mExpandableItemAdapter, true);
            notifyDataSetChanged();
        }
    }

    void collapseAll() {
        if (!mPositionTranslator.isAllCollapsed()) {
            mPositionTranslator.build(mExpandableItemAdapter, false);
            notifyDataSetChanged();
        }
    }

    boolean collapseGroup(int groupPosition, boolean fromUser) {
        if (!mPositionTranslator.isGroupExpanded(groupPosition)) {
            return false;
        }

        // call hook method
        if (!mExpandableItemAdapter.onHookGroupCollapse(groupPosition, fromUser)) {
            return false;
        }

        if (mPositionTranslator.collapseGroup(groupPosition)) {
            final long packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPosition);
            final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);
            final int childCount = mPositionTranslator.getChildCount(groupPosition);

            notifyItemRangeRemoved(flatPosition + 1, childCount);
        }


        {
            final long packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPosition);
            final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);

            notifyItemChanged(flatPosition);
        }

        // raise onGroupCollapse() event
        if (mOnGroupCollapseListener != null) {
            mOnGroupCollapseListener.onGroupCollapse(groupPosition, fromUser);
        }

        return true;
    }

    boolean expandGroup(int groupPosition, boolean fromUser) {
        if (mPositionTranslator.isGroupExpanded(groupPosition)) {
            return false;
        }

        // call hook method
        if (!mExpandableItemAdapter.onHookGroupExpand(groupPosition, fromUser)) {
            return false;
        }

        if (mPositionTranslator.expandGroup(groupPosition)) {
            final long packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPosition);
            final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);
            final int childCount = mPositionTranslator.getChildCount(groupPosition);

            notifyItemRangeInserted(flatPosition + 1, childCount);
        }

        {
            final long packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPosition);
            final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);

            notifyItemChanged(flatPosition);
        }

        // raise onGroupExpand() event
        if (mOnGroupExpandListener != null) {
            mOnGroupExpandListener.onGroupExpand(groupPosition, fromUser);
        }

        return true;
    }

    boolean isGroupExpanded(int groupPosition) {
        return mPositionTranslator.isGroupExpanded(groupPosition);
    }

    long getExpandablePosition(int flatPosition) {
        return mPositionTranslator.getExpandablePosition(flatPosition);
    }

    int getFlatPosition(long packedPosition) {
        return mPositionTranslator.getFlatPosition(packedPosition);
    }

    int[] getExpandedItemsSavedStateArray() {
        if (mPositionTranslator != null) {
            return mPositionTranslator.getSavedStateArray();
        } else {
            return null;
        }
    }

    void setOnGroupExpandListener(RecyclerViewExpandableItemManager.OnGroupExpandListener listener) {
        mOnGroupExpandListener = listener;
    }

    void setOnGroupCollapseListener(RecyclerViewExpandableItemManager.OnGroupCollapseListener listener) {
        mOnGroupCollapseListener = listener;
    }

    void restoreState(int[] adapterSavedState, boolean callHook, boolean callListeners) {
        mPositionTranslator.restoreExpandedGroupItems(
                adapterSavedState,
                (callHook ? mExpandableItemAdapter : null),
                (callListeners ? mOnGroupExpandListener : null),
                (callListeners ? mOnGroupCollapseListener : null));
    }

    void notifyGroupItemChanged(int groupPosition) {
        final long packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPosition);
        final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);

        if (flatPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(flatPosition);
        }
    }

    void notifyGroupAndChildrenItemsChanged(int groupPosition, Object payload) {
        final long packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPosition);
        final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);
        final int visibleChildCount = mPositionTranslator.getVisibleChildCount(groupPosition);

        if (flatPosition != RecyclerView.NO_POSITION) {
            notifyItemRangeChanged(flatPosition, 1 + visibleChildCount, payload);
        }
    }

    void notifyChildrenOfGroupItemChanged(int groupPosition, Object payload) {
        final int visibleChildCount = mPositionTranslator.getVisibleChildCount(groupPosition);

        // notify if the group is expanded
        if (visibleChildCount > 0) {
            final long packedPosition = ExpandableAdapterHelper.getPackedPositionForChild(groupPosition, 0);
            final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);

            if (flatPosition != RecyclerView.NO_POSITION) {
                notifyItemRangeChanged(flatPosition, visibleChildCount, payload);
            }
        }
    }

    void notifyChildItemChanged(int groupPosition, int childPosition, Object payload) {
        notifyChildItemRangeChanged(groupPosition, childPosition, 1, payload);
    }

    void notifyChildItemRangeChanged(int groupPosition, int childPositionStart, int itemCount, Object payload) {
        final int visibleChildCount = mPositionTranslator.getVisibleChildCount(groupPosition);

        // notify if the group is expanded
        if ((visibleChildCount > 0) && (childPositionStart < visibleChildCount)) {
            final long packedPosition = ExpandableAdapterHelper.getPackedPositionForChild(groupPosition, 0);
            final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);

            if (flatPosition != RecyclerView.NO_POSITION) {
                final int startPosition = flatPosition + childPositionStart;
                final int count = Math.min(itemCount, (visibleChildCount - childPositionStart));

                notifyItemRangeChanged(startPosition, count, payload);
            }
        }
    }

    void notifyChildItemInserted(int groupPosition, int childPosition) {
        mPositionTranslator.insertChildItem(groupPosition, childPosition);

        final long packedPosition = ExpandableAdapterHelper.getPackedPositionForChild(groupPosition, childPosition);
        final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);

        if (flatPosition != RecyclerView.NO_POSITION) {
            notifyItemInserted(flatPosition);
        }
    }

    void notifyChildItemRangeInserted(int groupPosition, int childPositionStart, int itemCount) {
        mPositionTranslator.insertChildItems(groupPosition, childPositionStart, itemCount);

        final long packedPosition = ExpandableAdapterHelper.getPackedPositionForChild(groupPosition, childPositionStart);
        final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);

        if (flatPosition != RecyclerView.NO_POSITION) {
            notifyItemRangeInserted(flatPosition, itemCount);
        }
    }

    void notifyChildItemRemoved(int groupPosition, int childPosition) {
        final long packedPosition = ExpandableAdapterHelper.getPackedPositionForChild(groupPosition, childPosition);
        final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);

        mPositionTranslator.removeChildItem(groupPosition, childPosition);

        if (flatPosition != RecyclerView.NO_POSITION) {
            notifyItemRemoved(flatPosition);
        }
    }

    void notifyChildItemRangeRemoved(int groupPosition, int childPositionStart, int itemCount) {
        final long packedPosition = ExpandableAdapterHelper.getPackedPositionForChild(groupPosition, childPositionStart);
        final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);

        mPositionTranslator.removeChildItems(groupPosition, childPositionStart, itemCount);

        if (flatPosition != RecyclerView.NO_POSITION) {
            notifyItemRangeRemoved(flatPosition, itemCount);
        }
    }

    void notifyGroupItemInserted(int groupPosition, boolean expanded) {
        int insertedCount = mPositionTranslator.insertGroupItem(groupPosition, expanded);
        if (insertedCount > 0) {
            final long packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPosition);
            final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);

            notifyItemInserted(flatPosition);

            // raise onGroupExpand() event
            raiseOnGroupExpandedSequentially(groupPosition, 1, false);
        }
    }

    void notifyGroupItemRangeInserted(int groupPositionStart, int count, boolean expanded) {
        int insertedCount = mPositionTranslator.insertGroupItems(groupPositionStart, count, expanded);
        if (insertedCount > 0) {
            final long packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPositionStart);
            final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);

            notifyItemRangeInserted(flatPosition, insertedCount);

            raiseOnGroupExpandedSequentially(groupPositionStart, count, false);
        }
    }

    private void raiseOnGroupExpandedSequentially(int groupPositionStart, int count, boolean fromUser) {
        if (mOnGroupExpandListener != null) {
            for (int i = 0; i < count; i++) {
                mOnGroupExpandListener.onGroupExpand(groupPositionStart + i, fromUser);
            }
        }
    }

    void notifyGroupItemRemoved(int groupPosition) {
        final long packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPosition);
        final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);

        int removedCount = mPositionTranslator.removeGroupItem(groupPosition);
        if (removedCount > 0) {
            notifyItemRangeRemoved(flatPosition, removedCount);
        }
    }

    void notifyGroupItemRangeRemoved(int groupPositionStart, int count) {
        final long packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPositionStart);
        final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);

        int removedCount = mPositionTranslator.removeGroupItems(groupPositionStart, count);
        if (removedCount > 0) {
            notifyItemRangeRemoved(flatPosition, removedCount);
        }
    }

    int getGroupCount() {
        return mExpandableItemAdapter.getGroupCount();
    }

    int getChildCount(int groupPosition) {
        return mExpandableItemAdapter.getChildCount(groupPosition);
    }

    private static ExpandableItemAdapter getExpandableItemAdapter(RecyclerView.Adapter adapter) {
        return WrapperAdapterUtils.findWrappedAdapter(adapter, ExpandableItemAdapter.class);
    }

    private static void safeUpdateExpandStateFlags(RecyclerView.ViewHolder holder, int flags) {
        if (!(holder instanceof ExpandableItemViewHolder)) {
            return;
        }

        final ExpandableItemViewHolder holder2 = (ExpandableItemViewHolder) holder;

        final int curFlags = holder2.getExpandStateFlags();
        final int mask = ~Constants.STATE_FLAG_IS_UPDATED;

        // append HAS_EXPANDED_STATE_CHANGED flag
        if ((curFlags != STATE_FLAG_INITIAL_VALUE) && (((curFlags ^ flags) & Constants.STATE_FLAG_IS_EXPANDED) != 0)) {
            flags |= Constants.STATE_FLAG_HAS_EXPANDED_STATE_CHANGED;
        }

        // append UPDATED flag
        if ((curFlags == STATE_FLAG_INITIAL_VALUE) || (((curFlags ^ flags) & mask) != 0)) {
            flags |= Constants.STATE_FLAG_IS_UPDATED;
        }

        holder2.setExpandStateFlags(flags);
    }
}
