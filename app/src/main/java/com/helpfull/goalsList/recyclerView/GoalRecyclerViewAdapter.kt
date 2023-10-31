package com.helpfull.goalsList.recyclerView

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.helpfull.goalsList.R
import com.helpfull.goalsList.models.Goal
import com.helpfull.goalsList.recyclerView.touchCallbacks.ItemMoveCallback

/**
 * Recycler view adapter for goals. Except standard methods of recycler view adapter, it contains
 * [updateList] method and implement [ItemMoveCallback.ItemTouchHelperContract] for dragging and
 * dropping goals.
 *
 * @param goals Initial list of goals. Can be empty.
 * @param clickCallback A callback that is invoked when the user clicks any goal. In this project,
 * it opens edit page.
 * @param saveMoveCallback A callback that is invoked when the user releases the goal after
 * dragging.
 */
class GoalRecyclerViewAdapter(
    private var goals: MutableList<Goal>,
    private val clickCallback: (Goal) -> Unit,
    private val saveMoveCallback: (Int, Goal) -> Unit
): RecyclerView.Adapter<GoalRecyclerViewAdapter.GoalViewHolder>(),
    ItemMoveCallback.ItemTouchHelperContract {

    private var selectedGoalIndex = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        return GoalViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.goal_item, parent, false))
    }

    override fun getItemCount(): Int {
        return goals.size
    }

    /**
     * Updates the list of goals and notifies about it.
     *
     * @param newList New list of goals.
     */
    fun updateList(newList: MutableList<Goal>) {
        goals = newList
        notifyItemRangeChanged(0, goals.size)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        // Takes the desired target.
        val goal = goals[position]
        // Runs the following code inside itemVies to make code cleaner.
        holder.itemView.run {
            // Sets the goal's text.
            findViewById<TextView>(R.id.goalText).text = goal.text
            // Sets the goal's priority.
            findViewById<TextView>(R.id.goalPriority).text = goal.priority.toString()
            // Sets on click listener.
            setOnClickListener {
                clickCallback(goal)
            }
        }
    }

    /**
     * Invokes when goal changes its position. In current implementation, changes position of
     * selected goal and notifies about it.
     *
     * @param fromPosition Position of the selected goal before moving.
     * @param toPosition Position of the selected goal after moving.
     */
    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        // Removes the selected goal with index fromPosition.
        val item = goals.removeAt(fromPosition)
        // Adds the selected goal at the specified position (index).
        goals.add(toPosition, item)
        // Notifies about the moved goal.
        notifyItemMoved(fromPosition, toPosition)
    }

    /**
     * Invokes when goal are selected. If viewHolder and itemView isn't null, takes the goal's
     * priority and goal's index by priority, sets the foreground appear of goal as if it is
     * selected.
     *
     * @param viewHolder A view holder with selected goal.
     */
    override fun onRowSelected(viewHolder: GoalViewHolder?) {
        // Runs the following code if viewHolder and itemView isn't null.
        viewHolder?.itemView?.let {
            selectedGoalIndex = getGoalIndex(getGoalPriority(it))
            // Makes the foreground appear of goal as if it is selected.
            it.foreground = ResourcesCompat.getDrawable(
                it.resources, R.drawable.item_selected_foreground, null
            )
        }
    }

    /**
     * Returns the index of the first goal with the corresponding [priority] using
     * [List.indexOfFirst].
     * @param priority The priority of the goal whose index is to be returned.
     * @return Index of first [Goal] with the corresponding [priority].
     */
    private fun getGoalIndex(priority: Int): Int {
        return goals.indexOfFirst { it.priority == priority }
    }
    /**
     * Finds view by [R.id.goalPriority] using [View.findViewById], takes its [TextView.getText]
     * converts it first to [String] and then to [Int].
     *
     * @param view [View] containing the [Goal] priority.
     * @return [Goal.priority] is taken from TextView.
     */
    private fun getGoalPriority(view: View): Int {
        return view.findViewById<TextView>(R.id.goalPriority).text.toString()
            .toInt()
    }

    /**
     * Invokes when the user release the selected goal.
     *
     * Function takes the [Goal.priority] of selected [Goal] and its position. If position was
     * changed, creates [Goal] with new [Goal.priority], which depends on position, invokes
     * [saveMoveCallback] and shows [Toast] with text "Changed!".
     * After if branch, sets [selectedGoalIndex] to -1.
     *
     * @param viewHolder A view holder containing the moved goal.
     */
    override fun onRowClear(viewHolder: GoalViewHolder?) {
        // Runs the following code if viewHolder and itemView isn't null.
        viewHolder?.itemView?.let {
            // Makes the goal's foreground transparent.
            it.foreground = null
            // Takes the priority of the selected goal before moving.
            val oldPriority = getGoalPriority(it)
            // Takes the position of the selected goal before moving.
            val position = getGoalIndex(oldPriority)
            // If the selected goal was moved.
            if (selectedGoalIndex != position) {
                // Creates the edited goal with new priority. Properties:
                // Priority - Depends on the new position of the selected goal.
                // Text - goals[position].text.
                val editedGoal = Goal(
                    // To explain this branches of if, i will use next explanation:
                    // 1, 2, {3}, 4, 5 => 1, 2, 4, |5|, {3}. Here {N} - selected goal, |N| - goal
                    // whose priority will be taken.
                    //
                    // Here we have something like that: 1, {2}, 3, 4, 5 => 1, 3, |4|, {2}, 5.
                    //
                    // Runs the following code if the selected goal shifted to bottom and the
                    // priority of the goal that is above the selected one is more than
                    // oldPriority:
                    priority = if(position > 0 && goals[position - 1].priority > oldPriority) {
                        goals[position - 1].priority
                    }
                    // Here we have something like that: 1, 2, 3, {4}, 5 => 1, {4}, |2|, 3, 5.
                    //
                    // Runs the following code if the selected goal shifted to top and the
                    // priority of the goal that is under the selected one is less than
                    // oldPriority:
                    else if (goals.size > 2 && goals[position + 1].priority < oldPriority) {
                        goals[position + 1].priority
                    }
                    // Here we have one of the next: {1}, 2 => |2|, {1}. 1, {2} => {2}, |1|
                    // Runs the following code if we have only two goals, run the following code:
                    else if (goals.size == 2){
                        // Chooses the opposite position to the position.
                        goals[position xor 1].priority
                    }
                    // If all if branches is false, just returns -1.
                    else {
                        Log.i(LOG_TAG, "Returned goals[position].priority.")
                        -1
                    }, text = goals[position].text
                )
                // If the priority of the edited goal isn't -1, runs the following code.
                if(editedGoal.priority != -1) {
                    // Invokes callback to edit the selected goal in the database.
                    saveMoveCallback(oldPriority, editedGoal)
                    // Shows a Toast message to the user.
                    Toast
                        .makeText(viewHolder.itemView.context, "Changed!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            // Sets the selected goal index to -1 because the goal was released.
            selectedGoalIndex = -1
        }
    }

    /** The view holder with views, that represent properties of goal (priority, text). */
    class GoalViewHolder(view: View): ViewHolder(view)

    companion object {
        /** Default log tag for this recycler view adapter. RVA means Recycler View Adapter. */
        const val LOG_TAG = "Goal RVA"
    }
}