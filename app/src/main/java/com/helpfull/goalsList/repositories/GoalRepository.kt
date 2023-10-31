package com.helpfull.goalsList.repositories

import android.util.Log
import com.helpfull.goalsList.models.Goal
import com.helpfull.goalsList.models.databases.GoalDatabase
import org.koin.java.KoinJavaComponent.inject

class GoalRepository {
    private val database: GoalDatabase by inject(GoalDatabase::class.java)
    private val dao
        get() = database.getGoalDao()

    /** Object for database reorganization. Its methods are invoked only when we have conflicting
     * goals in the database. */
    private val goalReorganizer = GoalReorganizer(::dao.get())

    /**
     * Adds new goal in the database. If the database contains zero goals with [goal].priority,
     * just adds goal in the database, otherwise, invokes [GoalReorganizer.startAdd] method.
     *
     * @param goal New goal to add to the database.
     */
    fun add(goal: Goal) {
        if(dao.findGoalsByPriority(goal.priority).isEmpty()) {
            dao.addGoal(goal)
            Log.i(LOG_TAG, "Add new goal with priority ${goal.priority}.")
        }
        else {
            goalReorganizer.startAdd(goal)
            Log.i(LOG_TAG, "Goal with priority ${goal.priority} added," +
                    "but goal with same priority was increased.")
        }
    }

    /**
     * Edits goal in the database. If the database contains zero goals with [editedGoal].priority
     * or [oldPriority] is equal to [editedGoal].priority, just edits goal in the database,
     * otherwise, invokes [GoalReorganizer.startEdit] method.
     *
     * @param oldPriority The priority of the goal before editing.
     * @param editedGoal The edited goal.
     */
    fun edit(oldPriority: Int, editedGoal: Goal) {
        // Check if there are not goals with the same priority.
        val sameGoalsDoNotExists = dao.findGoalsByPriority(editedGoal.priority).isEmpty()
        // If there are not goals with the same priority or newGoals priority are same, just send
        // the corresponding message and edit goal.
        if(sameGoalsDoNotExists || oldPriority == editedGoal.priority) {
            dao.editGoalByPriority(oldPriority, editedGoal.text, editedGoal.priority)
            Log.i(LOG_TAG, "Goal with priority ${editedGoal.priority} edited.")
        // Otherwise, send the corresponding message and do reorganization.
        } else {
            goalReorganizer.startEdit(oldPriority, editedGoal)
            Log.i(LOG_TAG, "Goal edited with changing other goals position.")
        }
    }

    /**
     * Aligns by priorities. For example: If we had goals with priorities 1, 2, 4, 7, 8, 20,
     * after invoking this method, we will have goals with priorities 1, 2, 3, 4, 5, 6, but with
     * the same texts and order.
     */
    fun alignByPriorities(): MutableList<Goal> {
        var goals = getGoals().toMutableList()
        var necessary = false
        for(i in goals.indices) {
            if(i == 0) continue
            if(goals[i - 1].priority + 1 != goals[i].priority) {
                necessary = true
                break
            }
        }
        if(necessary) {
            goals = goals.mapIndexed { index, goal ->
                Goal(index + 1, goal.text)
            }.toMutableList()
            dao.run {
                deleteGoals()
                for(goal in goals) {
                    addGoal(goal)
                }
            }
        }
        return goals
    }

    /**
     * Deletes a goal with the corresponding priority.
     * @return true, if goal with the corresponding priority was found and deleted;
     * otherwise - false.
     *
     * @param priority The priority of goal to be deleted.
     */
    fun delete(priority: Int): Boolean {
        // Gets list of goals with same priority.
        val goalsByPriority = dao.findGoalsByPriority(priority)
        // If list of goals isn't empty, delete goals, show in log message and return true.
        if(goalsByPriority.isNotEmpty()) {
            dao.deleteGoal(goalsByPriority.first())
            Log.i(LOG_TAG, "Goal with priority \"$priority\".")
            return true
        }
        // Otherwise, show in corresponding log message and return false.
        Log.i(LOG_TAG, "Goal with priority $priority wasn't deleted.")
        return false
    }

    /**
     * Deletes all goals from the database.
     * @return If deletion is successful - true, otherwise - false.
     */
    fun deleteAll(): Boolean {
        // Try to delete all goals from the database. But first check if goals exist in the database.
        return if(getGoals().isNotEmpty()) {
            // Try to delete. If there no error, returns true.
            try {
                dao.deleteGoals()
                true
            // Otherwise - false and message in log.
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Exception during deletion.", e)
                false
            }
        }
        else {
            false
        }
    }

    /**
     * Gets all goals from the database, sorted by priority.
     * @return [List] of goals.
     */
    fun getGoals(): List<Goal> {
        return dao.getGoals().sortedBy(Goal::priority)
    }
    
    companion object {
        /** Log tag for logging actions inside [GoalRepository] */
        const val LOG_TAG = "Goal repository"
    }
}