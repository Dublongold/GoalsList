package com.helpfull.goalsList.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Goal with priority and text.
 * Priority determines how important a goal is among others.
 * The text contains only the content of the goal.
 *
 * Notes: The priority is [PrimaryKey], so it's impossible to contain two goals with the same
 * priority. And the priority not automatically generated, so you will have determine the priority
 * manually.
 */
@Entity(tableName = "goals")
data class Goal (
    @PrimaryKey(autoGenerate = false)
    val priority: Int,
    val text: String
)