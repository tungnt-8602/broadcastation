package com.example.broadcastation.presentation.home.item

import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ItemMoveCustomCallback(private val mAdapter: ItemTouchHelperContract?) :
    ItemTouchHelper.Callback() {
    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {}
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        mAdapter?.onRowMoved(viewHolder.adapterPosition, target.adapterPosition)
        Log.d("thetung", "onMove: ${viewHolder.adapterPosition} to ${target.adapterPosition}")
        return true
    }

//    override fun onMoved(
//        recyclerView: RecyclerView,
//        viewHolder: RecyclerView.ViewHolder,
//        fromPos: Int,
//        target: RecyclerView.ViewHolder,
//        toPos: Int,
//        x: Int,
//        y: Int
//    ) {
//        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
//        Log.d("thetung", "onMoved: $fromPos to $toPos")
//        mAdapter?.onRowMoved(fromPos, toPos)
//    }

    override fun onSelectedChanged(
        viewHolder: RecyclerView.ViewHolder?,
        actionState: Int
    ) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder is ItemRemoteCustomAdapter.ViewHolder) {
                mAdapter?.onRowSelected(viewHolder)
            }
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ) {
        super.clearView(recyclerView, viewHolder)
        if (viewHolder is ItemRemoteCustomAdapter.ViewHolder) {
            mAdapter?.onRowClear(viewHolder)
        }
    }

    interface ItemTouchHelperContract {
        fun onRowMoved(fromPosition: Int, toPosition: Int)
        fun onRowSelected(myViewHolder: ItemRemoteCustomAdapter.ViewHolder?)
        fun onRowClear(myViewHolder: ItemRemoteCustomAdapter.ViewHolder?)
    }
}
