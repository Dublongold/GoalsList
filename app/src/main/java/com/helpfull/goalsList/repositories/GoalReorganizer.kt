package com.helpfull.goalsList.repositories

import android.util.Log
import com.helpfull.goalsList.daos.GoalDao
import com.helpfull.goalsList.models.Goal
import kotlin.math.abs

/**
 * Intended for database reorganization. Contains two public methods: [startAdd] and
 * [startEdit], which make a reorganized addition or edition accordingly.
 *
 * @param dao [GoalDao] object. It might be better to pass a get method instead of an object.
 */
class GoalReorganizer(private val dao: GoalDao) {
    /**
     * Adds goal in the database. Depending on goals list  size, position of goal with the same
     * priority and a new goal priority, add in different ways.
     * @param goal Goal to add in the database.
     */
    fun startAdd(goal: Goal) {
        val goals = dao.getGoals().toMutableList()
        val samePriorityIndex = getSamePriorityIndex(goal, goals)
        if(samePriorityIndex != -1) {
            if(goals.size > samePriorityIndex + 1) {
                if(goals[samePriorityIndex + 1].priority - 1 > goal.priority) {
                    addCase3(goals[samePriorityIndex], goal)
                }
                else {
                    addCase2(samePriorityIndex, goal, goals)
                }
            }
            else {
                addCase1(goal, goals.removeLast())
            }
        }
        else {
            throw IllegalStateException( "Why should i reorganize, if there is no " +
                    "element priority with index ${goal.priority}?")
        }
    }

    /**
     * Adds goal in the database. Depending on goal's priority, passed priority, goal, goals list
     * size and position of goal with same priority, it can, for example, remove old and add new or
     * can change conflict priorities.
     * @param priority Priority of the goal before editing.
     * @param goal The edited goal to add to the database.
     * */
    fun startEdit(priority: Int, goal: Goal) {
        val goals = dao.getGoals().toMutableList()
        when {
            // User moved the goals up.
            priority > goal.priority -> editCase1(priority, goal, goals)
            // User moved the goals down.
            priority < goal.priority -> {
                // Remove goal before edition from the database and get it with index.
                val (indexOfRemovedGoal, removedGoal) = removeOldElement(priority, goals)
                // Get goal, which priority conflict with edited goal's priority.
                val samePriorityIndex = getSamePriorityIndex(goal, goals)
                // Runs the following code if we have in database goal with the same priority.
                if(samePriorityIndex != -1) {
                    // Checks if goal with the same priority is not at the end of the goal's
                    // list or difference between priorities is 1:
                    // 1, 2, {3}, |4|, 5. Here {N} - a old goal, and |N| - a conflicting goal.
                    if(goals.size > samePriorityIndex + 1
                        || abs(priority - goal.priority) != 1) {
                        // Checks if the goals just have swapped places:
                        // 1, 2, {3}, |4|, 5. Here {N} - a old goal, and |N| - a conflicting goal.
                        if(indexOfRemovedGoal == goals.indexOfFirst {
                            it.priority == goal.priority
                            }) {
                            editCase2(goal, goals[samePriorityIndex], removedGoal.priority)
                        }
                        // Otherwise, we have something like this:
                        // 1, {2}, 3, |4|, 5. Here {N} - a old goal, and |N| - a conflicting goal.
                        else {
                            editCase3(goal, goals)
                        }
                    }
                    // Otherwise, we have next case:
                    // 1, 2, 3, 4, {5}, |6|. Here {N} - a old goal, and |N| - a conflicting goal.
                    else {
                        editCase4(goal, goals.last())
                    }
                }
                // Otherwise, throws exception.
                else {
                    throw IllegalStateException( "Why should i reorganize, if there is no " +
                            "element priority with index ${goal.priority}?")
                }
            }
            // Otherwise, throws exception.
            else -> throw IllegalStateException( "Why should i reorganize, if priority ($priority)"
                    + "is goal.priority (${goal.priority})?")
        }
    }
    /** Invokes function [List.indexOfFirst] with condition: [Goal.priority] == [goal] priority. */
    private fun getSamePriorityIndex(goal: Goal, goals: List<Goal>) =
        goals.indexOfFirst { it.priority == goal.priority }
    //
    // Add cases section.
    //
    /** Should be invoked if size is samePriorityIndex + 1, meaning it is the last target.
     *
     * Function removes from the database removed goal, adds new goal and add removed goal to the
     * end changing the priority to the priority of the new goal + 1.
     * @param goal The goal to add to the database.
     * @param removedGoal Removed goal from the local goals list.
     * */
    private fun addCase1(goal: Goal, removedGoal: Goal) {
        // Creates new goal with new goal's priority + 1.
        val lastGoal = Goal(goal.priority + 1, removedGoal.text)
        // Runs the following code inside dao to make just one call to the "get" method.
        dao.run {
            // Delete removed goal.
            deleteGoal(removedGoal)
            // Add new goal.
            addGoal(goal)
            // Adds the changed goal, the last one in the past.
            dao.addGoal(lastGoal)
        }
    }
    /** Should be invoked if we have goals that create a sequential list of priorities, where the
     * next priority is greater than the last by 1.
     *
     * Function takes goals to be deleted, deletes them from the database, adds the new goal and
     * removed goals, but increases the priority of each by 1.
     * @param samePriorityIndex Index of goal with the same priority.
     * @param goal The goal to add to the database.
     * @param goals List of goals retrieved from the database.
     * */
    private fun addCase2(
        samePriorityIndex: Int,
        goal: Goal,
        goals: MutableList<Goal>
    ) {
        // This variable will help get the goals from the goals list that need to be removed.
        // It contains priority of the last conflict goal's priority. From the beginning, it
        // priority of the goal passed as a parameter.
        var lastPriority = goal.priority
        // It contains goals that create a sequential list of priorities, where the next priority
        // is greater than the last by 1.
        val removedGoals = goals.subList(samePriorityIndex, goals.size)
            .takeWhile {
                val result = it.priority == lastPriority
                lastPriority++
                result
            }
        // Runs the following code inside dao to make just one call to the "get" method.
        dao.run {
            // Deletes from the database removed goals.
            for(removedGoal in removedGoals) {
                deleteGoal(removedGoal)
            }
            // Adds the new goal.
            addGoal(goal)
            // Adds removed goals to the database, but increases their priority by one.
            for(removedGoal in removedGoals) {
                addGoal(
                    Goal(
                    removedGoal.priority + 1,
                    removedGoal.text
                )
                )
            }
        }
    }
    /** Should be invoked if next goal after a conflicting goal have bigger priority, meaning
     * we should only remove 1 goal and add 2 goals.
     *
     * Function creates new goal with priority (goal.priority + 1). After this, deletes removed goal
     * and adds the new goal and created goal.
     * @param goal The goal to add to the database.
     * @param sameGoal The goal with the same priority as the new goal.
     */
    private fun addCase3(goal: Goal, sameGoal: Goal) {
        // Creates a new goal created based on the same one.
        // It only changes priority to goal's priority + 1.
        val newGoal = Goal(goal.priority + 1,
            sameGoal.text)
        // Runs the following code inside dao to make just one call to the "get" method.
        dao.run {
            // Delete goal with the same priority.
            deleteGoal(sameGoal)
            // Adds new goal.
            addGoal(goal)
            // Adds a new goal created based on the same one.
            addGoal(newGoal)
        }
    }
    //
    // Edit cases section.
    //
    /**
     * Should be invoked when goal moved up.
     *
     * Function takes list of goals for removing, removes them from the database, add edited goal,
     * and adds removed goals in next way:
     * if the priority of a last goal (first - the edited goal, in the process - last added
     * removed goal) is equal to the priority of a removed goal, increases the priority of a
     * removed goal by 1, otherwise previous priority is maintained. Text won't be changed.
     * @param priority Priority of edited goal before editing.
     * @param goal The edited goal to add to the database.
     * @param goals The list of goals containing the goal before editing.
     */
    private fun editCase1(priority: Int, goal: Goal, goals: MutableList<Goal>) {
        Log.i(LOG_TAG, "Edit case 1.")
        // Gets a sublist of the conflicting goals to change their priority.
        val removedGoals = goals.subList(
            goals.indexOfFirst { it.priority == goal.priority },
            goals.indexOfFirst { it.priority == priority } + 1
        ).toMutableList()
        // Runs the following code inside dao to make just one call to the "get" method.
        dao.run {
            // Removes the conflicting goals from the database.
            for (removedGoal in removedGoals) {
                deleteGoal(removedGoal)
            }
            // Removes last removed goal. In this case, this is our passed as parameter goal, but
            // with the old priority.
            removedGoals.removeLast()
            // Adds edited goal to the database.
            addGoal(goal)
            // Creates variable with last added goal to the database.
            // It is used to check whether we should increase a priority of the next added goal
            // by 1 or not.
            var lastGoal = goal
            // In general, assigns to lastGoal a removed goal with either priority increased by one
            // or priority without changing (depending on whether a priority of removed goal is
            // equal to a last goal priority), as well as the removed goal text. After this, adds
            // the last goal to the database.
            for (removedGoal in removedGoals) {
                lastGoal = Goal(
                    if (removedGoal.priority == lastGoal.priority) {
                        removedGoal.priority + 1
                    } else {
                        removedGoal.priority
                    },
                    removedGoal.text
                )
                addGoal(
                    lastGoal
                )
            }
        }
    }

    /**
     * Should be invoked when two goals are swapped.
     *
     * Function just delete a conflicting goal and add edited goal and created goal with removed goal
     * priority and the conflicting goal text.
     *
     * @param goal The edited goal to add to the database.
     * @param conflictingGoal The goal with priority equal to the edited goal.
     * @param removedGoalPriority The priority of a removed goal.
     */
    private fun editCase2(goal: Goal, conflictingGoal: Goal, removedGoalPriority: Int) {
        Log.i(LOG_TAG, "Edit case 2.")
        // Runs the following code inside dao to make just one call to the "get" method.
        dao.run {
            // Deletes the conflicting goal from the database.
            deleteGoal(conflictingGoal)
            // Adds the edited goal to the database.
            addGoal(goal)
            // Adds the conflicting goal to the database, but set priority to the
            // removedGoalPriority.
            addGoal(Goal(removedGoalPriority, conflictingGoal.text))
        }
    }

    /**
     * Should be invoked when we have more than one goals for which their priority should be
     * changed.
     *
     * Function takes reversed list of goals from the conflicting goal to the goal with the
     * priority, which isn't equal to a previous priority. Their will be named removed goals.
     * After this, removes the reversed list, adds the edited goal and removed goals, but
     * decreases their priority by 1.
     *
     * @param goal The edited goal to add to the database.
     * @param goals The list of goals without the goal before editing.
     */
    private fun editCase3(goal: Goal, goals: MutableList<Goal>) {
        Log.i(LOG_TAG, "Edit case 3.")
        // This contains priority of the last conflicting goal. From the beginning, it's the edited
        // goal.
        var lastPriority = goal.priority
        // This variable contains list of goal to be deleted.
        // First, it reverses list.
        // Next, it takes sublist from the first conflicting goal to end of the goals list.
        // Next, it takes goals as long as the goal's priority conflicts with the priority of the
        // last removed goal. After this, makes our list mutable.
        val removedGoals = goals.reversed().let {
            it.subList(
                it.indexOfFirst { g -> g.priority == lastPriority },
                goals.size
            )
        }.takeWhile {
            val result = it.priority == lastPriority
            lastPriority--
            result
        }.toMutableList()
        // Runs the following code inside dao to make just one call to the "get" method.
        dao.run {
            // Removes removed goals from the database.
            for (removedGoal in removedGoals) {
                deleteGoal(removedGoal)
            }
            // Adds the edited goal to the database.
            addGoal(goal)
            // Adds removed goals to the database, but decreases their priority by 1.
            for (removedGoal in removedGoals) {
                addGoal(
                    Goal(
                        removedGoal.priority - 1,
                        removedGoal.text
                    )
                )
            }
        }
    }

    /**
     * Should be invoked when we swap two goals and the last goal in the end of the goals list.
     *
     * Function just creates a second to the last goal with priority [goal].priority - 1 and
     * the [removedGoal] text. After this, deletes the [removedGoal] from the database and adds
     * the edited goal and newly created goal.
     *
     * @param goal The edited goal to add to the database.
     * @param removedGoal Goal to be removed from the database.
     */
    private fun editCase4(goal: Goal, removedGoal: Goal) {
        Log.i(LOG_TAG, "Edit case 4.")
        // Creates a second to last goal based on a removedGoal, but its priority is 1 less than
        // the goal's priority.
        val preLastGoal = Goal(goal.priority - 1, removedGoal.text)
        // Runs the following code inside dao to make just one call to the "get" method.
        dao.run {
            // Deletes the removed goal from the database.
            deleteGoal(removedGoal)
            // Adds the edited goal to the database.
            addGoal(goal)
            // Adds the newly created goal to the database.
            addGoal(preLastGoal)
        }
    }
    /**
     * Removes the old goal from goals list and database. This is necessary to avoid errors when
     * adding the edited goals to the database.
     *
     * @param priority The priority of the goal before editing.
     * @param goals The list of goals from the database.
     *
     * @return Pair with the index of removed goal and the removed goal itself.
     */
    private fun removeOldElement(priority: Int, goals: MutableList<Goal>): Pair<Int, Goal> {
        // Contains the index of removed goal. In the fact, it's only needed for returns.
        val indexOfRemovedGoal = goals.indexOfFirst {
            it.priority == priority
        }
        // Takes the removed goal from list of goals.
        val removedGoal = goals[indexOfRemovedGoal]
        // Removes the removed goals from the database.
        dao.deleteGoal(removedGoal)
        // Removes the removed goals from list of goals.
        goals.remove(removedGoal)
        // Returns the index of removed goal and the removed goal itself.
        return indexOfRemovedGoal to removedGoal
    }

    companion object {
        /** Log tag for logging actions inside [GoalReorganizer] */
        const val LOG_TAG = "Goal reorganizer"
    }
}