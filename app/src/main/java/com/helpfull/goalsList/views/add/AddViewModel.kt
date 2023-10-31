package com.helpfull.goalsList.views.add

import androidx.lifecycle.ViewModel
import com.helpfull.goalsList.models.Goal
import com.helpfull.goalsList.repositories.GoalRepository
import com.helpfull.goalsList.views.edit.EditFragment
import org.koin.java.KoinJavaComponent.inject

/**
 * View model for [EditFragment]. It can only add new goal.
 */
class AddViewModel: ViewModel() {
    /** Singleton object of [GoalRepository]. In this viewModel, it is used to add goals. */
    private val repository: GoalRepository by inject(GoalRepository::class.java)

    /** Invokes [GoalRepository.add] of [repository].
     * @param goal New goal to be added into the database. */
    fun add(goal: Goal) = repository.add(goal)
}