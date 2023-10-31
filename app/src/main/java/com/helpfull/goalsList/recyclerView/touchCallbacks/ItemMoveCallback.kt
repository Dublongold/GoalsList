package com.helpfull.goalsList.recyclerView.touchCallbacks

import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.helpfull.goalsList.models.Goal
import com.helpfull.goalsList.recyclerView.GoalRecyclerViewAdapter

class ItemMoveCallback(private val adapter: ItemTouchHelperContract ): ItemTouchHelper.Callback() {
    /**
     * Returns whether ItemTouchHelper should start a drag and drop operation if an item is
     * long pressed.
     * @return true
     */
    override fun isLongPressDragEnabled(): Boolean {
        return true
    }
    /**
     * Returns whether ItemTouchHelper should start a swipe operation if a pointer is swiped
     * over the View.
     * @return false
     */
    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: ViewHolder
    ): Int {
        // Allows to only move the element up and down.
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: ViewHolder,
        target: ViewHolder
    ): Boolean {
        // Invokes in adapter the corresponding method.
        adapter.onRowMoved(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
        // Just nothing.
    }

    override fun onSelectedChanged(viewHolder: ViewHolder?, actionState: Int) {
        // Checks if action state isn't idle and if viewHolder is GoalViewHolder.
        if(actionState != ItemTouchHelper.ACTION_STATE_IDLE &&
            viewHolder is GoalRecyclerViewAdapter.GoalViewHolder) {
            // Runs the corresponding method.
            adapter.onRowSelected(viewHolder)
        }
        // Invokes the super method.
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: ViewHolder) {
        // Invokes the super method.
        super.clearView(recyclerView, viewHolder)
        // If viewHolder is GoalViewHolder, invokes the corresponding method.
        if(viewHolder is GoalRecyclerViewAdapter.GoalViewHolder) {
            adapter.onRowClear(viewHolder)
        }
    }
    /** Contract developed for [RecyclerView.Adapter]. It is needed to implement drag and drop. */
    interface ItemTouchHelperContract {
        /** Should be invoked when element changes its position.
         * @param fromPosition Position of the selected element before moving.
         * @param toPosition Position of the selected element after moving.
         */
        fun onRowMoved(fromPosition: Int, toPosition: Int)

        /** Should be invoked when element are selected.
         * @param viewHolder A view holder with selected element.
         */
        fun onRowSelected(viewHolder: GoalRecyclerViewAdapter.GoalViewHolder?)

        /** Should be invoked when the user release the selected element.
         * @param viewHolder A view holder containing the moved element.
         */
        fun onRowClear(viewHolder: GoalRecyclerViewAdapter.GoalViewHolder?)
    }
}