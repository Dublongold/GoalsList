package com.helpfull.goalsList.views.main

import androidx.lifecycle.ViewModel
import com.helpfull.goalsList.models.Goal
import com.helpfull.goalsList.repositories.GoalRepository
import org.koin.java.KoinJavaComponent.inject
import com.helpfull.goalsList.views.submitDelete.SubmitDeleteViewModel

/**
 * View model for [MainFragment]. It can retrieve goals from repository, edit goals and align goals
 * by priorities. Delete all goals can only [SubmitDeleteViewModel].
 */
class MainViewModel: ViewModel() {
    /** Singleton object of [GoalRepository]. In this viewModel, it is used to retrieving,
     * editing goals and aligning them by priorities*/
    private val repository: GoalRepository by inject(GoalRepository::class.java)

    /** Invokes [GoalRepository.getGoals] of [repository]. */
    fun getGoals() = repository.getGoals()
    /** Invokes [GoalRepository.edit] of [repository]. */
    fun editGoal(oldPriority: Int, newGoal: Goal)  = repository.edit(oldPriority, newGoal)
    /** Invokes [GoalRepository.alignByPriorities] of [repository]. */
    fun alignByPriorities() = repository.alignByPriorities()
}