package com.helpfull.goalsList.models.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.helpfull.goalsList.daos.GoalDao
import com.helpfull.goalsList.models.Goal

/**
 * Database with goals. That's all.
 */
@Database(version = 1, entities = [Goal::class], exportSchema = false)
abstract class GoalDatabase: RoomDatabase() {
    /**
     * Dao object for interaction with goal in the database.
     */
    abstract fun getGoalDao(): GoalDao
}