package com.helpfull.goalsList.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.helpfull.goalsList.models.Goal

@Dao
interface GoalDao {
    /** Edits goal with priority [oldPriority] in the database to the goal with priority - [priority] and
     * text - [text]
     * @param oldPriority The priority of the goal before editing.
     * @param text The text of the edited goal.
     * @param priority The priority of the edited goal.
     */
    @Query("update goals set text = :text, priority = :priority where priority = :oldPriority")
    fun editGoalByPriority(oldPriority: Int, text: String, priority: Int)
    /**
     * Deletes goal with the same properties.
     * @param goal The goal to be deleted from the database.
     * */
    @Delete
    fun deleteGoal(goal: Goal)
    /** Deletes all goals from the database. You should use this method carefully. */
    @Query("delete from goals")
    fun deleteGoals()
    /** Add [goal] in the database.
     * @param goal The goal to be added to the database.
     */
    @Insert
    fun addGoal(goal: Goal)
    /** Finds goals with the same priority as [priority] and return list of them.
     * @param priority The priority by which goals will be searched.
     *
     * @return [List] of goals with the same priority as [priority]. May be empty.
     */
    @Query("select * from goals where priority = :priority")
    fun findGoalsByPriority(priority: Int): List<Goal>

    /**
     * Gets all goals from the database.
     * @return Goals from the database. May be empty.
     */
    @Query("select * from goals")
    fun getGoals(): List<Goal>
}